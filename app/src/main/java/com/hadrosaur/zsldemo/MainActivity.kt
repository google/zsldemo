package com.hadrosaur.zsldemo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.hadrosaur.zsldemo.CameraController.camera2OpenCamera

class MainActivity : AppCompatActivity() {

    private val REQUEST_CAMERA_PERMISSION = 1

    companion object {
        private val LOG_TAG = "ZSLDemo"
        lateinit var camViewModel:CamViewModel

        fun Logd(message: String) {
            Log.d(LOG_TAG, message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        camViewModel = ViewModelProviders.of(this).get(CamViewModel::class.java)

        if (checkCameraPermissions()) {
            setupCameraParams(this, camViewModel.getCameraParams())
        }
    }

    fun checkCameraPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            !== PackageManager.PERMISSION_GRANTED) {

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //We now have permission, restart the app
                    val intent = getIntent()
                    finish()
                    startActivity(intent)
                } else {
                }
                return
            }
        }
    }

    private fun startBackgroundThread(params: CameraParams) {
        if (params.backgroundThread == null) {
            params.backgroundThread = HandlerThread(LOG_TAG).apply {
                this.start()
                params.backgroundHandler = Handler(this.getLooper())
            }
        }
    }


    private fun stopBackgroundThread(params: CameraParams) {
        params.backgroundThread?.quitSafely()
        try {
            params.backgroundThread?.join()
        } catch (e: InterruptedException) {
            Logd( "Interrupted while shutting background thread down: " + e.message)
        }
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread(camViewModel.getCameraParams())
    }

    override fun onPause() {
        stopBackgroundThread(camViewModel.getCameraParams())
        super.onPause()
    }

}
