package com.yablonskyi.pdf.html

import android.app.Activity
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.coroutines.resume

/** Opens Android's print UI. Success means submission, not that a PDF has been saved. */
class HtmlToPdfConverter internal constructor(
    private val mainDispatcher: CoroutineDispatcher,
    private val createWebView: (Activity) -> WebView,
    private val submitPrint: (Activity, String, PrintDocumentAdapter, PrintAttributes) -> Unit,
) {
    constructor() : this(Dispatchers.Main.immediate, ::WebView, { activity, name, adapter, attributes ->
        val manager = activity.getSystemService(PrintManager::class.java)
            ?: error("Printing is unavailable")
        checkNotNull(manager.print(name, adapter, attributes)) { "Print request was rejected" }
    })

    suspend fun print(activity: Activity, html: String, jobName: String): Result<Unit> =
        withContext(mainDispatcher) {
            try {
                require(html.isNotBlank()) { "Cannot print an empty document" }
                check(!activity.isFinishing && !activity.isDestroyed) { "Print host is unavailable" }
                val lifecycle = (activity as? LifecycleOwner)?.lifecycle
                    ?: error("Print host must have a lifecycle")
                suspendCancellableCoroutine { continuation ->
                    val webView = createWebView(activity)
                    val handler = Handler(Looper.getMainLooper())
                    var submitted = false
                    var destroyed = false
                    var started = false
                    lateinit var observer: LifecycleEventObserver

                    fun cleanup() {
                        if (destroyed) return
                        destroyed = true
                        lifecycle.removeObserver(observer)
                        webView.stopLoading()
                        webView.webViewClient = WebViewClient()
                        webView.destroy()
                    }

                    fun fail(error: Exception) {
                        cleanup()
                        if (error is CancellationException) {
                            continuation.cancel(error)
                        } else if (continuation.isActive) {
                            continuation.resume(Result.failure(error))
                        }
                    }

                    observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_DESTROY) {
                            cleanup()
                            continuation.cancel()
                        }
                    }
                    lifecycle.addObserver(observer)
                    continuation.invokeOnCancellation {
                        // After acceptance the print adapter, not the calling coroutine, owns the page.
                        handler.post { if (!submitted) cleanup() }
                    }
                    try {
                        webView.settings.apply {
                            javaScriptEnabled = false
                            allowFileAccess = false
                            allowContentAccess = false
                            blockNetworkLoads = true
                        }
                        webView.webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String) {
                                if (destroyed || started || !continuation.isActive) return
                                started = true
                                try {
                                    val name = safePrintJobName(jobName)
                                    val delegate = view.createPrintDocumentAdapter(name)
                                    val adapter = FinishingPrintAdapter(delegate, ::cleanup)
                                    submitPrint(activity, name, adapter, printAttributes())
                                    submitted = true
                                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                                } catch (error: Exception) {
                                    fail(error)
                                }
                            }

                            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                                if (request.isForMainFrame) fail(IOException("Character sheet failed to load"))
                            }

                            override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                                fail(IOException("Character sheet WebView renderer stopped"))
                                return true
                            }

                            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = true
                        }
                        webView.loadDataWithBaseURL("https://localhost/", html, "text/html", "UTF-8", null)
                    } catch (error: Exception) {
                        fail(error)
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
}

internal fun printAttributes(): PrintAttributes = PrintAttributes.Builder()
    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
    .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
    .build()

/** Android invokes onFinish after the last layout/write; release the page only then. */
internal class FinishingPrintAdapter(
    private val delegate: PrintDocumentAdapter,
    private val finish: () -> Unit,
) : PrintDocumentAdapter() {
    private var finished = false
    override fun onStart() = delegate.onStart()
    override fun onLayout(
        oldAttributes: PrintAttributes?, newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal, callback: LayoutResultCallback, extras: Bundle?,
    ) = delegate.onLayout(oldAttributes, newAttributes, cancellationSignal, callback, extras)

    override fun onWrite(
        pages: Array<out PageRange>, destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal, callback: WriteResultCallback,
    ) = delegate.onWrite(pages, destination, cancellationSignal, callback)

    override fun onFinish() {
        if (finished) return
        finished = true
        try { delegate.onFinish() } finally { finish() }
    }
}
