package com.focusone.super_kitchen

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.focusone.super_kitchen.BaseWebView.Companion.mCallMethod
import com.focusone.super_kitchen.databinding.ActivityMainBinding
import com.google.zxing.client.android.Intents
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var backPressedForFinish: BackPressedForFinish
    private lateinit var mWebView: BaseWebView // BaseWebView 인스턴스를 선언합니다.

    companion object {
        const val TAG = "MainActivity"
        private val REQUEST_CODE_MEDIA = 2001
        private val REQUEST_CODE_CAMERA = 2002
        private val REQUEST_CODE_NOTIFICATION = 2003
        private val REQUEST_CODE_CONTACTS = 2004
        private val RESULT_CODE_ACTIVITY_APPLICATION_SETTINGS = 2005
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        backPressedForFinish = BackPressedForFinish(this)

        mWebView = BaseWebView(this)

        initView()
    }

    private fun initView() = with(binding) {
        var baseUrl = BuildConfig.MAIN_URL
        mainWebView.loadUrl(baseUrl)
        Log.d(TAG, "MAIN_URL: ${BuildConfig.MAIN_URL}")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView != null && mWebView.canGoBack()) {
            val msg = ">>>>> canGoBack: [${mWebView.url}]"
            Log.e(TAG, msg)
            val nIndex = 2
            val historyList = mWebView.copyBackForwardList()
            var mallMainUrl = ""
            val webHistoryItem = historyList.getItemAtIndex(nIndex)
            if (webHistoryItem != null) {
                mallMainUrl = webHistoryItem.url
            }
            if (mWebView.url.equals(mallMainUrl, ignoreCase = true)) {
                val backBtn: BackPressedForFinish = getBackPressedClass()
                backBtn.onBackPressed()
            } else {
                mWebView.goBack() // 뒤로가기
            }
        } else if (keyCode == KeyEvent.KEYCODE_BACK && !mWebView.canGoBack()) {
            val backBtn: BackPressedForFinish = getBackPressedClass()
            backBtn.onBackPressed()
        } else {
            return super.onKeyDown(keyCode, event)
        }
        return true
    }


    private fun getBackPressedClass(): BackPressedForFinish {
        return backPressedForFinish
    }

    //    //바코드기능
    val mBarcodeLauncher = registerForActivityResult<ScanOptions, ScanIntentResult>(
        ScanContract()
    ) { result: ScanIntentResult? ->
        if (result != null) {
            val originalIntent = result.originalIntent
            if (originalIntent != null) {
                if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                    permissionCamera()
                } else if (result.contents != null) {
                    if (mWebView != null) { // mWebView가 null이 아닌지 확인합니다.
                        Log.e(TAG, "Barcode : " + result.contents)
                        mWebView.loadUrl(("javascript:$mCallMethod").toString() + "('" + result.contents + "')")
                    } else {
                        Log.e(TAG, "mWebView is null")
                    }
                }
            }
        }
    }


    private fun permissionCamera() {
        TedPermission.create()
            .setRationaleMessage("이 기능을 사용하기 위해서는 권한 허용이 필요합니다.")
            .setDeniedMessage(R.string.string_common_camera_alert)
            .setPermissions(Manifest.permission.CAMERA)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    //이미 권한이 있거나 사용자가 권한을 허용했을 때 호출
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    //요청이 거부 되었을 때 호출
                }
            }).check()

    }

}