package com.yablonskyi.character.presentation.sheet.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.yablonskyi.character.presentation.common.*
import com.yablonskyi.character.presentation.common.validation.CharacterFormValidators as Validators
import com.yablonskyi.domain.character.CharacterChange
import com.yablonskyi.model.character.Character
import com.yablonskyi.ui.validation.IntFieldState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class HealthField { CURRENT, MAXIMUM, TEMPORARY, AMOUNT }
data class HealthFormUiState(
    val current: IntFieldState = Validators.number(),
    val maximum: IntFieldState = Validators.number(0, 0, 10000),
    val temporary: IntFieldState = Validators.number(0, 0, 10000),
    val amount: IntFieldState = Validators.number(),
    val initialized: Boolean = false,
) {
    val isValid get() = current.isValid && maximum.isValid && temporary.isValid
    fun field(field: HealthField) = when (field) {
        HealthField.CURRENT -> current
        HealthField.MAXIMUM -> maximum
        HealthField.TEMPORARY -> temporary
        HealthField.AMOUNT -> amount
    }
    fun withField(field: HealthField, value: IntFieldState) = when (field) {
        HealthField.CURRENT -> copy(current = value)
        HealthField.MAXIMUM -> copy(maximum = value)
        HealthField.TEMPORARY -> copy(temporary = value)
        HealthField.AMOUNT -> copy(amount = value)
    }
    fun change() = CharacterChange.Health(current.value, maximum.value, temporary.value)
}
sealed interface HealthFormIntent {
    data class BeginSession(val session: String, val character: Character) : HealthFormIntent
    data class Synchronize(val character: Character) : HealthFormIntent
    data class Changed(val field: HealthField, val value: Int) : HealthFormIntent
    data class FocusChanged(val field: HealthField, val focused: Boolean) : HealthFormIntent
    data class WriteFinished(val result: FormWriteResult) : HealthFormIntent
    data object Heal : HealthFormIntent
    data object Damage : HealthFormIntent
    data object FullHeal : HealthFormIntent
    data object MarkDead : HealthFormIntent
    data object EndSession : HealthFormIntent
    data object Dismiss : HealthFormIntent
}
sealed interface HealthFormEffect {
    data class ChangeRequested(val change: CharacterChange.Health, val write: FormWrite) : HealthFormEffect
    data class DismissRequested(val session: String) : HealthFormEffect
}
@HiltViewModel
class HealthFormViewModel @Inject constructor(private val saved: SavedStateHandle) : ViewModel() {
    private val mutable = MutableStateFlow(HealthFormUiState())
    val uiState = mutable.asStateFlow()
    private val channel = Channel<HealthFormEffect>(Channel.UNLIMITED)
    val effects = channel.receiveAsFlow()
    private var session: String? = null
    val currentSession: String? get() = session
    private var revision = 0L
    private var dirty = false

    fun onIntent(intent: HealthFormIntent) {
        if (intent == HealthFormIntent.EndSession) {
            session = null
            saved.keys().toList().forEach { saved.remove<Any>(it) }
            mutable.value = HealthFormUiState()
            return
        }

        val before = mutable.value
        when (intent) {
            is HealthFormIntent.BeginSession -> {
                if (session == intent.session) return
                val restore = saved.get<String>("session") == intent.session
                session = intent.session
                mutable.value = fromCharacter(intent.character)
                if (restore) {
                    HealthField.entries.forEach { field -> saved.get<Int>(field.name)?.let {
                        mutable.value = mutable.value.withField(field, mutable.value.field(field).copy(value = it))
                    } }
                }
                revision = if (restore) saved["revision"] ?: 0L else 0L
                dirty = restore && saved.get<Boolean>("dirty") == true
                updateCurrentBound(false)
                save()
                return
            }
            is HealthFormIntent.Synchronize -> if (!dirty && before.initialized) {
                mutable.value = fromCharacter(intent.character).copy(amount = before.amount)
            }
            is HealthFormIntent.WriteFinished -> {
                val result = intent.result
                if (result.write != FormWrite(session ?: "", revision)) return
                dirty = false
                if (!result.accepted && result.persisted != null) {
                    mutable.value = fromCharacter(result.persisted).copy(amount = before.amount)
                }
            }
            is HealthFormIntent.FocusChanged -> mutable.value = before.withField(intent.field, before.field(intent.field).let {
                if (intent.focused) it.onFocusGained() else it.onFocusLost()
            })
            HealthFormIntent.EndSession -> Unit
            HealthFormIntent.Dismiss -> session?.let { channel.trySend(HealthFormEffect.DismissRequested(it)) }
            else -> {
                if (!before.initialized) return
                when (intent) {
                    is HealthFormIntent.Changed -> {
                        mutable.value = before.withField(intent.field, before.field(intent.field).onValueChanged(intent.value))
                        if (intent.field == HealthField.MAXIMUM) updateCurrentBound(true)
                    }
                    HealthFormIntent.FullHeal -> if (before.maximum.isValid) mutable.value = before.copy(current = before.current.copy(value = before.maximum.value, error = null))
                    HealthFormIntent.MarkDead -> mutable.value = before.copy(current = before.current.copy(value = 0, error = null), temporary = before.temporary.copy(value = 0, error = null))
                    HealthFormIntent.Heal -> if (before.isValid && before.amount.isValid) mutable.value = before.copy(current = before.current.copy(value = (before.current.value.toLong() + before.amount.value).coerceAtMost(before.maximum.value.toLong()).toInt()))
                    HealthFormIntent.Damage -> if (before.isValid && before.amount.isValid) {
                        val remaining = (before.amount.value.toLong() - before.temporary.value).coerceAtLeast(0)
                        mutable.value = before.copy(temporary = before.temporary.copy(value = (before.temporary.value.toLong() - before.amount.value).coerceAtLeast(0).toInt()),
                            current = before.current.copy(value = (before.current.value.toLong() - remaining).coerceAtLeast(0).toInt()))
                    }
                }
                if (mutable.value.change() != before.change()) {
                    dirty = true
                    revision++
                }
                val state = mutable.value
                if (state.isValid && state.change() != before.change()) {
                    channel.trySend(HealthFormEffect.ChangeRequested(state.change(), FormWrite(session!!, revision)))
                }
            }
        }
        save()
    }
    private fun updateCurrentBound(clamp: Boolean) {
        val s = mutable.value
        if (s.maximum.isValid) mutable.value = s.copy(current = Validators.number(
            if (clamp) s.current.value.coerceIn(0, s.maximum.value) else s.current.value, 0, s.maximum.value,
        ).copy(isFocusedBefore = s.current.isFocusedBefore))
    }
    private fun save() {
        saved["session"] = session; saved["revision"] = revision; saved["dirty"] = dirty
        HealthField.entries.forEach { saved[it.name] = mutable.value.field(it).value }
    }
    private fun fromCharacter(c: Character) = HealthFormUiState(Validators.number(c.currentHp, 0, c.maxHp.coerceAtLeast(0)),
        Validators.number(c.maxHp, 0, 10000), Validators.number(c.tempHp, 0, 10000), initialized = true)
}