package com.tarvo.kedlin.yardgoat.domain.repository

import com.tarvo.kedlin.yardgoat.domain.model.RunReport

interface ProgressRepository {
    fun stars(job: Int): Int
    fun bestTime(job: Int): Float
    fun unlocked(job: Int): Boolean
    fun openJob(job: Int)
    fun totalStars(): Int
    fun jobsCleared(): Int
    fun chapterStars(chapter: Int): Int
    fun chapterCleared(chapter: Int): Boolean
    fun record(report: RunReport)
    fun recordFailure()
    fun statDocked(): Int
    fun statFailed(): Int
    fun statShunts(): Int
    fun statCones(): Int
    fun statDriveTime(): Float
    fun bestOffset(): Float
    fun awardUnlocked(id: String): Boolean
    fun unlockAward(id: String): Boolean
    fun sound(): Boolean
    fun setSound(value: Boolean)
    fun vibration(): Boolean
    fun setVibration(value: Boolean)
    fun backdrop(): Int
    fun setBackdrop(value: Int)
    fun tutorialSeen(): Boolean
    fun markTutorialSeen()
    fun resetProgress()
}
