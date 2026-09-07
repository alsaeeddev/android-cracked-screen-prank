package com.alsaeeddev.presentation.prank

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.alsaeeddev.domain.model.CrackStyle
import kotlinx.coroutines.delay

/**
 * Full-screen prank overlay composable displaying vector glass cracks over the entire device display.
 *
 * =========================================================================================
 * PLAY STORE POLICY COMPLIANCE NOTE:
 * Per Google Play Console Policies regarding Prank & Entertainment Applications (Malware /
 * Device & Network Abuse):
 * 1. This application is strictly an entertainment prank simulation.
 * 2. It must NEVER trap the user or lock them out of their Android device.
 * 3. A clear, reliable safe-exit gesture (long-press anywhere on screen for 1.8s, or double-
 *    tapping the top-right corner) is guaranteed to immediately dismiss the prank.
 * 4. First-time users are shown a prominent tutorial explaining how to safely exit at any time.
 * =========================================================================================
 */
@Composable
fun PrankOverlayDialog(
    crackStyle: CrackStyle,
    glintEnabled: Boolean,
    tapToCrackMore: Boolean,
    extraCracks: List<CrackGeometry>,
    showTutorial: Boolean,
    autoDismissRemainingSeconds: Int?,
    onScreenTap: (Offset) -> Unit,
    onDismissTutorial: () -> Unit,
    onExitPrank: () -> Unit
) {
    Dialog(
        onDismissRequest = onExitPrank,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        PrankOverlayContent(
            crackStyle = crackStyle,
            glintEnabled = glintEnabled,
            tapToCrackMore = tapToCrackMore,
            extraCracks = extraCracks,
            showTutorial = showTutorial,
            autoDismissRemainingSeconds = autoDismissRemainingSeconds,
            onScreenTap = onScreenTap,
            onDismissTutorial = onDismissTutorial,
            onExitPrank = onExitPrank
        )
    }
}

@Composable
fun PrankOverlayContent(
    crackStyle: CrackStyle,
    glintEnabled: Boolean,
    tapToCrackMore: Boolean,
    extraCracks: List<CrackGeometry>,
    showTutorial: Boolean,
    autoDismissRemainingSeconds: Int?,
    onScreenTap: (Offset) -> Unit,
    onDismissTutorial: () -> Unit,
    onExitPrank: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isLongPressActive by remember { mutableStateOf(false) }
    var localTutorialDismissed by remember(showTutorial) { mutableStateOf(false) }
    val isTutorialVisible = showTutorial && !localTutorialDismissed

    // Auto-dismiss tutorial after 5 seconds if not dismissed manually
    LaunchedEffect(isTutorialVisible) {
        if (isTutorialVisible) {
            delay(5000L)
            localTutorialDismissed = true
            onDismissTutorial()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .testTag("prank_overlay_container")
            // Dual exit gesture: Long-press anywhere for 1.8s triggers safe exit
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        // Double-tapping in top corner safely exits
                        if (offset.y < 200f && offset.x > size.width - 200f) {
                            onExitPrank()
                        } else {
                            onScreenTap(offset)
                        }
                    },
                    onLongPress = {
                        isLongPressActive = true
                        onExitPrank()
                    },
                    onTap = { offset ->
                        onScreenTap(offset)
                    }
                )
            }
    ) {
        // Core vector crack rendering canvas
        CrackCanvas(
            crackStyle = crackStyle,
            glintEnabled = glintEnabled,
            extraCracks = extraCracks,
            onScreenTap = null, // Handled by container pointerInput
            modifier = Modifier.fillMaxSize()
        )

        // Subtle discreet exit hotspot in top-right corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 36.dp, end = 16.dp)
                .size(48.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { onExitPrank() },
                        onLongPress = { onExitPrank() }
                    )
                }
                .testTag("exit_hotspot"),
            contentAlignment = Alignment.Center
        ) {
            // Invisible or ultra-subtle safe exit button
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0x22FFFFFF), CircleShape)
            )
        }

        // Auto-dismiss countdown badge if configured
        if (autoDismissRemainingSeconds != null && autoDismissRemainingSeconds > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
                    .testTag("auto_dismiss_badge"),
                shape = RoundedCornerShape(16.dp),
                color = Color(0x990F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
            ) {
                Text(
                    text = "Prank auto-dismissing in ${autoDismissRemainingSeconds}s",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // First-launch tutorial banner explaining safe exit gesture
        AnimatedVisibility(
            visible = isTutorialVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp, start = 20.dp, end = 20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xF00F172A),
                tonalElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4438BDF8)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("exit_tutorial_banner")
                    .pointerInput(Unit) {
                        detectTapGestures { /* consume tap so it does not trigger crack underneath */ }
                    }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Tutorial Info",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "How to Exit Prank",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                localTutorialDismissed = true
                                onDismissTutorial()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Long-press screen (2s) anywhere\n• Or double-tap top-right corner\n• Tap screen to add more cracks!",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            localTutorialDismissed = true
                            onDismissTutorial()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dismiss_tutorial_button")
                    ) {
                        Text(
                            text = "Got it",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
