package com.github.oryon.heartbest.ui.face

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.oryon.heartbest.data.Complex
import com.github.oryon.heartbest.utils.*
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class FaceViewModel : ViewModel() {
    private val mBufferSize = 32
    private var mDataBuffer = FloatArray(mBufferSize) { 0F }

    private var mTimes = FloatArray(mBufferSize) { 0F }

    private var mL = 0
    private var mFps = 0F

    private var mFft = FloatArray(0) { 0F }
    private var mFreqs = IntArray(0) { 0 }
    private var mBpm = 0

    private val mT0 = now()

    fun faceProcessing(face: Rect, rgba: Mat) {
        // Face
        Imgproc.rectangle(
            rgba,
            face,
            faceColor,
            2
        )
        Imgproc.putText(
            rgba,
            "Face",
            Point(face.x.toDouble(), face.y.toDouble()),
            Imgproc.FONT_HERSHEY_PLAIN,
            1.5,
            textColor,
            2
        )

        // Forehead
        val rectForeHead = subFaceCord(face, 0.5, 0.18, 0.25, 0.15)
        Imgproc.rectangle(
            rgba,
            rectForeHead,
            foreheadColor,
            1
        )
        Imgproc.putText(
            rgba,
            "Forehead",
            Point(rectForeHead.x.toDouble(), rectForeHead.y.toDouble()),
            Imgproc.FONT_HERSHEY_PLAIN,
            1.5,
            textColor,
            2
        )

        // Get forehead rgb sub frame
        val subFrame = rgba.submat(rectForeHead)

        // Save current delta time
        mTimes[mL] = (now() - mT0).toSec()
        // Save current forehead green average
        mDataBuffer[mL] = subFaceMeans(subFrame)
        mL++

        // 32 samples
        if (mL == mBufferSize) {
            val last = mL - 1
            // Calculate HR using a true fps of processor of the computer, not the fps the camera provide
            mFps = mL / (mTimes[last] - mTimes[0])

            val evenTimes = linspace(mTimes[0], mTimes[last], mL)

            // Interpolation by 1
            val interpolated = interp(evenTimes, mTimes, mDataBuffer)

            // Make the signal become more periodic (avoid spectral leakage)
            val ham = hamming(mL)
            for (i in interpolated.indices) {
                interpolated[i] *= ham[i]
            }

            // Average of interpolated value
            val ave = interpolated.average()

            val data = interpolated.map {
                // Subtract the mean from each element and convert the result to Complex
                Complex((it - ave), 0.0)
            }.toTypedArray()

            // Do real fft
            val fft = fft(data)
            val raw = rfft(fft)

            // Get amplitude spectrum
            mFft = raw.map { it.mod().toFloat() }.toFloatArray()

            val freqRange = mL / 2 + 1
            mFreqs = IntArray(freqRange)
            val idx = ArrayList<Int>()
            for (i in 0 until freqRange) {
                // Calculating frequencies
                mFreqs[i] = ((mFps / mL * i) * 60).toInt()

                // Only choose indices with adequate frequencies
                // The range of frequency that HR is supposed to be within
                if (mFreqs[i] in 48..180)
                    idx.add(i)
            }

            // Get pruned values
            val pFft = FloatArray(idx.size)
            val pFreq = IntArray(idx.size)
            for (i in idx.indices) {
                val it = idx[i]
                pFft[i] = mFft[it]
                pFreq[i] = mFreqs[it]
            }

            mFft = pFft
            mFreqs = pFreq

            // Max in the range can be HR
            val idx2 = argmax(pFft)
            mBpm = mFreqs[idx2]

            // RESET
            mL = 0
        }

        val text = "Estimate: $mBpm bpm"
        Log.i(TAG, text)

        Imgproc.putText(
            rgba,
            text,
            Point(rgba.cols() / 3.0, rgba.rows() * 0.1),
            Imgproc.FONT_HERSHEY_PLAIN,
            2.0,
            textColor,
            3
        )
    }

    companion object {
        private const val TAG = "FaceViewModel"

        private val faceColor = Scalar(255.0, 0.0, 0.0)
        private val foreheadColor = Scalar(0.0, 255.0, 0.0)
        private val textColor = Scalar(100.0, 255.0, 100.0)
    }
}

@Suppress("UNCHECKED_CAST")
class FaceViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FaceViewModel::class.java))
            return FaceViewModel() as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}