package com.yablonskyi.dice

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Skill
import com.yablonskyi.ui.utils.formatModifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class DiceViewModel @Inject constructor() : ViewModel() {
    private val _diceRollState = MutableStateFlow(DiceRollState())
    val diceRollState = _diceRollState.asStateFlow()

    private var hideResultJob: Job? = null

    fun onIntent(intent: DiceIntent) {
        when (intent) {
            is DiceIntent.SkillCheckRoll -> performRoll(
                diceMap = mapOf(20 to 1),
                modifier = intent.modifier,
                labelRes = intent.skill.nameRes
            )

            is DiceIntent.SaveThrowRoll -> performRoll(
                diceMap = mapOf(20 to 1),
                modifier = intent.modifier,
                labelRes = intent.ability.nameRes
            )

            is DiceIntent.AbilityCheckRoll -> performRoll(
                diceMap = mapOf(20 to 1),
                modifier = intent.modifier,
                labelRes = intent.ability.nameRes
            )

            is DiceIntent.RegularRoll -> performRoll(intent.diceMap, intent.modifier)

            is DiceIntent.RegularStringRoll -> rollDiceFromString(intent.notation)

            DiceIntent.PinResult -> pinResult()

            DiceIntent.DismissResult -> dismissResult()
        }
    }

    /**
     * @param diceMap sides to count pairs
     * @param labelRes optional label of the rolled check to be shown in the result box
     */
    private fun performRoll(
        diceMap: Map<Int, Int>,
        modifier: Int? = null,
        labelRes: Int? = null,
    ) {
        if (diceMap.isEmpty()) return

        hideResultJob?.cancel()

        val numbers = mutableListOf<Int>()
        val stringDices = mutableListOf<String>()
        var hasRegularDice = false

        var total = 0
        diceMap.forEach { (sides, count) ->
            if (sides == 20) hasRegularDice = true
            stringDices.add("${count}d${sides}${modifier?.let { formatModifier(modifier) } ?: ""}")
            repeat(count) {
                val roll = (1..sides).random()
                numbers.add(roll)
                total += roll
            }
        }
        modifier?.let { total += modifier }

        _diceRollState.value = _diceRollState.value.copy(showResult = false)

        hideResultJob = viewModelScope.launch {
            _diceRollState.value = DiceRollState(
                numbers = numbers,
                stringDices = stringDices,
                modifier = modifier,
                hasRegularDice = hasRegularDice,
                result = total,
                showResult = true,
                isPinned = false,
                labelRes = labelRes
            )

            delay(5.seconds)

            _diceRollState.value = _diceRollState.value.copy(showResult = false)
        }
    }

    /**
     * @param value e.g. 1d20 + 3
     */
    private fun rollDiceFromString(value: String) {
        val regex = """(\d+)\s*[dDКкKk]\s*(\d+)\s*([+-]?\s*\d+)?""".toRegex()

        val match = regex.find(value.trim()) ?: return

        val (countStr, sidesStr, modStr) = match.destructured

        val count = countStr.toInt()
        val sides = sidesStr.toInt()

        val modifier = modStr.replace(" ", "").toIntOrNull()

        performRoll(mapOf(sides to count), modifier)
    }

    private fun pinResult() {
        val currentState = _diceRollState.value

        if (currentState.isPinned) {
            _diceRollState.value = currentState.copy(
                isPinned = false,
                showResult = false
            )
        } else {
            hideResultJob?.cancel()
            _diceRollState.value = currentState.copy(isPinned = true)
        }
    }

    private fun dismissResult() {
        hideResultJob?.cancel()
        _diceRollState.value = _diceRollState.value.copy(showResult = false)
    }

    companion object {

        class DiceOptions(val sides: Int, val iconRes: Int)

        val diceOptions = listOf(
            DiceOptions(100, iconRes = R.drawable.ic_dice_d10),
            DiceOptions(20, iconRes = R.drawable.ic_dice_d20),
            DiceOptions(12, iconRes = R.drawable.ic_dice_d12),
            DiceOptions(10, iconRes = R.drawable.ic_dice_d10),
            DiceOptions(8, iconRes = R.drawable.ic_dice_d8),
            DiceOptions(6, iconRes = R.drawable.ic_dice_d6),
            DiceOptions(4, iconRes = R.drawable.ic_dice_d4),
        )
    }
}

sealed interface DiceIntent {
    data class SkillCheckRoll(val skill: Skill, val modifier: Int? = null) : DiceIntent

    data class SaveThrowRoll(val ability: Ability, val modifier: Int? = null) : DiceIntent

    data class AbilityCheckRoll(val ability: Ability, val modifier: Int? = null) : DiceIntent

    data class RegularRoll(val diceMap: Map<Int, Int>, val modifier: Int? = null) : DiceIntent

    data class RegularStringRoll(val notation: String) : DiceIntent

    data object PinResult : DiceIntent

    data object DismissResult : DiceIntent
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
    @param:StringRes val labelRes: Int? = null
)