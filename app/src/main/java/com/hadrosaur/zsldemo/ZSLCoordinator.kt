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

import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.media.Image
import android.util.Log
import com.hadrosaur.zsldemo.CameraController.recaptureRequest
import com.hadrosaur.zsldemo.CameraController.stopRepeating
import com.hadrosaur.zsldemo.MainActivity.Companion.Logd

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
        } else {
            Logd("Best result == null. No good frames found.")
        }

        return null
    }

    fun capturePhoto(activity: MainActivity, params: CameraParams) {
//stopRepeating(params)
        val bestPair: ZSLPair? = getBestFrame()

//        listTimestamps()

        if (bestPair != null) {
//            Logd("Found a good image/result pair. Doing recapture and saving to disk!")
            Logd("Doing recapture. Timestamp Image: " + bestPair.image.timestamp + ", timestamp Result: " + bestPair.result.get(CaptureResult.SENSOR_TIMESTAMP))
            recaptureRequest(activity, params, bestPair)

            //Now remove the bestPair from the buffer so we don't try to access the image again
            imageBuffer.remove(bestPair.image)
            resultBuffer.remove(bestPair.result)

        } else {
            //Do regular capture
            Logd("No best frame found. Fallback to regular capture.")
        }
    }

    fun listTimestamps() {
        val resultStamps = ArrayList<Long>()
        for (result in resultBuffer.buffer) {
            if (result.get(CaptureResult.SENSOR_TIMESTAMP) != null)
                resultStamps.add(result.get(CaptureResult.SENSOR_TIMESTAMP)!!)
        }
        val imageStamps = ArrayList<Long>()
        for (image in imageBuffer.buffer)
            imageStamps.add(image.timestamp)

        Logd("Listing Timestamps. " + resultStamps.size + " result stamps. " + imageStamps.size + " image stamps.")
        if (resultStamps.size != imageStamps.size) {
            Logd("Sizes are different not listing!!")
        } else {
            for (i in 0 until resultStamps.size) {
                Logd("Result: " + resultStamps[i] + "   Image: " + imageStamps[i])
            }
        }
    }
}