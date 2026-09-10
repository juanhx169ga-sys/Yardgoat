package com.tarvo.kedlin.yardgoat

import android.content.Context
import com.tarvo.kedlin.yardgoat.data.prefs.PrefsProgressRepository
import com.tarvo.kedlin.yardgoat.domain.model.Job
import com.tarvo.kedlin.yardgoat.domain.repository.ProgressRepository
import com.tarvo.kedlin.yardgoat.domain.usecase.NextJob
import com.tarvo.kedlin.yardgoat.domain.usecase.RecordRun
import com.tarvo.kedlin.yardgoat.domain.usecase.ReviewAwards
import com.tarvo.kedlin.yardgoat.presentation.DriveViewModel

class AppContainer(context: Context) {

    val appContext: Context = context.applicationContext

    val progress: ProgressRepository = PrefsProgressRepository(context.applicationContext)

    val recordRun = RecordRun(progress)
    val reviewAwards = ReviewAwards(progress)
    val nextJob = NextJob(progress)

    fun driveViewModel(job: Job) = DriveViewModel(job, recordRun, reviewAwards)
}
