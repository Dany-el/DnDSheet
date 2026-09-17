package com.yablonskyi.character.presentation.sheet

import com.yablonskyi.dice.DiceIntent
import com.yablonskyi.domain.character.CharacterChange
import com.yablonskyi.model.character.Spell

internal fun dispatchSpellCast(
    spell: Spell,
    onIntent: (CharacterSheetIntent) -> Unit,
    onDiceIntent: (DiceIntent) -> Unit,
) {
    spell.damageDice?.takeIf { it.isNotBlank() }?.let { dice ->
        onDiceIntent(DiceIntent.SpellDamageRoll(spell, dice))
    }
    if (!spell.level.isCantrip) {
        onIntent(CharacterSheetIntent.Change(CharacterChange.SlotUsed(spell.level, 1)))
    }
}
