package com.anyvoucher.app.utility

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
class PermissionHandler  {


    fun checkPermission(permissions:Array<String>,context: Context,activity:Activity): PermissionStatus {
        //check permission
        var permissionStatus = PermissionStatus.GRANTED

        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                Log.e("PermissionHandler","Permission not granted for $permission")

                var gotoSettings= ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    permission)
                Log.e("PermissionHandler","gotoSettings: $gotoSettings")
                permissionStatus = if(gotoSettings){
                    PermissionStatus.DENIED_FOREVER
                }else{
                    PermissionStatus.DENIED
                }

                break
            }

        }
        return permissionStatus

    }

    fun  requestPermission(permissions:Array<String>,activity:Activity,requestCode:Int){
        Log.e("PermissionHandler","requestPermission $requestCode")
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }

    //open settings
    fun openSettings(activity: Activity,requestCode:Int,permissions:Array<String>){
        var permissionString=""
        for (permission in permissions) {
            permissionString += "* $permission\n"
        }
        permissionString = permissionString.substring(0, permissionString.length - 1)
        AlertDialog.Builder(activity)
            .setTitle("Permission Required")
            .setMessage("Please allow permissions from settings:\n\n$permissionString")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, which ->
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = android.net.Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
                activity.startActivityForResult(intent, 101)
            }

            .show()


    }
}

enum class PermissionStatus {
    GRANTED,
    DENIED,
    DENIED_FOREVER
}