package com.yablonskyi.domain.provider

interface AppVersionProvider {
    val versionName: String
    val versionCode: Long
}