package net.kongbaguni.lightmetter.utill

import android.content.Context
import android.hardware.camera2.CameraManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CameraUsageMonitor(context: Context) {

    private val cameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private val _cameraInUse = MutableStateFlow(false)
    val cameraInUse: StateFlow<Boolean> = _cameraInUse

    private val callback = object : CameraManager.AvailabilityCallback() {

        override fun onCameraAvailable(cameraId: String) {
            _cameraInUse.value = false
        }

        override fun onCameraUnavailable(cameraId: String) {
            _cameraInUse.value = true
        }
    }

    fun start() {
        cameraManager.registerAvailabilityCallback(callback, null)
    }

    fun stop() {
        cameraManager.unregisterAvailabilityCallback(callback)
    }
}