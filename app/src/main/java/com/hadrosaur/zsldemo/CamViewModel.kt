package com.hadrosaur.zsldemo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CamViewModel : ViewModel() {
    private val cameraParams = CameraParams()
    private val zslCooridinator: ZSLCoordinator = ZSLCoordinator()

    fun getCameraParams(): CameraParams {
        return cameraParams
    }

    fun getZSLCoordinator(): ZSLCoordinator {
        return zslCooridinator
    }

}