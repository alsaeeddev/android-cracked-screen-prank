package com.alsaeeddev.fakes

import android.app.Activity
import com.alsaeeddev.domain.audio.SoundPlayer
import com.alsaeeddev.domain.model.CrackStyle
import com.alsaeeddev.domain.model.PrankSettings
import com.alsaeeddev.domain.model.ShakeSensitivity
import com.alsaeeddev.domain.model.SoundVariant
import com.alsaeeddev.domain.repository.AdsRepository
import com.alsaeeddev.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSettingsRepository(
    initialSettings: PrankSettings = PrankSettings()
) : SettingsRepository {

    val state = MutableStateFlow(initialSettings)
    override val settingsFlow: Flow<PrankSettings> = state.asStateFlow()

    var updateCrackStyleCalls = mutableListOf<CrackStyle>()
    var updateSoundEnabledCalls = mutableListOf<Boolean>()
    var updateSoundVolumeCalls = mutableListOf<Float>()
    var updateSoundVariantCalls = mutableListOf<SoundVariant>()
    var updateVibrationEnabledCalls = mutableListOf<Boolean>()
    var updateAutoDismissSecondsCalls = mutableListOf<Int>()
    var updateShakeSensitivityCalls = mutableListOf<ShakeSensitivity>()
    var updateDelaySecondsCalls = mutableListOf<Int>()
    var markExitTutorialSeenCalls = 0
    var resetToDefaultsCalls = 0

    override suspend fun updateCrackStyle(style: CrackStyle) {
        updateCrackStyleCalls.add(style)
        state.value = state.value.copy(crackStyle = style)
    }

    override suspend fun updateSoundEnabled(enabled: Boolean) {
        updateSoundEnabledCalls.add(enabled)
        state.value = state.value.copy(soundEnabled = enabled)
    }

    override suspend fun updateSoundVolume(volume: Float) {
        updateSoundVolumeCalls.add(volume)
        state.value = state.value.copy(soundVolume = volume)
    }

    override suspend fun updateSoundVariant(variant: SoundVariant) {
        updateSoundVariantCalls.add(variant)
        state.value = state.value.copy(soundVariant = variant)
    }

    override suspend fun updateVibrationEnabled(enabled: Boolean) {
        updateVibrationEnabledCalls.add(enabled)
        state.value = state.value.copy(vibrationEnabled = enabled)
    }

    override suspend fun updateAutoDismissSeconds(seconds: Int) {
        updateAutoDismissSecondsCalls.add(seconds)
        state.value = state.value.copy(autoDismissSeconds = seconds)
    }

    override suspend fun updateShakeSensitivity(sensitivity: ShakeSensitivity) {
        updateShakeSensitivityCalls.add(sensitivity)
        state.value = state.value.copy(shakeSensitivity = sensitivity)
    }

    override suspend fun updateDelaySeconds(seconds: Int) {
        updateDelaySecondsCalls.add(seconds)
        state.value = state.value.copy(delaySeconds = seconds)
    }

    override suspend fun markExitTutorialSeen() {
        markExitTutorialSeenCalls++
        state.value = state.value.copy(hasSeenExitTutorial = true)
    }

    override suspend fun updateGlintAnimationEnabled(enabled: Boolean) {
        state.value = state.value.copy(glintAnimationEnabled = enabled)
    }

    override suspend fun updateTapToCrackMoreEnabled(enabled: Boolean) {
        state.value = state.value.copy(tapToCrackMoreEnabled = enabled)
    }

    override suspend fun updateOverlayAutoExpireHours(hours: Int) {
        state.value = state.value.copy(overlayAutoExpireHours = hours)
    }

    override suspend fun updateOverlayAutoExpireEnabled(enabled: Boolean) {
        state.value = state.value.copy(overlayAutoExpireEnabled = enabled)
    }

    override suspend fun resetToDefaults() {
        resetToDefaultsCalls++
        state.value = PrankSettings()
    }
}

class FakeOverlayPermissionRepository(
    var hasPermission: Boolean = true
) : com.alsaeeddev.domain.repository.OverlayPermissionRepository {
    var requestPermissionCalls = 0

    override fun hasOverlayPermission(): Boolean = hasPermission

    override fun requestOverlayPermission() {
        requestPermissionCalls++
    }
}

class FakePrankModeRepository(
    initialActive: Boolean = false
) : com.alsaeeddev.domain.repository.PrankModeRepository {
    val activeState = MutableStateFlow(initialActive)
    val overlayActiveState = MutableStateFlow(false)

    override val isPrankModeActive = activeState.asStateFlow()
    override val isOverlayActive = overlayActiveState.asStateFlow()

    var startPrankModeCalls = 0
    var stopPrankModeCalls = 0
    var dismissOverlayCalls = 0

    override fun startPrankMode() {
        startPrankModeCalls++
        activeState.value = true
    }

    override fun stopPrankMode() {
        stopPrankModeCalls++
        activeState.value = false
        overlayActiveState.value = false
    }

    override fun dismissOverlay() {
        dismissOverlayCalls++
        overlayActiveState.value = false
    }

    override fun setOverlayActive(active: Boolean) {
        overlayActiveState.value = active
    }

    override fun setPrankModeActive(active: Boolean) {
        activeState.value = active
    }
}

class FakeSoundPlayer : SoundPlayer {
    var playedSounds = mutableListOf<Pair<SoundVariant, Float>>()
    var shatterHapticTriggerCount = 0
    var secondaryCrackHapticTriggerCount = 0
    var releaseCalled = false

    override fun playGlassShatter(variant: SoundVariant, volume: Float) {
        playedSounds.add(variant to volume)
    }

    override fun playShatterHaptic() {
        shatterHapticTriggerCount++
    }

    override fun playSecondaryCrackHaptic() {
        secondaryCrackHapticTriggerCount++
    }

    override fun release() {
        releaseCalled = true
    }
}

class FakeAdsRepository : AdsRepository {
    var preloadCalls = 0
    var showCalls = 0

    override fun isAdReady(): Boolean = true

    override fun showInterstitialAd(activity: Activity?, onDismissed: () -> Unit) {
        showCalls++
        onDismissed()
    }

    override fun preloadNextAd() {
        preloadCalls++
    }
}
