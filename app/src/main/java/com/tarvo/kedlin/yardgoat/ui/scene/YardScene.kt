package com.tarvo.kedlin.yardgoat.ui.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.filament.Engine
import com.google.android.filament.View
import com.tarvo.kedlin.yardgoat.domain.engine.Rig
import com.tarvo.kedlin.yardgoat.domain.engine.Solid
import io.github.sceneview.SceneScope
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.CameraNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberView
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val GROUND_SPAN = 7
private const val GROUND_STEP = 26f
private const val TILE_SCALE = 26f
private const val TRACTOR_SCALE = 6.2f
private const val TRACTOR_LIFT = 2.07f
private const val TRAILER_SCALE = 12.5f
private const val TRAILER_LIFT = 2.25f
private const val DOCK_SCALE = 6.0f
private const val DOCK_LIFT = 3.0f
private const val DOCK_BAYS = 7
private const val DOCK_PITCH = 4.6f
private const val CONE_SCALE = 0.85f
private const val CONE_LIFT = 0.42f
private const val BARREL_SCALE = 1.15f
private const val BARREL_LIFT = 0.57f
private const val MAST_SCALE = 11f
private const val MAST_LIFT = 5.5f
private const val FORKLIFT_SCALE = 3.2f
private const val FORKLIFT_LIFT = 1.3f
private const val CONE_NODES = 20
private const val BARREL_NODES = 8
private const val MAST_NODES = 4
private const val TRAILER_NODES = 4

private class Fleet {
    val ground = ArrayList<ModelNode>()
    val docks = ArrayList<ModelNode>()
    val tractor = ArrayList<ModelNode>()
    val trailers = ArrayList<ModelNode>()
    val cones = ArrayList<ModelNode>()
    val barrels = ArrayList<ModelNode>()
    val masts = ArrayList<ModelNode>()
    val forklift = ArrayList<ModelNode>()
    var last = 0L
    var camX = 0f
    var camZ = 0f
    var seeded = false
}

@Composable
fun YardScene(rig: Rig, modifier: Modifier = Modifier, onFrameStep: (Float) -> Unit) {
    val filament = rememberEngine(engineCreator = { egl ->
        Engine.Builder()
            .sharedContext(egl)
            .feature("backend.disable_parallel_shader_compile", true)
            .build()
    })
    val modelLoader = rememberModelLoader(filament)
    val fleet = remember { Fleet() }
    val camera = rememberCameraNode(filament)
    val mainLight = rememberMainLightNode(filament) {
        intensity = if (rig.job.night) 68_000f else 108_000f
    }
    val bloomOff = remember { View.BloomOptions().apply { enabled = false } }
    val dynResOff = remember { View.DynamicResolutionOptions().apply { enabled = false } }
    val view = rememberView(filament).apply {
        isPostProcessingEnabled = false
        bloomOptions = bloomOff
        dynamicResolutionOptions = dynResOff
        setShadowingEnabled(false)
        setScreenSpaceRefractionEnabled(false)
    }

    SceneView(
        modifier = modifier,
        engine = filament,
        modelLoader = modelLoader,
        view = view,
        isOpaque = false,
        surfaceType = SurfaceType.TextureSurface,
        cameraNode = camera,
        mainLightNode = mainLight,
        onFrame = { nanos ->
            view.isPostProcessingEnabled = false
            view.bloomOptions = bloomOff
            view.dynamicResolutionOptions = dynResOff
            view.setShadowingEnabled(false)
            view.setScreenSpaceRefractionEnabled(false)

            val raw = if (fleet.last == 0L) 0f else (nanos - fleet.last) / 1_000_000_000f
            fleet.last = nanos
            val dt = raw.coerceIn(0f, 0.05f)
            onFrameStep(dt)
            if (!fleet.seeded) {
                fleet.seeded = true
                seedYard(fleet, rig)
                fleet.camX = rig.x
                fleet.camZ = rig.z
            }
            placeRig(fleet, rig)
            placeMovers(fleet, rig)
            trackCamera(fleet, rig, camera, dt)
        },
        content = {
            fill(modelLoader, "models/ground.glb", GROUND_SPAN * GROUND_SPAN, fleet.ground, TILE_SCALE)
            fill(modelLoader, "models/dock.glb", DOCK_BAYS, fleet.docks, DOCK_SCALE)
            fill(modelLoader, "models/tractor.glb", 1, fleet.tractor, TRACTOR_SCALE)
            fill(modelLoader, "models/trailer.glb", TRAILER_NODES, fleet.trailers, TRAILER_SCALE)
            fill(modelLoader, "models/cone.glb", CONE_NODES, fleet.cones, CONE_SCALE)
            fill(modelLoader, "models/barrel.glb", BARREL_NODES, fleet.barrels, BARREL_SCALE)
            fill(modelLoader, "models/lightmast.glb", MAST_NODES, fleet.masts, MAST_SCALE)
            fill(modelLoader, "models/forklift.glb", 1, fleet.forklift, FORKLIFT_SCALE)
        }
    )
}

@Composable
private fun SceneScope.fill(
    modelLoader: ModelLoader,
    path: String,
    count: Int,
    out: ArrayList<ModelNode>,
    scaleToUnits: Float
) {
    val instances: List<ModelInstance> = remember(path) { modelLoader.createInstancedModel(path, count) }
    instances.forEach { instance ->
        ModelNode(
            modelInstance = instance,
            scaleToUnits = scaleToUnits,
            isVisible = false,
            apply = { out.add(this) }
        )
    }
}

private fun yaw(heading: Float): Float = -Math.toDegrees(heading.toDouble()).toFloat()

private fun seedYard(fleet: Fleet, rig: Rig) {
    for (i in fleet.ground.indices) {
        val row = i / GROUND_SPAN
        val col = i % GROUND_SPAN
        val node = fleet.ground[i]
        node.position = Position(
            (col - GROUND_SPAN / 2) * GROUND_STEP,
            -TILE_SCALE * 0.0416f,
            (row - GROUND_SPAN / 2) * GROUND_STEP
        )
        node.rotation = Rotation(-90f, 0f, 0f)
        node.isVisible = true
    }

    val job = rig.job
    val fx = cos(job.bayFacing)
    val fz = sin(job.bayFacing)
    for (i in fleet.docks.indices) {
        val slot = i - DOCK_BAYS / 2
        val node = fleet.docks[i]
        node.position = Position(
            job.bayX - fx * 1.9f + (-fz) * slot * DOCK_PITCH,
            DOCK_LIFT,
            job.bayZ - fz * 1.9f + fx * slot * DOCK_PITCH
        )
        node.rotation = Rotation(0f, yaw(job.bayFacing) + 90f, 0f)
        node.isVisible = true
    }

    var cone = 0
    var barrel = 0
    var mast = 0
    var parked = 1
    for (ob in rig.obstacles) {
        when (ob.kind) {
            Solid.Cone -> {
                if (cone >= fleet.cones.size) continue
                val node = fleet.cones[cone++]
                node.position = Position(ob.x, CONE_LIFT, ob.z)
                node.rotation = Rotation(0f, 0f, 0f)
                node.isVisible = true
            }
            Solid.Barrel -> {
                if (barrel >= fleet.barrels.size) continue
                val node = fleet.barrels[barrel++]
                node.position = Position(ob.x, BARREL_LIFT, ob.z)
                node.rotation = Rotation(0f, 0f, 0f)
                node.isVisible = true
            }
            Solid.Mast -> {
                if (mast >= fleet.masts.size) continue
                val node = fleet.masts[mast++]
                node.position = Position(ob.x, MAST_LIFT, ob.z)
                node.rotation = Rotation(0f, 0f, 0f)
                node.isVisible = true
            }
            Solid.Parked -> {
                if (parked >= fleet.trailers.size) continue
                val node = fleet.trailers[parked++]
                node.position = Position(
                    ob.x - cos(ob.heading) * Rig.TRAILER * 0.5f,
                    TRAILER_LIFT,
                    ob.z - sin(ob.heading) * Rig.TRAILER * 0.5f
                )
                node.rotation = Rotation(0f, yaw(ob.heading), 0f)
                node.isVisible = true
            }
            Solid.Forklift -> Unit
        }
    }
}

private fun placeRig(fleet: Fleet, rig: Rig) {
    fleet.tractor.firstOrNull()?.let { node ->
        node.position = Position(rig.x + cos(rig.heading) * 1.4f, TRACTOR_LIFT, rig.z + sin(rig.heading) * 1.4f)
        node.rotation = Rotation(0f, yaw(rig.heading), 0f)
        node.isVisible = true
    }
    fleet.trailers.firstOrNull()?.let { node ->
        node.position = Position(rig.trailerMidX(), TRAILER_LIFT, rig.trailerMidZ())
        node.rotation = Rotation(0f, yaw(rig.trailerHeading), 0f)
        node.isVisible = true
    }
}

private fun placeMovers(fleet: Fleet, rig: Rig) {
    var cone = 0
    for (ob in rig.obstacles) {
        if (ob.kind != Solid.Cone) continue
        if (cone >= fleet.cones.size) break
        val node = fleet.cones[cone++]
        if (ob.down) {
            node.position = Position(ob.x, 0.18f, ob.z)
            node.rotation = Rotation(84f, 0f, 0f)
        }
    }
    fleet.forklift.firstOrNull()?.let { node ->
        if (rig.job.forklift) {
            node.position = Position(rig.forkliftX, FORKLIFT_LIFT, rig.forkliftZ)
            node.rotation = Rotation(0f, yaw(rig.forkliftHeading), 0f)
            node.isVisible = true
        } else {
            node.isVisible = false
        }
    }
}

private fun trackCamera(fleet: Fleet, rig: Rig, camera: CameraNode, dt: Float) {
    val focusX = (rig.x + rig.trailerMidX()) * 0.5f
    val focusZ = (rig.z + rig.trailerMidZ()) * 0.5f
    val ease = (dt * 3.4f).coerceIn(0f, 1f)
    fleet.camX += (focusX - fleet.camX) * ease
    fleet.camZ += (focusZ - fleet.camZ) * ease

    val spread = kotlin.math.hypot(rig.trailerRearX() - rig.job.bayX, rig.trailerRearZ() - rig.job.bayZ)
    val camY = 48f + spread.coerceAtMost(46f) * 0.42f
    val back = 34f + spread.coerceAtMost(46f) * 0.3f
    val dx = -cos(rig.job.bayFacing)
    val dz = -sin(rig.job.bayFacing)
    val camPosX = fleet.camX - dx * back
    val camPosZ = fleet.camZ - dz * back
    camera.position = Position(camPosX, camY, camPosZ)

    val fx = focusX - camPosX
    val fy = -camY
    val fz = focusZ - camPosZ
    val len = sqrt(fx * fx + fy * fy + fz * fz).coerceAtLeast(0.001f)
    val pitch = Math.toDegrees(asin((fy / len).toDouble())).toFloat()
    val yawDeg = Math.toDegrees(atan2(fx.toDouble(), fz.toDouble())).toFloat()
    camera.rotation = Rotation(pitch, 180f + yawDeg, 0f)
}
