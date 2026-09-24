package com.yablonskyi.character.presentation.sheet.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.yablonskyi.character.presentation.common.validation.CharacterFormValidators as Validators
import com.yablonskyi.model.character.*
import com.yablonskyi.model.dice.DiceRoles
import com.yablonskyi.ui.R
import com.yablonskyi.ui.validation.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import javax.inject.Inject

enum class AttackTextField { NAME, NOTES }
enum class AttackNumberField { RANGE, DICE_COUNT, FIXED_DAMAGE, HIT_BONUS, DAMAGE_BONUS }

data class AttackFormUiState(
    val original: Attack = Attack(),
    val name: FieldState = FieldState(validator = Validators.attackName),
    val notes: FieldState = FieldState(),
    val range: IntFieldState = Validators.number(5, 1, 1000),
    val diceCount: IntFieldState = Validators.number(1, 1, 100),
    val die: DiceRoles = DiceRoles.D4,
    val fixedDamage: IntFieldState = Validators.number(),
    val hitBonus: IntFieldState = Validators.number(0, -100, 100),
    val damageBonus: IntFieldState = Validators.number(0, -100, 100),
    val damageMode: DamageMode = DamageMode.DICE,
    val damageAbilityModifier: DamageAbilityModifier = DamageAbilityModifier.FULL,
    val usages: Set<AttackUsage> = setOf(AttackUsage.ACTION),
    val attackType: AttackType = AttackType.NONE,
    val damageType: DamageType = DamageType.SLASHING,
    val ability: Ability = Ability.NONE,
    val proficient: Boolean = false,
    val rangeEdited: Boolean = false,
    val diceParseError: Boolean = false,
    val submitted: Boolean = false,
    val initialized: Boolean = false,
) {
    val isValid get() = name.isValid && range.isValid && hitBonus.isValid && damageBonus.isValid &&
        usages.isNotEmpty() && if (damageMode == DamageMode.DICE) diceCount.isValid && !diceParseError else fixedDamage.isValid
    fun number(field: AttackNumberField) = when (field) {
        AttackNumberField.RANGE -> range
        AttackNumberField.DICE_COUNT -> diceCount
        AttackNumberField.FIXED_DAMAGE -> fixedDamage
        AttackNumberField.HIT_BONUS -> hitBonus
        AttackNumberField.DAMAGE_BONUS -> damageBonus
    }
    fun withNumber(field: AttackNumberField, value: IntFieldState) = when (field) {
        AttackNumberField.RANGE -> copy(range = value)
        AttackNumberField.DICE_COUNT -> copy(diceCount = value)
        AttackNumberField.FIXED_DAMAGE -> copy(fixedDamage = value)
        AttackNumberField.HIT_BONUS -> copy(hitBonus = value)
        AttackNumberField.DAMAGE_BONUS -> copy(damageBonus = value)
    }
    fun toAttack() = original.copy(
        name = name.text.trim(), notes = notes.text.trim(),
        range = if (rangeEdited) range.value.toString() else original.range,
        damageDice = if (diceParseError) original.damageDice else "${diceCount.value}${die.name.lowercase()}",
        fixedDamage = fixedDamage.value, damageMode = damageMode, usages = usages,
        damageAbilityModifier = damageAbilityModifier, bonusToHit = hitBonus.value,
        bonusToDamage = damageBonus.value, ability = ability, attackType = attackType,
        damageType = damageType, isProficient = proficient,
    )
}

sealed interface AttackFormIntent {
    data class BeginSession(val session: String, val attack: Attack) : AttackFormIntent
    data class TextChanged(val field: AttackTextField, val value: String) : AttackFormIntent
    data class TextFocusChanged(val field: AttackTextField, val focused: Boolean) : AttackFormIntent
    data class NumberChanged(val field: AttackNumberField, val value: Int) : AttackFormIntent
    data class NumberFocusChanged(val field: AttackNumberField, val focused: Boolean) : AttackFormIntent
    data class DieChanged(val value: DiceRoles) : AttackFormIntent
    data class ModeChanged(val value: DamageMode) : AttackFormIntent
    data class ModifierChanged(val value: DamageAbilityModifier) : AttackFormIntent
    data class UsageChanged(val value: AttackUsage) : AttackFormIntent
    data class TypeChanged(val value: AttackType) : AttackFormIntent
    data class DamageTypeChanged(val value: DamageType) : AttackFormIntent
    data class AbilityChanged(val value: Ability) : AttackFormIntent
    data class ProficiencyChanged(val value: Boolean) : AttackFormIntent
    data object Submit : AttackFormIntent
    data object Delete : AttackFormIntent
    data object EndSession : AttackFormIntent
    data object Dismiss : AttackFormIntent
}

sealed interface AttackFormEffect {
    data class SaveRequested(val attack: Attack, val session: String) : AttackFormEffect
    data class DeleteRequested(val attack: Attack, val session: String) : AttackFormEffect
    data class DismissRequested(val session: String) : AttackFormEffect
}

@HiltViewModel
class AttackFormViewModel @Inject constructor(private val savedState: SavedStateHandle) : ViewModel() {
    private val mutableState = MutableStateFlow(AttackFormUiState())
    val uiState = mutableState.asStateFlow()
    private val channel = Channel<AttackFormEffect>(Channel.UNLIMITED)
    val effects = channel.receiveAsFlow()
    private var session: String? = null
    val currentSession: String? get() = session

    fun onIntent(intent: AttackFormIntent) {
        if (intent == AttackFormIntent.EndSession) {
            session = null
            savedState.keys().toList().forEach { savedState.remove<Any>(it) }
            mutableState.value = AttackFormUiState()
            return
        }

        if (intent is AttackFormIntent.BeginSession) {
            if (session == intent.session) return
            val restored = if (savedState.get<String>("session") == intent.session)
                savedState.get<String>("draft")?.let { Json.decodeFromString<Attack>(it) } else null
            session = intent.session
            savedState["session"] = intent.session
            mutableState.value = fromAttack(restored ?: intent.attack)
            saveDraft()
            return
        }
        val state = mutableState.value
        if (!state.initialized || state.submitted) return
        mutableState.value = when (intent) {
            is AttackFormIntent.TextChanged -> if (intent.field == AttackTextField.NAME)
                state.copy(name = state.name.onTextChanged(intent.value)) else state.copy(notes = state.notes.onTextChanged(intent.value))
            is AttackFormIntent.TextFocusChanged -> {
                val field = if (intent.field == AttackTextField.NAME) state.name else state.notes
                val updated = if (intent.focused) field.onFocusGained() else field.onFocusLost()
                if (intent.field == AttackTextField.NAME) state.copy(name = updated) else state.copy(notes = updated)
            }
            is AttackFormIntent.NumberChanged -> state.withNumber(intent.field, state.number(intent.field).onValueChanged(intent.value)).let {
                it.copy(rangeEdited = it.rangeEdited || intent.field == AttackNumberField.RANGE,
                    diceParseError = it.diceParseError && intent.field != AttackNumberField.DICE_COUNT)
            }
            is AttackFormIntent.NumberFocusChanged -> state.withNumber(intent.field, state.number(intent.field).let {
                if (intent.focused) it.onFocusGained() else it.onFocusLost()
            })
            is AttackFormIntent.DieChanged -> state.copy(die = intent.value, diceParseError = false)
            is AttackFormIntent.ModeChanged -> state.copy(damageMode = intent.value)
            is AttackFormIntent.ModifierChanged -> state.copy(damageAbilityModifier = intent.value)
            is AttackFormIntent.UsageChanged -> state.copy(usages = if (intent.value in state.usages) state.usages - intent.value else state.usages + intent.value)
            is AttackFormIntent.TypeChanged -> state.copy(attackType = intent.value)
            is AttackFormIntent.DamageTypeChanged -> state.copy(damageType = intent.value)
            is AttackFormIntent.AbilityChanged -> state.copy(ability = intent.value)
            is AttackFormIntent.ProficiencyChanged -> state.copy(proficient = intent.value)
            AttackFormIntent.Submit -> {
                if (state.isValid) {
                    channel.trySend(AttackFormEffect.SaveRequested(state.toAttack(), session!!))
                    state.copy(submitted = true)
                } else state.copy(name = state.name.validate(), range = state.range.validate(), diceCount = state.diceCount.validate(),
                    hitBonus = state.hitBonus.validate(), damageBonus = state.damageBonus.validate(), fixedDamage = state.fixedDamage.validate())
            }
            AttackFormIntent.Delete -> {
                if (state.original.attackId != 0L) channel.trySend(AttackFormEffect.DeleteRequested(state.original, session!!))
                state.copy(submitted = true)
            }
            AttackFormIntent.Dismiss -> { channel.trySend(AttackFormEffect.DismissRequested(session!!)); state.copy(submitted = true) }
            AttackFormIntent.EndSession, is AttackFormIntent.BeginSession -> state
        }
        saveDraft()
    }

    private fun saveDraft() { savedState["draft"] = Json.encodeToString(uiState.value.toAttack().copy(name = uiState.value.name.text, notes = uiState.value.notes.text)) }

    private fun fromAttack(attack: Attack): AttackFormUiState {
        val expression = if (attack.attackId == 0L && attack.damageDice.isEmpty()) "1d4" else attack.damageDice
        val parsed = Regex("^([1-9]\\d*)?[dDкК](4|6|8|10|12|20|100)$").matchEntire(expression)
        val count = parsed?.groupValues?.get(1)?.let { if (it.isEmpty()) 1 else it.toIntOrNull() }
        val die = DiceRoles.entries.firstOrNull { it.name.equals("D${parsed?.groupValues?.get(2)}", true) }
        return AttackFormUiState(
            original = attack, name = FieldState(text = attack.name, validator = Validators.attackName), notes = FieldState(text = attack.notes),
            range = Validators.number(Regex("\\d+").find(attack.range)?.value?.toIntOrNull() ?: 0, 1, 1000).let { if (it.isValid) it else it.validate() },
            diceCount = Validators.number(count ?: 0, 1, 100).let { if (it.isValid) it else it.validate() }, die = die ?: DiceRoles.D4,
            diceParseError = count == null || die == null, fixedDamage = Validators.number(attack.fixedDamage),
            hitBonus = Validators.number(attack.bonusToHit, -100, 100), damageBonus = Validators.number(attack.bonusToDamage, -100, 100),
            damageMode = attack.damageMode, damageAbilityModifier = attack.damageAbilityModifier, usages = attack.usages,
            attackType = attack.attackType, damageType = attack.damageType, ability = attack.ability, proficient = attack.isProficient, initialized = true,
        )
    }
}
