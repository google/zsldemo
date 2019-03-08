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

import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.hadrosaur.zsldemo.CameraController.recaptureRequest
import com.hadrosaur.zsldemo.MainActivity.Companion.Logd
import com.hadrosaur.zsldemo.MainActivity.Companion.camViewModel
import java.io.File

class CaptureImageAvailableListener(private val activity: MainActivity, internal var params: CameraParams) : ImageReader.OnImageAvailableListener {

    override fun onImageAvailable(reader: ImageReader) {
        val image: Image = reader.acquireNextImage()
        camViewModel.getZSLCoordinator().imageBuffer.add(image)

        if (null == params.debugResult || null == params.debugImage) {
            if (null != params.debugResult) {
                params.debugImage = image
                val tempPair = ZSLPair(image, params.debugResult!!)
                recaptureRequest(activity, params, tempPair)
            } else {
                params.debugImage = image
            }
        }

    }
}

class SaveImageAvailableListener(private val activity: MainActivity, internal var params: CameraParams) : ImageReader.OnImageAvailableListener {

    override fun onImageAvailable(reader: ImageReader) {

        params.captureEnd = System.currentTimeMillis()

        val image: Image = reader.acquireNextImage()
        Logd("We got a JPG image!!!! Capture time: " + (params.captureEnd - params.captureStart))
        WriteFile(activity, image)
    }
}

fun deleteTestPhotos(activity: MainActivity) {
    val photosDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), PHOTOS_DIR)

    if (photosDir.exists()) {

        for (photo in photosDir.listFiles())
            photo.delete()

        //Files are deleted, let media scanner know
        val scannerIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        scannerIntent.data = Uri.fromFile(photosDir)
        activity.sendBroadcast(scannerIntent)

        Toast.makeText(activity, "All photos deleted", Toast.LENGTH_SHORT).show()
        Logd("All photos in storage directory DCIM/" + PHOTOS_DIR + " deleted.")
    }
}
