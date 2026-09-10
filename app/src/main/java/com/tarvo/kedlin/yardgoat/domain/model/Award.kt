package com.tarvo.kedlin.yardgoat.domain.model

data class Award(val id: String, val title: String, val detail: String, val icon: String)

data class RunReport(
    val job: Int,
    val kind: JobKind,
    val docked: Boolean,
    val verdict: String,
    val stars: Int,
    val time: Float,
    val shunts: Int,
    val conesHit: Int,
    val offset: Float,
    val angleError: Float,
    val timeLeft: Float
)

object AwardBook {
    val all = listOf(
        Award("first", "On the Bumpers", "Dock a trailer for the first time.", "aw_first"),
        Award("clean", "Clean Yard", "Dock without touching a single cone.", "aw_clean"),
        Award("square", "Square to the Dock", "Finish within two degrees of the bay line.", "aw_square"),
        Award("tight", "Hair's Breadth", "Finish within thirty centimetres of the marking.", "aw_tight"),
        Award("quick", "Straight In", "Dock a bay in under twenty five seconds.", "aw_quick"),
        Award("onepull", "One Pull", "Dock without changing direction once.", "aw_onepull"),
        Award("coupler", "Kingpin", "Finish a coupling job.", "aw_coupler"),
        Award("blind", "Blind Side", "Finish a blindside job.", "aw_blind"),
        Award("parallel", "Kerb Artist", "Finish a parallel park.", "aw_parallel"),
        Award("window", "Beat the Gate", "Finish a window job with a fifth of the clock left.", "aw_window"),
        Award("nocones", "Cone Free", "Clear twenty jobs in total.", "aw_nocones"),
        Award("apron", "Apron Cleared", "Clear every job on the Depot Apron.", "aw_apron"),
        Award("cold", "Cold Store Cleared", "Clear every job at the Cold Store.", "aw_cold"),
        Award("narrows", "Narrows Cleared", "Clear every job in The Narrows.", "aw_narrows"),
        Award("night", "Night Yard Cleared", "Clear every job in the Night Yard.", "aw_night"),
        Award("master", "Yardgoat", "Take three stars on every job in the yard.", "aw_master")
    )
}
