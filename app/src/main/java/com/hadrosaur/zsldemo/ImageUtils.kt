package com.hadrosaur.zsldemo

import android.graphics.Bitmap
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.util.Log

class CaptureImageAvailableListener(private val activity: MainActivity, internal var params: CameraParams) : ImageReader.OnImageAvailableListener {

    override fun onImageAvailable(reader: ImageReader) {

        val image: Image = reader.acquireNextImage()



        image.close()
    }
}

class SaveImageAvailableListener(private val activity: MainActivity, internal var params: CameraParams) : ImageReader.OnImageAvailableListener {

    override fun onImageAvailable(reader: ImageReader) {

        val image: Image = reader.acquireNextImage()



        image.close()
    }
}
