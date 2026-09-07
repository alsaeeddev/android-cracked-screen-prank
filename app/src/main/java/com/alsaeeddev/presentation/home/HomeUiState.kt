package com.alsaeeddev.presentation.home

import com.alsaeeddev.domain.model.PrankSettings
import com.alsaeeddev.domain.model.TriggerType

/**
 * UI State for the Home / Launcher Dashboard screen.
 */
data class HomeUiState(
    val settings: PrankSettings = PrankSettings(),
    val isShakeDetectionArmed: Boolean = true,
    val countdownRemainingSeconds: Int? = null,
    val isCountdownActive: Boolean = false,
    val isPrankActive: Boolean = false,
    val activeTriggerType: TriggerType? = null,
    val isSystemPrankModeActive: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val showPermissionRationale: Boolean = false
)
