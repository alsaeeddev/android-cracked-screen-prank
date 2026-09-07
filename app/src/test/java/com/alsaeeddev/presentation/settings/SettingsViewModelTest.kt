package com.alsaeeddev.presentation.settings

import app.cash.turbine.test
import com.alsaeeddev.domain.model.CrackStyle
import com.alsaeeddev.domain.model.PrankSettings
import com.alsaeeddev.domain.usecase.StartPrankModeUseCase
import com.alsaeeddev.domain.usecase.StopPrankModeUseCase
import com.alsaeeddev.fakes.FakeOverlayPermissionRepository
import com.alsaeeddev.fakes.FakePrankModeRepository
import com.alsaeeddev.fakes.FakeSettingsRepository
import com.alsaeeddev.fakes.FakeSoundPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var soundEffectPlayer: FakeSoundPlayer
    private lateinit var overlayPermissionRepository: FakeOverlayPermissionRepository
    private lateinit var prankModeRepository: FakePrankModeRepository
    private lateinit var startPrankModeUseCase: StartPrankModeUseCase
    private lateinit var stopPrankModeUseCase: StopPrankModeUseCase

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = FakeSettingsRepository()
        soundEffectPlayer = FakeSoundPlayer()
        overlayPermissionRepository = FakeOverlayPermissionRepository(hasPermission = true)
        prankModeRepository = FakePrankModeRepository(initialActive = false)
        startPrankModeUseCase = StartPrankModeUseCase(prankModeRepository, overlayPermissionRepository)
        stopPrankModeUseCase = StopPrankModeUseCase(prankModeRepository)

        viewModel = SettingsViewModel(
            settingsRepository = settingsRepository,
            soundEffectPlayer = soundEffectPlayer,
            overlayPermissionRepository = overlayPermissionRepository,
            prankModeRepository = prankModeRepository,
            startPrankModeUseCase = startPrankModeUseCase,
            stopPrankModeUseCase = stopPrankModeUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState observes repository settingsFlow`() = runTest(testDispatcher) {
        val initialSettings = PrankSettings(
            crackStyle = CrackStyle.BULLET_HOLE,
            soundEnabled = true,
            soundVolume = 0.75f
        )
        settingsRepository.state.value = initialSettings
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            var state = awaitItem()
            if (state.isLoading) {
                state = awaitItem()
            }
            assertEquals(CrackStyle.BULLET_HOLE, state.settings.crackStyle)
            assertEquals(0.75f, state.settings.soundVolume)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `togglePrankMode starts prank mode when permission granted`() = runTest(testDispatcher) {
        overlayPermissionRepository.hasPermission = true
        viewModel.togglePrankMode()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, prankModeRepository.startPrankModeCalls)
        assertTrue(prankModeRepository.isPrankModeActive.value)
    }

    @Test
    fun `togglePrankMode shows permission rationale when permission not granted`() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        overlayPermissionRepository.hasPermission = false
        viewModel.checkOverlayPermission()
        viewModel.togglePrankMode()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, prankModeRepository.startPrankModeCalls)
        assertTrue(viewModel.uiState.value.showPermissionRationale)

        viewModel.onConfirmPermissionRationale()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, overlayPermissionRepository.requestPermissionCalls)
        assertFalse(viewModel.uiState.value.showPermissionRationale)
    }

    @Test
    fun `togglePrankMode stops prank mode when already active`() = runTest(testDispatcher) {
        prankModeRepository.startPrankMode()
        viewModel.togglePrankMode()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, prankModeRepository.stopPrankModeCalls)
        assertFalse(prankModeRepository.isPrankModeActive.value)
    }

    @Test
    fun `setCrackStyle delegates directly to repository`() = runTest(testDispatcher) {
        viewModel.setCrackStyle(CrackStyle.HORIZONTAL_FRACTURE)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(CrackStyle.HORIZONTAL_FRACTURE), settingsRepository.updateCrackStyleCalls)
    }

    @Test
    fun `setSoundEnabled delegates to repository`() = runTest(testDispatcher) {
        viewModel.setSoundEnabled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(false), settingsRepository.updateSoundEnabledCalls)
    }

    @Test
    fun `resetToDefaults triggers repository reset`() = runTest(testDispatcher) {
        viewModel.resetToDefaults()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, settingsRepository.resetToDefaultsCalls)
    }
}
