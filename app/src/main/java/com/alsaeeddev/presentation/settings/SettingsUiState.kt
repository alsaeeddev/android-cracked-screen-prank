package com.alsaeeddev.presentation.settings

import com.alsaeeddev.domain.model.PrankSettings

/**
 * UI State for the Settings Screen.
 */
data class SettingsUiState(
    val isLoading: Boolean = false,
    val settings: PrankSettings = PrankSettings(),
    val isTestingSound: Boolean = false,
    val isPrankModeActive: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val showPermissionRationale: Boolean = false
)
