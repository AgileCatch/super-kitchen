package com.focusone.super_kitchen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.webkit.ValueCallback
import androidx.appcompat.app.AppCompatActivity
import com.focusone.super_kitchen.databinding.ActivityMainBinding
import com.focusone.super_kitchen.util.BackPressedForFinish
import com.google.zxing.client.android.Intents
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import java.io.File

open class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var backPressedForFinish: BackPressedForFinish

    companion object {
        const val TAG = "MainActivity"
        private const val REQUEST_CODE_MEDIA = 2001
        private const val REQUEST_CODE_CAMERA = 2002

        // 파일 첨부
        private var mUploadMessage: ValueCallback<Uri?>? = null
        private var mUploadMessages: ValueCallback<Array<Uri>>? = null
        private var imageUri: Uri? = null

        // 파일첨부 resultCode.
        private const val RESULTCODE_FILECHOOSER = 1001
        private const val RESULTCODE_FILECHOOSER_LOLLIPOP = 1002
        private const val RESULT_CODE_ACTIVITY_APPLICATION_SETTINGS = 2005

        val UPLOAD_PERMISSIONS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                )
            } else {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //[1] 파일 첨부 callback 결과 처리
        if (requestCode == RESULTCODE_FILECHOOSER || requestCode == RESULTCODE_FILECHOOSER_LOLLIPOP) fileChooserResult(
            requestCode,
            resultCode,
            data
        ) else if (RESULT_CODE_ACTIVITY_APPLICATION_SETTINGS == requestCode) {
            // 권한 체크 수행
            permissionMedia()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        backPressedForFinish = BackPressedForFinish(this)

        initView()
    }

    private fun initView() = with(binding) {
        var baseUrl = BuildConfig.MAIN_URL
        mainWebView.loadUrl("http://www.naver.com/")
        Log.d(TAG, "MAIN_URL: ${BuildConfig.MAIN_URL}")
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = with(binding) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mainWebView != null && mainWebView.canGoBack()) {
            val msg = ">>>>> canGoBack: [${mainWebView.url}]"
            Log.e(TAG, msg)
            val nIndex = 2
            val historyList = mainWebView.copyBackForwardList()
            var mallMainUrl = ""
            val webHistoryItem = historyList.getItemAtIndex(nIndex)
            if (webHistoryItem != null) {
                mallMainUrl = webHistoryItem.url
            }
            if (mainWebView.url.equals(mallMainUrl, ignoreCase = true)) {
                val backBtn: BackPressedForFinish = getBackPressedClass()
                backBtn.onBackPressed()
            } else {
                mainWebView.goBack() // 뒤로가기
            }
        } else if (keyCode == KeyEvent.KEYCODE_BACK && !mainWebView.canGoBack()) {
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


    val mBarcodeLauncher = registerForActivityResult<ScanOptions, ScanIntentResult>(ScanContract()) { result: ScanIntentResult? ->
        Log.e(TAG, "Barcode Scanner Callback is called with result: $result")
        if (result != null) {
            val originalIntent = result.originalIntent
            if (originalIntent != null) {
                if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                    permissionCamera()
                } else if (result.contents != null) {
                    Log.e(TAG, "Barcode : " + result.contents)
                    Log.e(TAG, "CallMethod : " + BaseWebView.mCallMethod)
                    binding.mainWebView.loadUrl("javascript:" + BaseWebView.mCallMethod + "('" + result.contents + "')")
                }
            }
        }
    }


    fun doFileAttach(uploadMsg: ValueCallback<Uri?>) {
        mUploadMessage = uploadMsg
        showAttachmentDialog(false)
    }

    fun doFileAttachs(uploadMsg: ValueCallback<Array<Uri>>?) {
        mUploadMessages = uploadMsg
        showAttachmentDialog(true)
    }

    private fun showAttachmentDialog(isLOLLIPOP: Boolean) {
        // Create AndroidExampleFolder at sdcard
        val imageStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "SCMS"
        )
        if (!imageStorageDir.exists()) {
            imageStorageDir.mkdirs()
        }

        // Create camera captured image file path and name
        val file = File(
            imageStorageDir.toString() + File.separator + "IMG_" + System.currentTimeMillis()
                .toString() + ".jpg"
        )
        imageUri = Uri.fromFile(file)

        // Camera capture image intent
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.setType("*/*")

        // Create file chooser intent
        val chooserIntent = Intent.createChooser(i, "File Chooser")

        // Set camera intent to file chooser
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf<Parcelable>(captureIntent))

        // On select image call onActivityResult method of activity
        if (isLOLLIPOP) {
            startActivityForResult(chooserIntent, RESULTCODE_FILECHOOSER_LOLLIPOP)
        } else {
            startActivityForResult(chooserIntent, RESULTCODE_FILECHOOSER)
        }
    }

    private fun fileChooserResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (mUploadMessage == null && mUploadMessages == null) {
                return
            }
            if (requestCode == RESULTCODE_FILECHOOSER) {
                Log.e(TAG, "requestCode : RESULTCODE_FILECHOOSER || RESULTCODE_CAMERA")
                var result: Uri? = null
                if (data == null) {
                    result = imageUri
                } else {
                    result = data.data
                }
                mUploadMessage?.onReceiveValue(result)
                mUploadMessage = null
            } else if (requestCode == RESULTCODE_FILECHOOSER_LOLLIPOP) {
                Log.e(
                    TAG,
                    "requestCode : RESULTCODE_FILECHOOSER_LOLLIPOP || RESULTCODE_CAMRERA_LOLLIPOP"
                )
                val result: Array<Uri>
                val uri: String = imageUri.toString()
                result = if (data == null || data.data == null) {
                    arrayOf(Uri.parse(uri))
                } else {
                    arrayOf(Uri.parse(data.dataString))
                }
                mUploadMessages?.onReceiveValue(result)
                mUploadMessages = null
            }
        } else {
            if (requestCode == RESULTCODE_FILECHOOSER) {
                mUploadMessage?.onReceiveValue(null)
                mUploadMessage = null
            } else if (requestCode == RESULTCODE_FILECHOOSER_LOLLIPOP) {
                mUploadMessages?.onReceiveValue(null)
                mUploadMessages = null
            }

            // 앨범접근은 필수 권한이기 때문에  resultCode == RESULT_OK 이다.
            // 반면에 카메라 접근 권한은 선택 권한 이기 때문에 사용자가 미허용시
            // resultCode != RESULT_OK 이어서 이곳에서 카메라 접근 권한을 노출 한다.
            permissionCamera()
        }
    }

    private fun permissionMedia() {
        TedPermission.create()
            .setRationaleMessage(R.string.string_common_permission)
            .setDeniedMessage(R.string.string_common_media_alert)
            .setPermissions(*UPLOAD_PERMISSIONS)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    //이미 권한이 있거나 사용자가 권한을 허용했을 때 호출
                    requestPermissions(
                        arrayOf(*UPLOAD_PERMISSIONS),
                        REQUEST_CODE_MEDIA
                    )

                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    //요청이 거부 되었을 때 호출
                }
            }).check()

    }

    private fun permissionCamera() {
        TedPermission.create()
            .setRationaleMessage(R.string.string_common_permission)
            .setDeniedMessage(R.string.string_common_camera_alert)
            .setPermissions(Manifest.permission.CAMERA)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    //이미 권한이 있거나 사용자가 권한을 허용했을 때 호출
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                            arrayOf(Manifest.permission.CAMERA),
                            REQUEST_CODE_CAMERA
                        )
                        Handler().post { mBarcodeLauncher.launch(ScanOptions()) }//바코드 실행
                    }
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    //요청이 거부 되었을 때 호출
                }
            }).check()

    }

}