package com.github.oryon.heartbest.utils

fun now(): Long = System.currentTimeMillis()

fun Long.toSec(): Float = this / 1000F