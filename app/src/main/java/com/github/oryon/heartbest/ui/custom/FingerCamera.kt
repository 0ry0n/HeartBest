package com.github.oryon.heartbest.ui.custom

import android.content.Context
import android.hardware.Camera
import android.util.AttributeSet
import org.opencv.android.JavaCameraView

class FingerCamera(context: Context, attrs: AttributeSet) : JavaCameraView(context, attrs) {

    fun onFlash() {
        val params = mCamera.parameters
        params.flashMode = Camera.Parameters.FLASH_MODE_TORCH
        mCamera.parameters = params
    }

    fun offFlash() {
        val params = mCamera.parameters
        params.flashMode = Camera.Parameters.FLASH_MODE_OFF
        mCamera.parameters = params
    }


    companion object {
        private const val TAG = "FingerCamera"
    }
}