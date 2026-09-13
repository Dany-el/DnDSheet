package com.yablonskyi.ui.utils

import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class JsonFileSharingTest {
    @Test
    fun `sharing creates distinct UTF8 attachments with read permission`() = runTest {
        val files = mutableListOf<File>()
        val context = RuntimeEnvironment.getApplication()
        val payload = "[{\"name\":\"Ельф\"}]"
        val uri = Uri.parse("content://test.provider/shared_json/item.json")
        val effect = FileOperationEffect.ShareReady("../bad\\name:?.json", payload)
        suspend fun prepare() = prepareJsonShareIntent(
            context, effect, StandardTestDispatcher(testScheduler),
            uriProvider = { file -> files += file; uri },
        )
        val intent = prepare()
        prepare()
        assertNotEquals(files[0].parent, files[1].parent)
        files.forEach { file ->
            assertEquals("badname.json", file.name)
            assertEquals(payload, file.readText(Charsets.UTF_8))
            assertTrue(file.canonicalPath.startsWith(File(context.cacheDir, "shared_json").canonicalPath + File.separator))
        }
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("application/json", intent.type)
        assertEquals(uri, intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
        assertEquals(uri, intent.clipData!!.getItemAt(0).uri)
        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test
    fun `empty sanitized filename uses item json`() = runTest {
        var attachment: File? = null
        prepareJsonShareIntent(
            RuntimeEnvironment.getApplication(),
            FileOperationEffect.ShareReady("/?:.json", "[]"),
            StandardTestDispatcher(testScheduler),
            uriProvider = { attachment = it; Uri.parse("content://test.provider/item.json") },
        )
        assertEquals("item.json", attachment!!.name)
    }
}
