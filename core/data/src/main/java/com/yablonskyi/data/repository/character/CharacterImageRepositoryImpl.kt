package com.yablonskyi.data.repository.character

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import androidx.core.net.toUri
import com.yablonskyi.data.di.CharacterIoDispatcher
import com.yablonskyi.data.utils.getRotationDegrees
import com.yablonskyi.data.utils.rotateBitmap
import com.yablonskyi.domain.backup.BackupAccessGate
import com.yablonskyi.domain.character.CharacterChange
import com.yablonskyi.domain.repository.CharacterImageRepository
import com.yablonskyi.domain.repository.CharacterRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterImageRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val characters: CharacterRepository,
    private val backupGate: BackupAccessGate,
    @param:CharacterIoDispatcher private val io: CoroutineDispatcher,
) : CharacterImageRepository {
    private val replacements = Mutex()

    override suspend fun replace(characterId: Long, uri: String) = backupGate.access {
        replacements.withLock { withContext(io) {
            val current = characters.getCharacterById(characterId).first()
                ?: error("Character no longer exists")
            val source = uri.toUri()
            val original = context.contentResolver.openInputStream(source)
                ?.use { BitmapFactory.decodeStream(it) }
                ?: throw IOException("Cannot decode image")
            val ratio = minOf(1f, 500f / maxOf(original.width, original.height))
            val scaled = original.scale(
                (original.width * ratio).toInt().coerceAtLeast(1),
                (original.height * ratio).toInt().coerceAtLeast(1)
            )
            if (scaled !== original) original.recycle()
            val image = rotateBitmap(scaled, getRotationDegrees(context, source))
            val file = File(context.filesDir, "char_img_${UUID.randomUUID()}.jpg")
            var committed = false
            try {
                file.outputStream().use {
                    if (!image.compress(
                            Bitmap.CompressFormat.JPEG,
                            80,
                            it
                        )
                    ) throw IOException("Cannot encode image")
                }
                characters.applyChange(characterId, CharacterChange.Image(file.absolutePath))
                committed = true
                current.imagePath?.let { oldPath ->
                    val old = File(oldPath)
                    if (old.parentFile?.canonicalFile == context.filesDir.canonicalFile && old.name.startsWith(
                            "char_img_"
                        )
                    ) old.delete()
                }
            } finally {
                image.recycle()
                if (!committed) file.delete()
            }
            Unit
        } }
    }
}
