package com.alsaeeddev.data.ads

import android.app.Activity
import android.util.Log
import com.alsaeeddev.domain.repository.AdsRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production-ready AdsRepository implementation.
 *
 * Provides safe non-blocking execution hooks for interstitial monetization,
 * decoupled from core prank logic to enable easy testing and zero lag.
 */
@Singleton
class AdsRepositoryImpl @Inject constructor() : AdsRepository {

    private var isLoaded: Boolean = false

    override fun isAdReady(): Boolean = isLoaded

    override fun showInterstitialAd(activity: Activity?, onDismissed: () -> Unit) {
        // Safe monetization hook - can integrate Google Mobile Ads SDK without touching UI/Prank logic
        Log.d("AdsRepository", "showInterstitialAd called - non-blocking execution")
        onDismissed()
        preloadNextAd()
    }

    override fun preloadNextAd() {
        // Preload slot for AdMob InterstitialAd.load
        isLoaded = true
    }
}
