package com.tarvo.kedlin.yardgoat.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarvo.kedlin.yardgoat.audio.Haptics
import com.tarvo.kedlin.yardgoat.audio.Sfx
import com.tarvo.kedlin.yardgoat.audio.SoundBox
import com.tarvo.kedlin.yardgoat.domain.engine.Rig
import com.tarvo.kedlin.yardgoat.domain.engine.Solid
import com.tarvo.kedlin.yardgoat.presentation.Cue
import com.tarvo.kedlin.yardgoat.presentation.DriveViewModel
import com.tarvo.kedlin.yardgoat.ui.common.PlatePanel
import com.tarvo.kedlin.yardgoat.ui.scene.YardScene
import com.tarvo.kedlin.yardgoat.ui.theme.GameFonts
import com.tarvo.kedlin.yardgoat.ui.theme.Palette
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DriveScreen(
    model: DriveViewModel,
    sound: SoundBox,
    haptics: Haptics,
    onQuit: () -> Unit,
    onRetry: () -> Unit,
    onNext: () -> Unit
) {
    val rig = model.rig
    val job = model.job
    val report = model.report

    DisposableEffect(model) {
        model.onCue { cue ->
            when (cue) {
                Cue.Start -> sound.play(Sfx.Tap)
                Cue.Shift -> {
                    sound.play(Sfx.Shift, 0.7f)
                    haptics.tick()
                }
                Cue.Couple -> {
                    sound.play(Sfx.Couple)
                    haptics.clunk()
                }
                Cue.Cone -> {
                    sound.play(Sfx.Cone, 0.8f)
                    haptics.knock()
                }
                Cue.Docked -> {
                    sound.stopEngine()
                    sound.play(Sfx.Docked)
                    haptics.success()
                }
                Cue.Wreck -> {
                    sound.stopEngine()
                    sound.play(Sfx.Wreck)
                    haptics.error()
                }
            }
        }
        onDispose {
            sound.stopEngine()
            model.onCue { }
        }
    }

    BackHandler { model.pause(!model.paused) }

    Box(Modifier.fillMaxSize().background(Palette.Ink)) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    if (job.night) {
                        listOf(Color(0xFF060A10), Color(0xFF0E1621), Color(0xFF1A222B))
                    } else {
                        listOf(Color(0xFF8FA6BC), Color(0xFFB9C6D2), Color(0xFF6E7B87))
                    }
                )
            )
        )

        YardScene(rig, Modifier.fillMaxSize()) { dt ->
            model.step(dt)
            sound.engine(model.report == null && !model.paused, abs(rig.speed) / Rig.MAX_FORWARD)
        }

        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        job.name.uppercase(),
                        color = Palette.Cream,
                        fontFamily = GameFonts.primary,
                        fontSize = 15.sp,
                        letterSpacing = 2.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 3.dp)) {
                        Text(
                            job.kind.tag,
                            color = Palette.Ink,
                            fontFamily = GameFonts.primary,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(Palette.Amber)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        if (job.timed) {
                            Text(
                                "  " + fmt(rig.timeLeft(), 1) + " s left",
                                color = if (rig.timeLeft() < 10f) Palette.Danger else Palette.Lime,
                                fontFamily = GameFonts.hud,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                PlatePanel(
                    if (model.paused) "Resume" else "Pause",
                    { model.pause(!model.paused) },
                    Modifier.height(40.dp).width(96.dp),
                    "btn_plate_blue",
                    fontSize = 13
                )
            }

            Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(Modifier.weight(1f)) {
                    Gauge("SPEED", fmt(abs(rig.speed) * 3.6f, 0) + " km/h", Palette.Cream, model.tick)
                    Gauge(
                        "FOLD",
                        fmt(Math.toDegrees(abs(rig.foldAngle()).toDouble()).toFloat(), 0) + "°",
                        if (abs(rig.foldAngle()) > 1.0f) Palette.Danger else Palette.Lime,
                        model.tick
                    )
                    Gauge("REACH", fmt(rig.reach, 1) + " m", Palette.Amber, model.tick)
                    Gauge("OFFSET", fmt(rig.offset, 1) + " m", Palette.Cream, model.tick)
                    Gauge("SHUNTS", "${rig.shunts} / ${job.parShunts}", Palette.Cream, model.tick)
                    Gauge(
                        "CONES",
                        rig.conesHit.toString(),
                        if (rig.conesHit > 0) Palette.Danger else Palette.Lime,
                        model.tick
                    )
                }
                Minimap(rig, model.tick)
            }

            Spacer(Modifier.weight(1f))

            if (report == null && !model.paused) Controls(model, rig, haptics)
        }

        if (model.paused && report == null) {
            PauseCurtain(
                { model.pause(false) },
                {
                    model.retry()
                    onRetry()
                },
                onQuit
            )
        }

        report?.let {
            DebriefCurtain(
                report = it,
                job = job,
                onRetry = {
                    model.retry()
                    onRetry()
                },
                onNext = onNext,
                onQuit = onQuit
            )
        }
    }
}

@Composable
private fun Gauge(label: String, value: String, tint: Color, tick: Int) {
    val slot = tick
    Row(Modifier.padding(vertical = 1.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            color = Palette.Muted,
            fontFamily = GameFonts.hud,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.width(62.dp)
        )
        Text(
            value,
            color = tint,
            fontFamily = GameFonts.hud,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )
    }
}

@Composable
private fun Minimap(rig: Rig, tick: Int) {
    Box(
        Modifier
            .size(132.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.Ink.copy(alpha = 0.66f))
            .padding(6.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val slot = tick
            val span = 74f
            val scale = size.minDimension / (span * 2f)
            val centre = Offset(size.width / 2f, size.height / 2f)
            fun map(wx: Float, wz: Float) = Offset(centre.x + wx * scale, centre.y + wz * scale)

            drawCircle(Palette.Steel.copy(alpha = 0.5f), size.minDimension / 2f, centre, style = Stroke(1.5f))

            val job = rig.job
            val fx = cos(job.bayFacing)
            val fz = sin(job.bayFacing)
            val bay = map(job.bayX, job.bayZ)
            val wallA = map(job.bayX - fz * 9f, job.bayZ + fx * 9f)
            val wallB = map(job.bayX + fz * 9f, job.bayZ - fx * 9f)
            drawLine(Palette.Muted, wallA, wallB, strokeWidth = 3f)
            drawCircle(Palette.Amber, 5f, bay)

            for (ob in rig.obstacles) {
                val point = when (ob.kind) {
                    Solid.Forklift -> map(rig.forkliftX, rig.forkliftZ)
                    else -> map(ob.x, ob.z)
                }
                val tint = when (ob.kind) {
                    Solid.Cone -> if (ob.down) Palette.Steel else Palette.Amber
                    Solid.Barrel -> Palette.Danger
                    Solid.Mast -> Palette.Muted
                    Solid.Parked -> Palette.Steel
                    Solid.Forklift -> Palette.Danger
                }
                val radius = when (ob.kind) {
                    Solid.Cone -> 2f
                    Solid.Parked -> 4f
                    else -> 3f
                }
                if (ob.kind == Solid.Parked) {
                    val tail = map(
                        ob.x - cos(ob.heading) * Rig.TRAILER,
                        ob.z - sin(ob.heading) * Rig.TRAILER
                    )
                    drawLine(Palette.Steel, point, tail, strokeWidth = 5f)
                } else {
                    drawCircle(tint, radius, point)
                }
            }

            val nose = map(rig.trailerNoseX(), rig.trailerNoseZ())
            val rear = map(rig.trailerRearX(), rig.trailerRearZ())
            drawLine(Palette.Cream, nose, rear, strokeWidth = 6f)
            val cab = map(rig.x + cos(rig.heading) * 3f, rig.z + sin(rig.heading) * 3f)
            val tail = map(rig.x - cos(rig.heading) * 1.5f, rig.z - sin(rig.heading) * 1.5f)
            drawLine(Palette.Amber, cab, tail, strokeWidth = 7f)
        }
    }
}

@Composable
private fun Controls(model: DriveViewModel, rig: Rig, haptics: Haptics) {
    var wheel by remember { mutableFloatStateOf(0f) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            Text(
                "STEER",
                color = Palette.Muted,
                fontFamily = GameFonts.hud,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(74.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Palette.Ink.copy(alpha = 0.55f))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = { },
                            onDrag = { change, drag ->
                                change.consume()
                                wheel = (wheel + drag.x / (size.width * 0.42f)).coerceIn(-1f, 1f)
                                model.wheel(wheel)
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = {
                            wheel = 0f
                            model.wheel(0f)
                            haptics.tick()
                        })
                    }
            ) {
                Canvas(Modifier.fillMaxSize().padding(10.dp)) {
                    val mid = size.height / 2f
                    drawLine(
                        Palette.Steel,
                        Offset(0f, mid),
                        Offset(size.width, mid),
                        strokeWidth = 3f
                    )
                    val x = size.width / 2f + wheel * size.width * 0.42f
                    drawCircle(Palette.Amber, 15f, Offset(x, mid))
                    rotate(wheel * 42f, Offset(x, mid)) {
                        drawLine(
                            Palette.Cream,
                            Offset(x, mid - 20f),
                            Offset(x, mid + 20f),
                            strokeWidth = 4f
                        )
                    }
                }
            }
            Text(
                "drag to steer, double tap to centre",
                color = Palette.Muted,
                fontFamily = GameFonts.hud,
                fontSize = 11.sp
            )
        }
        Column(
            Modifier.padding(start = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Pedal("D", Palette.Lime) { model.throttle(if (it) 1 else 0) }
            Pedal("R", Palette.Amber) { model.throttle(if (it) -1 else 0) }
        }
    }
}

@Composable
private fun Pedal(label: String, tint: Color, onHold: (Boolean) -> Unit) {
    Box(
        Modifier
            .size(82.dp)
            .clip(CircleShape)
            .background(Palette.Panel.copy(alpha = 0.85f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onHold(true)
                        tryAwaitRelease()
                        onHold(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = tint,
            fontFamily = GameFonts.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp
        )
    }
}

@Composable
private fun PauseCurtain(onResume: () -> Unit, onRestart: () -> Unit, onQuit: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(Palette.Ink.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "HANDBRAKE ON",
                color = Palette.Cream,
                fontFamily = GameFonts.primary,
                fontSize = 24.sp,
                letterSpacing = 3.sp
            )
            PlatePanel(
                "Resume",
                onResume,
                Modifier.padding(top = 20.dp).width(230.dp).height(56.dp),
                "btn_plate_amber",
                Palette.Ink
            )
            PlatePanel(
                "Start over",
                onRestart,
                Modifier.padding(top = 10.dp).width(230.dp).height(52.dp),
                "btn_plate_steel"
            )
            PlatePanel(
                "Back to the yard",
                onQuit,
                Modifier.padding(top = 10.dp).width(230.dp).height(52.dp),
                "btn_plate_red"
            )
        }
    }
}

fun fmt(value: Float, digits: Int): String = String.format("%.${digits}f", value)
