package com.yablonskyi.data.backup

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import com.yablonskyi.data.AppDatabase
import com.yablonskyi.data.di.MIGRATION_8_9
import com.yablonskyi.data.di.MIGRATION_9_10
import com.yablonskyi.data.di.MIGRATION_11_12
import com.yablonskyi.data.di.MIGRATION_10_11
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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

    @Test fun givenActualLegacySchemas_whenUpgraded_thenPreservesEveryNotesStringAndMarkerTable() = runTest {
        val originals = listOf("", "  ", "quoted \"text\" and \\ slash", "🐉\nsecond line", "{\"notes\":[]}")
        for (version in 8..10) {
            val path = File(temporary.root, "migration-$version.db").absolutePath
            createLegacyDatabase(path, version, originals)
            val upgraded = Room.databaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java, path)
                .addMigrations(MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                .build()
            try {
                val characters = upgraded.backupDao().snapshot().characters.sortedBy { it.id }
                assertEquals(originals.size, characters.size)
                originals.forEachIndexed { index, original ->
                    val character = characters[index]
                    assertEquals("Hero $index", character.name)
                    assertEquals(42L + index, character.sortOrder)
                    assertEquals("Notes", character.notes.single().topic)
                    assertEquals(original, character.notes.single().text.plainText)
                    assertTrue(character.notes.single().text.spans.isEmpty())
                }
                assertNull(upgraded.backupDao().committedRestore())
                assertEquals(12, upgraded.openHelper.writableDatabase.version)
            } finally { upgraded.close() }
        }
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

    private fun createLegacyDatabase(path: String, version: Int, notes: List<String>) {
        val schemaFile = File("schemas/com.yablonskyi.data.AppDatabase/$version.json")
        val schema = Json.parseToJsonElement(schemaFile.readText()).jsonObject.getValue("database").jsonObject
        val db = SQLiteDatabase.openOrCreateDatabase(path, null)
        try {
            for (entity in schema.getValue("entities").jsonArray) {
                val definition = entity.jsonObject
                val table = definition.getValue("tableName").jsonPrimitive.content
                db.execSQL(definition.getValue("createSql").jsonPrimitive.content.replace("${'$'}{TABLE_NAME}", table))
                definition["indices"]?.jsonArray?.forEach { index ->
                    db.execSQL(index.jsonObject.getValue("createSql").jsonPrimitive.content.replace("${'$'}{TABLE_NAME}", table))
                }
            }
            db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
            db.execSQL("INSERT INTO room_master_table (id, identity_hash) VALUES (42, ?)",
                arrayOf<Any>(schema.getValue("identityHash").jsonPrimitive.content))
            val character = schema.getValue("entities").jsonArray.first {
                it.jsonObject.getValue("tableName").jsonPrimitive.content == "character"
            }.jsonObject
            notes.forEachIndexed { index, body ->
                val values = ContentValues()
                for (field in character.getValue("fields").jsonArray) {
                    val definition = field.jsonObject
                    val column = definition.getValue("columnName").jsonPrimitive.content
                    if (definition["notNull"]?.jsonPrimitive?.content != "true") continue
                    if (definition.getValue("affinity").jsonPrimitive.content == "INTEGER") values.put(column, 0L)
                    else values.put(column, "")
                }
                values.put("id", index + 1L)
                values.put("sortOrder", 42L + index)
                values.put("name", "Hero $index")
                values.put("notes", body)
                values.put("skill_proficiencies", "{}")
                values.put("spell_settings_spell_slots", "{}")
                db.insertOrThrow("character", null, values)
            }
            db.version = version
        } finally { db.close() }
    }
}
