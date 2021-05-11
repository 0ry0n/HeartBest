package com.github.oryon.heartbest.data

import kotlin.math.ln
import kotlin.math.pow

// From https://www.math.ksu.edu/~bennett/jomacg/c.html

class Complex(var real: Double, var imag: Double) {
    fun mod(): Double {
        return if (real != 0.0 || imag != 0.0) {
            kotlin.math.sqrt(real * real + imag * imag)
        } else {
            0.0
        }
    }

    fun arg(): Double {
        return kotlin.math.atan2(imag, real)
    }

    fun angle(deg: Boolean = false): Double {
        return if (deg)
            tanh() * (180.0 / Math.PI)
        else
            tanh()
    }

    fun conj(): Complex {
        return Complex(real, -imag)
    }

    operator fun plus(z: Complex): Complex {
        return Complex(real + z.real, imag + z.imag)
    }

    operator fun minus(z: Complex): Complex {
        return Complex(real - z.real, imag - z.imag)
    }

    operator fun times(z: Complex): Complex {
        return Complex(real * z.real - imag * z.imag, real * z.imag + imag * z.real)
    }

    operator fun times(x: Double): Complex {
        return Complex(real * x, imag * x)
    }

    operator fun div(z: Complex): Complex {
        val den = z.mod().pow(2.0)
        return Complex((real * z.real + imag * z.imag) / den, (imag * z.real - real * z.imag / den))
    }

    operator fun div(x: Double): Complex {
        return Complex(real / x, imag / x)
    }

    fun exp(): Complex {
        return Complex(
            kotlin.math.exp(real) * kotlin.math.cos(imag), kotlin.math.exp(real) * kotlin.math.sin(
                imag
            )
        )
    }

    fun log(): Complex {
        return Complex(ln(mod()), arg())
    }

    fun sqrt(): Complex {
        val r = kotlin.math.sqrt(mod())
        val theta = arg() / 2.0
        return Complex(r * kotlin.math.cos(theta), r * kotlin.math.sin(theta))
    }

    // Real cosh function (used to compute complex trig functions)
    private fun cosh(theta: Double): Double {
        return (kotlin.math.exp(theta) + kotlin.math.exp(-theta)) / 2.0
    }

    // Real sinh function (used to compute complex trig functions)
    private fun sinh(theta: Double): Double {
        return (kotlin.math.exp(theta) - kotlin.math.exp(-theta)) / 2.0
    }

    fun sin(): Complex {
        return Complex(cosh(imag) * kotlin.math.sin(real), sinh(imag) * kotlin.math.cos(real))
    }

    fun cos(): Complex {
        return Complex(cosh(imag) * kotlin.math.cos(real), -sinh(imag) * kotlin.math.sin(real))
    }

    fun tan(): Complex {
        return sin().div(cos())
    }

    fun sinh(): Complex {
        return Complex(sinh(real) * kotlin.math.cos(imag), cosh(real) * kotlin.math.sin(imag))
    }

    fun cosh(): Complex {
        return Complex(cosh(real) * kotlin.math.cos(imag), sinh(real) * kotlin.math.sin(imag))
    }

    private fun tanh(): Double {
        return kotlin.math.tanh(imag / real)
    }

    fun chs(): Complex {
        return Complex(-real, -imag)
    }

    override fun toString(): String {
        if (real != 0.0 && imag > 0) {
            return "$real + ${imag}i"
        }

        if (real != 0.0 && imag < 0) {
            return "$real - ${-imag}i"
        }

        if (imag == 0.0) {
            return "$real"
        }

        return if (real == 0.0)
            "${imag}i"
        else
            "$real + i*$imag"
        // shouldn't get here (unless Inf or NaN)
    }
}