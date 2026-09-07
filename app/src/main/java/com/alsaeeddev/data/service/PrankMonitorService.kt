package com.alsaeeddev.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import com.alsaeeddev.FakeCrackedScreenApp
import com.alsaeeddev.MainActivity
import com.alsaeeddev.R
import com.alsaeeddev.data.overlay.OverlayWindowController
import com.alsaeeddev.data.sensor.ShakeDetector
import com.alsaeeddev.domain.model.TriggerType
import com.alsaeeddev.domain.repository.SettingsRepository
import com.alsaeeddev.domain.usecase.TriggerPrankUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Foreground service executing background shake motion detection and system-wide overlay hosting.
 */
class PrankMonitorService : LifecycleService() {

    private lateinit var shakeDetector: ShakeDetector
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var triggerPrankUseCase: TriggerPrankUseCase
    private lateinit var overlayWindowController: OverlayWindowController
    private lateinit var prankModeState: PrankModeState

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var shakeCollectionJob: Job? = null
    private var autoExpireJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        val app = applicationContext as? FakeCrackedScreenApp
        if (app != null) {
            val container = app.appContainer
            shakeDetector = container.shakeDetector
            settingsRepository = container.settingsRepository
            triggerPrankUseCase = container.triggerPrankUseCase
            overlayWindowController = container.overlayWindowController
            prankModeState = container.prankModeState
        }
        createNotificationChannel()
        startForegroundSafely()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START_MONITORING -> {
                startForegroundMonitoring()
            }
            ACTION_STOP_MONITORING -> {
                stopMonitoringAndSelf()
            }
            ACTION_DISMISS_OVERLAY -> {
                if (::overlayWindowController.isInitialized) {
                    overlayWindowController.hideOverlay()
                }
                if (::prankModeState.isInitialized) {
                    prankModeState.setOverlayActive(false)
                }
            }
            else -> {
                startForegroundMonitoring()
            }
        }

        return START_STICKY
    }

    private fun startForegroundSafely() {
        try {
            val notification = buildForegroundNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                try {
                    ServiceCompat.startForeground(
                        this,
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    )
                } catch (e: Exception) {
                    Log.w("PrankMonitorService", "SpecialUse startForeground failed, falling back to standard", e)
                    startForeground(NOTIFICATION_ID, notification)
                }
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e("PrankMonitorService", "startForeground failed", e)
        }
    }

    private fun startForegroundMonitoring() {
        startForegroundSafely()

        if (::prankModeState.isInitialized) {
            prankModeState.setPrankModeActive(true)
        }

        if (!::shakeDetector.isInitialized || !::settingsRepository.isInitialized || !::overlayWindowController.isInitialized) {
            Log.e("PrankMonitorService", "Dependencies not initialized, cannot start monitoring loop")
            return
        }

        // Cancel existing background jobs if restarting
        shakeCollectionJob?.cancel()
        autoExpireJob?.cancel()

        // 1. Shake monitoring loop
        shakeCollectionJob = serviceScope.launch {
            val settings = settingsRepository.settingsFlow.first()

            // Schedule auto-expire watchdog if enabled
            if (settings.overlayAutoExpireEnabled && settings.overlayAutoExpireHours > 0) {
                scheduleAutoExpire(settings.overlayAutoExpireHours)
            }

            shakeDetector.observeBackgroundShakes(
                thresholdG = settings.shakeSensitivity.thresholdG,
                sustainedDurationMs = 500L,
                cooldownMs = 2500L
            ).collect { _ ->
                if (::prankModeState.isInitialized && !prankModeState.isOverlayActive.value && !overlayWindowController.isOverlayShowing()) {
                    val currentSettings = settingsRepository.settingsFlow.first()
                    if (currentSettings.delaySeconds > 0) {
                        delay(currentSettings.delaySeconds * 1000L)
                    }

                    if (isActive) {
                        triggerPrankUseCase(TriggerType.SHAKE)
                        overlayWindowController.showOverlay(
                            settings = currentSettings,
                            onExit = {
                                if (::prankModeState.isInitialized) {
                                    prankModeState.setOverlayActive(false)
                                }
                            },
                            onError = { _ ->
                                if (::prankModeState.isInitialized) {
                                    prankModeState.setOverlayActive(false)
                                }
                                showRevokedPermissionNotification()
                                stopMonitoringAndSelf()
                            }
                        )
                        if (::prankModeState.isInitialized) {
                            prankModeState.setOverlayActive(true)
                        }
                    }
                }
            }
        }
    }

    private fun scheduleAutoExpire(hours: Int) {
        autoExpireJob = serviceScope.launch {
            delay(hours * 3600 * 1000L)
            stopMonitoringAndSelf()
        }
    }

    private fun stopMonitoringAndSelf() {
        shakeCollectionJob?.cancel()
        autoExpireJob?.cancel()
        if (::overlayWindowController.isInitialized) {
            overlayWindowController.hideOverlay()
        }
        if (::prankModeState.isInitialized) {
            prankModeState.setOverlayActive(false)
            prankModeState.setPrankModeActive(false)
        }
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {}
        stopSelf()
    }

    private fun buildForegroundNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            101,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disarmIntent = Intent(this, PrankMonitorService::class.java).apply {
            action = ACTION_STOP_MONITORING
        }
        val disarmPendingIntent = PendingIntent.getService(
            this,
            102,
            disarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_crack)
            .setContentTitle("Prank Mode Active")
            .setContentText("Shake phone to crack screen • Tap Disarm to stop")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Disarm",
                disarmPendingIntent
            )
            .build()
    }

    private fun showRevokedPermissionNotification() {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_crack)
                .setContentTitle("Prank Mode Disarmed")
                .setContentText("Display over other apps permission was revoked.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            notificationManager?.notify(NOTIFICATION_REVOKED_ID, notification)
        } catch (e: Exception) {
            Log.e("PrankMonitorService", "Failed to show revoked notification", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Prank Mode Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows persistent status while background prank shake detection is armed."
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        if (::overlayWindowController.isInitialized) {
            overlayWindowController.hideOverlay()
        }
        if (::prankModeState.isInitialized) {
            prankModeState.setOverlayActive(false)
            prankModeState.setPrankModeActive(false)
        }
        super.onDestroy()
    }

    companion object {
        const val ACTION_START_MONITORING = "com.alsaeeddev.action.START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.alsaeeddev.action.STOP_MONITORING"
        const val ACTION_DISMISS_OVERLAY = "com.alsaeeddev.action.DISMISS_OVERLAY"

        const val CHANNEL_ID = "prank_monitor_service_channel"
        const val NOTIFICATION_ID = 2001
        const val NOTIFICATION_REVOKED_ID = 2002
    }
}
