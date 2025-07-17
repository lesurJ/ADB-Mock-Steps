package com.adbmocksteps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HealthScreen(
    status: String,
    isPermissionGranted: Boolean,
    isHealthConnectAvailable: Boolean,
    isHealthConnectInstallable: Boolean,
    onGrantPermissions: () -> Unit,
    onInstallUpdate: () -> Unit,
    lastBroadcastInfo: LastBroadcastInfo?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "ADB Mock Steps",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            StatusCard(status, isPermissionGranted, isHealthConnectAvailable, isHealthConnectInstallable, onGrantPermissions, onInstallUpdate)
            LastReceivedDataCard(lastBroadcastInfo)
            AdbCommandCard()
        }
    }
}

@Composable
fun StatusCard(
    status: String,
    isPermissionGranted: Boolean,
    isHealthConnectAvailable: Boolean,
    isHealthConnectInstallable: Boolean,
    onGrantPermissions: () -> Unit,
    onInstallUpdate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "🔐 HealthConnect status",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                )
            }
            Spacer(Modifier.height(12.dp))

            if (isHealthConnectAvailable and !isPermissionGranted) {
                Button(
                    onClick = onGrantPermissions,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                ) {
                    Text("Grant Permissions")
                }
            }

            if (!isHealthConnectAvailable and isHealthConnectInstallable) {
                Button(
                    onClick = onInstallUpdate,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                ) {
                    Text("Install/Update Health Connect")
                }
            }
        }
    }
}

@Composable
fun LastReceivedDataCard(lastBroadcastInfo: LastBroadcastInfo?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 Last received broadcast",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            if (lastBroadcastInfo == null) {
                Text(
                    text = "Waiting for first broadcast...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Steps:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = lastBroadcastInfo.steps.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Cyan,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Time:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = lastBroadcastInfo.timestamp,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Cyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AdbCommandCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📱 ADB Command",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "adb shell am broadcast -a com.adbmocksteps.SET_STEPS --es steps \"5000\" -f 0x01000000",
                style = MaterialTheme.typography.bodySmall.copy(
                    lineHeight = 20.sp
                ),
                color = Color(0xFFCCCCCC),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}