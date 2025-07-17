package com.adbmocksteps

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import com.adbmocksteps.ui.theme.ADBMockStepsTheme
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val permissions = setOf(
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    private lateinit var healthConnectClient: HealthConnectClient
    private var statusText by mutableStateOf("Checking Health Connect availability...")
    private var isPermissionGranted by mutableStateOf(false)
    private var isHealthConnectAvailable by mutableStateOf(false)
    private var isHealthConnectInstallable by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
        val healthConnectPermissionsLauncher = registerForActivityResult(requestPermissionActivityContract) { granted ->
            Log.d("MainActivity", "Permission result received, re-checking status.")
            checkAvailabilityAndPermissions()
        }

        setContent {
            ADBMockStepsTheme {
                val lastBroadcastInfo by BroadcastStateRepository.lastBroadcast.collectAsState()
                val lifecycleOwner = LocalLifecycleOwner.current
                val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()

                LaunchedEffect(lifecycleState) {
                    if (lifecycleState == Lifecycle.State.RESUMED) {
                        checkAvailabilityAndPermissions()
                    }
                }

                HealthScreen(
                    status = statusText,
                    isPermissionGranted = isPermissionGranted,
                    isHealthConnectAvailable = isHealthConnectAvailable,
                    isHealthConnectInstallable = isHealthConnectInstallable,
                    onGrantPermissions = {
                        healthConnectPermissionsLauncher.launch(permissions)
                    },
                    onInstallUpdate = {
                        redirectToHealthConnectInstallation()
                    },
                    lastBroadcastInfo = lastBroadcastInfo
                )
            }
        }
    }

    private fun redirectToHealthConnectInstallation() {
        val uriString = "https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata"
        startActivity(Intent(Intent.ACTION_VIEW, uriString.toUri()))
    }

    private fun checkAvailabilityAndPermissions() {
        when (HealthConnectClient.getSdkStatus(this)) {
            HealthConnectClient.SDK_AVAILABLE -> {
                healthConnectClient = HealthConnectClient.getOrCreate(this)
                isHealthConnectAvailable = true
                isHealthConnectInstallable = false

                lifecycleScope.launch {
                    val granted = healthConnectClient.permissionController.getGrantedPermissions()
                    if (granted.containsAll(permissions)) {
                        isPermissionGranted = true
                        statusText = "permissions granted ✅"
                    } else {
                        isPermissionGranted = false
                        statusText = "permissions not granted ❌"
                    }
                }
            }
            HealthConnectClient.SDK_UNAVAILABLE -> {
                isHealthConnectAvailable = false
                isPermissionGranted = false
                isHealthConnectInstallable = false
                statusText = "Health Connect is not available on this device :/"
            }
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                statusText = "Health Connect requires to be installed/updated."
                isHealthConnectAvailable = false
                isPermissionGranted = false
                isHealthConnectInstallable = true
            }
        }
    }

}