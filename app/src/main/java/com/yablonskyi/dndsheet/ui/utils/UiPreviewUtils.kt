package com.yablonskyi.dndsheet.ui.utils

import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.AbilityBlock
import com.yablonskyi.dndsheet.data.model.character.Attack
import com.yablonskyi.dndsheet.data.model.character.AttackType
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.Component
import com.yablonskyi.dndsheet.data.model.character.DamageType
import com.yablonskyi.dndsheet.data.model.character.MagicSchool
import com.yablonskyi.dndsheet.data.model.character.Money
import com.yablonskyi.dndsheet.data.model.character.ProficiencyLevel
import com.yablonskyi.dndsheet.data.model.character.Skill
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.data.model.character.SpellCastTime
import com.yablonskyi.dndsheet.data.model.character.SpellDuration
import com.yablonskyi.dndsheet.data.model.character.SpellLevel
import com.yablonskyi.dndsheet.data.model.character.SpellRangeType
import com.yablonskyi.dndsheet.data.model.character.SpellSettings
import com.yablonskyi.dndsheet.data.model.character.SpellSlot
import com.yablonskyi.dndsheet.ui.attack.AttackCalculator
import com.yablonskyi.dndsheet.ui.attack.AttackUiModel
import com.yablonskyi.dndsheet.ui.spell.SpellFilter

object UiUtils {

    val sampleCharacters = listOf(
        Character(
            id = 2,
            name = "Elara Moonwhisper",
            level = 9,
            currentHp = 38,
            maxHp = 42,
            tempHp = 5,
            hitDice = "9d6",
            charClass = "Wizard",
            subClass = "School of Evocation",
            race = "High Elf",
            speed = 30,
            armorClass = 12,
            shield = 0,
            coins = Money(1200),
            initiativeMiscBonus = 3,
            proficiencies = "Daggers, Darts, Slings, Quarterstaffs, Light Crossbows",
            traits = "Fey Ancestry, Trance, Keen Senses",
            feats = "War Caster, Spell Sniper",
            spellSettings = SpellSettings(
                spellCastingAbility = Ability.INT,
                dcMiscBonus = 0,
                attackMiscBonus = 0,
                spellSlots = mapOf(SpellLevel.LEVEL_1 to SpellSlot(current = 2, max = 4))
            ),
            abilityBlock = AbilityBlock(
                strength = 8, dexterity = 14, constitution = 14,
                intelligence = 20, wisdom = 12, charisma = 10
            ),
            skillProficiencies = mapOf(
                Skill.ARCANA to ProficiencyLevel.EXPERT,
                Skill.HISTORY to ProficiencyLevel.PROFICIENT,
                Skill.INVESTIGATION to ProficiencyLevel.PROFICIENT
            )
        ),
        Character(
            id = 1,
            name = "Thrain Ironfoot",
            level = 5,
            currentHp = 48,
            maxHp = 52,
            tempHp = 3,
            hitDice = "5d10",
            charClass = "Fighter",
            subClass = "Champion",
            race = "Mountain Dwarf",
            speed = 25,
            armorClass = 18,
            shield = 2,
            coins = Money(50,20),
            initiativeMiscBonus = 1,
            proficiencies = "Heavy Armor, Martial Weapons, Smith's Tools",
            traits = "Darkvision, Dwarven Resilience",
            feats = "Sentinel",
            spellSettings = SpellSettings(),
            abilityBlock = AbilityBlock(
                strength = 18, dexterity = 12, constitution = 16,
                intelligence = 10, wisdom = 13, charisma = 8
            ),
            skillProficiencies = mapOf(
                Skill.ATHLETICS to ProficiencyLevel.PROFICIENT,
                Skill.INTIMIDATION to ProficiencyLevel.PROFICIENT
            ),
            savingThrowProficiencies = setOf(Ability.STR, Ability.CON)
        ),


        Character(
            id = 3,
            name = "Nyx",
            level = 3,
            currentHp = 21,
            maxHp = 24,
            tempHp = 0,
            hitDice = "3d8",
            charClass = "Rogue",
            subClass = "Arcane Trickster",
            race = "Tiefling",
            speed = 30,
            armorClass = 14, // Leather Armor + DEX
            shield = 0,
            coins = Money(45),
            initiativeMiscBonus = 4,
            proficiencies = "Light Armor, Simple Weapons, Hand Crossbows, Thieves' Tools",
            traits = "Hellish Resistance, Infernal Legacy",
            feats = "Actor",
            spellSettings = SpellSettings(
                spellCastingAbility = Ability.INT,
                dcMiscBonus = 0,
                attackMiscBonus = 0
            ),
            abilityBlock = AbilityBlock(
                strength = 10, dexterity = 17, constitution = 13,
                intelligence = 14, wisdom = 10, charisma = 15
            ),
            skillProficiencies = mapOf(
                Skill.STEALTH to ProficiencyLevel.EXPERT,
                Skill.DECEPTION to ProficiencyLevel.PROFICIENT,
                Skill.ACROBATICS to ProficiencyLevel.PROFICIENT,
                Skill.SLEIGHT_OF_HAND to ProficiencyLevel.PROFICIENT
            )
        )
    )
    val sampleSpells = listOf(
        // --- CANTRIPS (2) ---
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
            description = "You hurl a mote of fire at a creature or object within range. Make a ranged spell attack against the target. On a hit, the target takes 1d10 fire damage. A flammable object hit by this spell ignites if it isn't being worn or carried.",
            higherLevels = "This spell's damage increases by 1d10 when you reach 5th level (2d10), 11th level (3d10), and 17th level (4d10)."
        ),
        Spell(
            spellId = 2,
            name = "Mage Hand",
            school = MagicSchool.CONJURATION,
            level = SpellLevel.CANTRIP,
            castTime = SpellCastTime.ACTION,
            rangeType = SpellRangeType.DISTANCE,
            rangeValue = 30,
            components = listOf(Component.VERBAL, Component.SOMATIC),
            duration = SpellDuration.ONE_MINUTE,
            description = "A spectral, floating hand appears at a point you choose within range. The hand lasts for the duration or until you dismiss it as an action. The hand can manipulate an object, open an unlocked door or container, stow or retrieve an item from an open container, or pour the contents out of a vial."
        ),

        // --- LEVEL 1 (4) ---
        Spell(
            spellId = 3,
            name = "Magic Missile",
            school = MagicSchool.EVOCATION,
            level = SpellLevel.LEVEL_1,
            castTime = SpellCastTime.ACTION,
            rangeType = SpellRangeType.DISTANCE,
            rangeValue = 120,
            components = listOf(Component.VERBAL, Component.SOMATIC),
            duration = SpellDuration.INSTANTANEOUS,
            damageType = DamageType.FORCE,
            damageDice = "3d4+3",
            description = "You create three glowing darts of magical force. Each dart hits a creature of your choice that you can see within range. A dart deals 1d4 + 1 force damage to its target. The darts all strike simultaneously, and you can direct them to hit one creature or several.",
            higherLevels = "When you cast this spell using a spell slot of 2nd level or higher, the spell creates one more dart for each slot level above 1st."
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
            description = "An invisible barrier of magical force appears and protects you. Until the start of your next turn, you have a +5 bonus to AC, including against the triggering attack, and you take no damage from magic missile."
        ),
        Spell(
            spellId = 5,
            name = "Cure Wounds",
            school = MagicSchool.EVOCATION,
            level = SpellLevel.LEVEL_1,
            castTime = SpellCastTime.ACTION,
            rangeType = SpellRangeType.TOUCH,
            components = listOf(Component.VERBAL, Component.SOMATIC),
            duration = SpellDuration.INSTANTANEOUS,
            description = "A creature you touch regains a number of hit points equal to 1d8 + your spellcasting ability modifier. This spell has no effect on undead or constructs.",
            higherLevels = "When you cast this spell using a spell slot of 2nd level or higher, the healing increases by 1d8 for each slot level above 1st."
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
            description = "For the duration, you sense the presence of magic within 30 feet of you. If you sense magic in this way, you can use your action to see a faint aura around any visible creature or object in the area that bears magic, and you learn its school of magic, if any."
        ),

        // --- LEVEL 3 (3) ---
        Spell(
            spellId = 7,
            name = "Fireball",
            school = MagicSchool.EVOCATION,
            level = SpellLevel.LEVEL_3,
            castTime = SpellCastTime.ACTION,
            rangeType = SpellRangeType.DISTANCE,
            rangeValue = 150,
            components = listOf(Component.VERBAL, Component.SOMATIC, Component.MATERIAL),
            material = "A tiny ball of bat guano and sulfur",
            duration = SpellDuration.INSTANTANEOUS,
            saveStat = Ability.DEX,
            damageType = DamageType.FIRE,
            damageDice = "8d6",
            description = "A bright streak flashes from your pointing finger to a point you choose within range and then blossoms with a low roar into an explosion of flame. Each creature in a 20-foot-radius sphere centered on that point must make a Dexterity saving throw. A target takes 8d6 fire damage on a failed save, or half as much on a successful one.",
            higherLevels = "When you cast this spell using a spell slot of 4th level or higher, the damage increases by 1d6 for each slot level above 3rd."
        ),
        Spell(
            spellId = 8,
            name = "Fly",
            school = MagicSchool.TRANSMUTATION,
            level = SpellLevel.LEVEL_3,
            castTime = SpellCastTime.ACTION,
            rangeType = SpellRangeType.TOUCH,
            components = listOf(Component.VERBAL, Component.SOMATIC, Component.MATERIAL),
            material = "A wing feather from any bird",
            duration = SpellDuration.TEN_MINUTES,
            isConcentration = true,
            description = "You touch a willing creature. The target gains a flying speed of 60 feet for the duration. When the spell ends, the target falls if it is still aloft, unless it can stop the fall.",
            higherLevels = "When you cast this spell using a spell slot of 4th level or higher, you can target one additional creature for each slot level above 3rd."
        ),
        Spell(
            spellId = 9,
            name = "Revivify",
            school = MagicSchool.NECROMANCY,
            level = SpellLevel.LEVEL_3,
            castTime = SpellCastTime.ACTION,
            rangeType = SpellRangeType.TOUCH,
            components = listOf(Component.VERBAL, Component.SOMATIC, Component.MATERIAL),
            material = "Diamonds worth 300 gp, which the spell consumes",
            duration = SpellDuration.INSTANTANEOUS,
            description = "You touch a creature that has died within the last minute. That creature returns to life with 1 hit point. This spell can't return to life a creature that has died of old age, nor can it restore any missing body parts."
        ),

        // --- LEVEL 4 (1) ---
        Spell(
            spellId = 10,
            name = "Dimension Door",
            school = MagicSchool.CONJURATION,
            level = SpellLevel.LEVEL_4,
            castTime = SpellCastTime.ACTION,
            rangeType = SpellRangeType.DISTANCE,
            rangeValue = 500,
            components = listOf(Component.VERBAL),
            duration = SpellDuration.INSTANTANEOUS,
            description = "You teleport yourself from your current location to any other spot within range. You arrive at exactly the spot desired. It can be a place you can see, one you can visualize, or one you can describe by stating distance and direction, such as '200 feet straight downward' or 'upward to the northwest at a 45-degree angle, 300 feet'."
        )
    )

    private val thrain = sampleCharacters.first()

    val rawAttacks = listOf(
        Attack(
            attackId = 101,
            characterId = thrain.id,
            name = "Warhammer +1",
            ability = Ability.STR,
            damageDice = "1d8",
            damageType = DamageType.BLUDGEONING,
            isProficient = true,
            bonusToHit = 1,    // Magic +1
            bonusToDamage = 1, // Magic +1
            attackType = AttackType.MELEE_ATTACK,
            range = "5ft",
            notes = "Versatile (1d10)"
        ),

        Attack(
            attackId = 102,
            characterId = thrain.id,
            name = "Handaxe",
            ability = Ability.STR,
            damageDice = "1d6",
            damageType = DamageType.SLASHING,
            isProficient = true,
            bonusToHit = 0,
            bonusToDamage = 0,
            attackType = AttackType.MELEE_ATTACK,
            range = "20/60ft",
            notes = "Light, Thrown"
        ),

        Attack(
            attackId = 103,
            characterId = thrain.id,
            name = "Heavy Crossbow",
            ability = Ability.DEX,
            damageDice = "1d10",
            damageType = DamageType.PIERCING,
            isProficient = true,
            bonusToHit = 0,
            bonusToDamage = 0,
            attackType = AttackType.RANGED_ATTACK,
            range = "100/400ft",
            notes = "Loading, Two-handed"
        )
    )

    val sampleAttacks = rawAttacks.map { attack ->
        val calculator = AttackCalculator(thrain, attack)

        AttackUiModel(
            id = attack.attackId,
            name = attack.name,
            toHit = calculator.getToHitModifier().let { if (it >= 0) "+$it" else "$it" },
            damage = calculator.getDamageString(),
            originalAttack = attack
        )
    }

    private fun getAvailableFilters(spells: List<Spell>): List<SpellFilter> {
        val filters = mutableListOf<SpellFilter>()

        filters.add(SpellFilter.All)

        val levels = spells.map { it.level }
            .distinct()
            .sortedBy { it.ordinal }

        levels.forEach { levelEnum ->
            filters.add(SpellFilter.ByLevel(levelEnum.ordinal))
        }

        if (spells.any { it.isConcentration }) {
            filters.add(SpellFilter.Concentration)
        }

        if (spells.any { it.isRitual }) {
            filters.add(SpellFilter.Ritual)
        }

        return filters
    }

    val availableFilters = getAvailableFilters(sampleSpells)

    val currentFilter = SpellFilter.All
}

data class DiceOptions(val sides: Int, val count: Int = 1, val iconRes: Int)

val diceOptions = listOf(
    DiceOptions(100, iconRes = R.drawable.ic_dice_d10),
    DiceOptions(20, iconRes = R.drawable.ic_dice_d20),
    DiceOptions(12, iconRes = R.drawable.ic_dice_d12),
    DiceOptions(10, iconRes = R.drawable.ic_dice_d10),
    DiceOptions(8, iconRes = R.drawable.ic_dice_d8),
    DiceOptions(6, iconRes = R.drawable.ic_dice_d6),
    DiceOptions(4, iconRes = R.drawable.ic_dice_d4),
)