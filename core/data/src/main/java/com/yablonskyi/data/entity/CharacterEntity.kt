package com.yablonskyi.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.AbilityBlock
import com.yablonskyi.model.character.Money
import com.yablonskyi.model.character.ProficiencyLevel
import com.yablonskyi.model.character.Skill
import com.yablonskyi.model.character.SpellSettings

@Entity(tableName = "character")
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val level: Int = 1,
    val imagePath: String? = null,
    // HP
    @ColumnInfo(name = "current_hp") val currentHp: Int = 0,
    @ColumnInfo(name = "max_hp") val maxHp: Int = 0,
    @ColumnInfo(name = "temp_hp") val tempHp: Int = 0,
    @ColumnInfo(name = "hit_dice") val hitDice: String = "",
    // Class
    @ColumnInfo(name = "class") val charClass: String = "",
    @ColumnInfo(name = "subclass") val subClass: String = "",
    // Other
    val race: String = "",
    val speed: Int = 30,
    @ColumnInfo(name = "armor_class") val armorClass: Int = 8,
    val shield: Int = 0,
    @Embedded(prefix = "money_")
    val coins: Money = Money(),
    @ColumnInfo(name = "initiative_bonus") val initiativeMiscBonus: Int = 0,
    val proficiencies: String = "",
    val traits: String = "",
    val feats: String = "",
    val inventory: String = "",
    val backstory: String = "",
    val notes: String = "",
    // Spells
    @Embedded(prefix = "spell_settings_")
    val spellSettings: SpellSettingsEntity = SpellSettingsEntity(),
    // Abilities
    @Embedded(prefix = "abilities_")
    val abilityBlock: AbilityBlockEntity = AbilityBlockEntity(),
    @ColumnInfo(name = "skill_proficiencies")
    val skillProficiencies: Map<Skill, ProficiencyLevel> = emptyMap(),
    val savingThrowProficiencies: Set<Ability> = emptySet(),
    val passivePerceptionBonus: Int = 0,
    val hasJackOfAllTrades: Boolean = false,
)
