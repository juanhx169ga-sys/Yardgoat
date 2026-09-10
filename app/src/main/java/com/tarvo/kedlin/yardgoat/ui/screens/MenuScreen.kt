package com.tarvo.kedlin.yardgoat.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarvo.kedlin.yardgoat.domain.model.YardCatalog
import com.tarvo.kedlin.yardgoat.domain.repository.ProgressRepository
import com.tarvo.kedlin.yardgoat.ui.common.Art
import com.tarvo.kedlin.yardgoat.ui.common.Backdrop
import com.tarvo.kedlin.yardgoat.ui.common.IconPlate
import com.tarvo.kedlin.yardgoat.ui.common.PlatePanel
import com.tarvo.kedlin.yardgoat.ui.common.StarBadge
import com.tarvo.kedlin.yardgoat.ui.theme.GameFonts
import com.tarvo.kedlin.yardgoat.ui.theme.Palette

@Composable
fun MenuScreen(
    progress: ProgressRepository,
    backdrop: String,
    nextJob: Int,
    onDrive: () -> Unit,
    onJobs: () -> Unit,
    onAwards: () -> Unit,
    onRecords: () -> Unit,
    onSetup: () -> Unit,
    onBriefing: () -> Unit
) {
    val activity = LocalContext.current as? Activity
    BackHandler { activity?.finish() }
    val job = YardCatalog.job(nextJob)

    Backdrop(backdrop, dim = 0.5f) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.End
            ) {
                StarBadge(progress.totalStars(), YardCatalog.STAR_COUNT)
            }

            Column(
                Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Art("logo", Modifier.size(150.dp))
                Text(
                    "YARDGOAT",
                    color = Palette.Cream,
                    fontFamily = GameFonts.primary,
                    fontSize = 42.sp,
                    letterSpacing = 8.sp
                )
                Text(
                    "Back it onto the bumpers",
                    color = Palette.Amber,
                    fontFamily = GameFonts.hud,
                    fontSize = 16.sp,
                    letterSpacing = 2.sp
                )
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Palette.Asphalt.copy(alpha = 0.94f))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    "NEXT ON THE SHEET",
                    color = Palette.Muted,
                    fontFamily = GameFonts.hud,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    "${job.name}  ·  ${job.kind.tag}",
                    color = Palette.Cream,
                    fontFamily = GameFonts.primary,
                    fontSize = 19.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    YardCatalog.chapters[job.chapter],
                    color = Palette.Amber,
                    fontFamily = GameFonts.hud,
                    fontSize = 14.sp
                )
                PlatePanel(
                    "Take the job",
                    onDrive,
                    Modifier.fillMaxWidth().padding(top = 12.dp).height(64.dp),
                    "btn_plate_amber",
                    Palette.Ink,
                    fontSize = 22
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconPlate("ic_jobs", "Jobs", onJobs, Modifier.weight(1f).height(66.dp))
                    IconPlate("ic_awards", "Awards", onAwards, Modifier.weight(1f).height(66.dp))
                    IconPlate("ic_records", "Records", onRecords, Modifier.weight(1f).height(66.dp))
                    IconPlate("ic_setup", "Setup", onSetup, Modifier.weight(1f).height(66.dp))
                }
                Row(Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    PlatePanel(
                        "Induction",
                        onBriefing,
                        Modifier.weight(1f).height(46.dp),
                        "btn_plate_steel",
                        fontSize = 14
                    )
                    PlatePanel(
                        "Clock off",
                        { activity?.finish() },
                        Modifier.weight(1f).padding(start = 10.dp).height(46.dp),
                        "btn_plate_red",
                        fontSize = 14
                    )
                }
            }
        }
    }
}
