package com.yablonskyi.character.presentation.sheet.model

import androidx.annotation.StringRes
import com.yablonskyi.ui.R

enum class CharacterTab(@StringRes val titleRes: Int) {
    ABILITIES(R.string.tab_abilities),
    SPELLS(R.string.tab_spells),
    ATTACKS(R.string.tab_attacks),
    FEATURES(R.string.tab_features),
    INVENTORY(R.string.tab_inventory),
    BACKSTORY(R.string.tab_backstory),
    NOTES(R.string.notes);

    companion object {
        fun getByIndex(index: Int): CharacterTab = entries.getOrElse(index) { ABILITIES }
    }
}
