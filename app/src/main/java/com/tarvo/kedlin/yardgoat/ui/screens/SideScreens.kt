package com.tarvo.kedlin.yardgoat.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarvo.kedlin.yardgoat.domain.model.AwardBook
import com.tarvo.kedlin.yardgoat.domain.model.YardCatalog
import com.tarvo.kedlin.yardgoat.domain.repository.ProgressRepository
import com.tarvo.kedlin.yardgoat.ui.common.Art
import com.tarvo.kedlin.yardgoat.ui.common.Backdrop
import com.tarvo.kedlin.yardgoat.ui.common.PlatePanel
import com.tarvo.kedlin.yardgoat.ui.theme.GameFonts
import com.tarvo.kedlin.yardgoat.ui.theme.Palette

val backdrops = listOf("yard_dawn", "yard_day", "yard_dusk", "yard_night")
val backdropNames = listOf("Dawn", "Day", "Dusk", "Night")

@Composable
private fun Header(title: String, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlatePanel("Back", onBack, Modifier.size(width = 96.dp, height = 44.dp), "btn_plate_blue", fontSize = 14)
        Text(
            title,
            color = Palette.Cream,
            fontFamily = GameFonts.primary,
            fontSize = 17.sp,
            letterSpacing = 3.sp,
            modifier = Modifier.padding(start = 14.dp)
        )
    }
}

@Composable
fun AwardsScreen(progress: ProgressRepository, backdrop: String, onBack: () -> Unit) {
    BackHandler { onBack() }
    Backdrop(backdrop, dim = 0.66f) {
        Column(Modifier.fillMaxSize()) {
            Header("AWARDS", onBack)
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items(AwardBook.all.size) { index ->
                    val award = AwardBook.all[index]
                    val won = progress.awardUnlocked(award.id)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Palette.Panel.copy(alpha = if (won) 0.92f else 0.6f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Art(award.icon, Modifier.size(48.dp), alpha = if (won) 1f else 0.25f)
                        Column(Modifier.padding(start = 12.dp)) {
                            Text(
                                award.title,
                                color = if (won) Palette.Cream else Palette.Muted,
                                fontFamily = GameFonts.primary,
                                fontSize = 16.sp
                            )
                            Text(
                                award.detail,
                                color = Palette.Muted,
                                fontFamily = GameFonts.hud,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecordsScreen(progress: ProgressRepository, backdrop: String, onBack: () -> Unit) {
    BackHandler { onBack() }
    Backdrop(backdrop, dim = 0.66f) {
        Column(Modifier.fillMaxSize()) {
            Header("RECORDS", onBack)
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card {
                    Stat("Jobs docked", "${progress.jobsCleared()} / ${YardCatalog.JOB_COUNT}")
                    Stat("Stars earned", "${progress.totalStars()} / ${YardCatalog.STAR_COUNT}")
                    Stat("Shunts docked", progress.statDocked().toString())
                    Stat("Failed shunts", progress.statFailed().toString())
                    Stat("Direction changes", progress.statShunts().toString())
                    Stat("Cones knocked", progress.statCones().toString())
                    Stat("Time behind the wheel", fmt(progress.statDriveTime(), 0) + " s")
                    Stat(
                        "Closest to the marking",
                        if (progress.bestOffset() > 0f) fmt(progress.bestOffset(), 2) + " m" else "no record"
                    )
                }
                for (chapter in YardCatalog.chapters.indices) {
                    Card {
                        Text(
                            YardCatalog.chapters[chapter].uppercase(),
                            color = Palette.Amber,
                            fontFamily = GameFonts.primary,
                            fontSize = 14.sp,
                            letterSpacing = 2.sp
                        )
                        Stat("Stars", "${progress.chapterStars(chapter)} / 30")
                        val cleared = (chapter * 10 until chapter * 10 + 10).count { progress.stars(it) > 0 }
                        Stat("Jobs docked", "$cleared / 10")
                    }
                }
            }
        }
    }
}

@Composable
fun SetupScreen(
    progress: ProgressRepository,
    backdrop: String,
    onBackdrop: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var sound by remember { mutableStateOf(progress.sound()) }
    var vibration by remember { mutableStateOf(progress.vibration()) }
    var pick by remember { mutableIntStateOf(progress.backdrop()) }
    var wipe by remember { mutableStateOf(false) }

    Backdrop(backdrop, dim = 0.66f) {
        Column(Modifier.fillMaxSize()) {
            Header("SETUP", onBack)
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card {
                    Text(
                        "YARD LIGHT",
                        color = Palette.Amber,
                        fontFamily = GameFonts.primary,
                        fontSize = 14.sp,
                        letterSpacing = 2.sp
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        backdrops.forEachIndexed { index, name ->
                            val chosen = index == pick
                            Column(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (chosen) Palette.Amber.copy(alpha = 0.25f) else Palette.Ink.copy(alpha = 0.5f))
                                    .clickable {
                                        pick = index
                                        progress.setBackdrop(index)
                                        onBackdrop(index)
                                    }
                                    .padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Art(name, Modifier.height(54.dp).fillMaxWidth())
                                Text(
                                    backdropNames[index],
                                    color = if (chosen) Palette.Amber else Palette.Muted,
                                    fontFamily = GameFonts.hud,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
                Card {
                    Toggle("Sound", sound) {
                        sound = it
                        progress.setSound(it)
                    }
                    Toggle("Vibration", vibration) {
                        vibration = it
                        progress.setVibration(it)
                    }
                }
                Card {
                    Text(
                        if (wipe) "This clears every star, record and award." else "Progress",
                        color = if (wipe) Palette.Danger else Palette.Muted,
                        fontFamily = GameFonts.hud,
                        fontSize = 14.sp
                    )
                    PlatePanel(
                        if (wipe) "Tap again to wipe the sheet" else "Wipe progress",
                        {
                            if (wipe) {
                                progress.resetProgress()
                                wipe = false
                                sound = progress.sound()
                                vibration = progress.vibration()
                                pick = progress.backdrop()
                                onBackdrop(pick)
                            } else {
                                wipe = true
                            }
                        },
                        Modifier.fillMaxWidth().padding(top = 10.dp).height(50.dp),
                        "btn_plate_red",
                        fontSize = 15
                    )
                }
            }
        }
    }
}

@Composable
private fun Card(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.Panel.copy(alpha = 0.9f))
            .padding(14.dp),
        content = content
    )
}

@Composable
private fun Stat(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
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
private fun Toggle(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            color = Palette.Cream,
            fontFamily = GameFonts.hud,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = value,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Palette.Ink,
                checkedTrackColor = Palette.Amber,
                uncheckedThumbColor = Palette.Muted,
                uncheckedTrackColor = Palette.Steel
            )
        )
    }
}

@Composable
fun SplashScreen(onDone: () -> Unit) {
    val fade by animateFloatAsState(1f, tween(900), label = "splash")
    Backdrop("splash", dim = 0.35f) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.alpha(fade)) {
                Art("logo", Modifier.size(140.dp))
                Text(
                    "YARDGOAT",
                    color = Palette.Cream,
                    fontFamily = GameFonts.primary,
                    fontSize = 40.sp,
                    letterSpacing = 8.sp
                )
                PlatePanel(
                    "Clock on",
                    onDone,
                    Modifier.padding(top = 26.dp).size(width = 220.dp, height = 56.dp),
                    "btn_plate_amber",
                    Palette.Ink,
                    fontSize = 20
                )
            }
        }
    }
}
