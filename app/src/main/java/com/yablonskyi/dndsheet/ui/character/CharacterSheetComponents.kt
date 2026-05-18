package com.yablonskyi.dndsheet.ui.character

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.Attack
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.dice.DiceRoles
import com.yablonskyi.dndsheet.ui.attack.UpdateAttackSheet
import com.yablonskyi.dndsheet.ui.character.slides.formatModifier
import com.yablonskyi.dndsheet.ui.dice.DiceRollResultBox
import com.yablonskyi.dndsheet.ui.dice.DiceRollState
import com.yablonskyi.dndsheet.ui.spell.SpellInfoSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterTopAppBar(
    name: String,
    race: String,
    charClass: String,
    imagePath: String?,
    // Modifiers
    modifier: Modifier = Modifier,
    nameModifier: Modifier,
    classRaceModifier: Modifier,
    imageModifier: Modifier,
    onNavigateBack: () -> Unit,
    onSettingsNavigate: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSettingsNavigate() }
                    )
            ) {
                Surface(
                    shape = CircleShape,
                    modifier = imageModifier.size(48.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    if (imagePath != null) {
                        AsyncImage(
                            model = imagePath,
                            contentDescription = "Character Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Add Photo",
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxSize()
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = name,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = nameModifier
                    )

                    val characterInfo = listOf(race, charClass)
                        .filter { it.isNotBlank() }
                        .joinToString(" — ")

                    Text(
                        text = characterInfo,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = classRaceModifier
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        },
        actions = {
            IconButton(
                onClick = { onSettingsNavigate() }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Options",
                )
            }
        }
    )
}

@Composable
fun HealthBar(
    currentHp: Int,
    maxHp: Int,
    tempHp: Int,
    onHealthClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hpColor by animateColorAsState(
        targetValue = if (currentHp > maxHp / 2) Color(0xff529c64) else Color(
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
                text = "${currentHp}/${maxHp} " + if (tempHp > 0) "(${tempHp})" else "",
                fontWeight = FontWeight.SemiBold,
                color = hpColor
            )
        }
    }
}

@Composable
fun SpeedDisplay(
    speed: Int,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = speed.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.char_speed).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ArmorClassDisplay(
    armorClass: Int,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_shield),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$armorClass",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DiceResultOverlay(
    diceState: DiceRollState,
    onDismiss: () -> Unit,
    onPinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = diceState.showResult,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        val dismissState = rememberSwipeToDismissBoxState(
            SwipeToDismissBoxValue.Settled,
            SwipeToDismissBoxDefaults.positionalThreshold
        )

        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {},
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = true,
            onDismiss = { dismissValue ->
                if (dismissValue != SwipeToDismissBoxValue.Settled) {
                    onDismiss()
                }
            }
        ) {
            DiceRollResultBox(
                numbers = diceState.numbers,
                strings = diceState.stringDices,
                hasRegularDice = diceState.hasRegularDice,
                diceMod = diceState.modifier,
                result = diceState.result,
                isPinned = diceState.isPinned,
                onPinClick = onPinClick
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
            border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.tertiary),
            shape = MaterialTheme.shapes.extraSmall,
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.tertiary
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
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
fun SlideSelector(
    tabs: List<CharacterTab>,
    currentTab: CharacterTab,
    onTabSelected: (CharacterTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedTabIndex = tabs.indexOf(currentTab).coerceAtLeast(0)


    Surface(
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            divider = {},
            indicator = {
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(selectedTabIndex, matchContentSize = false)
                        .fillMaxSize()
                        .zIndex(-1f)
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
                )
            },
            edgePadding = 8.dp,
            tabs = {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTabIndex == index

                    CompositionLocalProvider(LocalRippleConfiguration provides null) {
                        Tab(
                            selected = isSelected,
                            onClick = { onTabSelected(tab) },
                            text = {
                                Text(
                                    text = stringResource(tab.titleRes),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun VerticalCharacterLayout(
    // HP
    currentHp: Int,
    maxHp: Int,
    tempHp: Int,
    // Initiative
    initiativeBonus: Int,
    // AC
    armorClass: Int,
    // Speed
    speed: Int,
    // Prof bonus
    proficiencyBonus: Int,

    tabs: List<CharacterTab>,
    pagerState: PagerState,

    onDiceButtonClick: (String) -> Unit,
    onRestClick: () -> Unit,
    onHealthClick: () -> Unit,
    onTabSelected: (CharacterTab) -> Unit,
    tabContent: @Composable (CharacterTab, Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        CharacterDetailsRow(
            currentHp = currentHp,
            maxHp = maxHp,
            tempHp = tempHp,
            initiativeBonus = initiativeBonus,
            armorClass = armorClass,
            speed = speed,
            proficiencyBonus = proficiencyBonus,
            onRollClick = onDiceButtonClick,
            onRestClick = onRestClick,
            onHealthClick = onHealthClick,
        )

        val currentTab by remember {
            derivedStateOf { CharacterTab.getByIndex(pagerState.currentPage) }
        }

        SlideSelector(
            tabs = tabs,
            currentTab = currentTab,
            onTabSelected = onTabSelected,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 8.dp)
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            overscrollEffect = null,
            key = { pageIndex -> tabs[pageIndex].name }
        ) { pageIndex ->
            val tab = remember(pageIndex) { CharacterTab.getByIndex(pageIndex) }

            tabContent(tab, Modifier.fillMaxSize())
        }
    }
}

@Composable
fun CharacterDetailsRow(
    // HP
    currentHp: Int,
    maxHp: Int,
    tempHp: Int,
    // Initiative
    initiativeBonus: Int,
    // AC
    armorClass: Int,
    // Speed
    speed: Int,
    // Prof bonus
    proficiencyBonus: Int,
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
            modifier = Modifier.fillMaxWidth()
        ) {
            ArmorClassDisplay(
                armorClass = armorClass,
                modifier = Modifier.weight(0.4f)
            )
            HealthBar(
                currentHp = currentHp,
                maxHp = maxHp,
                tempHp = tempHp,
                onHealthClick = onHealthClick
            )
            SpeedDisplay(
                speed = speed,
                modifier = Modifier.weight(0.4f)
            )
        }
        AnimatedVisibility(
            visible = isVisible,
        ) {
            CollapsibleDetails(
                proficiencyBonus = proficiencyBonus,
                initiativeBonus = initiativeBonus,
                onRollClick = onRollClick,
                onRestClick = onRestClick
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = { isVisible = !isVisible },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.secondary
                )
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
fun CollapsibleDetails(
    proficiencyBonus: Int,
    initiativeBonus: Int,
    onRollClick: (String) -> Unit,
    onRestClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            TextButton(
                enabled = false,
                onClick = { },
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = Color.Transparent,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = Color.Transparent
                ),
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = formatModifier(proficiencyBonus),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(R.string.proficiency_bonus).uppercase()
                    .replaceFirst(" ", "\n"),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            TextButton(
                onClick = { onRollClick("${DiceRoles.D20.roll}${formatModifier(initiativeBonus)}") },
                border = BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.tertiary
                ),
                shape = MaterialTheme.shapes.extraSmall,
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.tertiary,
                ),
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = formatModifier(initiativeBonus),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = stringResource(R.string.initiative).uppercase()
                    .replaceFirst(" ", "\n"),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
    name: String,
    race: String,
    charClass: String,
    imagePath: String?,
    // Modifiers
    modifier: Modifier = Modifier,
    nameModifier: Modifier,
    classRaceModifier: Modifier,
    imageModifier: Modifier,
    // AC
    armorClass: Int,
    // Speed
    speed: Int,
    // Prof bonus
    proficiencyBonus: Int,
    onSettingsNavigate: () -> Unit,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        modifier = modifier.padding(top = 8.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                            onClick = { onSettingsNavigate() }
                        )
                ) {
                    Surface(
                        shape = CircleShape,
                        modifier = imageModifier.size(64.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        if (imagePath != null) {
                            AsyncImage(
                                model = imagePath,
                                contentDescription = "Character Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Add Photo",
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxSize()
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            text = name,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = nameModifier
                        )

                        val characterInfo = listOf(race, charClass)
                            .filter { it.isNotBlank() }
                            .joinToString(" — ")

                        Text(
                            text = characterInfo,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = classRaceModifier
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
                    ArmorClassDisplay(
                        armorClass = armorClass
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = formatModifier(proficiencyBonus),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.proficiency_bonus).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = speed.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.char_speed).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                )
            }
        },
        actions = {
            IconButton(
                onClick = { onSettingsNavigate() }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Options",
                )
            }
        }
    )
}

@Composable
fun CharacterDetailsRowExpanded(
    initiativeBonus: Int,
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
                onClick = { onRollClick("${DiceRoles.D20.roll}${formatModifier(initiativeBonus)}") },
                border = BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.tertiary
                ),
                shape = MaterialTheme.shapes.extraSmall,
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.tertiary
                ),
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = formatModifier(initiativeBonus),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = stringResource(R.string.initiative).uppercase()
                    .replaceFirst(" ", "\n"),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
fun CharacterSheetBottomSheets(
    activeSheet: CharacterSheetConfig?,
    character: Character,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onCloseSheet: () -> Unit,
    onUpdateCharacter: (Character) -> Unit,
    updateAbility: (Ability, Int) -> Unit,
    saveAttack: (Attack) -> Unit,
    deleteAttack: (Attack) -> Unit
) {
    activeSheet?.let { sheetConfig ->
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            properties = ModalBottomSheetProperties(
                shouldDismissOnBackPress = false
            )
        ) {
            when (sheetConfig) {
                is CharacterSheetConfig.EditAbility -> {
                    AbilityEditSheetContent(
                        ability = sheetConfig.ability,
                        abilityModifier = character.abilityBlock.getModifier(sheetConfig.ability),
                        currentValue = character.abilityBlock.getScore(sheetConfig.ability),
                        onDismiss = onCloseSheet,
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
                        onDismiss = onCloseSheet,
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
                        onDismiss = onCloseSheet,
                        onSave = { result -> saveAttack(result) },
                        onDelete = { deleteAttack(it) }
                    )
                }

                is CharacterSheetConfig.ViewSpell -> {
                    SpellInfoSheet(
                        spell = sheetConfig.spell,
                        onDismiss = onCloseSheet,
                    )
                }
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