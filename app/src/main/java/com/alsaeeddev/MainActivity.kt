package com.alsaeeddev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.alsaeeddev.presentation.navigation.FakeCrackedScreenNavGraph
import com.alsaeeddev.ui.theme.MyApplicationTheme

/**
 * Main entry activity configured with backward-compatible Android 7+ splash screen
 * and full edge-to-edge rendering.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen for Android 7+ backward compatibility
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FakeCrackedScreenNavGraph()
                }
            }
        }
    }
}

