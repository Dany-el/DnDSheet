package com.yablonskyi.model.backup

import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Note
import com.yablonskyi.model.character.ProficiencyLevel
import com.yablonskyi.model.character.Skill
import com.yablonskyi.model.character.legacyNote
import kotlinx.serialization.Serializable

@Serializable
data class BackupDataV2(
    val characters: List<BackupCharacterV2>,
    val attacks: List<BackupAttack>,
    val diceRolls: List<BackupDiceRoll>,
    val spells: List<BackupSpell>,
    val characterSpells: List<BackupCharacterSpell>,
    val races: List<BackupRace>,
    val classes: List<BackupClass>,
)

@Serializable
data class BackupCharacterV2(
    val id: Long,
    val sortOrder: Long,
    val name: String,
    val level: Int,
    val imageAssetId: String?,
    val currentHp: Int,
    val maxHp: Int,
    val tempHp: Int,
    val hitDice: String,
    val charClass: String,
    val subClass: String,
    val race: String,
    val speed: Int,
    val armorClass: Int,
    val shield: Int,
    val coins: BackupMoney,
    val initiativeMiscBonus: Int,
    val proficiencies: String,
    val traits: String,
    val feats: String,
    val inventory: String,
    val backstory: String,
    val notes: List<Note>,
    val spellSettings: BackupSpellSettings,
    val abilityBlock: BackupAbilities,
    val skillProficiencies: Map<Skill, ProficiencyLevel>,
    val savingThrowProficiencies: Set<Ability>,
    val passivePerceptionBonus: Int,
    val hasJackOfAllTrades: Boolean,
)

fun BackupDataV1.toV2(): BackupDataV2 = BackupDataV2(
    characters = characters.map { it.toV2() },
    attacks = attacks,
    diceRolls = diceRolls,
    spells = spells,
    characterSpells = characterSpells,
    races = races,
    classes = classes,
)

private fun BackupCharacter.toV2() = BackupCharacterV2(
    id, sortOrder, name, level, imageAssetId, currentHp, maxHp, tempHp, hitDice,
    charClass, subClass, race, speed, armorClass, shield, coins, initiativeMiscBonus,
    proficiencies, traits, feats, inventory, backstory, listOf(legacyNote(notes)),
    spellSettings, abilityBlock, skillProficiencies, savingThrowProficiencies,
    passivePerceptionBonus, hasJackOfAllTrades,
)

internal fun BackupDataV2.toV1ForValidation(): BackupDataV1 = BackupDataV1(
    characters = characters.map {
        BackupCharacter(
            it.id, it.sortOrder, it.name, it.level, it.imageAssetId, it.currentHp, it.maxHp,
            it.tempHp, it.hitDice, it.charClass, it.subClass, it.race, it.speed, it.armorClass,
            it.shield, it.coins, it.initiativeMiscBonus, it.proficiencies, it.traits, it.feats,
            it.inventory, it.backstory, "", it.spellSettings, it.abilityBlock,
            it.skillProficiencies, it.savingThrowProficiencies, it.passivePerceptionBonus,
            it.hasJackOfAllTrades,
        )
    },
    attacks = attacks,
    diceRolls = diceRolls,
    spells = spells,
    characterSpells = characterSpells,
    races = races,
    classes = classes,
)
