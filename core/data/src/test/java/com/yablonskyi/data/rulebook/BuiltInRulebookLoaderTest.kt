package com.yablonskyi.data.rulebook

import android.content.Context
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import androidx.test.core.app.ApplicationProvider
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BuiltInRulebookLoaderTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val loader = BuiltInRulebookLoader(context)

    @Test
    fun builtInRulebook_decodesRacesAndClasses() {
        val races = loader.getRaces()
        val classes = loader.getClasses()

        assertTrue(races.isNotEmpty())
        assertTrue(classes.isNotEmpty())
        assertTrue(races.all { it.id.isNotBlank() && it.name.isNotBlank() })
        assertTrue(classes.all { it.id.isNotBlank() && it.name.isNotBlank() })
    }
}