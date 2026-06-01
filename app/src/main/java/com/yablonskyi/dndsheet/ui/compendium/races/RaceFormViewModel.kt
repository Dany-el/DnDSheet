package com.yablonskyi.dndsheet.ui.compendium.races

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.Skill
import com.yablonskyi.dndsheet.data.model.rulebook.Race
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.UUID

class RaceFormViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RaceFormUiState())
    val uiState: StateFlow<RaceFormUiState> = _uiState.asStateFlow()

    val availableAbilities: StateFlow<List<Ability>> = _uiState
        .map { state ->
            Ability.playableAbilities.filterNot { it in state.abilityBonuses.keys }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = Ability.playableAbilities
        )

    val availableSkills: StateFlow<List<Skill>> = _uiState
        .map { state ->
            Skill.entries.filterNot { it in state.grantedSkills }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = Skill.entries
        )

    fun onNameChanged(newText: String) {
        val currentField = _uiState.value.nameField
        val updatedField = currentField.copy(
            text = newText,
            error = if (currentField.error != null) validateTextField(newText) else null
        )
        updateState(
            _uiState.value.copy(
                nameField = updatedField
            )
        )
    }

    fun onNameFocusChanged(isFocused: Boolean) {
        _uiState.update { currentState ->
            val currentField = currentState.nameField

            val updatedField = when {
                !isFocused && currentField.isFocusedBefore -> currentField.copy(
                    error = validateTextField(
                        currentField.text
                    )
                )

                isFocused -> currentField.copy(isFocusedBefore = true)
                else -> currentField
            }

            currentState.copy(nameField = updatedField)
        }
    }

    fun onSizeChanged(newText: String) {
        val currentField = _uiState.value.sizeField
        val updatedField = currentField.copy(
            text = newText,
            error = if (currentField.error != null) validateTextField(newText) else null
        )
        updateState(
            _uiState.value.copy(
                sizeField = updatedField
            )
        )
    }

    fun onSizeFocusChanged(isFocused: Boolean) {
        _uiState.update { currentState ->
            val currentField = currentState.sizeField

            val updatedField = when {
                !isFocused && currentField.isFocusedBefore -> currentField.copy(
                    error = validateTextField(
                        currentField.text
                    )
                )

                isFocused -> currentField.copy(isFocusedBefore = true)
                else -> currentField
            }

            currentState.copy(sizeField = updatedField)
        }
    }

    fun onSpeedChanged(newValue: Int) {
        val currentField = _uiState.value.speedField
        val updatedField = currentField.copy(
            value = newValue,
            error = if (currentField.error != null)
                validateIntField(
                    value = newValue,
                    maxValue = currentField.maxValue,
                    minValue = currentField.minValue
                ) else null
        )
        updateState(
            _uiState.value.copy(
                speedField = updatedField
            )
        )
    }

    fun onSpeedFocusChanged(isFocused: Boolean) {
        _uiState.update { currentState ->
            val currentField = currentState.speedField

            val updatedField = when {
                !isFocused && currentField.isFocusedBefore -> currentField.copy(
                    error = validateIntField(
                        value = currentField.value,
                        maxValue = currentField.maxValue,
                        minValue = currentField.minValue
                    )
                )

                isFocused -> currentField.copy(isFocusedBefore = true)
                else -> currentField
            }

            currentState.copy(speedField = updatedField)
        }
    }

    fun onDescriptionChanged(newText: String) {
        val currentField = _uiState.value.descriptionField
        val updatedField = currentField.copy(
            text = newText,
            error = if (currentField.error != null) validateTextField(newText) else null
        )
        updateState(
            _uiState.value.copy(
                descriptionField = updatedField
            )
        )
    }

    fun onDescriptionFocusChanged(isFocused: Boolean) {
        _uiState.update { currentState ->
            val currentField = currentState.descriptionField

            val updatedField = when {
                !isFocused && currentField.isFocusedBefore -> currentField.copy(
                    error = validateTextField(
                        currentField.text
                    )
                )

                isFocused -> currentField.copy(isFocusedBefore = true)
                else -> currentField
            }

            currentState.copy(descriptionField = updatedField)
        }
    }

    fun onTraitsChanged(newText: String) {
        val currentField = _uiState.value.traitsField

        val parsedTraits = newText.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val updatedField = currentField.copy(
            text = newText,
            error = validateTraitsPattern(newText)
        )

        updateState(
            _uiState.value.copy(
                traits = parsedTraits,
                traitsField = updatedField
            )
        )
    }

    fun onTraitsFocusChanged(isFocused: Boolean) {
        _uiState.update { currentState ->
            val currentField = currentState.traitsField

            val updatedField = when {
                !isFocused && currentField.isFocusedBefore -> currentField.copy(
                    error = validateTraitsPattern(
                        currentField.text
                    )
                )

                isFocused -> currentField.copy(isFocusedBefore = true)
                else -> currentField
            }

            currentState.copy(traitsField = updatedField)
        }
    }

    fun addAbilityBonus(ability: Ability) {
        val updatedMap = _uiState.value.abilityBonuses + (ability to 1)

        updateState(
            _uiState.value.copy(
                abilityBonuses = updatedMap
            )
        )
    }

    fun updateAbilityValue(ability: Ability, newValue: Int) {
        val updatedMap = _uiState.value.abilityBonuses.toMutableMap().apply {
            this[ability] = newValue
        }
        updateState(
            _uiState.value.copy(
                abilityBonuses = updatedMap
            )
        )
    }

    fun removeAbilityBonus(ability: Ability) {
        val updatedMap = _uiState.value.abilityBonuses - ability

        updateState(
            _uiState.value.copy(
                abilityBonuses = updatedMap
            )
        )
    }

    fun addGrantedSkill(skill: Skill) {
        updateState(
            _uiState.value.copy(
                grantedSkills = _uiState.value.grantedSkills + skill
            )
        )
    }

    fun removeGrantedSkill(skill: Skill) {
        updateState(
            _uiState.value.copy(
                grantedSkills = _uiState.value.grantedSkills - skill
            )
        )
    }

    private fun updateState(newState: RaceFormUiState) {
        _uiState.update {
            newState.copy(
                isFormValid = validateTextField(newState.nameField.text) == null &&
                        validateTextField(newState.sizeField.text) == null &&
                        validateIntField(
                            newState.speedField.value,
                            maxValue = newState.speedField.maxValue,
                            minValue = newState.speedField.minValue
                        ) == null &&
                        validateTextField(newState.descriptionField.text) == null
            )
        }
    }

    fun initializeWithRace(race: Race) {
        _uiState.update { currentState ->
            if (currentState.nameField.text.isNotEmpty() || currentState.descriptionField.text.isNotEmpty()) {
                return@update currentState
            }

            currentState.copy(
                id = race.id,
                nameField = FieldState(text = race.name),
                sizeField = FieldState(text = race.size),
                speedField = IntFieldState(value = race.speed, maxValue = 100, minValue = 0),
                descriptionField = FieldState(text = race.description),
                traitsField = FieldState(text = race.traits.joinToString(", ")),
                traits = race.traits,
                abilityBonuses = race.abilityBonuses,
                grantedSkills = race.grantedSkills
            )
        }
    }

    private fun validateTextField(text: String): Int? =
        if (text.isBlank()) R.string.error_empty else null

    private fun validateIntField(value: Int, maxValue: Int, minValue: Int): Int? =
        if (value !in minValue..maxValue) R.string.error_range else null

    private fun validateTraitsPattern(text: String): Int? {
        if (text.isEmpty()) return null

        val traits = text.split(",")
        val isValid = traits.all { it.trim().isNotEmpty() }

        return if (!isValid) R.string.error_traits else null
    }
}

data class FieldState(
    val text: String = "",
    val isFocusedBefore: Boolean = false,
    @StringRes val error: Int? = null
)

data class IntFieldState(
    val value: Int = 0,
    val isFocusedBefore: Boolean = false,
    val maxValue: Int,
    val minValue: Int,
    @StringRes val error: Int? = null
)

data class RaceFormUiState(
    val id: String = "",
    val nameField: FieldState = FieldState(),
    val sizeField: FieldState = FieldState(),
    val speedField: IntFieldState = IntFieldState(maxValue = 100, minValue = 0),
    val abilityBonuses: Map<Ability, Int> = emptyMap(),
    val grantedSkills: List<Skill> = emptyList(),
    val traitsField: FieldState = FieldState(),
    val traits: List<String> = emptyList(),
    val descriptionField: FieldState = FieldState(),
    val isFormValid: Boolean = false
)

fun RaceFormUiState.toRace(): Race {
    return Race(
        id = id.ifBlank { UUID.randomUUID().toString() },
        name = nameField.text,
        size = sizeField.text,
        speed = speedField.value,
        abilityBonuses = abilityBonuses,
        traits = traits,
        grantedSkills = grantedSkills,
        description = descriptionField.text
    )
}