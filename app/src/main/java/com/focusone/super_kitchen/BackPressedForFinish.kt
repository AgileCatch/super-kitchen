package com.focusone.super_kitchen

import android.app.Activity
import android.widget.Toast

class BackPressedForFinish(private val activity: Activity) {
    private var backKeyPressedTime: Long = 0
    private val TIME_INTERVAL: Long = 2000
    private var toast: Toast? = null

    fun onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + TIME_INTERVAL) {
            backKeyPressedTime = System.currentTimeMillis()
            showMessage()
        } else {
            toast?.cancel()
            activity.finish()
            System.exit(0)
        }
    }

    private fun showMessage() {
        toast = Toast.makeText(activity, "'뒤로' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT)
        toast?.show()
    }
}