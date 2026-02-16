package com.yablonskyi.dndsheet.ui.character

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.SpellLevel
import com.yablonskyi.dndsheet.data.model.character.SpellSlot
import com.yablonskyi.dndsheet.domain.repository.CharacterRepository
import com.yablonskyi.dndsheet.ui.navigation.CharacterSettingsRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import androidx.core.graphics.scale
import com.yablonskyi.dndsheet.ui.utils.getRotationDegrees
import com.yablonskyi.dndsheet.ui.utils.rotateBitmap


@HiltViewModel
class CharacterSettingsViewModel @Inject constructor(
    private val repository: CharacterRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args = savedStateHandle.toRoute<CharacterSettingsRoute>()

    val character: StateFlow<Character?> = repository.getCharacterById(args.characterId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun updateCharacter(character: Character) {
        viewModelScope.launch {
            repository.updateCharacter(character)
        }
    }

    fun updateSpellSlot(spellLevel: SpellLevel, newMax: Int) {
        val updatedCharacter = character.value?.let { char ->
            val updatedMap = char.spellSettings.spellSlots.toMutableMap().apply {
                this[spellLevel] = this[spellLevel]?.copy(max = newMax) ?: SpellSlot(max = newMax)
            }

            char.copy(
                spellSettings = char.spellSettings.copy(spellSlots = updatedMap)
            )
        } ?: return

        updateCharacter(updatedCharacter)
    }

    fun updateImage(newImagePath: String) {
        val updatedCharacter = character.value?.copy(imagePath = newImagePath) ?: return
        updateCharacter(updatedCharacter)
    }
}

suspend fun saveImageToInternalStorage(
    context: Context,
    uri: Uri,
    oldImagePath: String? = null
): String? {
    return withContext(Dispatchers.IO) {
        try {
            deleteImageFromInternalStorage(oldImagePath)

            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return@withContext null

            val maxSize = 500
            var width = originalBitmap.width
            var height = originalBitmap.height

            if (width > maxSize || height > maxSize) {
                val ratio = width.toFloat() / height.toFloat()
                if (ratio > 1) {
                    width = maxSize
                    height = (maxSize / ratio).toInt()
                } else {
                    height = maxSize
                    width = (maxSize * ratio).toInt()
                }
            }

            val scaledBitmap = originalBitmap.scale(width, height)

            if (originalBitmap != scaledBitmap) {
                originalBitmap.recycle()
            }

            val rotation = getRotationDegrees(context, uri)
            val finalBitmap = rotateBitmap(scaledBitmap, rotation)

            val fileName = "char_img_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)

            FileOutputStream(file).use { outputStream ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            }

            finalBitmap.recycle()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun deleteImageFromInternalStorage(imagePath: String?) {
    if (imagePath.isNullOrEmpty()) return

    try {
        val file = File(imagePath)
        if (file.exists()) {
            file.delete()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}