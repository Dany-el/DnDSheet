package com.yablonskyi.dndsheet.ui.character.slides

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme

@Composable
fun FeaturesSlide(
    traits: String,
    feats: String,
    proficiencies: String,
    updateTraits: (String) -> Unit,
    updateFeats: (String) -> Unit,
    updateProficiencies: (String) -> Unit,
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
            // Traits
            item {
                OutlinedTextFieldWithValue(
                    label = stringResource(R.string.traits),
                    value = traits,
                    onSaveText = updateTraits,
                )
            }
            // Feats
            item {
                OutlinedTextFieldWithValue(
                    label = stringResource(R.string.feats),
                    value = feats,
                    onSaveText = updateFeats,
                )
            }
            // Proficiencies
            item {
                OutlinedTextFieldWithValue(
                    label = stringResource(R.string.proficiencies),
                    value = proficiencies,
                    onSaveText = updateProficiencies,
                )
            }
        }
    }
}

@Composable
fun OutlinedTextFieldWithValue(
    label: String,
    value: String,
    onSaveText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    var text by remember {
        mutableStateOf(value)
    }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(text = label, style = MaterialTheme.typography.labelLarge) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                onSaveText(text)
            }
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Preview
@Composable
private fun FeaturesSlidePreview() {
    DnDSheetTheme {
        FeaturesSlide(
            traits = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec eleifend scelerisque tellus et porta. Aliquam rhoncus ante et enim consequat faucibus.",
            feats = "Actor",
            proficiencies = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec eleifend scelerisque tellus et porta. Aliquam rhoncus ante et enim consequat faucibus. Integer eget neque ornare elit vehicula posuere lacinia vitae nisl. Aliquam lorem ipsum, gravida sed viverra vitae, commodo suscipit enim. Nam finibus dolor venenatis, eleifend metus at, ultricies leo. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed et urna enim. Curabitur varius mauris eget libero congue, quis porta nisl elementum. Fusce ac tincidunt eros. Aliquam egestas posuere libero, quis vestibulum nunc ultrices nec. Ut sit amet ante non ligula vestibulum condimentum at sed elit. In congue efficitur sagittis. Etiam pulvinar consequat ligula eget cursus. Nullam in dapibus lectus. Vestibulum ligula libero, vulputate at risus at, elementum dictum ligula. ",
            {},
            {},
            {}
        )
    }
}