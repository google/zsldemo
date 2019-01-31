package com.hadrosaur.zsldemo.CameraController

import android.hardware.camera2.*
import android.view.Surface
import com.hadrosaur.zsldemo.CameraParams
import com.hadrosaur.zsldemo.MainActivity
import com.hadrosaur.zsldemo.MainActivity.Companion.Logd
import com.hadrosaur.zsldemo.MainActivity.Companion.camViewModel

class CaptureSessionCallback(val activity: MainActivity, internal var params: CameraParams) : CameraCaptureSession.CaptureCallback() {
    override fun onCaptureCompleted(
        session: CameraCaptureSession,
        request: CaptureRequest,
        result: TotalCaptureResult
    ) {
        camViewModel.getZSLCoordinator().resultBuffer.add(result)
        super.onCaptureCompleted(session, request, result)
    }
}