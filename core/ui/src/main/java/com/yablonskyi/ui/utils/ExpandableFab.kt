package com.yablonskyi.ui.utils

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import com.yablonskyi.ui.R
import com.yablonskyi.ui.theme.Dimens

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExpandableFab(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onLoad: () -> Unit,
    onSave: () -> Unit,
    onCreate: () -> Unit,
    saveEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = expanded) { onExpandedChange(false) }

    fun perform(action: () -> Unit) {
        onExpandedChange(false)
        action()
    }

    val focusRequester = remember { FocusRequester() }

    val stringOptions = stringResource(R.string.options)

    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = expanded,
        button = {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    if (expanded) TooltipAnchorPosition.Start else TooltipAnchorPosition.Above
                ),
                tooltip = {
                    PlainTooltip(
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Assertive
                            paneTitle = stringOptions
                        }
                    ) {
                        Text(stringResource(R.string.options))
                    }
                },
                state = rememberTooltipState(),
            ) {
                ToggleFloatingActionButton(
                    checked = expanded,
                    onCheckedChange = { onExpandedChange(!expanded) },
                    containerSize = { Dimens.Fab.Size },
                    modifier = Modifier
                        .semantics {
                            traversalIndex = -1f
                            stateDescription = if (expanded) "Expanded" else "Collapsed"
                            contentDescription = stringOptions
                        }
                        .testTag("library_fab")
                        .focusRequester(focusRequester),
                ) {
                    val imageVector by remember {
                        derivedStateOf {
                            if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                        }
                    }
                    Icon(
                        painter = rememberVectorPainter(imageVector),
                        contentDescription = null,
                        modifier = Modifier.animateIcon({ checkedProgress })
                    )
                }
            }
        }
    ) {
        // Load — first item, gets keyboard back-navigation to FAB
        FloatingActionButtonMenuItem(
            onClick = { perform(onLoad) },
            icon = { Icon(Icons.Default.Download, contentDescription = null) },
            text = { Text(stringResource(R.string.confirm_import)) },
            modifier = Modifier
                .semantics { isTraversalGroup = true }
                .onKeyEvent {
                    if (
                        it.type == KeyEventType.KeyDown &&
                        (it.key == Key.DirectionUp ||
                                it.key == Key.NumPadDirectionUp ||
                                (it.isShiftPressed && it.key == Key.Tab))
                    ) {
                        focusRequester.requestFocus()
                        return@onKeyEvent true
                    }
                    return@onKeyEvent false
                }
        )

        // Save — conditional
        if (saveEnabled) {
            FloatingActionButtonMenuItem(
                onClick = { perform(onSave) },
                icon = { Icon(Icons.Default.Save, contentDescription = null) },
                text = { Text(stringResource(R.string.export)) },
                modifier = Modifier.semantics { isTraversalGroup = true }
            )
        }

        // Create — last item, gets close accessibility action
        FloatingActionButtonMenuItem(
            onClick = { perform(onCreate) },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.create)) },
            modifier = Modifier
                .semantics {
                isTraversalGroup = true
                customActions = listOf(
                    CustomAccessibilityAction(
                        label = "Close menu",
                        action = {
                            onExpandedChange(false)
                            true
                        }
                    )
                )
            }
        )
    }
}