package com.focusone.super_kitchen.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class GalleryHelper(private val activity: FragmentActivity) {

    private var mCameraPhotoPath: String? = null
    var listener: OnGalleryListener? = null

    companion object {
        private const val REQUEST_CODE_CAPTURE_IMAGE = 100
        private val TAG = GalleryHelper::class.java.simpleName
    }

    fun getImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        activity.startActivityForResult(intent, REQUEST_CODE_CAPTURE_IMAGE)
    }

    fun startImageCapture() {
        var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (takePictureIntent?.resolveActivity(activity.packageManager) != null) {
            var photoFile: File? = null

            try {
                photoFile = createImageFile()
                takePictureIntent.putExtra("photoPath", mCameraPhotoPath)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to create Image File $e")
            }

            if (photoFile != null) {
                mCameraPhotoPath = "file:${photoFile.absolutePath}"
                val photoUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileProvider.getUriForFile(activity.applicationContext,
                        activity.applicationContext.packageName + ".provider", createImageFile())
                } else {
                    Uri.fromFile(photoFile)
                } as Uri

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                takePictureIntent = null
            }

        }
        val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("image/*")

        val intentArray: Array<Intent> = if (takePictureIntent != null) {
            arrayOf(takePictureIntent)
        } else {
            arrayOf()
        }

        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
            .putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
            .putExtra(Intent.EXTRA_TITLE, "ImageChooser")
            .putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)

        activity.startActivityForResult(chooserIntent, REQUEST_CODE_CAPTURE_IMAGE)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != REQUEST_CODE_CAPTURE_IMAGE) return false
        if (resultCode == Activity.RESULT_OK) {
            listener?.onSuccess(getResultUri(data))
        } else {
            listener?.onFail()
        }
        return true
    }

    @SuppressLint("SimpleDateFormat")
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//        val file = File.createTempFile(imageFileName, ".jpg", storageDir)
        return File("$storageDir/$imageFileName.jpg")
    }

    private fun getResultUri(data: Intent?): Uri? {
        var result: Uri? = null
        if (data == null || TextUtils.isEmpty(data.dataString)) {
            if (mCameraPhotoPath != null) {
                result = Uri.parse(mCameraPhotoPath)
            }
        } else {
            val dateUri = data.data
            val filePath = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> data.dataString
                dateUri != null -> "file:${RealPathUtils.getRealPath(activity, dateUri)}"
                else -> ""
            }
            result = Uri.parse(filePath)
        }
        return result
    }

    interface OnGalleryListener {

        fun onSuccess(uri: Uri?)

        fun onFail()
    }

}
