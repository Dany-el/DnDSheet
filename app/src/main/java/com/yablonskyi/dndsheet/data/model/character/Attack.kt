package com.yablonskyi.dndsheet.data.model.character

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    "attacks",
    foreignKeys = [
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["characterId"])]
)
data class Attack(
    @PrimaryKey(autoGenerate = true) val attackId: Long = 0,
    val characterId: Long = 0,
    // Identity
    val name: String = "",
    // Hit
    val attackType: AttackType = AttackType.NONE,
    val ability: Ability = Ability.NONE,
    @ColumnInfo("is_proficient") val isProficient: Boolean = false,
    // Bonuses
    @ColumnInfo(name = "bonus_attack") val bonusToHit: Int = 0,
    @ColumnInfo(name = "bonus_damage") val bonusToDamage: Int = 0,
    // Damage Logic
    val damageDice: String = "",
    val damageType: DamageType = DamageType.SLASHING,
    // Properties
    val range: String = "5",
    val notes: String = "",
)