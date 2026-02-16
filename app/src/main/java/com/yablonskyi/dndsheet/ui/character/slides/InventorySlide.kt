package com.yablonskyi.dndsheet.ui.character.slides

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.CurrencyHelper
import kotlin.math.roundToInt

@Composable
fun InventorySlide(
    coins: Double,
    inventory: String,
    onCoinChange: (Double) -> Unit,
    onSaveText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Coins
            item {
                MoneyPouch(
                    totalCoins = coins,
                    onCoinsChange = onCoinChange
                )
            }
            // Inventory
            item {
                OutlinedTextFieldWithValue(
                    label = stringResource(R.string.inventory),
                    value = inventory,
                    onSaveText = onSaveText,
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

        OutlinedTextField(
            value = amount.toString(),
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    val newInt = newValue.toIntOrNull() ?: 0
                    onAmountChange(newInt)
                }
            },
            label = { Text(name) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
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
    totalCoins: Double,
    onCoinsChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    fun calculateNewTotal(gold: Int, silver: Int, copper: Int): Double {
        val g = gold.toDouble()
        val s = silver.toDouble() / 10.0
        val c = copper.toDouble() / 100.0
        return (((g + s + c) * 100.0).roundToInt() / 100.0)
    }

    val purse = remember(totalCoins) { CurrencyHelper.parse(totalCoins) }

    Surface(
        color = OutlinedTextFieldDefaults.colors().unfocusedContainerColor,
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // --- GOLD ---
            CoinRow(
                name = stringResource(R.string.gold),
                amount = purse.gold,
                icon = R.drawable.ic_gold_coin,
                onAmountChange = { newGold ->
                    onCoinsChange(calculateNewTotal(newGold, purse.silver, purse.copper))
                },
                onAdd = { onCoinsChange(CurrencyHelper.modify(totalCoins, 1.0)) },
                onSubtract = { onCoinsChange(CurrencyHelper.modify(totalCoins, -1.0)) }
            )

            // --- SILVER ---
            CoinRow(
                name = stringResource(R.string.silver),
                amount = purse.silver,
                icon = R.drawable.ic_silver_coin,
                onAmountChange = { newSilver ->
                    onCoinsChange(calculateNewTotal(purse.gold, newSilver, purse.copper))
                },
                onAdd = { onCoinsChange(CurrencyHelper.modify(totalCoins, 0.1)) },
                onSubtract = { onCoinsChange(CurrencyHelper.modify(totalCoins, -0.1)) }
            )

            // --- COPPER ---
            CoinRow(
                name = stringResource(R.string.copper),
                amount = purse.copper,
                icon = R.drawable.ic_copper_coin,
                onAmountChange = { newCopper ->
                    onCoinsChange(calculateNewTotal(purse.gold, purse.silver, newCopper))
                },
                onAdd = { onCoinsChange(CurrencyHelper.modify(totalCoins, 0.01)) },
                onSubtract = { onCoinsChange(CurrencyHelper.modify(totalCoins, -0.01)) }
            )
        }
    }
}

@Preview
@Composable
private fun InventorySlidePreview() {
    DnDSheetTheme {
        InventorySlide(
            coins = 10.63,
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