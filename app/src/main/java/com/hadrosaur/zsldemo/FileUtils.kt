package com.hadrosaur.zsldemo

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.hadrosaur.zsldemo.MainActivity.Companion.Logd
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

val PHOTOS_DIR = "ZSLDemo"

fun WriteFile(activity: MainActivity, bytes: ByteArray) {
    val jpgFile = getFileHandle(activity, "ZSLDemo", true)

    var output: FileOutputStream? = null
    try {
        output = FileOutputStream(jpgFile)
        output.write(bytes)
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        if (null != output) {
            try {
                output.close()

                //File is written, let media scanner know
                val scannerIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                scannerIntent.data = Uri.fromFile(jpgFile)
                activity.sendBroadcast(scannerIntent)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    Logd("WriteFile: Completed.")
}

fun getFileHandle (activity: MainActivity, name: String, withTimestamp: Boolean) : File {
    val PHOTOS_DIR: String = "ZSLDemo"

    var filePath = File.separatorChar + PHOTOS_DIR + File.separatorChar + name

    if (withTimestamp)
        filePath += "-" + generateTimestamp()

    filePath += ".jpg"

    val jpgFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), filePath)

    val photosDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), PHOTOS_DIR)

    if (!photosDir.exists()) {
        val createSuccess = photosDir.mkdir()
        if (!createSuccess) {
            Toast.makeText(activity, "DCIM/" + PHOTOS_DIR + " creation failed.", Toast.LENGTH_SHORT).show()
            Logd("Photo storage directory DCIM/" + PHOTOS_DIR + " creation failed!!")
        } else {
            Logd("Photo storage directory DCIM/" + PHOTOS_DIR + " did not exist. Created.")
        }
    }

    return jpgFile
}

fun generateTimestamp(): String {
    val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US)
    return sdf.format(Date())
}
