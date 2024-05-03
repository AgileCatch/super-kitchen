package com.focusone.super_kitchen

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.journeyapps.barcodescanner.ScanOptions
import java.net.URISyntaxException

class BaseWebView : WebView {

    companion object {
        private const val TAG = "BaseWebView"
    }

    private var mContext: Context? = null

    constructor(context: Context) : super(context) {
        mContext = context
        initializeOptions()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
        initializeOptions()
    }

    init {
        initializeOptions()
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeOptions() {
        if (BuildConfig.DEBUG){
            setWebContentsDebuggingEnabled(true)
        }
        // WebView 설정
        val webSettings: WebSettings = this.settings
        webSettings.apply {
            javaScriptEnabled = true
            builtInZoomControls = true
            displayZoomControls = false
            loadsImagesAutomatically = true
            loadWithOverviewMode = true
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
            // user-agent에 ",hazzys@LF" 등을 추가하여 Web에서 App인지를 판단
//            setUserAgent(settings)
            // https -> http 호출 허용
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            // 시스템 텍스트 크기 무시
            textZoom = 100
        }

//        // 서드파티 쿠키 허용
//        val cookieManager: CookieManager = CookieManager.getInstance()
//        cookieManager.setAcceptCookie(true)
//        cookieManager.setAcceptThirdPartyCookies(this, true)
//
//        // App <----> Javascript 통신 객체 생성
//        addJavascriptInterface(AndroidScriptBridge(), "skscms")
//
//        // WebViewClient 설정
//        webViewClient = MyWebViewClient()
//        // WebChromeClient 설정
//        webChromeClient = MyWebChromeClient()
//
//        // DownloadListener 설정
//        setDownloadListener { url, _, _, _, _ ->
//            DwonloadReceiverStart()
//
//            val contentId = Uri.parse(url)
//            val newFilename = android.net.Uri.decode(contentId.lastPathSegment)
//
//            val request = DownloadManager.Request(contentId)
//            val dm = mContext?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//
//            val cookie = CookieManager.getInstance().getCookie(url)
//            request.addRequestHeader("Cookie", cookie)
//            request.setVisibleInDownloadsUi(true)
//            request.setMimeType("")
//            request.addRequestHeader("User-Agent", settings.userAgentString)
//            request.setDescription("Downloading File")
//            request.setAllowedOverMetered(true)
//            request.setAllowedOverRoaming(true)
//            request.setTitle(newFilename)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                request.setRequiresCharging(false)
//            }
//            request.allowScanningByMediaScanner()
//            request.setAllowedOverMetered(true)
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, newFilename)
//
//            downloadId = dm.enqueue(request)
//            Toast.makeText(mContext, "파일을 다운로드 합니다.", Toast.LENGTH_SHORT).show()
//        }
    }

//    private fun DwonloadReceiverStart() {
//        val completeFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
//        mContext?.registerReceiver(downloadCompleteReceiver, completeFilter)
//    }
//
//    private fun DwonloadReeceiverStop() {
//        if (downloadId > 0) {
//            mContext?.unregisterReceiver(downloadCompleteReceiver)
//            downloadId = 0
//        }
//    }
//
//    private val downloadCompleteReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
//                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
//                if (id != downloadId) {
//                    return
//                }
//                if ((mContext as? Activity)?.isFinishing == true) {
//                    return
//                }
//                try {
//                    val downloadManager =
//                        mContext?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//                    val cursor = downloadManager.query(DownloadManager.Query().setFilterById(id))
//
//                    if (cursor != null && cursor.moveToNext()) {
//                        val status =
//                            cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
//                        cursor.close()
//
//                        if (status == DownloadManager.STATUS_FAILED) {
//                            Toast.makeText(mContext, "다운실패.", Toast.LENGTH_LONG).show()
//                            DwonloadReeceiverStop()
//                        } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
//                            Toast.makeText(mContext, "다운완료.", Toast.LENGTH_LONG).show()
//                            DwonloadReeceiverStop()
//                        } else {
//                            Toast.makeText(mContext, "다운실패.", Toast.LENGTH_LONG).show()
//                            DwonloadReeceiverStop()
//                        }
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    Toast.makeText(mContext, "다운실패.", Toast.LENGTH_LONG).show()
//                    DwonloadReeceiverStop()
//                }
//            }
//        }
//    }

//    private inner class MyWebViewClient : WebViewClient() {
//        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
//            Log.e(TAG, "shouldOverrideUrlLoading : $url")
//
//            try {
//                if (url?.startsWith("about:blank") == true) {
//                    Log.e(TAG, "about:blank")
//                    return true
//
//                } else if (url?.startsWith("tel:") == true) {
//                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse(url))
//                    mContext?.startActivity(intent)
//                    return true
//
//                } else if (url?.startsWith("mailto:") == true) {
//                    val eMail = url.replace("mailto", "")
//                    val intent = Intent(Intent.ACTION_SEND)
//                    intent.type = "plain/text"
//                    intent.putExtra(Intent.EXTRA_EMAIL, eMail)
//                    mContext?.startActivity(intent)
//
//                    return true
//
//                } else if (url?.startsWith("intent:kakao") == true || url?.startsWith("kakao") == true) {
//                    try {
//                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
//                        val existPackage =
//                            mContext?.packageManager?.getLaunchIntentForPackage(intent.packageName)
//
//                        if (existPackage != null) {
//                            mContext?.startActivity(intent)
//                        } else {
//                            val marketIntent = Intent(Intent.ACTION_VIEW)
//                            marketIntent.data = Uri.parse("market://details?id=" + intent.packageName)
//                            mContext?.startActivity(marketIntent)
//                        }
//
//                    } catch (e: Exception) {
//                        Log.d(TAG, "Bad URI $url: ${e.message}")
//                        return false
//                    }
//
//                    return true
//                }
//
//                // Add your other URL handling logic here
//
//            } catch (e: Exception) {
//                return false
//            }
//
//            return false
//        }
//    }

//    private inner class MyWebChromeClient : WebChromeClient() {
//        override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
//            AlertDialog.Builder(mContext, android.R.style.Theme_DeviceDefault_Light_Dialog)
//                .setMessage(message)
//                .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm() }
//                .setCancelable(false)
//                .show()
//            return true
//        }
//    }
//
//    //바코드
//    private inner class AndroidScriptBridge {
//        @JavascriptInterface
//        fun openBarcodeScanner(callMethod: String) {
//            mCallMethod = callMethod
//            post {
//                ((mContext as? MainActivity)?.mBarcodeLauncher)?.launch(ScanOptions())
//            }
//        }
//    }
//
//    private fun setUserAgent(settings: WebSettings?) {
//        if (settings == null) return
//        try {
//            val pm: PackageManager? = mContext?.packageManager
//            val deviceVersion = pm?.getPackageInfo(mContext?.packageName, 0)?.versionName
//            val deviceModelName = Build.MODEL
//
//            settings.userAgentString = settings.userAgentString + " [SKApp/Android]"
//
//        } catch (e: PackageManager.NameNotFoundException) {
//            e.printStackTrace()
//        }
//    }

    fun loadMainUrl(url: String) {
        loadUrl(url)
    }
}