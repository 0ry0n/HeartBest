package com.github.oryon.heartbest.ui.finger

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.oryon.heartbest.utils.now
import com.github.oryon.heartbest.utils.toSec
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class FingerViewModel : ViewModel() {
    private var mAverageIndex = 0
    private val mAverageSize = 4
    private val mAverage = IntArray(mAverageSize)

    private var mBeatsIndex = 0
    private val mBeatsSize = 3
    private val mBeats = IntArray(mBeatsSize)

    private var mPulse = true
    private var mText = ""

    private var beats = 0
    private var startTime = 0L

    private var mRollingAverage = 0

    fun fingerProcessing(rgba: Mat) {
        val imgAvg = redAvg(rgba.nativeObjAddr)

        if (imgAvg < mRollingAverage) {
            if (mPulse) { // True -> new beat
                beats++
                Imgproc.putText(
                    rgba,
                    "BEAT!!",
                    Point(rgba.cols() * 0.5, rgba.rows() * 0.5),
                    Imgproc.FONT_HERSHEY_PLAIN,
                    2.0,
                    Scalar(255.0, 255.0, 255.0),
                    3
                )
            }

            mPulse = false
        } else if (imgAvg > mRollingAverage) {
            mPulse = true
        }

        if (mAverageIndex == mAverageSize)
            mAverageIndex = 0

        // Save new img average
        mAverage[mAverageIndex] = imgAvg
        mAverageIndex++

        // Average of saved values
        mRollingAverage = 0
        for (i in 0 until mAverageIndex) {
            mRollingAverage += mAverage[i]
        }
        mRollingAverage /= mAverageIndex

        val endTime = now()
        val totalTimeInSecs = (endTime - startTime).toSec()

        if (totalTimeInSecs >= 10) {
            val bps = beats / totalTimeInSecs
            val bpm = (bps * 60).toInt()

            if (bpm in 48..180) {
                if (mBeatsIndex == mBeatsSize)
                    mBeatsIndex = 0

                // Save new bpm value
                mBeats[mBeatsIndex] = bpm
                mBeatsIndex++

                // Average of saved values
                var beatsAvg = 0
                for (i in 0 until mBeatsIndex) {
                    beatsAvg += mBeats[i]
                }
                beatsAvg /= mBeatsIndex

                mText = "Estimate: $beatsAvg bpm"
                Log.i(TAG, mText)
            }

            // New cycle
            startTime = now()
            beats = 0
        }

        Imgproc.putText(
            rgba,
            mText,
            Point(rgba.cols() / 3.0, rgba.rows() * 0.1),
            Imgproc.FONT_HERSHEY_PLAIN,
            2.0,
            textColor,
            3
        )
    }

    // Average 0 to 255
    private external fun redAvg(matAddrRgba: Long): Int

    companion object {
        private const val TAG = "FingerViewModel"

        private val textColor = Scalar(100.0, 255.0, 100.0)
    }
}

@Suppress("UNCHECKED_CAST")
class FingerViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FingerViewModel::class.java))
            return FingerViewModel() as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}