package com.alsaeeddev.presentation.prank

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.alsaeeddev.domain.model.CrackStyle

/**
 * Realistic vector glass crack overlay rendered entirely via Compose [Canvas].
 *
 * Employs physical light refraction modeling:
 * - Low-angle shadow pass underneath fractures creating 3D depth and displacement
 * - Core bright crystalline highlight strokes (translucent ice white)
 * - Pulverized frosted micro-shards at impact epicenters
 * - Non-blocking GPU compositor light sweep ("glass glint") animation
 *
 * @param crackStyle The selected glass fracture pattern
 * @param glintEnabled Whether to animate the light glint across the fractured glass
 * @param extraCracks Additional tap-induced crack geometries
 * @param onScreenTap Callback when user taps the screen to shatter further
 * @param modifier Composable modifier
 */
@Composable
fun CrackCanvas(
    crackStyle: CrackStyle,
    glintEnabled: Boolean = true,
    extraCracks: List<CrackGeometry> = emptyList(),
    onScreenTap: ((Offset) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glass_glint")
    val glintProgress by infiniteTransition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glint_sweep"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("crack_canvas")
            .then(
                if (onScreenTap != null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures { offset ->
                            onScreenTap(offset)
                        }
                    }
                } else Modifier
            )
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        if (canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

        // Generate or fetch base crack geometry for this aspect ratio
        val baseGeometry = CrackVectorPaths.generate(
            style = crackStyle,
            width = canvasWidth,
            height = canvasHeight,
            seed = crackStyle.hashCode().toLong()
        )

        val allGeometries = listOf(baseGeometry) + extraCracks

        // 1. Pass: Dark physical depth shadow underneath fracture fault lines
        allGeometries.forEach { geom ->
            drawFractureShadowPass(geom)
        }

        // 2. Pass: Pulverized impact core shards (frosted glass appearance)
        allGeometries.forEach { geom ->
            drawImpactShards(geom)
        }

        // 3. Pass: Primary crystalline fracture lines (sharp white & cyan tint)
        allGeometries.forEach { geom ->
            drawPrimaryFractureLines(geom)
        }

        // 4. Pass: Delicate secondary hairline cracks
        allGeometries.forEach { geom ->
            drawSecondaryHairlines(geom)
        }

        // 5. Pass: Subtle glass glint sweep animation (runs directly on GPU compositor)
        if (glintEnabled) {
            drawGlintSweep(glintProgress)
        }
    }
}

/**
 * Renders shadow lines slightly offset to simulate glass thickness and internal total refraction.
 */
private fun DrawScope.drawFractureShadowPass(geom: CrackGeometry) {
    val shadowOffset = Offset(1.8f, 2.2f)
    val shadowColor = Color(0x66000000)
    val shadowStroke = Stroke(
        width = 3.2.dp.toPx(),
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )

    geom.primaryPaths.forEach { path ->
        // Draw with translate offset
        drawContext.canvas.save()
        drawContext.canvas.translate(shadowOffset.x, shadowOffset.y)
        drawPath(path, color = shadowColor, style = shadowStroke)
        drawContext.canvas.restore()
    }
}

/**
 * Renders frosted glass impact shards with translucent fills and highlights.
 */
private fun DrawScope.drawImpactShards(geom: CrackGeometry) {
    val shardFillColor = Color(0x35FFFFFF)
    val shardEdgeColor = Color(0xB0FFFFFF)
    val shardStroke = Stroke(width = 1.0.dp.toPx())

    geom.coreShards.forEach { shard ->
        drawPath(shard, color = shardFillColor, style = Fill)
        drawPath(shard, color = shardEdgeColor, style = shardStroke)
    }
}

/**
 * Renders the high-contrast brilliant white & icy blue fracture fault lines.
 */
private fun DrawScope.drawPrimaryFractureLines(geom: CrackGeometry) {
    val rimStroke = Stroke(
        width = 2.4.dp.toPx(),
        cap = StrokeCap.Round,
        join = StrokeJoin.Miter
    )
    val coreStroke = Stroke(
        width = 1.2.dp.toPx(),
        cap = StrokeCap.Round,
        join = StrokeJoin.Miter
    )

    val rimColor = Color(0xCCBAE6FD) // Light sky blue glass refraction
    val coreColor = Color(0xFFFFFFFF) // Brilliant direct reflection

    geom.primaryPaths.forEach { path ->
        drawPath(path, color = rimColor, style = rimStroke)
        drawPath(path, color = coreColor, style = coreStroke)
    }
}

/**
 * Renders delicate hairline fissures.
 */
private fun DrawScope.drawSecondaryHairlines(geom: CrackGeometry) {
    val hairlineStroke = Stroke(
        width = 0.8.dp.toPx(),
        cap = StrokeCap.Round,
        join = StrokeJoin.Miter
    )
    val hairColor = Color(0xA0FFFFFF)

    geom.secondaryPaths.forEach { path ->
        drawPath(path, color = hairColor, style = hairlineStroke)
    }
}

/**
 * Draws a subtle diagonal light sweep across the screen simulating ambient light catching glass facets.
 */
private fun DrawScope.drawGlintSweep(progress: Float) {
    val sweepCenter = Offset(size.width * progress, size.height * progress)
    val sweepWidth = size.width * 0.45f

    val glintBrush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            Color(0x08FFFFFF),
            Color(0x30FFFFFF),
            Color(0x08FFFFFF),
            Color.Transparent
        ),
        start = Offset(sweepCenter.x - sweepWidth, sweepCenter.y - sweepWidth),
        end = Offset(sweepCenter.x + sweepWidth, sweepCenter.y + sweepWidth)
    )

    drawRect(brush = glintBrush, blendMode = BlendMode.Screen)
}
