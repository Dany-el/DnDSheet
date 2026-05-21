package com.yablonskyi.dndsheet.data.rulebook

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.rulebook.CharacterClass
import com.yablonskyi.dndsheet.data.model.rulebook.Race
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BuiltInRulebookLoader @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private var cache: RulebookData? = null

    fun getRaces(): List<Race> = load().races
    fun getClasses(): List<CharacterClass> = load().classes

    private fun load(): RulebookData {
        cache?.let { return it }

        val json = context.resources
            .openRawResource(R.raw.dnd_data)
            .bufferedReader()
            .use { it.readText() }

        val root = Gson().fromJson(json, JsonObject::class.java)
        val races = Gson().fromJson<List<Race>>(
            root.getAsJsonArray("races"),
            object : TypeToken<List<Race>>() {}.type
        )
        val classes = Gson().fromJson<List<CharacterClass>>(
            root.getAsJsonArray("classes"),
            object : TypeToken<List<CharacterClass>>() {}.type
        )

        return RulebookData(races, classes).also { cache = it }
    }

    fun invalidateCache() { cache = null }
}

data class RulebookData(
    val races: List<Race>,
    val classes: List<CharacterClass>
)