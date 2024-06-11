package com.farmistand.bletesting

//import android.bluetooth.BluetoothAdapter

//bluetooth
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.farmistand.bletesting.ui.theme.BLETestingTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

//const val TARGET_UUID = "5569"  //first few characters of UUID needed (the write characteristic)

class MainActivity : ComponentActivity() {

    private lateinit var jXCT : JXCT

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

        jXCT = JXCT(this,
            {contentShowWaitingForBleConnection()},
            {contentShowButtonsTester()},
            { Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()},
            { contentShowDisconnection()})
        }


   // }

    //Bluetooth related



    //Show Composable

    private fun contentShowWaitingForBleConnection(){
        setContent{
            showWaitingForBleConnection()
        }
    }

    private fun contentShowButtonsTester() {
        setContent {
            showButtonsTester()
        }
    }

    private fun contentShowDisconnection(){
        setContent{
            showDisconnection()
        }
    }
    private fun contentShowSensorValues(sensorValues: SensorValues){
        setContent{
            showSensorValues(sensorValues = sensorValues)
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

        val queryValue = "010300120001240F".decodeHex()  //correct
        //val queryValue = "01030015000195AA.decodeHex()"  //invalid crc
        jXCT.interrogationCharacteristic.setValue(queryValue)
        var success = jXCT.gatt.writeCharacteristic(jXCT.interrogationCharacteristic)

    }
    @Composable
    fun showButtonsTester() {
        Column() {
            Button(onClick = {
                examineAtBreak()
            }
            ) {
                Text(text = "Break", fontSize = 25.sp)
            }
            Button(onClick = {
                GlobalScope.launch {
                    val senValues = jXCT.readSensorValues()
                    contentShowSensorValues(senValues)
                }

            }
            ) {
                Text(text = "Send Inquiry", fontSize = 25.sp)
            }

        }
    }

    @Composable
    fun showSensorValues(sensorValues: SensorValues){

        Column() {
            Text ("PH: ${sensorValues.ph}", fontSize = 17.sp )
            Text ("Moisture: ${sensorValues.moisture}", fontSize = 17.sp )
            Text ("Temperature: ${sensorValues.temperature}", fontSize = 17.sp )
            Text ("Conductivity: ${sensorValues.conductivity}", fontSize = 17.sp )
            Text ("Nitrogen: ${sensorValues.nitrogen}", fontSize = 17.sp )
            Text ("Phosphorus: ${sensorValues.phosphorus}" , fontSize = 17.sp )
            Text ("Potassium: ${sensorValues.potassium}", fontSize = 17.sp )
        }

    }

}



//***** OUT SIDE OF MAIN ACTIVITY ***//
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