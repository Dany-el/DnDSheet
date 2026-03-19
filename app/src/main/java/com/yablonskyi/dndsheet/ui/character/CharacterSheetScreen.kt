package com.yablonskyi.dndsheet.ui.character

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.window.core.layout.WindowSizeClass
import coil.compose.AsyncImage
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.Attack
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.ProficiencyLevel
import com.yablonskyi.dndsheet.data.model.character.Skill
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.data.model.character.SpellLevel
import com.yablonskyi.dndsheet.data.model.dice.DiceRoles
import com.yablonskyi.dndsheet.ui.attack.AttackUiModel
import com.yablonskyi.dndsheet.ui.attack.UpdateAttackSheet
import com.yablonskyi.dndsheet.ui.character.slides.AbilitySlide
import com.yablonskyi.dndsheet.ui.character.slides.AttackSlide
import com.yablonskyi.dndsheet.ui.character.slides.BackstorySlide
import com.yablonskyi.dndsheet.ui.character.slides.FeaturesSlide
import com.yablonskyi.dndsheet.ui.character.slides.InventorySlide
import com.yablonskyi.dndsheet.ui.character.slides.NotesSlide
import com.yablonskyi.dndsheet.ui.character.slides.SpellSlide
import com.yablonskyi.dndsheet.ui.character.slides.formatModifier
import com.yablonskyi.dndsheet.ui.dice.DiceRollFloatingActionButton
import com.yablonskyi.dndsheet.ui.dice.DiceRollResultBox
import com.yablonskyi.dndsheet.ui.dice.DiceRollState
import com.yablonskyi.dndsheet.ui.spell.SpellFilter
import com.yablonskyi.dndsheet.ui.spell.SpellInfoSheet
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.UiUtils
import kotlinx.coroutines.launch

sealed class CharacterSheetConfig {
    data class EditAbility(val ability: Ability) : CharacterSheetConfig()
    object EditHealth : CharacterSheetConfig()
    data class EditAttack(val attack: Attack) : CharacterSheetConfig()
    data class ViewSpell(val spell: Spell) : CharacterSheetConfig()
}

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
    onSlotClick: (SpellLevel, Int) -> Unit,
    onRestClick: () -> Unit,
    // Tabs
    rightSelectedTab: CharacterTab,
    leftSelectedTab: CharacterTab,
    onLeftTabSelected: (CharacterTab) -> Unit,
    onRightTabSelected: (CharacterTab) -> Unit,
) {
    if (character == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val scope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val hasEnoughWidth =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    val isWideScreen = hasEnoughWidth && isLandscape

    val tabs = CharacterTab.entries

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val currentTab = tabs.getOrElse(pagerState.currentPage) { tabs.first() }

    // Sheets
    var activeSheet by remember { mutableStateOf<CharacterSheetConfig?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val closeSheet: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            activeSheet = null
        }
    }

    val focusManager = LocalFocusManager.current

    LaunchedEffect(pagerState.currentPage) {
        focusManager.clearFocus()
    }

    val tabContent: @Composable (CharacterTab, Modifier) -> Unit = { tab, modifier ->
        when (tab) {
            CharacterTab.ABILITIES -> {
                AbilitySlide(
                    character = character,
                    onRollClick = onDiceButtonClick,
                    onAbilityClick = { ability ->
                        activeSheet = CharacterSheetConfig.EditAbility(ability)
                    },
                    onProfSavingThrowClick = updateSavingThrowProficiency,
                    onProficiencyChange = updateProfLevel,
                    modifier = modifier
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
                    onSlotClick = onSlotClick,
                    onSpellClick = { activeSheet = CharacterSheetConfig.ViewSpell(it) },
                    modifier = modifier,
                )
            }

            CharacterTab.ATTACKS -> {
                AttackSlide(
                    attacks = attacks,
                    onAdd = {
                        activeSheet =
                            CharacterSheetConfig.EditAttack(Attack(characterId = character.id))
                    },
                    onUpdate = { activeSheet = CharacterSheetConfig.EditAttack(it) },
                    onRollClick = onDiceButtonClick,
                    modifier = modifier
                )
            }

            CharacterTab.FEATURES -> {
                FeaturesSlide(
                    traits = character.traits,
                    feats = character.feats,
                    proficiencies = character.proficiencies,
                    updateFeats = { onUpdateCharacter(character.copy(feats = it)) },
                    updateTraits = { onUpdateCharacter(character.copy(traits = it)) },
                    updateProficiencies = { onUpdateCharacter(character.copy(proficiencies = it)) },
                    modifier = modifier
                )
            }

            CharacterTab.INVENTORY -> {
                InventorySlide(
                    coins = character.coins,
                    inventory = character.inventory,
                    onCoinChange = { onUpdateCharacter(character.copy(coins = it)) },
                    onSaveText = { onUpdateCharacter(character.copy(inventory = it)) },
                    modifier = modifier
                )
            }

            CharacterTab.BACKSTORY -> {
                BackstorySlide(
                    backstory = character.backstory,
                    onSaveText = { onUpdateCharacter(character.copy(backstory = it)) },
                    modifier = modifier
                )
            }

            CharacterTab.NOTES -> {
                NotesSlide(
                    notes = character.notes,
                    onSaveText = { onUpdateCharacter(character.copy(notes = it)) },
                    modifier = modifier
                )
            }
        }
    }

    Scaffold(
        topBar = {
            if (isWideScreen) {
                ExpandedTopAppBar(
                    character = character,
                    onSettingsNavigate = onSettingsNavigate,
                    onNavigateBack = onNavigateBack,
                )
            } else {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onSettingsNavigate(character.id) }
                            )
                        ) {
                            Text(
                                text = character.name,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            val characterInfo = listOf(character.race, character.charClass)
                                .filter { it.isNotBlank() }
                                .joinToString(" — ")

                            Text(
                                text = characterInfo,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                        IconButton(
                            onClick = {
                                onSettingsNavigate(character.id)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            DiceRollFloatingActionButton(
                onClick = onDiceClick
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
        ) {
            if (isWideScreen) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .align(Alignment.Top)
                                .padding(vertical = 4.dp)
                                .padding(end = 8.dp)
                        ) {
                            SlideSelector(
                                tabs = tabs,
                                currentTab = leftSelectedTab,
                                onTabSelected = onLeftTabSelected,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(16.dp))
                            HealthBar(
                                character = character,
                                onHealthClick = {
                                    activeSheet = CharacterSheetConfig.EditHealth
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            CharacterDetailsRowExpanded(
                                character = character,
                                onRollClick = onDiceButtonClick,
                                onRestClick = onRestClick,
                                modifier = Modifier.weight(1f)
                            )

                            SlideSelector(
                                tabs = tabs,
                                currentTab = rightSelectedTab,
                                onTabSelected = onRightTabSelected,
                                modifier = Modifier
                                    .weight(1f)
                                    .align(Alignment.Top)
                                    .padding(4.dp)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(end = 4.dp)
                        ) {
                            Crossfade(
                                targetState = leftSelectedTab,
                                modifier = Modifier.fillMaxSize(),
                                label = "LeftPaneAnimation"
                            ) { currentTab ->
                                tabContent(currentTab, Modifier.fillMaxSize())
                            }
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(start = 4.dp)
                        ) {
                            Crossfade(
                                targetState = rightSelectedTab,
                                modifier = Modifier.fillMaxSize(),
                                label = "RightPaneAnimation"
                            ) { currentTab ->
                                tabContent(currentTab, Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CharacterDetailsRow(
                        character = character,
                        onRollClick = onDiceButtonClick,
                        onRestClick = onRestClick,
                        onHealthClick = {
                            activeSheet = CharacterSheetConfig.EditHealth
                        },
                    )
                    SlideSelector(
                        tabs = tabs,
                        currentTab = currentTab,
                        onTabSelected = { newTab ->
                            scope.launch {
                                pagerState.animateScrollToPage(newTab.ordinal)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 8.dp)
                    )
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                    ) { pageIndex ->
                        val tab = CharacterTab.getByIndex(pageIndex)

                        val slideModifier = Modifier.fillMaxSize()

                        tabContent(tab, slideModifier)
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
                    strings = diceState.stringDices,
                    hasRegularDice = diceState.hasRegularDice,
                    diceMod = diceState.modifier,
                    result = diceState.result
                )
            }
        }
    }
    activeSheet?.let { sheetConfig ->
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            properties = ModalBottomSheetProperties(
                shouldDismissOnBackPress = false
            )
        ) {
            when (sheetConfig) {
                is CharacterSheetConfig.EditAbility -> {
                    val currentScore = character.abilityBlock.getScore(sheetConfig.ability)
                    val currentModifier = character.abilityBlock.getModifier(sheetConfig.ability)

                    AbilityEditSheetContent(
                        ability = sheetConfig.ability,
                        abilityModifier = currentModifier,
                        currentValue = currentScore,
                        onDismiss = closeSheet,
                        onApply = { newValue ->
                            updateAbility(sheetConfig.ability, newValue)
                        }
                    )
                }

                CharacterSheetConfig.EditHealth -> {
                    HealthEditSheetContent(
                        currentHp = character.currentHp,
                        maxHp = character.maxHp,
                        tempHp = character.tempHp,
                        onDismiss = closeSheet,
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

                is CharacterSheetConfig.EditAttack -> {
                    UpdateAttackSheet(
                        attack = sheetConfig.attack,
                        onDismiss = closeSheet,
                        onSave = { result -> saveAttack(result) },
                        onDelete = { deleteAttack(it) }
                    )
                }

                is CharacterSheetConfig.ViewSpell -> {
                    SpellInfoSheet(
                        spell = sheetConfig.spell,
                        onDismiss = closeSheet,
                    )
                }
            }
        }
    }
}

@Composable
fun CharacterDetailsRow(
    character: Character,
    onRollClick: (String) -> Unit,
    onHealthClick: () -> Unit,
    onRestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember {
        mutableStateOf(true)
    }

    Column(
        modifier = modifier.padding(horizontal = 8.dp)
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
                    modifier = Modifier.size(64.dp),
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
            HealthBar(
                character = character,
                onHealthClick = onHealthClick
            )
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
        AnimatedVisibility(
            visible = isVisible
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    TextButton(
                        enabled = false,
                        onClick = { },
                        colors = ButtonDefaults.buttonColors().copy(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary,
                            disabledContentColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.width(80.dp)
                    ) {
                        Text(
                            text = formatModifier(character.getProfBonus()),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        text = stringResource(R.string.proficiency_bonus).uppercase()
                            .replaceFirst(" ", "\n"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    TextButton(
                        onClick = { onRollClick("${DiceRoles.D20.roll}${formatModifier(character.getInitiativeBonus())}") },
                        border = BorderStroke(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        ),
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
                        text = stringResource(R.string.initiative).uppercase()
                            .replaceFirst(" ", "\n"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                    )
                }
                LongRestButton(
                    onConfirmRest = onRestClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = { isVisible = !isVisible }
            ) {
                HorizontalDivider(Modifier.weight(1f))
                Text(
                    text = if (isVisible) stringResource(R.string.collapse).uppercase()
                    else stringResource(R.string.expand).uppercase(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                HorizontalDivider(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CharacterDetailsRowExpanded(
    character: Character,
    onRollClick: (String) -> Unit,
    onRestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            TextButton(
                onClick = { onRollClick("${DiceRoles.D20.roll}${formatModifier(character.getInitiativeBonus())}") },
                border = BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                ),
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
                text = stringResource(R.string.initiative).uppercase()
                    .replaceFirst(" ", "\n"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
        LongRestButton(
            onConfirmRest = onRestClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedTopAppBar(
    character: Character,
    onSettingsNavigate: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier.padding(top = 8.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSettingsNavigate(character.id) }
                    )
                ){
                    Surface(
                        shape = CircleShape,
                        modifier = Modifier
                            .wrapContentHeight()
                            .heightIn(max = 64.dp)
                            .widthIn(max = 64.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        if (character.imagePath != null) {
                            AsyncImage(
                                model = character.imagePath,
                                contentDescription = "Character Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Add Photo",
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxSize()
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            text = character.name,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val characterInfo = listOf(character.race, character.charClass)
                            .filter { it.isNotBlank() }
                            .joinToString(" — ")

                        Text(
                            text = characterInfo,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_shield),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
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
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = formatModifier(character.getProfBonus()),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.proficiency_bonus).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
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
            IconButton(
                onClick = {
                    onSettingsNavigate(character.id)
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Options",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    )
}

@Composable
fun HealthBar(
    character: Character,
    onHealthClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hpColor by animateColorAsState(
        targetValue = if (character.currentHp > character.maxHp / 2) Color(0xff529c64) else Color(
            0xffe34c1e
        ),
        animationSpec = tween(500),
        label = "Health Color Animation"
    )

    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0f),
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(2.dp, hpColor),
        modifier = modifier
            .widthIn(min = 240.dp)
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
}

@Composable
fun LongRestButton(onConfirmRest: () -> Unit, modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Button(
            border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary),
            shape = MaterialTheme.shapes.extraSmall,
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            onClick = { showDialog = true },
            modifier = Modifier.width(80.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_campfire),
                contentDescription = null
            )
        }
        Text(
            text = stringResource(R.string.long_rest).uppercase().replaceFirst(" ", "\n"),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.label_long_rest)) },
            text = { Text(stringResource(R.string.msg_long_rest)) },
            confirmButton = {
                TextButton(onClick = {
                    onConfirmRest()
                    showDialog = false
                }) { Text(stringResource(R.string.rest)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun SlideSelector(
    tabs: List<CharacterTab>,
    currentTab: CharacterTab,
    onTabSelected: (CharacterTab) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val density = LocalDensity.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(bottom = 4.dp)
    ) {
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
                    imageVector = Icons.AutoMirrored.Filled.List,
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
            tabs.forEach { tab ->
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
    BACKSTORY(R.string.tab_backstory),
    NOTES(R.string.notes);

    companion object {
        fun getByIndex(index: Int): CharacterTab = entries.getOrElse(index) { ABILITIES }
    }
}

@Preview(
    showBackground = false, showSystemUi = false, locale = "ru",
    device = "spec:width=1280dp,height=800dp,dpi=240"
)
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
            updateSavingThrowProficiency = { _, _ -> },
            saveAttack = {},
            deleteAttack = {},
            onSettingsNavigate = {},
            onNavigateBack = {},
            onManageClick = {},
            onRestClick = {},
            onSlotClick = { _, _ -> },
            leftSelectedTab = CharacterTab.ABILITIES,
            rightSelectedTab = CharacterTab.SPELLS,
            onLeftTabSelected = {},
            onRightTabSelected = {}
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
            updateSavingThrowProficiency = { _, _ -> },
            saveAttack = {},
            deleteAttack = {},
            onSettingsNavigate = {},
            onNavigateBack = {},
            onManageClick = {},
            onRestClick = {},
            onSlotClick = { _, _ -> },
            leftSelectedTab = CharacterTab.ABILITIES,
            rightSelectedTab = CharacterTab.SPELLS,
            onLeftTabSelected = {},
            onRightTabSelected = {}
        )
    }
}