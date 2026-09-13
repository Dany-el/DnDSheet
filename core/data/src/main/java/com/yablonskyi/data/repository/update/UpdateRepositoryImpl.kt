package com.yablonskyi.data.repository.update

import android.content.Context
import com.yablonskyi.domain.provider.AppVersionProvider
import com.yablonskyi.domain.repository.UpdateRepository
import com.yablonskyi.model.update.AppUpdate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject

class UpdateRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appVersionProvider: AppVersionProvider
) : UpdateRepository {
    private val client = OkHttpClient()

    override suspend fun fetchUpdate(): AppUpdate? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://raw.githubusercontent.com/Dany-el/DnDSheet/master/version.json")
                .build()

            val body = client.newCall(request).execute().body.string()
            val unknownKeys = Json { ignoreUnknownKeys = true }
            val update = unknownKeys.decodeFromString<AppUpdate>(body)
            if (update.versionCode > appVersionProvider.versionCode) update else null
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun downloadApk(
        url: String,
        onProgress: (Float) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(Request.Builder().url(url).build()).execute()
            val body = response.body
            val total = body.contentLength()
            var downloaded = 0L

            val file = File(context.getExternalFilesDir("updates"), "update.apk")
            file.outputStream().use { outputStream ->
                body.byteStream().use { inputStream ->
                    val buffer = ByteArray(8 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                        downloaded += read
                        if (total > 0) onProgress(downloaded / total.toFloat())
                    }
                }
            }
            file

        } catch (_: Exception) {
            null
        }
    }
}