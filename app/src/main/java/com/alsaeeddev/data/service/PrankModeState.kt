package com.alsaeeddev.data.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton-scoped state container synchronizing background prank armed status
 * and overlay visibility between the UI (Activity / ViewModel) and the background [PrankMonitorService].
 */
@Singleton
class PrankModeState @Inject constructor() {
    private val _isPrankModeActive = MutableStateFlow(false)
    val isPrankModeActive: StateFlow<Boolean> = _isPrankModeActive.asStateFlow()

    private val _isOverlayActive = MutableStateFlow(false)
    val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

    fun setPrankModeActive(active: Boolean) {
        _isPrankModeActive.value = active
    }

    fun setOverlayActive(active: Boolean) {
        _isOverlayActive.value = active
    }
}
