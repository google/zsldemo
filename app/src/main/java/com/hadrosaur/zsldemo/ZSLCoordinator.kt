package com.hadrosaur.zsldemo

import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.media.Image

class ZSLPair (val image: Image, val result: TotalCaptureResult){
}

class ZSLCoordinator {
    val imageBuffer = CircularImageBuffer()
    val resultBuffer = CircularResultBuffer()

    //Returns null if no pair found
    fun getBestFrame() : ZSLPair? {
        var bestImage: Image? = null
        var bestResult = resultBuffer.findBestFrame()

        if (bestResult != null) {
            bestImage = imageBuffer.findMatchingImage(bestResult)

            if (bestImage != null) {
                return ZSLPair(bestImage, bestResult)
            }
        }

        return null
    }
}