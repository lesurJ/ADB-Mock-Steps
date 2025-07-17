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
                if (steps == null) {
                    Log.e("ADBMockSteps", "Invalid or missing 'steps' extra in broadcast.")
                    return@launch
                }

                val healthConnectClient = HealthConnectClient.getOrCreate(context)
                val requiredPermissions = setOf(HealthPermission.getWritePermission(StepsRecord::class))
                val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()

                if (grantedPermissions.containsAll(requiredPermissions)) {
                    writeSteps(healthConnectClient, steps)
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

    private suspend fun writeSteps(client: HealthConnectClient, steps: Long) {
        try {
            // We'll record the steps as having occurred in the last 5 minutes.
            val endTime = Instant.now()
            val startTime = endTime.minus(5, ChronoUnit.MINUTES)

            val stepsRecord = StepsRecord(
                count = steps,
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = null,
                endZoneOffset = null,
                metadata = Metadata.autoRecorded(device = Device(type = Device.TYPE_WATCH))
            )

            val records = listOf(stepsRecord)
            client.insertRecords(records)
            Log.i("ADBMockSteps", "Successfully inserted $steps steps into Health Connect.")

        } catch (e: Exception) {
            Log.e("ADBMockSteps", "Failed to write steps to Health Connect", e)
        }
    }
}