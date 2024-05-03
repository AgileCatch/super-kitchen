package com.focusone.super_kitchen

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.ValueCallback
import androidx.appcompat.app.AppCompatActivity
import com.focusone.super_kitchen.databinding.ActivityMainBinding
import com.google.zxing.client.android.BuildConfig

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var backPressedForFinish: BackPressedForFinish
    private lateinit var mWebView: BaseWebView

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

        initView()

    }

    private fun initView() = with(binding) {
        var baseUrl = com.focusone.super_kitchen.BuildConfig.MAIN_URL

        mWebView = binding.mainWebView
        mainWebView.loadUrl(baseUrl)
        Log.d(TAG, "MAIN_URL: ${com.focusone.super_kitchen.BuildConfig.MAIN_URL}")
    }

}