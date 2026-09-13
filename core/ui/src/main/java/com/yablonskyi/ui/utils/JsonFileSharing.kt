package com.yablonskyi.ui.utils

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

internal suspend fun prepareJsonShareIntent(
    context: Context,
    effect: FileOperationEffect.ShareReady,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    uriProvider: (File) -> Uri = { file ->
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    },
): Intent = withContext(ioDispatcher) {
    val directory = File(context.cacheDir, "shared_json/${UUID.randomUUID()}")
    check(directory.mkdirs()) { "Could not create sharing directory" }
    val name = effect.fileName.removeSuffix(".json")
        .replace(Regex("[\\\\/:*?\"<>|\\p{Cntrl}]"), "")
        .trim().trim('.').take(100).ifBlank { "item" } + ".json"
    val file = File(directory, name)
    file.writeText(effect.json, Charsets.UTF_8)
    val uri = uriProvider(file)
    Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newRawUri(name, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}
