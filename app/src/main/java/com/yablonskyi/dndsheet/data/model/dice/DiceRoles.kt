package com.yablonskyi.dndsheet.data.model.dice

enum class DiceRoles(val roll: String) {
    D20("1d20"),
    D100("1d100"),
    D12("1d12"),
    D10("1d10"),
    D8("1d8"),
    D6("1d6"),
    D4("1d4");

    companion object {
        val hitDices: List<String> =
            entries.filterNot { it == D20 || it == D100 || it == D4 }.map { it.name.lowercase() }
    }
}