package com.juul.sensortag

import com.juul.khronicle.ConsoleLogger
import com.juul.khronicle.Log

internal const val LogTag = "SensorTag"

fun configureLogging() {
    Log.dispatcher.install(ConsoleLogger)
}
