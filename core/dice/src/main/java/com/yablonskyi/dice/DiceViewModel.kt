package com.yablonskyi.dice

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.dice.DiceRollLabel.TypeString
import com.yablonskyi.dice.DiceRollLabel.TypeStringRes
import com.yablonskyi.domain.repository.DiceRollRepository
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Attack
import com.yablonskyi.model.character.Skill
import com.yablonskyi.model.character.Spell
import com.yablonskyi.model.dice.DiceGroup
import com.yablonskyi.model.dice.SavedDiceRoll
import com.yablonskyi.ui.utils.formatModifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DiceViewModel @Inject constructor(
    private val repository: DiceRollRepository,
    private val clock: DiceRollClock,
    private val labelResolver: DiceRollLabelResolver,
) : ViewModel() {
    private val _diceRollState = MutableStateFlow(DiceRollState())
    val diceRollState = _diceRollState.asStateFlow()
    private val _persistenceState = MutableStateFlow(DicePersistenceState())
    val persistenceState = _persistenceState.asStateFlow()

    private val selectedCharacterId = MutableStateFlow<Long?>(null)
    private val writeCommands = Channel<WriteCommand>(Channel.UNLIMITED)
    private var failedCommand: WriteCommand? = null
    private var hideResultJob: Job? = null

    init {
        viewModelScope.launch {
            selectedCharacterId.flatMapLatest(::observeRolls).collect { rolls ->
                _persistenceState.update { it.copy(savedRolls = rolls) }
            }
        }
        viewModelScope.launch {
            for (command in writeCommands) process(command)
        }
    }

    fun observeHistory(characterId: Long) {
        if (characterId <= 0 || selectedCharacterId.value == characterId) return
        _persistenceState.update { it.copy(savedRolls = emptyList(), observationError = null) }
        selectedCharacterId.value = characterId
    }

    fun onIntent(characterId: Long, intent: DiceIntent) {
        when (intent) {
            is DiceIntent.SkillCheckRoll -> performRoll(
                characterId, defaultDice, intent.modifier,
                TypeStringRes(RollType.CHECK.type, intent.skill.nameRes)
            )

            is DiceIntent.SaveThrowRoll -> performRoll(
                characterId, defaultDice, intent.modifier,
                TypeStringRes(RollType.SAVE_THROW.type, intent.ability.nameRes, true)
            )

            is DiceIntent.AbilityCheckRoll -> performRoll(
                characterId, defaultDice, intent.modifier,
                TypeStringRes(RollType.CHECK.type, intent.ability.nameRes, true)
            )

            is DiceIntent.WeaponAttackRoll -> performRoll(
                characterId, defaultDice, intent.modifier,
                TypeString(RollType.ATTACK.type, intent.attack.name)
            )

            is DiceIntent.WeaponDamageRoll -> rollDiceFromString(
                characterId, intent.diceWithMod,
                TypeString(RollType.DAMAGE.type, intent.attack.name)
            )

            is DiceIntent.SpellAttackRoll -> performRoll(
                characterId, defaultDice, intent.modifier,
                TypeString(RollType.ATTACK.type)
            )

            is DiceIntent.SpellDamageRoll -> rollDiceFromString(
                characterId, intent.diceWithMod,
                TypeString(RollType.DAMAGE.type, intent.spell.name)
            )

            is DiceIntent.RegularRoll -> performRoll(
                characterId, intent.diceMap, intent.modifier,
                TypeString(RollType.ROLL.type)
            )

            is DiceIntent.RegularStringRoll -> rollDiceFromString(
                characterId, intent.notation,
                TypeString(RollType.ROLL.type)
            )

            is DiceIntent.InitiativeRoll -> performRoll(characterId, defaultDice, intent.modifier,
                labelRes = TypeString(RollType.INITIATIVE.type))
            DiceIntent.ClearDiceRolls -> clearDiceRolls(characterId)
            DiceIntent.RetryPersistence -> retryPersistence()
            DiceIntent.DismissPersistenceError -> dismissPersistenceError()
            DiceIntent.PinResult -> pinResult()
            DiceIntent.DismissResult -> dismissResult()
        }
    }

    private fun performRoll(
        characterId: Long,
        diceMap: Map<Int, Int>,
        modifier: Int? = null,
        labelRes: DiceRollLabel? = null,
    ) {
        if (characterId <= 0 || diceMap.isEmpty() ||
            diceMap.any { (sides, count) -> sides !in 1..MAX_SIDES || count !in 1..MAX_DICE_COUNT } ||
            diceMap.values.sum() > MAX_DICE_COUNT
        ) return

        hideResultJob?.cancel()
        val numbers = mutableListOf<Int>()
        val stringDices = mutableListOf<String>()
        val dices = diceMap.map { (sides, count) -> DiceGroup(sides, count) }
        var hasRegularDice = false
        var total = 0
        dices.forEach { (sides, count) ->
            if (sides == 20) hasRegularDice = true
            stringDices += "${count}d${sides}${modifier?.let(::formatModifier) ?: ""}"
            repeat(count) {
                val roll = (1..sides).random()
                numbers += roll
                total += roll
            }
        }
        total += modifier ?: 0

        val completedState = DiceRollState(
            numbers = numbers,
            stringDices = stringDices,
            modifier = modifier,
            result = total,
            hasRegularDice = hasRegularDice,
            showResult = true,
            labelRes = labelRes,
        )
        _diceRollState.value = completedState
        addDiceRoll(characterId, completedState, dices, clock.currentTimeMillis())
        hideResultJob = viewModelScope.launch {
            delay(5.seconds)
            _diceRollState.update { it.copy(showResult = false) }
        }
    }

    private fun addDiceRoll(
        characterId: Long,
        state: DiceRollState,
        dices: List<DiceGroup>,
        timestamp: Long,
    ) {
        enqueue(
            WriteCommand.Add(
                SavedDiceRoll(
                    characterId = characterId,
                    label = labelResolver.resolve(state.labelRes),
                    numbers = state.numbers.toList(),
                    modifier = state.modifier,
                    result = state.result,
                    dices = dices.toList(),
                    timestamp = timestamp,
                )
            )
        )
    }

    private fun clearDiceRolls(characterId: Long) {
        if (characterId <= 0) return
        if (failedCommand?.characterId == characterId) failedCommand = null
        enqueue(WriteCommand.Clear(characterId))
    }

    private fun enqueue(command: WriteCommand) {
        _persistenceState.update { it.copy(pendingWrites = it.pendingWrites + 1) }
        writeCommands.trySend(command).getOrThrow()
    }

    private suspend fun process(command: WriteCommand) {
        val result = when (command) {
            is WriteCommand.Add -> repository.addDiceRoll(command.roll).map { Unit }
            is WriteCommand.Clear -> repository.clearDiceRolls(command.characterId)
        }
        result.fold(
            onSuccess = {
                if (failedCommand == command ||
                    command is WriteCommand.Clear && failedCommand?.characterId == command.characterId
                ) failedCommand = null
                _persistenceState.update {
                    it.copy(
                        pendingWrites = (it.pendingWrites - 1).coerceAtLeast(0),
                        writeError = null
                    )
                }
            },
            onFailure = { error ->
                failedCommand = command
                _persistenceState.update {
                    it.copy(
                        pendingWrites = (it.pendingWrites - 1).coerceAtLeast(0),
                        writeError = error
                    )
                }
            },
        )
    }

    private fun retryPersistence() {
        val command = failedCommand ?: return
        failedCommand = null
        _persistenceState.update { it.copy(writeError = null) }
        enqueue(command)
    }

    private fun dismissPersistenceError() {
        failedCommand = null
        _persistenceState.update { it.copy(writeError = null, observationError = null) }
    }

    private fun observeRolls(characterId: Long?): Flow<List<SavedDiceRoll>> {
        if (characterId == null || characterId <= 0) return flowOf(emptyList())
        return repository.observeDiceRolls(characterId)
            .onEach { _persistenceState.update { state -> state.copy(observationError = null) } }
            .catch { error ->
                if (error is CancellationException) throw error
                _persistenceState.update { it.copy(observationError = error) }
                emit(emptyList())
            }
    }

    private fun rollDiceFromString(
        characterId: Long,
        value: String,
        labelRes: DiceRollLabel? = null
    ) {
        val match = DICE_NOTATION.matchEntire(value.trim()) ?: return
        val (countStr, sidesStr, modStr) = match.destructured
        val count = countStr.toIntOrNull() ?: return
        val sides = sidesStr.toIntOrNull() ?: return
        val modifier = modStr.replace(" ", "").toIntOrNull()
        performRoll(characterId, mapOf(sides to count), modifier, labelRes)
    }

    private fun pinResult() {
        val current = _diceRollState.value
        if (current.isPinned) {
            _diceRollState.value = current.copy(isPinned = false, showResult = false)
        } else {
            hideResultJob?.cancel()
            _diceRollState.value = current.copy(isPinned = true)
        }
    }

    private fun dismissResult() {
        hideResultJob?.cancel()
        _diceRollState.update { it.copy(showResult = false) }
    }

    private sealed interface WriteCommand {
        val characterId: Long

        data class Add(val roll: SavedDiceRoll) : WriteCommand {
            override val characterId = roll.characterId
        }

        data class Clear(override val characterId: Long) : WriteCommand
    }

    companion object {
        private const val MAX_DICE_COUNT = 100
        private const val MAX_SIDES = 10_000
        private val DICE_NOTATION = """^(\d+)\s*[dDКкKk]\s*(\d+)\s*([+-]?\s*\d+)?$""".toRegex()
        private val defaultDice = mapOf(20 to 1)

        class DiceOptions(val sides: Int, val iconRes: Int)

        val diceOptions = listOf(
            DiceOptions(100, R.drawable.ic_dice_d10), DiceOptions(20, R.drawable.ic_dice_d20),
            DiceOptions(12, R.drawable.ic_dice_d12), DiceOptions(10, R.drawable.ic_dice_d10),
            DiceOptions(8, R.drawable.ic_dice_d8), DiceOptions(6, R.drawable.ic_dice_d6),
            DiceOptions(4, R.drawable.ic_dice_d4),
        )
    }
}

@Immutable
data class DicePersistenceState(
    val savedRolls: List<SavedDiceRoll> = emptyList(),
    val pendingWrites: Int = 0,
    val writeError: Throwable? = null,
    val observationError: Throwable? = null,
)

sealed interface DiceIntent {
    data class SkillCheckRoll(val skill: Skill, val modifier: Int? = null) : DiceIntent
    data class SaveThrowRoll(val ability: Ability, val modifier: Int? = null) : DiceIntent
    data class AbilityCheckRoll(val ability: Ability, val modifier: Int? = null) : DiceIntent
    data class WeaponAttackRoll(val attack: Attack, val modifier: Int? = null) : DiceIntent
    data class WeaponDamageRoll(val attack: Attack, val diceWithMod: String) : DiceIntent
    data class SpellAttackRoll(val modifier: Int? = null) : DiceIntent
    data class SpellDamageRoll(val spell: Spell, val diceWithMod: String) : DiceIntent
    data class InitiativeRoll(val modifier: Int) : DiceIntent
    data class RegularRoll(val diceMap: Map<Int, Int>, val modifier: Int? = null) : DiceIntent
    data class RegularStringRoll(val notation: String) : DiceIntent
    data object ClearDiceRolls : DiceIntent
    data object RetryPersistence : DiceIntent
    data object DismissPersistenceError : DiceIntent
    data object PinResult : DiceIntent
    data object DismissResult : DiceIntent
}

enum class RollType(@param:StringRes val type: Int) {
    DAMAGE(R.string.roll_type_damage), ATTACK(R.string.roll_type_attack),
    SAVE_THROW(R.string.roll_type_save), CHECK(com.yablonskyi.ui.R.string.check),
    INITIATIVE(com.yablonskyi.ui.R.string.initiative),
    ROLL(R.string.roll_type_roll),
}

@Immutable
data class DiceRollState(
    val numbers: List<Int> = emptyList(),
    val stringDices: List<String> = emptyList(),
    val modifier: Int? = null,
    val result: Int = 0,
    val hasRegularDice: Boolean = false,
    val showResult: Boolean = false,
    val isPinned: Boolean = false,
    val labelRes: DiceRollLabel? = null,
) {
    val hasCritSuccess: Boolean get() = hasRegularDice && numbers.any { it == 20 }
    val hasCritFailure: Boolean get() = hasRegularDice && numbers.any { it == 1 }
}

sealed interface DiceRollLabel {
    @Immutable
    data class TypeStringRes(
        @param:StringRes val type: Int,
        @param:StringRes val name: Int,
        val abbreviateName: Boolean = false,
    ) : DiceRollLabel

    data class TypeString(@param:StringRes val type: Int, val name: String = "") : DiceRollLabel
}