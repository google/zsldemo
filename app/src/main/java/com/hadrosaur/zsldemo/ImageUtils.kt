package com.hadrosaur.zsldemo

import android.graphics.Bitmap
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.util.Log
import com.hadrosaur.zsldemo.MainActivity.Companion.Logd
import com.hadrosaur.zsldemo.MainActivity.Companion.camViewModel

class CaptureImageAvailableListener(private val activity: MainActivity, internal var params: CameraParams) : ImageReader.OnImageAvailableListener {

    override fun onImageAvailable(reader: ImageReader) {
        val image: Image = reader.acquireNextImage()
        camViewModel.getZSLCoordinator().imageBuffer.add(image)
    }
}

class SaveImageAvailableListener(private val activity: MainActivity, internal var params: CameraParams) : ImageReader.OnImageAvailableListener {

    override fun onImageAvailable(reader: ImageReader) {

        val image: Image = reader.acquireNextImage()
        Logd("HOLY TOLEDO! We got a JPG image!!!!")
        val bytes = ByteArray(image.planes[0].buffer.remaining())
        image.planes[0].buffer.get(bytes)
        WriteFile(activity, bytes)

        image.close()
    }
}
