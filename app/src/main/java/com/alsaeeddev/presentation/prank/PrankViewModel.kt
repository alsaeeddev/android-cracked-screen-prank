package com.alsaeeddev.presentation.prank

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alsaeeddev.data.audio.SoundEffectPlayer
import com.alsaeeddev.domain.model.CrackStyle
import com.alsaeeddev.domain.model.TriggerType
import com.alsaeeddev.domain.repository.SettingsRepository
import com.alsaeeddev.domain.usecase.TriggerPrankUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the active full-screen prank overlay.
 */
data class PrankUiState(
    val crackStyle: CrackStyle = CrackStyle.SPIDERWEB,
    val glintEnabled: Boolean = true,
    val tapToCrackMore: Boolean = true,
    val extraCracks: List<CrackGeometry> = emptyList(),
    val showTutorial: Boolean = false,
    val autoDismissRemainingSeconds: Int? = null,
    val isDismissed: Boolean = false
)

/**
 * Manages prank overlay lifecycle, interactive touch fissures, and safe-exit timers.
 *
 * Threading note: All state mutations and sound playback occur off the composition thread.
 */
class PrankViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val soundEffectPlayer: SoundEffectPlayer,
    private val triggerPrankUseCase: TriggerPrankUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrankUiState())
    val uiState: StateFlow<PrankUiState> = _uiState.asStateFlow()

    private var autoDismissJob: Job? = null

    /**
     * Initializes the prank with active user preferences and triggers sensory feedback.
     */
    fun startPrank(triggerType: TriggerType = TriggerType.TAP) {
        viewModelScope.launch {
            val activation = triggerPrankUseCase(triggerType)
            val settings = activation.settings

            _uiState.update {
                it.copy(
                    crackStyle = settings.crackStyle,
                    glintEnabled = settings.glintAnimationEnabled,
                    tapToCrackMore = settings.tapToCrackMoreEnabled,
                    showTutorial = !settings.hasSeenExitTutorial,
                    isDismissed = false
                )
            }

            if (settings.autoDismissSeconds > 0) {
                startAutoDismissCountdown(settings.autoDismissSeconds)
            }
        }
    }

    /**
     * Adds an extra fractured fissure when the user taps on the screen.
     */
    fun onScreenTap(offset: Offset) {
        if (!_uiState.value.tapToCrackMore) return

        val newImpact = CrackVectorPaths.generateTouchImpact(
            touchX = offset.x,
            touchY = offset.y,
            seed = System.currentTimeMillis()
        )

        _uiState.update { current ->
            // Keep at most 8 extra interactive cracks to protect memory and compositor frame rate
            val updated = (current.extraCracks + newImpact).takeLast(8)
            current.copy(extraCracks = updated)
        }

        // Secondary sensory feedback
        soundEffectPlayer.playSecondaryCrackHaptic()
        soundEffectPlayer.playGlassShatter(volume = 0.6f)
    }

    /**
     * Dismisses the tutorial hint and saves the preference.
     */
    fun dismissTutorial() {
        _uiState.update { it.copy(showTutorial = false) }
        viewModelScope.launch {
            settingsRepository.markExitTutorialSeen()
        }
    }

    /**
     * Safely terminates the prank overlay.
     */
    fun exitPrank() {
        autoDismissJob?.cancel()
        _uiState.update { it.copy(isDismissed = true) }
    }

    private fun startAutoDismissCountdown(seconds: Int) {
        autoDismissJob?.cancel()
        autoDismissJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                _uiState.update { it.copy(autoDismissRemainingSeconds = remaining) }
                delay(1000L)
                remaining--
            }
            exitPrank()
        }
    }
}
