package com.alsaeeddev.domain.model

/**
 * Represents the visual style and geometry pattern of the glass crack.
 *
 * @property id Unique identifier for the style
 * @property displayName User-facing name of the crack variant
 * @property description Brief description of the fracture pattern
 * @property tag Descriptive tag for UI chip
 */
enum class CrackStyle(
    val id: String,
    val displayName: String,
    val description: String,
    val tag: String
) {
    SPIDERWEB(
        id = "spiderweb",
        displayName = "Spiderweb Center",
        description = "Radial web fracture with concentric stress rings",
        tag = "Classic"
    ),
    CORNER_SHATTER(
        id = "corner_shatter",
        displayName = "Corner Shatter",
        description = "Violent impact originating from corner with diagonal shear fissures",
        tag = "Severe"
    ),
    BULLET_HOLE(
        id = "bullet_hole",
        displayName = "Bullet Hole",
        description = "Dense pulverized impact core with radial shockwaves",
        tag = "Deep Impact"
    ),
    HORIZONTAL_FRACTURE(
        id = "horizontal_fracture",
        displayName = "Horizontal Fracture",
        description = "Cross-screen lateral shear stress with jagged vertical splinters",
        tag = "Stress Crack"
    ),
    MULTI_IMPACT(
        id = "multi_impact",
        displayName = "Multi-Point Shatter",
        description = "Multiple catastrophic impact epicenters with intersecting fractures",
        tag = "Total Wreck"
    );

    companion object {
        fun fromId(id: String): CrackStyle =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: SPIDERWEB
    }
}
