package com.alsaeeddev.domain.usecase

import com.alsaeeddev.fakes.FakeOverlayPermissionRepository
import com.alsaeeddev.fakes.FakePrankModeRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PrankModeUseCasesTest {

    private lateinit var repository: FakePrankModeRepository
    private lateinit var overlayPermissionRepository: FakeOverlayPermissionRepository
    private lateinit var startUseCase: StartPrankModeUseCase
    private lateinit var stopUseCase: StopPrankModeUseCase

    @Before
    fun setUp() {
        repository = FakePrankModeRepository(initialActive = false)
        overlayPermissionRepository = FakeOverlayPermissionRepository(hasPermission = true)
        startUseCase = StartPrankModeUseCase(repository, overlayPermissionRepository)
        stopUseCase = StopPrankModeUseCase(repository)
    }

    @Test
    fun `StartPrankModeUseCase succeeds when permission granted`() {
        val result = startUseCase()
        assertTrue(result)
        assertEquals(1, repository.startPrankModeCalls)
        assertTrue(repository.isPrankModeActive.value)
    }

    @Test
    fun `StartPrankModeUseCase fails when permission not granted`() {
        overlayPermissionRepository.hasPermission = false
        val result = startUseCase()
        assertFalse(result)
        assertEquals(0, repository.startPrankModeCalls)
        assertFalse(repository.isPrankModeActive.value)
    }

    @Test
    fun `StopPrankModeUseCase delegates to repository`() {
        repository.startPrankMode()
        stopUseCase()
        assertEquals(1, repository.stopPrankModeCalls)
        assertFalse(repository.isPrankModeActive.value)
    }
}
