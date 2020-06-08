package com.uuuuk.blemultiple

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
interface gatt{
    fun Discovered(p1:BluetoothGatt){}
}
open class mBluetoothGattCallback:BluetoothGattCallback(),gatt {

}