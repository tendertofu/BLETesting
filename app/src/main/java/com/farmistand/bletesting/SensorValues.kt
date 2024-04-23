package com.farmistand.bletesting

class SensorValues (val byteArray: ByteArray) {
    val arraySize = byteArray.size

    var humidity = 0.0
    var temperature = 0.0
    var conductivity = 0.0
    var ph = 0.0
    var nitrogen = 0.0
    var potassium = 0.0
    var phosphorus = 0.0
    var hexValue=""
    var itemizedValues = ""

    init{
        if (arraySize!=14){
            throw Exception("Illegal size of byte array: $arraySize")
        }
        humidity= getParameterValue (0)/10.0
        temperature=getParameterValue(1)/10.0
        conductivity=getParameterValue(2).toDouble()
        ph = getParameterValue(3)/10.0
        nitrogen = getParameterValue(4).toDouble()
        potassium=getParameterValue(5).toDouble()
        phosphorus=getParameterValue(6).toDouble()
        hexValue = byteArray.toHex()
        itemizedValues="""
            Humidity: $humidity
            Temperature: $temperature
            Conductivity: $conductivity
            Ph: $ph
            Nitrogen: $nitrogen
            Potassium: $potassium
            Phosphorus: $phosphorus
        """.trimIndent()
    }

    fun twoBytesToDecimal(byteArray: ByteArray): Int {
        if (byteArray.size != 2) {
            throw IllegalArgumentException("Input byte array must have exactly 2 elements.")
        }

        // Assuming unsigned bytes for simplicity
        return (byteArray[0].toInt() shl 8) or byteArray[1].toInt()
    }

    fun byteArrayFrom2(byte1: Byte, byte2: Byte): ByteArray{
        val byteArray = ByteArray(2)
        byteArray[1]=byte1
        byteArray[0]= byte2
        return  byteArray
    }
    fun getParameterValue(parameterPosition: Int): Int{
        //This function returns the parameter in the byteArray returned by the
        //sensor based its position (parameterPosition)
        val index = parameterPosition*2
        return (byteArray[index].toInt() * 256) + (byteArray[index+1]).toInt()
    }
    // Extension function to convert byte array to hex string
    fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }
}