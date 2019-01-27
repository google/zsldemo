package com.hadrosaur.zsldemo

import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.media.Image
import com.hadrosaur.zsldemo.MainActivity.Companion.Logd
import java.util.*

//TODO: there is no reason this could not be managed on the fly with a slider
val CIRCULAR_BUFFER_SIZE = 10

class CircularImageBuffer {
    val buffer: ArrayDeque<Image> = ArrayDeque(CIRCULAR_BUFFER_SIZE)

    fun add(image: Image) {
        if (CIRCULAR_BUFFER_SIZE <= buffer.size) {
            buffer.removeLast().close()
        }

        buffer.add(image)
    }

    fun findMatchingImage(result: TotalCaptureResult) : Image {
        val timestamp: Long? = result.get(CaptureResult.SENSOR_TIMESTAMP)

        //If the best result has no timestamp, we cannot match, return the first image in buffer
        if (timestamp == null)
            return buffer.first

        //Look through the buffer for the image that matches
        for (image in buffer) {
            Logd("Checking timestamp: " + image.timestamp)
            if (image.timestamp == timestamp) {
                return image
            }
        }

        MainActivity.Logd("This did NOT work, cannot find timestamp: " + timestamp)

        //If we didn't find the matching image, just return the latest one
        return buffer.first
    }


}