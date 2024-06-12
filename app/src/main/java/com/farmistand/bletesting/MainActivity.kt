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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

    @Composable
    fun showButtonsTester() {
        var showSpinner by remember { mutableStateOf(false) }
        Column(  verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(10.dp).fillMaxSize()) {
            Text(text="BLE Tester", fontSize=25.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                GlobalScope.launch {
                    showSpinner=true
                    val senValues = jXCT.readSensorValues()
                    contentShowSensorValues(senValues)
                }

            }
            ) {
                Text(text = "Read Sensor", fontSize = 20.sp)
            }
            if (showSpinner) {
                Box(contentAlignment = Alignment.Center, modifier=Modifier.padding(15.dp)) {
                    CircularProgressIndicator()
                }
            }

        }
    }

    @Composable
    fun showSensorValues(sensorValues: SensorValues){

        Column(modifier = Modifier.padding(10.dp)) {
            Text ("PH: ${sensorValues.ph} (pH)", fontSize = 17.sp )
            Text ("Moisture: ${sensorValues.moisture} (%RH)", fontSize = 17.sp )
            Text ("Temperature: ${sensorValues.temperature} (C)", fontSize = 17.sp )
            Text ("Conductivity: ${sensorValues.conductivity} (us/cm)", fontSize = 17.sp )
            Text ("Nitrogen: ${sensorValues.nitrogen} (mg/kg)", fontSize = 17.sp )
            Text ("Phosphorus: ${sensorValues.phosphorus} (mg/kg)" , fontSize = 17.sp )
            Text ("Potassium: ${sensorValues.potassium} (mg/kg)" , fontSize = 17.sp )
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
               contentShowButtonsTester()
            }
            ) {
                Text(text = "Go Back", fontSize = 17.sp)
            }
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