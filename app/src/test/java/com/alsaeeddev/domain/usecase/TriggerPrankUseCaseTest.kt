package com.alsaeeddev.domain.usecase

import com.alsaeeddev.domain.model.CrackStyle
import com.alsaeeddev.domain.model.PrankSettings
import com.alsaeeddev.domain.model.SoundVariant
import com.alsaeeddev.domain.model.TriggerType
import com.alsaeeddev.fakes.FakeAdsRepository
import com.alsaeeddev.fakes.FakeSettingsRepository
import com.alsaeeddev.fakes.FakeSoundPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TriggerPrankUseCaseTest {

    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var soundEffectPlayer: FakeSoundPlayer
    private lateinit var adsRepository: FakeAdsRepository

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var useCase: TriggerPrankUseCase

    @Before
    fun setUp() {
        settingsRepository = FakeSettingsRepository()
        soundEffectPlayer = FakeSoundPlayer()
        adsRepository = FakeAdsRepository()

        useCase = TriggerPrankUseCase(
            settingsRepository = settingsRepository,
            soundEffectPlayer = soundEffectPlayer,
            adsRepository = adsRepository,
            defaultDispatcher = testDispatcher
        )
    }

    @Test
    fun `invoke triggers audio and haptics when enabled in settings`() = runTest(testDispatcher) {
        val testSettings = PrankSettings(
            crackStyle = CrackStyle.CORNER_SHATTER,
            soundEnabled = true,
            soundVariant = SoundVariant.CRISP_SNAP,
            soundVolume = 0.8f,
            vibrationEnabled = true
        )
        settingsRepository.state.value = testSettings

        val result = useCase(TriggerType.SHAKE)

        // Verify audio was played with correct variant and volume
        assertEquals(1, soundEffectPlayer.playedSounds.size)
        assertEquals(SoundVariant.CRISP_SNAP, soundEffectPlayer.playedSounds[0].first)
        assertEquals(0.8f, soundEffectPlayer.playedSounds[0].second, 0.01f)

        // Verify haptic feedback was triggered
        assertEquals(1, soundEffectPlayer.shatterHapticTriggerCount)

        // Verify ad preload hook was executed
        assertEquals(1, adsRepository.preloadCalls)

        // Verify result payload
        assertEquals(TriggerType.SHAKE, result.triggerType)
        assertEquals(CrackStyle.CORNER_SHATTER, result.settings.crackStyle)
    }

    @Test
    fun `invoke skips audio and haptics when disabled in settings`() = runTest(testDispatcher) {
        val testSettings = PrankSettings(
            soundEnabled = false,
            vibrationEnabled = false
        )
        settingsRepository.state.value = testSettings

        val result = useCase(TriggerType.TAP)

        // Verify sound was NOT played
        assertTrue(soundEffectPlayer.playedSounds.isEmpty())

        // Verify haptic was NOT triggered
        assertEquals(0, soundEffectPlayer.shatterHapticTriggerCount)

        assertEquals(TriggerType.TAP, result.triggerType)
    }
}
