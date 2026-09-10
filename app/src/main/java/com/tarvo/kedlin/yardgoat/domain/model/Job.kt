package com.tarvo.kedlin.yardgoat.domain.model

enum class JobKind(val tag: String, val brief: String, val bonus: String) {
    Straight("STRAIGHT", "Back the trailer straight down the lane into the bay.", "No cone touched, and square to the dock."),
    Alley("ALLEY", "Ninety degree alley dock from the driving lane.", "No cone touched, inside the shunt limit."),
    Blindside("BLINDSIDE", "The bay is on your blind side. No mirror helps you here.", "No cone touched, inside the shunt limit."),
    Parallel("PARALLEL", "Park the trailer along the wall between two rigs.", "No cone touched, and within half a metre."),
    Coupling("COUPLING", "Start bobtail. Back under the kingpin, then dock the trailer.", "No cone touched, inside the shunt limit."),
    Window("WINDOW", "The dispatch window closes. Be on the bumpers before it does.", "No cone touched, with time still on the clock.")
}

data class Job(
    val index: Int,
    val chapter: Int,
    val name: String,
    val brief: String,
    val kind: JobKind,
    val startSeed: Int,
    val bayX: Float,
    val bayZ: Float,
    val bayFacing: Float,
    val cones: Int,
    val barrels: Int,
    val parked: Int,
    val masts: Int,
    val forklift: Boolean,
    val lane: Float,
    val parTime: Float,
    val parShunts: Int,
    val timeLimit: Float,
    val night: Boolean
) {
    val bobtail: Boolean get() = kind == JobKind.Coupling
    val timed: Boolean get() = timeLimit > 0f
}

object YardCatalog {

    const val JOB_COUNT = 40
    const val STAR_COUNT = JOB_COUNT * 3

    val chapters = listOf("Depot Apron", "Cold Store", "The Narrows", "Night Yard")

    val chapterBrief = listOf(
        "Wide concrete, forgiving bays, nobody watching.",
        "Tight bays under the chill doors, and the aisles are full.",
        "Lanes barely wider than the trailer, cones everywhere.",
        "Floodlights, dispatch windows and no room for a second try."
    )

    private const val EAST = 0f
    private const val NORTH = 1.5708f
    private const val WEST = 3.1416f
    private const val SOUTH = -1.5708f

    val jobs: List<Job> = listOf(
        row(0, "Empty Apron", "One bay, no traffic, all the room in the world.", JobKind.Straight, 26f, 0f, EAST, parTime = 40f, parShunts = 2, lane = 16f),
        row(1, "Second Door", "Same lane, the bay two doors along.", JobKind.Straight, 26f, -4f, EAST, cones = 4, parTime = 42f, parShunts = 2, lane = 15f),
        row(2, "First Corner", "The bay sits off the side of the lane.", JobKind.Alley, 22f, -8f, NORTH, cones = 6, parTime = 52f, parShunts = 3, lane = 14f),
        row(3, "Cone Row", "Somebody left the whole row of cones out.", JobKind.Straight, 26f, 2f, EAST, cones = 12, parTime = 46f, parShunts = 2, lane = 13f),
        row(4, "Yard Goat", "The short hop every shunter does a hundred times a shift.", JobKind.Alley, 20f, 10f, WEST, cones = 8, barrels = 2, parTime = 55f, parShunts = 3, lane = 13f),
        row(5, "Neighbours", "Two rigs already on the bumpers either side.", JobKind.Alley, 24f, -2f, NORTH, cones = 8, parked = 2, parTime = 58f, parShunts = 3, lane = 12f),
        row(6, "Hitch Up", "Your trailer is parked across the yard. Go and get it.", JobKind.Coupling, 24f, 6f, EAST, cones = 6, parTime = 70f, parShunts = 4, lane = 13f),
        row(7, "Wrong Side", "Dispatch put the load on your blind side.", JobKind.Blindside, 22f, 12f, SOUTH, cones = 8, barrels = 2, parTime = 62f, parShunts = 4, lane = 12f),
        row(8, "Kerb Line", "Slot it along the wall between two rigs.", JobKind.Parallel, 20f, 0f, EAST, cones = 6, parked = 2, parTime = 66f, parShunts = 4, lane = 12f),
        row(9, "Shift Change", "Twenty seconds before the next driver wants the lane.", JobKind.Window, 26f, -6f, EAST, cones = 8, barrels = 2, parTime = 44f, parShunts = 3, timeLimit = 70f, lane = 12f),
        row(10, "Chill Door", "The cold store bays are narrower than the apron.", JobKind.Straight, 24f, 0f, EAST, cones = 10, barrels = 2, parTime = 44f, parShunts = 2, lane = 11f),
        row(11, "Between Reefers", "Two reefer units running either side of the door.", JobKind.Alley, 22f, -6f, NORTH, cones = 10, parked = 2, parTime = 58f, parShunts = 3, lane = 11f),
        row(12, "Blind Chill", "Blind side, and the aisle is half a trailer wide.", JobKind.Blindside, 22f, 10f, SOUTH, cones = 10, barrels = 3, parTime = 64f, parShunts = 4, lane = 10.5f),
        row(13, "Forklift Run", "A forklift is working the aisle. It will not stop for you.", JobKind.Alley, 22f, -4f, NORTH, cones = 8, barrels = 2, forklift = true, parTime = 60f, parShunts = 3, lane = 11f),
        row(14, "Pallet Line", "Pallets stacked where you wanted to swing.", JobKind.Straight, 24f, 4f, EAST, cones = 14, barrels = 3, parTime = 46f, parShunts = 2, lane = 10f),
        row(15, "Tight Slot", "Parallel, with barely a trailer length of room.", JobKind.Parallel, 20f, -2f, EAST, cones = 8, parked = 2, barrels = 2, parTime = 70f, parShunts = 5, lane = 10f),
        row(16, "Cold Coupling", "Hook the reefer, then put it on the chill door.", JobKind.Coupling, 22f, 8f, EAST, cones = 8, barrels = 2, parTime = 76f, parShunts = 5, lane = 10.5f),
        row(17, "Two Minute Slot", "The chill door only opens for a minute.", JobKind.Window, 24f, -4f, EAST, cones = 10, barrels = 2, parTime = 46f, parShunts = 3, timeLimit = 62f, lane = 10.5f),
        row(18, "Full House", "Every neighbouring bay is taken.", JobKind.Alley, 22f, 0f, NORTH, cones = 12, parked = 3, parTime = 62f, parShunts = 4, lane = 10f),
        row(19, "Cold Crown", "The last door on the chill run, blind side, on the clock.", JobKind.Blindside, 20f, 12f, SOUTH, cones = 12, barrels = 3, parked = 2, parTime = 70f, parShunts = 5, timeLimit = 95f, lane = 9.5f),
        row(20, "The Narrows", "Two walls, one trailer, no margin.", JobKind.Straight, 22f, 0f, EAST, cones = 16, barrels = 3, parTime = 46f, parShunts = 3, lane = 9f),
        row(21, "Elbow", "A hard ninety with a wall behind the swing.", JobKind.Alley, 20f, -6f, NORTH, cones = 14, barrels = 4, parTime = 62f, parShunts = 4, lane = 9f),
        row(22, "Blind Elbow", "The same elbow, from the side you cannot see.", JobKind.Blindside, 20f, 10f, SOUTH, cones = 14, barrels = 4, parTime = 68f, parShunts = 5, lane = 9f),
        row(23, "Traffic", "Two forklifts, one lane, and your trailer in the middle.", JobKind.Alley, 22f, -2f, NORTH, cones = 12, barrels = 3, forklift = true, parTime = 64f, parShunts = 4, lane = 9f),
        row(24, "Squeeze", "Parallel park with a rig's nose at each end.", JobKind.Parallel, 18f, 0f, EAST, cones = 12, parked = 3, barrels = 2, parTime = 74f, parShunts = 5, lane = 9f),
        row(25, "Dead Ground", "You lose sight of the bay for the whole approach.", JobKind.Blindside, 20f, 8f, SOUTH, cones = 14, barrels = 5, parked = 2, parTime = 70f, parShunts = 5, lane = 8.5f),
        row(26, "Late Hook", "Couple in the aisle, then thread it through the cones.", JobKind.Coupling, 20f, 6f, EAST, cones = 14, barrels = 4, parTime = 82f, parShunts = 6, lane = 8.5f),
        row(27, "Clock Run", "Fifty seconds. The gate closes after that.", JobKind.Window, 22f, -4f, EAST, cones = 14, barrels = 4, parTime = 48f, parShunts = 3, timeLimit = 58f, lane = 9f),
        row(28, "Crossed Lanes", "A forklift crosses exactly where you need to swing.", JobKind.Alley, 20f, -8f, NORTH, cones = 16, barrels = 4, forklift = true, parTime = 66f, parShunts = 4, lane = 8.5f),
        row(29, "Narrow Crown", "The narrowest bay in the yard, both mirrors useless.", JobKind.Blindside, 18f, 10f, SOUTH, cones = 16, barrels = 5, parked = 2, parTime = 74f, parShunts = 6, lane = 8f),
        row(30, "Floodlight", "Night shift. Only the dock lamps to line up on.", JobKind.Straight, 22f, 2f, EAST, cones = 14, barrels = 3, masts = 3, night = true, parTime = 48f, parShunts = 3, lane = 9f),
        row(31, "Dark Elbow", "A ninety in the dark, cones you can barely see.", JobKind.Alley, 20f, -6f, NORTH, cones = 16, barrels = 4, masts = 3, night = true, parTime = 64f, parShunts = 4, lane = 8.5f),
        row(32, "Night Hook", "Find the trailer, couple it, dock it. All in the dark.", JobKind.Coupling, 20f, 8f, EAST, cones = 14, barrels = 4, masts = 3, night = true, parTime = 86f, parShunts = 6, lane = 8.5f),
        row(33, "Gate Window", "Forty five seconds to the gate cut-off.", JobKind.Window, 22f, -4f, EAST, cones = 16, barrels = 4, masts = 2, night = true, parTime = 46f, parShunts = 3, timeLimit = 54f, lane = 8.5f),
        row(34, "Blind Night", "Blind side, night, and a full row of neighbours.", JobKind.Blindside, 18f, 10f, SOUTH, cones = 16, barrels = 5, parked = 3, masts = 2, night = true, parTime = 76f, parShunts = 6, lane = 8f),
        row(35, "Yard Traffic", "The night forklift does not care that you are reversing.", JobKind.Alley, 20f, -4f, NORTH, cones = 16, barrels = 5, masts = 3, forklift = true, night = true, parTime = 68f, parShunts = 5, lane = 8f),
        row(36, "Wall Slot", "Parallel, in the dark, against the perimeter wall.", JobKind.Parallel, 18f, 0f, EAST, cones = 14, parked = 3, barrels = 4, masts = 2, night = true, parTime = 78f, parShunts = 6, lane = 8f),
        row(37, "Last Reefer", "One door left, and it is the awkward one.", JobKind.Blindside, 18f, 8f, SOUTH, cones = 18, barrels = 5, parked = 2, masts = 3, night = true, parTime = 78f, parShunts = 6, lane = 7.5f),
        row(38, "Double Shift", "Couple, thread the narrows, dock before the window shuts.", JobKind.Coupling, 18f, 6f, EAST, cones = 16, barrels = 5, masts = 3, night = true, parTime = 90f, parShunts = 7, timeLimit = 130f, lane = 7.5f),
        row(39, "Yardgoat", "The bay nobody on the shift will take. Yours now.", JobKind.Blindside, 16f, 10f, SOUTH, cones = 18, barrels = 6, parked = 3, masts = 3, forklift = true, night = true, parTime = 82f, parShunts = 7, timeLimit = 140f, lane = 7f)
    )

    private fun row(
        index: Int,
        name: String,
        brief: String,
        kind: JobKind,
        bayX: Float,
        bayZ: Float,
        dockSide: Float,
        cones: Int = 0,
        barrels: Int = 0,
        parked: Int = 0,
        masts: Int = 0,
        forklift: Boolean = false,
        night: Boolean = false,
        lane: Float,
        parTime: Float,
        parShunts: Int,
        timeLimit: Float = 0f
    ) = Job(
        index = index, chapter = index / 10, name = name, brief = brief, kind = kind,
        startSeed = index,
        bayX = bayX, bayZ = bayZ, bayFacing = flip(dockSide),
        cones = cones, barrels = barrels, parked = parked, masts = masts,
        forklift = forklift, lane = lane, parTime = parTime, parShunts = parShunts,
        timeLimit = timeLimit, night = night
    )

    private fun flip(angle: Float): Float {
        var v = angle + 3.14159f
        while (v > 3.14159f) v -= 6.28318f
        while (v < -3.14159f) v += 6.28318f
        return v
    }

    fun job(index: Int): Job = jobs[index.coerceIn(0, jobs.lastIndex)]

    fun grade(job: Job): String = when {
        job.index < 6 -> "Easy shift"
        job.index < 12 -> "Steady"
        job.index < 20 -> "Awkward"
        job.index < 28 -> "Tight"
        job.index < 36 -> "Hard"
        else -> "Nightmare"
    }
}
