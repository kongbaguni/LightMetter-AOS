package net.kongbaguni.lightmetter.utill

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import androidx.core.content.ContextCompat

class LightMetterCameraManager(
    private val context: Context
) {

    private val cameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null

    private val backgroundThread = HandlerThread("LightMeterThread").apply { start() }
    private val backgroundHandler = Handler(backgroundThread.looper)

    fun watch(
        onChangeISO: (Double) -> Unit,
        onChangeShutterSpeed: (Double) -> Unit,
        onChangeAperture: (Double) -> Unit,
        onRequestCameraPermission: () -> Unit,
    ) {
        val hasPermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            // 권한이 없으면 상위(UI) 레이어에 권한 요청을 위임
            onRequestCameraPermission()
            return
        }

        val cameraId = findBackCameraId() ?: return

        imageReader = ImageReader.newInstance(
            320, 240,
            ImageFormat.YUV_420_888,
            2
        )

        cameraManager.openCamera(
            cameraId,
            object : CameraDevice.StateCallback() {

                override fun onOpened(device: CameraDevice) {
                    cameraDevice = device
                    createSession(
                        device,
                        onChangeISO,
                        onChangeShutterSpeed,
                        onChangeAperture
                    )
                }

                override fun onDisconnected(device: CameraDevice) {
                    device.close()
                }

                override fun onError(device: CameraDevice, error: Int) {
                    device.close()
                }
            },
            backgroundHandler
        )
    }

    private fun createSession(
        device: CameraDevice,
        onChangeISO: (Double) -> Unit,
        onChangeShutterSpeed: (Double) -> Unit,
        onChangeAperture: (Double) -> Unit,
    ) {
        val surface = imageReader!!.surface

        val requestBuilder =
            device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {

                addTarget(surface)

                // 🔑 핵심: 자동 노출
                set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)

                // 플래시 / AF 비활성 (노출계니까)
                set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
            }

        device.createCaptureSession(
            listOf(surface),
            object : CameraCaptureSession.StateCallback() {

                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session

                    session.setRepeatingRequest(
                        requestBuilder.build(),
                        object : CameraCaptureSession.CaptureCallback() {

                            override fun onCaptureCompleted(
                                session: CameraCaptureSession,
                                request: CaptureRequest,
                                result: TotalCaptureResult
                            ) {
                                handleResult(
                                    result,
                                    onChangeISO,
                                    onChangeShutterSpeed,
                                    onChangeAperture
                                )
                            }
                        },
                        backgroundHandler
                    )
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            },
            backgroundHandler
        )
    }

    private fun handleResult(
        result: TotalCaptureResult,
        onChangeISO: (Double) -> Unit,
        onChangeShutterSpeed: (Double) -> Unit,
        onChangeAperture: (Double) -> Unit,
    ) {
        result.get(CaptureResult.SENSOR_SENSITIVITY)?.let {
            onChangeISO(it.toDouble())
        }

        result.get(CaptureResult.SENSOR_EXPOSURE_TIME)?.let {
            // ns → sec
            onChangeShutterSpeed(it / 1_000_000_000.0)
        }

        result.get(CaptureResult.LENS_APERTURE)?.let {
            onChangeAperture(it.toDouble())
        }
    }

    private fun findBackCameraId(): String? {
        return cameraManager.cameraIdList.firstOrNull { id ->
            val chars = cameraManager.getCameraCharacteristics(id)

            chars.get(CameraCharacteristics.LENS_FACING) ==
                    CameraCharacteristics.LENS_FACING_BACK
        }
    }

    fun stop() {
        captureSession?.close()
        cameraDevice?.close()
        imageReader?.close()
        backgroundThread.quitSafely()
    }
}