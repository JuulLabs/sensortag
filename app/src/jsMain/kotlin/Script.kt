package com.juul.sensortag

import com.juul.kable.Bluetooth
import com.juul.kable.Options
import com.juul.kable.State.Disconnected
import com.juul.kable.requestPeripheral
import com.juul.khronicle.ConsoleLogger
import com.juul.khronicle.ConstantTagGenerator
import com.juul.khronicle.Log
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.w3c.dom.ErrorEvent
import org.w3c.dom.ErrorEventInit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val scope = CoroutineScope(
    Job().apply { invokeOnCompletion { cause -> console.error("DONE with $cause") } } + CoroutineExceptionHandler { _, cause ->
        // Deliver unhandled errors to `window` so that Chrome shows error overlay.
        // https://github.com/Kotlin/kotlinx.coroutines/issues/2407#issuecomment-1472409187
        window.dispatchEvent(ErrorEvent("error", ErrorEventInit(error = cause)))
    }
)

@JsExport
class Script {

    init {
        Log.tagGenerator = ConstantTagGenerator(tag = "SensorTag")
        Log.dispatcher.install(ConsoleLogger)
    }

    val availability = BluetoothAvailability()
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

    private var connection: Job? = null

    fun initialize() {
        availability.launchIn(scope)
        scope.launch {
            if (!Bluetooth.isAvailable.first()) status.emit("Bluetooth not available")
        }
    }

    fun connect(): Unit {
        disconnect() // Clean up previous connection, if any.

        connection = scope.launch {
            val peripheral = requestPeripheral(options, this) ?: return@launch
            SensorTag(peripheral).apply {
                state.onEach { status.emit(it.toString()) }.launchIn(this@launch)
                establishConnection()
                autoReconnector(5.seconds).launchIn(this@launch)
                try {
                    gyro.collect(movement::emit)
                } finally {
                    status.emit("Disconnected")
                    disconnect()
                }
            }
        }
    }

    fun disconnect() {
        connection?.cancel()
        connection = null
    }

    private suspend fun SensorTag.establishConnection(): Unit = coroutineScope {
        connect()
        enableGyro()
    }

    private fun SensorTag.autoReconnector(
        reconnectDelay: Duration,
    ) = state.filterIsInstance<Disconnected>().onEach {
        Log.info { "Waiting $reconnectDelay to reconnect..." }
        delay(reconnectDelay)
        establishConnection()
    }
}
