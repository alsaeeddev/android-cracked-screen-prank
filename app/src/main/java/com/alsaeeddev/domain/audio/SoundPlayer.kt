package com.alsaeeddev.domain.audio

import com.alsaeeddev.domain.model.SoundVariant

/**
 * Interface contract for glass fracture sound and haptic sensory feedback.
 */
interface SoundPlayer {
    fun playGlassShatter(variant: SoundVariant = SoundVariant.SHATTER, volume: Float = 1.0f)
    fun playShatterHaptic()
    fun playSecondaryCrackHaptic()
    fun release()
}
