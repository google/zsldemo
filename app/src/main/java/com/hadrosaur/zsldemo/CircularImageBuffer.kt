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

        buffer.addFirst(image)
    }

    fun findMatchingImage(result: TotalCaptureResult) : Image {
        val timestamp: Long? = result.get(CaptureResult.SENSOR_TIMESTAMP)

        if (timestamp != null) {
            //Look through the buffer for the image that matches
            for (image in buffer) {
                if (image.timestamp == timestamp) {
                    return image
                }
            }
        }

        //If we didn't find the matching image, or there is no timestamp just return one
        //Note: we pick the 3rd newest if we have it to account for the finger press causing capture to be unfocused
        if (buffer.size >= 3)
            return buffer.elementAt(2)
        else
            return buffer.first
    }

    fun remove(image: Image) {
        buffer.remove(image)
    }

}