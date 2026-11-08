package com.fabri.nonna.feedback

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator

fun vibrarCorto(context: Context) {
    val v = context.getSystemService(Vibrator::class.java) ?: return
    if (v.hasVibrator()) v.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
}
