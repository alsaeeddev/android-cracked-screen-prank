package com.alsaeeddev.di

import android.content.Context
import com.alsaeeddev.data.ads.AdsRepositoryImpl
import com.alsaeeddev.data.audio.SoundEffectPlayer
import com.alsaeeddev.data.overlay.OverlayPermissionRepositoryImpl
import com.alsaeeddev.data.overlay.OverlayWindowController
import com.alsaeeddev.data.sensor.ShakeDetector
import com.alsaeeddev.data.service.PrankModeState
import com.alsaeeddev.data.service.ServicePrankModeController
import com.alsaeeddev.data.settings.DataStoreSettingsRepository
import com.alsaeeddev.domain.repository.AdsRepository
import com.alsaeeddev.domain.repository.OverlayPermissionRepository
import com.alsaeeddev.domain.repository.PrankModeRepository
import com.alsaeeddev.domain.repository.SettingsRepository
import com.alsaeeddev.domain.usecase.ObserveShakeEventsUseCase
import com.alsaeeddev.domain.usecase.StartPrankModeUseCase
import com.alsaeeddev.domain.usecase.StopPrankModeUseCase
import com.alsaeeddev.domain.usecase.TriggerPrankUseCase
import kotlinx.coroutines.Dispatchers

/**
 * Dependency container providing single-source-of-truth instances for repositories and use cases.
 * Allows pure dependency injection across ViewModels, tests, and preview providers.
 */
interface AppContainer {
    val settingsRepository: SettingsRepository
    val adsRepository: AdsRepository
    val shakeDetector: ShakeDetector
    val soundEffectPlayer: SoundEffectPlayer
    val observeShakeEventsUseCase: ObserveShakeEventsUseCase
    val triggerPrankUseCase: TriggerPrankUseCase
    val overlayPermissionRepository: OverlayPermissionRepository
    val prankModeRepository: PrankModeRepository
    val startPrankModeUseCase: StartPrankModeUseCase
    val stopPrankModeUseCase: StopPrankModeUseCase
    val overlayWindowController: OverlayWindowController
    val prankModeState: PrankModeState
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val settingsRepository: SettingsRepository by lazy {
        DataStoreSettingsRepository(context, Dispatchers.IO)
    }

    override val adsRepository: AdsRepository by lazy {
        AdsRepositoryImpl()
    }

    override val shakeDetector: ShakeDetector by lazy {
        ShakeDetector(context)
    }

    override val soundEffectPlayer: SoundEffectPlayer by lazy {
        SoundEffectPlayer(context, Dispatchers.IO)
    }

    override val observeShakeEventsUseCase: ObserveShakeEventsUseCase by lazy {
        ObserveShakeEventsUseCase(shakeDetector, settingsRepository)
    }

    override val triggerPrankUseCase: TriggerPrankUseCase by lazy {
        TriggerPrankUseCase(settingsRepository, soundEffectPlayer, adsRepository, Dispatchers.Default)
    }

    override val prankModeState: PrankModeState by lazy {
        PrankModeState()
    }

    override val overlayPermissionRepository: OverlayPermissionRepository by lazy {
        OverlayPermissionRepositoryImpl(context)
    }

    override val overlayWindowController: OverlayWindowController by lazy {
        OverlayWindowController(context, soundEffectPlayer)
    }

    override val prankModeRepository: PrankModeRepository by lazy {
        ServicePrankModeController(context, prankModeState)
    }

    override val startPrankModeUseCase: StartPrankModeUseCase by lazy {
        StartPrankModeUseCase(prankModeRepository, overlayPermissionRepository)
    }

    override val stopPrankModeUseCase: StopPrankModeUseCase by lazy {
        StopPrankModeUseCase(prankModeRepository)
    }
}
