package com.tarvo.kedlin.yardgoat.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.tarvo.kedlin.yardgoat.domain.engine.Outcome
import com.tarvo.kedlin.yardgoat.domain.engine.Rig
import com.tarvo.kedlin.yardgoat.domain.model.Award
import com.tarvo.kedlin.yardgoat.domain.model.Job
import com.tarvo.kedlin.yardgoat.domain.model.RunReport
import com.tarvo.kedlin.yardgoat.domain.usecase.RecordRun
import com.tarvo.kedlin.yardgoat.domain.usecase.ReviewAwards

enum class Cue { Start, Shift, Couple, Cone, Docked, Wreck }

class DriveViewModel(
    val job: Job,
    private val recordRun: RecordRun,
    private val reviewAwards: ReviewAwards
) : ViewModel() {

    val rig = Rig(job)

    var tick by mutableIntStateOf(0)
        private set

    var paused by mutableStateOf(false)
        private set

    var report by mutableStateOf<RunReport?>(null)
        private set

    var earned by mutableStateOf<List<Award>>(emptyList())
        private set

    private var settled = false
    private var lastGear = 0
    private var lastCones = 0
    private var wasCoupled = rig.coupled
    private var cueSink: ((Cue) -> Unit)? = null

    fun onCue(sink: (Cue) -> Unit) {
        cueSink = sink
    }

    fun wheel(value: Float) {
        if (!paused) rig.wheel(value)
    }

    fun throttle(value: Int) {
        if (paused) {
            rig.throttle(0)
            return
        }
        rig.throttle(value)
        if (value != 0 && value != lastGear) {
            lastGear = value
            cueSink?.invoke(Cue.Shift)
        }
        if (value == 0) lastGear = 0
    }

    fun pause(value: Boolean) {
        paused = value
        if (value) rig.throttle(0)
    }

    fun retry() {
        rig.reset()
        settled = false
        paused = false
        report = null
        earned = emptyList()
        lastGear = 0
        lastCones = 0
        wasCoupled = rig.coupled
        tick++
    }

    fun step(dt: Float) {
        if (paused || settled) return
        rig.step(dt)
        tick++
        if (rig.conesHit > lastCones) {
            lastCones = rig.conesHit
            cueSink?.invoke(Cue.Cone)
        }
        if (!wasCoupled && rig.coupled) {
            wasCoupled = true
            cueSink?.invoke(Cue.Couple)
        }
        if (rig.outcome != Outcome.Driving) finish()
    }

    private fun finish() {
        if (settled) return
        settled = true
        val docked = rig.outcome == Outcome.Docked
        val made = RunReport(
            job = job.index,
            kind = job.kind,
            docked = docked,
            verdict = rig.verdict,
            stars = rig.stars,
            time = rig.elapsed,
            shunts = rig.shunts,
            conesHit = rig.conesHit,
            offset = rig.offset,
            angleError = rig.angleError,
            timeLeft = rig.timeLeft()
        )
        recordRun(made)
        earned = reviewAwards(made)
        report = made
        cueSink?.invoke(if (docked) Cue.Docked else Cue.Wreck)
    }
}
