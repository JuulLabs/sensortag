package com.juul.sensortag

// Adapted from: https://github.com/Kotlin/kotlinx.serialization/blob/4bd949b0634fd498f847b1e059826cc7e67adf07/formats/protobuf/commonTest/src/kotlinx/serialization/HexConverter.kt
private const val hexCode = "0123456789ABCDEF"
fun ByteArray.hex(lowerCase: Boolean = false): String {
    val r = StringBuilder(size * 2)
    for (b in this) {
        r.append(hexCode[b.toInt() shr 4 and 0xF])
        r.append(hexCode[b.toInt() and 0xF])
    }
    return if (lowerCase) r.toString().toLowerCase() else r.toString()
}
