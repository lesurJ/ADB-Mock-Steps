package com.adbmocksteps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class SetStepsBroadcastReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "com.adbmocksteps.SET_STEPS") {
            return
        }

        val pendingResult = goAsync()
        scope.launch {
            try {
                Log.i("ADBMockSteps", "=== BROADCAST RECEIVER TRIGGERED ===")
                val steps = intent.getStringExtra("steps")?.toLongOrNull()
                val duration = intent.getStringExtra("duration")?.toLongOrNull()
                if (steps == null || duration == null) {
                    Log.e("ADBMockSteps", "Invalid extras: steps=$steps, duration=$duration in broadcast.")
                    return@launch
                }
                BroadcastStateRepository.updateLastBroadcast(steps, duration)

                if (HealthConnectClient.getSdkStatus(context) != HealthConnectClient.SDK_AVAILABLE) {
                   Log.e("ADBMockSteps", "Health Connect is not available. Cannot write steps.")
                   return@launch
                }

                val healthConnectClient = HealthConnectClient.getOrCreate(context)
                val requiredPermissions = setOf(HealthPermission.getWritePermission(StepsRecord::class))
                val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()

                if (grantedPermissions.containsAll(requiredPermissions)) {
                    writeSteps(healthConnectClient, steps, duration)
                } else {
                    Log.e("ADBMockSteps", "WRITE_STEPS permission not granted. Please open the app to grant permission.")
                }

            } catch (e: Exception) {
                Log.e("ADBMockSteps", "Error processing broadcast", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun writeSteps(client: HealthConnectClient, steps: Long, duration: Long) {
        try {
            val endTime = Instant.now()
            val startTime = endTime.minus(duration, ChronoUnit.MINUTES)
            val zoneOffset = ZoneOffset.systemDefault().rules.getOffset(startTime)
            val stepsRecord = StepsRecord(
                count = steps,
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = zoneOffset,
                endZoneOffset = zoneOffset,
                metadata = Metadata.autoRecorded(device = Device(type = Device.TYPE_PHONE))
            )

            val records = listOf(stepsRecord)
            client.insertRecords(records)
            Log.i("ADBMockSteps", "Successfully inserted $steps steps with duration $duration minute(s) into Health Connect.")

        } catch (e: Exception) {
            Log.e("ADBMockSteps", "Failed to write steps to Health Connect", e)
        }
    }
}