package com.alsaeeddev.domain.repository

import com.alsaeeddev.domain.model.CrackStyle
import com.alsaeeddev.domain.model.PrankSettings
import com.alsaeeddev.domain.model.ShakeSensitivity
import com.alsaeeddev.domain.model.SoundVariant
import kotlinx.coroutines.flow.Flow

/**
 * Contract for persisting and observing prank configuration preferences.
 */
interface SettingsRepository {
    /**
     * Hot observable flow of the current prank configuration state.
     */
    val settingsFlow: Flow<PrankSettings>

    /**
     * Updates the active crack style.
     */
    suspend fun updateCrackStyle(style: CrackStyle)

    /**
     * Toggles sound playback on/off.
     */
    suspend fun updateSoundEnabled(enabled: Boolean)

    /**
     * Sets sound volume (0.0 to 1.0).
     */
    suspend fun updateSoundVolume(volume: Float)

    /**
     * Sets sound variant preset.
     */
    suspend fun updateSoundVariant(variant: SoundVariant)

    /**
     * Toggles haptic feedback on/off.
     */
    suspend fun updateVibrationEnabled(enabled: Boolean)

    /**
     * Sets auto-dismiss timer duration in seconds (0 for manual exit only).
     */
    suspend fun updateAutoDismissSeconds(seconds: Int)

    /**
     * Sets shake sensor sensitivity preset.
     */
    suspend fun updateShakeSensitivity(sensitivity: ShakeSensitivity)

    /**
     * Sets trigger delay in seconds.
     */
    suspend fun updateDelaySeconds(seconds: Int)

    /**
     * Marks the first-launch exit tutorial as seen.
     */
    suspend fun markExitTutorialSeen()

    /**
     * Toggles glass glint animation effect.
     */
    suspend fun updateGlintAnimationEnabled(enabled: Boolean)

    /**
     * Toggles tap-to-add-more-cracks behavior during the active prank.
     */
    suspend fun updateTapToCrackMoreEnabled(enabled: Boolean)

    /**
     * Updates auto-expire duration for background prank overlay mode in hours.
     */
    suspend fun updateOverlayAutoExpireHours(hours: Int)

    /**
     * Toggles whether background prank mode auto-expires.
     */
    suspend fun updateOverlayAutoExpireEnabled(enabled: Boolean)

    /**
     * Resets all settings to factory defaults.
     */
    suspend fun resetToDefaults()
}
