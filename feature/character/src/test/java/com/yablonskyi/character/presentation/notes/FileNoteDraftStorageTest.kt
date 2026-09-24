package com.yablonskyi.character.presentation.notes

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import com.yablonskyi.model.character.Note
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.Dispatchers
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FileNoteDraftStorageTest {
    private val sessionId = "cd6b6172-0a95-40d4-aa83-8b31f8a9180d"
    private val noteId = "b8d6919b-4422-48de-b55f-38c14ee94ef2"

    @Test fun givenSavedDraft_whenStoreRecreated_thenRecoversAndDeletesIt() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val draft = NoteDraft(sessionId, 7, noteId, "fingerprint", null, Note(noteId, "Draft"))
        val first = FileNoteDraftStorage(context, Dispatchers.IO)
        first.write(draft)
        val second = FileNoteDraftStorage(context, Dispatchers.IO)
        assertEquals(draft, second.read(sessionId, 7, noteId))
        second.delete(sessionId, 7, noteId)
        assertNull(first.read(sessionId, 7, noteId))
    }

    @Test fun givenCorruptDraft_whenRead_thenFailsExplicitly() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = File(context.filesDir, "note-drafts/$sessionId-7-$noteId.json")
        file.parentFile!!.mkdirs()
        file.writeText("not json")
        try {
            assertTrue(runCatching { FileNoteDraftStorage(context, Dispatchers.IO).read(sessionId, 7, noteId) }.isFailure)
        } finally {
            file.delete()
        }
    }
}
