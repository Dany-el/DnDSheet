package com.yablonskyi.dndsheet.ui.character

import android.content.res.Configuration
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.Attack
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.ProficiencyLevel
import com.yablonskyi.dndsheet.data.model.character.Skill
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.data.model.dice.DiceRoles
import com.yablonskyi.dndsheet.ui.attack.AttackUiModel
import com.yablonskyi.dndsheet.ui.attack.UpdateAttackSheet
import com.yablonskyi.dndsheet.ui.character.slides.AbilitySlide
import com.yablonskyi.dndsheet.ui.character.slides.AttackSlide
import com.yablonskyi.dndsheet.ui.character.slides.BackstorySlide
import com.yablonskyi.dndsheet.ui.character.slides.FeaturesSlide
import com.yablonskyi.dndsheet.ui.character.slides.InventorySlide
import com.yablonskyi.dndsheet.ui.character.slides.SpellSlide
import com.yablonskyi.dndsheet.ui.character.slides.formatModifier
import com.yablonskyi.dndsheet.ui.dice.DiceRollResultBox
import com.yablonskyi.dndsheet.ui.dice.DiceRollState
import com.yablonskyi.dndsheet.ui.dice.MultiFloatingActionButton
import com.yablonskyi.dndsheet.ui.spell.SpellFilter
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.UiUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterSheetScreen(
    character: Character?,
    spells: List<Spell>,
    attacks: List<AttackUiModel>,
    currentFilter: SpellFilter,
    diceState: DiceRollState,
    availableFilters: List<SpellFilter>,

    onDiceButtonClick: (String) -> Unit,
    onDiceClick: (Map<Int, Int>) -> Unit,

    onUpdateCharacter: (Character) -> Unit,

    onFilterChange: (SpellFilter) -> Unit,

    updateAbility: (Ability, Int) -> Unit,
    updateProfLevel: (Skill, ProficiencyLevel) -> Unit,
    updateSavingThrowProficiency: (Ability, Boolean) -> Unit,

    saveAttack: (Attack) -> Unit,
    deleteAttack: (Attack) -> Unit,

    onSettingsNavigate: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    onManageClick: (Long) -> Unit,
) {
    if (character == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { CharacterTab.entries.size })
    val scope = rememberCoroutineScope()

    val currentTab = CharacterTab.getByIndex(pagerState.currentPage)

    var editingAbility by remember { mutableStateOf<Ability?>(null) }
    var showHealthSheet by remember { mutableStateOf(false) }
    var showAttackSheet by remember { mutableStateOf<Attack?>(null) }

    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = character.name,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${character.race} — ${character.charClass}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    var menuExpanded by remember { mutableStateOf(false) }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings)) },
                                onClick = {
                                    menuExpanded = false
                                    onSettingsNavigate(character.id)
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            MultiFloatingActionButton(
                onClick = onDiceClick
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                CharacterDetailsRow(
                    character = character,
                    onRollClick = onDiceButtonClick,
                    onHealthClick = {
                        showHealthSheet = true
                    },
                )
                SlideSelector(
                    currentTab = currentTab,
                    onTabSelected = { newTab ->
                        scope.launch {
                            pagerState.animateScrollToPage(newTab.ordinal)
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                ) { pageIndex ->
                    val tab = CharacterTab.getByIndex(pageIndex)

                    val slideModifier = Modifier.fillMaxSize()

                    when (tab) {
                        CharacterTab.ABILITIES -> {
                            AbilitySlide(
                                character = character,
                                onRollClick = onDiceButtonClick,
                                onAbilityClick = { ability ->
                                    editingAbility = ability
                                },
                                onProfSavingThrowClick = updateSavingThrowProficiency,
                                onProficiencyChange = updateProfLevel,
                                modifier = slideModifier
                            )
                        }

                        CharacterTab.SPELLS -> {
                            SpellSlide(
                                character = character,
                                spells = spells,
                                availableFilters = availableFilters,
                                currentFilter = currentFilter,
                                onFilterChange = onFilterChange,
                                onRollClick = onDiceButtonClick,
                                onManageSpellsClick = onManageClick,
                                modifier = slideModifier,
                            )
                        }

                        CharacterTab.ATTACKS -> {
                            AttackSlide(
                                attacks = attacks,
                                onAdd = { showAttackSheet = Attack(characterId = character.id) },
                                onUpdate = { showAttackSheet = it },
                                onRollClick = onDiceButtonClick,
                                modifier = slideModifier
                            )
                        }

                        CharacterTab.FEATURES -> {
                            FeaturesSlide(
                                traits = character.traits,
                                feats = character.feats,
                                proficiencies = character.proficiencies,
                                updateFeats = {
                                    onUpdateCharacter(
                                        character.copy(
                                            feats = it
                                        )
                                    )
                                },
                                updateTraits = {
                                    onUpdateCharacter(
                                        character.copy(
                                            traits = it
                                        )
                                    )
                                },
                                updateProficiencies = {
                                    onUpdateCharacter(
                                        character.copy(
                                            proficiencies = it
                                        )
                                    )
                                },
                                modifier = slideModifier
                            )
                        }

                        CharacterTab.INVENTORY -> {
                            InventorySlide(
                                coins = character.coins,
                                inventory = character.inventory,
                                onCoinChange = {
                                    onUpdateCharacter(
                                        character.copy(
                                            coins = it
                                        )
                                    )
                                },
                                onSaveText = {
                                    onUpdateCharacter(
                                        character.copy(
                                            inventory = it
                                        )
                                    )
                                },
                                modifier = slideModifier
                            )
                        }

                        CharacterTab.BACKSTORY -> {
                            BackstorySlide(
                                backstory = character.backstory,
                                onSaveText = {
                                    onUpdateCharacter(
                                        character.copy(
                                            backstory = it
                                        )
                                    )
                                },
                                modifier = slideModifier
                            )
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = diceState.showResult,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                DiceRollResultBox(
                    numbers = diceState.numbers,
                    diceMod = diceState.modifier,
                    result = diceState.result
                )
            }
        }
    }
    if (editingAbility != null) {
        ModalBottomSheet(
            onDismissRequest = { editingAbility = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            val currentScore = character.abilityBlock.getScore(editingAbility!!)
            val currentModifier = character.abilityBlock.getModifier(editingAbility!!)

            AbilityEditSheetContent(
                ability = editingAbility!!,
                abilityModifier = currentModifier,
                currentValue = currentScore,
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        editingAbility = null
                    }
                },
                onApply = { newValue ->
                    updateAbility(editingAbility!!, newValue)
                }
            )
        }
    }

    if (showHealthSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHealthSheet = false },
            sheetState = sheetState
        ) {
            HealthEditSheetContent(
                currentHp = character.currentHp,
                maxHp = character.maxHp,
                tempHp = character.tempHp,
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showHealthSheet = false
                    }
                },
                onApply = { newCurrent, newMax, newTemp ->
                    onUpdateCharacter(
                        character.copy(
                            currentHp = newCurrent,
                            maxHp = newMax,
                            tempHp = newTemp
                        )
                    )
                }
            )
        }
    }

    if (showAttackSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { showAttackSheet = null },
            sheetState = sheetState
        ) {
            UpdateAttackSheet(
                attack = showAttackSheet!!,
                onDismiss = { showAttackSheet = null },
                onSave = { result ->
                    Log.i("Attack","Saving/updating attack $result")
                    saveAttack(result)
                },
                onDelete = { deleteAttack(it) }
            )
        }
    }
}

@Composable
fun CharacterDetailsRow(
    character: Character,
    onRollClick: (String) -> Unit,
    onHealthClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val hpColor by animateColorAsState(
        targetValue = if (character.currentHp > character.maxHp / 2) Color(0xff529c64) else Color(0xffe34c1e),
        animationSpec = tween(500),
        label = "Health Color Animation"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.padding(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.weight(0.4f)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_shield),
                    contentDescription = null,
                    modifier = Modifier.size(54.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${character.getTotalAc()}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0f),
                shape = MaterialTheme.shapes.extraSmall,
                border = BorderStroke(2.dp, hpColor),
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        onClick = onHealthClick
                    )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_vital_signs),
                        contentDescription = null,
                        tint = hpColor
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "${character.currentHp}/${character.maxHp} " + if (character.tempHp > 0) "(${character.tempHp})" else "",
                        fontWeight = FontWeight.SemiBold,
                        color = hpColor
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(0.4f)
            ) {
                Text(
                    text = character.speed.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.char_speed).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = { onRollClick("${DiceRoles.D20.roll}${formatModifier(character.getInitiativeBonus())}") },
                border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary),
                shape = MaterialTheme.shapes.extraSmall,
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = formatModifier(character.getInitiativeBonus()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = stringResource(R.string.char_initiative_bonus).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SlideSelector(
    currentTab: CharacterTab,
    onTabSelected: (CharacterTab) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val density = LocalDensity.current

    Box(contentAlignment = Alignment.Center, modifier = modifier.padding(8.dp)) {
        Surface(
            color = OutlinedTextFieldDefaults.colors().unfocusedContainerColor,
            shape = MaterialTheme.shapes.extraSmall,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        textFieldSize = coordinates.size.toSize()
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { expanded = true }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(currentTab.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(density) { textFieldSize.width.toDp() })
        ) {
            CharacterTab.entries.forEach { tab ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(tab.titleRes),
                            fontWeight = if (tab == currentTab) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        expanded = false
                        onTabSelected(tab)
                    },
                    leadingIcon = if (tab == currentTab) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null
                )
            }
        }
    }
}


enum class CharacterTab(@StringRes val titleRes: Int) {
    ABILITIES(R.string.tab_abilities),
    SPELLS(R.string.tab_spells),
    ATTACKS(R.string.tab_attacks),
    FEATURES(R.string.tab_features),
    INVENTORY(R.string.tab_inventory),
    BACKSTORY(R.string.tab_backstory);

    companion object {
        fun getByIndex(index: Int): CharacterTab = entries.getOrElse(index) { ABILITIES }
    }
}

@Preview(showBackground = false)
@Composable
private fun CharacterSheetScreenPreview() {
    DnDSheetTheme {
        CharacterSheetScreen(
            character = UiUtils.sampleCharacters[2],
            spells = UiUtils.sampleSpells,
            attacks = UiUtils.sampleAttacks,
            currentFilter = SpellFilter.All,
            diceState = DiceRollState(),
            availableFilters = emptyList(),
            onDiceButtonClick = {},
            onDiceClick = {},
            onUpdateCharacter = {},
            onFilterChange = {},
            updateAbility = { _, _ -> },
            updateProfLevel = { _, _ -> },
            updateSavingThrowProficiency = {_, _ ->},
            saveAttack = {},
            deleteAttack = {},
            onSettingsNavigate = {},
            onManageClick = {},
            onNavigateBack = {}
        )
    }
}

@Preview(
    showBackground = false, showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun CharacterSheetScreenPreview_Night() {
    DnDSheetTheme {
        CharacterSheetScreen(
            character = UiUtils.sampleCharacters.first(),
            spells = emptyList(),
            attacks = emptyList(),
            currentFilter = SpellFilter.All,
            diceState = DiceRollState(),
            availableFilters = emptyList(),
            onDiceButtonClick = {},
            onDiceClick = {},
            onUpdateCharacter = {},
            onFilterChange = {},
            updateAbility = { _, _ -> },
            updateProfLevel = { _, _ -> },
            updateSavingThrowProficiency = {_,_ ->},
            saveAttack = {},
            deleteAttack = {},
            onSettingsNavigate = {},
            onManageClick = {},
            onNavigateBack = {}
        )
    }
}