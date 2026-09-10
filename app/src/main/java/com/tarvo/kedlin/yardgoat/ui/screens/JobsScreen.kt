package com.tarvo.kedlin.yardgoat.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarvo.kedlin.yardgoat.domain.model.YardCatalog
import com.tarvo.kedlin.yardgoat.domain.repository.ProgressRepository
import com.tarvo.kedlin.yardgoat.ui.common.Art
import com.tarvo.kedlin.yardgoat.ui.common.Backdrop
import com.tarvo.kedlin.yardgoat.ui.common.PlatePanel
import com.tarvo.kedlin.yardgoat.ui.common.StarRow
import com.tarvo.kedlin.yardgoat.ui.theme.GameFonts
import com.tarvo.kedlin.yardgoat.ui.theme.Palette
import kotlin.math.sin

private const val ROW_HEIGHT = 118

@Composable
fun JobsScreen(
    progress: ProgressRepository,
    backdrop: String,
    onPick: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val state = rememberLazyListState()
    val focus = remember {
        (0 until YardCatalog.JOB_COUNT).firstOrNull { progress.unlocked(it) && progress.stars(it) == 0 } ?: 0
    }
    LaunchedEffect(focus) {
        state.scrollToItem((focus / 10).coerceAtMost(YardCatalog.chapters.lastIndex))
    }

    Backdrop(backdrop, dim = 0.62f) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlatePanel("Back", onBack, Modifier.height(44.dp).size(width = 96.dp, height = 44.dp), "btn_plate_blue", fontSize = 14)
                Text(
                    "THE SHEET",
                    color = Palette.Cream,
                    fontFamily = GameFonts.primary,
                    fontSize = 17.sp,
                    letterSpacing = 3.sp,
                    modifier = Modifier.padding(start = 14.dp)
                )
            }
            LazyColumn(Modifier.fillMaxSize(), state = state) {
                items(YardCatalog.chapters.size) { chapter ->
                    ChapterLane(chapter, progress, onPick)
                }
            }
        }
    }
}

@Composable
private fun ChapterLane(chapter: Int, progress: ProgressRepository, onPick: (Int) -> Unit) {
    val first = chapter * 10
    val stars = progress.chapterStars(chapter)
    Column(Modifier.fillMaxWidth().padding(bottom = 18.dp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Palette.Panel.copy(alpha = 0.9f))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    YardCatalog.chapters[chapter].uppercase(),
                    color = Palette.Cream,
                    fontFamily = GameFonts.primary,
                    fontSize = 16.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier.weight(1f)
                )
                Art("ic_star", Modifier.size(15.dp))
                Text(
                    "  $stars / 30",
                    color = Palette.Gold,
                    fontFamily = GameFonts.hud,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Text(
                YardCatalog.chapterBrief[chapter],
                color = Palette.Muted,
                fontFamily = GameFonts.hud,
                fontSize = 13.sp
            )
        }

        val density = LocalDensity.current
        Box(Modifier.fillMaxWidth().height((ROW_HEIGHT * 10).dp)) {
            Canvas(Modifier.fillMaxSize()) {
                val path = Path()
                val step = size.height / 10f
                for (i in 0..40) {
                    val t = i / 40f
                    val y = t * size.height
                    val x = size.width / 2f + sin(t * 10f) * size.width * 0.29f
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, Palette.Steel.copy(alpha = 0.85f), style = Stroke(width = 46f))
                drawPath(
                    path,
                    Palette.Cream.copy(alpha = 0.5f),
                    style = Stroke(
                        width = 4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 22f))
                    )
                )
                val slot = step
            }
            for (slot in 0 until 10) {
                val index = first + slot
                val job = YardCatalog.job(index)
                val unlocked = progress.unlocked(index)
                val stars3 = progress.stars(index)
                val t = (slot + 0.5f) / 10f
                val dx = sin(t * 10f) * 0.29f
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(ROW_HEIGHT.dp)
                        .offset(y = (slot * ROW_HEIGHT).dp)
                ) {
                    Row(
                        Modifier
                            .align(Alignment.Center)
                            .offset(x = (dx * 300).dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Palette.Ink.copy(alpha = 0.72f))
                            .clickable(enabled = unlocked) { onPick(index) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Art("btn_node", Modifier.size(52.dp), alpha = if (unlocked) 1f else 0.4f)
                            if (unlocked) {
                                Text(
                                    "${index + 1}",
                                    color = Palette.Cream,
                                    fontFamily = GameFonts.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            } else {
                                Art("ic_lock", Modifier.size(22.dp))
                            }
                        }
                        Column(Modifier.padding(start = 10.dp)) {
                            Text(
                                job.name,
                                color = if (unlocked) Palette.Cream else Palette.Muted,
                                fontFamily = GameFonts.primary,
                                fontSize = 15.sp
                            )
                            Text(
                                if (unlocked) "${job.kind.tag}  ·  ${YardCatalog.grade(job)}" else "Locked",
                                color = Palette.Amber.copy(alpha = if (unlocked) 1f else 0.4f),
                                fontFamily = GameFonts.hud,
                                fontSize = 12.sp
                            )
                            StarRow(stars3, 12, Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }
        }
    }
}
