package com.juul.sensortag.coroutines.flow

import kotlin.time.TimeSource
import kotlin.time.TimeSource.Monotonic.ValueTimeMark
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class TimedValue<out T>(val start: ValueTimeMark, val value: T)

fun <T> Flow<T>.withStartTime(): Flow<TimedValue<T>> = flow {
    val startTime = TimeSource.Monotonic.markNow()
    collect { value ->
        emit(TimedValue(startTime, value))
    }
}
