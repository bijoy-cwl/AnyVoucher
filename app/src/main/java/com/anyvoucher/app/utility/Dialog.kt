package com.anyvoucher.app.utility

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import com.anyvoucher.app.R

class Dialog internal constructor( // 2 objects activity and dialog
    private val activity: Activity
) {
    private var dialog: AlertDialog? = null
    @SuppressLint("InflateParams")
    fun startLoadingDialog() {

        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.loading, null))
        builder.setCancelable(true)
        dialog = builder.create()
        dialog!!.show()
    }

    // dismiss method
    fun dismissDialog() {
        if (dialog!!.isShowing)
        dialog!!.dismiss()
    }
}