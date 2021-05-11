package com.github.oryon.heartbest.ui.finger

import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.github.oryon.heartbest.databinding.ActivityFingerBinding
import com.github.oryon.heartbest.ui.custom.FingerCamera
import org.opencv.android.*
import org.opencv.core.Mat

class FingerActivity : CameraActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private lateinit var binding: ActivityFingerBinding

    private lateinit var viewModel: FingerViewModel

    private lateinit var mOpenCvCameraView: FingerCamera

    private lateinit var mRgba: Mat

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    mOpenCvCameraView.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // View
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Binding
        binding = ActivityFingerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ViewModel
        viewModel =
            ViewModelProvider(this, FingerViewModelFactory()).get(FingerViewModel::class.java)

        mOpenCvCameraView = binding.fingerCamera
        mOpenCvCameraView.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView.setCvCameraViewListener(this)
    }

    override fun onPause() {
        super.onPause()
        mOpenCvCameraView.disableView()
    }

    override fun onResume() {
        super.onResume()
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        } else {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mOpenCvCameraView.disableView()
    }

    override fun getCameraViewList(): List<CameraBridgeViewBase> {
        return listOf(mOpenCvCameraView)
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        mRgba = Mat()

        mOpenCvCameraView.onFlash()
    }

    override fun onCameraViewStopped() {
        mRgba.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        mRgba = inputFrame.rgba()

        viewModel.fingerProcessing(mRgba)

        return mRgba
    }

    companion object {
        private const val TAG = "FingerActivity"

        init {
            // Load native library after(!) OpenCV initialization
            System.loadLibrary("native-lib")
        }
    }
}