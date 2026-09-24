package com.yablonskyi.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.DamageMode
import com.yablonskyi.model.character.AttackUsage
import com.yablonskyi.model.character.DamageAbilityModifier
import com.yablonskyi.model.character.AttackType
import com.yablonskyi.model.character.DamageType

@Entity(
    tableName = "attacks",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["characterId"])]
)
data class AttackEntity(
    @PrimaryKey(autoGenerate = true) val attackId: Long = 0,
    val characterId: Long = 0,
    val name: String = "",
    val attackType: AttackType = AttackType.NONE,
    val ability: Ability = Ability.NONE,
    @ColumnInfo("is_proficient") val isProficient: Boolean = false,
    @ColumnInfo(name = "bonus_attack") val bonusToHit: Int = 0,
    @ColumnInfo(name = "bonus_damage") val bonusToDamage: Int = 0,
    val damageDice: String = "",
    val damageType: DamageType = DamageType.SLASHING,
    val range: String = "5",
    val notes: String = "",
    @ColumnInfo(defaultValue = "'DICE'") val damageMode: DamageMode = DamageMode.DICE,
    @ColumnInfo(defaultValue = "0") val fixedDamage: Int = 0,
    @ColumnInfo(defaultValue = "'ACTION'") val usages: Set<AttackUsage> = setOf(AttackUsage.ACTION),
    @ColumnInfo(defaultValue = "'FULL'") val damageAbilityModifier: DamageAbilityModifier = DamageAbilityModifier.FULL,
)
