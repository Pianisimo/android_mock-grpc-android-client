package com.pianisimo.grpcandroidclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pianisimo.grpcandroidclient.ui.theme.GRPCAndroidClientTheme
import com.pianisimo.grpcandroidclient.utils.DeviceInfo
import com.pianisimo.grpcandroidclient.utils.MyDeviceServiceGRPC
import java.net.URI

class MainActivity : ComponentActivity() {
    private val address = "18.221.226.156"
    private val port = 50051
    private val uri = URI("grpc://$address:$port")
    private val myDeviceServiceGRPC by lazy { MyDeviceServiceGRPC(uri) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GRPCAndroidClientTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DeviceInfoScreen(
                        grpcService = myDeviceServiceGRPC,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        myDeviceServiceGRPC.close()
    }
}

@Composable
fun DeviceInfoScreen(
    grpcService: MyDeviceServiceGRPC,
    modifier: Modifier = Modifier
) {
    var cpeId by remember { mutableStateOf("") }
    val deviceInfo by grpcService.deviceInfoState
    val isLoading by grpcService.isLoading

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "Device Info Lookup",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // CPE ID Input Field
        OutlinedTextField(
            value = cpeId,
            onValueChange = { cpeId = it },
            label = { Text("CPE ID") },
            placeholder = { Text("Enter CPE ID") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        // Execute Button
        Button(
            onClick = {
                if (cpeId.isNotBlank()) {
                    grpcService.requestDownloadTest(cpeId.trim())
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && cpeId.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Loading...")
            } else {
                Text("Get Device Info")
            }
        }

        // Device Info Display
        DeviceInfoDisplay(deviceInfo = deviceInfo)
    }
}

@Composable
fun DeviceInfoDisplay(
    deviceInfo: DeviceInfo?,
    modifier: Modifier = Modifier
) {
    when (deviceInfo) {
        null -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No device info available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        else -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Device Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Divider()

                    DeviceInfoRow(label = "Serial Number", value = deviceInfo.serialNumber)
                    DeviceInfoRow(label = "IP Address", value = deviceInfo.ipAddress)
                    DeviceInfoRow(label = "Status", value = deviceInfo.status)
                    DeviceInfoRow(label = "Manufacturer", value = deviceInfo.manufacturer)
                }
            }
        }
    }
}

@Composable
fun DeviceInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value.ifEmpty { "N/A" },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}