/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
//        Logd("In surfaceTextureAvailable. Id: " + params.id)
        camera2OpenCamera(activity, params)
    }

    override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
//        Logd("In surfaceTextureSizeChanged. Id: " + params.id)
//        rotatePreviewTexture(activity, params, textureView)
    }

    override fun onSurfaceTextureDestroyed(texture: SurfaceTexture) : Boolean {
//        Logd("In surfaceTextureDestroyed. Id: " + params.id)
        closeCamera(activity, params)
        return true
    }
}
