package android.webkit

// The platform constructor is package-private; keep the test fake in its package.
class TestWebResourceError : WebResourceError() {
    override fun getErrorCode() = -1
    override fun getDescription(): CharSequence = "Failed"
}
