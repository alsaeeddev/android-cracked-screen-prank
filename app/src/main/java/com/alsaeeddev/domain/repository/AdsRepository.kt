package com.alsaeeddev.domain.repository

import android.app.Activity

/**
 * Interface abstracting advertisement display hooks to prevent coupling core prank logic.
 */
interface AdsRepository {
    /**
     * Checks whether an interstitial or rewarded ad is loaded and ready to present.
     */
    fun isAdReady(): Boolean

    /**
     * Attempts to show an interstitial ad without blocking prank overlay activation.
     */
    fun showInterstitialAd(activity: Activity?, onDismissed: () -> Unit = {})

    /**
     * Preloads the next ad unit in background.
     */
    fun preloadNextAd()
}
