package com.yablonskyi.data.backup

import com.yablonskyi.data.entity.CharacterEntity
import com.yablonskyi.model.backup.BackupCharacter
import com.yablonskyi.data.entity.AttackEntity
import com.yablonskyi.model.backup.BackupAttack
import com.yablonskyi.data.entity.DiceRollEntity
import com.yablonskyi.model.backup.BackupDiceRoll
import com.yablonskyi.data.entity.SpellEntity
import com.yablonskyi.model.backup.BackupSpell
import com.yablonskyi.data.entity.CharacterSpellCrossRefEntity
import com.yablonskyi.model.backup.BackupCharacterSpell
import com.yablonskyi.data.entity.RaceEntity
import com.yablonskyi.model.backup.BackupRace
import com.yablonskyi.data.entity.CharacterClassEntity
import com.yablonskyi.model.backup.BackupClass
import com.yablonskyi.data.entity.AbilityBlockEntity
import com.yablonskyi.model.backup.BackupAbilities
import com.yablonskyi.data.entity.SpellSettingsEntity
import com.yablonskyi.model.backup.BackupSpellSettings
import com.yablonskyi.model.backup.BackupDataV1
import com.yablonskyi.model.backup.BackupMoney
import com.yablonskyi.model.backup.BackupSpellSlot
import com.yablonskyi.model.backup.BackupDiceGroup
import com.yablonskyi.model.character.Money
import com.yablonskyi.model.character.SpellSlot
import com.yablonskyi.model.dice.DiceGroup

internal fun CharacterEntity.toBackup(assetIds: Map<String, String>): BackupCharacter = BackupCharacter(
    id = id,
    sortOrder = sortOrder,
    name = name,
    level = level,
    imageAssetId = imagePath?.let { assetIds.getValue(it) },
    currentHp = currentHp,
    maxHp = maxHp,
    tempHp = tempHp,
    hitDice = hitDice,
    charClass = charClass,
    subClass = subClass,
    race = race,
    speed = speed,
    armorClass = armorClass,
    shield = shield,
    coins = BackupMoney(coins.gold, coins.silver, coins.copper),
    initiativeMiscBonus = initiativeMiscBonus,
    proficiencies = proficiencies,
    traits = traits,
    feats = feats,
    inventory = inventory,
    backstory = backstory,
    notes = notes,
    spellSettings = spellSettings.toBackup(),
    abilityBlock = abilityBlock.toBackup(),
    skillProficiencies = skillProficiencies,
    savingThrowProficiencies = savingThrowProficiencies,
    passivePerceptionBonus = passivePerceptionBonus,
    hasJackOfAllTrades = hasJackOfAllTrades,
)

internal fun BackupCharacter.toEntity(imagePaths: Map<String, String>): CharacterEntity = CharacterEntity(
    id = id,
    sortOrder = sortOrder,
    name = name,
    level = level,
    imagePath = imageAssetId?.let { imagePaths.getValue(it) },
    currentHp = currentHp,
    maxHp = maxHp,
    tempHp = tempHp,
    hitDice = hitDice,
    charClass = charClass,
    subClass = subClass,
    race = race,
    speed = speed,
    armorClass = armorClass,
    shield = shield,
    coins = Money(coins.gold, coins.silver, coins.copper),
    initiativeMiscBonus = initiativeMiscBonus,
    proficiencies = proficiencies,
    traits = traits,
    feats = feats,
    inventory = inventory,
    backstory = backstory,
    notes = notes,
    spellSettings = spellSettings.toEntity(),
    abilityBlock = abilityBlock.toEntity(),
    skillProficiencies = skillProficiencies,
    savingThrowProficiencies = savingThrowProficiencies,
    passivePerceptionBonus = passivePerceptionBonus,
    hasJackOfAllTrades = hasJackOfAllTrades,
)

internal fun AttackEntity.toBackup(): BackupAttack = BackupAttack(
    attackId = attackId,
    characterId = characterId,
    name = name,
    attackType = attackType,
    ability = ability,
    isProficient = isProficient,
    bonusToHit = bonusToHit,
    bonusToDamage = bonusToDamage,
    damageDice = damageDice,
    damageType = damageType,
    range = range,
    notes = notes,
)

internal fun BackupAttack.toEntity(): AttackEntity = AttackEntity(
    attackId = attackId,
    characterId = characterId,
    name = name,
    attackType = attackType,
    ability = ability,
    isProficient = isProficient,
    bonusToHit = bonusToHit,
    bonusToDamage = bonusToDamage,
    damageDice = damageDice,
    damageType = damageType,
    range = range,
    notes = notes,
)

internal fun DiceRollEntity.toBackup(): BackupDiceRoll = BackupDiceRoll(
    id = id,
    characterId = characterId,
    label = label,
    numbers = numbers,
    modifier = modifier,
    result = result,
    dices = dices.map { BackupDiceGroup(it.sides, it.count) },
    timestamp = timestamp,
)

internal fun BackupDiceRoll.toEntity(): DiceRollEntity = DiceRollEntity(
    id = id,
    characterId = characterId,
    label = label,
    numbers = numbers,
    modifier = modifier,
    result = result,
    dices = dices.map { DiceGroup(it.sides, it.count) },
    timestamp = timestamp,
)

internal fun SpellEntity.toBackup(): BackupSpell = BackupSpell(
    spellId = spellId,
    name = name,
    school = school,
    level = level,
    castTime = castTime,
    rangeType = rangeType,
    rangeValue = rangeValue,
    components = components,
    material = material,
    isRitual = isRitual,
    duration = duration,
    isConcentration = isConcentration,
    attackType = attackType,
    saveStat = saveStat,
    damageType = damageType,
    damageDice = damageDice,
    description = description,
    higherLevels = higherLevels,
)

internal fun BackupSpell.toEntity(): SpellEntity = SpellEntity(
    spellId = spellId,
    name = name,
    school = school,
    level = level,
    castTime = castTime,
    rangeType = rangeType,
    rangeValue = rangeValue,
    components = components,
    material = material,
    isRitual = isRitual,
    duration = duration,
    isConcentration = isConcentration,
    attackType = attackType,
    saveStat = saveStat,
    damageType = damageType,
    damageDice = damageDice,
    description = description,
    higherLevels = higherLevels,
)

internal fun CharacterSpellCrossRefEntity.toBackup(): BackupCharacterSpell = BackupCharacterSpell(
    characterId = characterId,
    spellId = spellId,
)

internal fun BackupCharacterSpell.toEntity(): CharacterSpellCrossRefEntity = CharacterSpellCrossRefEntity(
    characterId = characterId,
    spellId = spellId,
)

internal fun RaceEntity.toBackup(): BackupRace = BackupRace(
    id = id,
    name = name,
    size = size,
    speed = speed,
    abilityBonuses = abilityBonuses,
    grantedSkills = grantedSkills,
    traits = traits,
    description = description,
    isHomebrew = isHomebrew,
)

internal fun BackupRace.toEntity(): RaceEntity = RaceEntity(
    id = id,
    name = name,
    size = size,
    speed = speed,
    abilityBonuses = abilityBonuses,
    grantedSkills = grantedSkills,
    traits = traits,
    description = description,
    isHomebrew = isHomebrew,
)

internal fun CharacterClassEntity.toBackup(): BackupClass = BackupClass(
    id = id,
    name = name,
    hitDice = hitDice,
    primaryAbility = primaryAbility,
    savingThrows = savingThrows,
    skillChoiceCount = skillChoiceCount,
    availableSkills = availableSkills,
    spellcastingAbility = spellcastingAbility,
    description = description,
    isHomebrew = isHomebrew,
)

internal fun BackupClass.toEntity(): CharacterClassEntity = CharacterClassEntity(
    id = id,
    name = name,
    hitDice = hitDice,
    primaryAbility = primaryAbility,
    savingThrows = savingThrows,
    skillChoiceCount = skillChoiceCount,
    availableSkills = availableSkills,
    spellcastingAbility = spellcastingAbility,
    description = description,
    isHomebrew = isHomebrew,
)

internal fun AbilityBlockEntity.toBackup(): BackupAbilities = BackupAbilities(
    strength = strength,
    dexterity = dexterity,
    constitution = constitution,
    intelligence = intelligence,
    wisdom = wisdom,
    charisma = charisma,
)

internal fun BackupAbilities.toEntity(): AbilityBlockEntity = AbilityBlockEntity(
    strength = strength,
    dexterity = dexterity,
    constitution = constitution,
    intelligence = intelligence,
    wisdom = wisdom,
    charisma = charisma,
)

internal fun SpellSettingsEntity.toBackup(): BackupSpellSettings = BackupSpellSettings(
    spellCastingAbility = spellCastingAbility,
    dcMiscBonus = dcMiscBonus,
    attackMiscBonus = attackMiscBonus,
    spellSlots = spellSlots.mapValues { (_, slot) -> BackupSpellSlot(slot.max, slot.current) },
)

internal fun BackupSpellSettings.toEntity(): SpellSettingsEntity = SpellSettingsEntity(
    spellCastingAbility = spellCastingAbility,
    dcMiscBonus = dcMiscBonus,
    attackMiscBonus = attackMiscBonus,
    spellSlots = spellSlots.mapValues { (_, slot) -> SpellSlot(slot.max, slot.current) },
)

internal fun BackupRoomSnapshot.toBackup(assetIds: Map<String, String>): BackupDataV1 = BackupDataV1(
    characters = characters.map { it.toBackup(assetIds) },
    attacks = attacks.map { it.toBackup() },
    diceRolls = diceRolls.map { it.toBackup() },
    spells = spells.map { it.toBackup() },
    characterSpells = characterSpells.map { it.toBackup() },
    races = races.map { it.toBackup() },
    classes = classes.map { it.toBackup() },
)

internal fun BackupDataV1.toRoomSnapshot(imagePaths: Map<String, String>): BackupRoomSnapshot = BackupRoomSnapshot(
    characters = characters.map { it.toEntity(imagePaths) },
    attacks = attacks.map { it.toEntity() },
    diceRolls = diceRolls.map { it.toEntity() },
    spells = spells.map { it.toEntity() },
    characterSpells = characterSpells.map { it.toEntity() },
    races = races.map { it.toEntity() },
    classes = classes.map { it.toEntity() },
)
