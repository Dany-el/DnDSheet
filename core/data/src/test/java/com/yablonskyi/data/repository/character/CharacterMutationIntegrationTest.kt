package com.yablonskyi.data.repository.character

import androidx.room.Room
import com.yablonskyi.data.AppDatabase
import com.yablonskyi.domain.backup.BackupAccessGate
import com.yablonskyi.domain.backup.BackupRecoveryRequiredException
import com.yablonskyi.domain.character.*
import com.yablonskyi.model.character.Character
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CharacterMutationIntegrationTest {
    @Test fun givenStartupRecoveryPending_whenCharacterInserted_thenWriteIsRejected() = runTest {
        val db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java).build()
        try {
            val gate = BackupAccessGate()
            val repository = CharacterRepositoryImpl(
                db, db.characterDao(), db.attackDao(), db.spellDao(), gate,
            )

            val failure = runCatching { repository.insertCharacter(Character(name = "Blocked")) }.exceptionOrNull()

            assertTrue(failure is BackupRecoveryRequiredException)
            assertTrue(db.backupDao().snapshot().characters.isEmpty())
        } finally { db.close() }
    }

    @Test fun givenConcurrentEdits_whenAppliedInTransactions_thenPreservesBothChanges() = runTest {
        val db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java).build()
        try {
            val gate = BackupAccessGate().apply { recover { } }
            val repository = CharacterRepositoryImpl(db, db.characterDao(), db.attackDao(), db.spellDao(), gate)
            val id = repository.insertCharacter(Character(name = "Before"))
            val name = async { repository.applyChange(id, CharacterChange.Text(CharacterTextField.NAME, "Hero")) }
            val notes = async { repository.applyChange(id, CharacterChange.Text(CharacterTextField.NOTES, "Notes")) }
            name.await(); notes.await()
            val character = repository.getCharacterSheetById(id).character
            assertEquals("Hero", character.name)
            assertEquals("Notes", character.notes)
        } finally { db.close() }
    }

    @Test fun givenDeletedCharacter_whenFieldChanged_thenDoesNotRecreateIt() = runTest {
        val db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java).build()
        try {
            val gate = BackupAccessGate().apply { recover { } }
            val repository = CharacterRepositoryImpl(db, db.characterDao(), db.attackDao(), db.spellDao(), gate)
            val result = runCatching { repository.applyChange(99, CharacterChange.Text(CharacterTextField.NAME, "Hero")) }
            assertTrue(result.isFailure)
            assertTrue(repository.getAllCharacterSheets().isEmpty())
        } finally { db.close() }
    }
}
