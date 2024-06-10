package com.farmistand.bletesting

import java.math.BigInteger
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Return the float receiver as a string display with numOfDec after the decimal (rounded)
 * (e.g. 35.72 with numOfDec = 1 will be 35.7, 35.78 with numOfDec = 2 will be 35.80)
 *
 * @param numOfDec number of decimal places to show (receiver is rounded to that number)
 * @return the String representation of the receiver up to numOfDec decimal places
 */
fun Float.toString(numOfDec: Int): String {
    val integerDigits = this.toInt()
    val floatDigits = ((this - integerDigits) * 10f.pow(numOfDec)).roundToInt()
    return "${integerDigits}.${floatDigits}"
}

//Returns a byte array from a hexadamical string
fun String.decodeHex(): ByteArray {
    //return a byte array from a hex string
    check(length % 2 == 0) { "Must have an even length" }

    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

//returns a hex string from byte array
fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

//returns an integer from byte array using BigEndian
fun ByteArray.toInt(): Int {
    val bigInteger = BigInteger(this)
    return bigInteger.toInt()
}

