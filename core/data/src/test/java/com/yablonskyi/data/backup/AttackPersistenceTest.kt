package com.yablonskyi.data.backup

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import com.yablonskyi.data.AppDatabase
import com.yablonskyi.data.di.MIGRATION_11_12
import com.yablonskyi.data.entity.AttackEntity
import com.yablonskyi.data.mapper.*
import com.yablonskyi.model.backup.BackupAttack
import com.yablonskyi.model.character.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
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
class AttackPersistenceTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun givenNewAttackFields_whenBackedUpAndStored_thenRoundTripsEveryField() = runTest {
        val db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java).build()
        try {
            val attack = AttackEntity(attackId = 3, characterId = 7, name = "Strike", damageMode = DamageMode.FIXED,
                fixedDamage = 8, damageAbilityModifier = DamageAbilityModifier.NEGATIVE_ONLY,
                usages = setOf(AttackUsage.BONUS_ACTION, AttackUsage.REACTION))
            val snapshot = backupFixture().copy(attacks = listOf(attack))
            db.backupDao().replace(snapshot)
            val stored = db.backupDao().snapshot().attacks.single()
            assertEquals(attack, stored)
            assertEquals(attack, stored.toModel().toEntity())
            val encoded = Json.encodeToString(stored.toBackup())
            assertEquals(attack, Json.decodeFromString<BackupAttack>(encoded).toEntity())
        } finally { db.close() }
    }

    @Test fun givenLegacyBackup_whenDecoded_thenUsesCompatibleDefaults() {
        val legacy = """{"attackId":1,"characterId":1,"name":"Sword","attackType":"MELEE_ATTACK","ability":"STR","isProficient":true,"bonusToHit":0,"bonusToDamage":0,"damageDice":"1d8","damageType":"SLASHING","range":"5","notes":""}"""
        val attack = Json.decodeFromString<BackupAttack>(legacy)
        assertEquals(DamageMode.DICE, attack.damageMode)
        assertEquals(DamageAbilityModifier.FULL, attack.damageAbilityModifier)
        assertEquals(setOf(AttackUsage.ACTION), attack.usages)
    }

    @Test fun givenVersion11_whenMigrated_thenPreservesAttackAndAddsDefaults() = runTest {
        val path = File(temporary.root, "attack-migration.db").absolutePath
        val schema = Json.parseToJsonElement(File("schemas/com.yablonskyi.data.AppDatabase/11.json").readText()).jsonObject.getValue("database").jsonObject
        SQLiteDatabase.openOrCreateDatabase(path, null).use { old ->
            schema.getValue("entities").jsonArray.forEach { element ->
                val definition = element.jsonObject
                val table = definition.getValue("tableName").jsonPrimitive.content
                old.execSQL(definition.getValue("createSql").jsonPrimitive.content.replace("${'$'}{TABLE_NAME}", table))
                definition["indices"]?.jsonArray?.forEach { index -> old.execSQL(index.jsonObject.getValue("createSql").jsonPrimitive.content.replace("${'$'}{TABLE_NAME}", table)) }
            }
            old.execSQL("INSERT INTO attacks VALUES (1, 7, 'Sword', 'MELEE_ATTACK', 'STR', 1, 2, 3, '2d8', 'SLASHING', '5 ft', 'Description')")
            old.version = 11
        }
        val upgraded = Room.databaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java, path).addMigrations(MIGRATION_11_12).build()
        try {
            val attack = upgraded.backupDao().snapshot().attacks.single()
            assertEquals("2d8", attack.damageDice)
            assertEquals("5 ft", attack.range)
            assertEquals("Description", attack.notes)
            assertEquals(DamageMode.DICE, attack.damageMode)
            assertEquals(DamageAbilityModifier.FULL, attack.damageAbilityModifier)
            assertEquals(setOf(AttackUsage.ACTION), attack.usages)
        } finally { upgraded.close() }
    }
}
