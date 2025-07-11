package com.pianisimo.grpcandroidclient.utils

import DeviceServiceGrpc
import Mock
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.net.URI

data class DeviceInfo(
    val serialNumber: String = "",
    val ipAddress: String = "",
    val status: String = "",
    val manufacturer: String = ""
)

class MyDeviceServiceGRPC(uri: URI) : Closeable {
    val deviceInfoState = mutableStateOf<DeviceInfo?>(null)
    val isLoading = mutableStateOf(false)

    private val channel = let {
        Log.d("test", "Connecting to ${uri.host}:${uri.port}")
        val builder = ManagedChannelBuilder.forAddress(uri.host, uri.port)
        if (uri.scheme == "https") {
            builder.useTransportSecurity()
        } else {
            builder.usePlaintext()
        }
        builder.executor(Dispatchers.IO.asExecutor()).build()
    }


    private val deviceService = DeviceServiceGrpc.newStub(channel)

    fun requestDownloadTest(cpeId: String) {
        try {
            isLoading.value = true

            val request = Mock.DeviceRequest.newBuilder()
                .setCpeId(cpeId)
                .build()

            val byteArrayOutputStream = ByteArrayOutputStream()

            val responseObserver =
                object : StreamObserver<Mock.DeviceResponse> {
                    override fun onNext(value: Mock.DeviceResponse?) {
                        // Handle the response here
                        value?.let { response ->
                            Log.d(TAG, "Received response: $response")
                            // Extract DeviceInfo from the response and update state
                            if (response.hasDevice()) {
                                val protoDeviceInfo = response.device
                                deviceInfoState.value = DeviceInfo(
                                    serialNumber = protoDeviceInfo.serialNumber,
                                    ipAddress = protoDeviceInfo.ipAddress,
                                    status = protoDeviceInfo.status,
                                    manufacturer = protoDeviceInfo.manufacturer
                                )
                                Log.d(TAG, "Updated device info: ${deviceInfoState.value}")
                            } else {
                                Log.w(TAG, "Response does not contain device info")
                            }
                        }
                    }

                    override fun onError(t: Throwable?) {
                        isLoading.value = false
                        val errorMessage = t?.message ?: "Unknown Error"
                        Log.e(TAG, "gRPC call failed", t)
                    }

                    override fun onCompleted() {
                        isLoading.value = false
                        Log.d(TAG, "gRPC call completed")
                    }
                }

            deviceService.getDeviceInfo(request, responseObserver)
        } catch (e: Exception) {
            Log.e(TAG, "Error in gRPC call", e)
        }
    }

    override fun close() {
        channel.shutdownNow()
    }

    companion object {
        const val TAG = "DeviceServiceGRPC"
    }
}
