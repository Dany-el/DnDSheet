package com.yablonskyi.data.backup

import androidx.room.Room
import com.yablonskyi.data.AppDatabase
import com.yablonskyi.data.di.MIGRATION_9_10
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RestoreCommitMigrationTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun givenVersionNineTables_whenUpgraded_thenPreservesContentAndAddsEmptyMarkerTable() = runTest {
        val path = File(temporary.root, "migration.db").absolutePath
        val context = RuntimeEnvironment.getApplication()
        val original = backupFixture()
        val db = Room.databaseBuilder(context, AppDatabase::class.java, path).build()
        try {
            db.backupDao().replace(original)
            // Version 10 changes no content tables; removing only its marker reconstructs version 9.
            db.openHelper.writableDatabase.execSQL("DROP TABLE restore_commit")
            db.openHelper.writableDatabase.version = 9
        } finally { db.close() }
        val upgraded = Room.databaseBuilder(context, AppDatabase::class.java, path).addMigrations(MIGRATION_9_10).build()
        try {
            assertEquals(original, upgraded.backupDao().snapshot()) // Opens DB and runs Room schema validation.
            assertNull(upgraded.backupDao().committedRestore())
            assertEquals(10, upgraded.openHelper.writableDatabase.version)
        } finally { upgraded.close() }
    }

    @Test fun givenInvalidRestore_whenCommitting_thenRollsBackMarkerWithContent() = runTest {
        val db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java).build()
        try {
            val original = backupFixture()
            db.backupDao().replace(original)
            val invalid = original.copy(characterSpells = listOf(com.yablonskyi.data.entity.CharacterSpellCrossRefEntity(7, 999)))
            assertTrue(runCatching { db.backupDao().commitRestore(invalid, "test-operation") }.isFailure)
            assertEquals(original, db.backupDao().snapshot())
            assertNull(db.backupDao().committedRestore())
        } finally { db.close() }
    }
}
