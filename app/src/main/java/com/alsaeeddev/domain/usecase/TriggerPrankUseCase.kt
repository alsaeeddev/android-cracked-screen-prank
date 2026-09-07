package com.alsaeeddev.domain.usecase

import com.alsaeeddev.domain.audio.SoundPlayer
import com.alsaeeddev.domain.model.PrankSettings
import com.alsaeeddev.domain.model.TriggerType
import com.alsaeeddev.domain.repository.AdsRepository
import com.alsaeeddev.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Result data payload when a prank is activated.
 */
data class PrankActivationResult(
    val settings: PrankSettings,
    val triggerType: TriggerType,
    val timestampMs: Long = System.currentTimeMillis()
)

/**
 * Executes the complete prank triggering sequence:
 * 1. Fetches current user preferences
 * 2. Emits physical sensory feedback (SoundPool glass break & haptic shockwave)
 * 3. Preloads or coordinates advertisement hook
 * 4. Yields the active configuration for the rendering overlay
 */
class TriggerPrankUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val soundEffectPlayer: SoundPlayer,
    private val adsRepository: AdsRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    suspend operator fun invoke(triggerType: TriggerType): PrankActivationResult = withContext(defaultDispatcher) {
        val settings = settingsRepository.settingsFlow.first()

        // 1. Audio feedback
        if (settings.soundEnabled) {
            soundEffectPlayer.playGlassShatter(
                variant = settings.soundVariant,
                volume = settings.soundVolume
            )
        }

        // 2. Tactile haptic feedback
        if (settings.vibrationEnabled) {
            soundEffectPlayer.playShatterHaptic()
        }

        // 3. Ad hook (non-blocking)
        adsRepository.preloadNextAd()

        PrankActivationResult(
            settings = settings,
            triggerType = triggerType
        )
    }
}
