package com.uuuuk.blemultiple

import android.Manifest
import android.bluetooth.*
import android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.ble_item.view.*
import kotlinx.android.synthetic.main.ble_item.view.tv1
import kotlinx.android.synthetic.main.ble_item.view.tv2
import kotlinx.android.synthetic.main.ble_item.view.tv3
import kotlinx.android.synthetic.main.ble_item.view.tv_blename
import kotlinx.android.synthetic.main.ble_item.view.tv_bty
import kotlinx.android.synthetic.main.sample_devices_view.view.*
import java.lang.Float.intBitsToFloat
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.experimental.and


class MainActivity : AppCompatActivity() {
    private var mBluetoothLeScanner: BluetoothLeScanner? = null
    val scnList  = ArrayList<String>()
    val viewList = ArrayList<View>()
    var first=true
//    val charList = ArrayList<BluetoothGattCharacteristic>()
    val charMap  = mutableMapOf<BluetoothGatt,ArrayList<BluetoothGattCharacteristic>>()

    val gattList = ArrayList<BluetoothGatt>()
    val a=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permisson_list = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (ContextCompat.checkSelfPermission(this, permisson_list[0]) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, permisson_list[1]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permisson_list, 1)
            super.recreate()
        }
        val mBtAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show()
            finish()
            return
        } else {
            mBtAdapter.enable()
        }
        mBluetoothLeScanner = mBtAdapter.bluetoothLeScanner
        bt_con.setOnClickListener {
            AlertDialog.Builder(this)
                .setItems(scnList.toTypedArray()) { _, w ->
                    val addr = scnList[w].substring(scnList[w].lastIndexOf(',') + 1)
                    val device = mBtAdapter.getRemoteDevice(addr)
                    gattList.add(device.connectGatt(this, false, mGattCallback))
//                    deviceList.add(device)
                    val view = devicesView(this)
                    viewList.add(view)
                    view.setOnLongClickListener {
                        gattList[viewList.indexOf(view)].disconnect()
                        return@setOnLongClickListener true
                    }
                }
                .show()
        }
        bt_ent.setOnClickListener {
            val activityIntent = Intent()
            activityIntent.component =
                ComponentName("com.DefaultCompany.PEO", "com.unity3d.player.UnityPlayerActivity")
            startActivity(activityIntent)
        }

    }

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (!scnList.contains(result.device.name+","+result.device.address)&&result.device.name!=null){
                scnList.add(result.device.name+","+result.device.address)
            }
        }
    }


    val mGattCallback: BluetoothGattCallback = object : mBluetoothGattCallback() {
        override fun Discovered( p1: BluetoothGatt) {
            super.Discovered( p1)
            if (p1!=gattList.last()){
                requestCharacteristics(gattList[gattList.indexOf(p1)+1])
            }
            else{
                requestCharacteristics(gattList[0])
            }
        }
        fun requestCharacteristics(gatt: BluetoothGatt) {
            gatt.readCharacteristic(charMap[gatt]!!.last())
        }
        override fun onConnectionStateChange(gatt: BluetoothGatt,status: Int,newState: Int) {
            when(newState){
                BluetoothProfile.STATE_CONNECTED->{
                    showMessage("連線成功")
                    gatt.discoverServices()
                    runOnUiThread {
                        val layoutParams=LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
                        layoutParams.setMargins(5,5,5,5)
                        ll.addView(viewList[gattList.indexOf(gatt)],layoutParams)
                        ll[gattList.indexOf(gatt)].tv_blename.text=gatt.device.name
                    }

                }
                BluetoothProfile.STATE_DISCONNECTED->{
                    showMessage("連線失敗")
                    runOnUiThread {
                        ll.removeView(viewList[gattList.indexOf(gatt)])
                        viewList.removeAt(gattList.indexOf(gatt))
                        gattList.remove(gatt)
                    }


                }
            }

        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val BatteryService=gatt.getService(Battery_Service)
                    if (BatteryService == null) {
                        showMessage("Battery service not found!")
                        gatt.disconnect()
                        return
                    }
                    val BatteryLevel=BatteryService.getCharacteristic(Battery_Level)
                    if (BatteryLevel == null) {
                        showMessage("Battery Level not found!")
                        gatt.disconnect()
                        return
                    }
                    val GsensorService=gatt.getService(Gsensor_Service)
                    if (GsensorService == null) {
                        showMessage("Gsensor service not found!")
                        gatt.disconnect()
                        return
                    }
                    val GsensorXYZ=GsensorService.getCharacteristic(Gsensor_XYZ)
                    if (GsensorXYZ == null) {
                        showMessage("GsensorXYZ not found!")
                        gatt.disconnect()
                        return
                    }
                    if (!charMap.containsKey(gatt)){
                        charMap[gatt]= arrayListOf()
                    }
                    charMap[gatt]!!.add(BatteryLevel)
                    charMap[gatt]!!.add(GsensorXYZ)
                    if (first){
                        requestCharacteristics(gatt)
                        first=false
                    }

//                    for(i in charMap[gatt]!!){
//                        gatt.setCharacteristicNotification(i,true)
//                        val descriptors=i.descriptors
//                        for (j in descriptors){
//                            if(j!=null){
//                                j.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//                                gatt.writeDescriptor(j)
//                            }
//                        }
//                    }

                    Discovered(gatt)
                }
                else {
                    Log.w("GATT","onServicesDiscovered received: $status")
                }
            }
            catch (e:java.lang.Exception){
                Log.e("e",e.toString())
            }

        }


        override fun onCharacteristicChanged(gatt: BluetoothGatt,characteristic: BluetoothGattCharacteristic) {
            Log.d("gattOnChange",characteristic.uuid.toString())
            if(Battery_Level==characteristic.uuid){
                showMessage("bty:"+characteristic.getIntValue(FORMAT_UINT8, 0).toString())
                Log.d("change","bty")
            }
            if (Gsensor_XYZ==characteristic.uuid){
                showMessage("x:"+ ByteBuffer.wrap(characteristic.value).order(ByteOrder.LITTLE_ENDIAN).float.toString())
                Log.d("change","x")
            }
        }


        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == 0) {
                when (characteristic.uuid) {
                    Battery_Level -> {//讀取電池電量
                        var btyLevel = "0"
                        btyLevel = characteristic.getIntValue(FORMAT_UINT8, 0).toString()
                        runOnUiThread {
                            ll[gattList.indexOf(gatt)].tv_bty.text = "$btyLevel%"
                            Log.d("show",gatt.device.name + ",bty")
                        }
                        Log.d("gatt", gatt.device.name + ",bty")
                    }
                    Gsensor_XYZ -> {//讀取角度
                        val angleArray= arrayOf(0f,0f,0f) //x,y,z
                        val buffer=characteristic.value     //每個角度以float拆成4個byte 共12個byte
                        for (n in angleArray.indices){
                            val intBits: Int=(buffer[(n*4)+3].toUByte().toInt() shl 24) or
                                    (buffer[(n*4)+2].toUByte().toInt() shl 16) or
                                    (buffer[(n*4)+1].toUByte().toInt() shl 8) or
                                    (buffer[(n*4)+0].toUByte().toInt() and 0xFF)
                            angleArray[n]=intBitsToFloat(intBits)
                        }
                        runOnUiThread {
                            val tvArray= arrayOf(ll[gattList.indexOf(gatt)].tv1,
                                ll[gattList.indexOf(gatt)].tv2,ll[gattList.indexOf(gatt)].tv3)
                            val xyz= arrayOf("x:","y:","z:")
                            for (i in angleArray.indices){
                                tvArray[i].text=xyz[i]+angleArray[i].toString()
                            }
                            Log.d("show", gatt.device.name + ",xyz")
                        }
                        Log.d("gatt", gatt.device.name + ",xyz")
                    }
                }
                charMap[gatt]!!.remove(charMap[gatt]!!.last())
                if (charMap[gatt]!!.size > 0) {
                        requestCharacteristics(gatt)
                } else {
                    gatt.discoverServices()
                }
            }


        }

    }


    override fun onResume() {
        super.onResume()
        Handler().postDelayed({ mBluetoothLeScanner!!.stopScan(scanCallback)}, 5000)
        mBluetoothLeScanner!!.startScan(scanCallback)
    }

    fun showMessage(text:String,time:Int=Toast.LENGTH_SHORT){
        runOnUiThread {
            Toast.makeText(this,text,time).show()
        }
    }


    companion object {
        val Battery_Service=    UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
        val Battery_Level=      UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
        val CCCD=               UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        val CCCD2=              UUID.fromString("00002901-0000-1000-8000-00805f9b34fb")
        val RX_SERVICE_UUID=    UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
        val RX_CHAR_UUID=       UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
        val TX_CHAR_UUID=       UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")
        val Gsensor_Service=    UUID.fromString("90406bee-33fd-381a-8fd4-dfed9f7d5310")
        val Gsensor_XYZ=        UUID.fromString("90406bef-33fd-381a-8fd4-dfed9f7d5310")
    }


}

