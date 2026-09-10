package com.tarvo.kedlin.yardgoat.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.tarvo.kedlin.yardgoat.domain.model.RunReport
import com.tarvo.kedlin.yardgoat.domain.model.YardCatalog
import com.tarvo.kedlin.yardgoat.domain.repository.ProgressRepository

class PrefsProgressRepository(context: Context) : ProgressRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("com.tarvo.kedlin.yardgoat_game_v1", Context.MODE_PRIVATE)

    override fun stars(job: Int): Int = prefs.getInt("job_${job}_stars", 0)

    override fun bestTime(job: Int): Float = prefs.getFloat("job_${job}_time", 0f)

    override fun unlocked(job: Int): Boolean = job == 0 || prefs.getBoolean("job_${job}_open", false)

    override fun openJob(job: Int) {
        prefs.edit().putBoolean("job_${job}_open", true).apply()
    }

    override fun totalStars(): Int {
        var sum = 0
        for (i in 0 until YardCatalog.JOB_COUNT) sum += stars(i)
        return sum
    }

    override fun jobsCleared(): Int {
        var sum = 0
        for (i in 0 until YardCatalog.JOB_COUNT) if (stars(i) > 0) sum++
        return sum
    }

    override fun chapterStars(chapter: Int): Int {
        var sum = 0
        for (i in chapter * 10 until chapter * 10 + 10) sum += stars(i)
        return sum
    }

    override fun chapterCleared(chapter: Int): Boolean {
        for (i in chapter * 10 until chapter * 10 + 10) if (stars(i) == 0) return false
        return true
    }

    override fun record(report: RunReport) {
        val editor = prefs.edit()
        if (report.stars > stars(report.job)) editor.putInt("job_${report.job}_stars", report.stars)
        val prev = bestTime(report.job)
        if (prev <= 0f || report.time < prev) editor.putFloat("job_${report.job}_time", report.time)
        editor.putInt("stat_docked", statDocked() + 1)
        editor.putInt("stat_shunts", statShunts() + report.shunts)
        editor.putInt("stat_cones", statCones() + report.conesHit)
        editor.putFloat("stat_drive_time", statDriveTime() + report.time)
        val best = bestOffset()
        if (best <= 0f || report.offset < best) editor.putFloat("stat_offset", report.offset)
        editor.apply()
    }

    override fun recordFailure() {
        prefs.edit().putInt("stat_failed", statFailed() + 1).apply()
    }

    override fun statDocked(): Int = prefs.getInt("stat_docked", 0)
    override fun statFailed(): Int = prefs.getInt("stat_failed", 0)
    override fun statShunts(): Int = prefs.getInt("stat_shunts", 0)
    override fun statCones(): Int = prefs.getInt("stat_cones", 0)
    override fun statDriveTime(): Float = prefs.getFloat("stat_drive_time", 0f)
    override fun bestOffset(): Float = prefs.getFloat("stat_offset", 0f)

    override fun awardUnlocked(id: String): Boolean = prefs.getBoolean("aw_$id", false)

    override fun unlockAward(id: String): Boolean {
        if (awardUnlocked(id)) return false
        prefs.edit().putBoolean("aw_$id", true).apply()
        return true
    }

    override fun sound(): Boolean = prefs.getBoolean("sound", true)
    override fun setSound(value: Boolean) = prefs.edit().putBoolean("sound", value).apply()
    override fun vibration(): Boolean = prefs.getBoolean("vibration", true)
    override fun setVibration(value: Boolean) = prefs.edit().putBoolean("vibration", value).apply()
    override fun backdrop(): Int = prefs.getInt("backdrop", 0)
    override fun setBackdrop(value: Int) = prefs.edit().putInt("backdrop", value).apply()
    override fun tutorialSeen(): Boolean = prefs.getBoolean("tutorial_seen", false)
    override fun markTutorialSeen() = prefs.edit().putBoolean("tutorial_seen", true).apply()

    override fun resetProgress() {
        prefs.edit().clear().apply()
    }
}
