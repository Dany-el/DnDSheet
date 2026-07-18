package com.yablonskyi.dndsheet.data.update

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.yablonskyi.dndsheet.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject

class UpdateRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient()

    suspend fun fetchUpdate(): AppUpdate? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://raw.githubusercontent.com/Dany-el/DnDSheet/master/version.json")
                .build()

            val body = client.newCall(request).execute().body?.string() ?: return@withContext null
            val unknownKeys = Json { ignoreUnknownKeys = true }
            val update = unknownKeys.decodeFromString<AppUpdate>(body)
            if (update.versionCode > BuildConfig.VERSION_CODE) update else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun downloadApk(
        url: String,
        onProgress: (Float) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(Request.Builder().url(url).build()).execute()
            val body = response.body ?: return@withContext null
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

    fun installApk(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }
}