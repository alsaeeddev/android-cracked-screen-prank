package com.alsaeeddev.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alsaeeddev.data.audio.SoundEffectPlayer
import com.alsaeeddev.domain.model.CrackStyle
import com.alsaeeddev.domain.model.TriggerType
import com.alsaeeddev.domain.repository.OverlayPermissionRepository
import com.alsaeeddev.domain.repository.PrankModeRepository
import com.alsaeeddev.domain.repository.SettingsRepository
import com.alsaeeddev.domain.usecase.ObserveShakeEventsUseCase
import com.alsaeeddev.domain.usecase.StartPrankModeUseCase
import com.alsaeeddev.domain.usecase.StopPrankModeUseCase
import com.alsaeeddev.domain.usecase.TriggerPrankUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Primary ViewModel orchestrating prank triggers (instant tap, shake sensor, timed countdown)
 * and system-wide overlay state.
 */
class HomeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val observeShakeEventsUseCase: ObserveShakeEventsUseCase,
    private val triggerPrankUseCase: TriggerPrankUseCase,
    private val soundEffectPlayer: SoundEffectPlayer,
    private val prankModeRepository: PrankModeRepository,
    private val startPrankModeUseCase: StartPrankModeUseCase,
    private val stopPrankModeUseCase: StopPrankModeUseCase,
    private val overlayPermissionRepository: OverlayPermissionRepository
) : ViewModel() {

    private val _isShakeDetectionArmed = MutableStateFlow(true)
    private val _countdownRemainingSeconds = MutableStateFlow<Int?>(null)
    private val _isCountdownActive = MutableStateFlow(false)
    private val _isPrankActive = MutableStateFlow(false)
    private val _activeTriggerType = MutableStateFlow<TriggerType?>(null)
    private val _hasOverlayPermission = MutableStateFlow(overlayPermissionRepository.hasOverlayPermission())
    private val _showPermissionRationale = MutableStateFlow(false)

    private var countdownJob: Job? = null
    private var shakeObservationJob: Job? = null

    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.settingsFlow,
        _isShakeDetectionArmed,
        _countdownRemainingSeconds,
        _isCountdownActive,
        _isPrankActive,
        prankModeRepository.isPrankModeActive,
        _hasOverlayPermission,
        _showPermissionRationale
    ) { args: Array<Any?> ->
        val settings = args[0] as com.alsaeeddev.domain.model.PrankSettings
        val shakeArmed = args[1] as Boolean
        val countdownSec = args[2] as? Int
        val isCountdown = args[3] as Boolean
        val isPrank = args[4] as Boolean
        val isSystemActive = args[5] as Boolean
        val hasPermission = args[6] as Boolean
        val showRationale = args[7] as Boolean

        HomeUiState(
            settings = settings,
            isShakeDetectionArmed = shakeArmed,
            countdownRemainingSeconds = countdownSec,
            isCountdownActive = isCountdown,
            isPrankActive = isPrank,
            activeTriggerType = _activeTriggerType.value,
            isSystemPrankModeActive = isSystemActive,
            hasOverlayPermission = hasPermission,
            showPermissionRationale = showRationale
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun checkOverlayPermission() {
        _hasOverlayPermission.value = overlayPermissionRepository.hasOverlayPermission()
    }

    fun toggleSystemPrankMode() {
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

    init {
        startShakeObservation()
    }

    /**
     * Starts listening for accelerometer shake gestures when armed and prank is not currently covering screen.
     */
    fun startShakeObservation() {
        shakeObservationJob?.cancel()
        shakeObservationJob = observeShakeEventsUseCase()
            .onEach {
                if (_isShakeDetectionArmed.value && !_isPrankActive.value && !_isCountdownActive.value) {
                    launchPrank(TriggerType.SHAKE)
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Pauses or stops listening to shake events (e.g., when screen is not visible).
     */
    fun stopShakeObservation() {
        shakeObservationJob?.cancel()
        shakeObservationJob = null
    }

    fun toggleShakeArmed(armed: Boolean) {
        _isShakeDetectionArmed.value = armed
        if (armed && shakeObservationJob == null) {
            startShakeObservation()
        }
    }

    fun setCrackStyle(style: CrackStyle) {
        viewModelScope.launch {
            settingsRepository.updateCrackStyle(style)
        }
    }

    /**
     * Instantly triggers full-screen prank mode.
     */
    fun launchPrank(triggerType: TriggerType = TriggerType.TAP) {
        cancelCountdown()
        _activeTriggerType.value = triggerType
        _isPrankActive.value = true

        viewModelScope.launch {
            triggerPrankUseCase(triggerType)
        }
    }

    /**
     * Schedules prank activation after a specified countdown interval.
     */
    fun startDelayedCountdown(seconds: Int) {
        cancelCountdown()
        _isCountdownActive.value = true
        _countdownRemainingSeconds.value = seconds

        countdownJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                _countdownRemainingSeconds.value = remaining
                delay(1000L)
                remaining--
            }
            _isCountdownActive.value = false
            _countdownRemainingSeconds.value = null
            launchPrank(TriggerType.DELAYED_TIMER)
        }
    }

    fun cancelCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        _isCountdownActive.value = false
        _countdownRemainingSeconds.value = null
    }

    /**
     * Safely closes the active prank overlay.
     */
    fun dismissPrank() {
        _isPrankActive.value = false
        _activeTriggerType.value = null
    }

    /**
     * Marks exit tutorial as seen and saves to repository.
     */
    fun dismissExitTutorial() {
        viewModelScope.launch {
            settingsRepository.markExitTutorialSeen()
        }
    }
}
