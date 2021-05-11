package com.github.oryon.heartbest.utils

import org.opencv.core.Mat
import org.opencv.core.Rect

fun subFaceCord(faceRect: Rect, x: Double, y: Double, w: Double, h: Double): Rect {
    val fx = faceRect.x
    val fy = faceRect.y
    val fw = faceRect.width
    val fh = faceRect.height
    return Rect(
        (fx + fw * x - (fw * w / 2.0)).toInt(),
        (fy + fh * y - (fh * h / 2.0)).toInt(),
        (fw * w).toInt(),
        (fh * h).toInt()
    )
}

fun subFaceMeans(rgba: Mat): Float {
    //var rAverage = 0.0
    var gAverage = 0.0 // less interference
    //var bAverage = 0.0

    val rows = rgba.rows()
    val cols = rgba.cols()
    val pixels = rows * cols

    for (pRow in 0 until rows) {
        for (pCol in 0 until cols) {
            val pixel = rgba[pRow, pCol]

            //rAverage += pixel[0]
            gAverage += pixel[1]
            //bAverage += pixel[2]
        }
    }

    //rAverage /= pixels
    gAverage /= pixels
    //bAverage /= pixels

    return gAverage.toFloat()//(rAverage + gAverage + bAverage) / 3.0
}
