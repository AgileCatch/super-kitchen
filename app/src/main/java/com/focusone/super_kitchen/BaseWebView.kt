package com.focusone.super_kitchen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.JsResult
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.journeyapps.barcodescanner.ScanOptions

open class BaseWebView : WebView {
    companion object {
        private const val TAG = "BaseWebView"
        var mCallMethod = ""
        var mContext: Context? = null
        lateinit var mWebView: BaseWebView

        val UPLOAD_PERMISSIONS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.CAMERA,
                )
            } else {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
            }

    }

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
        if (BuildConfig.DEBUG) {
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

            // user-agent에 ",hazzys@LF" 등을 추가 하여 Web 에서 App 인지를 판단 하게 한다.
            setUserAgent(webSettings)

            // https -> http 호출 허용
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            // 시스템 텍스트 크기 무시
            textZoom = 100
        }

        // 서드파티 쿠키 허용.
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(this, true)

        // App <----> Javascript 통신객체 생성
        addJavascriptInterface(
            BaseWebView.AndroidScriptBridge(this), "skscms"
        )
        // WebViewClient 설정
        webViewClient = MyWebViewClient()
        // WebChromeClient 설정
        webChromeClient = MyWebChromeClient()


    }


    private inner class MyWebViewClient() : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.e(TAG, "onPageStarted URL : $url")
        }

        override fun onReceivedError(
            view: WebView?,
            errorCode: Int,
            description: String?,
            failingUrl: String
        ) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            Log.e(TAG, "onReceivedError : $failingUrl")

            //loadUrl(URL_ERROR);
        }

        override fun onPageFinished(view: WebView?, url: String) {
            super.onPageFinished(view, url)
            Log.e(TAG, "onPageFinished : $url")

            //[1] 앱 최초 기동 유무를 확인 -> MainActivity. stopAnimation() 에서 처리

            // 웹뷰의 RAM과 영구 저장소 사이에 쿠키 강제 동기화 수행 함.
            CookieManager.getInstance().flush()

            //testCode();
        }

    }

    private inner class MyWebChromeClient : WebChromeClient() {
        // webview에 있는 inline 동영상 player가 영상을  load 할 때, 보이는
        // 회색 play button이 안보이게 한다.
        // (web에서 meta_tag로 poster를 설정 하면 이런 현상이 발생되어 App에서 제거처리를 해야 함)
        override fun getDefaultVideoPoster(): Bitmap {
            return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        }

        override fun onCreateWindow(
            view: WebView,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message
        ): Boolean {
            val newWebView = WebView(view.context)
            val settings = newWebView.getSettings()
            settings.apply {
                javaScriptEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
                setSupportMultipleWindows(true)
            }
            val transport = resultMsg.obj as WebViewTransport
            transport.webView = newWebView
            resultMsg.sendToTarget()
            return true
        }

        override fun onCloseWindow(window: WebView) {
            super.onCloseWindow(window)
            window.goBack()
            window.goBack()
        }

        override fun onJsAlert(
            view: WebView?,
            url: String?,
            message: String?,
            result: JsResult
        ): Boolean {
            message?.let {
                mContext?.let { it1 ->
                    CustomAlert(
                        it1, it, "확인",
                        DialogInterface.OnClickListener { dialog, which ->
                            result.confirm() // 확인
                        })
                }
            }?.show()
            return true
        }

        override fun onJsConfirm(
            view: WebView?,
            url: String?,
            message: String?,
            result: JsResult
        ): Boolean {
            val myAlert = mContext?.let {
                message?.let { it1 ->
                    CustomAlert(
                        it, it1, "확인", "취소",
                        DialogInterface.OnClickListener { dialog, which ->
                            result.confirm() // 확인
                        }, DialogInterface.OnClickListener { dialog, which ->
                            result.cancel() // 취소
                        })
                }
            }
            myAlert?.show()
            return true
        }

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
            Log.d(TAG, "onShowFileChooser called!!!")

            val chooserIntent = createChooserIntent()
            context.startActivity(chooserIntent)
        }

    }

    private fun createChooserIntent(): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            val mimeTypes = arrayOf("image/*", "application/pdf")
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        return Intent.createChooser(intent, "첨부파일 선택")
    }

    private fun setUserAgent(settings: WebSettings?) {
        if (settings == null || mContext == null) return
        try {
            val pm = mContext!!.packageManager
            val deviceVersion = pm.getPackageInfo(mContext!!.packageName, 0).versionName
            val deviceModelName = Build.MODEL
            //String deviceModelName = android.os.Build.BRAND  + android.os.Build.MODEL;

            // UserAgent를 설정한다.
            settings.setUserAgentString(settings.userAgentString + " [SKApp/Android]")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }


    //바코드기능
    private class AndroidScriptBridge(webview: BaseWebView) {
        //private final Handler handler = new Handler();
        var bPushEnable = false
        var bAdEnable = false

        init {
            mWebView = webview
        }

        @JavascriptInterface
        fun openBarcodeScanner(callMethod: String) {
            mCallMethod = callMethod
            mWebView.post(Runnable {
                Log.e(
                    BaseWebView.TAG,
                    "openBarcodeScanner('$callMethod')"
                )
                (mContext as MainActivity).mBarcodeLauncher.launch(ScanOptions())
            })
        }
    }

    private fun printToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

}