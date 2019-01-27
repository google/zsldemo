package com.hadrosaur.zsldemo

import android.graphics.SurfaceTexture
import android.view.TextureView
import com.hadrosaur.zsldemo.CameraController.camera2OpenCamera
import com.hadrosaur.zsldemo.CameraController.closeCamera
import com.hadrosaur.zsldemo.MainActivity.Companion.Logd

class TextureListener(internal val activity: MainActivity, internal var params: CameraParams, internal val textureView: AutoFitTextureView): TextureView.SurfaceTextureListener {
    override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) {
//        Logd( "In surfaceTextureUpdated. Id: " + params.id)
//        openCamera(params, activity)
    }

    override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
        Logd("In surfaceTextureAvailable. Id: " + params.id)
        camera2OpenCamera(activity, params)
    }

    override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
        Logd("In surfaceTextureSizeChanged. Id: " + params.id)
//        rotatePreviewTexture(activity, params, textureView)
    }

    override fun onSurfaceTextureDestroyed(texture: SurfaceTexture) : Boolean {
        Logd("In surfaceTextureDestroyed. Id: " + params.id)
        closeCamera(activity, params)
        return true
    }
}
