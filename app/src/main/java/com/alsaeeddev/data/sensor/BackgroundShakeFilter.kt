package com.alsaeeddev.data.sensor

import com.alsaeeddev.domain.model.ShakeEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

/**
 * Representation of an accelerometer reading with Earth-relative G force.
 */
data class AccelerometerSample(
    val timestampMs: Long,
    val gForce: Float,
    val timestampNs: Long = timestampMs * 1_000_000L
)

/**
 * Robust filter for detecting sustained physical shaking motions while rejecting momentary bumps or steps.
 *
 * Implements a sliding sustained-shake window:
 * - High G-force readings must be sustained continuously over [sustainedDurationMs] (e.g. 500ms).
 * - Momentary pocket drops, table taps, or single-step jars are ignored because their duration is < 200ms.
 * - Enforces a [cooldownMs] period after triggering to prevent rapid repeated fires.
 *
 * @property thresholdG Acceleration magnitude threshold in G (Earth gravities).
 * @property sustainedDurationMs Minimum duration high-G acceleration must continue.
 * @property cooldownMs Minimum time after a trigger before another can be emitted.
 * @property maxGapMs Maximum allowable duration between high-G readings within an active shake gesture.
 */
class BackgroundShakeFilter(
    val thresholdG: Float = 2.5f,
    val sustainedDurationMs: Long = 500L,
    val cooldownMs: Long = 2000L,
    val maxGapMs: Long = 400L
) {
    private var windowStartMs: Long = 0L
    private var lastSampleMs: Long = 0L
    private var lastTriggerMs: Long = 0L

    /**
     * Ingests a sensor sample and returns a [ShakeEvent] if the sustained shake criteria are met.
     */
    fun processSample(
        timestampMs: Long,
        gForce: Float,
        timestampNs: Long = timestampMs * 1_000_000L
    ): ShakeEvent? {
        if (lastTriggerMs > 0L && (timestampMs - lastTriggerMs < cooldownMs)) {
            return null
        }

        if (gForce >= thresholdG) {
            if (windowStartMs == 0L || (timestampMs - lastSampleMs > maxGapMs)) {
                // Begin a new candidate sustained window
                windowStartMs = timestampMs
            } else if (timestampMs - windowStartMs >= sustainedDurationMs) {
                // Sustained threshold satisfied!
                lastTriggerMs = timestampMs
                windowStartMs = 0L
                lastSampleMs = 0L
                return ShakeEvent(magnitudeG = gForce, timestampNs = timestampNs)
            }
            lastSampleMs = timestampMs
        } else {
            if (timestampMs - lastSampleMs > maxGapMs) {
                // Acceleration fell below threshold longer than maxGap; reset candidate window
                windowStartMs = 0L
            }
        }
        return null
    }

    /**
     * Resets internal tracking state.
     */
    fun reset() {
        windowStartMs = 0L
        lastSampleMs = 0L
        lastTriggerMs = 0L
    }
}

/**
 * Transforms a Flow of [AccelerometerSample] into a debounced Flow of [ShakeEvent]s
 * using the sustained shake filter.
 */
fun Flow<AccelerometerSample>.filterSustainedShakes(filter: BackgroundShakeFilter): Flow<ShakeEvent> =
    transform { sample ->
        val event = filter.processSample(sample.timestampMs, sample.gForce, sample.timestampNs)
        if (event != null) {
            emit(event)
        }
    }
