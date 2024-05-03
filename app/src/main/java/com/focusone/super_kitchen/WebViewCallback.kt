package com.focusone.super_kitchen

interface WebViewCallback {
    fun onPageStarted(url: String)
    fun onPageFinished(url: String)
    fun onError(url: String, errorCode: Int, description: String)
    fun onDownloadRequested(url: String, userAgent: String, contentDisposition: String, mimetype: String, contentLength: Long)
}