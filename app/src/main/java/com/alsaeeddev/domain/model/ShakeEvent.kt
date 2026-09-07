package com.alsaeeddev.domain.model

/**
 * Event emitted by the accelerometer sensor detector when a physical shake is detected.
 *
 * @property magnitudeG Peak acceleration force detected in G-force units
 * @property timestampNs System uptime nanoseconds when shake occurred
 */
data class ShakeEvent(
    val magnitudeG: Float,
    val timestampNs: Long = System.nanoTime()
)
