package com.yablonskyi.data.repository.character

import android.content.Context
import android.util.Base64
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yablonskyi.data.di.CharacterIoDispatcher
import com.yablonskyi.data.utils.encodeImageToBase64
import com.yablonskyi.domain.repository.CharacterFileRepository
import com.yablonskyi.domain.repository.CharacterRepository
import com.yablonskyi.model.character.CharacterSheet
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class CharacterFileRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val characters: CharacterRepository,
    @param:CharacterIoDispatcher private val io: CoroutineDispatcher,
) : CharacterFileRepository {
    override suspend fun read(uri: String): List<CharacterSheet> = withContext(io) {
        val input = context.contentResolver.openInputStream(uri.toUri())
            ?: throw IOException("Cannot open document")
        input.bufferedReader().use { reader ->
            val type = object : TypeToken<List<CharacterSheet>>() {}.type
            val sheets: List<CharacterSheet> =
                Gson().fromJson(reader, type) ?: throw IOException("Invalid character document")
            // Gson can bypass Kotlin's non-null constructors: validate before confirmation/persistence.
            sheets.forEach { sheet ->
                requireNotNull(sheet.character)
                requireNotNull(sheet.character.name)
                requireNotNull(sheet.character.abilityBlock)
                requireNotNull(sheet.character.spellSettings)
                requireNotNull(sheet.spells)
                requireNotNull(sheet.attacks)
            }
            sheets
        }
    }

    override suspend fun import(sheets: List<CharacterSheet>) = withContext(io) {
        val created = mutableListOf<File>()
        var committed = false
        try {
            val restored = sheets.map { sheet ->
                val path = sheet.character.imagePath?.takeIf { it.isNotBlank() }?.let { encoded ->
                    val file = File(context.filesDir, "char_img_${UUID.randomUUID()}.jpg")
                    created += file
                    file.writeBytes(Base64.decode(encoded, Base64.DEFAULT))
                    file.absolutePath
                }
                sheet.copy(character = sheet.character.copy(imagePath = path))
            }
            characters.insertCharacters(restored)
            committed = true
        } finally {
            if (!committed) created.forEach { it.delete() }
        }
    }

    override suspend fun export(uri: String, characterIds: List<Long>) = withContext(io) {
        val sheets = characters.getCharacterSheetsByIds(characterIds)
        require(sheets.isNotEmpty()) { "No characters to save" }
        val exported = sheets.map { sheet ->
            sheet.copy(character = sheet.character.copy(imagePath = encodeImageToBase64(sheet.character.imagePath)))
        }
        val output = context.contentResolver.openOutputStream(uri.toUri())
            ?: throw IOException("Cannot open destination")
        output.bufferedWriter().use { it.write(Gson().toJson(exported)) }
    }
}
