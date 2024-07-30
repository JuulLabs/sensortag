package com.juul.sensortag

import com.benasher44.uuid.uuidFrom
import com.juul.kable.Bluetooth
import com.juul.kable.IOException
import com.juul.kable.Options
import com.juul.kable.State.Disconnected
import com.juul.kable.requestPeripheral
import com.juul.khronicle.ConsoleLogger
import com.juul.khronicle.ConstantTagGenerator
import com.juul.khronicle.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@JsExport
class Script {

    init {
        Log.tagGenerator = ConstantTagGenerator(tag = "SensorTag")
        Log.dispatcher.install(ConsoleLogger)
    }

    private val scope = CoroutineScope(Job().apply { invokeOnCompletion { println("ROOT CANCELLED! $it") } })
    private var connection: Job? = null

    val availability = BluetoothAvailability(Bluetooth.availability).apply { launchIn(scope) }
    val status = Status()
    val movement = Movement()

    private val options = Options {
        filters {
            match {
                services = listOf(uuidFrom(sensorTagUuid))
            }
        }
        optionalServices = services
    }

    fun connect(): Unit {
        disconnect() // Clean up previous connection, if any.

        connection = scope.launch {
            val peripheral = try {
                status.emit("Requesting device...")
                requestPeripheral(options, this)
            } catch (e: IOException) {
                status.emit(e.message ?: e.toString())
                console.warn(e.stackTraceToString()) // FIXME: Delete
                return@launch
            }

            if (peripheral != null) {
                val sensorTag = SensorTag(peripheral).apply {
                    establishConnection()
                    autoReconnector(reconnectDelay = 5.seconds)
                        .launchIn(this@launch)
                }

                try {
                    sensorTag.gyro.collect(movement::emit)
                } finally {
                    status.emit("Disconnected")
                    sensorTag.disconnect()
                }
            } else {
                status.emit("Device picker dismissed")
            }
        }.apply {
            invokeOnCompletion { cause ->
                Log.info { "invokeOnCompletion $cause" }
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

    private fun SensorTag.autoReconnector(
        reconnectDelay: Duration,
    ) = state.onEach { state ->
        Log.info { "State: ${state::class.simpleName}" }
        if (state is Disconnected) {
            Log.info { "Waiting $reconnectDelay to reconnect..." }
            delay(reconnectDelay)
            establishConnection()
        }
    }
}
