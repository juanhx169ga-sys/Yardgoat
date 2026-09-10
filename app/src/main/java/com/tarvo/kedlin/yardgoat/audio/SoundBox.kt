package com.tarvo.kedlin.yardgoat.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.tarvo.kedlin.yardgoat.R
import com.tarvo.kedlin.yardgoat.domain.repository.ProgressRepository

enum class Sfx { Tap, Shift, Couple, Cone, Docked, Wreck, Chime }

class SoundBox(context: Context, private val progress: ProgressRepository) {

    private val pool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val ids = mapOf(
        Sfx.Tap to pool.load(context, R.raw.tap, 1),
        Sfx.Shift to pool.load(context, R.raw.shift, 1),
        Sfx.Couple to pool.load(context, R.raw.couple, 1),
        Sfx.Cone to pool.load(context, R.raw.cone, 1),
        Sfx.Docked to pool.load(context, R.raw.docked, 1),
        Sfx.Wreck to pool.load(context, R.raw.wreck, 1),
        Sfx.Chime to pool.load(context, R.raw.chime, 1)
    )

    private val engineId = pool.load(context, R.raw.engine, 1)
    private var engineStream = 0

    fun play(sfx: Sfx, volume: Float = 1f) {
        if (!progress.sound()) return
        ids[sfx]?.let { pool.play(it, volume, volume, 1, 0, 1f) }
    }

    fun engine(running: Boolean, load: Float = 0f) {
        if (!progress.sound() || !running) {
            stopEngine()
            return
        }
        val rate = (0.72f + load * 0.5f).coerceIn(0.5f, 2f)
        if (engineStream == 0) {
            engineStream = pool.play(engineId, 0.5f, 0.5f, 0, -1, rate)
        } else {
            pool.setRate(engineStream, rate)
        }
    }

    fun stopEngine() {
        if (engineStream != 0) {
            pool.stop(engineStream)
            engineStream = 0
        }
    }

    fun release() {
        stopEngine()
        pool.release()
    }
}
