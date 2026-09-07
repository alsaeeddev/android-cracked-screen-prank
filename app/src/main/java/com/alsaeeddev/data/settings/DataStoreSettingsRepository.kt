package com.alsaeeddev.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.alsaeeddev.domain.model.CrackStyle
import com.alsaeeddev.domain.model.PrankSettings
import com.alsaeeddev.domain.model.ShakeSensitivity
import com.alsaeeddev.domain.model.SoundVariant
import com.alsaeeddev.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "prank_settings")

/**
 * DataStore Preferences implementation of [SettingsRepository].
 *
 * Guarantees that all disk reading and writing is performed on [ioDispatcher]
 * and never blocks the main UI thread.
 */
@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SettingsRepository {

    private object PreferencesKeys {
        val CRACK_STYLE = stringPreferencesKey("crack_style")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val SOUND_VOLUME = floatPreferencesKey("sound_volume")
        val SOUND_VARIANT = stringPreferencesKey("sound_variant")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val AUTO_DISMISS_SECONDS = intPreferencesKey("auto_dismiss_seconds")
        val SHAKE_SENSITIVITY = floatPreferencesKey("shake_sensitivity")
        val DELAY_SECONDS = intPreferencesKey("delay_seconds")
        val HAS_SEEN_EXIT_TUTORIAL = booleanPreferencesKey("has_seen_exit_tutorial")
        val GLINT_ANIMATION_ENABLED = booleanPreferencesKey("glint_animation_enabled")
        val TAP_TO_CRACK_MORE_ENABLED = booleanPreferencesKey("tap_to_crack_more_enabled")
        val OVERLAY_AUTO_EXPIRE_HOURS = intPreferencesKey("overlay_auto_expire_hours")
        val OVERLAY_AUTO_EXPIRE_ENABLED = booleanPreferencesKey("overlay_auto_expire_enabled")
    }

    override val settingsFlow: Flow<PrankSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val crackStyleId = preferences[PreferencesKeys.CRACK_STYLE] ?: CrackStyle.SPIDERWEB.id
            val soundVariantId = preferences[PreferencesKeys.SOUND_VARIANT] ?: SoundVariant.SHATTER.id
            val sensitivityG = preferences[PreferencesKeys.SHAKE_SENSITIVITY] ?: ShakeSensitivity.MEDIUM.thresholdG

            PrankSettings(
                crackStyle = CrackStyle.fromId(crackStyleId),
                soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true,
                soundVolume = preferences[PreferencesKeys.SOUND_VOLUME] ?: 1.0f,
                soundVariant = SoundVariant.fromId(soundVariantId),
                vibrationEnabled = preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true,
                autoDismissSeconds = preferences[PreferencesKeys.AUTO_DISMISS_SECONDS] ?: 0,
                shakeSensitivity = ShakeSensitivity.fromThreshold(sensitivityG),
                delaySeconds = preferences[PreferencesKeys.DELAY_SECONDS] ?: 0,
                hasSeenExitTutorial = preferences[PreferencesKeys.HAS_SEEN_EXIT_TUTORIAL] ?: false,
                glintAnimationEnabled = preferences[PreferencesKeys.GLINT_ANIMATION_ENABLED] ?: true,
                tapToCrackMoreEnabled = preferences[PreferencesKeys.TAP_TO_CRACK_MORE_ENABLED] ?: true,
                overlayAutoExpireHours = preferences[PreferencesKeys.OVERLAY_AUTO_EXPIRE_HOURS] ?: 2,
                overlayAutoExpireEnabled = preferences[PreferencesKeys.OVERLAY_AUTO_EXPIRE_ENABLED] ?: true
            )
        }
        .flowOn(ioDispatcher)

    override suspend fun updateCrackStyle(style: CrackStyle) = withContext(ioDispatcher) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CRACK_STYLE] = style.id
        }
        Unit
    }

    override suspend fun updateSoundEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
        Unit
    }

    override suspend fun updateSoundVolume(volume: Float) = withContext(ioDispatcher) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_VOLUME] = volume.coerceIn(0f, 1f)
        }
        Unit
    }

    override suspend fun updateSoundVariant(variant: SoundVariant) = withContext(ioDispatcher) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_VARIANT] = variant.id
        }
        Unit
    }

    override suspend fun updateVibrationEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
        Unit
    }

    override suspend fun updateAutoDismissSeconds(seconds: Int) = withContext(ioDispatcher) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_DISMISS_SECONDS] = seconds.coerceAtLeast(0)
        }
        Unit
    }

    override suspend fun updateShakeSensitivity(sensitivity: ShakeSensitivity) = withContext(ioDispatcher) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHAKE_SENSITIVITY] = sensitivity.thresholdG
        }
        Unit
    }

    override suspend fun updateDelaySeconds(seconds: Int) = withContext(ioDispatcher) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DELAY_SECONDS] = seconds.coerceAtLeast(0)
        }
        Unit
    }

    override suspend fun markExitTutorialSeen() = withContext(ioDispatcher) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_EXIT_TUTORIAL] = true
        }
        Unit
    }

    override suspend fun updateGlintAnimationEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GLINT_ANIMATION_ENABLED] = enabled
        }
        Unit
    }

    override suspend fun updateTapToCrackMoreEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TAP_TO_CRACK_MORE_ENABLED] = enabled
        }
        Unit
    }

    override suspend fun updateOverlayAutoExpireHours(hours: Int) = withContext(ioDispatcher) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OVERLAY_AUTO_EXPIRE_HOURS] = hours.coerceIn(1, 24)
        }
        Unit
    }

    override suspend fun updateOverlayAutoExpireEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OVERLAY_AUTO_EXPIRE_ENABLED] = enabled
        }
        Unit
    }

    override suspend fun resetToDefaults() = withContext(ioDispatcher) {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
        Unit
    }
}
