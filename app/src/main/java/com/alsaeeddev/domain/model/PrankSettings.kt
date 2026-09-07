package com.alsaeeddev.domain.model

/**
 * Shake sensitivity presets for accelerometer detection.
 *
 * @property thresholdG Acceleration magnitude threshold in G (9.8 m/s²)
 * @property label Human-readable description
 */
enum class ShakeSensitivity(val thresholdG: Float, val label: String) {
    HIGH(1.7f, "High (Gentle shake)"),
    MEDIUM(2.5f, "Medium (Normal shake)"),
    LOW(3.6f, "Low (Firm shake)");

    companion object {
        fun fromThreshold(g: Float): ShakeSensitivity = when {
            g <= 2.0f -> HIGH
            g <= 3.0f -> MEDIUM
            else -> LOW
        }
    }
}

/**
 * Sound effect variants for realistic glass shattering.
 */
enum class SoundVariant(val id: String, val displayName: String) {
    SHATTER("shatter", "Heavy Glass Shatter"),
    CRISP_SNAP("crisp_snap", "Crisp Glass Snap"),
    BULLET_IMPACT("bullet", "High-Velocity Impact"),
    CRYSTAL_BREAK("crystal", "Crystal Fragment Break");

    companion object {
        fun fromId(id: String): SoundVariant =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: SHATTER
    }
}

/**
 * Domain model representing all persistent prank configuration options.
 */
data class PrankSettings(
    val crackStyle: CrackStyle = CrackStyle.SPIDERWEB,
    val soundEnabled: Boolean = true,
    val soundVolume: Float = 1.0f,
    val soundVariant: SoundVariant = SoundVariant.SHATTER,
    val vibrationEnabled: Boolean = true,
    val autoDismissSeconds: Int = 0, // 0 = no auto-dismiss (manual exit gesture only)
    val shakeSensitivity: ShakeSensitivity = ShakeSensitivity.MEDIUM,
    val delaySeconds: Int = 0, // 0 = instant trigger
    val hasSeenExitTutorial: Boolean = false,
    val glintAnimationEnabled: Boolean = true,
    val tapToCrackMoreEnabled: Boolean = true,
    val overlayAutoExpireHours: Int = 2,
    val overlayAutoExpireEnabled: Boolean = true
)
