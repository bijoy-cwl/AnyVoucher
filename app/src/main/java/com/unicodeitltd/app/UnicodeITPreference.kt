package com.unicodeitltd.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import kotlin.math.roundToInt

class UnicodeITPreference(context: Context) {

    val shName = "unicode_pref"

    val bt_is_connected = "isConnected"
    val bt_address = "btAddress"
    val bt_name = "btName"



    var mContext: Context? = null

    init {
        mContext = context

    }

    var sharedPreferences = mContext!!.getSharedPreferences(shName, Context.MODE_PRIVATE)

    var editor: SharedPreferences.Editor? = null

    fun getIsBluetoothConnected(): Boolean {
        return sharedPreferences.getBoolean(bt_is_connected, false)
    }

    fun getValue(key: String): String {
        return sharedPreferences.getString(key, "")!!;
    }


    fun setBluetoothAddress(address: String, name: String, isConnected: Boolean) {
        editor = sharedPreferences.edit()
        editor!!.putString(bt_address, address)
        editor!!.putString(bt_name, name)
        editor!!.putBoolean(bt_is_connected, isConnected)
        editor!!.commit()
        editor!!.apply()
    }
}