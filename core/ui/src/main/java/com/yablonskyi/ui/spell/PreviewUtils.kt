package com.yablonskyi.ui.spell

import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.AttackType
import com.yablonskyi.model.character.Component
import com.yablonskyi.model.character.DamageType
import com.yablonskyi.model.character.MagicSchool
import com.yablonskyi.model.character.Spell
import com.yablonskyi.model.character.SpellCastTime
import com.yablonskyi.model.character.SpellDuration
import com.yablonskyi.model.character.SpellLevel
import com.yablonskyi.model.character.SpellRangeType
import com.yablonskyi.ui.utils.PreviewStateProvider

object PreviewUtils {

    val sampleSpells: List<Spell> = listOf(
        Spell(
            spellId = 1,
            name = "Fire Bolt",
            school = MagicSchool.EVOCATION,
            level = SpellLevel.CANTRIP,
            castTime = SpellCastTime.ACTION,
            rangeType = SpellRangeType.DISTANCE,
            rangeValue = 120,
            components = listOf(Component.VERBAL, Component.SOMATIC),
            duration = SpellDuration.INSTANTANEOUS,
            attackType = AttackType.RANGED_ATTACK,
            damageType = DamageType.FIRE,
            damageDice = "1d10",
            description = "You hurl a mote of fire at a creature or object within range."
        ),
        Spell(
            spellId = 2,
            name = "Minor Illusion",
            school = MagicSchool.ILLUSION,
            level = SpellLevel.CANTRIP,
            castTime = SpellCastTime.ACTION,
            rangeType = SpellRangeType.DISTANCE,
            rangeValue = 30,
            components = listOf(Component.SOMATIC, Component.MATERIAL),
            material = "A bit of fleece",
            duration = SpellDuration.ONE_MINUTE,
            description = "You create a sound or an image of an object within range that lasts for the duration."
        ),
        Spell(
            spellId = 3,
            name = "Mage Hand",
            school = MagicSchool.CONJURATION,
            level = SpellLevel.CANTRIP,
            castTime = SpellCastTime.ACTION,
            rangeType = SpellRangeType.DISTANCE,
            rangeValue = 30,
            components = listOf(Component.VERBAL, Component.SOMATIC),
            duration = SpellDuration.ONE_MINUTE,
            description = "A spectral hand appears at a point you choose within range."
        ),
        Spell(
            spellId = 4,
            name = "Shield",
            school = MagicSchool.ABJURATION,
            level = SpellLevel.LEVEL_1,
            castTime = SpellCastTime.REACTION,
            rangeType = SpellRangeType.SELF,
            components = listOf(Component.VERBAL, Component.SOMATIC),
            duration = SpellDuration.ONE_ROUND,
            description = "An invisible barrier of magical force appears and protects you."
        ),
        Spell(
            spellId = 5,
            name = "Magic Missile",
            school = MagicSchool.EVOCATION,
            level = SpellLevel.LEVEL_1,
            castTime = SpellCastTime.ACTION,
            rangeType = SpellRangeType.DISTANCE,
            rangeValue = 120,
            components = listOf(Component.VERBAL, Component.SOMATIC),
            duration = SpellDuration.INSTANTANEOUS,
            attackType = AttackType.NONE,
            damageType = DamageType.FORCE,
            damageDice = "3d4+3",
            description = "You create three glowing darts of magical force."
        ),
        Spell(
            spellId = 6,
            name = "Detect Magic",
            school = MagicSchool.DIVINATION,
            level = SpellLevel.LEVEL_1,
            castTime = SpellCastTime.ACTION,
            rangeType = SpellRangeType.SELF,
            components = listOf(Component.VERBAL, Component.SOMATIC),
            isRitual = true,
            duration = SpellDuration.TEN_MINUTES,
            isConcentration = true,
            description = "For the duration, you sense the presence of magic within 30 feet of yourself."
        ),
        Spell(
            spellId = 7,
            name = "Cure Wounds",
            school = MagicSchool.EVOCATION,
            level = SpellLevel.LEVEL_1,
            castTime = SpellCastTime.ACTION,
            rangeType = SpellRangeType.TOUCH,
            components = listOf(Component.VERBAL, Component.SOMATIC),
            duration = SpellDuration.INSTANTANEOUS,
            description = "A creature you touch regains a number of hit points equal to 1d8 + your spellcasting modifier."
        ),
        Spell(
            spellId = 8,
            name = "Invisibility",
            school = MagicSchool.ILLUSION,
            level = SpellLevel.LEVEL_2,
            castTime = SpellCastTime.ACTION,
            rangeType = SpellRangeType.TOUCH,
            components = listOf(Component.VERBAL, Component.SOMATIC, Component.MATERIAL),
            material = "An eyelash encased in gum arabic",
            duration = SpellDuration.ONE_HOUR,
            isConcentration = true,
            description = "A creature you touch becomes invisible until the spell ends."
        ),
        Spell(
            spellId = 9,
            name = "Hold Person",
            school = MagicSchool.ENCHANTMENT,
            level = SpellLevel.LEVEL_2,
            castTime = SpellCastTime.ACTION,
            rangeType = SpellRangeType.DISTANCE,
            rangeValue = 60,
            components = listOf(Component.VERBAL, Component.SOMATIC, Component.MATERIAL),
            material = "A small, straight piece of iron",
            duration = SpellDuration.ONE_MINUTE,
            isConcentration = true,
            attackType = AttackType.SAVE,
            saveStat = Ability.WIS,
            description = "Choose a humanoid that you can see within range. The target must succeed on a Wisdom saving throw or be paralyzed."
        ),
        Spell(
            spellId = 10,
            name = "Counterspell",
            school = MagicSchool.ABJURATION,
            level = SpellLevel.LEVEL_3,
            castTime = SpellCastTime.REACTION,
            rangeType = SpellRangeType.DISTANCE,
            rangeValue = 60,
            components = listOf(Component.SOMATIC),
            duration = SpellDuration.INSTANTANEOUS,
            description = "You attempt to interrupt a creature in the process of casting a spell."
        ),
        Spell(
            spellId = 11,
            name = "Fireball",
            school = MagicSchool.EVOCATION,
            level = SpellLevel.LEVEL_3,
            castTime = SpellCastTime.ACTION,
            rangeType = SpellRangeType.DISTANCE,
            rangeValue = 150,
            components = listOf(Component.VERBAL, Component.SOMATIC, Component.MATERIAL),
            material = "A tiny ball of bat guano and sulfur",
            duration = SpellDuration.INSTANTANEOUS,
            attackType = AttackType.SAVE,
            saveStat = Ability.DEX,
            damageType = DamageType.FIRE,
            damageDice = "8d6",
            higherLevels = "When you cast this spell using a spell slot of 4th level or higher, the damage increases by 1d6 for each slot level above 3rd.",
            description = "A bright streak flashes from your pointing finger to a point you choose within range and then blossoms with a low roar into an explosion of flame."
        ),
        Spell(
            spellId = 12,
            name = "Fly",
            school = MagicSchool.TRANSMUTATION,
            level = SpellLevel.LEVEL_3,
            castTime = SpellCastTime.ACTION,
            rangeType = SpellRangeType.TOUCH,
            components = listOf(Component.VERBAL, Component.SOMATIC, Component.MATERIAL),
            material = "A wing feather from any bird",
            duration = SpellDuration.TEN_MINUTES,
            isConcentration = true,
            description = "This spell gives the willing creature you touch the ability to fly until the spell ends."
        )
    )

    val loading: SpellLibraryState = SpellLibraryState(isLoading = true)

    val learnMode: SpellLibraryState = SpellLibraryState(
        spells = sampleSpells.map { SpellLibraryItem(it, isLearned = it.isConcentration) },
        isLearnMode = true
    )

    val selectionMode: SpellLibraryState = SpellLibraryState(
        spells = sampleSpells.map { SpellLibraryItem(it, isLearned = false) },
        selectedSpellIds = sampleSpells
            .filter { it.level == SpellLevel.LEVEL_1 }
            .mapTo(mutableSetOf()) { it.spellId },
        isSelectionMode = true,
        isAllSelected = false
    )

    val filtered: SpellLibraryState = SpellLibraryState(
        spells = sampleSpells
            .filter { it.level == SpellLevel.LEVEL_3 }
            .map { SpellLibraryItem(it, isLearned = false) },
        filterState = SpellFilterState(
            levels = setOf(SpellLevel.LEVEL_3),
            onlyConcentration = true
        )
    )

    val StateProvider = object : PreviewStateProvider<SpellLibraryState> {
        override val default: SpellLibraryState = SpellLibraryState(
            spells = sampleSpells.map { SpellLibraryItem(it, isLearned = false) }
        )

        override val samples: List<SpellLibraryState>
            get() = listOf(default, loading, learnMode, selectionMode, filtered)
    }
}