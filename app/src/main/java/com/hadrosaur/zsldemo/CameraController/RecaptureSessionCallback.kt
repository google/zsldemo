package com.hadrosaur.zsldemo.CameraController

import android.hardware.camera2.*
import android.view.Surface
import com.hadrosaur.zsldemo.CameraParams
import com.hadrosaur.zsldemo.MainActivity

class RecaptureSessionCallback(val activity: MainActivity, internal var params: CameraParams) : CameraCaptureSession.CaptureCallback() {
    override fun onCaptureSequenceAborted(session: CameraCaptureSession, sequenceId: Int) {
        super.onCaptureSequenceAborted(session, sequenceId)
    }

    override fun onCaptureCompleted(
        session: CameraCaptureSession,
        request: CaptureRequest,
        result: TotalCaptureResult
    ) {
        super.onCaptureCompleted(session, request, result)
    }

    override fun onCaptureFailed(session: CameraCaptureSession, request: CaptureRequest, failure: CaptureFailure) {
        super.onCaptureFailed(session, request, failure)
    }

    override fun onCaptureSequenceCompleted(session: CameraCaptureSession, sequenceId: Int, frameNumber: Long) {
        super.onCaptureSequenceCompleted(session, sequenceId, frameNumber)
    }

    override fun onCaptureStarted(
        session: CameraCaptureSession,
        request: CaptureRequest,
        timestamp: Long,
        frameNumber: Long
    ) {
        super.onCaptureStarted(session, request, timestamp, frameNumber)
    }

    override fun onCaptureProgressed(
        session: CameraCaptureSession,
        request: CaptureRequest,
        partialResult: CaptureResult
    ) {
        super.onCaptureProgressed(session, request, partialResult)
    }

    override fun onCaptureBufferLost(
        session: CameraCaptureSession,
        request: CaptureRequest,
        target: Surface,
        frameNumber: Long
    ) {
        super.onCaptureBufferLost(session, request, target, frameNumber)
    }

}