package com.alsaeeddev.data.service

import android.content.Context
import android.content.Intent
import android.os.Build
import com.alsaeeddev.domain.repository.PrankModeRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * Data layer implementation of [PrankModeRepository] controlling [PrankMonitorService].
 *
 * Keeps all Android Service intent dispatches and framework calls encapsulated away
 * from Domain use cases and Presentation ViewModels.
 */
class ServicePrankModeController(
    private val context: Context,
    private val prankModeState: PrankModeState
) : PrankModeRepository {

    override val isPrankModeActive: StateFlow<Boolean> = prankModeState.isPrankModeActive

    override val isOverlayActive: StateFlow<Boolean> = prankModeState.isOverlayActive

    override fun startPrankMode() {
        val intent = Intent(context, PrankMonitorService::class.java).apply {
            action = PrankMonitorService.ACTION_START_MONITORING
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            prankModeState.setPrankModeActive(true)
        } catch (e: Exception) {
            android.util.Log.e("ServicePrankModeController", "Failed to startForegroundService, falling back to startService", e)
            try {
                context.startService(intent)
                prankModeState.setPrankModeActive(true)
            } catch (e2: Exception) {
                android.util.Log.e("ServicePrankModeController", "Failed to startService", e2)
                prankModeState.setPrankModeActive(false)
            }
        }
    }

    override fun stopPrankMode() {
        val intent = Intent(context, PrankMonitorService::class.java).apply {
            action = PrankMonitorService.ACTION_STOP_MONITORING
        }
        try {
            context.startService(intent)
        } catch (_: Exception) {
            context.stopService(intent)
        }
        prankModeState.setPrankModeActive(false)
        prankModeState.setOverlayActive(false)
    }

    override fun dismissOverlay() {
        val intent = Intent(context, PrankMonitorService::class.java).apply {
            action = PrankMonitorService.ACTION_DISMISS_OVERLAY
        }
        try {
            context.startService(intent)
        } catch (_: Exception) {}
        prankModeState.setOverlayActive(false)
    }

    override fun setOverlayActive(active: Boolean) {
        prankModeState.setOverlayActive(active)
    }

    override fun setPrankModeActive(active: Boolean) {
        prankModeState.setPrankModeActive(active)
    }
}
