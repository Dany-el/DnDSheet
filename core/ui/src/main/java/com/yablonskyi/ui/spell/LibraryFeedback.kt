package com.yablonskyi.ui.spell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yablonskyi.ui.R

@Composable
fun LibraryFeedback(
    isLoading: Boolean,
    isEmpty: Boolean,
    hasSearch: Boolean,
    hasFilters: Boolean = false,
    onClearSearch: () -> Unit,
    onClearFilters: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when {
            isLoading -> {
                //            CircularProgressIndicator()
//            Text(stringResource(R.string.compendium_loading))
            }

            !isLoading && (hasFilters || hasSearch) -> {
                Text(
                    stringResource(R.string.compendium_no_results),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                if (hasSearch) TextButton(onClick = onClearSearch) { Text(stringResource(R.string.compendium_clear_search)) }
                if (hasFilters) TextButton(onClick = onClearFilters) { Text(stringResource(R.string.compendium_clear_filters)) }
            }

            !isLoading && isEmpty -> {
//                Text(
//                    stringResource(R.string.compendium_empty),
//                    style = MaterialTheme.typography.titleMedium,
//                    textAlign = TextAlign.Center
//                )
            }
        }
    }
}