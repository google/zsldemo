package com.hadrosaur.zsldemo

import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import java.util.*

// When users presses the screen, there will probably be a wiggle
// skip a couple of frames to improve picture quality
val FRAMES_TO_SKIP = 2

class CircularResultBuffer {
    val buffer: ArrayDeque<TotalCaptureResult> = ArrayDeque(CIRCULAR_BUFFER_SIZE)

    fun add(result: TotalCaptureResult) {
        if (CIRCULAR_BUFFER_SIZE <= buffer.size) {
            buffer.removeLast()
        }

        buffer.add(result)
    }

    //Returns null if no good frame
    fun findBestFrame() : TotalCaptureResult? {
        val iter = buffer.iterator()
        var bestFrame: TotalCaptureResult? = null
        var backupFrame: TotalCaptureResult? = null
        var iterCount = 0

        // We want frame 3-10, if they are good (AF/AE converged)
        // If no good frames in that range, we're ok with frame 2 or 1 if they seem ok
        while (iter.hasNext()) {
            val tempFrame = iter.next()

            if (iterCount < FRAMES_TO_SKIP) {
                if (isResultGood(tempFrame))
                    backupFrame = tempFrame

            } else {
                if (isResultGood(tempFrame)) {
                    bestFrame = tempFrame
                    break
                }
            }

            iterCount++
        }

        if (null != bestFrame)
            return bestFrame

        if (null != backupFrame)
            return backupFrame

        // Otherwise there are no good frames so we fall back to regular capture
        // Note: we may wish to just choose frame 3 instead of falling back to regular capture in this case to provide
        // consistent capture latency for the user. The exception being if we know we need flash.
        return null
    }

    //We think a frame is good if it's AF is focused and AE is converged and we don't need flash
    fun isResultGood(result: TotalCaptureResult) : Boolean {
        val afState = result.get(CaptureResult.CONTROL_AF_STATE)
        val aeState = result.get(CaptureResult.CONTROL_AE_STATE)

        //Need AF state to be focused and AE state converged
        if ( (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED || afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED)
            && (aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED || aeState == null)) {
            return true
        }

        return false
    }
}