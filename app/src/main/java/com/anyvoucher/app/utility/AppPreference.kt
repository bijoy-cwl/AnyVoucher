package com.anyvoucher.app.utility

import android.content.Context
import android.content.SharedPreferences
import com.anyvoucher.app.CompanyData
import com.anyvoucher.app.UserData

class AppPreference(context: Context) {

    val shName = "unicode_pref"

    val bt_is_connected = "isConnected"
    val bt_address = "btAddress"
    val bt_name = "btName"


    val name = "name"
    val email = "email"
    val uId = "uId"
    val isLogged = "isLogged"
    val cName = "cName"
    val cMobile = "cMobile"
    val cAddress = "cAddress"

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
    fun setUserData(userData: UserData, logged: Boolean) {
        editor = sharedPreferences.edit()
        editor!!.putString(name, userData.name)
        editor!!.putString(email, userData.email)
        editor!!.putBoolean(isLogged, logged)
        editor!!.putString(uId, userData.uID)
        editor!!.commit()
        editor!!.apply()
    }
    fun setCompanyData(companyData: CompanyData) {
        editor = sharedPreferences.edit()
        editor!!.putString(cName, companyData.cName)
        editor!!.putString(cMobile, companyData.cMobile)
        editor!!.putString(cAddress, companyData.cAddress)
        editor!!.commit()
        editor!!.apply()
    }
}