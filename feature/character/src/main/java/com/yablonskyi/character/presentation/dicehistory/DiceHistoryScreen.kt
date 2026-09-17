package com.yablonskyi.character.presentation.dicehistory

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yablonskyi.dice.DiceIntent
import com.yablonskyi.model.dice.SavedDiceRoll
import com.yablonskyi.ui.R
import com.yablonskyi.ui.theme.Dimens
import com.yablonskyi.ui.utils.formatModifier
import com.yablonskyi.ui.utils.listItemShape
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.abs

@Composable
fun DiceHistoryRouteContent(
    onBack: () -> Unit,
    viewModel: DiceHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DiceHistoryScreen(state, viewModel::onIntent, onBack)
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiceHistoryScreen(
    state: DiceHistoryState,
    onIntent: (DiceIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showClearConfirmation by remember { mutableStateOf(false) }

    val elevatedSurfaceColor =
        MaterialTheme.colorScheme.surfaceColorAtElevation(Dimens.TopBar.Elevation)

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.dice_history)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showClearConfirmation = true },
                        enabled = state.rolls.isNotEmpty() && !state.isClearing,
                    ) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = stringResource(R.string.clear_dice_history),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = elevatedSurfaceColor,
                    scrolledContainerColor = elevatedSurfaceColor,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    Modifier.testTag(
                        DICE_HISTORY_LOADING_TAG
                    )
                )

                state.rolls.isEmpty() -> Text(
                    stringResource(R.string.dice_history_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.testTag(DICE_HISTORY_EMPTY_TAG),
                )

                else -> {
                    val locale = LocalConfiguration.current.locales[0]
                        ?: LocalLocale.current.platformLocale
                    val dayFormat = remember(locale) {
                        DateFormat.getDateInstance(DateFormat.SHORT, locale)
                    }
                    val rollsByDay = remember(state.rolls, locale) {
                        state.rolls.groupBy { roll -> dayFormat.format(Date(roll.timestamp)) }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(DICE_HISTORY_LIST_TAG),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        rollsByDay.forEach { (day, rolls) ->
                            stickyHeader(key = "day-$day") {
                                Surface(
                                    color = MaterialTheme.colorScheme.background,
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.Start,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = Dimens.Spacing.Small)
                                    ) {
                                        Text(
                                            text = day,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.testTag("$DICE_HISTORY_DAY_HEADER_TAG-$day")
                                        )
                                    }
                                }
                            }
                            itemsIndexed(
                                items = rolls,
                                key = { _, roll -> roll.id },
                            ) { index, roll ->
                                DiceHistoryItem(roll, listItemShape(index, rolls.size))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text(stringResource(R.string.clear_dice_history)) },
            text = { Text(stringResource(R.string.clear_dice_history_confirmation)) },
            confirmButton = {
                TextButton(onClick = {
                    showClearConfirmation = false
                    onIntent(DiceIntent.ClearDiceRolls)
                }) { Text(stringResource(R.string.clear_all)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    state.error?.let {
        AlertDialog(
            onDismissRequest = { onIntent(DiceIntent.DismissPersistenceError) },
            title = { Text(stringResource(R.string.dice_history_error_title)) },
            text = { Text(stringResource(R.string.dice_history_error_message)) },
            confirmButton = {
                TextButton(onClick = { onIntent(DiceIntent.RetryPersistence) }) {
                    Text(stringResource(R.string.character_retry))
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(DiceIntent.DismissPersistenceError) }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun DiceHistoryItem(
    roll: SavedDiceRoll,
    shape: Shape,
) {
    val locale = LocalConfiguration.current.locales[0] ?: LocalLocale.current.platformLocale
    val dateFormat = remember(locale) {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.DEFAULT, locale)
    }

    val color = if (roll.hasCritSuccess) Color.Green
    else if (roll.hasCritFailure) Color.Red
    else null

    val fallbackSurface = MaterialTheme.colorScheme.surfaceContainer

    val solidDimColor = color?.let {
        lerp(start = it, stop = fallbackSurface, fraction = 0.7f)
    } ?: fallbackSurface

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = solidDimColor),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = roll.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(rollNumbersText(roll), style = MaterialTheme.typography.titleSmall)

                val diceLetter = stringResource(R.string.dice_first_letter)
                Text(
                    text = roll.dices.joinToString(" + ") { "${it.count}$diceLetter${it.sides}" } +
                            (roll.modifier?.let(::formatModifier) ?: ""),
                    style = MaterialTheme.typography.labelMedium,
                )

                Text(
                    text = dateFormat.format(Date(roll.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = roll.result.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun rollNumbersText(roll: SavedDiceRoll): String = buildString {
    append(roll.numbers.joinToString(" + "))
    roll.modifier?.let { modifier ->
        append(if (modifier >= 0) " + " else " - ")
        append(abs(modifier))
    }
}

internal const val DICE_HISTORY_LOADING_TAG = "dice_history_loading"
internal const val DICE_HISTORY_EMPTY_TAG = "dice_history_empty"
internal const val DICE_HISTORY_LIST_TAG = "dice_history_list"
internal const val DICE_HISTORY_DAY_HEADER_TAG = "dice_history_day_header"