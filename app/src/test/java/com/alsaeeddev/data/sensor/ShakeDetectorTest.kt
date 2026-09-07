package com.alsaeeddev.data.sensor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying ShakeDetector threshold calculations and debounce windowing.
 */
class ShakeDetectorTest {

    @Test
    fun `calculateGForce returns 1G for stationary device at rest on table`() {
        // Standard Earth gravity pointing along Z-axis (face up on table)
        val gForce = ShakeDetector.calculateGForce(0f, 0f, 9.80665f)
        assertEquals(1.0f, gForce, 0.01f)
    }

    @Test
    fun `calculateGForce detects rapid shake acceleration exceeding 2_5G`() {
        // Strong shake acceleration along X and Y axes
        val gForce = ShakeDetector.calculateGForce(20.0f, 15.0f, 9.8f)
        assertTrue("Expected gForce >= 2.5G but got $gForce", gForce >= 2.5f)
    }

    @Test
    fun `isDebounced rejects second shake within debounce window`() {
        val lastShakeTime = 1000L
        val immediateNextShakeTime = 1300L // 300ms later
        val debounceInterval = 600L

        val isAllowed = ShakeDetector.isDebounced(
            lastShakeMs = lastShakeTime,
            currentMs = immediateNextShakeTime,
            debounceMs = debounceInterval
        )

        assertFalse("Expected shake to be debounced/suppressed", isAllowed)
    }

    @Test
    fun `isDebounced accepts shake after debounce interval has elapsed`() {
        val lastShakeTime = 1000L
        val subsequentShakeTime = 1750L // 750ms later
        val debounceInterval = 600L

        val isAllowed = ShakeDetector.isDebounced(
            lastShakeMs = lastShakeTime,
            currentMs = subsequentShakeTime,
            debounceMs = debounceInterval
        )

        assertTrue("Expected shake to be permitted after debounce window", isAllowed)
    }
}
