package com.yablonskyi.data.rulebook

import android.content.Context
import com.yablonskyi.data.R
import com.yablonskyi.data.entity.CharacterClassEntity
import com.yablonskyi.data.entity.RaceEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

class BuiltInRulebookLoader @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    private var cache: RulebookData? = null

    fun getRaces(): List<RaceEntity> = load().races

    fun getClasses(): List<CharacterClassEntity> = load().classes

    private fun load(): RulebookData {
        cache?.let { return it }

        val content = context.resources
            .openRawResource(R.raw.dnd_data)
            .bufferedReader()
            .use { it.readText() }

        return json.decodeFromString<RulebookData>(content)
            .also { cache = it }
    }

    fun invalidateCache() {
        cache = null
    }
}

@Serializable
data class RulebookData(
    val races: List<RaceEntity>,
    val classes: List<CharacterClassEntity>
)