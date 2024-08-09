package com.juul.sensortag

import com.juul.kable.Bluetooth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

typealias AvailabilityListener = (isAvailable: Boolean) -> Unit

@JsExport
class BluetoothAvailability internal constructor() {

    private val listeners = mutableListOf<AvailabilityListener>()

    internal fun launchIn(scope: CoroutineScope) {
        Bluetooth
            .isAvailable
            .onEach {
                listeners.forEach { listener -> listener.invoke(it) }
            }.launchIn(scope)
    }

    fun addListener(listener: AvailabilityListener) { listeners += listener }
    fun removeListener(listener: AvailabilityListener) { listeners -= listener }
}
