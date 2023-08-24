package com.anyvoucher.app.utility

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import java.text.DecimalFormat
import java.util.BitSet

object Utils {

    external fun getPath(): String

    external fun getMainPath(): String
    external fun getB(): String
    external fun getH(): String


    // Used to load the 'native-lib' library on application startup.
    init {
        System.loadLibrary("native-lib")
    }

    var nPrintWidth = 384
    var bCutter = false
    var bDrawer = false
    var bBeeper = true
    var nPrintCount = 1
    var nCompressMethod = 0


    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }

    }

     fun getImageFromAssetsFile(name: String, mActivity: Activity): Bitmap {
        val bitmap = mActivity.assets.open(name)
        val btm = BitmapFactory.decodeStream(bitmap)
        return btm
    }


    fun convertBitmap(inputBitmap: Bitmap): Bitmap? {
       var mWidth = inputBitmap.width
        var  mHeight = inputBitmap.height
       return convertArgbToGrayscale(inputBitmap, mWidth, mHeight)
    }

    private fun convertArgbToGrayscale(
        bmpOriginal: Bitmap, width: Int,
        height: Int
    ):Bitmap {
        var pixel: Int
        var k = 0
        var B = 0
        var G = 0
        var R = 0
      var  dots = BitSet()
        try {
            for (x in 0 until height) {
                for (y in 0 until width) {
                    // get one pixel color
                    pixel = bmpOriginal.getPixel(y, x)

                    // retrieve color of all channels
                    R = Color.red(pixel)
                    G = Color.green(pixel)
                    B = Color.blue(pixel)
                    // take conversion up to one single value by calculating
                    // pixel intensity.
                    B = (0.299 * R + 0.587 * G + 0.114 * B).toInt()
                    G = B
                    R = G
                    // set bit into bitset, by calculating the pixel's luma
                    if (R < 55) {
                        dots.set(k) //this is the bitset that i'm printing
                    }
                    k++
                }
            }
        } catch (e: Exception) {
            // TODO: handle exception
        }
        return  bmpOriginal
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }


    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    //double to String with 2 fraction
    fun doubleToString(d: Double): String {
        val formatter = DecimalFormat("#,###.00")
        return if(d==0.0)
            "0.00"
        else
            formatter.format(d)
       // return String.format("%.2f", d)
    }





    fun getError(objects: List<Any>): String {
        var error = StringBuilder()
        if (objects.isNotEmpty()) {
            for (i in objects.indices) {
                val d = objects[i] as List<*>
                for (j in d.indices) {
                    if (j == 1) {
                        val e = d[1]
                        if (error.toString() == "") {
                            error = StringBuilder("\u2022 $e")
                        } else {
                            error.append("\n").append("\u2022 ").append(e)
                        }
                    }
                }

            }
        }
        return error.toString()
    }

    fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

}