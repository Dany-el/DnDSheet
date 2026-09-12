package com.yablonskyi.model.dice

enum class DiceRoles(val roll: String) {
    D4("1d4"),
    D6("1d6"),
    D8("1d8"),
    D10("1d10"),
    D12("1d12"),
    D20("1d20"),
    D100("1d100");

    companion object {
        val hitDices: List<String> =
            entries.filterNot { it == D20 || it == D100 || it == D4 }.map { it.name.lowercase() }
    }
}