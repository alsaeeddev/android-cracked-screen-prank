package com.alsaeeddev.data.di

import android.content.Context
import com.alsaeeddev.data.audio.SoundEffectPlayer
import com.alsaeeddev.data.overlay.OverlayPermissionRepositoryImpl
import com.alsaeeddev.data.overlay.OverlayWindowController
import com.alsaeeddev.data.service.PrankModeState
import com.alsaeeddev.data.service.ServicePrankModeController
import com.alsaeeddev.domain.repository.OverlayPermissionRepository
import com.alsaeeddev.domain.repository.PrankModeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding overlay permissions, prank mode lifecycle state, and system overlay services.
 */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun providePrankModeState(): PrankModeState {
        return PrankModeState()
    }

    @Provides
    @Singleton
    fun provideOverlayPermissionRepository(
        @ApplicationContext context: Context
    ): OverlayPermissionRepository {
        return OverlayPermissionRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun providePrankModeRepository(
        @ApplicationContext context: Context,
        prankModeState: PrankModeState
    ): PrankModeRepository {
        return ServicePrankModeController(context, prankModeState)
    }

    @Provides
    @Singleton
    fun provideOverlayWindowController(
        @ApplicationContext context: Context,
        soundEffectPlayer: SoundEffectPlayer
    ): OverlayWindowController {
        return OverlayWindowController(context, soundEffectPlayer)
    }
}
