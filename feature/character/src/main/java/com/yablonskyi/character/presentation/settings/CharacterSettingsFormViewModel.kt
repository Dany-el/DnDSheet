package com.yablonskyi.character.presentation.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.yablonskyi.character.presentation.common.*
import com.yablonskyi.character.presentation.common.validation.CharacterFormValidators as Validators
import com.yablonskyi.domain.character.*
import com.yablonskyi.model.character.*
import com.yablonskyi.model.character.Character
import com.yablonskyi.ui.validation.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class CharacterSettingsFormUiState(
    val character: Character? = null,
    val texts: Map<CharacterTextField, FieldState> = emptyMap(),
    val numbers: Map<CharacterNumberField, IntFieldState> = emptyMap(),
    val slots: Map<SpellLevel, IntFieldState> = emptyMap(),
) {
    companion object {
        fun from(c: Character) = CharacterSettingsFormUiState(c,
            mapOf(CharacterTextField.NAME to c.name, CharacterTextField.RACE to c.race,
                CharacterTextField.CHAR_CLASS to c.charClass, CharacterTextField.SUB_CLASS to c.subClass).mapValues { FieldState(text = it.value) },
            mapOf(
                CharacterNumberField.LEVEL to Validators.number(c.level, 1, 20),
                CharacterNumberField.ARMOR_CLASS to Validators.number(c.armorClass, 0, 99),
                CharacterNumberField.SHIELD to Validators.number(c.shield, 0, 99),
                CharacterNumberField.SPEED to Validators.number(c.speed, 0, 99),
                CharacterNumberField.INITIATIVE_MISC_BONUS to Validators.number(c.initiativeMiscBonus, -100, 100),
                CharacterNumberField.DC_MISC_BONUS to Validators.number(c.spellSettings.dcMiscBonus, -100, 100),
                CharacterNumberField.ATTACK_MISC_BONUS to Validators.number(c.spellSettings.attackMiscBonus, -100, 100),
            ), SpellLevel.entries.drop(1).associateWith { Validators.number(c.spellSettings.spellSlots[it]?.max ?: 0, 0, 9) },
        )
    }
}
sealed interface CharacterSettingsFormIntent {
    data class Synchronize(val character: Character) : CharacterSettingsFormIntent
    data class Change(val change: CharacterChange) : CharacterSettingsFormIntent
    data class TextFocus(val field: CharacterTextField, val focused: Boolean) : CharacterSettingsFormIntent
    data class NumberFocus(val field: CharacterNumberField, val focused: Boolean) : CharacterSettingsFormIntent
    data class SlotFocus(val level: SpellLevel, val focused: Boolean) : CharacterSettingsFormIntent
    data class WriteFinished(val result: FormWriteResult) : CharacterSettingsFormIntent
    data object IncreaseLevel : CharacterSettingsFormIntent
    data object DecreaseLevel : CharacterSettingsFormIntent
}
sealed interface CharacterSettingsFormEffect {
    data class ChangeRequested(val change: CharacterChange, val write: FormWrite) : CharacterSettingsFormEffect
}
@HiltViewModel
class CharacterSettingsFormViewModel @Inject constructor(private val saved: SavedStateHandle) : ViewModel() {
    private val mutable = MutableStateFlow(CharacterSettingsFormUiState())
    val uiState = mutable.asStateFlow()
    private val channel = Channel<CharacterSettingsFormEffect>(Channel.UNLIMITED)
    val effects = channel.receiveAsFlow()
    private val dirty = mutableMapOf<String, Long>()
    private var revision = saved.get<Long>("revision") ?: 0L
    private var initialized = false

    fun onIntent(intent: CharacterSettingsFormIntent) {
        when (intent) {
            is CharacterSettingsFormIntent.Synchronize -> synchronize(intent.character)
            is CharacterSettingsFormIntent.Change -> change(intent.change)
            is CharacterSettingsFormIntent.TextFocus -> mutable.update { s -> s.copy(texts = s.texts + (intent.field to s.texts.getValue(intent.field).let {
                if (intent.focused) it.onFocusGained() else it.onFocusLost()
            })) }
            is CharacterSettingsFormIntent.NumberFocus -> mutable.update { s -> s.copy(numbers = s.numbers + (intent.field to s.numbers.getValue(intent.field).let {
                if (intent.focused) it.onFocusGained() else it.onFocusLost()
            })) }
            is CharacterSettingsFormIntent.SlotFocus -> mutable.update { s -> s.copy(slots = s.slots + (intent.level to s.slots.getValue(intent.level).let {
                if (intent.focused) it.onFocusGained() else it.onFocusLost()
            })) }
            CharacterSettingsFormIntent.IncreaseLevel -> step(1)
            CharacterSettingsFormIntent.DecreaseLevel -> step(-1)
            is CharacterSettingsFormIntent.WriteFinished -> {
                val r = intent.result
                val key = key(r.change)
                if (r.write.session != session() || dirty[key] != r.write.revision) return
                dirty.remove(key)
                r.persisted?.let(::synchronize)
            }
        }
        save()
    }
    private fun session() = "settings:${mutable.value.character?.id}"
    private fun step(delta: Int) {
        val level = mutable.value.numbers[CharacterNumberField.LEVEL] ?: return
        if (level.isValid) change(CharacterChange.Number(CharacterNumberField.LEVEL, (level.value + delta).coerceIn(1, 20)))
    }
    private fun change(change: CharacterChange) {
        val s = mutable.value
        val c = s.character ?: return
        val key = key(change)
        var valid = true
        val updated = when (change) {
            is CharacterChange.Text -> s.copy(texts = s.texts + (change.field to s.texts.getValue(change.field).onTextChanged(change.value)))
            is CharacterChange.Number -> s.copy(numbers = s.numbers + (change.field to s.numbers.getValue(change.field).onValueChanged(change.value).also { valid = it.isValid }))
            is CharacterChange.SlotMaximum -> s.copy(slots = s.slots + (change.level to s.slots.getValue(change.level).onValueChanged(change.maximum).also { valid = it.isValid }))
            is CharacterChange.JackOfAllTrades, is CharacterChange.CastingAbility -> s
            else -> return
        }.copy(character = applyCharacterChange(c, change))
        if (updated == s) return
        mutable.value = updated
        dirty[key] = ++revision
        if (valid) {
            channel.trySend(CharacterSettingsFormEffect.ChangeRequested(change, FormWrite(session(), revision)))
        }
    }
    private fun synchronize(c: Character) {
        if (!initialized || mutable.value.character?.id != c.id) {
            dirty.clear()
            mutable.value = CharacterSettingsFormUiState.from(c)
            if (saved.get<Long>("characterId") == c.id) {
                saved.get<ArrayList<String>>("dirty")?.forEach { dirty[it] = saved["revision:$it"] ?: 0L }
                val initial = mutable.value
                mutable.value = initial.copy(
                    texts = initial.texts.mapValues { (field, value) -> value.copy(text = saved.get<String>("text:$field") ?: value.text) },
                    numbers = initial.numbers.mapValues { (field, value) -> value.copy(value = saved.get<Int>("number:$field") ?: value.value) },
                    slots = initial.slots.mapValues { (level, value) -> value.copy(value = saved.get<Int>("slot:$level") ?: value.value) },
                    character = c.copy(hasJackOfAllTrades = saved.get<Boolean>("jack") ?: c.hasJackOfAllTrades,
                        spellSettings = c.spellSettings.copy(spellCastingAbility = if (saved.contains("casting"))
                            saved.get<String>("casting")?.let { Ability.valueOf(it) } else c.spellSettings.spellCastingAbility)),
                )
            }
            initialized = true
        }
        val old = mutable.value
        var merged = c
        val draft = old.character ?: c
        old.texts.forEach { (field, value) -> if ("text:$field" in dirty) merged = applyCharacterChange(merged, CharacterChange.Text(field, value.text)) }
        old.numbers.forEach { (field, value) -> if ("number:$field" in dirty) merged = applyCharacterChange(merged, CharacterChange.Number(field, value.value)) }
        old.slots.forEach { (level, value) -> if ("slot:$level" in dirty) merged = applyCharacterChange(merged, CharacterChange.SlotMaximum(level, value.value)) }
        if ("jack" in dirty) merged = merged.copy(hasJackOfAllTrades = draft.hasJackOfAllTrades)
        if ("casting" in dirty) merged = merged.copy(spellSettings = merged.spellSettings.copy(spellCastingAbility = draft.spellSettings.spellCastingAbility))
        val fresh = CharacterSettingsFormUiState.from(merged)
        mutable.value = fresh.copy(
            texts = fresh.texts.mapValues { (key, value) -> old.texts[key]?.takeIf { it.text == value.text } ?: value },
            numbers = fresh.numbers.mapValues { (key, value) -> old.numbers[key]?.takeIf { it.value == value.value } ?: value },
            slots = fresh.slots.mapValues { (key, value) -> old.slots[key]?.takeIf { "slot:$key" in dirty || it.value == value.value } ?: value },
        )
    }
    private fun save() {
        mutable.value.character?.let {
            saved["characterId"] = it.id
            saved["jack"] = it.hasJackOfAllTrades
            saved["casting"] = it.spellSettings.spellCastingAbility?.name
        }
        mutable.value.texts.forEach { (field, value) -> saved["text:$field"] = value.text }
        mutable.value.numbers.forEach { (field, value) -> saved["number:$field"] = value.value }
        saved["revision"] = revision; saved["dirty"] = ArrayList(dirty.keys)
        mutable.value.slots.forEach { (level, field) -> saved["slot:$level"] = field.value }
        dirty.forEach { (key, value) -> saved["revision:$key"] = value }
    }
    private fun key(change: CharacterChange): String = when (change) {
        is CharacterChange.Text -> "text:${change.field}"
        is CharacterChange.Number -> "number:${change.field}"
        is CharacterChange.SlotMaximum -> "slot:${change.level}"
        is CharacterChange.JackOfAllTrades -> "jack"
        is CharacterChange.CastingAbility -> "casting"
        else -> error("Unsupported settings change")
    }
}
