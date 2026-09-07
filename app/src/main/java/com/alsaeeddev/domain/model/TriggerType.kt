package com.alsaeeddev.domain.model

/**
 * The user-facing mechanism that initiated the prank activation.
 */
enum class TriggerType {
    TAP,
    SHAKE,
    DELAYED_TIMER,
    PREVIEW
}
