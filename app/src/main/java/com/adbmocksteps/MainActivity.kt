package com.adbmocksteps

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import com.adbmocksteps.ui.theme.ADBMockStepsTheme


class MainActivity : ComponentActivity() {

    private val permissions = setOf(
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    private lateinit var healthConnectClient: HealthConnectClient
    private var statusText by mutableStateOf("Checking Health Connect availability...")
    private var isPermissionGranted by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launcher for the Health Connect permission screen
        val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
        val healthConnectPermissionsLauncher = registerForActivityResult(requestPermissionActivityContract) { granted ->
            if (granted.containsAll(permissions)) {
                isPermissionGranted = true
                statusText = "permissions granted ✅"
                Log.i("MainActivity", "All permissions granted.")
            } else {
                isPermissionGranted = false
                statusText = "permissions not granted ❌"
                Log.w("MainActivity", "Not all permissions were granted.")
            }
        }

        checkAvailabilityAndPermissions()

        setContent {
            ADBMockStepsTheme {
                // Check permissions on composition
                LaunchedEffect(Unit) {
                    val granted = healthConnectClient.permissionController.getGrantedPermissions()
                    if (granted.containsAll(permissions)) {
                        isPermissionGranted = true
                        statusText = "permissions granted ✅"
                    } else {
                        isPermissionGranted = false
                        statusText = "permissions not granted ❌"
                    }
                }

                HealthScreen(
                    status = statusText,
                    isPermissionGranted = isPermissionGranted,
                    onGrantPermissions = {
                        healthConnectPermissionsLauncher.launch(permissions)
                    }
                )
            }
        }
    }

    private fun checkAvailabilityAndPermissions() {
        when (HealthConnectClient.getSdkStatus(this)) {
            HealthConnectClient.SDK_AVAILABLE -> {
                healthConnectClient = HealthConnectClient.getOrCreate(this)
                statusText = "Health Connect is available."
//                checkPermissions()
            }
            HealthConnectClient.SDK_UNAVAILABLE -> {
                statusText = "Health Connect is not available on this device."
                isPermissionGranted = false
            }
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                statusText = "Health Connect requires an update. Please update from the Play Store."
                isPermissionGranted = false
                // Optionally redirect to the Play Store
//                val uriString = "market://details?id=${HealthConnectClient.DEFAULT_PROVIDER_PACKAGE_NAME}&url=healthconnect%3A%2F%2Fonboarding"
//                startActivity(
//                    Intent(Intent.ACTION_VIEW).apply {
//                        setPackage("com.android.vending")
//                        data = Uri.parse(uriString)
//                        putExtra("overlay", true)
//                        putExtra("callerId", packageName)
//                    }
//                )
            }
        }
    }

}