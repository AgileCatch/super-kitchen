package com.focusone.super_kitchen

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

class CustomAlert(
    context: Context,
    content: String,
    okName: String,
    singleListener: DialogInterface.OnClickListener
) {
    private val alertBuilder: AlertDialog.Builder =
        AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog)

    init {
        alertBuilder.apply {
            setTitle("")
            setMessage(content)
            setCancelable(false)
            setPositiveButton(okName, singleListener)
        }
    }

    constructor(
        context: Context,
        content: String,
        okName: String,
        cancelName: String,
        leftListener: DialogInterface.OnClickListener,
        rightListener: DialogInterface.OnClickListener
    )
            : this(
        context,
        content,
        okName,
        DialogInterface.OnClickListener { dialog, _ -> leftListener.onClick(dialog, 0) }) {
        alertBuilder.setNegativeButton(cancelName, rightListener)
    }

    fun show() {
        alertBuilder.show()
    }
}
