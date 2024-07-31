package com.juul.sensortag

import com.juul.kable.Bluetooth
import com.juul.kable.Options
import com.juul.kable.State.Disconnected
import com.juul.kable.requestPeripheral
import com.juul.khronicle.ConsoleLogger
import com.juul.khronicle.ConstantTagGenerator
import com.juul.khronicle.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@JsExport
class Script {

    init {
        Log.tagGenerator = ConstantTagGenerator(tag = "SensorTag")
        Log.dispatcher.install(ConsoleLogger)
    }

    private val scope = CoroutineScope(Job())
    private var connection: Job? = null

    val availability = BluetoothAvailability(Bluetooth.availability).apply { launchIn(scope) }
    val status = Status()
    val movement = Movement()

    private val options = Options {
        filters {
            match {
                services = listOf(sensorTagUuid)
            }
        }
        optionalServices = services
    }

    fun connect(): Unit {
        disconnect() // Clean up previous connection, if any.

        connection = scope.launch {
            val sensorTag = SensorTag(requestPeripheral(options).await())
            sensorTag.establishConnection()
            enableAutoReconnect(sensorTag)

            try {
                sensorTag.gyro.collect(movement::emit)
            } finally {
                sensorTag.disconnect()
            }
        }.apply {
            invokeOnCompletion { cause ->
                Log.info { "invokeOnCompletion $cause" }
                status.emit("Disconnected")
            }
        }
    }

    fun disconnect() {
        connection?.cancel()
        connection = null
    }

    private suspend fun SensorTag.establishConnection(): Unit = coroutineScope {
        status.emit("Connecting")
        connect()
        enableGyro()
        status.emit("Connected")
    }

    private fun CoroutineScope.enableAutoReconnect(
        sensorTag: SensorTag
    ) = sensorTag.state.onEach { state ->
        Log.info { "State: ${state::class.simpleName}" }
        if (state is Disconnected) {
            Log.info { "Waiting 5 seconds to reconnect..." }
            delay(5_000L)
            sensorTag.establishConnection()
        }
    }.launchIn(this)
}
