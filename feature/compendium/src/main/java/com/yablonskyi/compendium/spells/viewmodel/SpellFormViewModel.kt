package com.yablonskyi.compendium.spells.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.compendium.CompendiumSpellUpdateRoute
import com.yablonskyi.domain.repository.SpellRepository
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
import com.yablonskyi.ui.validation.FieldState
import com.yablonskyi.ui.validation.FieldValidator
import com.yablonskyi.ui.validation.IntFieldState
import com.yablonskyi.ui.validation.SpellValidators
import com.yablonskyi.ui.validation.ValidationPattern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpellFormViewModel @Inject constructor(
    private val repository: SpellRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val spellId: Long =
        savedStateHandle.toRoute<CompendiumSpellUpdateRoute>().spellId

    private val _effect = Channel<SpellFormEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _uiState = MutableStateFlow(
        SpellFormUiState(
            name = FieldState(validator = SpellValidators.name)
                .copy(text = savedStateHandle[KEY_NAME] ?: ""),
            material = FieldState(validator = SpellValidators.material)
                .copy(text = savedStateHandle[KEY_MATERIAL] ?: ""),
            damageDice = FieldState(validator = SpellValidators.damageDice)
                .copy(text = savedStateHandle[KEY_DAMAGE_DICE] ?: ""),
            description = FieldState(validator = SpellValidators.description)
                .copy(text = savedStateHandle[KEY_DESCRIPTION] ?: ""),
            higherLevels = FieldState(validator = SpellValidators.higherLevels)
                .copy(text = savedStateHandle[KEY_HIGHER_LEVELS] ?: ""),
        )
    )
    val uiState: StateFlow<SpellFormUiState> = _uiState.asStateFlow()

    private var initialSent = false

    init {
        if (spellId != 0L) {
            viewModelScope.launch {
                repository.getSpellById(spellId).collect { spell ->
                    spell?.let {
                        if (!initialSent) {
                            initialSent = true
                            _uiState.update { current ->
                                current.copy(
                                    id = spell.spellId,
                                    name = current.name.copy(
                                        text = if (savedStateHandle.contains(KEY_NAME)) {
                                            current.name.text
                                        } else {
                                            spell.name
                                        }
                                    ),
                                    school = spell.school,
                                    level = spell.level,
                                    castTime = spell.castTime,
                                    rangeType = spell.rangeType,
                                    rangeValueField = current.rangeValueField.copy(
                                        value = spell.rangeValue ?: 0
                                    ),
                                    components = spell.components.toSet(),
                                    duration = spell.duration,
                                    isRitual = spell.isRitual,
                                    isConcentration = spell.isConcentration,
                                    attackType = spell.attackType,
                                    saveStat = spell.saveStat,
                                    damageType = spell.damageType,
                                    material = current.material.copy(
                                        text = if (savedStateHandle.contains(KEY_MATERIAL)) {
                                            current.material.text
                                        } else {
                                            spell.material.orEmpty()
                                        }
                                    ),
                                    damageDice = current.damageDice.copy(
                                        text = if (savedStateHandle.contains(KEY_DAMAGE_DICE)) {
                                            current.damageDice.text
                                        } else {
                                            spell.damageDice.orEmpty()
                                        }
                                    ),
                                    description = current.description.copy(
                                        text = if (savedStateHandle.contains(KEY_DESCRIPTION)) {
                                            current.description.text
                                        } else {
                                            spell.description
                                        }
                                    ),
                                    higherLevels = current.higherLevels.copy(
                                        text = if (savedStateHandle.contains(KEY_HIGHER_LEVELS)) {
                                            current.higherLevels.text
                                        } else {
                                            spell.higherLevels.orEmpty()
                                        }
                                    ),
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun onIntent(intent: SpellFormIntent) {
        when (intent) {
            is SpellFormIntent.NameChanged -> onNameChanged(intent.value)
            is SpellFormIntent.NameFocusChanged -> onNameFocusChanged(intent.isFocused)
            is SpellFormIntent.MaterialChanged -> onMaterialChanged(intent.value)
            is SpellFormIntent.MaterialFocusChanged -> onMaterialFocusChanged(intent.isFocused)
            is SpellFormIntent.DamageDiceChanged -> onDamageDiceChanged(intent.value)
            is SpellFormIntent.DamageDiceFocusChanged ->
                onDamageDiceFocusChanged(intent.isFocused)

            is SpellFormIntent.DescriptionChanged -> onDescriptionChanged(intent.value)
            is SpellFormIntent.DescriptionFocusChanged ->
                onDescriptionFocusChanged(intent.isFocused)

            is SpellFormIntent.HigherLevelsChanged -> onHigherLevelsChanged(intent.value)
            is SpellFormIntent.HigherLevelsFocusChanged ->
                onHigherLevelsFocusChanged(intent.isFocused)

            is SpellFormIntent.LevelChanged ->
                updateState(_uiState.value.copy(level = intent.value))

            is SpellFormIntent.SchoolChanged ->
                updateState(_uiState.value.copy(school = intent.value))

            is SpellFormIntent.CastTimeChanged ->
                updateState(_uiState.value.copy(castTime = intent.value))

            is SpellFormIntent.RangeTypeChanged -> onRangeTypeChanged(intent.value)
            is SpellFormIntent.RangeValueChanged -> onRangeValueChanged(intent.value)
            is SpellFormIntent.RangeValueFocusChanged ->
                onRangeValueFocusChanged(intent.isFocused)

            is SpellFormIntent.ComponentToggled -> onComponentToggled(intent.component)

            is SpellFormIntent.DurationChanged ->
                updateState(_uiState.value.copy(duration = intent.value))

            SpellFormIntent.RitualToggled ->
                updateState(_uiState.value.copy(isRitual = !_uiState.value.isRitual))

            SpellFormIntent.ConcentrationToggled ->
                updateState(
                    _uiState.value.copy(isConcentration = !_uiState.value.isConcentration)
                )

            is SpellFormIntent.AttackTypeChanged -> onAttackTypeChanged(intent.value)
            is SpellFormIntent.SaveStatChanged ->
                updateState(_uiState.value.copy(saveStat = intent.value))

            is SpellFormIntent.DamageTypeChanged -> onDamageTypeChanged(intent.value)
            SpellFormIntent.Submit -> submit()
            is SpellFormIntent.NavigateBack -> {
                viewModelScope.launch {
                    _effect.send(SpellFormEffect.NavigateBack)
                }
            }
        }
    }

    private fun submit() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                val spell = Spell(
                    spellId = spellId,
                    name = state.name.text.trim(),
                    school = state.school,
                    level = state.level,
                    castTime = state.castTime,
                    rangeType = state.rangeType,
                    rangeValue = if (state.rangeType == SpellRangeType.DISTANCE) {
                        state.rangeValueField.value
                    } else {
                        null
                    },
                    components = state.components.sorted(),
                    material = if (Component.MATERIAL in state.components && state.material.text.isNotBlank()) {
                        state.material.text.trim()
                    } else {
                        null
                    },
                    isRitual = state.isRitual,
                    duration = state.duration,
                    isConcentration = state.isConcentration,
                    attackType = state.attackType,
                    saveStat = if (state.attackType == AttackType.SAVE) state.saveStat else null,
                    damageType = state.damageType,
                    damageDice = state.damageType?.let {
                        state.damageDice.text.trim().ifBlank { null }
                    },
                    description = state.description.text.trim(),
                    higherLevels = state.higherLevels.text.trim().ifBlank { null }
                )
                if (spellId == 0L) repository.insertSpell(spell)
                else repository.updateSpell(spell)
            } finally {
                _effect.send(SpellFormEffect.NavigateBack)
            }
        }
    }

    // ── Range ─────────────────────────────────────────────────────────────────

    private fun onRangeTypeChanged(value: SpellRangeType) {
        val state = _uiState.value
        updateState(
            state.copy(
                rangeType = value,
                rangeValueField = if (value != SpellRangeType.DISTANCE) {
                    state.rangeValueField.copy(error = null)
                } else {
                    state.rangeValueField
                }
            )
        )
    }

    private fun onRangeValueChanged(newValue: Int) {
        updateState(
            _uiState.value.copy(
                rangeValueField = _uiState.value.rangeValueField.onValueChanged(newValue)
            )
        )
    }

    private fun onRangeValueFocusChanged(isFocused: Boolean) {
        _uiState.update { state ->
            val current = state.rangeValueField
            val updated = when {
                !isFocused -> current.onFocusLost()
                isFocused -> current.onFocusGained()
                else -> current
            }
            state.copy(rangeValueField = updated)
        }
    }

    // ── Components ────────────────────────────────────────────────────────────

    private fun onComponentToggled(component: Component) {
        val updated = if (component in _uiState.value.components) {
            _uiState.value.components - component
        } else {
            _uiState.value.components + component
        }
        updateState(_uiState.value.copy(components = updated))
    }

    // ── Combat stats ──────────────────────────────────────────────────────────

    private fun onAttackTypeChanged(value: AttackType) {
        val state = _uiState.value
        updateState(
            state.copy(
                attackType = value,
                saveStat = if (value == AttackType.NONE) null else state.saveStat
            )
        )
    }

    private fun onDamageTypeChanged(value: DamageType?) {
        updateState(_uiState.value.copy(damageType = value))
    }

    // ── Text Fields (SavedStateHandle-backed) ─────────────────────────────────

    private fun onNameChanged(newValue: String) {
        savedStateHandle[KEY_NAME] = newValue
        _uiState.update { it.copy(name = it.name.onTextChanged(newValue)) }
    }

    private fun onNameFocusChanged(isFocused: Boolean) {
        _uiState.update {
            it.copy(name = if (isFocused) it.name.onFocusGained() else it.name.onFocusLost())
        }
    }

    private fun onMaterialChanged(newValue: String) {
        savedStateHandle[KEY_MATERIAL] = newValue
        _uiState.update { it.copy(material = it.material.onTextChanged(newValue)) }
    }

    private fun onMaterialFocusChanged(isFocused: Boolean) {
        _uiState.update {
            it.copy(
                material = if (isFocused) it.material.onFocusGained()
                else it.material.onFocusLost()
            )
        }
    }

    private fun onDamageDiceChanged(newValue: String) {
        if (newValue.isNotEmpty() && !SpellValidators.damageDiceTyping.isValid(newValue)) return
        savedStateHandle[KEY_DAMAGE_DICE] = newValue
        _uiState.update { it.copy(damageDice = it.damageDice.onTextChanged(newValue)) }
    }

    private fun onDamageDiceFocusChanged(isFocused: Boolean) {
        _uiState.update {
            it.copy(
                damageDice = if (isFocused) it.damageDice.onFocusGained()
                else it.damageDice.onFocusLost()
            )
        }
    }

    private fun onDescriptionChanged(newValue: String) {
        savedStateHandle[KEY_DESCRIPTION] = newValue
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

    private fun onHigherLevelsChanged(newValue: String) {
        savedStateHandle[KEY_HIGHER_LEVELS] = newValue
        _uiState.update { it.copy(higherLevels = it.higherLevels.onTextChanged(newValue)) }
    }

    private fun onHigherLevelsFocusChanged(isFocused: Boolean) {
        _uiState.update {
            it.copy(
                higherLevels = if (isFocused) it.higherLevels.onFocusGained()
                else it.higherLevels.onFocusLost()
            )
        }
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private fun updateState(newState: SpellFormUiState) {
        _uiState.update { newState }
    }
}

private const val RANGE_VALUE_MAX = 1000

@Immutable
data class SpellFormUiState(
    val id: Long = 0L,
    val name: FieldState = FieldState(validator = SpellValidators.name),
    val material: FieldState = FieldState(validator = SpellValidators.material),
    val damageDice: FieldState = FieldState(validator = SpellValidators.damageDice),
    val description: FieldState = FieldState(validator = SpellValidators.description),
    val higherLevels: FieldState = FieldState(validator = SpellValidators.higherLevels),
    val level: SpellLevel = SpellLevel.CANTRIP,
    val school: MagicSchool = MagicSchool.EVOCATION,
    val castTime: SpellCastTime = SpellCastTime.ACTION,
    val rangeType: SpellRangeType = SpellRangeType.SELF,
    val rangeValueField: IntFieldState = IntFieldState(
        minValue = 0,
        maxValue = RANGE_VALUE_MAX,
        validator = FieldValidator(
            ValidationPattern.Range(0, RANGE_VALUE_MAX)
        )
    ),
    val components: Set<Component> = emptySet(),
    val duration: SpellDuration = SpellDuration.INSTANTANEOUS,
    val isRitual: Boolean = false,
    val isConcentration: Boolean = false,
    val attackType: AttackType = AttackType.NONE,
    val saveStat: Ability? = null,
    val damageType: DamageType? = null,
    val isLoading: Boolean = false,
) {
    val isFormValid: Boolean
        get() = name.isValid &&
                description.isValid &&
                (Component.MATERIAL !in components || material.isValid) &&
                (damageType == null || damageDice.isValid) &&
                (attackType != AttackType.SAVE || saveStat != null) &&
                (rangeType != SpellRangeType.DISTANCE || rangeValueField.isValid)
}

sealed interface SpellFormIntent {
    data class NameChanged(val value: String) : SpellFormIntent
    data class NameFocusChanged(val isFocused: Boolean) : SpellFormIntent
    data class MaterialChanged(val value: String) : SpellFormIntent
    data class MaterialFocusChanged(val isFocused: Boolean) : SpellFormIntent
    data class DamageDiceChanged(val value: String) : SpellFormIntent
    data class DamageDiceFocusChanged(val isFocused: Boolean) : SpellFormIntent
    data class DescriptionChanged(val value: String) : SpellFormIntent
    data class DescriptionFocusChanged(val isFocused: Boolean) : SpellFormIntent
    data class HigherLevelsChanged(val value: String) : SpellFormIntent
    data class HigherLevelsFocusChanged(val isFocused: Boolean) : SpellFormIntent

    data class LevelChanged(val value: SpellLevel) : SpellFormIntent
    data class SchoolChanged(val value: MagicSchool) : SpellFormIntent

    data class CastTimeChanged(val value: SpellCastTime) : SpellFormIntent
    data class RangeTypeChanged(val value: SpellRangeType) : SpellFormIntent
    data class RangeValueChanged(val value: Int) : SpellFormIntent
    data class RangeValueFocusChanged(val isFocused: Boolean) : SpellFormIntent

    data class ComponentToggled(val component: Component) : SpellFormIntent

    data class DurationChanged(val value: SpellDuration) : SpellFormIntent
    data object RitualToggled : SpellFormIntent
    data object ConcentrationToggled : SpellFormIntent

    data class AttackTypeChanged(val value: AttackType) : SpellFormIntent
    data class SaveStatChanged(val value: Ability?) : SpellFormIntent

    data class DamageTypeChanged(val value: DamageType?) : SpellFormIntent

    data object Submit : SpellFormIntent

    data object NavigateBack : SpellFormIntent
}

sealed interface SpellFormEffect {
    data object NavigateBack : SpellFormEffect
}

private const val KEY_NAME = "spell_name"
private const val KEY_MATERIAL = "spell_material"
private const val KEY_DAMAGE_DICE = "spell_damage_dice"
private const val KEY_DESCRIPTION = "spell_description"
private const val KEY_HIGHER_LEVELS = "spell_higher_levels"