package com.yablonskyi.data.backup

import com.yablonskyi.domain.repository.BackupError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files

/** Stream ownership and bounded private staging; no document-provider or live-store dependencies. */
internal class BackupDocumentTransport(
    private val io: CoroutineDispatcher,
    private val maximumArchiveBytes: Long = BackupArchiveLimits().archiveBytes,
) {
    init {
        require(maximumArchiveBytes > 0)
    }

    /**
     * Closes the source before invoking validation. The temporary ZIP exists only within [consume];
     * consumers must not return it or retain it. Decoded assets belong in a separate staging directory.
     * Deletion runs on failure and cancellation, including cancellation at the dispatcher boundary.
     */
    suspend fun <T> withStagedSource(
        parent: File,
        openSource: () -> InputStream,
        consume: suspend (File) -> T,
    ): T = withContext(io) {
        val file = Files.createTempFile(parent.toPath(), "backup-document-", ".zip").toFile()
        Closeable { Files.deleteIfExists(file.toPath()) }.use {
            openSource().use { source ->
                file.outputStream().use { output -> copy(source, output) }
            }
            currentCoroutineContext().ensureActive()
            consume(file)
        }
    }

    /**
     * Copies a completed private archive; the caller retains ownership of [archive]. All callbacks
     * run on IO. [removePartialDestination] must target only the newly created backup document,
     * never a pre-existing user file. It is attempted even if opening that document fails.
     * Cleanup failures are suppressed on the original error, rather than replacing it.
     * Success requires both copy and stream close to succeed.
     */
    suspend fun writeArchive(
        archive: File,
        openDestination: () -> OutputStream,
        removePartialDestination: () -> Unit,
    ) = withContext(io) {
        currentCoroutineContext().ensureActive()
        checkSize(archive.length())
        // Open the private source first: a missing source must not touch the destination.
        val source = archive.inputStream()
        var complete = false
        Closeable { if (!complete) removePartialDestination() }.use {
            source.use {
                openDestination().use { output -> copy(source, output) }
            }
            currentCoroutineContext().ensureActive()
            complete = true
        }
    }

    private suspend fun copy(source: InputStream, output: OutputStream) {
        val context = currentCoroutineContext()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            context.ensureActive()
            val count = source.read(buffer)
            context.ensureActive()
            if (count < 0) break
            if (count == 0) {
                // Defensive support for providers returning zero without reaching EOF.
                val next = source.read()
                context.ensureActive()
                if (next < 0) break
                checkSize(++total)
                output.write(next)
            } else {
                total += count
                checkSize(total)
                output.write(buffer, 0, count)
            }
        }
    }

    private fun checkSize(size: Long) {
        if (size > maximumArchiveBytes) {
            throw BackupArchiveException(BackupError.SIZE_LIMIT_EXCEEDED, "Backup document exceeds size limit")
        }
    }
}
