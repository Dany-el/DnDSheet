package com.yablonskyi.dndsheet.ui.character.slides

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Money
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.IntTextField

@Composable
fun InventorySlide(
    coins: Money,
    inventory: String,
    onCoinChange: (Money) -> Unit,
    onSaveText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                    }
                )
            }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Coins
            item {
                MoneyPouch(
                    money = coins,
                    onMoneyChange = onCoinChange
                )
            }
            // Inventory
            item {
                OutlinedTextFieldWithValue(
                    label = stringResource(R.string.inventory),
                    value = inventory,
                    onSaveText = {
                        onSaveText(it)
                    },
                    maxLines = 5
                )
            }
        }
    }
}

@Composable
fun CoinRow(
    name: String,
    amount: Int,
    @DrawableRes icon: Int,
    onAmountChange: (Int) -> Unit,
    onAdd: () -> Unit,
    onSubtract: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        IntTextField(
            value = amount,
            label = name,
            onValueChange = { onAmountChange(it) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            ),
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onSubtract,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_remove),
                    contentDescription = "Subtract",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onAdd,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun MoneyPouch(
    money: Money,
    onMoneyChange: (Money) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = OutlinedTextFieldDefaults.colors().unfocusedContainerColor,
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            CoinRow(
                name = stringResource(R.string.gold),
                amount = money.gold,
                icon = R.drawable.ic_gold_coin,
                onAmountChange = { newGold ->
                    onMoneyChange(money.copy(gold = newGold))
                },
                onAdd = { onMoneyChange(money.copy(gold = money.gold + 1)) },
                onSubtract = { onMoneyChange(money.copy(gold = money.gold - 1)) }
            )

            CoinRow(
                name = stringResource(R.string.silver),
                amount = money.silver,
                icon = R.drawable.ic_silver_coin,
                onAmountChange = { newSilver ->
                    onMoneyChange(money.copy(silver = newSilver))
                },
                onAdd = { onMoneyChange(money.copy(silver = money.silver + 1)) },
                onSubtract = { onMoneyChange(money.copy(silver = money.silver - 1)) }
            )

            CoinRow(
                name = stringResource(R.string.copper),
                amount = money.copper,
                icon = R.drawable.ic_copper_coin,
                onAmountChange = { newCopper ->
                    onMoneyChange(money.copy(copper = newCopper))
                },
                onAdd = { onMoneyChange(money.copy(copper = money.copper + 1)) },
                onSubtract = { onMoneyChange(money.copy(copper = money.copper - 1)) }
            )
        }
    }
}

@Preview
@Composable
private fun InventorySlidePreview() {
    DnDSheetTheme {
        InventorySlide(
            coins = Money(24, 3, 99),
            inventory =
                "    Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n" +
                        "    Cras a ante convallis, condimentum leo non, tempor lorem.\n" +
                        "    Mauris porta enim sed lorem pretium aliquet.\n" +
                        "    Nulla nec mauris sed eros commodo porttitor.\n" +
                        "    In tristique metus in ligula tincidunt vulputate.\n" +
                        "\n" +
                        "    Praesent eget ante sit amet justo venenatis tincidunt at egestas diam.\n" +
                        "    Vivamus ultricies odio ut dictum varius.\n" +
                        "    Proin cursus tellus ac varius eleifend.\n" +
                        "    Nunc non justo pulvinar, pellentesque justo et, tincidunt diam.\n" +
                        "    Etiam vitae leo viverra, vulputate diam sit amet, euismod eros.\n",
            onSaveText = {},
            onCoinChange = {}
        )
    }
}