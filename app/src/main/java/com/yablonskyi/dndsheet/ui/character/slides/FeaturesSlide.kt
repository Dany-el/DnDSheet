package com.yablonskyi.dndsheet.ui.character.slides

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme

@OptIn(ExperimentalLayoutApi::class)
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
    val focusManager = LocalFocusManager.current

    Box(
        modifier
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                OutlinedTextFieldWithValue(
                    label = stringResource(R.string.traits),
                    value = traits,
                    onSaveText = updateTraits,
                )
            }
            item {
                OutlinedTextFieldWithValue(
                    label = stringResource(R.string.feats),
                    value = feats,
                    onSaveText = updateFeats
                )
            }
            item {
                OutlinedTextFieldWithValue(
                    label = stringResource(R.string.proficiencies),
                    value = proficiencies,
                    onSaveText = updateProficiencies
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
    modifier: Modifier = Modifier,
    maxLines: Int = 5
) {
    val focusManager = LocalFocusManager.current

    var text by remember {
        mutableStateOf(value)
    }

    var isFocused by remember { mutableStateOf(false) }

    BackHandler(enabled = isFocused) {
        focusManager.clearFocus()
    }

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onSaveText(text)
        },
        label = { Text(text = label, style = MaterialTheme.typography.labelLarge) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Default,
            capitalization = KeyboardCapitalization.Sentences
        ),
        minLines = maxLines,
        maxLines = maxLines,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            },
    )
}

@Preview
@Composable
private fun FeaturesSlidePreview() {
    DnDSheetTheme {
        FeaturesSlide(
            traits = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec eleifend scelerisque tellus et porta. Aliquam rhoncus ante et enim consequat faucibus. Integer eget neque ornare elit vehicula posuere lacinia vitae nisl. Aliquam lorem ipsum, gravida sed viverra vitae, commodo suscipit enim. Nam finibus dolor venenatis, eleifend metus at, ultricies leo. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed et urna enim. Curabitur varius mauris eget libero congue, quis porta nisl elementum. Fusce ac tincidunt eros. Aliquam egestas posuere libero, quis vestibulum nunc ultrices nec. Ut sit amet ante non ligula vestibulum condimentum at sed elit. In congue efficitur sagittis. Etiam pulvinar consequat ligula eget cursus. Nullam in dapibus lectus. Vestibulum ligula libero, vulputate at risus at, elementum dictum ligula. ",
            feats = "Actor",
            proficiencies = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec eleifend scelerisque tellus et porta. Aliquam rhoncus ante et enim consequat faucibus. Integer eget neque ornare elit vehicula posuere lacinia vitae nisl. Aliquam lorem ipsum, gravida sed viverra vitae, commodo suscipit enim. Nam finibus dolor venenatis, eleifend metus at, ultricies leo. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed et urna enim. Curabitur varius mauris eget libero congue, quis porta nisl elementum. Fusce ac tincidunt eros. Aliquam egestas posuere libero, quis vestibulum nunc ultrices nec. Ut sit amet ante non ligula vestibulum condimentum at sed elit. In congue efficitur sagittis. Etiam pulvinar consequat ligula eget cursus. Nullam in dapibus lectus. Vestibulum ligula libero, vulputate at risus at, elementum dictum ligula. ",
            {},
            {},
            {}
        )
    }
}