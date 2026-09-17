package com.yablonskyi.data.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.util.Base64
import com.yablonskyi.model.character.CharacterSheet
import com.yablonskyi.model.character.Spell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File
import java.util.UUID

private val spellJson = kotlinx.serialization.json.Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

suspend fun exportSpellsToJson(context: Context, uri: Uri, spells: List<Spell>): Result<Unit> {
    return withContext(Dispatchers.IO) {
        try {
            val jsonString = spellJson.encodeToString(spells)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

suspend fun importSpellsFromJson(
    context: Context,
    uri: Uri,
    errorMessage: String = "Could not open file stream"
): Result<List<Spell>> {
    return withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val jsonString = inputStream.bufferedReader().use { it.readText() }

                val spells: List<Spell> = spellJson.decodeFromString(jsonString)

                Result.success(spells)
            } ?: Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

suspend fun exportCharactersToJson(
    context: Context,
    uri: Uri,
    sheets: List<CharacterSheet>
): Result<Unit> {
    return withContext(Dispatchers.IO) {
        try {
            val sheetsForExport = sheets.map { sheet ->
                val base64String = encodeImageToBase64(sheet.character.imagePath)

                sheet.copy(
                    character = sheet.character.copy(imagePath = base64String)
                )
            }

            val jsonString = CharacterBackupCodec.encode(sheetsForExport)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

suspend fun importCharactersFromJson(
    context: Context,
    uri: Uri,
    errorMessage: String = "Could not open file stream"
): Result<List<CharacterSheet>> {
    return withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val jsonString = inputStream.bufferedReader().use { it.readText() }

                val importedSheets: List<CharacterSheet> = CharacterBackupCodec.decode(jsonString)

                val restoredSheets = importedSheets.map { sheet ->
                    val newLocalPath = decodeBase64ToImage(context, sheet.character.imagePath)

                    sheet.copy(
                        character = sheet.character.copy(imagePath = newLocalPath)
                    )
                }

                Result.success(restoredSheets)
            } ?: Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

fun encodeImageToBase64(path: String?): String? {
    if (path.isNullOrEmpty()) return null
    return try {
        val file = File(path)
        if (file.exists()) {
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun decodeBase64ToImage(context: Context, base64String: String?): String? {
    if (base64String.isNullOrEmpty()) return null

    if (base64String.startsWith("/data/") || base64String.startsWith("/storage/")) {
        return base64String
    }

    return try {
        val bytes = Base64.decode(base64String, Base64.DEFAULT)

        val fileName = "char_img_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, fileName)

        file.writeBytes(bytes)

        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getRotationDegrees(context: Context, uri: Uri): Int {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val exif = ExifInterface(inputStream)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } ?: 0
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }
}

fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
    if (degrees == 0) return bitmap

    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

    if (rotatedBitmap != bitmap) {
        bitmap.recycle()
    }

    return rotatedBitmap
}
