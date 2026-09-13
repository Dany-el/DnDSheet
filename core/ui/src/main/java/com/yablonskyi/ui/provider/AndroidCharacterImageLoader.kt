package com.yablonskyi.ui.provider

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidCharacterImageLoader @Inject constructor(
    @param:ApplicationContext private val context: Context
) : CharacterImageLoader {
    override fun loadImageBytes(imagePath: String?): ByteArray? {
        if (imagePath.isNullOrBlank()) return null

        return try {
            if (imagePath.startsWith("content://")) {
                val uri = imagePath.toUri()
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } else {
                val file = File(imagePath)
                if (file.exists()) {
                    file.readBytes()
                } else {
                    Log.e("PdfGen", "File does not exist at path: $imagePath")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("PdfGen", "Failed to load character image: $imagePath", e)
            null
        }
    }
}
