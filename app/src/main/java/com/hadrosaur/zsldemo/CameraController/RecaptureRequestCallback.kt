package com.hadrosaur.zsldemo.CameraController

import android.hardware.camera2.*
import android.view.Surface
import com.hadrosaur.zsldemo.CameraParams
import com.hadrosaur.zsldemo.MainActivity

class RecaptureRequestCallback(val activity: MainActivity, internal var params: CameraParams) : CameraCaptureSession.CaptureCallback() {
}