package com.yablonskyi.character.presentation.notes

import android.content.Context
import com.yablonskyi.data.di.CharacterIoDispatcher
import com.yablonskyi.model.character.Note
import com.yablonskyi.model.character.TextFormat
import com.yablonskyi.model.character.validateNoteId
import com.yablonskyi.model.character.validateNotes
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class NoteDraft(
    val sessionId: String,
    val characterId: Long,
    val noteId: String,
    val characterFingerprint: String,
    val original: Note?,
    val current: Note,
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0,
    val typingFormats: Set<TextFormat> = emptySet(),
) {
    fun validate() {
        validateNoteId(sessionId)
        validateNoteId(noteId)
        require(characterId > 0 && current.id == noteId && (original == null || original.id == noteId))
        require(characterFingerprint.isNotBlank())
        original?.let { listOf(it).validateNotes() }
        current.text.validate()
        require(selectionStart in 0..current.text.plainText.length && selectionEnd in 0..current.text.plainText.length)
        require(current.text.isBoundary(selectionStart) && current.text.isBoundary(selectionEnd))
    }
}

interface NoteDraftStorage {
    suspend fun read(sessionId: String, characterId: Long, noteId: String): NoteDraft?
    suspend fun write(draft: NoteDraft)
    suspend fun delete(sessionId: String, characterId: Long, noteId: String)
    suspend fun cleanupOrphans()
}

/** Drafts live outside SavedStateHandle so large bodies never enter the saved-state Bundle. */
class FileNoteDraftStorage @Inject constructor(
    @ApplicationContext context: Context,
    @param:CharacterIoDispatcher private val io: CoroutineDispatcher,
) : NoteDraftStorage {
    private val directory = File(context.filesDir, "note-drafts")
    private val mutex = Mutex()
    private val json = Json { encodeDefaults = true }

    override suspend fun read(sessionId: String, characterId: Long, noteId: String): NoteDraft? = withContext(io) {
        mutex.withLock {
            val file = file(sessionId, characterId, noteId)
            if (!file.exists()) return@withLock null
            json.decodeFromString<NoteDraft>(file.readText(Charsets.UTF_8)).also { draft ->
                draft.validate()
                require(draft.sessionId == sessionId && draft.characterId == characterId && draft.noteId == noteId)
            }
        }
    }

    override suspend fun write(draft: NoteDraft) = withContext(io) {
        draft.validate()
        mutex.withLock {
            Files.createDirectories(directory.toPath())
            val target = file(draft.sessionId, draft.characterId, draft.noteId).toPath()
            val temp = Files.createTempFile(directory.toPath(), "draft-", ".tmp")
            try {
                java.io.FileOutputStream(temp.toFile()).use { output ->
                    output.write(json.encodeToString(draft).toByteArray(Charsets.UTF_8))
                    output.flush()
                    output.fd.sync()
                }
                Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            } finally {
                Files.deleteIfExists(temp)
            }
            Unit
        }
    }

    override suspend fun delete(sessionId: String, characterId: Long, noteId: String) = withContext(io) {
        mutex.withLock { Files.deleteIfExists(file(sessionId, characterId, noteId).toPath()); Unit }
    }

    override suspend fun cleanupOrphans() = withContext(io) {
        mutex.withLock {
            val cutoff = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000
            directory.listFiles()?.filter { it.isFile && it.lastModified() < cutoff &&
                (it.name.endsWith(".json") || it.name.endsWith(".tmp")) }?.forEach { it.delete() }
            Unit
        }
    }

    private fun file(sessionId: String, characterId: Long, noteId: String): File {
        validateNoteId(sessionId)
        validateNoteId(noteId)
        require(characterId > 0)
        return File(directory, "$sessionId-$characterId-$noteId.json")
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NoteDraftStorageModule {
    @Binds abstract fun bindNoteDraftStorage(storage: FileNoteDraftStorage): NoteDraftStorage
}