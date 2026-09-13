package com.yablonskyi.ui.provider

interface CharacterImageLoader {
    fun loadImageBytes(imagePath: String?): ByteArray?
}
