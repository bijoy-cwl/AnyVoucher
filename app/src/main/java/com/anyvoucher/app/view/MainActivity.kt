package com.anyvoucher.app.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anyvoucher.app.ActivityCloseListener
import com.anyvoucher.app.utility.AppPreference
import com.anyvoucher.app.utility.PermissionHandler
import com.anyvoucher.app.utility.PermissionStatus
import com.anyvoucher.app.printer.PrintData
import com.anyvoucher.app.R
import com.anyvoucher.app.utility.Dialog
import com.anyvoucher.app.utility.Utils
import com.google.android.material.textfield.TextInputLayout
import com.lvrenyang.io.BTPrinting
import com.lvrenyang.io.IOCallBack
import com.lvrenyang.io.Pos
import com.anyvoucher.app.utility.Utils.getImageFromAssetsFile
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), IOCallBack, ActivityCloseListener {

    private lateinit var btAdapter: BluetoothAdapter
    lateinit var prefs: AppPreference

    var es: ExecutorService =
        Executors.newScheduledThreadPool(30) as ExecutorService
    var mPos = Pos()
    var mBt = BTPrinting()

    var printData: PrintData? = null

    private lateinit var typeTIL: TextInputLayout
    private lateinit var dateTIL: TextInputLayout
    private lateinit var invoiceTIL: TextInputLayout
    private lateinit var nameTIL: TextInputLayout
    private lateinit var numberTIL: TextInputLayout
    private lateinit var addressTIL: TextInputLayout
    private lateinit var productTIL: TextInputLayout
    private lateinit var totalTIL: TextInputLayout
    private lateinit var noteTIL: TextInputLayout
    private lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = AppPreference(this)
        loadingDialog = Dialog(this)

        typeTIL = findViewById(R.id.typeTIL);
        dateTIL = findViewById(R.id.dateTIL);
        invoiceTIL = findViewById(R.id.invoiceTIL);
        nameTIL = findViewById(R.id.nameTIL);
        numberTIL = findViewById(R.id.numberTIL);
        addressTIL = findViewById(R.id.addressTIL);
        productTIL = findViewById(R.id.productTIL);
        totalTIL = findViewById(R.id.totalTIL);
        noteTIL = findViewById(R.id.noteTIL);


        val bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = bluetoothManager.adapter
        mPos.Set(mBt)
        mBt.SetCallBack(this)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val status = PermissionHandler().checkPermission(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                this,
                this
            )
            Log.d("status", status.toString())
            when (status) {
                PermissionStatus.DENIED -> {

                    Log.d("status", "DENIED")
                    PermissionHandler().requestPermission(
                        arrayOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ),
                        this,
                        111
                    )
                }

                PermissionStatus.GRANTED -> {
                    Log.d("status", "Successful")
                }

                PermissionStatus.DENIED_FOREVER -> {
                    PermissionHandler().openSettings(this, 101, arrayOf("Bluetooth"))
                }
            }
        }

    }

    private fun clearData() {
        typeTIL.editText?.setText("BILL")
        dateTIL.editText?.setText("")
        invoiceTIL.editText?.setText("")
        nameTIL.editText?.setText("")
        numberTIL.editText?.setText("")
        addressTIL.editText?.setText("")
        productTIL.editText?.setText("")
        totalTIL.editText?.setText("")
        noteTIL.editText?.setText("")
    }

    @SuppressLint("MissingPermission")
    private fun getData() {
        val type = typeTIL.editText?.text.toString().trim();
        val date = dateTIL.editText?.text.toString().trim();
        val invoice = invoiceTIL.editText?.text.toString().trim();
        val name = nameTIL.editText?.text.toString().trim();
        val number = numberTIL.editText?.text.toString().trim();
        val address = addressTIL.editText?.text.toString().trim();
        val product = productTIL.editText?.text.toString().trim();
        val total = totalTIL.editText?.text.toString().trim();
        val note = noteTIL.editText?.text.toString().trim();

        if (date == "") {
            showToast("Enter date")
            dateTIL.editText?.requestFocus();
        } else if (invoice == "") {
            showToast("Enter invoice number")
            invoiceTIL.editText?.requestFocus();
        } else if (name == "") {
            showToast("Enter name")
            nameTIL.editText?.requestFocus();
        } else if (number == "") {
            showToast("Enter mobile number")
            numberTIL.editText?.requestFocus();
        } else if (address == "") {
            showToast("Enter address")
            addressTIL.editText?.requestFocus();
        } else if (product == "") {
            showToast("Enter product details")
            productTIL.editText?.requestFocus();
        } else if (total == "") {
            showToast("Enter total amount")
            totalTIL.editText?.requestFocus();
        } else {
            printData = PrintData(type, date, invoice, name, number, address, product, total, note)

            if (!btAdapter.isEnabled) {
                val enable = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivity(enable)
                return
            }
            if (prefs.getValue("btAddress") == "") {
                val intent = Intent(
                    this@MainActivity,
                    PrinterConnectActivity::class.java
                )
                startActivity(intent)
            } else {
                val status = ByteArray(1)
                var b = false
                for (i in 0..9) {
                    b = mPos.POS_QueryStatus(status, 3000, 3)
                    Log.e("PIA", " status \$i: \$b")
                    if (b) break
                }
                Log.e("PIA", " status : \$b")
                if (b) {
                    //printData = PrintData(type,date, invoice, name, number, address, product, total, note)

                    printData?.let { checkPrint(it) }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.connecting), Toast.LENGTH_SHORT
                    ).show()

                    es.submit(
                        TaskOpen(
                            mBt,
                            prefs.getValue("btAddress"),
                            this
                        )
                    )
                }
            }

        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    override fun OnOpen() {
        Log.d("PrinterConnectActivity", "OnOpen: ");
        runOnUiThread {

            Toast.makeText(
                this@MainActivity,
                getString(R.string.device_connected),
                Toast.LENGTH_SHORT
            ).show()

            printData?.let { checkPrint(it) }
        }
    }

    override fun OnOpenFailed() {

        runOnUiThread {
            // viewLoadingDialog!!.dismiss()
            Toast.makeText(
                this@MainActivity,
                getString(R.string.device_connection_falied),
                Toast.LENGTH_SHORT
            )
                .show()
            Log.d("PrinterConnectActivity", "OnOpenFailed: ");
            PrinterConnectActivity.getStartIntent(this, 1)
            //  prefs.setBluetoothAddress(btDeviceD!!.address, false)
        }

    }

    override fun OnClose() {
        Log.d("PrinterConnectActivity", "OnClose: ");
    }

    class TaskClose(bt: BTPrinting?) : Runnable {
        var bt: BTPrinting? = null
        override fun run() { // TODO Auto-generated method stub
            bt!!.Close()
        }

        init {
            this.bt = bt
        }
    }

    class TaskOpen(
        bt: BTPrinting?,
        address: String?,
        context: Context?
    ) :
        Runnable {
        var bt: BTPrinting? = null
        var address: String? = null
        var context: Context? = null
        override fun run() { // TODO Auto-generated method stub
            bt!!.Open(address, context)
        }

        init {
            this.bt = bt
            this.address = address
            this.context = context
        }
    }

    @SuppressLint("MissingPermission")
    fun print(view: View) {
        getData()
    }

    private fun checkPrint(printData: PrintData) {
        if (mPos.GetIO().IsOpened()) {
            loadingDialog.startLoadingDialog()
            val db = Firebase.firestore

            val company = hashMapOf(
                "uID" to prefs.getValue(prefs.uId),
                "type" to printData.type,
                "date" to printData.date,
                "invoice" to printData.invoiceId,
                "clientName" to printData.name,
                "clientMobile" to printData.mobile,
                "clientAddress" to printData.address,
                "product" to printData.product,
                "total" to printData.total,
                "note" to printData.note
            )


            // Add a new document with a generated ID
            db.collection("invoice")
                .document(printData.invoiceId)
                .set(company)
                .addOnSuccessListener {
                    loadingDialog.dismissDialog()

                    printTicket(printData, this, this)

                }
                .addOnFailureListener { e ->
                    loadingDialog.dismissDialog()
                    showToast("Something went wrong")
                }

        } else {

            Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show()
        }
    }

    private fun printTicket(
        printDataM: PrintData,
        context: Context,
        activity: Activity
    ) {
        es.submit(TaskPrint(mPos, printDataM, context, activity, this))
    }

    class TaskPrint(
        mPos: Pos,
        printData3: PrintData,
        context: Context,
        activity: Activity,
        activityCloseListener: ActivityCloseListener
    ) : Runnable {

        var pos: Pos
        private lateinit var printData: PrintData
        var con: Context
        var mActivity: Activity
        var listener: ActivityCloseListener? = null

        init {
            pos = mPos
            printData = printData3
            con = context
            mActivity = activity
            listener = activityCloseListener
        }

        override fun run() {
            // TODO Auto-generated method stub
            val bPrintResult = printTicket(
                Utils.nPrintWidth,
                Utils.bCutter,
                Utils.bDrawer,
                Utils.bBeeper,
                Utils.nPrintCount,
                Utils.nCompressMethod,
                mActivity
            )
            val bIsOpened: Boolean = pos.GetIO().IsOpened()

            // TODO Auto-generated method stub

            Log.e("PIA", "print result : $bPrintResult")

            if (bPrintResult) {
                Log.e(
                    "PIA", "print success"
                )

                listener!!.onCloseActivity(true, true)
            } else {
                Log.e("PIA", "print failed")

                listener!!.onCloseActivity(true, false)
                //  activityCloseListener?.onCloseActivity(false)
            }
            //  activityCloseListener?.onCloseActivity(true)

        }

        fun printTicket(
            nPrintWidth: Int,
            bCutter: Boolean,
            bDrawer: Boolean,
            bBeeper: Boolean,
            nCount: Int,
            nCompressMethod: Int,
            activity: Activity
        ): Boolean {
            var bPrintResult = false

            run {


                if (!pos.GetIO().IsOpened())
                    return@run


                val pref = AppPreference(activity)

                pos.POS_S_Align(1)
                pos.POS_S_TextOut("${pref.getValue(pref.cName)}r\n", 0, 1, 1, 0, 0x00)
                pos.POS_S_TextOut(
                    "\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )
//                pos.POS_PrintPicture(
//                    getImageFromAssetsFile("logo.jpg", mActivity),
//                    350,
//                    1,
//                    nCompressMethod
//                )

                pos.POS_S_TextOut("${pref.getValue(pref.cMobile)}\r\n", 0, 0, 0, 0, 0x00)
                pos.POS_S_TextOut(
                    "${pref.getValue(pref.cAddress)}\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )


                pos.POS_S_TextOut(
                    "\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )

                if (printData.type != "") {

                    pos.POS_S_TextOut("${printData.type}\r\n", 0, 1, 1, 0, 0x00)
                    pos.POS_S_TextOut(
                        "\r\n",
                        0,
                        0,
                        0,
                        0,
                        0x00
                    )
                }
                pos.POS_S_Align(0)
                pos.POS_S_TextOut(
                    "Date: ${printData.date}\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )

                pos.POS_S_TextOut(
                    "Invoice: ${printData.invoiceId}\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )
                pos.POS_S_TextOut(
                    "\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )
                pos.POS_S_TextOut(
                    "Client Details\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )
                pos.POS_S_TextOut(
                    "\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )
                pos.POS_S_TextOut(
                    "Name: ${printData.name}\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )
                pos.POS_S_TextOut(
                    "Mobile: ${printData.mobile}\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )
                pos.POS_S_TextOut(
                    "Address: ${printData.address}\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )
                pos.POS_S_TextOut(
                    "\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )

                pos.POS_S_TextOut(
                    "Product Details:\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )
                pos.POS_S_TextOut(
                    "\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )

                pos.POS_S_TextOut(
                    "${printData.product}\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )
                pos.POS_S_TextOut(
                    "\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )

                pos.POS_S_Align(1)
                pos.POS_S_TextOut(
                    "Total: ${printData.total}\r\n",
                    0,
                    1,
                    1,
                    0,
                    0x00
                )
                pos.POS_S_TextOut(
                    "\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )

                if (printData.note != "") {
                    pos.POS_S_Align(0)
                    pos.POS_S_TextOut(
                        "Note: ${printData.note}\r\n",
                        0,
                        0,
                        0,
                        0,
                        0x00
                    )
                }
                pos.POS_S_TextOut(
                    "\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )
                val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())
                val currentDate = sdf.format(Calendar.getInstance().time)


                pos.POS_S_TextOut(
                    "Printed by: ${pref.getValue(pref.name)}\r\n            $currentDate \r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )
                pos.POS_S_TextOut(
                    "\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )
                pos.POS_S_Align(1)
                pos.POS_S_TextOut(
                    "Thank you for your payment\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )


                pos.POS_S_TextOut(
                    "\r\n",
                    0,
                    0,
                    0,
                    0,
                    0x00
                )


            }
            pos.POS_FeedLine()
            Log.e("PIA", "print start 1")



            Log.e("PIA", "print start 2")
            if (bBeeper) pos.POS_Beep(1, 5)
            if (bCutter) pos.POS_CutPaper()
            if (bDrawer) pos.POS_KickDrawer(0, 100)
            //int dwTicketIndex = dwWriteIndex++;
            //bPrintResult = pos.POS_TicketSucceed(dwTicketIndex, 30000);
            bPrintResult = pos.GetIO().IsOpened()



            // instance.finish()
            return bPrintResult


        }

    }

    private fun getImageFromAssetsFile(name: String, mActivity: Activity): Bitmap {
        val bitmap = mActivity.assets.open(name)
        val btm = BitmapFactory.decodeStream(bitmap)
        return btm
    }

    override fun onCloseActivity(isClose: Boolean, needClearData: Boolean) {

        Log.e("PrinterConnectActivity", "onCloseActivity: $isClose");

    }

    fun clearData(view: View) {
        clearData()
    }

}