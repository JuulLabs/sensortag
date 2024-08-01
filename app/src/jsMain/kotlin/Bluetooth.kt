package com.juul.sensortag

import com.juul.kable.Bluetooth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Bluetooth.isAvailable: Flow<Boolean>
    get() = availability.map(Bluetooth.Availability::toBoolean)

private fun Bluetooth.Availability.toBoolean() = when (this) {
    Bluetooth.Availability.Available -> true
    is Bluetooth.Availability.Unavailable -> false
}
