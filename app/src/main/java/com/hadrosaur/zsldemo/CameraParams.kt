package com.hadrosaur.zsldemo

import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.media.ImageWriter
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.hadrosaur.zsldemo.CameraController.CameraDeviceStateCallback
import com.hadrosaur.zsldemo.CameraController.PreviewSessionCallback
import com.hadrosaur.zsldemo.MainActivity.Companion.Logd
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class CameraParams {
    var id = "0"
    var device: CameraDevice? = null
    var characteristics: CameraCharacteristics? = null

    var cameraDeviceStateCallback: CameraDeviceStateCallback? = null
    var previewSessionCallback: PreviewSessionCallback? = null

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
}

fun setupCameraParams(activity: MainActivity, params: CameraParams) {
    val manager = activity.getSystemService(AppCompatActivity.CAMERA_SERVICE) as CameraManager

    params.apply {
        try {
            //Default to the first camera, which will normally be "0" (Rear)
            //For devices with no rear camera, like many chromebooks, this will probably be "1" (Front)
            //This program cannot handle devices with no camera
            for (cameraId in manager.cameraIdList) {
                id = cameraId
            }

        } catch (accessError: CameraAccessException) {
            accessError.printStackTrace()
        }
        characteristics = manager.getCameraCharacteristics(id)

        previewTextureView = activity.texture_foreground
        previewTextureView?.surfaceTextureListener = TextureListener(activity, params, activity.texture_foreground)

        captureImageAvailableListener = CaptureImageAvailableListener(activity, params)
        saveImageAvailableListener = SaveImageAvailableListener(activity, params)

        //Get image capture sizes
        val map = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        if (map != null) {
            //Camera devices can only support up to 1920x1080 with PRIVATE and preview
            maxSize = chooseSmallEnoughSize(map.getOutputSizes(ImageFormat.PRIVATE), 1920, 1080)
            minSize = Collections.min(
                Arrays.asList(*map.getOutputSizes(ImageFormat.PRIVATE)),
                CompareSizesByArea())

            Logd("Max width: " + maxSize.width + " Max height: " + maxSize.height)

            setupImageReaders(activity, params)
        } //if map != null

    }

}

fun setupImageReaders(activity: MainActivity, params: CameraParams) {
    with (params) {
        params.jpegImageReader?.close()
        params.privateImageReader?.close()
        params.recaptureImageWriter?.close()
        jpegImageReader = ImageReader.newInstance(maxSize.width, maxSize.height,
            ImageFormat.JPEG, /*maxImages*/1)
        privateImageReader = ImageReader.newInstance(maxSize.width, maxSize.height,
            ImageFormat.PRIVATE, /*maxImages*/1)
        recaptureImageWriter = ImageWriter.newInstance(privateImageReader?.surface, 1)

        privateImageReader?.setOnImageAvailableListener(
            captureImageAvailableListener, backgroundHandler)
        jpegImageReader?.setOnImageAvailableListener(
            saveImageAvailableListener, backgroundHandler)

        //For some cameras, using the max preview size can conflict with big image captures
        //We just uses the smallest preview size to avoid this situation
        params.previewTextureView?.surfaceTexture?.setDefaultBufferSize(minSize.width, minSize.height)
        params.previewTextureView?.setAspectRatio(minSize.width, minSize.height)
//        params.previewTextureView?.surfaceTexture?.setDefaultBufferSize(640, 480)
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
