package com.github.oryon.heartbest.ui.face

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.github.oryon.heartbest.R
import com.github.oryon.heartbest.databinding.ActivityFaceBinding
import org.opencv.android.*
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import org.opencv.objdetect.Objdetect
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class FaceActivity : CameraActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private lateinit var binding: ActivityFaceBinding

    private lateinit var viewModel: FaceViewModel

    private lateinit var mOpenCvCameraView: CameraBridgeViewBase

    private lateinit var mJavaDetector: CascadeClassifier

    private lateinit var mRgba: Mat
    private lateinit var mGray: Mat

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")

                    try {
                        // load cascade file from application resources
                        val inputStream: InputStream =
                            resources.openRawResource(R.raw.haarcascade_frontalface_default)

                        val cascadeDir: File = getDir("cascade", Context.MODE_PRIVATE)
                        val cascadeFile = File(cascadeDir, "haarcascade_frontalface_default.xml")

                        val os = FileOutputStream(cascadeFile)
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also {
                                bytesRead = it
                            } != -1) {
                            os.write(buffer, 0, bytesRead)
                        }

                        inputStream.close()
                        os.close()

                        mJavaDetector = CascadeClassifier(cascadeFile.absolutePath)
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier")
                        } else Log.i(
                            TAG,
                            "Loaded cascade classifier from " + cascadeFile.absolutePath
                        )
                        cascadeDir.delete()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Log.e(
                            TAG,
                            "Failed to load cascade. Exception thrown: $e"
                        )
                    }

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
        binding = ActivityFaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ViewModel
        viewModel = ViewModelProvider(this, FaceViewModelFactory()).get(FaceViewModel::class.java)

        mOpenCvCameraView = binding.faceCamera
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
        mGray = Mat()
    }

    override fun onCameraViewStopped() {
        mGray.release()
        mRgba.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        mRgba = inputFrame.rgba()
        mGray = inputFrame.gray()

        // Rotate 90º counter-clockwise
        Core.flip(mRgba.t(), mRgba, 0)
        Core.flip(mGray.t(), mGray, 0)

        Imgproc.equalizeHist(mGray, mGray)

        val faces = MatOfRect()
        mJavaDetector.detectMultiScale(
            mGray,
            faces,
            1.3,
            4,
            Objdetect.CASCADE_SCALE_IMAGE,
            Size(50.0, 50.0)
        )

        faces.toArray().let {
            if (it.isNotEmpty()) {
                viewModel.faceProcessing(it[0], mRgba)
            }
        }

        return mRgba
    }

    companion object {
        private const val TAG = "FaceActivity"

        init {
            // Load native library after(!) OpenCV initialization
            System.loadLibrary("native-lib")
        }
    }
}