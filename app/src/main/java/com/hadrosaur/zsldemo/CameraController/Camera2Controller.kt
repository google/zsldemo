package com.hadrosaur.zsldemo.CameraController

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.InputConfiguration
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageWriter
import android.os.Build
import android.renderscript.ScriptGroup
import android.view.Surface
import com.hadrosaur.zsldemo.CameraParams
import com.hadrosaur.zsldemo.MainActivity
import com.hadrosaur.zsldemo.MainActivity.Companion.Logd
import com.hadrosaur.zsldemo.ZSLPair
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

fun createCameraPreviewSession(activity: MainActivity, camera: CameraDevice, params: CameraParams) {
    try {
        val texture = params.previewTextureView?.surfaceTexture

        if (null == texture)
            return

        Logd("Starting camera preview session")

        val previewSurface = Surface(texture)
        val privateImageReaderSurface = params.privateImageReader?.surface
        val jpegImageReaderSurface = params.jpegImageReader?.surface

        params.previewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        params.previewBuilder?.addTarget(previewSurface)
        params.previewBuilder?.addTarget(privateImageReaderSurface)

        camera.createReprocessableCaptureSession(InputConfiguration(params.maxSize.width, params.maxSize.height, ImageFormat.PRIVATE),
            Arrays.asList(previewSurface, privateImageReaderSurface, jpegImageReaderSurface),
            PreviewSessionStateCallback(activity, params), params.backgroundHandler)


    } catch (e: CameraAccessException) {
        e.printStackTrace()
    } catch (e: IllegalStateException) {
        Logd("createCameraPreviewSession IllegalStateException, aborting: " + e)
    }
}

fun recaptureRequest(activity: MainActivity, params: CameraParams, zslPair: ZSLPair) {

    val jpegImageReaderSurface = params.jpegImageReader?.surface
    params.recaptureBuilder = params.device?.createReprocessCaptureRequest(zslPair.result)
    params.recaptureBuilder?.addTarget(jpegImageReaderSurface)
    //TODO: after calling this, we should remove the pair from the buffers as the image is no longer accessible
    params.recaptureImageWriter?.queueInputImage(zslPair.image)
    params.captureSession?.capture(params.recaptureBuilder?.build(), RecaptureSessionCallback(activity, params), params.backgroundHandler)

}

fun closeCamera(activity: MainActivity, params: CameraParams?) {
    if (null == params)
        return

    params.captureSession?.close()
    params.device?.close()
    params.isOpen = false
}


