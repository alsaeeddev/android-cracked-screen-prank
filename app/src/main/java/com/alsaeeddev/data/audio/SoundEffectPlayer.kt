package com.alsaeeddev.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.alsaeeddev.domain.audio.SoundPlayer
import com.alsaeeddev.domain.model.SoundVariant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * High-performance audio and haptic feedback manager.
 *
 * Uses direct [AudioTrack] PCM playback for zero-latency, artifact-free physical acoustics
 * without invoking system media decoders (Codec2/MediaCodec), eliminating hardware resource
 * query errors and ensuring complete offline reliability.
 *
 * @param context Application Context
 * @param ioDispatcher Coroutine dispatcher for background audio generation & loading
 */
@Singleton
class SoundEffectPlayer @Inject constructor(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SoundPlayer {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private val samplesMap = ConcurrentHashMap<SoundVariant, ShortArray>()
    private val tracksMap = ConcurrentHashMap<SoundVariant, List<AudioTrack>>()
    private val trackIndexMap = ConcurrentHashMap<SoundVariant, AtomicInteger>()

    init {
        scope.launch {
            preloadAllSounds()
        }
    }

    /**
     * Preloads synthesized glass shatter acoustics into memory on [ioDispatcher].
     */
    suspend fun preloadAllSounds() = withContext(ioDispatcher) {
        try {
            SoundVariant.entries.forEach { variant ->
                if (!tracksMap.containsKey(variant)) {
                    val samples = generateGlassShatterSamples(variant)
                    samplesMap[variant] = samples

                    val tracks = mutableListOf<AudioTrack>()
                    // 2 static tracks per variant for seamless rapid/overlapping playback
                    for (i in 0 until 2) {
                        createAudioTrack(samples)?.let { tracks.add(it) }
                    }
                    if (tracks.isNotEmpty()) {
                        tracksMap[variant] = tracks
                        trackIndexMap[variant] = AtomicInteger(0)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("SoundEffectPlayer", "Failed to preload audio tracks", e)
        }
    }

    private fun createAudioTrack(samples: ShortArray): AudioTrack? {
        return try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(44100)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val bufferSizeInBytes = samples.size * 2
            val track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(bufferSizeInBytes)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            val written = track.write(samples, 0, samples.size)
            if (written > 0 && track.state == AudioTrack.STATE_INITIALIZED) {
                track
            } else {
                track.release()
                null
            }
        } catch (e: Exception) {
            Log.w("SoundEffectPlayer", "Could not initialize AudioTrack", e)
            null
        }
    }

    /**
     * Plays the glass fracture sound effect for the given variant using zero-latency AudioTrack.
     *
     * @param variant Sound style preset
     * @param volume Playback volume (0.0 to 1.0)
     */
    override fun playGlassShatter(variant: SoundVariant, volume: Float) {
        val clampedVolume = volume.coerceIn(0.1f, 1.0f)
        val tracks = tracksMap[variant] ?: tracksMap[SoundVariant.SHATTER]

        if (!tracks.isNullOrEmpty()) {
            val counter = trackIndexMap[variant] ?: trackIndexMap[SoundVariant.SHATTER]
            val index = (counter?.getAndIncrement() ?: 0) % tracks.size
            val track = tracks[index]

            try {
                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        track.pause()
                    }
                    track.reloadStaticData()
                    track.setVolume(clampedVolume)
                    track.play()
                }
            } catch (e: Exception) {
                Log.w("SoundEffectPlayer", "Failed playing audio track", e)
            }
        } else {
            // Fallback if triggered before background preloading completes
            scope.launch {
                val samples = samplesMap[variant] ?: generateGlassShatterSamples(variant)
                samplesMap[variant] = samples
                val tempTrack = createAudioTrack(samples)
                if (tempTrack != null) {
                    try {
                        tempTrack.setVolume(clampedVolume)
                        tempTrack.play()
                    } catch (e: Exception) {
                        Log.w("SoundEffectPlayer", "Fallback play failed", e)
                    }
                }
            }
        }
    }

    /**
     * Triggers a tactile glass-shatter haptic feedback pattern.
     */
    override fun playShatterHaptic() {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Shatter sensation: Initial hard jolt, brief micro-pause, followed by crisp settling shards
                val timings = longArrayOf(0, 70, 30, 45, 25, 90)
                val amplitudes = intArrayOf(0, 255, 0, 180, 0, 120)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                v.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(longArrayOf(0, 80, 40, 100), -1)
            }
        } catch (e: Exception) {
            Log.w("SoundEffectPlayer", "Haptic trigger failed", e)
        }
    }

    /**
     * Plays a lighter haptic feedback for secondary tap cracks.
     */
    override fun playSecondaryCrackHaptic() {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(45, 200))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(45)
            }
        } catch (e: Exception) {
            Log.w("SoundEffectPlayer", "Secondary crack haptic failed", e)
        }
    }

    /**
     * Releases AudioTracks and cleans up audio resources.
     */
    override fun release() {
        try {
            tracksMap.values.forEach { trackList ->
                trackList.forEach { track ->
                    try {
                        if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                            track.stop()
                        }
                        track.release()
                    } catch (e: Exception) {
                        Log.w("SoundEffectPlayer", "Error releasing AudioTrack", e)
                    }
                }
            }
            tracksMap.clear()
            samplesMap.clear()
        } catch (e: Exception) {
            Log.w("SoundEffectPlayer", "Error releasing audio tracks", e)
        }
    }

    companion object {
        fun generateGlassShatterSamples(variant: SoundVariant): ShortArray {
            val sampleRate = 44100
            val durationSec = when (variant) {
                SoundVariant.CRISP_SNAP -> 0.45
                SoundVariant.BULLET_IMPACT -> 0.70
                SoundVariant.CRYSTAL_BREAK -> 0.85
                SoundVariant.SHATTER -> 1.05
            }
            val numSamples = (sampleRate * durationSec).toInt()
            val samples = ShortArray(numSamples)

            val rng = Random(variant.hashCode() + 42)

            // Resonant modal frequencies of glass shards based on acoustic physics
            val resonantFrequencies = when (variant) {
                SoundVariant.CRISP_SNAP -> doubleArrayOf(3400.0, 4800.0, 6200.0, 8100.0)
                SoundVariant.BULLET_IMPACT -> doubleArrayOf(1200.0, 2400.0, 4200.0, 6800.0, 9500.0)
                SoundVariant.CRYSTAL_BREAK -> doubleArrayOf(2800.0, 4100.0, 5600.0, 7200.0, 9100.0)
                SoundVariant.SHATTER -> doubleArrayOf(1850.0, 2740.0, 3950.0, 5400.0, 7600.0, 8900.0)
            }

            // Scatter impulses for falling shards
            val shardImpulses = IntArray(18) {
                (rng.nextDouble(0.04, durationSec * 0.9) * sampleRate).toInt()
            }

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate

                // 1. Initial high-energy transient impact burst (0-18ms)
                val impactEnvelope = exp(-t * 85.0)
                var signal = (rng.nextDouble(-1.0, 1.0)) * impactEnvelope * 0.65

                // 2. Resonant high-frequency glass ringing modes
                for ((idx, freq) in resonantFrequencies.withIndex()) {
                    val decayRate = 14.0 + (idx * 6.0)
                    val modeEnvelope = exp(-t * decayRate)
                    val modeWave = sin(2.0 * PI * freq * t)
                    signal += modeWave * modeEnvelope * (0.28 / (idx + 1))
                }

                // 3. Cascading micro-shatters and tinkles
                for (impulseSample in shardImpulses) {
                    val delta = i - impulseSample
                    if (delta in 0..1200) {
                        val shardT = delta.toDouble() / sampleRate
                        val shardEnvelope = exp(-shardT * 120.0)
                        val shardFreq = 4500.0 + (rng.nextDouble(-800.0, 1500.0))
                        val shardWave = sin(2.0 * PI * shardFreq * shardT)
                        signal += (shardWave * 0.22 + rng.nextDouble(-0.15, 0.15)) * shardEnvelope
                    }
                }

                // Apply soft limiting
                val clamped = signal.coerceIn(-1.0, 1.0)
                samples[i] = (clamped * 32767.0).toInt().toShort()
            }

            return samples
        }

        fun generateGlassShatterWav(variant: SoundVariant): ByteArray {
            val samples = generateGlassShatterSamples(variant)
            val sampleRate = 44100
            val byteCount = samples.size * 2
            val totalDataLen = byteCount + 36
            val buffer = ByteBuffer.allocate(44 + byteCount).order(ByteOrder.LITTLE_ENDIAN)

            // RIFF chunk descriptor
            buffer.put('R'.code.toByte())
            buffer.put('I'.code.toByte())
            buffer.put('F'.code.toByte())
            buffer.put('F'.code.toByte())
            buffer.putInt(totalDataLen)
            buffer.put('W'.code.toByte())
            buffer.put('A'.code.toByte())
            buffer.put('V'.code.toByte())
            buffer.put('E'.code.toByte())

            // "fmt " sub-chunk
            buffer.put('f'.code.toByte())
            buffer.put('m'.code.toByte())
            buffer.put('t'.code.toByte())
            buffer.put(' '.code.toByte())
            buffer.putInt(16) // SubChunk1Size for PCM
            buffer.putShort(1.toShort()) // AudioFormat (1 = PCM)
            buffer.putShort(1.toShort()) // NumChannels (1 = Mono)
            buffer.putInt(sampleRate)
            buffer.putInt(sampleRate * 2) // ByteRate
            buffer.putShort(2.toShort()) // BlockAlign
            buffer.putShort(16.toShort()) // BitsPerSample

            // "data" sub-chunk
            buffer.put('d'.code.toByte())
            buffer.put('a'.code.toByte())
            buffer.put('t'.code.toByte())
            buffer.put('a'.code.toByte())
            buffer.putInt(byteCount)

            for (sample in samples) {
                buffer.putShort(sample)
            }

            return buffer.array()
        }
    }
}
