package com.yablonskyi.model.backup

import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.AttackType
import com.yablonskyi.model.character.Component
import com.yablonskyi.model.character.DamageType
import com.yablonskyi.model.character.MagicSchool
import com.yablonskyi.model.character.ProficiencyLevel
import com.yablonskyi.model.character.Skill
import com.yablonskyi.model.character.SpellCastTime
import com.yablonskyi.model.character.SpellDuration
import com.yablonskyi.model.character.SpellLevel
import com.yablonskyi.model.character.SpellRangeType
import com.yablonskyi.model.character.Note
import com.yablonskyi.model.character.RichText
import com.yablonskyi.model.character.TextFormat
import com.yablonskyi.model.character.TextSpan
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupContractTest {
    @Test
    fun givenFormattedV2Notes_whenSerialized_thenRoundTripsAndValidates() {
        val data = populatedData().toV2().let { v2 ->
            v2.copy(characters = v2.characters.map { character ->
                character.copy(notes = listOf(Note(
                    "15412a7e-37e6-4e8a-92cb-af49e0759032", "Travel",
                    RichText(plainText = "Forest", spans = listOf(TextSpan(0, 6, TextFormat.BOLD))),
                )))
            })
        }
        val v2Manifest = metadataFor(populatedData()).copy(formatVersion = 2)
        BackupValidator.validate(v2Manifest, data)
        assertEquals(data, Json.decodeFromString<BackupDataV2>(Json.encodeToString(data)))
        val corrupt = data.copy(characters = data.characters.map {
            it.copy(notes = listOf(it.notes.single().copy(text = RichText(version = 2))))
        })
        assertThrows(BackupValidationException::class.java) { BackupValidator.validate(v2Manifest, corrupt) }
    }
    @Test
    fun givenBackup_whenSerialized_thenContainsOnlyContentSections() {
        val document = Json.parseToJsonElement(Json.encodeToString(emptyData)) as kotlinx.serialization.json.JsonObject
        assertEquals(setOf("characters", "attacks", "diceRolls", "spells", "characterSpells", "races", "classes"), document.keys)
    }
    private val emptyData = BackupDataV1(
        characters = emptyList(), attacks = emptyList(), diceRolls = emptyList(),
        spells = emptyList(), characterSpells = emptyList(), races = emptyList(),
        classes = emptyList(),
    )
    private val manifest = BackupManifest(
        format = "dnd-sheet-backup", formatVersion = 1,
        createdAt = "2026-09-21T12:34:56Z", sourceAppVersion = "1.0",
        counts = BackupRecordCounts(0, 0, 0, 0, 0, 0, 0),
        dataSha256 = "a".repeat(64), assets = emptyList(),
    )

    @Test
    fun givenEmptyBackup_whenValidatedAndSerialized_thenRoundTrips() {
        BackupValidator.validate(manifest, emptyData)
        assertEquals(emptyData, Json.decodeFromString<BackupDataV1>(Json.encodeToString(emptyData)))
        assertEquals(manifest, Json.decodeFromString<BackupManifest>(Json.encodeToString(manifest)))
    }

    @Test
    fun givenMissingSections_whenDecoded_thenFailsInsteadOfDefaultingToEmpty() {
        assertThrows(SerializationException::class.java) { Json.decodeFromString<BackupDataV1>("{}") }
    }

    @Test
    fun givenPreferenceBearingDevelopmentContract_whenDecoded_thenRejectsUnknownSection() {
        val json = Json.encodeToString(emptyData).dropLast(1) +
            ",\"preferences\":{\"theme\":\"DARK\",\"listView\":\"GRID\",\"languageCode\":\"uk\"}}"

        assertThrows(SerializationException::class.java) {
            Json.decodeFromString<BackupDataV1>(json)
        }
    }

    @Test
    fun givenNewerVersion_whenValidated_thenReportsUnsupportedVersion() {
        val error = assertThrows(BackupValidationException::class.java) {
            BackupValidator.validate(manifest.copy(formatVersion = 3), emptyData)
        }
        assertEquals(BackupValidationFailure.UNSUPPORTED_VERSION, error.failure)
    }

    @Test
    fun givenDanglingAssociation_whenValidated_thenFails() {
        invalid(emptyData.copy(characterSpells = listOf(BackupCharacterSpell(1, 2))),
            manifest.copy(counts = manifest.counts.copy(characterSpells = 1)))
    }

    @Test
    fun givenUnsafeOrDuplicateAssetPaths_whenValidated_thenFails() {
        listOf("../portrait.jpg", "/portrait.jpg", "images/../portrait.jpg", "images\\portrait.jpg").forEach { path ->
            invalid(emptyData, manifest.copy(assets = listOf(BackupAsset("portrait", path, 1, "b".repeat(64)))))
        }
        val asset = BackupAsset("portrait", "images/portrait.jpg", 1, "b".repeat(64))
        invalid(emptyData, manifest.copy(assets = listOf(asset, asset.copy(id = "another"))))
    }

    @Test
    fun givenInvalidMetadata_whenValidated_thenFails() {
        invalid(emptyData, manifest.copy(dataSha256 = "bad"))
        invalid(emptyData, manifest.copy(createdAt = "yesterday"))
        invalid(emptyData, manifest.copy(counts = manifest.counts.copy(characters = 1)))
    }

    @Test
    fun givenExcessiveRecordOrAssetCounts_whenValidated_thenReportsSizeLimit() {
        val limits = BackupValidationLimits(
            characters = 1,
            attacks = 1,
            diceRolls = 1,
            spells = 1,
            characterSpells = 1,
            races = 1,
            classes = 1,
            assets = 1,
            totalRecords = 1,
        )
        val oversized = listOf(
            manifest.copy(counts = manifest.counts.copy(characters = 2)),
            manifest.copy(counts = manifest.counts.copy(characters = 1, attacks = 1)),
            manifest.copy(assets = listOf(
                BackupAsset("one", "images/one.bin", 1, "b".repeat(64)),
                BackupAsset("two", "images/two.bin", 1, "c".repeat(64)),
            )),
        )

        oversized.forEach { candidate ->
            val failure = assertThrows(BackupValidationException::class.java) {
                BackupValidator.validateManifest(candidate, limits)
            }
            assertEquals(BackupValidationFailure.SIZE_LIMIT_EXCEEDED, failure.failure)
        }
    }

    @Test
    fun givenEveryRecordTypeAndLongHistory_whenSerialized_thenPreservesEveryValue() {
        val data = populatedData()
        val metadata = metadataFor(data)
        BackupValidator.validate(metadata, data)
        val restored = Json.decodeFromString<BackupDataV1>(Json.encodeToString(data))
        assertEquals(data, restored)
        BackupValidator.validate(metadata, restored)
    }

    @Test
    fun givenDuplicateIdsOrNames_whenValidated_thenFails() {
        val data = populatedData()
        listOf(
            data.copy(characters = data.characters + data.characters.first()),
            data.copy(attacks = data.attacks + data.attacks.first()),
            data.copy(diceRolls = data.diceRolls + data.diceRolls.first()),
            data.copy(spells = data.spells + data.spells.first().copy(spellId = 99)),
            data.copy(characterSpells = data.characterSpells + data.characterSpells.first()),
            data.copy(races = data.races + data.races.first()),
            data.copy(classes = data.classes + data.classes.first()),
        ).forEach { invalid(it, metadataFor(it)) }
    }

    @Test
    fun givenMissingImagesOrOwners_whenValidated_thenFails() {
        val data = populatedData()
        invalid(data, metadataFor(data).copy(assets = emptyList()))
        val missingCharacter = data.copy(characters = emptyList())
        invalid(missingCharacter, metadataFor(missingCharacter))
        val missingSpell = data.copy(spells = emptyList())
        invalid(missingSpell, metadataFor(missingSpell))
    }

    @Test
    fun givenMissingNestedField_whenDecoded_thenFails() {
        val json = Json.encodeToString(populatedData()).replace("\"sortOrder\":42,", "")
        assertThrows(SerializationException::class.java) { Json.decodeFromString<BackupDataV1>(json) }
    }

    private fun populatedData(): BackupDataV1 {
        val character = BackupCharacter(
            id = 7, sortOrder = 42, name = "Мандрівник", level = 8, imageAssetId = "portrait",
            currentHp = 12, maxHp = 39, tempHp = 3, hitDice = "2d8", charClass = "Bard",
            subClass = "Lore", race = "Elf", speed = 35, armorClass = 15, shield = 2,
            coins = BackupMoney(11, 22, 33), initiativeMiscBonus = 4, proficiencies = "Tools",
            traits = "Trait", feats = "Feat", inventory = "Lute", backstory = "Історія",
            notes = "Line one\nLine two 🐉",
            spellSettings = BackupSpellSettings(Ability.CHA, 2, 3, mapOf(SpellLevel.LEVEL_1 to BackupSpellSlot(4, 2))),
            abilityBlock = BackupAbilities(9, 14, 12, 13, 10, 18),
            skillProficiencies = mapOf(Skill.PERCEPTION to ProficiencyLevel.EXPERT),
            savingThrowProficiencies = setOf(Ability.DEX), passivePerceptionBonus = 2, hasJackOfAllTrades = true,
        )
        val spell = BackupSpell(
            5, "Spell", MagicSchool.ABJURATION, SpellLevel.CANTRIP, SpellCastTime.ACTION,
            SpellRangeType.SELF, 10, listOf(Component.entries.first()), "Material", true,
            SpellDuration.INSTANTANEOUS, true, AttackType.NONE, Ability.WIS, DamageType.SLASHING,
            "2d6", "Description", "Higher levels",
        )
        return BackupDataV1(
            characters = listOf(character, character.copy(id = 8, sortOrder = 43)),
            attacks = listOf(BackupAttack(3, 7, "Sword", AttackType.NONE, Ability.STR, true, 2, 3,
                "1d8", DamageType.SLASHING, "5", "Attack note")),
            diceRolls = (1L..1001L).map { BackupDiceRoll(it, 7, "Roll", listOf(4, 5), null, 9,
                listOf(BackupDiceGroup(6, 2)), 123456789L + it) },
            spells = listOf(spell, spell.copy(spellId = 6, name = "Unassigned spell")),
            characterSpells = listOf(BackupCharacterSpell(7, 5), BackupCharacterSpell(8, 5)),
            races = listOf(BackupRace("race", "Elf", "Medium", 35, mapOf(Ability.DEX to 2),
                listOf(Skill.PERCEPTION), listOf("Trait"), "Description", true)),
            classes = listOf(BackupClass("class", "Bard", "d8", Ability.CHA, setOf(Ability.DEX),
                3, listOf(Skill.PERFORMANCE), Ability.CHA, "Description", true)),
        )
    }

    private fun metadataFor(data: BackupDataV1) = manifest.copy(
        counts = BackupRecordCounts(data.characters.size, data.attacks.size, data.diceRolls.size,
            data.spells.size, data.characterSpells.size, data.races.size, data.classes.size),
        assets = listOf(BackupAsset("portrait", "images/portrait.jpg", 123, "b".repeat(64))),
    )

    private fun invalid(data: BackupDataV1, metadata: BackupManifest = manifest) {
        assertThrows(BackupValidationException::class.java) { BackupValidator.validate(metadata, data) }
    }
}
