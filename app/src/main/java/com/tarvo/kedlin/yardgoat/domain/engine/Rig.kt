package com.tarvo.kedlin.yardgoat.domain.engine

import com.tarvo.kedlin.yardgoat.domain.model.Job
import com.tarvo.kedlin.yardgoat.domain.model.JobKind
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.tan

enum class Outcome { Driving, Docked, Wrecked }

enum class Solid { Cone, Barrel, Mast, Parked, Forklift }

class Obstacle(
    val x: Float,
    val z: Float,
    val heading: Float,
    val radius: Float,
    val kind: Solid
) {
    var down = false
}

class Rig(val job: Job) {

    var x = 0f
    var z = 0f
    var heading = 0f
    var trailerHeading = 0f
    var speed = 0f
    var steer = 0f
    var gear = 0
    var elapsed = 0f
    var coupled = true
    var outcome = Outcome.Driving
    var verdict = ""
    var stars = 0
    var conesHit = 0
    var shunts = 0
    var offset = 0f
    var angleError = 0f
    var reach = 0f
    var forkliftX = 0f
    var forkliftZ = 0f
    var forkliftHeading = 0f

    val obstacles = ArrayList<Obstacle>()

    var parkX = 0f
    var parkZ = 0f
    var parkHeading = 0f

    private var throttle = 0
    private var wheel = 0f
    private var forkliftT = 0f
    private var forkliftDir = 1f
    private var lastGear = 0

    init { reset() }

    fun reset() {
        placeStart()
        speed = 0f
        steer = 0f
        gear = 0
        elapsed = 0f
        outcome = Outcome.Driving
        verdict = ""
        stars = 0
        conesHit = 0
        shunts = 0
        offset = 0f
        angleError = 0f
        reach = 0f
        throttle = 0
        wheel = 0f
        lastGear = 0
        forkliftT = 0f
        forkliftDir = 1f
        coupled = !job.bobtail
        buildYard()
        stepForklift(0f)
    }

    private fun placeStart() {
        val fx = cos(job.bayFacing)
        val fz = sin(job.bayFacing)
        val px = -fz
        val pz = fx
        parkHeading = job.bayFacing
        parkX = job.bayX + fx * 30f + px * 15f
        parkZ = job.bayZ + fz * 30f + pz * 15f
        when (job.kind) {
            JobKind.Straight, JobKind.Window -> {
                x = job.bayX + fx * 42f
                z = job.bayZ + fz * 42f
                heading = job.bayFacing
            }
            JobKind.Alley -> {
                x = job.bayX + fx * 20f + px * 20f
                z = job.bayZ + fz * 20f + pz * 20f
                heading = wrap(job.bayFacing - 1.5708f)
            }
            JobKind.Blindside -> {
                x = job.bayX + fx * 20f - px * 20f
                z = job.bayZ + fz * 20f - pz * 20f
                heading = wrap(job.bayFacing + 1.5708f)
            }
            JobKind.Parallel -> {
                x = job.bayX + fx * 8f + px * 26f
                z = job.bayZ + fz * 8f + pz * 26f
                heading = wrap(job.bayFacing - 1.5708f)
            }
            JobKind.Coupling -> {
                x = job.bayX + fx * 46f - px * 12f
                z = job.bayZ + fz * 46f - pz * 12f
                heading = wrap(job.bayFacing + 0.5f)
            }
        }
        trailerHeading = if (job.bobtail) parkHeading else heading
    }

    fun wheel(value: Float) {
        wheel = value.coerceIn(-1f, 1f)
    }

    fun throttle(value: Int) {
        throttle = value.coerceIn(-1, 1)
    }

    fun hitchX(): Float = x + HITCH * cos(heading)

    fun hitchZ(): Float = z + HITCH * sin(heading)

    fun trailerNoseX(): Float = if (coupled) hitchX() else parkX

    fun trailerNoseZ(): Float = if (coupled) hitchZ() else parkZ

    fun trailerRearX(): Float = trailerNoseX() - TRAILER * cos(trailerHeading)

    fun trailerRearZ(): Float = trailerNoseZ() - TRAILER * sin(trailerHeading)

    fun trailerMidX(): Float = trailerNoseX() - TRAILER * 0.5f * cos(trailerHeading)

    fun trailerMidZ(): Float = trailerNoseZ() - TRAILER * 0.5f * sin(trailerHeading)

    fun foldAngle(): Float = wrap(heading - trailerHeading)

    fun timeLeft(): Float =
        if (!job.timed) 0f else (job.timeLimit - elapsed).coerceAtLeast(0f)

    fun step(dt: Float) {
        if (outcome != Outcome.Driving) return
        elapsed += dt

        val target = wheel * MAX_STEER
        steer += (target - steer).coerceIn(-STEER_RATE * dt, STEER_RATE * dt)

        gear = throttle
        if (gear != 0 && gear != lastGear && lastGear != 0) shunts++
        if (gear != 0) lastGear = gear

        val wanted = when (gear) {
            1 -> MAX_FORWARD
            -1 -> -MAX_REVERSE
            else -> 0f
        }
        val rate = if (gear == 0) BRAKE else ACCEL
        speed += (wanted - speed).coerceIn(-rate * dt, rate * dt)
        if (gear == 0 && abs(speed) < 0.05f) speed = 0f

        val turn = speed / WHEELBASE * tan(steer)
        x += speed * cos(heading) * dt
        z += speed * sin(heading) * dt
        heading = wrap(heading + turn * dt)

        if (coupled) {
            val delta = wrap(heading - trailerHeading)
            val swing = (speed * sin(delta) - HITCH * turn * cos(delta)) / TRAILER
            trailerHeading = wrap(trailerHeading + swing * dt)
            if (abs(wrap(heading - trailerHeading)) > JACKKNIFE) {
                fail("Jackknifed the trailer")
                return
            }
        } else {
            tryCouple()
        }

        stepForklift(dt)
        checkObstacles()
        if (outcome != Outcome.Driving) return
        if (abs(x) > BOUNDS || abs(z) > BOUNDS) {
            fail("Left the yard")
            return
        }
        if (job.timed && elapsed > job.timeLimit) {
            fail("The window closed")
            return
        }
        measureBay()
    }

    private fun tryCouple() {
        val gap = hypot(hitchX() - parkX, hitchZ() - parkZ)
        val aligned = abs(wrap(heading - parkHeading)) < 0.28f
        if (gap < 1.4f && aligned && abs(speed) < 1.4f) {
            coupled = true
            trailerHeading = parkHeading
            heading = parkHeading
            x = parkX - HITCH * cos(parkHeading)
            z = parkZ - HITCH * sin(parkHeading)
            speed = 0f
            verdict = "Coupled"
        }
    }

    private fun measureBay() {
        val rearX = trailerRearX()
        val rearZ = trailerRearZ()
        val dx = rearX - job.bayX
        val dz = rearZ - job.bayZ
        val fx = cos(job.bayFacing)
        val fz = sin(job.bayFacing)
        reach = dx * fx + dz * fz
        offset = abs(-dx * fz + dz * fx)
        angleError = abs(wrap(trailerHeading - job.bayFacing))
        if (!coupled) return
        if (reach < -0.6f) {
            fail("Pushed the trailer into the dock")
            return
        }
        if (reach <= DOCK_REACH && offset <= DOCK_OFFSET && angleError <= DOCK_ANGLE && abs(speed) < 0.6f) {
            settle()
        }
    }

    private fun settle() {
        outcome = Outcome.Docked
        verdict = "On the bumpers"
        speed = 0f
        stars = 1
        if (elapsed <= job.parTime) stars++
        val clean = conesHit == 0
        val bonus = when (job.kind) {
            JobKind.Straight -> clean && angleError <= 0.09f
            JobKind.Parallel -> clean && offset <= 0.5f
            JobKind.Window -> clean && timeLeft() >= job.timeLimit * 0.2f
            else -> clean && shunts <= job.parShunts
        }
        if (bonus) stars++
    }

    private fun checkObstacles() {
        for (ob in obstacles) {
            if (ob.down) continue
            val hit = when (ob.kind) {
                Solid.Parked -> hitsParked(ob)
                Solid.Forklift -> hitsPoint(forkliftX, forkliftZ, 2.4f)
                else -> hitsPoint(ob.x, ob.z, ob.radius)
            }
            if (!hit) continue
            when (ob.kind) {
                Solid.Cone -> {
                    ob.down = true
                    conesHit++
                }
                Solid.Barrel -> {
                    fail("Hit a barrier drum")
                    return
                }
                Solid.Mast -> {
                    fail("Hit a light mast")
                    return
                }
                Solid.Parked -> {
                    fail("Hit a parked rig")
                    return
                }
                Solid.Forklift -> {
                    fail("Hit the forklift")
                    return
                }
            }
        }
    }

    private fun hitsParked(ob: Obstacle): Boolean {
        val fx = cos(ob.heading)
        val fz = sin(ob.heading)
        for (i in 0..3) {
            val t = -TRAILER * i / 3f
            if (hitsPoint(ob.x + fx * t, ob.z + fz * t, 2.0f)) return true
        }
        return false
    }

    private fun hitsPoint(px: Float, pz: Float, radius: Float): Boolean {
        if (hypot(px - x, pz - z) < radius + 1.7f) return true
        val noseX = x + 3.0f * cos(heading)
        val noseZ = z + 3.0f * sin(heading)
        if (hypot(px - noseX, pz - noseZ) < radius + 1.6f) return true
        if (!coupled) return false
        val fx = cos(trailerHeading)
        val fz = sin(trailerHeading)
        for (i in 1..3) {
            val t = -TRAILER * i / 3f
            val bx = trailerNoseX() + fx * t
            val bz = trailerNoseZ() + fz * t
            if (hypot(px - bx, pz - bz) < radius + 1.7f) return true
        }
        return false
    }

    private fun stepForklift(dt: Float) {
        if (!job.forklift) return
        forkliftT += forkliftDir * dt * 0.16f
        if (forkliftT > 1f) {
            forkliftT = 1f
            forkliftDir = -1f
        }
        if (forkliftT < 0f) {
            forkliftT = 0f
            forkliftDir = 1f
        }
        val ax = job.bayX - 16f
        val az = job.bayZ - 14f
        val bx = job.bayX - 16f
        val bz = job.bayZ + 14f
        forkliftX = ax + (bx - ax) * forkliftT
        forkliftZ = az + (bz - az) * forkliftT
        forkliftHeading = if (forkliftDir > 0f) 1.5708f else -1.5708f
    }

    private fun fail(reason: String) {
        outcome = Outcome.Wrecked
        verdict = reason
        speed = 0f
    }

    private fun buildYard() {
        obstacles.clear()
        val fx = cos(job.bayFacing)
        val fz = sin(job.bayFacing)
        var slot = 0
        var placed = 0
        while (placed < job.cones && slot < 400) {
            val px = (scatter(job.index * 17 + slot, 3) - 0.5f) * 84f
            val pz = (scatter(job.index * 17 + slot, 11) - 0.5f) * 84f
            slot++
            if (!clear(px, pz, 4.5f, fx, fz)) continue
            obstacles.add(Obstacle(px, pz, 0f, 0.8f, Solid.Cone))
            placed++
        }
        placed = 0
        while (placed < job.barrels && slot < 700) {
            val px = (scatter(job.index * 17 + slot, 23) - 0.5f) * 80f
            val pz = (scatter(job.index * 17 + slot, 29) - 0.5f) * 80f
            slot++
            if (!clear(px, pz, 7f, fx, fz)) continue
            obstacles.add(Obstacle(px, pz, 0f, 1.2f, Solid.Barrel))
            placed++
        }
        placed = 0
        while (placed < job.masts && slot < 900) {
            val px = (scatter(job.index * 17 + slot, 37) - 0.5f) * 88f
            val pz = (scatter(job.index * 17 + slot, 41) - 0.5f) * 88f
            slot++
            if (!clear(px, pz, 9f, fx, fz)) continue
            obstacles.add(Obstacle(px, pz, 0f, 1.4f, Solid.Mast))
            placed++
        }
        for (i in 0 until job.parked) {
            val side = if (i % 2 == 0) 1f else -1f
            val lane = job.lane + 3.2f * (i / 2)
            val px = job.bayX + (-fz) * side * lane
            val pz = job.bayZ + fx * side * lane
            obstacles.add(Obstacle(px, pz, job.bayFacing, 2.0f, Solid.Parked))
        }
        if (job.forklift) obstacles.add(Obstacle(0f, 0f, 0f, 2.4f, Solid.Forklift))
    }

    private fun clear(px: Float, pz: Float, margin: Float, fx: Float, fz: Float): Boolean {
        if (hypot(px - x, pz - z) < 20f) return false
        if (job.bobtail && hypot(px - parkX, pz - parkZ) < 16f) return false
        val dx = px - job.bayX
        val dz = pz - job.bayZ
        val along = dx * fx + dz * fz
        val side = abs(-dx * fz + dz * fx)
        if (along > -2f && along < 26f && side < job.lane * 0.5f + margin * 0.2f) return false
        if (hypot(dx, dz) < 9f) return false
        return true
    }

    companion object {
        const val WHEELBASE = 4.4f
        const val HITCH = 0.9f
        const val TRAILER = 11.5f
        const val MAX_STEER = 0.66f
        const val STEER_RATE = 1.9f
        const val ACCEL = 2.4f
        const val BRAKE = 4.2f
        const val MAX_FORWARD = 6.4f
        const val MAX_REVERSE = 3.4f
        const val JACKKNIFE = 1.32f
        const val DOCK_REACH = 1.7f
        const val DOCK_OFFSET = 1.3f
        const val DOCK_ANGLE = 0.2f
        const val BOUNDS = 62f

        fun wrap(a: Float): Float {
            var v = a
            while (v > Math.PI) v -= (2.0 * Math.PI).toFloat()
            while (v < -Math.PI) v += (2.0 * Math.PI).toFloat()
            return v
        }

        fun scatter(index: Int, salt: Int): Float {
            var h = index * 374761393 + salt * 668265263
            h = (h xor (h shr 13)) * 1274126177
            h = h xor (h shr 16)
            return ((h and 0xFFFF) / 65535f)
        }
    }
}
