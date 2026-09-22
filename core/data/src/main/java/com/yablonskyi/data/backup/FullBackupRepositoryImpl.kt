package com.yablonskyi.data.backup

import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteFullException
import android.system.ErrnoException
import android.system.OsConstants
import com.yablonskyi.data.dao.BackupDao
import com.yablonskyi.domain.backup.BackupAccessGate
import com.yablonskyi.domain.backup.BackupAccessState
import com.yablonskyi.domain.backup.BackupRecoveryRequiredException
import com.yablonskyi.domain.repository.*
import com.yablonskyi.model.backup.BackupValidationException
import com.yablonskyi.model.backup.BackupValidationFailure
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import java.io.Closeable
import java.io.EOFException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.CharacterCodingException
import java.nio.file.Files
import java.time.Instant
import java.util.UUID
import java.util.zip.ZipException

/**
 * Internal orchestration bound by [com.yablonskyi.data.di.BackupModule]. [stagingDirectory] must be
 * a dedicated private scratch directory, not filesDir.
 */
internal class FullBackupRepositoryImpl(
    private val dao: BackupDao,
    private val gate: BackupAccessGate,
    private val coordinator: BackupRestoreCoordinator,
    private val stagingDirectory: File,
    private val io: CoroutineDispatcher,
    private val appVersion: String,
    private val now: () -> Instant,
    private val codec: BackupArchiveCodec = BackupArchiveCodec(),
) : FullBackupRepository {
    private val transport = BackupDocumentTransport(io)
    private val operationMutex = Mutex()
    private val mutableProgress = MutableStateFlow(BackupProgress.IDLE)
    override val progress = mutableProgress.asStateFlow()
    private var prepared: PendingRestore? = null

    override suspend fun createBackup(
        openDestination: () -> OutputStream,
        removePartialDestination: () -> Unit,
    ): BackupResult<Unit> = operation(BackupProgress.SNAPSHOTTING) {
        var archive: File? = null
        Closeable { archive?.let(::deleteOwned) }.use {
            gate.access {
                val snapshot = dao.snapshot()
                val assetIds = snapshot.characters.mapNotNull { it.imagePath }.distinct()
                    .associateWith { UUID.randomUUID().toString() }
                val images = assetIds.entries.associate { (path, id) -> id to File(path) }
                archive = codec.write(snapshot.toBackup(assetIds), images,
                    stagingDirectory, appVersion, now())
            }
            currentCoroutineContext().ensureActive()
            mutableProgress.value = BackupProgress.WRITING
            transport.writeArchive(checkNotNull(archive), openDestination, removePartialDestination)
            BackupResult.Success(Unit)
        }
    }

    override suspend fun prepareRestore(openSource: () -> InputStream): BackupResult<PreparedRestore> {
        var candidate: PendingRestore? = null
        var delivered = false
        var result: BackupResult<PreparedRestore>? = null
        var cancellation: CancellationException? = null
        try {
            result = operation(BackupProgress.VALIDATING) {
                gate.access { Unit }
                clearPrepared()
                transport.withStagedSource(stagingDirectory, openSource) { archive ->
                    candidate = PendingRestore(UUID.randomUUID().toString(), codec.read(archive, stagingDirectory))
                }
                currentCoroutineContext().ensureActive()
                val pending = checkNotNull(candidate)
                prepared = pending
                BackupResult.Success(PreparedRestore(
                    token = pending.token,
                    summary = BackupSummary(
                        createdAt = Instant.parse(pending.staged.manifest.createdAt),
                        sourceAppVersion = pending.staged.manifest.sourceAppVersion,
                        counts = pending.staged.manifest.counts,
                        portraitCount = pending.staged.manifest.assets.size,
                    ),
                ))
            }
            delivered = result is BackupResult.Success
        } catch (cancelled: CancellationException) {
            cancellation = cancelled
            throw cancelled
        } finally {
            // Also handles prompt cancellation while returning from the IO dispatcher: no lost token
            // may retain its staging. Identity checking cannot discard a newer preparation.
            if (!delivered) candidate?.let { abandoned ->
                try {
                    withContext(NonCancellable + io) {
                        operationMutex.withLock {
                            if (prepared === abandoned) prepared = null
                            deleteOwned(abandoned.staged.directory)
                        }
                    }
                } catch (cleanup: IOException) {
                    if (cancellation != null) cancellation.addSuppressed(cleanup)
                    else result = failure(BackupError.IO_FAILURE)
                } catch (cleanup: SecurityException) {
                    if (cancellation != null) cancellation.addSuppressed(cleanup)
                    else result = failure(BackupError.PERMISSION_DENIED)
                }
            }
        }
        return checkNotNull(result)
    }

    override suspend fun restore(token: String): BackupResult<Unit> = operation(BackupProgress.RESTORING) {
        val pending = prepared?.takeIf { it.token == token }
            ?: return@operation BackupResult.Failure(BackupError.EXPIRED_RESTORE)
        prepared = null
        Closeable { deleteOwned(pending.staged.directory) }.use {
            coordinator.restore(pending.staged)
            BackupResult.Success(Unit)
        }
    }

    override suspend fun discardPreparedRestore(token: String) = withContext(io) {
        operationMutex.withLock {
            if (prepared?.token == token) clearPrepared()
        }
    }

    /** Must run before production consumers. Journal recovery never depends on temporary archives. */
    override suspend fun recover(): BackupResult<Unit> = operation(BackupProgress.RECOVERING) {
        coordinator.recover()
        clearPrepared()
        // Only codec/transport-generated names directly beneath the dedicated scratch directory.
        stagingDirectory.listFiles()?.filter { ownedName.matches(it.name) }?.forEach(::deleteOwned)
        BackupResult.Success(Unit)
    }

    private suspend fun <T> operation(
        stage: BackupProgress,
        block: suspend () -> BackupResult<T>,
    ): BackupResult<T> {
        if (!operationMutex.tryLock()) return BackupResult.Failure(BackupError.BUSY)
        try {
            mutableProgress.value = stage
            return withContext(io) {
                Files.createDirectories(stagingDirectory.toPath())
                block()
            }
        } catch (failure: BackupArchiveException) {
            return failure(failure.error)
        } catch (failure: BackupValidationException) {
            return failure(when (failure.failure) {
                BackupValidationFailure.UNSUPPORTED_VERSION -> BackupError.UNSUPPORTED_VERSION
                BackupValidationFailure.SIZE_LIMIT_EXCEEDED -> BackupError.SIZE_LIMIT_EXCEEDED
                BackupValidationFailure.INVALID_DATA -> BackupError.INVALID_ARCHIVE
            })
        } catch (_: BackupRecoveryRequiredException) {
            return failure(BackupError.RECOVERY_REQUIRED)
        } catch (_: SerializationException) {
            return failure(BackupError.INVALID_ARCHIVE)
        } catch (_: ZipException) {
            return failure(BackupError.INVALID_ARCHIVE)
        } catch (_: EOFException) {
            return failure(BackupError.INVALID_ARCHIVE)
        } catch (_: CharacterCodingException) {
            return failure(BackupError.INVALID_ARCHIVE)
        } catch (_: SecurityException) {
            return failure(BackupError.PERMISSION_DENIED)
        } catch (_: SQLiteFullException) {
            return failure(BackupError.INSUFFICIENT_STORAGE)
        } catch (_: SQLiteException) {
            return failure(BackupError.IO_FAILURE)
        } catch (error: IOException) {
            val errno = generateSequence<Throwable>(error) { it.cause }.filterIsInstance<ErrnoException>().firstOrNull()?.errno
            return failure(when (errno) {
                OsConstants.ENOSPC -> BackupError.INSUFFICIENT_STORAGE
                OsConstants.EACCES, OsConstants.EPERM -> BackupError.PERMISSION_DENIED
                else -> BackupError.IO_FAILURE
            })
        } finally {
            mutableProgress.value = BackupProgress.IDLE
            operationMutex.unlock()
        }
    }

    private fun failure(error: BackupError) = BackupResult.Failure(
        if (gate.state.value == BackupAccessState.RECOVERY_REQUIRED) BackupError.RECOVERY_REQUIRED else error,
    )

    private fun clearPrepared() {
        prepared?.let { deleteOwned(it.staged.directory) }
        prepared = null
    }

    private fun deleteOwned(file: File) {
        check(file.parentFile?.canonicalFile == stagingDirectory.canonicalFile && ownedName.matches(file.name))
        if (!Files.exists(file.toPath())) return
        // Files.walk does not follow symbolic links. Never traverse live portraits or user documents.
        Files.walk(file.toPath()).use { paths ->
            paths.sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) }
        }
    }

    private data class PendingRestore(val token: String, val staged: StagedBackup)

    private companion object {
        val ownedName = Regex("(?:backup(?:-document)?-\\d+\\.zip|restore-\\d+)")
    }
}
