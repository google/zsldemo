package com.hadrosaur.zsldemo

import android.hardware.camera2.TotalCaptureResult
import android.media.Image
import java.util.*

//TODO: there is no reason this could not be managed on the fly with a slider
val CIRCULAR_BUFFER_SIZE = 10

class CircularImageBuffer {
    val buffer: ArrayDeque<Image> = ArrayDeque(CIRCULAR_BUFFER_SIZE)

    fun add(image: Image) {
        if (CIRCULAR_BUFFER_SIZE <= buffer.size)
            buffer.removeLast()

        buffer.add(image)
    }

    fun findMatchingImage(result: TotalCaptureResult) : Image {
        return buffer.first
    }


}