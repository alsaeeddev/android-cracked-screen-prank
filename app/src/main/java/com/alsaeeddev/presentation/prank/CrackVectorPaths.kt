package com.alsaeeddev.presentation.prank

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import com.alsaeeddev.domain.model.CrackStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Geometric definition of crack layers for vector rendering.
 *
 * @property primaryPaths Heavy structural fracture fault lines
 * @property secondaryPaths Delicate hairline fissures branching from primary lines
 * @property coreShards Polygons representing crushed glass fragments at impact epicenter
 * @property impactCenters List of point locations where impacts occurred
 */
data class CrackGeometry(
    val primaryPaths: List<Path>,
    val secondaryPaths: List<Path>,
    val coreShards: List<Path>,
    val impactCenters: List<Offset>
)

/**
 * Vector generator synthesizing realistic tempered and annealed glass fractures.
 * Scales dynamically to any canvas width, height, and display density.
 */
object CrackVectorPaths {

    fun generate(
        style: CrackStyle,
        width: Float,
        height: Float,
        seed: Long = 1337L
    ): CrackGeometry {
        if (width <= 0f || height <= 0f) {
            return CrackGeometry(emptyList(), emptyList(), emptyList(), emptyList())
        }

        return when (style) {
            CrackStyle.SPIDERWEB -> generateSpiderweb(width, height, seed)
            CrackStyle.CORNER_SHATTER -> generateCornerShatter(width, height, seed)
            CrackStyle.BULLET_HOLE -> generateBulletHole(width, height, seed)
            CrackStyle.HORIZONTAL_FRACTURE -> generateHorizontalFracture(width, height, seed)
            CrackStyle.MULTI_IMPACT -> generateMultiImpact(width, height, seed)
        }
    }

    /**
     * Classic spiderweb fracture originating near center screen with concentric stress arcs.
     */
    private fun generateSpiderweb(width: Float, height: Float, seed: Long): CrackGeometry {
        val rng = Random(seed)
        val center = Offset(width * 0.48f, height * 0.45f)
        val maxRadius = min(width, height) * 0.75f

        val primary = mutableListOf<Path>()
        val secondary = mutableListOf<Path>()
        val shards = mutableListOf<Path>()

        val numRadialRays = 14
        val rayEndPoints = mutableListOf<List<Offset>>()

        // 1. Radiating fault lines with sharp jitter
        for (i in 0 until numRadialRays) {
            val baseAngle = (i.toFloat() / numRadialRays) * (2f * PI.toFloat())
            val path = Path()
            path.moveTo(center.x, center.y)

            val points = mutableListOf<Offset>()
            points.add(center)

            val segments = 8
            var currentPos = center
            val rayLength = maxRadius * (0.6f + rng.nextFloat() * 0.65f)

            for (s in 1..segments) {
                val progress = s.toFloat() / segments
                val dist = progress * rayLength
                val angleJitter = (rng.nextFloat() - 0.5f) * 0.22f
                val nextAngle = baseAngle + angleJitter
                val nextPos = Offset(
                    center.x + cos(nextAngle) * dist,
                    center.y + sin(nextAngle) * dist
                )
                path.lineTo(nextPos.x, nextPos.y)
                points.add(nextPos)
                currentPos = nextPos

                // Hairline branch
                if (s in 2..6 && rng.nextFloat() > 0.45f) {
                    val branchPath = Path()
                    branchPath.moveTo(nextPos.x, nextPos.y)
                    val branchAngle = nextAngle + (if (rng.nextBoolean()) 0.55f else -0.55f)
                    val branchLen = 40f + rng.nextFloat() * 70f
                    val branchEnd = Offset(
                        nextPos.x + cos(branchAngle) * branchLen,
                        nextPos.y + sin(branchAngle) * branchLen
                    )
                    branchPath.lineTo(branchEnd.x, branchEnd.y)
                    secondary.add(branchPath)
                }
            }
            primary.add(path)
            rayEndPoints.add(points)
        }

        // 2. Concentric polygonal stress rings connecting rays
        val numRings = 7
        for (r in 1..numRings) {
            val ringPath = Path()
            var started = false
            for (rayIdx in 0 until numRadialRays) {
                val rayPoints = rayEndPoints[rayIdx]
                val pointIdx = min(r, rayPoints.size - 1)
                val pt = rayPoints[pointIdx]
                val jitterPt = Offset(
                    pt.x + (rng.nextFloat() - 0.5f) * 14f,
                    pt.y + (rng.nextFloat() - 0.5f) * 14f
                )
                if (!started) {
                    ringPath.moveTo(jitterPt.x, jitterPt.y)
                    started = true
                } else {
                    ringPath.lineTo(jitterPt.x, jitterPt.y)
                }
            }
            ringPath.close()
            primary.add(ringPath)
        }

        // 3. Impact epicenter pulverized glass shards
        shards.addAll(generateCoreShards(center, radius = 45f, rng))

        return CrackGeometry(primary, secondary, shards, listOf(center))
    }

    /**
     * Corner shatter pattern radiating violently from the top-right corner.
     */
    private fun generateCornerShatter(width: Float, height: Float, seed: Long): CrackGeometry {
        val rng = Random(seed)
        val impact = Offset(width * 0.95f, height * 0.05f)
        val primary = mutableListOf<Path>()
        val secondary = mutableListOf<Path>()
        val shards = mutableListOf<Path>()

        val numRays = 18
        for (i in 0 until numRays) {
            val baseAngle = PI.toFloat() * 0.5f + (i.toFloat() / numRays) * (PI.toFloat() * 0.75f)
            val path = Path()
            path.moveTo(impact.x, impact.y)

            val maxDist = min(width, height) * (0.8f + rng.nextFloat() * 0.8f)
            var cur = impact
            val steps = 10
            for (s in 1..steps) {
                val d = (s.toFloat() / steps) * maxDist
                val angle = baseAngle + (rng.nextFloat() - 0.5f) * 0.28f
                val next = Offset(impact.x + cos(angle) * d, impact.y + sin(angle) * d)
                path.lineTo(next.x, next.y)

                if (rng.nextFloat() > 0.4f) {
                    val branch = Path()
                    branch.moveTo(next.x, next.y)
                    val bAngle = angle + (if (rng.nextBoolean()) 0.7f else -0.7f)
                    val bLen = 35f + rng.nextFloat() * 85f
                    branch.lineTo(next.x + cos(bAngle) * bLen, next.y + sin(bAngle) * bLen)
                    secondary.add(branch)
                }
                cur = next
            }
            primary.add(path)
        }

        // Corner cross stress arcs
        for (dist in listOf(80f, 160f, 260f, 380f, 520f)) {
            val arcPath = Path()
            val startAngle = PI.toFloat() * 0.55f
            val endAngle = PI.toFloat() * 1.25f
            val steps = 14
            for (s in 0..steps) {
                val ang = startAngle + (s.toFloat() / steps) * (endAngle - startAngle)
                val d = dist + (rng.nextFloat() - 0.5f) * 22f
                val x = impact.x + cos(ang) * d
                val y = impact.y + sin(ang) * d
                if (s == 0) arcPath.moveTo(x, y) else arcPath.lineTo(x, y)
            }
            primary.add(arcPath)
        }

        shards.addAll(generateCoreShards(impact, radius = 65f, rng))
        return CrackGeometry(primary, secondary, shards, listOf(impact))
    }

    /**
     * Bullet hole pattern with high-velocity shock core and starburst fissures.
     */
    private fun generateBulletHole(width: Float, height: Float, seed: Long): CrackGeometry {
        val rng = Random(seed)
        val center = Offset(width * 0.52f, height * 0.42f)
        val primary = mutableListOf<Path>()
        val secondary = mutableListOf<Path>()
        val shards = mutableListOf<Path>()

        // 1. Concentric pulverized spalling crater rings
        for (radius in listOf(18f, 32f, 48f, 68f, 92f)) {
            val craterPath = Path()
            val steps = 24
            for (s in 0..steps) {
                val angle = (s.toFloat() / steps) * (2f * PI.toFloat())
                val r = radius + (rng.nextFloat() - 0.5f) * 12f
                val x = center.x + cos(angle) * r
                val y = center.y + sin(angle) * r
                if (s == 0) craterPath.moveTo(x, y) else craterPath.lineTo(x, y)
            }
            craterPath.close()
            primary.add(craterPath)
        }

        // 2. High-speed radial starburst spokes
        val rays = 20
        for (i in 0 until rays) {
            val angle = (i.toFloat() / rays) * (2f * PI.toFloat()) + (rng.nextFloat() - 0.5f) * 0.15f
            val path = Path()
            val startDist = 18f
            path.moveTo(center.x + cos(angle) * startDist, center.y + sin(angle) * startDist)

            val totalDist = min(width, height) * (0.4f + rng.nextFloat() * 0.6f)
            val steps = 7
            for (s in 1..steps) {
                val d = startDist + (s.toFloat() / steps) * (totalDist - startDist)
                val jAngle = angle + (rng.nextFloat() - 0.5f) * 0.18f
                val next = Offset(center.x + cos(jAngle) * d, center.y + sin(jAngle) * d)
                path.lineTo(next.x, next.y)

                if (rng.nextFloat() > 0.5f) {
                    val side = Path()
                    side.moveTo(next.x, next.y)
                    val sAngle = jAngle + (if (rng.nextBoolean()) 0.65f else -0.65f)
                    val sLen = 30f + rng.nextFloat() * 50f
                    side.lineTo(next.x + cos(sAngle) * sLen, next.y + sin(sAngle) * sLen)
                    secondary.add(side)
                }
            }
            primary.add(path)
        }

        shards.addAll(generateCoreShards(center, radius = 55f, rng))
        return CrackGeometry(primary, secondary, shards, listOf(center))
    }

    /**
     * Cross-screen horizontal fracture simulating bending stress across phone glass.
     */
    private fun generateHorizontalFracture(width: Float, height: Float, seed: Long): CrackGeometry {
        val rng = Random(seed)
        val primary = mutableListOf<Path>()
        val secondary = mutableListOf<Path>()
        val shards = mutableListOf<Path>()

        val baseHeights = listOf(height * 0.38f, height * 0.58f)
        val impacts = mutableListOf<Offset>()

        baseHeights.forEachIndexed { lineIdx, baseY ->
            val mainLine = Path()
            mainLine.moveTo(0f, baseY + (rng.nextFloat() - 0.5f) * 30f)

            val segments = 22
            var curX = 0f
            var curY = baseY
            val stepX = width / segments

            for (s in 1..segments) {
                val nextX = s * stepX
                val nextY = baseY + (rng.nextFloat() - 0.5f) * 48f
                mainLine.lineTo(nextX, nextY)

                // Vertical jagged splinter
                if (rng.nextFloat() > 0.35f) {
                    val splinter = Path()
                    splinter.moveTo(nextX, nextY)
                    val dir = if (rng.nextBoolean()) 1f else -1f
                    val splLen = 45f + rng.nextFloat() * 110f
                    val splAngle = (PI.toFloat() * 0.5f * dir) + (rng.nextFloat() - 0.5f) * 0.35f
                    val endPt = Offset(nextX + cos(splAngle) * splLen, nextY + sin(splAngle) * splLen)
                    splinter.lineTo(endPt.x, endPt.y)
                    if (rng.nextBoolean()) {
                        primary.add(splinter)
                    } else {
                        secondary.add(splinter)
                    }
                }
                curX = nextX
                curY = nextY
            }
            primary.add(mainLine)
            val impactPt = Offset(width * (0.35f + lineIdx * 0.3f), baseY)
            impacts.add(impactPt)
            shards.addAll(generateCoreShards(impactPt, radius = 35f, rng))
        }

        return CrackGeometry(primary, secondary, shards, impacts)
    }

    /**
     * Multiple catastrophic impact epicenters with interconnected fracture lines.
     */
    private fun generateMultiImpact(width: Float, height: Float, seed: Long): CrackGeometry {
        val rng = Random(seed)
        val primary = mutableListOf<Path>()
        val secondary = mutableListOf<Path>()
        val shards = mutableListOf<Path>()

        val impacts = listOf(
            Offset(width * 0.28f, height * 0.32f),
            Offset(width * 0.72f, height * 0.48f),
            Offset(width * 0.42f, height * 0.78f)
        )

        // Connect impacts with severe shear cracks
        for (i in 0 until impacts.size) {
            val from = impacts[i]
            val to = impacts[(i + 1) % impacts.size]

            val bridge = Path()
            bridge.moveTo(from.x, from.y)
            val dist = kotlin.math.hypot(to.x - from.x, to.y - from.y)
            val steps = 8
            for (s in 1..steps) {
                val t = s.toFloat() / steps
                val midX = from.x + (to.x - from.x) * t + (rng.nextFloat() - 0.5f) * 40f
                val midY = from.y + (to.y - from.y) * t + (rng.nextFloat() - 0.5f) * 40f
                bridge.lineTo(midX, midY)
            }
            primary.add(bridge)
        }

        // Radiating rays around each impact
        impacts.forEach { epicenter ->
            val rays = 10
            for (r in 0 until rays) {
                val angle = (r.toFloat() / rays) * (2f * PI.toFloat())
                val rayPath = Path()
                rayPath.moveTo(epicenter.x, epicenter.y)
                val len = min(width, height) * (0.25f + rng.nextFloat() * 0.35f)
                val steps = 5
                for (s in 1..steps) {
                    val d = (s.toFloat() / steps) * len
                    val a = angle + (rng.nextFloat() - 0.5f) * 0.22f
                    val pt = Offset(epicenter.x + cos(a) * d, epicenter.y + sin(a) * d)
                    rayPath.lineTo(pt.x, pt.y)
                }
                primary.add(rayPath)
            }
            shards.addAll(generateCoreShards(epicenter, radius = 40f, rng))
        }

        return CrackGeometry(primary, secondary, shards, impacts)
    }

    /**
     * Generates an extra local crack pattern centered at an interactive touch point.
     */
    fun generateTouchImpact(touchX: Float, touchY: Float, seed: Long): CrackGeometry {
        val rng = Random(seed)
        val center = Offset(touchX, touchY)
        val primary = mutableListOf<Path>()
        val secondary = mutableListOf<Path>()
        val shards = mutableListOf<Path>()

        val numRays = 7
        for (i in 0 until numRays) {
            val angle = (i.toFloat() / numRays) * (2f * PI.toFloat()) + (rng.nextFloat() - 0.5f) * 0.2f
            val path = Path()
            path.moveTo(center.x, center.y)

            val len = 90f + rng.nextFloat() * 120f
            val steps = 4
            for (s in 1..steps) {
                val d = (s.toFloat() / steps) * len
                val jAngle = angle + (rng.nextFloat() - 0.5f) * 0.25f
                path.lineTo(center.x + cos(jAngle) * d, center.y + sin(jAngle) * d)
            }
            primary.add(path)
        }

        shards.addAll(generateCoreShards(center, radius = 24f, rng))
        return CrackGeometry(primary, secondary, shards, listOf(center))
    }

    /**
     * Produces small pulverized polygon glass shards at impact points.
     */
    private fun generateCoreShards(center: Offset, radius: Float, rng: Random): List<Path> {
        val shards = mutableListOf<Path>()
        val count = 12
        for (i in 0 until count) {
            val startAngle = (i.toFloat() / count) * (2f * PI.toFloat())
            val endAngle = ((i + 1).toFloat() / count) * (2f * PI.toFloat())

            val rInner = radius * 0.2f * rng.nextFloat()
            val rOuter = radius * (0.6f + rng.nextFloat() * 0.5f)

            val shardPath = Path()
            val p1 = Offset(center.x + cos(startAngle) * rInner, center.y + sin(startAngle) * rInner)
            val p2 = Offset(center.x + cos(startAngle) * rOuter, center.y + sin(startAngle) * rOuter)
            val p3 = Offset(center.x + cos(endAngle) * rOuter, center.y + sin(endAngle) * rOuter)
            val p4 = Offset(center.x + cos(endAngle) * rInner, center.y + sin(endAngle) * rInner)

            shardPath.moveTo(p1.x, p1.y)
            shardPath.lineTo(p2.x, p2.y)
            shardPath.lineTo(p3.x, p3.y)
            shardPath.lineTo(p4.x, p4.y)
            shardPath.close()

            shards.add(shardPath)
        }
        return shards
    }
}
