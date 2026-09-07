package com.alsaeeddev.data.sensor

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class BackgroundShakeFilterTest {

    private lateinit var filter: BackgroundShakeFilter

    @Before
    fun setUp() {
        filter = BackgroundShakeFilter(
            thresholdG = 2.5f,
            sustainedDurationMs = 500L,
            cooldownMs = 2000L,
            maxGapMs = 400L
        )
    }

    @Test
    fun `isolated momentary shake reading does not trigger sustained shake`() {
        val result = filter.processSample(timestampMs = 1000L, gForce = 3.2f)
        assertNull("Single high-G reading must not trigger sustained shake", result)
    }

    @Test
    fun `sustained high-G acceleration over 500ms triggers successfully`() {
        assertNull(filter.processSample(timestampMs = 1000L, gForce = 3.0f))
        assertNull(filter.processSample(timestampMs = 1250L, gForce = 3.5f))
        val event = filter.processSample(timestampMs = 1550L, gForce = 3.2f) // 550ms after first high-G reading
        assertNotNull("Sustained high-G shaking over 500ms should trigger shake event", event)
    }

    @Test
    fun `long gap between high-G readings resets the sustained window`() {
        assertNull(filter.processSample(timestampMs = 1000L, gForce = 3.0f))
        // 500ms gap > maxGapMs (400ms) with low G reading
        assertNull(filter.processSample(timestampMs = 1500L, gForce = 1.0f))
        // High G reading after reset starts a new window
        assertNull(filter.processSample(timestampMs = 1600L, gForce = 3.0f))
        // Only 200ms since new window start -> should not trigger yet
        val event = filter.processSample(timestampMs = 1800L, gForce = 3.0f)
        assertNull(event)
    }

    @Test
    fun `cooldown prevents immediate second trigger`() {
        filter.processSample(timestampMs = 1000L, gForce = 3.0f)
        filter.processSample(timestampMs = 1250L, gForce = 3.0f)
        val event1 = filter.processSample(timestampMs = 1550L, gForce = 3.0f)
        assertNotNull(event1)

        // Reading within cooldown period (< 2000ms from 1550ms)
        val event2 = filter.processSample(timestampMs = 2000L, gForce = 4.0f)
        assertNull("Cooldown must prevent immediate second trigger", event2)
    }

    @Test
    fun `reset clears tracking state`() {
        filter.processSample(timestampMs = 1000L, gForce = 3.0f)
        filter.reset()
        val event = filter.processSample(timestampMs = 1550L, gForce = 3.0f)
        assertNull("After reset, previous window is discarded", event)
    }
}
