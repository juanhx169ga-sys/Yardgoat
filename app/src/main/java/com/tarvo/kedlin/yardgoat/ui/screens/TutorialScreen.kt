package com.tarvo.kedlin.yardgoat.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarvo.kedlin.yardgoat.ui.common.Backdrop
import com.tarvo.kedlin.yardgoat.ui.common.PlatePanel
import com.tarvo.kedlin.yardgoat.ui.theme.GameFonts
import com.tarvo.kedlin.yardgoat.ui.theme.Palette

private enum class Sketch { Rig, Steer, Fold, Bay, Cones }

private data class Chapter(val title: String, val body: String, val sketch: Sketch)

private val story = listOf(
    Chapter(
        "The rig",
        "A yard tractor and a trailer joined at one pin. The tractor goes where you point it. The trailer goes wherever the pin drags it, which is rarely where you wanted.",
        Sketch.Rig
    ),
    Chapter(
        "Steering",
        "Drag the bar at the bottom to turn the wheel. It stays where you leave it, the way a real wheel does. Double tap the bar to straighten up.",
        Sketch.Steer
    ),
    Chapter(
        "Reversing",
        "Hold R to back up. In reverse the trailer swings the opposite way to the wheel: steer left and the trailer goes right. Let the fold angle grow too far and the trailer jackknifes across the tractor. Watch the FOLD readout.",
        Sketch.Fold
    ),
    Chapter(
        "The bay",
        "The job is done when the rear doors sit square on the bumpers. REACH is how far the doors still are from the dock, OFFSET is how far off the marking you sit. Both need to be small, and the trailer needs to be straight.",
        Sketch.Bay
    ),
    Chapter(
        "The yard",
        "Cones cost you the third star but nothing else. Barrels, masts, forklifts and parked rigs end the shunt. Pull forward as often as you like, but every change of direction counts as a shunt.",
        Sketch.Cones
    )
)

@Composable
fun TutorialScreen(backdrop: String, onDone: () -> Unit) {
    BackHandler { onDone() }
    Backdrop(backdrop, dim = 0.7f) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "INDUCTION",
                    color = Palette.Cream,
                    fontFamily = GameFonts.primary,
                    fontSize = 22.sp,
                    letterSpacing = 4.sp,
                    modifier = Modifier.weight(1f)
                )
                PlatePanel("Skip", onDone, Modifier.height(40.dp).size(width = 82.dp, height = 40.dp), "btn_plate_steel", fontSize = 13)
            }
            Text(
                "Read it once. The yard does not give second chances, but the retry button does.",
                color = Palette.Amber,
                fontFamily = GameFonts.hud,
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
            story.forEachIndexed { index, chapter ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Palette.Panel.copy(alpha = 0.92f))
                        .padding(16.dp)
                ) {
                    Text(
                        "STEP ${index + 1}",
                        color = Palette.Amber,
                        fontFamily = GameFonts.hud,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp
                    )
                    Text(
                        chapter.title,
                        color = Palette.Cream,
                        fontFamily = GameFonts.primary,
                        fontSize = 21.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(vertical = 10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Palette.Asphalt)
                    ) {
                        Canvas(Modifier.fillMaxSize().padding(12.dp)) { drawSketch(chapter.sketch) }
                    }
                    Text(
                        chapter.body,
                        color = Palette.Muted,
                        fontFamily = GameFonts.hud,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )
                }
            }
            PlatePanel(
                "Start the shift",
                onDone,
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp)
                    .height(58.dp),
                "btn_plate_amber",
                Palette.Ink,
                fontSize = 20
            )
        }
    }
}

private fun DrawScope.drawSketch(sketch: Sketch) {
    val midY = size.height / 2f
    val cream = Palette.Cream
    val amber = Palette.Amber
    val lime = Palette.Lime
    val danger = Palette.Danger
    when (sketch) {
        Sketch.Rig -> {
            drawRigShape(size.width * 0.28f, midY, 0f, cream, amber)
            drawCircle(lime, 7f, Offset(size.width * 0.28f + 26f, midY))
        }
        Sketch.Steer -> {
            drawRigShape(size.width * 0.3f, midY, 0f, cream, amber)
            val barY = size.height - 12f
            drawLine(Palette.Steel, Offset(size.width * 0.55f, barY), Offset(size.width * 0.95f, barY), strokeWidth = 6f)
            drawCircle(amber, 11f, Offset(size.width * 0.68f, barY))
            drawLine(cream, Offset(size.width * 0.55f, midY - 20f), Offset(size.width * 0.9f, midY - 40f), strokeWidth = 4f)
        }
        Sketch.Fold -> {
            drawRigShape(size.width * 0.3f, midY, 0.55f, cream, danger)
            drawArc(
                danger,
                200f,
                60f,
                false,
                topLeft = Offset(size.width * 0.3f - 40f, midY - 40f),
                size = androidx.compose.ui.geometry.Size(80f, 80f),
                style = Stroke(width = 4f)
            )
        }
        Sketch.Bay -> {
            drawLine(Palette.Muted, Offset(size.width * 0.86f, 10f), Offset(size.width * 0.86f, size.height - 10f), strokeWidth = 8f)
            drawRigShape(size.width * 0.3f, midY, 0f, cream, amber)
            drawLine(lime, Offset(size.width * 0.62f, midY), Offset(size.width * 0.84f, midY), strokeWidth = 3f)
        }
        Sketch.Cones -> {
            drawRigShape(size.width * 0.26f, midY, 0.12f, cream, amber)
            for (i in 0..3) {
                val x = size.width * (0.62f + i * 0.09f)
                drawCircle(if (i == 3) danger else amber, if (i == 3) 9f else 6f, Offset(x, midY + 14f))
            }
        }
    }
}

private fun DrawScope.drawRigShape(x: Float, y: Float, fold: Float, boxColor: Color, cabColor: Color) {
    rotate(Math.toDegrees(fold.toDouble()).toFloat(), Offset(x, y)) {
        drawRect(
            boxColor,
            topLeft = Offset(x, y - 13f),
            size = androidx.compose.ui.geometry.Size(96f, 26f)
        )
    }
    drawRect(
        cabColor,
        topLeft = Offset(x - 42f, y - 15f),
        size = androidx.compose.ui.geometry.Size(38f, 30f)
    )
    drawLine(Palette.Steel, Offset(x - 6f, y), Offset(x, y), strokeWidth = 5f)
}
