package com.yablonskyi.data.repository.character

import android.graphics.Bitmap
import android.net.Uri
import com.yablonskyi.domain.backup.BackupAccessGate
import com.yablonskyi.domain.backup.BackupRecoveryRequiredException
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
class CharacterImageRepositoryTest {
    @Test fun givenRecoveryPending_whenImageReplaced_thenDoesNotTouchFiles() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val before = context.filesDir.listFiles()!!.map { it.name }.toSet()
        val images = CharacterImageRepositoryImpl(
            context,
            FakeCharacterRepository(),
            BackupAccessGate(),
            StandardTestDispatcher(testScheduler),
        )

        val failure = runCatching { images.replace(7, "invalid://image") }.exceptionOrNull()

        assertTrue(failure is BackupRecoveryRequiredException)
        assertEquals(before, context.filesDir.listFiles()!!.map { it.name }.toSet())
    }

    @Test fun givenPersistenceFailure_whenImageReplaced_thenPreservesOldImageAndRemovesReplacement() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val repository = FakeCharacterRepository()
        val old = File(context.filesDir, "char_img_old.jpg").apply { writeText("old image") }
        repository.characters.value = listOf(repository.characters.value.single().copy(imagePath = old.absolutePath))
        val source = File(context.cacheDir, "replacement.png")
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        repository.failure = IllegalStateException("Cannot save")
        val gate = BackupAccessGate().apply { recover { } }
        val images = CharacterImageRepositoryImpl(context, repository, gate, StandardTestDispatcher(testScheduler))
        val before = context.filesDir.listFiles()!!.map { it.name }.toSet()
        val result = runCatching { images.replace(7, Uri.fromFile(source).toString()) }
        assertTrue(result.isFailure)
        assertTrue(old.exists())
        assertEquals(old.absolutePath, repository.characters.value.single().imagePath)
        assertEquals(before, context.filesDir.listFiles()!!.map { it.name }.toSet())
    }

    @Test fun givenValidReplacement_whenCommitted_thenRemovesOldImageAfterSavingNewPath() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val repository = FakeCharacterRepository()
        val old = File(context.filesDir, "char_img_old.jpg").apply { writeText("old image") }
        repository.characters.value = listOf(repository.characters.value.single().copy(imagePath = old.absolutePath))
        val source = File(context.cacheDir, "replacement.png")
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        val gate = BackupAccessGate().apply { recover { } }
        CharacterImageRepositoryImpl(context, repository, gate, StandardTestDispatcher(testScheduler))
            .replace(7, Uri.fromFile(source).toString())
        assertFalse(old.exists())
        assertTrue(File(repository.characters.value.single().imagePath!!).exists())
    }
}
