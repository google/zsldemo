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

import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.media.ImageWriter
import androidx.annotation.NonNull
import com.hadrosaur.zsldemo.CameraParams
import com.hadrosaur.zsldemo.MainActivity
import com.hadrosaur.zsldemo.MainActivity.Companion.Logd

class CameraCaptureSessionStateCallback(val activity: MainActivity, val params: CameraParams) : CameraCaptureSession.StateCallback() {

    override fun onConfigured(@NonNull cameraCaptureSession: CameraCaptureSession) {
        Logd("Camera Capture Session State Callback: session configured")

        // When the session is ready, we start displaying the preview.
        try {
            params.previewBuilder?.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)

            params.captureSession = cameraCaptureSession
            params.captureSessionCallback = CaptureSessionCallback(activity, params)

            params.recaptureImageWriter = ImageWriter.newInstance(params.captureSession?.inputSurface, 1)

            params.captureSession?.setRepeatingRequest(params.previewBuilder?.build(),
                params.captureSessionCallback, params.backgroundHandler)

        } catch (e: CameraAccessException) {
            MainActivity.Logd("Create Camera Capture Session error: " + params.id)
            e.printStackTrace()

        } catch (e: IllegalStateException) {
            MainActivity.Logd("createCameraCaptureSession onConfigured IllegalStateException, aborting: " + e)
        }
    }

    override fun onConfigureFailed(@NonNull cameraCaptureSession: CameraCaptureSession) {
        MainActivity.Logd("Camera capture session initialization failed.")
    }
}
