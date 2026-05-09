package com.juul.sensortag

import com.juul.kable.Peripheral
import com.juul.kable.State.Disconnected
import com.juul.kable.logs.Logging.Level.Data
import com.juul.khronicle.Log
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.io.IOException

suspend fun CoroutineScope.headlessApp() {
    Log.info(LogTag) { "Searching for SensorTag..." }
    val advertisement = SensorTag.scanner.advertisements.first()
    Log.info(LogTag) { "Found $advertisement" }

    val sensorTag = Peripheral(advertisement) {
        logging {
            level = Data
        }
    }.let(::SensorTag)

    sensorTag.gyro.onEach { rotation ->
        Log.info(LogTag) { rotation.toString() }
    }.launchIn(this)

    Log.info(LogTag) { "Configuring auto connector" }
    sensorTag.state.onEach { state ->
        Log.info(LogTag) { "Received state: $state" }
        if (state is Disconnected) {
            try {
                Log.verbose(LogTag) { "Attempting connection" }
                sensorTag.connect()
            } catch (e: IOException) {
                Log.error(LogTag, e) { "Connect failed." }
                throw e
            }
            Log.verbose(LogTag) { "Waiting to reconnect" }
            delay(2.seconds) // Throttle reconnects so we don't hammer the system if connection immediately drops.
        }
    }.launchIn(this).apply {
        invokeOnCompletion { cause ->
            Log.warn(LogTag, cause) { "Auto connector complete" }
        }
    }
}
