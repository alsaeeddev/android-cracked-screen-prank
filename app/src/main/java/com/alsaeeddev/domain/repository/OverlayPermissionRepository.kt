package com.alsaeeddev.domain.repository

/**
 * Repository interface for checking and requesting SYSTEM_ALERT_WINDOW permission.
 *
 * Keeps Android framework Settings calls isolated from the presentation layer.
 */
interface OverlayPermissionRepository {
    /**
     * Checks if the app currently holds permission to draw over other applications.
     */
    fun hasOverlayPermission(): Boolean

    /**
     * Directs the user to the system settings page to grant overlay permission.
     */
    fun requestOverlayPermission()
}
