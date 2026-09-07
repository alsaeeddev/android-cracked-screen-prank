package com.alsaeeddev.domain.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * Domain contract for controlling and observing the system-wide background prank mode.
 *
 * Keeps Android Service interactions fully isolated in the data layer.
 */
interface PrankModeRepository {
    /**
     * Observable state indicating whether background shake detection / system prank mode is armed.
     */
    val isPrankModeActive: StateFlow<Boolean>

    /**
     * Observable state indicating whether the system overlay window is currently rendered on screen.
     */
    val isOverlayActive: StateFlow<Boolean>

    /**
     * Arms system-wide prank mode, initiating background monitoring.
     */
    fun startPrankMode()

    /**
     * Disarms system-wide prank mode, stopping background monitoring and dismissing any active overlay.
     */
    fun stopPrankMode()

    /**
     * Dismisses only the visible overlay without stopping background monitoring if armed.
     */
    fun dismissOverlay()

    /**
     * Updates the internal active state of the overlay window.
     */
    fun setOverlayActive(active: Boolean)

    /**
     * Sets whether prank mode is armed.
     */
    fun setPrankModeActive(active: Boolean)
}
