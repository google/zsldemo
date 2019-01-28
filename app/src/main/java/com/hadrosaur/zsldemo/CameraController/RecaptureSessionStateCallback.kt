package com.hadrosaur.zsldemo.CameraController

import android.hardware.camera2.CameraCaptureSession
import android.media.Image
import android.media.ImageWriter
import android.view.Surface
import com.hadrosaur.zsldemo.CameraParams
import com.hadrosaur.zsldemo.MainActivity

class RecaptureSessionStateCallback(val activity: MainActivity, val params: CameraParams, val image: Image) : CameraCaptureSession.StateCallback() {
    override fun onReady(session: CameraCaptureSession) {
        super.onReady(session)
    }

    override fun onCaptureQueueEmpty(session: CameraCaptureSession) {
        super.onCaptureQueueEmpty(session)
    }

    override fun onConfigureFailed(session: CameraCaptureSession) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onClosed(session: CameraCaptureSession) {
        super.onClosed(session)
    }

    override fun onSurfacePrepared(session: CameraCaptureSession, surface: Surface) {
        super.onSurfacePrepared(session, surface)
    }

    override fun onConfigured(session: CameraCaptureSession) {
        params.recaptureImageWriter = ImageWriter.newInstance(session.inputSurface, 1)
        //TODO: after calling this, we should remove the pair from the buffers as the image is no longer accessible
        params.recaptureImageWriter?.queueInputImage(image)
        session.capture(params.recaptureBuilder?.build(), RecaptureSessionCallback(activity, params), params.backgroundHandler)
    }

    override fun onActive(session: CameraCaptureSession) {
        super.onActive(session)
    }

}