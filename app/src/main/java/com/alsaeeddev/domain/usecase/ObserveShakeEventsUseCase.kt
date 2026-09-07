package com.alsaeeddev.domain.usecase

import com.alsaeeddev.data.sensor.ShakeDetector
import com.alsaeeddev.domain.model.ShakeEvent
import com.alsaeeddev.domain.repository.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

/**
 * Use case that streams [ShakeEvent]s configured with the user's saved sensitivity threshold.
 */
class ObserveShakeEventsUseCase @Inject constructor(
    private val shakeDetector: ShakeDetector,
    private val settingsRepository: SettingsRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<ShakeEvent> {
        return settingsRepository.settingsFlow.flatMapLatest { settings ->
            shakeDetector.observeShakes(
                thresholdG = settings.shakeSensitivity.thresholdG,
                debounceMs = 600L
            )
        }
    }
}
