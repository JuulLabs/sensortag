package com.juul.sensortag

import com.juul.kable.Advertisement
import com.juul.kable.ManufacturerData

fun Advertisement.multilineString(): List<String> {
    val lines = mutableListOf<String>()
    lines += toString()
    manufacturerData?.also { lines += "  ${it.string}" }
    lines.addAll(multilineServiceUuids(indent = "  "))
    return lines.toList()
}

private val ManufacturerData.string: String
    get() = "ManufacturerData(code=$code, data=${data.hex()})"

private fun Advertisement.multilineServiceUuids(
    indent: String = ""
): List<String> = uuids.flatMap { uuid ->
    listOf(
        "${indent}Service UUID $uuid",
        "${indent.repeat(2)}data=${serviceData(uuid)?.hex()}",
    )
}
