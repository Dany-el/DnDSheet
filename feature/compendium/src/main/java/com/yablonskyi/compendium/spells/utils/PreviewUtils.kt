package com.yablonskyi.compendium.spells.utils

import com.yablonskyi.compendium.spells.viewmodel.SpellFormUiState
import com.yablonskyi.ui.spell.PreviewUtils as CoreUiSpellPreviewUtils
import com.yablonskyi.ui.validation.FieldState
import com.yablonskyi.ui.validation.FieldValidator
import com.yablonskyi.ui.validation.IntFieldState
import com.yablonskyi.ui.validation.SpellValidators
import com.yablonskyi.ui.validation.ValidationPattern

internal object PreviewUtils {

    val spellFormState: SpellFormUiState
        get() {
            val spell = CoreUiSpellPreviewUtils.sampleSpells.first { it.higherLevels != null }
            return SpellFormUiState(
                id = spell.spellId,
                name = FieldState(validator = SpellValidators.name)
                    .copy(text = spell.name),
                material = FieldState(validator = SpellValidators.material)
                    .copy(text = spell.material.orEmpty()),
                damageDice = FieldState(validator = SpellValidators.damageDice)
                    .copy(text = spell.damageDice.orEmpty()),
                description = FieldState(validator = SpellValidators.description)
                    .copy(text = spell.description),
                higherLevels = FieldState(validator = SpellValidators.higherLevels)
                    .copy(text = spell.higherLevels.orEmpty()),
                level = spell.level,
                school = spell.school,
                castTime = spell.castTime,
                rangeType = spell.rangeType,
                rangeValueField = IntFieldState(
                    value = spell.rangeValue ?: 0,
                    minValue = 0,
                    maxValue = 999,
                    validator = FieldValidator(ValidationPattern.Range(0, 999))
                ),
                components = spell.components.toSet(),
                duration = spell.duration,
                isRitual = spell.isRitual,
                isConcentration = spell.isConcentration,
                attackType = spell.attackType,
                saveStat = spell.saveStat,
                damageType = spell.damageType
            )
        }
}