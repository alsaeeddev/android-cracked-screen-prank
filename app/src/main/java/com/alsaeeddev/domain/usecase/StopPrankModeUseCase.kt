package com.alsaeeddev.domain.usecase

import com.alsaeeddev.domain.repository.PrankModeRepository
import javax.inject.Inject

/**
 * Use case that disarms system-wide prank monitoring and dismisses any active overlay.
 */
class StopPrankModeUseCase @Inject constructor(
    private val prankModeRepository: PrankModeRepository
) {
    operator fun invoke() {
        prankModeRepository.stopPrankMode()
    }
}
