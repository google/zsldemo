package com.hadrosaur.zsldemo.CameraController

import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.media.ImageWriter
import android.os.Build
import androidx.annotation.NonNull
import com.hadrosaur.zsldemo.CameraParams
import com.hadrosaur.zsldemo.MainActivity
import com.hadrosaur.zsldemo.MainActivity.Companion.Logd

class PreviewSessionStateCallback(val activity: MainActivity, val params: CameraParams) : CameraCaptureSession.StateCallback() {
    override fun onActive(session: CameraCaptureSession?) {
        Logd("Preview Session State Callback: session active")
        super.onActive(session)
    }

    override fun onReady(session: CameraCaptureSession?) {
        super.onReady(session)
    }

    override fun onConfigured(@NonNull cameraCaptureSession: CameraCaptureSession) {
        Logd("Preview Session State Callback: session configured")

        // When the session is ready, we start displaying the preview.
        try {
            params.previewBuilder?.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)

            params.captureSession = cameraCaptureSession
            params.previewSessionCallback = PreviewSessionCallback(activity, params)
//            params.state = STATE_PREVIEW

            params.recaptureImageWriter = ImageWriter.newInstance(params.captureSession?.inputSurface, 1)

            params.captureSession?.setRepeatingRequest(params.previewBuilder?.build(),
                params.previewSessionCallback, params.backgroundHandler)

        } catch (e: CameraAccessException) {
            MainActivity.Logd("Create Preview Session error: " + params.id)
            e.printStackTrace()

        } catch (e: IllegalStateException) {
            MainActivity.Logd("createCameraPreviewSession onConfigured IllegalStateException, aborting: " + e)
        }
    }

    override fun onConfigureFailed(@NonNull cameraCaptureSession: CameraCaptureSession) {
        MainActivity.Logd("Camera preview initialization failed.")
    }
}
