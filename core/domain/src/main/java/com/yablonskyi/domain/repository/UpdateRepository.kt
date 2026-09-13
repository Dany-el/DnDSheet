package com.yablonskyi.domain.repository

import com.yablonskyi.model.update.AppUpdate
import java.io.File

interface UpdateRepository {
    suspend fun fetchUpdate(): AppUpdate?
    suspend fun downloadApk(url: String, onProgress: (Float) -> Unit): File?
}
