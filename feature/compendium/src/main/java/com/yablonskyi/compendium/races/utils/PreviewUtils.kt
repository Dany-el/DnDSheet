package com.yablonskyi.compendium.races.utils

import com.yablonskyi.compendium.races.viewmodel.RaceDetailsUiState
import com.yablonskyi.compendium.races.viewmodel.RaceFormUiState
import com.yablonskyi.compendium.races.viewmodel.RaceUiState
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Skill
import com.yablonskyi.model.rulebook.Race
import com.yablonskyi.ui.utils.PreviewStateProvider
import com.yablonskyi.ui.validation.FieldState
import com.yablonskyi.ui.validation.IntFieldState
import com.yablonskyi.ui.validation.RaceValidators

internal object PreviewUtils {

    private val human = Race(
        id = "race-human",
        name = "Human",
        size = "Medium",
        speed = 30,
        abilityBonuses = Ability.playableAbilities.associateWith { 1 },
        grantedSkills = listOf(Skill.PERCEPTION),
        traits = listOf("Versatile", "Ambitious"),
        description = "A versatile and ambitious people who can thrive in any environment."
    )

    private val woodElf = Race(
        id = "race-wood-elf",
        name = "Wood Elf Wood Elf Wood Elf Wood Elf",
        size = "Medium",
        speed = 35,
        abilityBonuses = mapOf(Ability.DEX to 2, Ability.WIS to 1),
        grantedSkills = listOf(Skill.PERCEPTION, Skill.SURVIVAL),
        traits = listOf("Fey Ancestry", "Trance", "Mask of the Wild"),
        description = "Guardians of ancient forests who value freedom and the natural world.",
        isHomebrew = true
    )

    val sampleRaces: List<Race> = listOf(human, woodElf)

    val filled: RaceFormUiState = RaceFormUiState(
        id = woodElf.id,
        name = FieldState(validator = RaceValidators.name).copy(text = woodElf.name),
        size = FieldState(validator = RaceValidators.size).copy(text = woodElf.size),
        traits = FieldState(validator = RaceValidators.traits)
            .copy(text = woodElf.traits.joinToString(", ")),
        description = FieldState(validator = RaceValidators.description)
            .copy(text = woodElf.description),
        speedField = IntFieldState(
            value = woodElf.speed,
            maxValue = 100,
            minValue = 0,
            validator = RaceValidators.speed
        ),
        abilityBonuses = woodElf.abilityBonuses,
        grantedSkills = woodElf.grantedSkills
    )

    val empty: RaceFormUiState = RaceFormUiState()

    val loading: RaceFormUiState = RaceFormUiState(isLoading = true)

    val racesList: RaceUiState = RaceUiState(
        origRaces = listOf(human, human.copy(id = "2"), human.copy(id = "3")),
        homebrewRaces = listOf(woodElf, woodElf.copy(id = "4", name = "Name Real"), woodElf.copy(id = "5")),
        isLoading = false
    )

    val detailsState: RaceDetailsUiState = RaceDetailsUiState(
        selectedRace = woodElf,
        isLoading = false
    )

    val StateProvider = object : PreviewStateProvider<RaceFormUiState> {
        override val default: RaceFormUiState get() = filled

        override val samples: List<RaceFormUiState>
            get() = listOf(default, empty, loading)
    }
}