package com.github.oryon.heartbest.utils

import kotlin.math.cos
import kotlin.math.sqrt

fun linspace(start: Float, stop: Float, num: Int): FloatArray {
    val out = FloatArray(num)
    out[0] = start
    out[num - 1] = stop

    val y = (stop - start) / (num - 1)   // step
    for (i in 1..(num - 2)) {
        out[i] = out[i - 1] + y
    }
    return out
}

fun hamming(m: Int): FloatArray {
    if (m < 1)
        return FloatArray(0)
    if (m == 1)
        return FloatArray(1) { 1F }

    val out = ArrayList<Float>()
    for (n in 1 - m..m step 2) {
        val x = 0.54 + 0.46 * cos((Math.PI * n) / (m - 1))
        out.add(x.toFloat())
    }

    return out.toFloatArray()
}

fun lerp(start: Float, stop: Float, amount: Float): Float {
    // x2 y2 = 0
    return amount + ((start - stop) / (0F - stop)) * (0F - amount)
}

fun argmax(array: FloatArray): Int {
    val max = array.maxOrNull()
    return array.indexOfFirst { it == max }
}

fun interp(start: FloatArray, end: FloatArray, count: FloatArray): FloatArray {
    val out = FloatArray(start.size)
    for (i in start.indices) {
        out[i] = lerp(start[i], end[i], count[i])
    }

    return out
}

private fun vectorNorm(vector: FloatArray): Double {
    var sum = 0.0
    vector.forEach {
        sum += it * it
    }

    return sqrt(sum)
}