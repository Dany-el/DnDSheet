package com.yablonskyi.dndsheet.data.update

import kotlinx.serialization.Serializable

@Serializable
data class AppUpdate(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String,
)