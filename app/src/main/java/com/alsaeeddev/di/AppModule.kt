package com.alsaeeddev.di

import com.alsaeeddev.data.audio.SoundEffectPlayer
import com.alsaeeddev.data.sensor.ShakeDetector
import com.alsaeeddev.domain.repository.AdsRepository
import com.alsaeeddev.domain.repository.OverlayPermissionRepository
import com.alsaeeddev.domain.repository.PrankModeRepository
import com.alsaeeddev.domain.repository.SettingsRepository
import com.alsaeeddev.domain.usecase.ObserveShakeEventsUseCase
import com.alsaeeddev.domain.usecase.StartPrankModeUseCase
import com.alsaeeddev.domain.usecase.StopPrankModeUseCase
import com.alsaeeddev.domain.usecase.TriggerPrankUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

/**
 * Top-level application Hilt module providing threading dispatchers and domain use cases.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Singleton
    fun provideObserveShakeEventsUseCase(
        shakeDetector: ShakeDetector,
        settingsRepository: SettingsRepository
    ): ObserveShakeEventsUseCase {
        return ObserveShakeEventsUseCase(shakeDetector, settingsRepository)
    }

    @Provides
    @Singleton
    fun provideTriggerPrankUseCase(
        settingsRepository: SettingsRepository,
        soundEffectPlayer: SoundEffectPlayer,
        adsRepository: AdsRepository
    ): TriggerPrankUseCase {
        return TriggerPrankUseCase(settingsRepository, soundEffectPlayer, adsRepository, Dispatchers.Default)
    }

    @Provides
    @Singleton
    fun provideStartPrankModeUseCase(
        prankModeRepository: PrankModeRepository,
        overlayPermissionRepository: OverlayPermissionRepository
    ): StartPrankModeUseCase {
        return StartPrankModeUseCase(prankModeRepository, overlayPermissionRepository)
    }

    @Provides
    @Singleton
    fun provideStopPrankModeUseCase(
        prankModeRepository: PrankModeRepository
    ): StopPrankModeUseCase {
        return StopPrankModeUseCase(prankModeRepository)
    }
}
