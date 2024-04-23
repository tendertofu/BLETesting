package com.farmistand.bletesting

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import java.util.UUID

//device scan callback
/* leScanCallback = object : ScanCallback(){
    override fun onScanResult(callbackType: Int, result: ScanResult){
        //Handle scan results here (e.g. add devices to a list adapter)
        //
        //Toast.makeText(this@MainActivity,"result is: ${result.device}",Toast.LENGTH_SHORT).show()
        Toast.makeText(this@MainActivity,"I passed here",Toast.LENGTH_SHORT).show()
        listResult.add(result.device.toString())
    }
}

val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
var scanning = false
var handler = Handler(Looper.getMainLooper()).postDelayed({
    scanning = false
    bluetoothLeScanner.stopScan(leScanCallback)
}, SCAN_PERIOD)*//*
        val SCAN_PERIOD: Long = 10000 // Stops scanning after 10 seconds

        fun scanLeDevice() {
            if(!scanning){
            Handler(Looper.getMainLooper()).postDelayed({
                scanning = false
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    //return
                }
                bluetoothLeScanner.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanning = true
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                bluetoothLeScanner.startScan(leScanCallback)
        } else
        {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }

        //start Bluetooth Scan
        scanLeDevice()

*/


/*if(!bluetoothAdapter.isEnabled){
    //val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    //registerForActivityResult( enableBluetoothIntent, REQUEST_ENABLE_BT)
    Toast.makeText(this,"Please turn on bluetooth", Toast.LENGTH_SHORT).show()
}*/


//WORK WITH BLUETOOTH GATT

/*if (devicex.name == "Your Device Name") {
    // Connect to the device
    val gatt = devicex.connectGatt(this, false, object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Discover services after successful connection
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Interact with the BLE device
                val service = gatt.getService(UUID.fromString("Your Service UUID"))
                val characteristic = service.getCharacteristic(UUID.fromString("Your Characteristic UUID"))

                // Read from the characteristic
                gatt.readCharacteristic(characteristic)

                // Write to the characteristic
                val valueToWrite = byteArrayOf(0x01, 0x02) // Example data to write
                characteristic.setValue(valueToWrite)
                gatt.writeCharacteristic(characteristic)
            }
        }
    })
}*/
