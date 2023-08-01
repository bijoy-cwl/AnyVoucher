package com.unicodeitltd.app.printer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lvrenyang.io.BTPrinting
import com.lvrenyang.io.IOCallBack
import com.lvrenyang.io.Pos
import com.unicodeitltd.app.PermissionHandler
import com.unicodeitltd.app.PermissionStatus
import com.unicodeitltd.app.R
import com.unicodeitltd.app.UnicodeITPreference
import com.unicodeitltd.app.Utils
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class PrinterConnectActivity : AppCompatActivity(), DevicesAdapter.OnSelectDeviceListener,
    IOCallBack {


    var PERMISSION_ALL = 1

    var PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    lateinit var prefs: UnicodeITPreference

    var btDeviceD: BluetoothDevice? = null
    @SuppressLint("MissingPermission")
    override fun onSelect(btDevice: BluetoothDevice) {
        btDeviceD = btDevice
//        Toast.makeText(
//            this@PrinterConnectActivity,
//            getString(R.string.connecting),
//            Toast.LENGTH_SHORT
//        ).show()

        prefs.setBluetoothAddress(btDevice.address, btDevice.name, false)
        finish()
//        es.submit(TaskOpen(mBt, btDevice.address, context))

    }


    var es: ExecutorService =
        Executors.newScheduledThreadPool(30)
    var mPos = Pos()
    var mBt = BTPrinting()


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


    @SuppressLint("StaticFieldLeak")
    inner class PrinterConnectTask :
        AsyncTask<BluetoothDevice, Void, Boolean>() {

        private var connected = false

        override fun onPreExecute() {
            super.onPreExecute()
            Toast.makeText(
                this@PrinterConnectActivity,
                getString(R.string.connecting),
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun doInBackground(vararg p0: BluetoothDevice?): Boolean {
            val btDevice = p0[0]

            return connected
        }

        @SuppressLint("MissingPermission")
        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            if (result!!) {
                Toast.makeText(
                    this@PrinterConnectActivity,
                    getString(R.string.device_connected),
                    Toast.LENGTH_SHORT
                )
                    .show()
                prefs.setBluetoothAddress(btDeviceD!!.address, btDeviceD!!.name, true)


            } else {
                prefs.setBluetoothAddress(btDeviceD!!.address, btDeviceD!!.name, false)

                Toast.makeText(
                    this@PrinterConnectActivity,
                    getString(R.string.device_connection_falied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private var mDeviceList = ArrayList<BluetoothDevice>()
    private lateinit var adapter: DevicesAdapter
    private lateinit var btAdapter: BluetoothAdapter


    lateinit var devicesRV: RecyclerView
    lateinit var msgTV: TextView
    var allPermissionsGranted: Boolean = true

    companion object {

        internal const val EXTRAS_FROM = "from"
//        internal const val EXTRAS_printer = "printer"

        fun getStartIntent(context: Context, from: Int) {
            val intent = Intent(context, PrinterConnectActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(EXTRAS_FROM, from)
//            intent.putExtra(EXTRAS_printer, printer)
            context.startActivity(intent)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printer_connect)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val scanButton = findViewById<Button>(R.id.scanButton)
        devicesRV = findViewById(R.id.devicesRV)
        msgTV = findViewById(R.id.msgTV)

        setSupportActionBar(toolbar)
        prefs = UnicodeITPreference(this)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = getString(R.string.printer_setup)


        mPos.Set(mBt)

        mBt.SetCallBack(this)

        val bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter =  bluetoothManager.adapter



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
                    getData()
                }

                PermissionStatus.DENIED_FOREVER -> {
                    PermissionHandler().openSettings(this, 101, arrayOf("Bluetooth"))
                }
            }
        }else{
            if (!Utils.hasPermissions(this@PrinterConnectActivity, PERMISSIONS)) {
                ActivityCompat.requestPermissions(
                    this@PrinterConnectActivity,
                    PERMISSIONS,
                    PERMISSION_ALL
                )
            } else {
                getData()
            }

        }



        scanButton.setOnClickListener {
            if (btAdapter.isEnabled) {
                btAdapter.startDiscovery()
            } else {
                val enable = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivity(enable)
            }
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        for (iGranting in grantResults) {
            if (iGranting != PermissionChecker.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (allPermissionsGranted) {
            getData()
        } else {
            ActivityCompat.requestPermissions(
                this@PrinterConnectActivity,
                PERMISSIONS,
                PERMISSION_ALL
            )
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    @SuppressLint("MissingPermission")
    private fun getData() {
        if (!btAdapter.isEnabled) {
            val enable = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enable)
        } else {

            if (loadPairedDevices().isEmpty()) {
                devicesRV.visibility = View.GONE
                msgTV.visibility = View.VISIBLE
                msgTV.text = getString(R.string.searching_printer)
                searchNewDevices()
            } else {
                devicesRV.layoutManager =
                    LinearLayoutManager(this@PrinterConnectActivity)
                adapter = DevicesAdapter(loadPairedDevices(), this@PrinterConnectActivity)
                devicesRV.adapter = adapter
                devicesRV.visibility = View.VISIBLE
                msgTV.visibility = View.GONE

            }

        }
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(mReceiver, filter)
    }

    @SuppressLint("MissingPermission")
    public override fun onPause() {
        if (btAdapter != null) {
            if (btAdapter.isDiscovering) {
                btAdapter.cancelDiscovery()
            }
        }

        super.onPause()
    }

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent
                    .getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                // if (device.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.IMAGING) {

                if (!mDeviceList.contains(device)) {
                    if (device != null) {
                        mDeviceList.add(device)
                    }
                }
                //   }

                if (mDeviceList.isNotEmpty()) {
                    devicesRV.visibility = View.VISIBLE
                    msgTV.visibility = View.GONE
                }

                //  Log.e("BT", "image code: " + BluetoothClass.Device.Major.IMAGING)
//                Log.e(
//                    "BT",
//                    device.name + "\n" + device.address + "\n" + device.bluetoothClass.majorDeviceClass
//                )
                devicesRV.layoutManager =
                    LinearLayoutManager(this@PrinterConnectActivity)
                adapter = DevicesAdapter(mDeviceList, this@PrinterConnectActivity)
                devicesRV.adapter = adapter


            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )

                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        devicesRV.visibility = View.GONE
                        msgTV.visibility = View.VISIBLE
                        msgTV.text = getString(R.string.turn_on_bluetooth)
                    }

                    BluetoothAdapter.STATE_ON -> {
                        if (loadPairedDevices().isEmpty()) {
                            searchNewDevices()
                        } else {
                            getData()
                        }
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED == action) {
                devicesRV.visibility = View.GONE
                msgTV.visibility = View.VISIBLE
                msgTV.text = getString(R.string.searching_printers)
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                if (mDeviceList.isEmpty()) {
                    devicesRV.visibility = View.GONE
                    msgTV.visibility = View.VISIBLE
                    msgTV.text = getString(R.string.no_printer_found)
                } else {
                    devicesRV.visibility = View.VISIBLE
                    msgTV.visibility = View.GONE
                }
            }

        }
    }

    @SuppressLint("MissingPermission")
    private fun searchNewDevices() {
        mDeviceList.clear()
        if (btAdapter.isEnabled) {
            if (!btAdapter.isDiscovering)
                btAdapter.startDiscovery()
        } else {
            val enable = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enable)
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadPairedDevices(): ArrayList<BluetoothDevice> {
        val pairedDevices = btAdapter.bondedDevices

        mDeviceList.clear()
        for (btDevice in pairedDevices) {
            // if (btDevice.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.IMAGING) {
            mDeviceList.add(btDevice)
            //}
        }
        return mDeviceList
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

    override fun onDestroy() {
        TaskClose(mBt)
        unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.printer_search_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            finish()
        } else if (item.itemId == R.id.search) {
            devicesRV.visibility = View.GONE
            msgTV.visibility = View.VISIBLE
            msgTV.text = getString(R.string.searching_printers)
            searchNewDevices()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun OnClose() {
        Toast.makeText(this, getString(R.string.close), Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    override fun OnOpenFailed() {
        Toast.makeText(
            this@PrinterConnectActivity,
            getString(R.string.device_connection_falied),
            Toast.LENGTH_SHORT
        )
            .show()
        prefs.setBluetoothAddress(btDeviceD!!.address, btDeviceD!!.name, false)
    }

    @SuppressLint("MissingPermission")
    override fun OnOpen() {
        // TODO Auto-generated method stub
        runOnUiThread {
            Toast.makeText(
                this@PrinterConnectActivity,
                getString(R.string.device_connected),
                Toast.LENGTH_SHORT
            )
                .show()
            prefs.setBluetoothAddress(btDeviceD!!.address, btDeviceD!!.name, true)
            finish()
        }

    }

}
