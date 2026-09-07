package com.alsaeeddev.domain.usecase

import com.alsaeeddev.domain.repository.OverlayPermissionRepository
import com.alsaeeddev.domain.repository.PrankModeRepository
import javax.inject.Inject

/**
 * Use case that activates background system-wide prank monitoring.
 *
 * Validates overlay permission prerequisites before requesting service start.
 * Returns true if started successfully, or false if overlay permission is missing.
 */
class StartPrankModeUseCase @Inject constructor(
    private val prankModeRepository: PrankModeRepository,
    private val overlayPermissionRepository: OverlayPermissionRepository
) {
    operator fun invoke(): Boolean {
        if (!overlayPermissionRepository.hasOverlayPermission()) {
            return false
        }
        prankModeRepository.startPrankMode()
        return true
    }
}
