package com.alsaeeddev.data.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.alsaeeddev.data.audio.SoundEffectPlayer
import com.alsaeeddev.domain.model.PrankSettings
import com.alsaeeddev.presentation.prank.CrackGeometry
import com.alsaeeddev.presentation.prank.CrackVectorPaths
import com.alsaeeddev.presentation.prank.PrankOverlayContent
import com.alsaeeddev.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

/**
 * Custom lifecycle host for attaching Jetpack Compose trees to a system [WindowManager] overlay.
 */
class OverlayLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    init {
        savedStateRegistryController.performRestore(Bundle())
        lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
    }

    fun start() {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun destroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        store.clear()
    }

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
}

/**
 * Controller managing the injection, rendering, and safe dismissal of the system-wide
 * cracked screen overlay view hosted via [WindowManager.addView].
 *
 * Enforces:
 * 1. Strict main-thread dispatching for all WindowManager operations.
 * 2. Proper ComposeView lifecycle ownership to prevent leaks.
 * 3. Graceful fallback on [WindowManager.BadTokenException] or permission revocation.
 * 4. Safe exit gestures (long-press 1.8s, double-tap top-right, or auto-dismiss countdown).
 */
class OverlayWindowController(
    private val context: Context,
    private val soundEffectPlayer: SoundEffectPlayer
) {
    private val windowManager by lazy {
        context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var composeView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null
    private var timerJob: Job? = null
    private val overlayScope = CoroutineScope(Dispatchers.Main + Job())

    @Volatile
    private var isShowing = false

    fun isOverlayShowing(): Boolean = isShowing

    /**
     * Renders the cracked screen overlay on top of all applications and system UI.
     *
     * @param settings Configuration snapshot controlling style, sounds, and auto-dismiss.
     * @param onExit Invoked when the user performs a safe-exit gesture or auto-dismiss expires.
     * @param onError Invoked if permission is absent or a WindowManager error occurs.
     */
    fun showOverlay(
        settings: PrankSettings,
        onExit: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        mainHandler.post {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                    onError(SecurityException("SYSTEM_ALERT_WINDOW permission not granted"))
                    return@post
                }

                val wm = windowManager
                if (wm == null) {
                    onError(IllegalStateException("WindowManager service unavailable"))
                    return@post
                }

                // If already visible, tear down existing instance first
                hideOverlayInternal()

                val owner = OverlayLifecycleOwner()
                val view = ComposeView(context)

                view.setViewTreeLifecycleOwner(owner)
                view.setViewTreeViewModelStoreOwner(owner)
                view.setViewTreeSavedStateRegistryOwner(owner)

                owner.start()

                view.setContent {
                    MyApplicationTheme {
                        val extraCracks = remember { mutableStateListOf<CrackGeometry>() }
                        var remainingSeconds by remember {
                            mutableIntStateOf(settings.autoDismissSeconds)
                        }

                        // Auto-dismiss countdown timer
                        androidx.compose.runtime.LaunchedEffect(settings.autoDismissSeconds) {
                            if (settings.autoDismissSeconds > 0) {
                                for (s in settings.autoDismissSeconds downTo 1) {
                                    remainingSeconds = s
                                    delay(1000L)
                                }
                                hideOverlay()
                                onExit()
                            }
                        }

                        PrankOverlayContent(
                            crackStyle = settings.crackStyle,
                            glintEnabled = settings.glintAnimationEnabled,
                            tapToCrackMore = settings.tapToCrackMoreEnabled,
                            extraCracks = extraCracks,
                            showTutorial = false, // Never show "How to exit" dialog over other apps
                            autoDismissRemainingSeconds = if (settings.autoDismissSeconds > 0) remainingSeconds else null,
                            onScreenTap = { tapOffset ->
                                if (settings.tapToCrackMoreEnabled) {
                                    val impact = CrackVectorPaths.generateTouchImpact(
                                        touchX = tapOffset.x,
                                        touchY = tapOffset.y,
                                        seed = System.currentTimeMillis()
                                    )
                                    extraCracks.add(impact)
                                    if (extraCracks.size > 8) {
                                        extraCracks.removeAt(0)
                                    }
                                    if (settings.soundEnabled) {
                                        soundEffectPlayer.playGlassShatter(
                                            variant = settings.soundVariant,
                                            volume = settings.soundVolume * 0.6f
                                        )
                                    }
                                    if (settings.vibrationEnabled) {
                                        soundEffectPlayer.playShatterHaptic()
                                    }
                                }
                            },
                            onDismissTutorial = {
                                // Not used in system overlay
                            },
                            onExitPrank = {
                                hideOverlay()
                                onExit()
                            }
                        )
                    }
                }

                val layoutParams = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        @Suppress("DEPRECATION")
                        WindowManager.LayoutParams.TYPE_PHONE
                    },
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                }

                wm.addView(view, layoutParams)
                composeView = view
                lifecycleOwner = owner
                isShowing = true
            } catch (e: Exception) {
                hideOverlayInternal()
                onError(e)
            }
        }
    }

    /**
     * Safely detaches and dismisses the overlay view on the main thread.
     */
    fun hideOverlay() {
        mainHandler.post {
            hideOverlayInternal()
        }
    }

    private fun hideOverlayInternal() {
        timerJob?.cancel()
        timerJob = null

        val view = composeView
        if (view != null) {
            try {
                windowManager?.removeView(view)
            } catch (_: Exception) {
                try {
                    windowManager?.removeViewImmediate(view)
                } catch (_: Exception) {}
            }
        }
        lifecycleOwner?.destroy()
        lifecycleOwner = null
        composeView = null
        isShowing = false
    }
}
