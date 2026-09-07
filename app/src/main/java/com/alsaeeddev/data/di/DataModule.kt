package com.alsaeeddev.data.di

import android.content.Context
import com.alsaeeddev.data.ads.AdsRepositoryImpl
import com.alsaeeddev.data.audio.SoundEffectPlayer
import com.alsaeeddev.data.sensor.ShakeDetector
import com.alsaeeddev.data.settings.DataStoreSettingsRepository
import com.alsaeeddev.domain.repository.AdsRepository
import com.alsaeeddev.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

/**
 * Hilt module binding data-layer implementations to domain interfaces.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository {
        return DataStoreSettingsRepository(context, Dispatchers.IO)
    }

    @Provides
    @Singleton
    fun provideAdsRepository(): AdsRepository {
        return AdsRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideShakeDetector(
        @ApplicationContext context: Context
    ): ShakeDetector {
        return ShakeDetector(context)
    }

    @Provides
    @Singleton
    fun provideSoundEffectPlayer(
        @ApplicationContext context: Context
    ): SoundEffectPlayer {
        return SoundEffectPlayer(context, Dispatchers.IO)
    }
}
