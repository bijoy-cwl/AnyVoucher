package com.anyvoucher.app

interface ActivityCloseListener {
    fun onCloseActivity(isClose: Boolean, needClearData: Boolean)
}