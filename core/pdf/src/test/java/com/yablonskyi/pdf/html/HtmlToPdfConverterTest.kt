package com.yablonskyi.pdf.html

import android.app.Activity
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.webkit.TestWebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.net.Uri
import androidx.activity.ComponentActivity
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HtmlToPdfConverterTest {
    @Test fun givenLoadedHtml_whenPrinted_thenSubmitsOnceAndRetainsPageUntilAdapterFinishes() = runTest {
        val host = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val webView = TestWebView(host.get())
        var submissions = 0
        lateinit var adapter: PrintDocumentAdapter
        val printer = HtmlToPdfConverter(StandardTestDispatcher(testScheduler), { webView }) { activity, name, document, attributes ->
            assertSame(host.get(), activity)
            assertEquals("Hero_", name)
            assertEquals(PrintAttributes.MediaSize.ISO_A4, attributes.mediaSize)
            assertEquals(300, attributes.resolution!!.horizontalDpi)
            assertEquals(PrintAttributes.Margins.NO_MARGINS, attributes.minMargins)
            submissions++
            adapter = document
        }
        val result = async { printer.print(host.get(), "<html>Українська</html>", "Hero/") }
        runCurrent()
        assertFalse(result.isCompleted)
        assertEquals("<html>Українська</html>", webView.html)
        assertEquals("UTF-8", webView.encoding)
        assertFalse(webView.settings.javaScriptEnabled)
        assertFalse(webView.settings.allowFileAccess)
        assertFalse(webView.settings.allowContentAccess)
        assertTrue(webView.settings.blockNetworkLoads)
        val client = webView.webViewClient
        client.onPageFinished(webView, "https://localhost/")
        client.onPageFinished(webView, "https://localhost/")
        runCurrent()
        assertTrue(result.await().isSuccess)
        assertEquals(1, submissions)
        assertEquals(0, webView.destroyCount)
        adapter.onFinish()
        adapter.onFinish()
        assertEquals(1, webView.destroyCount)
        assertEquals(1, webView.delegate.finishCount)
        host.pause().stop().destroy()
        assertEquals(1, webView.destroyCount)
    }

    @Test fun givenLoadingPage_whenCancelled_thenCleansUpWithoutSubmitting() = runTest {
        val host = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val webView = TestWebView(host.get())
        val printer = HtmlToPdfConverter(StandardTestDispatcher(testScheduler), { webView }) { _, _, _, _ -> fail("Must not print") }
        val result = async { printer.print(host.get(), "html", "Hero") }
        runCurrent()
        result.cancel()
        runCurrent()
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(1, webView.destroyCount)
        host.pause().stop().destroy()
    }

    @Test fun givenLoadError_whenPageFails_thenReturnsFailureAndIgnoresLateCallback() = runTest {
        val host = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val webView = TestWebView(host.get())
        val printer = HtmlToPdfConverter(StandardTestDispatcher(testScheduler), { webView }) { _, _, _, _ -> fail("Must not print") }
        val result = async { printer.print(host.get(), "html", "Hero") }
        runCurrent()
        val client = webView.webViewClient
        client.onReceivedError(webView, mainFrameRequest(), TestWebResourceError())
        client.onPageFinished(webView, "https://localhost/")
        runCurrent()
        assertTrue(result.await().isFailure)
        assertEquals(1, webView.destroyCount)
        host.pause().stop().destroy()
    }

    @Test fun givenSubmissionFailure_whenPrinting_thenReturnsFailureAndCleansUp() = runTest {
        val host = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val webView = TestWebView(host.get())
        val printer = HtmlToPdfConverter(StandardTestDispatcher(testScheduler), { webView }) { _, _, _, _ -> error("Rejected") }
        val result = async { printer.print(host.get(), "html", "Hero") }
        runCurrent()
        webView.webViewClient.onPageFinished(webView, "https://localhost/")
        runCurrent()
        assertTrue(result.await().isFailure)
        assertEquals(1, webView.destroyCount)
        host.pause().stop().destroy()
    }

    @Test fun givenAcceptedRequest_whenActivityDestroyed_thenReleasesPage() = runTest {
        val host = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val webView = TestWebView(host.get())
        val printer = HtmlToPdfConverter(StandardTestDispatcher(testScheduler), { webView }) { _, _, _, _ -> }
        val result = async { printer.print(host.get(), "html", "Hero") }
        runCurrent()
        webView.webViewClient.onPageFinished(webView, "https://localhost/")
        runCurrent()
        assertTrue(result.await().isSuccess)
        host.pause().stop().destroy()
        assertEquals(1, webView.destroyCount)
    }

    private class TestWebView(activity: Activity) : WebView(activity) {
        var html: String? = null
        var encoding: String? = null
        var destroyCount = 0
        val delegate = TestAdapter()
        override fun loadDataWithBaseURL(baseUrl: String?, data: String, mimeType: String?, encoding: String?, historyUrl: String?) {
            html = data
            this.encoding = encoding
        }
        override fun createPrintDocumentAdapter(documentName: String) = delegate
        override fun destroy() { destroyCount++; super.destroy() }
    }

    private class TestAdapter : PrintDocumentAdapter() {
        var finishCount = 0
        override fun onFinish() { finishCount++ }
        override fun onLayout(oldAttributes: PrintAttributes?, newAttributes: PrintAttributes, cancellationSignal: CancellationSignal, callback: LayoutResultCallback, extras: Bundle?) = Unit
        override fun onWrite(pages: Array<out PageRange>, destination: ParcelFileDescriptor, cancellationSignal: CancellationSignal, callback: WriteResultCallback) = Unit
    }

    private fun mainFrameRequest() = object : WebResourceRequest {
        override fun getUrl() = Uri.parse("https://localhost/")
        override fun isForMainFrame() = true
        override fun isRedirect() = false
        override fun hasGesture() = false
        override fun getMethod() = "GET"
        override fun getRequestHeaders() = emptyMap<String, String>()
    }
}
