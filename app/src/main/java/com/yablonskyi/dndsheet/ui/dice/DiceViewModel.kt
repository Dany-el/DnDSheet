package com.yablonskyi.dndsheet.ui.dice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        modifier: Int = 0,
        isModifierFromAbility: Boolean = false
    ) {
        if (diceMap.isEmpty()) return

        hideResultJob?.cancel()

        val numbers = mutableListOf<Int>()
        var total = 0
        diceMap.forEach { (sides, count) ->
            repeat(count) {
                val roll = (1..sides).random()
                numbers.add(roll)
                total += roll
            }
            total += modifier
        }

        _diceRollState.value = _diceRollState.value.copy(showResult = false)

        hideResultJob = viewModelScope.launch {

            _diceRollState.value = DiceRollState(
                numbers = numbers,
                modifier = modifier.run {
                    if (isModifierFromAbility) this
                    else null
                },
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

        // Find the pattern in the string if not found, exit.
        val match = regex.find(value.trim()) ?: return

        // Destructure the groups directly into variables
        val (countStr, sidesStr, modStr) = match.destructured


        val count = countStr.toInt()
        val sides = sidesStr.toInt()


        // Clean up the modifier (remove spaces like " + 5" -> "+5") and parse
        val modifier = modStr.replace(" ", "").toIntOrNull() ?: 0


        rollDice(mapOf(sides to count), modifier, true)
    }
}

data class DiceRollState(
    val numbers: List<Int> = emptyList(),
    val modifier: Int? = null,
    val result: Int = 0,
    val showResult: Boolean = false
)