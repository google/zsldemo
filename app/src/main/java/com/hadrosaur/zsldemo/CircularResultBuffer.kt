package com.hadrosaur.zsldemo

import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import com.hadrosaur.zsldemo.MainActivity.Companion.Logd
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

        buffer.addFirst(result)
    }

    //Returns null if no good frame
    fun findBestFrame() : TotalCaptureResult? {
        val resultArray = arrayOfNulls<TotalCaptureResult>(buffer.size)
        buffer.toArray(resultArray)
        var bestFrame: TotalCaptureResult? = null
        var backupFrame: TotalCaptureResult? = null

        // We want frame 3-10, if they are good (AF/AE converged)
        // If no good frames in that range, we're ok with frame 2 or 1 if they seem ok
        for (i in 0 until resultArray.size) {
            if (resultArray[i] == null)
                continue

            if (i < FRAMES_TO_SKIP) {
                if (isResultGood(resultArray[i]!!)) {
                    backupFrame = resultArray[i]!!
                }
            } else {
                if (isResultGood(resultArray[i]!!)) {
                    bestFrame = resultArray[i]!!
                    break
                }
            }
        }

        if (null != bestFrame)
            return bestFrame

        if (null != backupFrame)
            return backupFrame

        Logd("CircularResultBuffer: Could not find focused frame 1-10.")

        //If we didn't find the matching image, or there is no timestamp just return one
        //Note: we pick the 3rd newest if we have it to account for the finger press causing capture to be unfocused
        //TODO: Add check for flash
        if (buffer.size >= 3)
            return buffer.elementAt(2)
        else
            return buffer.first
    }

    //We think a frame is good if it's AF is focused and AE is converged and we don't need flash
    fun isResultGood(result: TotalCaptureResult) : Boolean {
        val afState = result.get(CaptureResult.CONTROL_AF_STATE)
        val aeState = result.get(CaptureResult.CONTROL_AE_STATE)

//        Logd("Auto-Focus State: " + afState)
//        Logd("Auto-Exposure State: " + aeState)

        //Need AF state to be focused and AE state converged
        if ( (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED || afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED)
            && (aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED || aeState == null)) {
            return true
        }

        return false
    }

    fun remove(result: TotalCaptureResult) {
        buffer.remove(result)
    }
}