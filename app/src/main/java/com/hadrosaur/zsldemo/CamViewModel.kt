package com.hadrosaur.zsldemo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CamViewModel : ViewModel() {
    private val cameraParams = CameraParams()

    fun getCameraParams(): CameraParams {
        return cameraParams
    }
}