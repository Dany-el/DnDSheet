package com.yablonskyi.data.repository.character

import android.net.Uri
import android.util.Base64
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.CharacterSheet
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CharacterFileRepositoryTest {
    @Test fun givenMalformedDocument_whenRead_thenRejectsBeforeImport() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val document = File(context.cacheDir, "invalid.json").apply { writeText("[{}]") }
        val repository = FakeCharacterRepository()
        val files = CharacterFileRepositoryImpl(context, repository, StandardTestDispatcher(testScheduler))
        assertTrue(runCatching { files.read(Uri.fromFile(document).toString()) }.isFailure)
        assertEquals(1, repository.characters.value.size)
    }

    @Test fun givenFailedImport_whenImagesDecoded_thenRemovesNewImages() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val repository = FakeCharacterRepository().apply { failure = IllegalStateException() }
        val files = CharacterFileRepositoryImpl(context, repository, StandardTestDispatcher(testScheduler))
        val before = context.filesDir.listFiles()!!.map { it.name }.toSet()
        val image = Base64.encodeToString(byteArrayOf(1, 2, 3), Base64.NO_WRAP)
        val sheets = listOf(CharacterSheet(Character(imagePath = image), emptyList(), emptyList()))
        assertTrue(runCatching { files.import(sheets) }.isFailure)
        assertEquals(before, context.filesDir.listFiles()!!.map { it.name }.toSet())
    }

    @Test fun givenSelectedCharacter_whenExportedAndRead_thenPreservesContent() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val document = File(context.cacheDir, "characters.json")
        val repository = FakeCharacterRepository()
        val files = CharacterFileRepositoryImpl(context, repository, StandardTestDispatcher(testScheduler))
        files.export(Uri.fromFile(document).toString(), listOf(7))
        val loaded = files.read(Uri.fromFile(document).toString())
        assertEquals("Hero", loaded.single().character.name)
    }
}
