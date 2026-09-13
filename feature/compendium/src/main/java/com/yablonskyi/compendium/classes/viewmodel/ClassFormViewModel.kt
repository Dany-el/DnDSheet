package com.yablonskyi.compendium.classes.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.compendium.CompendiumClassUpdateRoute
import com.yablonskyi.domain.repository.ClassRepository
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Skill
import com.yablonskyi.model.dice.DiceRoles
import com.yablonskyi.model.rulebook.CharacterClass
import com.yablonskyi.ui.validation.ClassValidators
import com.yablonskyi.ui.validation.FieldState
import com.yablonskyi.ui.validation.IntFieldState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ClassFormViewModel @Inject constructor(
    private val repository: ClassRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val classId: String? =
        savedStateHandle.toRoute<CompendiumClassUpdateRoute>().classId

    private val _effect = Channel<ClassFormEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _uiState = MutableStateFlow(ClassFormUiState())
    val uiState: StateFlow<ClassFormUiState> = _uiState.asStateFlow()

    private var initialSent = false

    init {
        if (classId != null) {
            viewModelScope.launch {
                repository.getClassById(classId).collect { cls ->
                    cls?.let {
                        if (!initialSent) {
                            initialSent = true
                            _uiState.update { state ->
                                state.copy(
                                    id = cls.id,
                                    name = state.name.copy(text = cls.name),
                                    hitDice = cls.hitDice,
                                    primaryAbility = cls.primaryAbility,
                                    savingThrows = cls.savingThrows,
                                    skillChoiceCountField = state.skillChoiceCountField.copy(
                                        value = cls.skillChoiceCount
                                    ),
                                    chosenSkills = cls.availableSkills,
                                    spellcastingAbility = cls.spellcastingAbility,
                                    description = state.description.copy(text = cls.description),
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun onIntent(intent: ClassFormIntent) {
        when (intent) {
            is ClassFormIntent.NameChanged -> onNameChanged(intent.value)
            is ClassFormIntent.NameFocusChanged -> onNameFocusChanged(intent.isFocused)
            is ClassFormIntent.HitDiceChanged -> onHitDiceChanged(intent.value)
            is ClassFormIntent.DescriptionChanged -> onDescriptionChanged(intent.value)
            is ClassFormIntent.DescriptionFocusChanged -> onDescriptionFocusChanged(intent.isFocused)
            is ClassFormIntent.PrimaryAbilityChanged -> onPrimaryAbilityChanged(intent.ability)
            is ClassFormIntent.SkillChoiceCountChanged -> onSkillChoiceCountChanged(intent.value)
            is ClassFormIntent.SkillChoiceCountFocusChanged -> onSkillChoiceCountFocusChanged(intent.isFocused)
            is ClassFormIntent.SpellcastingAbilityChanged -> onSpellcastingAbilityChanged(intent.ability)
            is ClassFormIntent.AddSavingThrow -> addSavingThrow(intent.ability)
            is ClassFormIntent.RemoveSavingThrow -> removeSavingThrow(intent.ability)
            is ClassFormIntent.AddSkill -> addAvailableSkill(intent.skill)
            is ClassFormIntent.RemoveSkill -> removeAvailableSkill(intent.skill)
            ClassFormIntent.Submit -> submit()
            is ClassFormIntent.NavigateBack -> {
                viewModelScope.launch {
                    _effect.send(ClassFormEffect.NavigateBack)
                }
            }
        }
    }

    private fun submit() {
        val s = _uiState.value

        val safeSkillCount = s.skillChoiceCountField.value.coerceIn(
            s.skillChoiceCountField.minValue,
            s.effectiveMaxSkillCount
        )

        viewModelScope.launch {
            try {
                val cls = CharacterClass(
                    id = classId ?: UUID.randomUUID().toString(),
                    name = s.name.text.trim(),
                    hitDice = s.hitDice,
                    primaryAbility = s.primaryAbility,
                    savingThrows = s.savingThrows,
                    skillChoiceCount = safeSkillCount,
                    availableSkills = s.chosenSkills,
                    spellcastingAbility = s.spellcastingAbility,
                    description = s.description.text.trim(),
                    isHomebrew = true
                )
                if (classId == null) repository.insert(cls)
                else repository.update(cls)
            } finally {
                _effect.send(ClassFormEffect.NavigateBack)
            }
        }
    }

    // ── Text Fields ───────────────────────────────────────────────────────────

    private fun onNameChanged(newValue: String) {
        _uiState.update { it.copy(name = it.name.onTextChanged(newValue)) }
    }

    private fun onNameFocusChanged(isFocused: Boolean) {
        _uiState.update {
            it.copy(name = if (isFocused) it.name.onFocusGained() else it.name.onFocusLost())
        }
    }

    private fun onHitDiceChanged(newValue: String) {
        _uiState.update { it.copy(hitDice = newValue) }
    }

    private fun onDescriptionChanged(newValue: String) {
        _uiState.update { it.copy(description = it.description.onTextChanged(newValue)) }
    }

    private fun onDescriptionFocusChanged(isFocused: Boolean) {
        _uiState.update {
            it.copy(
                description = if (isFocused) it.description.onFocusGained()
                else it.description.onFocusLost()
            )
        }
    }

    // ── Primary Ability ───────────────────────────────────────────────────────

    private fun onPrimaryAbilityChanged(ability: Ability) {
        updateState(_uiState.value.copy(primaryAbility = ability))
    }

    // ── Skill Choice Count ────────────────────────────────────────────────────

    private fun onSkillChoiceCountChanged(newValue: Int) {
        updateState(
            _uiState.value.copy(
                skillChoiceCountField = _uiState.value.skillChoiceCountField.onValueChanged(newValue)
            )
        )
    }

    private fun onSkillChoiceCountFocusChanged(isFocused: Boolean) {
        _uiState.update { state ->
            val current = state.skillChoiceCountField
            val updated = when {
                !isFocused -> current.onFocusLost()
                isFocused -> current.onFocusGained()
                else -> current
            }
            state.copy(skillChoiceCountField = updated)
        }
    }

    // ── Spellcasting Ability ──────────────────────────────────────────────────

    private fun onSpellcastingAbilityChanged(ability: Ability?) {
        updateState(_uiState.value.copy(spellcastingAbility = ability))
    }

    // ── Saving Throws ─────────────────────────────────────────────────────────

    private fun addSavingThrow(ability: Ability) {
        updateState(_uiState.value.copy(savingThrows = _uiState.value.savingThrows + ability))
    }

    private fun removeSavingThrow(ability: Ability) {
        updateState(_uiState.value.copy(savingThrows = _uiState.value.savingThrows - ability))
    }

    // ── Available Skills ──────────────────────────────────────────────────────

    private fun addAvailableSkill(skill: Skill) {
        val newSkills = _uiState.value.chosenSkills + skill
        updateState(syncSkillChoiceCount(_uiState.value.copy(chosenSkills = newSkills)))
    }

    private fun removeAvailableSkill(skill: Skill) {
        val newSkills = _uiState.value.chosenSkills - skill
        updateState(syncSkillChoiceCount(_uiState.value.copy(chosenSkills = newSkills)))
    }

    private fun syncSkillChoiceCount(state: ClassFormUiState): ClassFormUiState {
        val newMax = state.effectiveMaxSkillCount
        val clampedValue = state.skillChoiceCountField.value.coerceAtMost(newMax)
        return state.copy(
            skillChoiceCountField = state.skillChoiceCountField.copy(
                value = clampedValue,
                maxValue = newMax,
                validator = ClassValidators.skillChoiceCount(max = newMax)
            )
        )
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private fun updateState(newState: ClassFormUiState) {
        _uiState.update { newState }
    }
}

@Immutable
data class ClassFormUiState(
    val id: String = "",
    val name: FieldState = FieldState(validator = ClassValidators.name),
    val primaryAbility: Ability = Ability.STR,
    val savingThrows: Set<Ability> = emptySet(),
    val skillChoiceCountField: IntFieldState = IntFieldState(
        value = 2,
        maxValue = Skill.entries.size,
        minValue = 0,
        validator = ClassValidators.skillChoiceCount()
    ),
    val chosenSkills: List<Skill> = emptyList(),
    val spellcastingAbility: Ability? = null,
    val hitDice: String = DiceRoles.hitDices.first(),
    val description: FieldState = FieldState(validator = ClassValidators.description),
    val isLoading: Boolean = false,
) {
    val isFormValid: Boolean
        get() = name.isValid && description.isValid && skillChoiceCountField.isValid &&
                skillChoiceCountField.value <= effectiveMaxSkillCount

    val availableSavingThrows: List<Ability>
        get() = Ability.playableAbilities.filterNot { it in savingThrows }

    val availableSkills: List<Skill>
        get() = Skill.entries.filterNot { it in chosenSkills }

    val effectiveMaxSkillCount: Int
        get() = if (chosenSkills.isEmpty()) Skill.entries.size
        else chosenSkills.size
}

sealed interface ClassFormIntent {
    data class NameChanged(val value: String) : ClassFormIntent
    data class NameFocusChanged(val isFocused: Boolean) : ClassFormIntent
    data class HitDiceChanged(val value: String) : ClassFormIntent
    data class DescriptionChanged(val value: String) : ClassFormIntent
    data class DescriptionFocusChanged(val isFocused: Boolean) : ClassFormIntent
    data class PrimaryAbilityChanged(val ability: Ability) : ClassFormIntent
    data class SkillChoiceCountChanged(val value: Int) : ClassFormIntent
    data class SkillChoiceCountFocusChanged(val isFocused: Boolean) : ClassFormIntent
    data class SpellcastingAbilityChanged(val ability: Ability?) : ClassFormIntent
    data class AddSavingThrow(val ability: Ability) : ClassFormIntent
    data class RemoveSavingThrow(val ability: Ability) : ClassFormIntent
    data class AddSkill(val skill: Skill) : ClassFormIntent
    data class RemoveSkill(val skill: Skill) : ClassFormIntent
    data object Submit : ClassFormIntent
    data object NavigateBack : ClassFormIntent
}

sealed interface ClassFormEffect {
    data object NavigateBack : ClassFormEffect
}