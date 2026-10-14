package com.listadecompras.app.ui

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream

/** Only the bundled map may be a top-level page. Remote scripts/tiles are allowlisted. */
internal class MapWebViewClient(private val onPageLoaded: (Boolean) -> Unit) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = true

    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        val uri = request?.url?.toString()
        return if (MapResourcePolicy.isAllowedResource(uri)) null else
            WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        onPageLoaded(url == MapResourcePolicy.LOCAL_MAP)
    }
}
