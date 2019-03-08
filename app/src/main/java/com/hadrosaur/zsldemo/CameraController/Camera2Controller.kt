/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hadrosaur.zsldemo.CameraController

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureRequest.CONTROL_ENABLE_ZSL
import android.hardware.camera2.params.InputConfiguration
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageWriter
import android.os.Build
import android.renderscript.ScriptGroup
import android.view.Surface
import android.widget.Toast
import com.hadrosaur.zsldemo.CameraParams
import com.hadrosaur.zsldemo.MainActivity
import com.hadrosaur.zsldemo.MainActivity.Companion.Logd
import com.hadrosaur.zsldemo.ZSLPair
import java.lang.Thread.sleep
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

fun createCameraCaptureSession(activity: MainActivity, camera: CameraDevice, params: CameraParams) {
    try {
        val texture = params.previewTextureView?.surfaceTexture

        if (null == texture)
            return

        Logd("Starting camera preview session")

        val previewSurface = Surface(texture)
        val privateImageReaderSurface = params.privateImageReader?.surface
        val jpegImageReaderSurface = params.jpegImageReader?.surface

        //Set up the preview capture request for when the session is ready
        params.previewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG)
        params.previewBuilder?.addTarget(previewSurface)

        if (privateImageReaderSurface != null)
            params.previewBuilder?.addTarget(privateImageReaderSurface)
        else
            Logd("createCameraCaptureSession: privateImageReaderSurface is NULL!")

        //Create the global capture setting. Note:
        // [output] - preview surface
        // [output] - full-quality private format image reader (for ZSL Buffer)
        // [output] - full-quality jpeg image reader - final output image after reprocessing
        // [input]  - full-quailty private format image writer (input from ZSL Buffer for reprocessing)
        if (params.canReprocess) {
            camera.createReprocessableCaptureSession(
                InputConfiguration(params.maxSize.width, params.maxSize.height, ImageFormat.PRIVATE),
                Arrays.asList(previewSurface, privateImageReaderSurface, jpegImageReaderSurface),
                CameraCaptureSessionStateCallback(activity, params), params.backgroundHandler
            )

        } else {
            activity.runOnUiThread {
                Logd("Sorry, this device cannot implement reprocessing.")
                Toast.makeText(activity, "Sorry, this device cannot implement reprocessing", Toast.LENGTH_LONG).show()
            }
        }
    } catch (e: CameraAccessException) {
        e.printStackTrace()
    } catch (e: IllegalStateException) {
        Logd("createCameraPreviewSession IllegalStateException, aborting: " + e)
    }
}

//We have a good ZSLPair, create the request to reprocess the private image into a jpeg output frame
//Note: we use the same capture session which is already configured for this, we just need to send off the request
fun recaptureRequest(activity: MainActivity, params: CameraParams, zslPair: ZSLPair) {
    params.captureStart = System.currentTimeMillis()

    val jpegImageReaderSurface = params.jpegImageReader?.surface
    params.recaptureBuilder = params.device?.createReprocessCaptureRequest(zslPair.result)
    params.recaptureBuilder?.addTarget(jpegImageReaderSurface)

    params.recaptureImageWriter?.queueInputImage(zslPair.image)
    params.captureSession?.capture(params.recaptureBuilder?.build(), RecaptureRequestCallback(activity, params), params.backgroundHandler)

}

fun stopRepeating(params: CameraParams) {
    params.captureSession?.stopRepeating()
    sleep(1000)
}

fun closeCamera(activity: MainActivity, params: CameraParams?) {
    if (null == params)
        return

    params.captureSession?.close()
    params.device?.close()
    params.isOpen = false
}


