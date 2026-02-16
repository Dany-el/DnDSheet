package com.yablonskyi.dndsheet.data.model.character

import androidx.annotation.StringRes
import com.yablonskyi.dndsheet.R

enum class MagicSchool(@StringRes val resId: Int) {
    ABJURATION(R.string.school_abjuration),
    CONJURATION(R.string.school_conjuration),
    DIVINATION(R.string.school_divination),
    ENCHANTMENT(R.string.school_enchantment),
    EVOCATION(R.string.school_evocation),
    ILLUSION(R.string.school_illusion),
    NECROMANCY(R.string.school_necromancy),
    TRANSMUTATION(R.string.school_transmutation)
}

enum class Component(@StringRes val resId: Int) {
    VERBAL(R.string.comp_verbal),
    SOMATIC(R.string.comp_somatic),
    MATERIAL(R.string.comp_material)
}

enum class AttackType(@StringRes val resId: Int) {
    NONE(R.string.attack_none),
    MELEE_ATTACK(R.string.attack_melee),
    RANGED_ATTACK(R.string.attack_ranged),
    SAVE(R.string.attack_save)
}

enum class DamageType(@StringRes val resId: Int) {
    ACID(R.string.damage_acid),
    BLUDGEONING(R.string.damage_bludgeoning),
    COLD(R.string.damage_cold),
    FIRE(R.string.damage_fire),
    FORCE(R.string.damage_force),
    LIGHTNING(R.string.damage_lightning),
    NECROTIC(R.string.damage_necrotic),
    PIERCING(R.string.damage_piercing),
    POISON(R.string.damage_poison),
    PSYCHIC(R.string.damage_psychic),
    RADIANT(R.string.damage_radiant),
    SLASHING(R.string.damage_slashing),
    THUNDER(R.string.damage_thunder)
}

enum class SpellLevel(val value: Int, @StringRes val resId: Int) {
    CANTRIP(0, R.string.level_cantrip),
    LEVEL_1(1, R.string.level_1),
    LEVEL_2(2, R.string.level_2),
    LEVEL_3(3, R.string.level_3),
    LEVEL_4(4, R.string.level_4),
    LEVEL_5(5, R.string.level_5),
    LEVEL_6(6, R.string.level_6),
    LEVEL_7(7, R.string.level_7),
    LEVEL_8(8, R.string.level_8),
    LEVEL_9(9, R.string.level_9);

    val isCantrip: Boolean
        get() = this == CANTRIP
}

enum class SpellCastTime(@StringRes val resId: Int, @StringRes val clippedResId: Int) {
    ACTION(R.string.time_action, R.string.clipped_time_action),
    BONUS_ACTION(R.string.time_bonus_action, R.string.clipped_time_bonus_action),
    REACTION(R.string.time_reaction, R.string.clipped_time_reaction),
    SPECIAL(R.string.time_special, R.string.clipped_time_special);
}

enum class SpellDuration(@StringRes val resId: Int) {
    INSTANTANEOUS(R.string.duration_instant),
    ONE_ROUND(R.string.duration_1_round),
    ONE_MINUTE(R.string.duration_1_min),
    TEN_MINUTES(R.string.duration_10_min),
    ONE_HOUR(R.string.duration_1_hr),
    EIGHT_HOURS(R.string.duration_8_hr),
    TWENTY_FOUR_HOURS(R.string.duration_24_hr),
    SEVEN_DAYS(R.string.duration_7_days),
    THIRTY_DAYS(R.string.duration_30_days),
    UNTIL_DISPELLED(R.string.duration_until_dispelled),
    SPECIAL(R.string.duration_special);
}

enum class SpellRangeType(@StringRes val resId: Int) {
    SELF(R.string.range_self),
    TOUCH(R.string.range_touch),
    DISTANCE(R.string.range_distance),
    SIGHT(R.string.range_sight),
    UNLIMITED(R.string.range_unlimited)
}