package com.yablonskyi.character.presentation.sheet.mapper

import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.ProficiencyLevel
import com.yablonskyi.model.character.Skill

data class SkillUiModel(
    val skill: Skill,
    val profLevel: ProficiencyLevel,
    val modifier: Int,
)

fun Character.toSkillUiModel(): List<SkillUiModel> =
    Skill.entries.map { skill ->
        SkillUiModel(
            skill = skill,
            profLevel = skillProficiencies[skill] ?: ProficiencyLevel.NONE,
            modifier = getSkillMod(skill)
        )
    }