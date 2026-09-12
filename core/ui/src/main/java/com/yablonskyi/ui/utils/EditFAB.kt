package com.yablonskyi.ui.utils

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yablonskyi.ui.R
import com.yablonskyi.ui.theme.Dimens

@Composable
fun EditFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(Dimens.Fab.Size)
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = stringResource(R.string.edit)
        )
    }
}