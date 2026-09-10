package com.tarvo.kedlin.yardgoat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.tarvo.kedlin.yardgoat.audio.Haptics
import com.tarvo.kedlin.yardgoat.audio.SoundBox
import com.tarvo.kedlin.yardgoat.domain.model.YardCatalog
import com.tarvo.kedlin.yardgoat.presentation.Route
import com.tarvo.kedlin.yardgoat.presentation.Shell
import com.tarvo.kedlin.yardgoat.ui.screens.AwardsScreen
import com.tarvo.kedlin.yardgoat.ui.screens.DriveScreen
import com.tarvo.kedlin.yardgoat.ui.screens.JobsScreen
import com.tarvo.kedlin.yardgoat.ui.screens.MenuScreen
import com.tarvo.kedlin.yardgoat.ui.screens.RecordsScreen
import com.tarvo.kedlin.yardgoat.ui.screens.SetupScreen
import com.tarvo.kedlin.yardgoat.ui.screens.SplashScreen
import com.tarvo.kedlin.yardgoat.ui.screens.TutorialScreen
import com.tarvo.kedlin.yardgoat.ui.screens.backdrops
import com.tarvo.kedlin.yardgoat.ui.theme.YardgoatTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        val container = AppContainer(this)
        setContent {
            YardgoatTheme {
                YardgoatApp(container)
            }
        }
    }
}

@Composable
private fun YardgoatApp(container: AppContainer) {
    val progress = container.progress
    val shell = remember { Shell() }
    val sound = remember { SoundBox(container.appContext, progress) }
    val haptics = remember { Haptics(container.appContext, progress) }
    var skin by remember { mutableIntStateOf(progress.backdrop()) }
    var pending by remember { mutableIntStateOf(0) }
    val backdrop = backdrops[skin.coerceIn(backdrops.indices)]

    DisposableEffect(Unit) {
        onDispose { sound.release() }
    }

    when (val route = shell.current) {
        Route.Splash -> SplashScreen {
            if (progress.tutorialSeen()) shell.swap(Route.Menu) else shell.swap(Route.Tutorial)
        }

        Route.Tutorial -> TutorialScreen(backdrop) {
            progress.markTutorialSeen()
            shell.swap(Route.Menu)
        }

        Route.Menu -> MenuScreen(
            progress = progress,
            backdrop = backdrop,
            nextJob = container.nextJob(),
            onDrive = { shell.go(Route.Drive(container.nextJob())) },
            onJobs = { shell.go(Route.Jobs) },
            onAwards = { shell.go(Route.Awards) },
            onRecords = { shell.go(Route.Records) },
            onSetup = { shell.go(Route.Setup) },
            onBriefing = { shell.go(Route.Tutorial) }
        )

        Route.Jobs -> JobsScreen(
            progress = progress,
            backdrop = backdrop,
            onPick = { shell.go(Route.Drive(it)) },
            onBack = { shell.home() }
        )

        is Route.Drive -> {
            val model = remember(route.job, pending) {
                container.driveViewModel(YardCatalog.job(route.job))
            }
            DriveScreen(
                model = model,
                sound = sound,
                haptics = haptics,
                onQuit = { shell.home() },
                onRetry = { pending++ },
                onNext = {
                    val next = (route.job + 1).coerceAtMost(YardCatalog.JOB_COUNT - 1)
                    shell.swap(Route.Drive(next))
                }
            )
        }

        Route.Awards -> AwardsScreen(progress, backdrop) { shell.home() }

        Route.Records -> RecordsScreen(progress, backdrop) { shell.home() }

        Route.Setup -> SetupScreen(progress, backdrop, { skin = it }) { shell.home() }
    }
}
