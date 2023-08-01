package com.unicodeitltd.app

interface ActivityCloseListener {
    fun onCloseActivity(isClose: Boolean, needClearData: Boolean)
}