package com.tarvo.kedlin.yardgoat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarvo.kedlin.yardgoat.domain.model.Job
import com.tarvo.kedlin.yardgoat.domain.model.RunReport
import com.tarvo.kedlin.yardgoat.ui.common.Art
import com.tarvo.kedlin.yardgoat.ui.common.PlatePanel
import com.tarvo.kedlin.yardgoat.ui.common.StarRow
import com.tarvo.kedlin.yardgoat.ui.theme.GameFonts
import com.tarvo.kedlin.yardgoat.ui.theme.Palette

@Composable
fun DebriefCurtain(
    report: RunReport,
    job: Job,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    onQuit: () -> Unit
) {
    Box(
        Modifier.fillMaxSize().background(Palette.Ink.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (report.docked) "DOCKED" else "SHUNT FAILED",
                color = if (report.docked) Palette.Lime else Palette.Danger,
                fontFamily = GameFonts.primary,
                fontSize = 30.sp,
                letterSpacing = 4.sp
            )
            Text(
                "${job.name} — ${report.verdict}",
                color = Palette.Cream,
                fontFamily = GameFonts.hud,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            StarRow(report.stars, 30, Modifier.padding(top = 12.dp))

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Palette.Panel.copy(alpha = 0.9f))
                    .padding(14.dp)
            ) {
                Line("Job type", job.kind.tag)
                Line("Time", fmt(report.time, 1) + " s   (par " + fmt(job.parTime, 0) + ")")
                Line("Shunts", "${report.shunts}   (par ${job.parShunts})")
                Line("Cones knocked", report.conesHit.toString())
                Line("Offset from marking", fmt(report.offset, 2) + " m")
                Line("Angle to the bay", fmt(Math.toDegrees(report.angleError.toDouble()).toFloat(), 1) + "°")
                if (job.timed) Line("Window left", fmt(report.timeLeft, 1) + " s")
                Text(
                    "THIRD STAR",
                    color = Palette.Amber,
                    fontFamily = GameFonts.hud,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    job.kind.bonus,
                    color = Palette.Cream,
                    fontFamily = GameFonts.hud,
                    fontSize = 14.sp
                )
            }

            Row(Modifier.fillMaxWidth().padding(top = 16.dp)) {
                PlatePanel(
                    "Retry",
                    onRetry,
                    Modifier.weight(1f).height(54.dp),
                    "btn_plate_steel"
                )
                if (report.docked) {
                    PlatePanel(
                        "Next job",
                        onNext,
                        Modifier.weight(1f).padding(start = 10.dp).height(54.dp),
                        "btn_plate_amber",
                        Palette.Ink
                    )
                }
            }
            PlatePanel(
                "Back to the yard",
                onQuit,
                Modifier.fillMaxWidth().padding(top = 10.dp).height(50.dp),
                "btn_plate_blue"
            )
        }
    }
}

@Composable
private fun Line(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            color = Palette.Muted,
            fontFamily = GameFonts.hud,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            color = Palette.Cream,
            fontFamily = GameFonts.hud,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
fun AwardToast(icon: String, title: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Palette.Panel.copy(alpha = 0.95f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Art(icon, Modifier.size(34.dp))
        Column(Modifier.padding(start = 10.dp).width(210.dp)) {
            Text(
                "AWARD",
                color = Palette.Amber,
                fontFamily = GameFonts.primary,
                fontSize = 10.sp,
                letterSpacing = 2.sp
            )
            Text(
                title,
                color = Palette.Cream,
                fontFamily = GameFonts.hud,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}
