package com.yablonskyi.data.backup

import androidx.room.Room
import com.yablonskyi.data.AppDatabase
import com.yablonskyi.data.entity.*
import com.yablonskyi.model.dice.DiceGroup
import com.yablonskyi.model.character.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.time.Instant
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupRoomTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun givenDatabaseSnapshot_whenArchivedAndRestored_thenPreservesAllTablesAndImageBytes() = runTest {
        val db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java).build()
        try {
            val original = backupFixture()
            db.backupDao().replace(original)
            val data = db.backupDao().snapshot().toBackup(mapOf("/old/portrait.jpg" to "portrait"))
            val image = temporary.newFile().apply { writeBytes(byteArrayOf(5, 4, 3, 2, 1)) }
            val codec = BackupArchiveCodec()
            val archive = codec.write(data, mapOf("portrait" to image), temporary.root, "1", Instant.now())
            val staged = codec.read(archive, temporary.root)
            val paths = staged.images.mapValues { it.value.absolutePath }
            db.backupDao().replace(staged.data.toRoomSnapshot(paths))
            assertEquals(original.copy(characters = original.characters.map { it.copy(imagePath = paths.getValue("portrait")) }),
                db.backupDao().snapshot())
            assertArrayEquals(image.readBytes(), staged.images.getValue("portrait").readBytes())
        } finally { db.close() }
    }

    @Test fun givenAllTables_whenReplacedTwice_thenPreservesSnapshotAndNewIds() = runTest {
        val db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java).build()
        try {
            val snapshot = backupFixture()
            val data = snapshot.toBackup(mapOf("/old/portrait.jpg" to "portrait"))
            val restored = data.toRoomSnapshot(mapOf("portrait" to "/new/portrait.jpg"))
            assertEquals(snapshot.copy(characters = snapshot.characters.map { it.copy(imagePath = "/new/portrait.jpg") }), restored)
            db.backupDao().replace(restored)
            db.backupDao().replace(restored)
            assertEquals(restored, db.backupDao().snapshot())
            assertTrue(db.characterDao().insertCharacter(CharacterEntity(name = "New")) > 8)
            assertTrue(db.attackDao().insertAttack(AttackEntity(characterId = 7, name = "New")) > 5)
            assertTrue(db.spellDao().insertSpell(SpellEntity(name = "New")) > 4)
            assertTrue(db.diceRollDao().insert(DiceRollEntity(characterId = 7, label = "New", numbers = listOf(1),
                modifier = null, result = 1, dices = listOf(DiceGroup(6, 1)), timestamp = 99)) > 1001)
        } finally { db.close() }
    }

    @Test fun givenDifferentExistingContent_whenReplacing_thenRemovesEveryOldRowInsteadOfMerging() = runTest {
        val db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java).build()
        try {
            val restored = backupFixture()
            val existing = restored.copy(
                characters = restored.characters + restored.characters.first().copy(id = 99, name = "Old character", imagePath = null),
                attacks = restored.attacks + restored.attacks.first().copy(attackId = 98, characterId = 99, name = "Old attack"),
                diceRolls = restored.diceRolls + restored.diceRolls.first().copy(id = 1002, characterId = 99, label = "Old roll"),
                spells = restored.spells + restored.spells.first().copy(spellId = 97, name = "Old unassigned spell"),
                characterSpells = restored.characterSpells + CharacterSpellCrossRefEntity(99, 97),
                races = restored.races + restored.races.first().copy(id = "old-race", name = "Old race"),
                classes = restored.classes + restored.classes.first().copy(id = "old-class", name = "Old class"),
            )
            db.backupDao().replace(existing)

            db.backupDao().replace(restored)

            assertEquals(restored, db.backupDao().snapshot())
        } finally { db.close() }
    }

    @Test fun givenCommitMarkerConflict_whenCommittingRestore_thenRollsBackReplacement() = runTest {
        val db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java).build()
        try {
            val original = backupFixture()
            val replacement = original.copy(
                characters = original.characters.map { it.copy(name = "Replacement") },
            )
            db.backupDao().replace(original)
            db.backupDao().insertCommitMarker(RestoreCommitEntity(operationId = "pending"))

            assertTrue(runCatching { db.backupDao().commitRestore(replacement, "new") }.isFailure)

            assertEquals(original, db.backupDao().snapshot())
            assertEquals("pending", db.backupDao().committedRestore())
        } finally { db.close() }
    }

    @Test fun givenConstraintFailure_whenReplacing_thenRollsBackEveryTable() = runTest {
        val db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java).build()
        try {
            val original = backupFixture()
            db.backupDao().replace(original)
            val invalid = original.copy(characterSpells = listOf(CharacterSpellCrossRefEntity(7, 999)))
            assertTrue(runCatching { db.backupDao().replace(invalid) }.isFailure)
            assertEquals(original, db.backupDao().snapshot())
            db.backupDao().replace(BackupRoomSnapshot(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList()))
            assertTrue(db.backupDao().snapshot().characters.isEmpty())
        } finally { db.close() }
    }

}

internal fun backupFixture() = BackupRoomSnapshot(
        characters = listOf(CharacterEntity(id = 7, sortOrder = 42, name = "Герой", imagePath = "/old/portrait.jpg", notes = listOf(
            com.yablonskyi.model.character.Note("15412a7e-37e6-4e8a-92cb-af49e0759032", "Notes",
            com.yablonskyi.model.character.RichText(plainText = "🐉\nNotes", spans = listOf(
                com.yablonskyi.model.character.TextSpan(0, 2, com.yablonskyi.model.character.TextFormat.BOLD))))),
            level = 9, currentHp = 12, maxHp = 39, tempHp = 5, hitDice = "2d8", charClass = "Bard",
            subClass = "Lore", race = "Elf", speed = 35, armorClass = 14, shield = 2, coins = Money(11, 22, 33),
            initiativeMiscBonus = 4, proficiencies = "Tools", traits = "Trait", feats = "Feat", inventory = "Lute",
            backstory = "Історія", spellSettings = SpellSettingsEntity(Ability.CHA, 2, 3,
                mapOf(SpellLevel.LEVEL_1 to SpellSlot(4, 2))), abilityBlock = AbilityBlockEntity(9, 14, 12, 13, 10, 18),
            skillProficiencies = mapOf(Skill.PERCEPTION to ProficiencyLevel.EXPERT),
            savingThrowProficiencies = setOf(Ability.DEX), passivePerceptionBonus = 2, hasJackOfAllTrades = true),
            CharacterEntity(id = 8, sortOrder = 43, imagePath = "/old/portrait.jpg")),
        attacks = listOf(AttackEntity(attackId = 5, characterId = 7, name = "Sword", attackType = AttackType.MELEE_ATTACK,
            ability = Ability.DEX, isProficient = true, bonusToHit = 2, bonusToDamage = 3, damageDice = "1d8",
            damageType = DamageType.PIERCING, range = "10", notes = "Attack note")),
        diceRolls = (1L..1001L).map { DiceRollEntity(it, 7, "Roll", listOf(2, 4), 1, 7, listOf(DiceGroup(6, 2)), it) },
        spells = listOf(SpellEntity(spellId = 3, name = "Shared", school = MagicSchool.EVOCATION,
            level = SpellLevel.LEVEL_1, castTime = SpellCastTime.entries.last(), rangeType = SpellRangeType.entries.last(),
            rangeValue = 30, components = Component.entries, material = "Dust", isRitual = true,
            duration = SpellDuration.entries.last(), isConcentration = true, attackType = AttackType.SAVE,
            saveStat = Ability.WIS, damageType = DamageType.FIRE, damageDice = "2d6", description = "Description",
            higherLevels = "More dice"), SpellEntity(spellId = 4, name = "Unassigned")),
        characterSpells = listOf(CharacterSpellCrossRefEntity(7, 3), CharacterSpellCrossRefEntity(8, 3)),
        races = listOf(RaceEntity(id = "race", name = "Race", size = "Small", speed = 25,
            abilityBonuses = mapOf(Ability.DEX to 2), grantedSkills = listOf(Skill.PERCEPTION),
            traits = listOf("Trait"), description = "Race description", isHomebrew = true)),
        classes = listOf(CharacterClassEntity(id = "class", name = "Class", hitDice = "d8", primaryAbility = Ability.CHA,
            savingThrows = setOf(Ability.DEX, Ability.CHA), skillChoiceCount = 3, availableSkills = listOf(Skill.PERFORMANCE),
            spellcastingAbility = Ability.CHA, description = "Class description", isHomebrew = true)),
    )