package com.yablonskyi.character.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.yablonskyi.character.presentation.common.validation.CharacterFormValidators
import com.yablonskyi.character.presentation.sheet.editor.*
import com.yablonskyi.model.character.Character
import com.yablonskyi.ui.theme.DnDSheetTheme
import com.yablonskyi.ui.validation.FieldState

@Preview(showBackground = true)
@Preview(name = "Landscape", widthDp = 1000, heightDp = 700)
@Composable
private fun SettingsFormPreview() {
    val character = Character(name = "Aria", level = 5, race = "Elf", charClass = "Ranger")
    DnDSheetTheme { CharacterSettingsScreen(form = CharacterSettingsFormUiState.from(character), onIntent = {}, onFormIntent = {}) }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun AttackFormPreview() {
    DnDSheetTheme { UpdateAttackSheet(AttackFormUiState(name = FieldState("Dagger"), initialized = true), {}) }
}

@Preview(showBackground = true)
@Composable
private fun HealthFormPreview() {
    DnDSheetTheme { HealthEditSheetContent(HealthFormUiState(
        current = CharacterFormValidators.number(17, 0, 30), maximum = CharacterFormValidators.number(30, 0, 10000), initialized = true,
    ), {}) }
}
