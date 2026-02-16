package com.yablonskyi.dndsheet.ui.dice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.dndsheet.ui.character.slides.formatModifier
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiceViewModel : ViewModel() {
    private val _diceRollState = MutableStateFlow(DiceRollState())
    val diceRollState = _diceRollState.asStateFlow()

    private var hideResultJob: Job? = null

    /**
     * @param diceMap sides to count pairs
     */
    fun rollDice(
        diceMap: Map<Int, Int>,
        modifier: Int? = null,
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
            modifier?.let { total += modifier }
        }

        _diceRollState.value = _diceRollState.value.copy(showResult = false)

        hideResultJob = viewModelScope.launch {
            _diceRollState.value = DiceRollState(
                numbers = numbers,
                stringDices = stringDices,
                modifier = modifier,
                hasRegularDice = hasRegularDice,
                result = total,
                showResult = true
            )

            delay(5000L)

            _diceRollState.value = _diceRollState.value.copy(showResult = false)
        }
    }

    /**
     * @param value e.g. 1d20 + 3
     */
    fun rollDiceFromString(value: String) {
        val regex = """(\d+)\s*[dDКкKk]\s*(\d+)\s*([+-]?\s*\d+)?""".toRegex()

        val match = regex.find(value.trim()) ?: return

        val (countStr, sidesStr, modStr) = match.destructured

        val count = countStr.toInt()
        val sides = sidesStr.toInt()

        val modifier = modStr.replace(" ", "").toIntOrNull()

        rollDice(mapOf(sides to count), modifier)
    }
}

data class DiceRollState(
    val numbers: List<Int> = emptyList(),
    val modifier: Int? = null,
    val result: Int = 0,
    val stringDices: List<String> = emptyList(),
    val hasRegularDice: Boolean = false,
    val showResult: Boolean = false
)