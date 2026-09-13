package com.yablonskyi.character.presentation.sheet.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yablonskyi.character.presentation.sheet.model.CharacterTab
import com.yablonskyi.model.character.Character

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
    lessDetails: Boolean,
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
        AnimatedVisibility(
            visible = !lessDetails
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
        }

        Spacer(Modifier.height(8.dp))

        val currentTab by remember {
            derivedStateOf { CharacterTab.getByIndex(pagerState.currentPage) }
        }

        SlideSelector(
            tabs = tabs,
            currentTab = currentTab,
            onTabSelected = onTabSelected,
            modifier = Modifier.align(Alignment.CenterHorizontally)
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
fun WideCharacterLayout(
    character: Character,
    leftSelectedTab: CharacterTab,
    rightSelectedTab: CharacterTab,
    onLeftTabSelected: (CharacterTab) -> Unit,
    onRightTabSelected: (CharacterTab) -> Unit,
    onDiceButtonClick: (String) -> Unit,
    onRestClick: () -> Unit,
    onHealthClick: () -> Unit,
    tabContent: @Composable (CharacterTab, Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
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
                    .align(Alignment.Top)
                    .padding(vertical = 4.dp)
                    .padding(end = 8.dp)
            ) {
                SlideSelector(
                    tabs = CharacterTab.entries,
                    currentTab = leftSelectedTab,
                    onTabSelected = onLeftTabSelected,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(16.dp))
                HealthBar(
                    currentHp = character.currentHp,
                    maxHp = character.maxHp,
                    tempHp = character.tempHp,
                    onHealthClick = onHealthClick,
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
                    initiativeBonus = character.getInitiativeBonus(),
                    onRollClick = onDiceButtonClick,
                    onRestClick = onRestClick,
                    modifier = Modifier.weight(1f)
                )

                SlideSelector(
                    tabs = CharacterTab.entries,
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Crossfade(
                    targetState = leftSelectedTab,
                    modifier = Modifier.align(Alignment.TopCenter),
                    label = "LeftPaneAnimation"
                ) { currentTab ->
                    tabContent(currentTab, Modifier)
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Crossfade(
                    targetState = rightSelectedTab,
                    modifier = Modifier.align(Alignment.TopCenter),
                    label = "RightPaneAnimation"
                ) { currentTab ->
                    tabContent(currentTab, Modifier)
                }
            }
        }
    }
}
