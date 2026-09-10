package com.tarvo.kedlin.yardgoat.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.tarvo.kedlin.yardgoat.domain.repository.ProgressRepository

class Haptics(context: Context, private val progress: ProgressRepository) {

    private val vibrator: Vibrator =
        if (Build.VERSION.SDK_INT >= 31) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

    fun buzz(ms: Long) {
        if (!progress.vibration() || !vibrator.hasVibrator()) return
        vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun tick() = buzz(14)
    fun knock() = buzz(28)
    fun clunk() = buzz(55)
    fun success() = buzz(80)
    fun error() = buzz(140)
}
