package com.adbmocksteps

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * A simple data class to hold the information from the broadcast.
 */
data class LastBroadcastInfo(
    val steps: Long,
    val timestamp: String
)

/**
 * A singleton object to hold the state of the last broadcast.
 * Using a StateFlow allows the UI to reactively collect updates.
 */
object BroadcastStateRepository {
    private val _lastBroadcast = MutableStateFlow<LastBroadcastInfo?>(null)
    val lastBroadcast = _lastBroadcast.asStateFlow()

    fun updateLastBroadcast(steps: Long) {
        val currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        _lastBroadcast.value = LastBroadcastInfo(
            steps = steps,
            timestamp = currentTime.format(formatter)
        )
    }
}