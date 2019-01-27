package com.hadrosaur.zsldemo.CameraController

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.Build
import android.view.Surface
import com.hadrosaur.zsldemo.CameraParams
import com.hadrosaur.zsldemo.MainActivity
import com.hadrosaur.zsldemo.MainActivity.Companion.Logd
import java.util.*

fun camera2OpenCamera(activity: MainActivity, params: CameraParams) {
    val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    try {
        params.cameraDeviceStateCallback = CameraDeviceStateCallback(activity, params)
        manager.openCamera(params.id, params.cameraDeviceStateCallback, params.backgroundHandler)

    } catch (e: CameraAccessException) {
        Logd("openCamera CameraAccessException: " + params.id)
        e.printStackTrace()
    } catch (e: SecurityException) {
        Logd("openCamera SecurityException: " + params.id)
        e.printStackTrace()
    }

    params.isOpen = true
}

//TODO: implement preview session from BasicBokeh
fun createCameraPreviewSession(activity: MainActivity, camera: CameraDevice, params: CameraParams) {
    try {
        val texture = params.previewTextureView?.surfaceTexture

        if (null == texture)
            return

        Logd("Starting camera preview session")

        val previewSurface = Surface(texture)
        val privateImageReaderSurface = params.privateImageReader?.surface

        params.previewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        params.previewBuilder?.addTarget(previewSurface)
        params.previewBuilder?.addTarget(privateImageReaderSurface)

        camera.createCaptureSession(
            Arrays.asList(previewSurface, privateImageReaderSurface),
            PreviewSessionStateCallback(activity, params), params.backgroundHandler)

    } catch (e: CameraAccessException) {
        e.printStackTrace()
    } catch (e: IllegalStateException) {
        Logd("createCameraPreviewSession IllegalStateException, aborting: " + e)
    }
}


fun closeCamera(activity: MainActivity, params: CameraParams?) {
    if (null == params)
        return

    params.captureSession?.close()
    params.device?.close()
    params.isOpen = false
}


