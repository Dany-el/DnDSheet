package com.yablonskyi.data.rulebook

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.yablonskyi.data.R
import com.yablonskyi.data.entity.CharacterClassEntity
import com.yablonskyi.data.entity.RaceEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BuiltInRulebookLoader @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) {
    private var cache: RulebookData? = null

    private enum class RulebookType(
        val typeName: String
    ) {
        RACE("races"),
        CLASSES("classes")
    }

    fun getRaces(): List<RaceEntity> = load().races
    fun getClasses(): List<CharacterClassEntity> = load().classes

    private fun load(): RulebookData {
        cache?.let { return it }

        val json = context.resources
            .openRawResource(R.raw.dnd_data)
            .bufferedReader()
            .use { it.readText() }

        val root = Gson().fromJson(json, JsonObject::class.java)
        val races = Gson().fromJson<List<RaceEntity>>(
            root.getAsJsonArray(RulebookType.RACE.typeName),
            object : TypeToken<List<RaceEntity>>() {}.type
        )
        val classes = Gson().fromJson<List<CharacterClassEntity>>(
            root.getAsJsonArray(RulebookType.CLASSES.typeName),
            object : TypeToken<List<CharacterClassEntity>>() {}.type
        )

        return RulebookData(races, classes).also { cache = it }
    }

    fun invalidateCache() { cache = null }
}

data class RulebookData(
    val races: List<RaceEntity>,
    val classes: List<CharacterClassEntity>
)
