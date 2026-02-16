package com.yablonskyi.dndsheet.ui.character.slides

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme

@Composable
fun BackstorySlide(
    backstory: String,
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
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Backstory
            item {
                OutlinedTextFieldWithValue(
                    label = stringResource(R.string.backstory),
                    value = backstory,
                    maxLines = 8,
                    onSaveText = {
                        onSaveText(it)
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun BackstorySlidePreview() {
    DnDSheetTheme {
        BackstorySlide(
            backstory = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec eleifend scelerisque tellus et porta. Aliquam rhoncus ante et enim consequat faucibus. Integer eget neque ornare elit vehicula posuere lacinia vitae nisl. Aliquam lorem ipsum, gravida sed viverra vitae, commodo suscipit enim. Nam finibus dolor venenatis, eleifend metus at, ultricies leo. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed et urna enim. Curabitur varius mauris eget libero congue, quis porta nisl elementum. Fusce ac tincidunt eros. Aliquam egestas posuere libero, quis vestibulum nunc ultrices nec. Ut sit amet ante non ligula vestibulum condimentum at sed elit. In congue efficitur sagittis. Etiam pulvinar consequat ligula eget cursus. Nullam in dapibus lectus. Vestibulum ligula libero, vulputate at risus at, elementum dictum ligula. ",
            onSaveText = {}
        )
    }
}