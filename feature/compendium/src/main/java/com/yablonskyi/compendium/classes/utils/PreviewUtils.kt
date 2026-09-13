package com.yablonskyi.compendium.classes.utils

import com.yablonskyi.compendium.classes.viewmodel.ClassDetailsUiState
import com.yablonskyi.compendium.classes.viewmodel.ClassFormUiState
import com.yablonskyi.compendium.classes.viewmodel.ClassUiState
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Skill
import com.yablonskyi.model.rulebook.CharacterClass
import com.yablonskyi.ui.utils.PreviewStateProvider
import com.yablonskyi.ui.validation.ClassValidators
import com.yablonskyi.ui.validation.FieldState
import com.yablonskyi.ui.validation.IntFieldState

internal object PreviewUtils {

    private val champion = CharacterClass(
        id = "class-champion",
        name = "Champion",
        hitDice = "d10",
        primaryAbility = Ability.STR,
        savingThrows = setOf(Ability.STR, Ability.CON),
        skillChoiceCount = 2,
        availableSkills = listOf(
            Skill.ATHLETICS,
            Skill.INTIMIDATION,
            Skill.PERCEPTION,
            Skill.SURVIVAL
        ),
        spellcastingAbility = null,
        description = "A warrior of extraordinary prowess who masters weapons and armor."
    )

    private val arcanist = CharacterClass(
        id = "class-arcanist",
        name = "Arcanist",
        hitDice = "d6",
        primaryAbility = Ability.INT,
        savingThrows = setOf(Ability.INT, Ability.WIS),
        skillChoiceCount = 3,
        availableSkills = listOf(Skill.ARCANA, Skill.HISTORY, Skill.INVESTIGATION),
        spellcastingAbility = Ability.INT,
        description = "A scholar of the arcane arts who bends reality through studied magic.",
        isHomebrew = true
    )

    val sampleClasses: List<CharacterClass> = listOf(champion, arcanist)

    val filled: ClassFormUiState = ClassFormUiState(
        id = arcanist.id,
        name = FieldState(validator = ClassValidators.name).copy(text = arcanist.name),
        primaryAbility = arcanist.primaryAbility,
        savingThrows = arcanist.savingThrows,
        skillChoiceCountField = IntFieldState(
            value = arcanist.skillChoiceCount,
            maxValue = 10,
            minValue = 0,
            validator = ClassValidators.skillChoiceCount(max = 10)
        ),
        chosenSkills = arcanist.availableSkills,
        spellcastingAbility = arcanist.spellcastingAbility,
        hitDice = arcanist.hitDice,
        description = FieldState(validator = ClassValidators.description)
            .copy(text = arcanist.description)
    )

    val empty: ClassFormUiState = ClassFormUiState()

    val loading: ClassFormUiState = ClassFormUiState(isLoading = true)

    val classesList: ClassUiState = ClassUiState(
        origClasses = listOf(champion),
        homebrewClasses = listOf(arcanist),
        isLoading = false
    )

    val detailsState: ClassDetailsUiState = ClassDetailsUiState(
        selectedClass = arcanist,
        isLoading = false
    )

    val StateProvider = object : PreviewStateProvider<ClassFormUiState> {
        override val default: ClassFormUiState get() = filled

        override val samples: List<ClassFormUiState>
            get() = listOf(default, empty, loading)
    }
}