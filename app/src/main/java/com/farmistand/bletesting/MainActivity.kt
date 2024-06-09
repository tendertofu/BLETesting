package com.farmistand.bletesting

//import android.bluetooth.BluetoothAdapter

//bluetooth
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.farmistand.bletesting.ui.theme.BLETestingTheme
import java.util.UUID

const val TARGET_UUID = "5569"  //first few characters of UUID needed (the write characteristic)

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter

    //private lateinit var leScanCallback: ScanCallback
    //private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private var scanning = false
    private val handler = Handler()
    private val SCAN_PERIOD: Long = 10000
    private var deviceCounter: Int = 0
    private lateinit var gatt: BluetoothGatt
    private lateinit var targetCharacteristic: BluetoothGattCharacteristic
    private var listOfSensorValues = mutableListOf<SensorValues>()
    private val uuidPrimaryService: UUID = UUID.fromString("b007f7f2-8507-4c59-bdae-0d113792909a")
    private val uuidRead: UUID = UUID.fromString( "82816da6-5648-4dd8-8c8f-ba1e184e8bb9")
    private val uuidWrite: UUID = UUID.fromString("0e7a55b6-f1c4-40c4-89d8-c3f701ce5569")
    private lateinit var responseCharacteristic: BluetoothGattCharacteristic
    private lateinit var interrogationCharacteristic: BluetoothGattCharacteristic
    private  var gotInterrogationCharac: Boolean = false
    private  var gotResponseCharac: Boolean = false
    private  var onCharacteristicChanged_detected: Boolean = false
    private lateinit var responseValue: ByteArray

    //private lateinit var bleDevices: ArrayList<ScanResult>

    //private val leDeviceListAdapter = LeDeviceListAdapter()

    var listResult = mutableListOf<String>()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.
                }

                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                }

                else -> {
                    // No location access granted.
                }
            }
        }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        //Request Bluetooth permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                )
            )

        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }

        contentShowWaitingForBleConnection()

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        //check if bluetooth is supported
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
        }

        //bleDevices = ArrayList()
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        //scanLeDevice()

        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices

        /* for (pairedDevice in pairedDevices) {
             listResult.add(pairedDevice.name)
         }*/

        val devicex = findMyDevice(pairedDevices, "NPK")


        if (devicex != null) {
            // Connect to the device
            gatt = devicex.connectGatt(this, false, object : BluetoothGattCallback()
            {
                override fun onConnectionStateChange(
                    gatt: BluetoothGatt,
                    status: Int,
                    newState: Int
                ) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        // Discover services after successful connection
                        gatt.discoverServices()
                        runOnUiThread(Runnable {
                            contentShowListOfSensorValues(listOfSensorValues = listOfSensorValues) {
                                gatt.readCharacteristic(targetCharacteristic)
                            }
                        })
                    }

                    if(newState==BluetoothProfile.STATE_DISCONNECTED){
                        runOnUiThread(Runnable {
                          contentShowDisconnection()
                        })
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onCharacteristicRead(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    status: Int
                ) {
                    if (characteristic == responseCharacteristic) {
                       /* val sensorValues = SensorValues(targetCharacteristic.value)
                        listOfSensorValues.add(sensorValues)
                        runOnUiThread(Runnable {
                            contentShowListOfSensorValues(listOfSensorValues = listOfSensorValues) {
                                gatt.readCharacteristic(targetCharacteristic)
                            }
                        }) */
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
                        gatt.readCharacteristic(characteristic)

                      /*  val fullResponse=characteristic.value.take(7)
                        responseValue=fullResponse.toByteArray()
                        runOnUiThread(Runnable {
                            Toast.makeText(
                                this@MainActivity,
                                "Response: " + characteristic.value.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }) */
                    }
                }


                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        /* // Interact with the BLE device
                         val service = gatt.getService(UUID.fromString("Your Service UUID"))
                         val characteristic = service.getCharacteristic(UUID.fromString("Your Characteristic UUID"))

                         // Read from the characteristic
                         gatt.readCharacteristic(characteristic)

                         // Write to the characteristic
                         val valueToWrite = byteArrayOf(0x01, 0x02) // Example data to write
                         characteristic.setValue(valueToWrite)
                         gatt.writeCharacteristic(characteristic)*/

                        val services = gatt.services
                        services.forEach() {
                            val characteristics = it.characteristics
                            characteristics.forEach() { _charac ->
                                if (_charac.uuid==uuidWrite) {
                                    interrogationCharacteristic=_charac
                                      runOnUiThread(Runnable {
                                          Toast.makeText(
                                              this@MainActivity,
                                              "On Characteristic found: " + interrogationCharacteristic.service.toString(),
                                              Toast.LENGTH_LONG
                                          ).show()
                                      })
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
                        //showResults()
                    }
                }


            })

       /*     //PART WHERE WE WRITE TO CHARACTERISTICS
            val primaryService = gatt.getService(uuidPrimaryService)
            primaryService?.let {
                interrogationCharacteristic = it.getCharacteristic(uuidWrite)
                interrogationCharacteristic?.let {
                    it.value = "01030012000125CA".decodeHex()
                    gotInterrogationCharac = gatt.writeCharacteristic(it)
                  /*  runOnUiThread(Runnable {
                       Toast.makeText(this,"Success of write: $success",Toast.LENGTH_SHORT).show()
                    }) */
                }
            }
            */


        }

    }

    //Bluetooth related
/*@SuppressLint("MissingPermission")
fun scanLeDevice() {
    if (!scanning) { // Stops scanning after a pre-defined scan period.
        handler.postDelayed({
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
             Toast.makeText(this@MainActivity,"Number of devices: ${bleDevices.size}", Toast.LENGTH_SHORT).show()
            //showResults()
        }, SCAN_PERIOD)
        scanning = true
        bluetoothLeScanner.startScan(leScanCallback)
    } else {
        scanning = false
        bluetoothLeScanner.stopScan(leScanCallback)
    }
}*/


    @SuppressLint("MissingPermission")
    fun findMyDevice(pairedDevices: Set<BluetoothDevice>, nameOfDevice: String): BluetoothDevice? {
        for (pairedDevice in pairedDevices) {
            if (pairedDevice.name == nameOfDevice) {
                return pairedDevice
            }
        }
        return null
    }



    // Device scan callback.
 /*   @SuppressLint("MissingPermission")
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (!bleDevices.contains(result)) {
                bleDevices.add(result)
            }
            deviceCounter++
            if (result.device.name != null) {
                listResult.add(result.device.name + " w bond state: " + (result.device.bondState == BluetoothDevice.BOND_BONDED))
                //result.device.bondState.toString()
            }

            //leDeviceListAdapter.addDevice(result.device)
            //leDeviceListAdapter.notifyDataSetChanged()
        }
    } */

    /*
    // Extension function to convert byte array to hex string
    fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }

    private val hexArray = "0123456789ABCDEF".toCharArray()
    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF

            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    } */

    //Show Composable

    /*  fun showResults() {
          setContent {
              BLETestingTheme {
                  // A surface container using the 'background' color from the theme
                  Surface(
                      modifier = Modifier.fillMaxSize(),
                      color = MaterialTheme.colorScheme.background
                  ) {
                      //Greeting("Android")
                      showDevices(listResult, deviceCounter)
                  }
              }
          }
      }*/

    /*  fun showHex(hexStr: String){
          setContent{
              showHexValue(hexString = hexStr)
          }
      }*/

    private fun contentShowWaitingForBleConnection(){
        setContent{
            showWaitingForBleConnection()
        }
    }

    private fun contentShowListOfSensorValues(listOfSensorValues: MutableList<SensorValues>, clickMe: () -> Unit) {
        setContent {
            showListOfSensorValues(listOfSensorValues, clickMe)
        }
    }

    private fun contentShowDisconnection(){
        setContent{
            showDisconnection()
        }
    }

    //Permissions

    private var requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                //granted
            } else {
                //deny
            }
        }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }


    fun examineAtBreak(){

    }

    @SuppressLint("MissingPermission")
    fun writeToCharacteristic(){
        val queryValue = "01030015000195CE".decodeHex()  //correct
        //val queryValue = "01030015000195AA.decodeHex()"  //invalid crc
        interrogationCharacteristic.setValue(queryValue)
        var success = gatt.writeCharacteristic(interrogationCharacteristic)
    }
    @Composable
    fun showListOfSensorValues(listOfSensorValues: MutableList<SensorValues>, clickMe: () -> Unit) {
        Column() {
            Button(onClick = {
                clickMe()
            }
            ) {
                Text(text = "Get Reading", fontSize = 25.sp)
            }
            Button(onClick = {
                examineAtBreak()
            }
            ) {
                Text(text = "Break", fontSize = 25.sp)
            }
            Button(onClick = {
                writeToCharacteristic()
            }
            ) {
                Text(text = "Send Inquiry", fontSize = 25.sp)
            }

            LazyColumn() {
                items(listOfSensorValues.size) { index ->
                    Column {
                        val reverseIndex=(listOfSensorValues.size-1)-index
                        Text(text = "---------")
                        Text(text = listOfSensorValues[reverseIndex].itemizedValues)
                    }
                }
            }
        }
    }

}

@Composable
fun showHexValue(hexString: String) {
    Text(text = hexString, fontSize = 20.sp)
}

@Composable
fun showDevices(devicesList: MutableList<String>, counter: Int) {

    Column() {
        Text(text = "Heres the list of services", fontSize = 25.sp)
        //Text(text="Devices found: $counter")
        Text(text = "")
        LazyColumn() {
            items(devicesList.size) { index ->
                Text(text = devicesList[index])
            }
        }
    }
}


/*@Composable
fun showListOfSensorValues(listOfSensorValues: MutableList<SensorValues>, clickMe: () -> Unit) {
    Column() {
        Button(onClick = {
            clickMe()
        }
        ) {
            Text(text = "Get Reading", fontSize = 25.sp)
        }
        LazyColumn() {
            items(listOfSensorValues.size) { index ->
                Column {
                    val reverseIndex=(listOfSensorValues.size-1)-index
                    Text(text = "---------")
                    Text(text = listOfSensorValues[reverseIndex].itemizedValues)
                }
            }
        }
    }
}
*/
@Composable
fun showWaitingForBleConnection(){
    Text(text="""
        Waiting to connect...
        Please turn on sensor
    """.trimIndent(),
    fontSize = 25.sp)
}

@Composable
fun showDisconnection(){
    Text(text="""
        You have been disconnected...
    """.trimIndent(),
        fontSize = 25.sp)
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BLETestingTheme {
        Greeting("Android")
    }
}