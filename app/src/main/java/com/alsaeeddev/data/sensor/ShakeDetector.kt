package com.alsaeeddev.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.alsaeeddev.domain.model.ShakeEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * SensorManager wrapper that detects physical phone shake motions via the accelerometer.
 *
 * Implements a debounced, lifecycle-safe Flow of [ShakeEvent] objects. The underlying
 * sensor listener is automatically unregistered as soon as the Flow collection completes
 * or the lifecycle scope cancels, ensuring zero battery waste and no memory leaks.
 *
 * @param context Android Application Context
 */
@Singleton
class ShakeDetector @Inject constructor(
    private val context: Context
) {
    private val sensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    }

    /**
     * Creates a cold Flow of [ShakeEvent]s using [callbackFlow].
     *
     * @param thresholdG Acceleration magnitude threshold in G (e.g., 2.5G for normal shake).
     * @param debounceMs Milliseconds required between successive shake triggers to prevent double-firing.
     */
    fun observeShakes(
        thresholdG: Float = 2.5f,
        debounceMs: Long = 600L
    ): Flow<ShakeEvent> = callbackFlow {
        val manager = sensorManager
        if (manager == null) {
            close()
            return@callbackFlow
        }

        val accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer == null) {
            close()
            return@callbackFlow
        }

        val lastShakeTimestampMs = AtomicLong(0L)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

                val x = event.values.getOrNull(0) ?: 0f
                val y = event.values.getOrNull(1) ?: 0f
                val z = event.values.getOrNull(2) ?: 0f

                // Convert acceleration into Earth G-force units
                val gX = x / SensorManager.GRAVITY_EARTH
                val gY = y / SensorManager.GRAVITY_EARTH
                val gZ = z / SensorManager.GRAVITY_EARTH

                val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

                if (gForce >= thresholdG) {
                    val nowMs = System.currentTimeMillis()
                    val lastMs = lastShakeTimestampMs.get()

                    if (nowMs - lastMs >= debounceMs) {
                        if (lastShakeTimestampMs.compareAndSet(lastMs, nowMs)) {
                            trySend(
                                ShakeEvent(
                                    magnitudeG = gForce,
                                    timestampNs = event.timestamp
                                )
                            )
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // No-op for accelerometer accuracy changes
            }
        }

        manager.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )

        awaitClose {
            manager.unregisterListener(listener)
        }
    }

    /**
     * Creates a battery-efficient background flow of [ShakeEvent]s using [SensorManager.SENSOR_DELAY_NORMAL]
     * and [BackgroundShakeFilter] requiring sustained movement (e.g. >= 500ms) to prevent pocket triggers.
     */
    fun observeBackgroundShakes(
        thresholdG: Float = 2.5f,
        sustainedDurationMs: Long = 500L,
        cooldownMs: Long = 2000L
    ): Flow<ShakeEvent> = callbackFlow {
        val manager = sensorManager
        if (manager == null) {
            close()
            return@callbackFlow
        }

        val accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer == null) {
            close()
            return@callbackFlow
        }

        val filter = BackgroundShakeFilter(
            thresholdG = thresholdG,
            sustainedDurationMs = sustainedDurationMs,
            cooldownMs = cooldownMs
        )

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

                val x = event.values.getOrNull(0) ?: 0f
                val y = event.values.getOrNull(1) ?: 0f
                val z = event.values.getOrNull(2) ?: 0f

                val gX = x / SensorManager.GRAVITY_EARTH
                val gY = y / SensorManager.GRAVITY_EARTH
                val gZ = z / SensorManager.GRAVITY_EARTH

                val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)
                val nowMs = System.currentTimeMillis()

                val shakeEvent = filter.processSample(nowMs, gForce, event.timestamp)
                if (shakeEvent != null) {
                    trySend(shakeEvent)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // No-op
            }
        }

        // Use SENSOR_DELAY_NORMAL to conserve battery while phone is idle in background
        manager.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        awaitClose {
            manager.unregisterListener(listener)
        }
    }

    /**
     * Pure calculation helper for unit testing shake threshold and debounce logic
     * without requiring real hardware sensors.
     */
    companion object {
        fun calculateGForce(x: Float, y: Float, z: Float): Float {
            val gX = x / 9.80665f
            val gY = y / 9.80665f
            val gZ = z / 9.80665f
            return sqrt(gX * gX + gY * gY + gZ * gZ)
        }

        fun isDebounced(lastShakeMs: Long, currentMs: Long, debounceMs: Long): Boolean {
            return (currentMs - lastShakeMs) >= debounceMs
        }
    }
}
