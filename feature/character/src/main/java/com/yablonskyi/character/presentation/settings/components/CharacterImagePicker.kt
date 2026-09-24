package com.yablonskyi.character.presentation.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yablonskyi.ui.R

@Composable
fun CharacterProfilePicturePicker(
    currentImagePath: String?,
    onImagePicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (currentImagePath == null) {
            Text(
                text = stringResource(R.string.change_character_profile_picture),
                style = MaterialTheme.typography.labelSmall
            )
        }
        OutlinedCard(
            onClick = onImagePicker,
            shape = MaterialTheme.shapes.extraSmall,
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (currentImagePath != null) {
                    AsyncImage(
                        model = currentImagePath,
                        contentDescription = stringResource(R.string.character_profile),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.NoPhotography,
                        contentDescription = stringResource(R.string.add_photo),
                        modifier = Modifier
                            .padding(32.dp)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}
