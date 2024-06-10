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
    private var currentWaiting = 0
    public var sensorValues= SensorValues()
    val crc = CRC16Modbus()


    @SuppressLint("MissingPermission")
    constructor(myMainActivity: MainActivity,
                onWaiting: ()->Unit,
                onConnection:()->Unit,
                onBleNotSupported:()->Unit,
                onBleDisconnected: ()->Unit) {

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
                        //val fullResponse=characteristic.value.take(7)
                        //responseValue=fullResponse.toByteArray()
                        parseResponse(characteristic.value.take(7).toByteArray())
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

    class CodeForResponse{
        companion object{
            public val PH = 10
            public val Moisture = 20
            public val Temperature = 30
            public val Conductivity = 40
            public val Nitrogen = 50
            public val Phosphorus = 60
            public val Potassium = 70
            public val Done = 80
        }
    }

    @SuppressLint("MissingPermission")
    fun parseResponse(response: ByteArray){
        when (currentWaiting) {
            CodeForResponse.PH ->{
                sensorValues.ph = (byteArrayOf(response[3], response[4]).toInt())/100.0
                currentWaiting = CodeForResponse.Moisture   //set for next response
                interrogationCharacteristic.setValue(queryMaker("12"))  //request moisture
                gatt.writeCharacteristic(interrogationCharacteristic)
            }
            CodeForResponse.Moisture ->{
                sensorValues.moisture = (byteArrayOf(response[3], response[4]).toInt())/10.0
                currentWaiting = CodeForResponse.Temperature   //set for next response
                interrogationCharacteristic.setValue(queryMaker("13"))  //request temperature
                gatt.writeCharacteristic(interrogationCharacteristic)
            }
            CodeForResponse.Temperature ->{
                sensorValues.temperature = (byteArrayOf(response[3], response[4]).toInt())/10.0
                currentWaiting = CodeForResponse.Conductivity   //set for next response
                interrogationCharacteristic.setValue(queryMaker("15")) //request conductivity
                gatt.writeCharacteristic(interrogationCharacteristic)
            }
            CodeForResponse.Conductivity ->{
                sensorValues.conductivity = (byteArrayOf(response[3], response[4]).toInt())/1.0
                currentWaiting = CodeForResponse.Nitrogen   //set for next response
                interrogationCharacteristic.setValue(queryMaker("1E")) //request nitrogen
                gatt.writeCharacteristic(interrogationCharacteristic)
            }
            CodeForResponse.Nitrogen ->{
                sensorValues.nitrogen = (byteArrayOf(response[3], response[4]).toInt())/1.0
                currentWaiting = CodeForResponse.Phosphorus  //set for next response
                interrogationCharacteristic.setValue(queryMaker("1F")) //request phosphorus
                gatt.writeCharacteristic(interrogationCharacteristic)
            }
            CodeForResponse.Phosphorus ->{
                sensorValues.phosphorus = (byteArrayOf(response[3], response[4]).toInt())/1.0
                currentWaiting = CodeForResponse.Potassium   //set for next response
                interrogationCharacteristic.setValue(queryMaker("20")) //request potassium
                gatt.writeCharacteristic(interrogationCharacteristic)
            }
            CodeForResponse.Potassium ->{
                sensorValues.potassium = (byteArrayOf(response[3], response[4]).toInt())/1.0
                currentWaiting = CodeForResponse.Done   //set for next response
                //finised, no need to ask for another query
            }
        }
    }
    public fun queryMaker(sensorCode: String): ByteArray{
        //This function returns the byte array that should be written to the interrogatory characteristic
        //the input is the string representing the location of the statistic needed from sensor.  For example
        //moisture is 12 in hexadecimal so should be entered as "12" in the parameter

        //base = device (1 byte) + function (1 byte) + starting address (2 bytes) + register length (2 bytes)
        var base = "010300"+sensorCode+"0001"   //always has a register length to be trieved of 1
        crc.reset()
        crc.calcCRC(base.decodeHex())
        val checkSum = crc.crcBytes
        var baseArray = base.decodeHex()
        val byteArrayWithCRC = baseArray + checkSum[0] + checkSum[1]
        return byteArrayWithCRC
    }

    @SuppressLint("MissingPermission")
    public fun readSensorValues(){
        currentWaiting= CodeForResponse.PH
        interrogationCharacteristic.setValue(queryMaker("06"))  //request PH
        gatt.writeCharacteristic(interrogationCharacteristic)
    }
}