package com.alsaeeddev.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alsaeeddev.domain.audio.SoundPlayer
import com.alsaeeddev.domain.model.CrackStyle
import com.alsaeeddev.domain.model.ShakeSensitivity
import com.alsaeeddev.domain.model.SoundVariant
import com.alsaeeddev.domain.repository.OverlayPermissionRepository
import com.alsaeeddev.domain.repository.PrankModeRepository
import com.alsaeeddev.domain.repository.SettingsRepository
import com.alsaeeddev.domain.usecase.StartPrankModeUseCase
import com.alsaeeddev.domain.usecase.StopPrankModeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing prank configuration, system overlay permissions, and background Prank Mode.
 */
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val soundEffectPlayer: SoundPlayer,
    private val overlayPermissionRepository: OverlayPermissionRepository,
    private val prankModeRepository: PrankModeRepository,
    private val startPrankModeUseCase: StartPrankModeUseCase,
    private val stopPrankModeUseCase: StopPrankModeUseCase
) : ViewModel() {

    private val _isTestingSound = MutableStateFlow(false)
    private val _hasOverlayPermission = MutableStateFlow(overlayPermissionRepository.hasOverlayPermission())
    private val _showPermissionRationale = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settingsFlow,
        _isTestingSound,
        prankModeRepository.isPrankModeActive,
        _hasOverlayPermission,
        _showPermissionRationale
    ) { settings, isTesting, isPrankActive, hasPermission, showRationale ->
        SettingsUiState(
            isLoading = false,
            settings = settings,
            isTestingSound = isTesting,
            isPrankModeActive = isPrankActive,
            hasOverlayPermission = hasPermission,
            showPermissionRationale = showRationale
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(isLoading = true)
    )

    fun checkOverlayPermission() {
        _hasOverlayPermission.value = overlayPermissionRepository.hasOverlayPermission()
    }

    fun togglePrankMode() {
        checkOverlayPermission()
        if (prankModeRepository.isPrankModeActive.value) {
            stopPrankModeUseCase()
        } else {
            if (overlayPermissionRepository.hasOverlayPermission()) {
                startPrankModeUseCase()
            } else {
                _showPermissionRationale.value = true
            }
        }
    }

    fun dismissPermissionRationale() {
        _showPermissionRationale.value = false
    }

    fun onConfirmPermissionRationale() {
        _showPermissionRationale.value = false
        overlayPermissionRepository.requestOverlayPermission()
    }

    fun setOverlayAutoExpireHours(hours: Int) {
        viewModelScope.launch {
            settingsRepository.updateOverlayAutoExpireHours(hours)
        }
    }

    fun setOverlayAutoExpireEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateOverlayAutoExpireEnabled(enabled)
        }
    }

    fun setCrackStyle(style: CrackStyle) {
        viewModelScope.launch {
            settingsRepository.updateCrackStyle(style)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSoundEnabled(enabled)
        }
    }

    fun setSoundVolume(volume: Float) {
        viewModelScope.launch {
            settingsRepository.updateSoundVolume(volume)
        }
    }

    fun setSoundVariant(variant: SoundVariant) {
        viewModelScope.launch {
            settingsRepository.updateSoundVariant(variant)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateVibrationEnabled(enabled)
        }
    }

    fun setAutoDismissSeconds(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.updateAutoDismissSeconds(seconds)
        }
    }

    fun setShakeSensitivity(sensitivity: ShakeSensitivity) {
        viewModelScope.launch {
            settingsRepository.updateShakeSensitivity(sensitivity)
        }
    }

    fun setDelaySeconds(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.updateDelaySeconds(seconds)
        }
    }

    fun setGlintAnimationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateGlintAnimationEnabled(enabled)
        }
    }

    fun setTapToCrackMoreEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateTapToCrackMoreEnabled(enabled)
        }
    }

    fun previewSound() {
        val currentSettings = uiState.value.settings
        soundEffectPlayer.playGlassShatter(
            variant = currentSettings.soundVariant,
            volume = currentSettings.soundVolume
        )
        if (currentSettings.vibrationEnabled) {
            soundEffectPlayer.playShatterHaptic()
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            settingsRepository.resetToDefaults()
        }
    }
}
