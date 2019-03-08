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

package com.hadrosaur.zsldemo

import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.hardware.camera2.CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL
import android.media.Image
import android.media.ImageReader
import android.media.ImageWriter
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.hadrosaur.zsldemo.CameraController.CameraDeviceStateCallback
import com.hadrosaur.zsldemo.CameraController.CaptureSessionCallback
import com.hadrosaur.zsldemo.MainActivity.Companion.Logd
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class CameraParams {
    var id = "0"
    var device: CameraDevice? = null
    var characteristics: CameraCharacteristics? = null

    var isOpen = false
    var canReprocess = false

    var cameraDeviceStateCallback: CameraDeviceStateCallback? = null
    var captureSessionCallback: CaptureSessionCallback? = null

    var backgroundThread: HandlerThread? = null
    var backgroundHandler: Handler? = null

    var previewBuilder: CaptureRequest.Builder? = null
    var captureBuilder: CaptureRequest.Builder? = null
    var recaptureBuilder: CaptureRequest.Builder? = null
    var captureSession: CameraCaptureSession? = null

    var previewTextureView: AutoFitTextureView? = null
    var jpegImageReader: ImageReader? = null
    var privateImageReader: ImageReader? = null
    var recaptureImageWriter: ImageWriter? = null

    var captureImageAvailableListener: CaptureImageAvailableListener? = null
    var saveImageAvailableListener: SaveImageAvailableListener? = null

    var minSize: Size = Size(0, 0)
    var maxSize: Size = Size(0, 0)

    var minJpegSize: Size = Size(0, 0)
    var maxJpegSize: Size = Size(0, 0)

    //For latency measurements
    var captureStart: Long = 0
    var captureEnd: Long = 0

    var debugImage: Image? = null
    var debugResult: TotalCaptureResult? = null
}

fun setupCameraParams(activity: MainActivity, params: CameraParams) {
    val manager = activity.getSystemService(AppCompatActivity.CAMERA_SERVICE) as CameraManager

    params.apply {
        try {
            //Default to the first camera, which will normally be "0" (Rear)
            id = "0"
            if (!manager.cameraIdList.contains("0")) {
                //For devices with no rear camera, like many chromebooks, this will probably be "1" (Front)
                //This program cannot handle devices with no camera
                for (cameraId in manager.cameraIdList) {
                    id = cameraId
                }
            }
        } catch (accessError: CameraAccessException) {
            accessError.printStackTrace()
        }
        characteristics = manager.getCameraCharacteristics(id)

        previewTextureView = activity.texture_foreground
        previewTextureView?.surfaceTextureListener = TextureListener(activity, params, activity.texture_foreground)

        captureImageAvailableListener = CaptureImageAvailableListener(activity, params)
        saveImageAvailableListener = SaveImageAvailableListener(activity, params)

        val cameraCapabilities = manager.getCameraCharacteristics(id).get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
        for (capability in cameraCapabilities) {
            when (capability) {
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_PRIVATE_REPROCESSING -> canReprocess = true
            }
        }
        Logd("Camera can reprocess: " + canReprocess)
        Logd("Supported Hardware Level: " + characteristics?.get(INFO_SUPPORTED_HARDWARE_LEVEL))

        //Get image capture sizes
        val map = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        if (map != null) {
//            Logd("Input formats: " + Arrays.toString(map.inputFormats))
//            Logd("Output formats: " + Arrays.toString(map.outputFormats))

            maxSize = Collections.max(
                Arrays.asList(*map.getOutputSizes(ImageFormat.PRIVATE)),
                CompareSizesByArea())

            //Camera devices can only support up to 1920x1080 with PRIVATE and preview
//            maxSize = chooseSmallEnoughSize(map.getOutputSizes(ImageFormat.PRIVATE), 1920, 1080)

            minSize = Collections.min(
                Arrays.asList(*map.getOutputSizes(ImageFormat.PRIVATE)),
                CompareSizesByArea())

            maxJpegSize = Collections.max(
                Arrays.asList(*map.getOutputSizes(ImageFormat.JPEG)),
                CompareSizesByArea())
            minJpegSize = Collections.max(
                Arrays.asList(*map.getOutputSizes(ImageFormat.JPEG)),
                CompareSizesByArea())

//            Logd("Max width: " + maxSize.width + " Max height: " + maxSize.height)
//            Logd("Min width: " + minSize.width + " Min height: " + minSize.height)

/*            for (size in map.getOutputSizes(ImageFormat.PRIVATE)) {
                Logd("Supported size: " + size.width + "x" + size.height)
            }
*/
            setupImageReaders(activity, params)
        } //if map != null

    }

}

fun setupImageReaders(activity: MainActivity, params: CameraParams) {
    with (params) {
        params.jpegImageReader?.close()
        params.privateImageReader?.close()
        params.recaptureImageWriter?.close()

//                jpegImageReader = ImageReader.newInstance(3264, 2448,
//            ImageFormat.JPEG, /*maxImages*/CIRCULAR_BUFFER_SIZE + 1)

//        privateImageReader = ImageReader.newInstance(3264, 2448,
//            ImageFormat.PRIVATE, /*maxImages*/CIRCULAR_BUFFER_SIZE + 1)

        jpegImageReader = ImageReader.newInstance(maxJpegSize.width, maxJpegSize.height,
            ImageFormat.JPEG, /*maxImages*/CIRCULAR_BUFFER_SIZE + 1)

        privateImageReader = ImageReader.newInstance(maxSize.width, maxSize.height,
            ImageFormat.PRIVATE, /*maxImages*/CIRCULAR_BUFFER_SIZE + 1)

        privateImageReader?.setOnImageAvailableListener(captureImageAvailableListener, backgroundHandler)
        jpegImageReader?.setOnImageAvailableListener(saveImageAvailableListener, backgroundHandler)

        //For some cameras, using the max preview size can conflict with big image captures
        //We just uses the smallest preview size to avoid this situation
        params.previewTextureView?.surfaceTexture?.setDefaultBufferSize(minSize.width, minSize.height)
        params.previewTextureView?.setAspectRatio(minSize.width, minSize.height)
    }
}

internal class CompareSizesByArea : Comparator<Size> {
    override fun compare(lhs: Size, rhs: Size): Int {
        // We cast here to ensure the multiplications won't overflow
        return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
    }
}

/**
 * Given `choices` of `Size`s supported by a camera, chooses the largest one whose
 * width and height are at less than the given max values
 * @param choices The list of sizes that the camera supports for the intended output class
 * @param width The maximum desired width
 * @param height The maximum desired height
 * @return The optimal `Size`, or an arbitrary one if none were big enough
 */
internal fun chooseSmallEnoughSize(choices: Array<Size>, width: Int, height: Int): Size {
    val smallEnough = ArrayList<Size>()
    for (option in choices) {
        if (option.width <= width && option.height <= height) {
            smallEnough.add(option)
        }
    }
    // Pick the smallest of those, assuming we found any
    if (smallEnough.size > 0) {
        return Collections.max(smallEnough, CompareSizesByArea())
    } else {
        Logd("Couldn't find any suitable preview size")
        return choices[0]
    }
}
