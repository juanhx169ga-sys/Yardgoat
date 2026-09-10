package com.tarvo.kedlin.yardgoat.domain.usecase

import com.tarvo.kedlin.yardgoat.domain.model.Award
import com.tarvo.kedlin.yardgoat.domain.model.AwardBook
import com.tarvo.kedlin.yardgoat.domain.model.JobKind
import com.tarvo.kedlin.yardgoat.domain.model.RunReport
import com.tarvo.kedlin.yardgoat.domain.model.YardCatalog
import com.tarvo.kedlin.yardgoat.domain.repository.ProgressRepository

class RecordRun(private val repo: ProgressRepository) {
    operator fun invoke(report: RunReport) {
        if (report.docked) {
            repo.record(report)
            if (report.job + 1 < YardCatalog.JOB_COUNT) repo.openJob(report.job + 1)
        } else {
            repo.recordFailure()
        }
    }
}

class ReviewAwards(private val repo: ProgressRepository) {
    operator fun invoke(report: RunReport?): List<Award> {
        val won = ArrayList<Award>()
        fun claim(id: String) {
            if (repo.unlockAward(id)) AwardBook.all.firstOrNull { it.id == id }?.let { won.add(it) }
        }
        if (repo.statDocked() > 0) claim("first")
        if (report != null && report.docked) {
            if (report.conesHit == 0) claim("clean")
            if (report.angleError <= 0.035f) claim("square")
            if (report.offset <= 0.3f) claim("tight")
            if (report.time <= 25f) claim("quick")
            if (report.shunts == 0) claim("onepull")
            when (report.kind) {
                JobKind.Coupling -> claim("coupler")
                JobKind.Blindside -> claim("blind")
                JobKind.Parallel -> claim("parallel")
                JobKind.Window -> if (report.timeLeft > 0f) claim("window")
                else -> Unit
            }
        }
        if (repo.jobsCleared() >= 20) claim("nocones")
        if (repo.chapterCleared(0)) claim("apron")
        if (repo.chapterCleared(1)) claim("cold")
        if (repo.chapterCleared(2)) claim("narrows")
        if (repo.chapterCleared(3)) claim("night")
        if (repo.totalStars() >= YardCatalog.STAR_COUNT) claim("master")
        return won
    }
}

class NextJob(private val repo: ProgressRepository) {
    operator fun invoke(): Int =
        (0 until YardCatalog.JOB_COUNT).firstOrNull { repo.unlocked(it) && repo.stars(it) == 0 }
            ?: YardCatalog.JOB_COUNT - 1
}
