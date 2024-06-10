package com.farmistand.bletesting

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile

import java.util.UUID

class BLE  {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    public lateinit var gatt: BluetoothGatt
    private val uuidRead: UUID = UUID.fromString( "82816da6-5648-4dd8-8c8f-ba1e184e8bb9")
    private val uuidWrite: UUID = UUID.fromString("0e7a55b6-f1c4-40c4-89d8-c3f701ce5569")
    private lateinit var responseCharacteristic: BluetoothGattCharacteristic
    public lateinit var interrogationCharacteristic: BluetoothGattCharacteristic
    public lateinit var responseValue: ByteArray
    private  var onCharacteristicChanged_detected: Boolean = false


    @SuppressLint("MissingPermission")
    constructor(myMainActivity: MainActivity,
                onWaiting: ()->Unit,
                onConnection:()->Unit,
                onBleNotSupported:()->Unit,
                onBleDisconnected: ()->Unit) {

        //contentShowWaitingForBleConnection()

        onWaiting()

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        //check if bluetooth is supported
        if (bluetoothAdapter == null) {
          onBleNotSupported()
        }

        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices

        val devicex = findMyDevice(pairedDevices, "NPK")

        if (devicex != null) {
            // Connect to the device
            gatt = devicex.connectGatt(myMainActivity, false, object : BluetoothGattCallback()
            {
                override fun onConnectionStateChange(
                    gatt: BluetoothGatt,
                    status: Int,
                    newState: Int
                ) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        // Discover services after successful connection
                        gatt.discoverServices()
                        onConnection()
                    }

                    if(newState== BluetoothProfile.STATE_DISCONNECTED){
                        /*
                        runOnUiThread(Runnable {
                            contentShowDisconnection()
                        })
                        */
                        onBleDisconnected()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onCharacteristicRead(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    status: Int
                ) {
                    if (characteristic == responseCharacteristic) {
                        val fullResponse=characteristic.value.take(7)
                        responseValue=fullResponse.toByteArray()
                    }
                }

                override fun onCharacteristicChanged(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic
                ) {
                    super.onCharacteristicChanged(gatt,characteristic)

                    if (characteristic.uuid == uuidRead) {
                        onCharacteristicChanged_detected=true
                        gatt.readCharacteristic(characteristic)  //The characteristic value will be captured in OnCharacteristicRead
                    }
                }


                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        val services = gatt.services
                        services.forEach() {
                            val characteristics = it.characteristics
                            characteristics.forEach() { _charac ->
                                if (_charac.uuid==uuidWrite) {
                                    interrogationCharacteristic=_charac
                                }
                                if(_charac.uuid == uuidRead){
                                    //gotResponseCharac=true
                                    responseCharacteristic = _charac
                                    gatt.setCharacteristicNotification(responseCharacteristic, true)
                                    val uuidString = "00002902-0000-1000-8000-00805F9B34FB"
                                    val descriptor = responseCharacteristic.getDescriptor(UUID.fromString(uuidString))
                                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                    gatt.writeDescriptor(descriptor)
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    @SuppressLint("MissingPermission")
    fun findMyDevice(pairedDevices: Set<BluetoothDevice>, nameOfDevice: String): BluetoothDevice? {
        for (pairedDevice in pairedDevices) {
            if (pairedDevice.name == nameOfDevice) {
                return pairedDevice
            }
        }
        return null
    }
}