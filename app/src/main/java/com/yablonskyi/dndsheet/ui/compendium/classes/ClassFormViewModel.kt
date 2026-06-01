package com.yablonskyi.dndsheet.ui.compendium.classes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.Skill
import com.yablonskyi.dndsheet.data.model.dice.DiceRoles
import com.yablonskyi.dndsheet.data.model.rulebook.CharacterClass
import com.yablonskyi.dndsheet.ui.compendium.races.FieldState
import com.yablonskyi.dndsheet.ui.compendium.races.IntFieldState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.UUID

class ClassFormViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ClassFormUiState())
    val uiState: StateFlow<ClassFormUiState> = _uiState.asStateFlow()

    val availableSavingThrows: StateFlow<List<Ability>> = _uiState
        .map { state ->
            Ability.playableAbilities.filterNot { it in state.savingThrows }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = Ability.playableAbilities
        )

    val availableSkills: StateFlow<List<Skill>> = _uiState
        .map { state ->
            Skill.entries.filterNot { it in state.availableSkills }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = Skill.entries
        )

    // ── Name ──────────────────────────────────────────────────────────────────

    fun onNameChanged(newText: String) {
        val current = _uiState.value.nameField
        val updated = current.copy(
            text = newText,
            error = if (current.error != null) validateTextField(newText) else null
        )
        updateState(_uiState.value.copy(nameField = updated))
    }

    fun onNameFocusChanged(isFocused: Boolean) {
        _uiState.update { state ->
            val current = state.nameField
            val updated = when {
                !isFocused && current.isFocusedBefore ->
                    current.copy(error = validateTextField(current.text))

                isFocused -> current.copy(isFocusedBefore = true)
                else -> current
            }
            state.copy(nameField = updated)
        }
    }

    // ── Hit Dice ──────────────────────────────────────────────────────────────

    fun onHitDiceChanged(newText: String) {
        val current = _uiState.value.hitDiceField
        val updated = current.copy(
            text = newText,
            error = if (current.error != null) validateTextField(newText) else null
        )
        updateState(_uiState.value.copy(hitDiceField = updated))
    }

    // ── Primary Ability ───────────────────────────────────────────────────────

    fun onPrimaryAbilityChanged(ability: Ability) {
        updateState(_uiState.value.copy(primaryAbility = ability))
    }

    // ── Skill Choice Count ────────────────────────────────────────────────────

    fun onSkillChoiceCountChanged(newValue: Int) {
        val current = _uiState.value.skillChoiceCountField

        Log.i(
            "SkillChoiceCountChanged", "Value: $newValue, Error: ${
                validateIntField(
                    value = newValue,
                    minValue = current.minValue,
                    maxValue = current.maxValue
                )
            }"
        )

        val updated = current.copy(
            value = newValue,
            error = if (current.error != null)
                validateIntField(
                    value = newValue, minValue = current.minValue, maxValue = current.maxValue
                ) else null,
            minValue = current.minValue,
            maxValue = current.maxValue
        )
        updateState(_uiState.value.copy(skillChoiceCountField = updated))
    }

    fun onSkillChoiceCountFocusChanged(isFocused: Boolean) {
        _uiState.update { state ->
            val current = state.skillChoiceCountField
            val updated = when {
                !isFocused && current.isFocusedBefore ->
                    current.copy(
                        error = validateIntField(
                            value = current.value,
                            minValue = current.minValue,
                            maxValue = current.maxValue
                        )
                    )

                isFocused -> current.copy(isFocusedBefore = true)
                else -> current
            }
            state.copy(skillChoiceCountField = updated)
        }
    }

    // ── Spellcasting Ability ──────────────────────────────────────────────────

    fun onSpellcastingAbilityChanged(ability: Ability?) {
        updateState(_uiState.value.copy(spellcastingAbility = ability))
    }

    // ── Description ───────────────────────────────────────────────────────────

    fun onDescriptionChanged(newText: String) {
        val current = _uiState.value.descriptionField
        val updated = current.copy(
            text = newText,
            error = if (current.error != null) validateTextField(newText) else null
        )
        updateState(_uiState.value.copy(descriptionField = updated))
    }

    fun onDescriptionFocusChanged(isFocused: Boolean) {
        _uiState.update { state ->
            val current = state.descriptionField
            val updated = when {
                !isFocused && current.isFocusedBefore ->
                    current.copy(error = validateTextField(current.text))

                isFocused -> current.copy(isFocusedBefore = true)
                else -> current
            }
            state.copy(descriptionField = updated)
        }
    }

    // ── Saving Throws ─────────────────────────────────────────────────────────

    fun addSavingThrow(ability: Ability) {
        updateState(_uiState.value.copy(savingThrows = _uiState.value.savingThrows + ability))
    }

    fun removeSavingThrow(ability: Ability) {
        updateState(_uiState.value.copy(savingThrows = _uiState.value.savingThrows - ability))
    }

    // ── Available Skills ──────────────────────────────────────────────────────

    fun addAvailableSkill(skill: Skill) {
        updateState(
            _uiState.value.copy(availableSkills = _uiState.value.availableSkills + skill)
        )
    }

    fun removeAvailableSkill(skill: Skill) {
        updateState(
            _uiState.value.copy(availableSkills = _uiState.value.availableSkills - skill)
        )
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    fun initializeWithClass(cls: CharacterClass) {
        _uiState.update { current ->
            if (current.nameField.text.isNotEmpty() || current.descriptionField.text.isNotEmpty()) {
                return@update current
            }
            current.copy(
                id = cls.id,
                nameField = FieldState(text = cls.name),
                hitDiceField = FieldState(text = cls.hitDice),
                primaryAbility = cls.primaryAbility,
                savingThrows = cls.savingThrows,
                skillChoiceCountField = IntFieldState(
                    value = cls.skillChoiceCount,
                    maxValue = 10,
                    minValue = 0
                ),
                availableSkills = cls.availableSkills,
                spellcastingAbility = cls.spellcastingAbility,
                descriptionField = FieldState(text = cls.description)
            )
        }
    }

    private fun updateState(newState: ClassFormUiState) {
        _uiState.update {
            newState.copy(
                isFormValid = validateTextField(newState.nameField.text) == null &&
                        validateTextField(newState.descriptionField.text) == null &&
                        validateIntField(
                            value = newState.skillChoiceCountField.value,
                            minValue = newState.skillChoiceCountField.minValue,
                            maxValue = newState.skillChoiceCountField.maxValue
                        ) == null
            )
        }
    }

    private fun validateTextField(text: String): Int? =
        if (text.isBlank()) R.string.error_empty else null

    private fun validateIntField(value: Int, maxValue: Int, minValue: Int): Int? {
        Log.i("ValidatorInt", "$value in $minValue .. $maxValue: ${value in minValue..maxValue}")
        return if (value in minValue..maxValue) {
            null
        } else {
            R.string.error_range
        }
    }
}

data class ClassFormUiState(
    val id: String = "",
    val nameField: FieldState = FieldState(),
    val hitDiceField: FieldState = FieldState(text = DiceRoles.D8.roll.drop(1)),
    val primaryAbility: Ability = Ability.STR,
    val savingThrows: Set<Ability> = emptySet(),
    val skillChoiceCountField: IntFieldState = IntFieldState(
        value = 2,
        maxValue = 10,
        minValue = 0
    ),
    val availableSkills: List<Skill> = emptyList(),
    val spellcastingAbility: Ability? = null,
    val descriptionField: FieldState = FieldState(),
    val isFormValid: Boolean = false
)

fun ClassFormUiState.toCharacterClass(): CharacterClass = CharacterClass(
    id = id.ifBlank { UUID.randomUUID().toString() },
    name = nameField.text,
    hitDice = hitDiceField.text,
    primaryAbility = primaryAbility,
    savingThrows = savingThrows,
    skillChoiceCount = skillChoiceCountField.value,
    availableSkills = availableSkills,
    spellcastingAbility = spellcastingAbility,
    description = descriptionField.text,
    isHomebrew = true
)