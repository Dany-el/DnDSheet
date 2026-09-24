package com.yablonskyi.model.backup

import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.DamageMode
import com.yablonskyi.model.character.AttackUsage
import com.yablonskyi.model.character.DamageAbilityModifier
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
import kotlinx.serialization.Serializable

/** Versioned wire records. Required fields must not gain defaults that hide missing backup data. */
@Serializable
data class BackupDataV1(
    val characters: List<BackupCharacter>,
    val attacks: List<BackupAttack>,
    val diceRolls: List<BackupDiceRoll>,
    val spells: List<BackupSpell>,
    val characterSpells: List<BackupCharacterSpell>,
    val races: List<BackupRace>,
    val classes: List<BackupClass>,
)

@Serializable
data class BackupCharacter(
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
    val notes: String,
    val spellSettings: BackupSpellSettings,
    val abilityBlock: BackupAbilities,
    val skillProficiencies: Map<Skill, ProficiencyLevel>,
    val savingThrowProficiencies: Set<Ability>,
    val passivePerceptionBonus: Int,
    val hasJackOfAllTrades: Boolean,
)

@Serializable
data class BackupMoney(val gold: Int, val silver: Int, val copper: Int)

@Serializable
data class BackupAbilities(
    val strength: Int, val dexterity: Int, val constitution: Int,
    val intelligence: Int, val wisdom: Int, val charisma: Int,
)

@Serializable
data class BackupSpellSettings(
    val spellCastingAbility: Ability?,
    val dcMiscBonus: Int,
    val attackMiscBonus: Int,
    val spellSlots: Map<SpellLevel, BackupSpellSlot>,
)

@Serializable
data class BackupSpellSlot(val max: Int, val current: Int)

@Serializable
data class BackupAttack(
    val attackId: Long,
    val characterId: Long,
    val name: String,
    val attackType: AttackType,
    val ability: Ability,
    val isProficient: Boolean,
    val bonusToHit: Int,
    val bonusToDamage: Int,
    val damageDice: String,
    val damageType: DamageType,
    val range: String,
    val notes: String,
    val damageMode: DamageMode = DamageMode.DICE,
    val fixedDamage: Int = 0,
    val usages: Set<AttackUsage> = setOf(AttackUsage.ACTION),
    val damageAbilityModifier: DamageAbilityModifier = DamageAbilityModifier.FULL,
)

@Serializable
data class BackupDiceRoll(
    val id: Long,
    val characterId: Long,
    val label: String,
    val numbers: List<Int>,
    val modifier: Int?,
    val result: Int,
    val dices: List<BackupDiceGroup>,
    val timestamp: Long,
)

@Serializable
data class BackupDiceGroup(val sides: Int, val count: Int)

@Serializable
data class BackupSpell(
    val spellId: Long,
    val name: String,
    val school: MagicSchool,
    val level: SpellLevel,
    val castTime: SpellCastTime,
    val rangeType: SpellRangeType,
    val rangeValue: Int?,
    val components: List<Component>,
    val material: String?,
    val isRitual: Boolean,
    val duration: SpellDuration,
    val isConcentration: Boolean,
    val attackType: AttackType,
    val saveStat: Ability?,
    val damageType: DamageType?,
    val damageDice: String?,
    val description: String,
    val higherLevels: String?,
)

@Serializable
data class BackupCharacterSpell(val characterId: Long, val spellId: Long)

@Serializable
data class BackupRace(
    val id: String,
    val name: String,
    val size: String,
    val speed: Int,
    val abilityBonuses: Map<Ability, Int>,
    val grantedSkills: List<Skill>,
    val traits: List<String>,
    val description: String,
    val isHomebrew: Boolean,
)

@Serializable
data class BackupClass(
    val id: String,
    val name: String,
    val hitDice: String,
    val primaryAbility: Ability,
    val savingThrows: Set<Ability>,
    val skillChoiceCount: Int,
    val availableSkills: List<Skill>,
    val spellcastingAbility: Ability?,
    val description: String,
    val isHomebrew: Boolean,
)
