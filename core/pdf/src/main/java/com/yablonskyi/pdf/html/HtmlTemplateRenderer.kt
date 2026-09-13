package com.yablonskyi.pdf.html

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

fun interface HtmlTemplateRenderer {
    fun render(data: Map<String, Any>, languageCode: String): String
}

@Singleton
class PythonHtmlTemplateRenderer @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : HtmlTemplateRenderer {
    override fun render(data: Map<String, Any>, languageCode: String): String {
        synchronized(startupLock) {
            if (!Python.isStarted()) Python.start(AndroidPlatform(context))
        }
        return Python.getInstance().getModule("character_sheet_generator")
            // Pass JSON so nested Kotlin maps/lists become native Python containers for Jinja.
            .callAttr("render_character_sheet_json", JSONObject(data).toString(), languageCode).toString()
    }

    private companion object { val startupLock = Any() }
}
