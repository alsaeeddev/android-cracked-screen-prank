package com.alsaeeddev.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alsaeeddev.domain.model.CrackStyle
import com.alsaeeddev.domain.model.TriggerType
import com.alsaeeddev.presentation.prank.CrackCanvas
import com.alsaeeddev.presentation.prank.CrackGeometry
import com.alsaeeddev.presentation.prank.CrackVectorPaths
import com.alsaeeddev.presentation.prank.PrankOverlayDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = uiState.settings
    val lifecycleOwner = LocalLifecycleOwner.current

    // Unregister / pause accelerometer observation during onPause to protect battery
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.startShakeObservation()
                viewModel.checkOverlayPermission()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.stopShakeObservation()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Dynamic extra interactive crack shards accumulated if user taps during active prank
    val interactiveCracks = remember { mutableStateListOf<CrackGeometry>() }
    var isTutorialDismissedLocally by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(com.alsaeeddev.ui.theme.PolishPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚡", fontSize = 18.sp, color = Color.White)
                        }
                        Text(
                            text = "ShatterPrank",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Hero Active Profile Card ("Professional Polish" style)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hero_tap_trigger_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = com.alsaeeddev.ui.theme.PolishHeroContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        Text(
                            text = "ACTIVE PROFILE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = com.alsaeeddev.ui.theme.PolishOnHeroContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${settings.crackStyle.displayName} Glass",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.alsaeeddev.ui.theme.PolishOnHeroContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (uiState.isShakeDetectionArmed)
                                "High sensitivity shake trigger active"
                            else
                                "Tap button below to launch instant crack overlay",
                            fontSize = 13.sp,
                            color = com.alsaeeddev.ui.theme.PolishOnHeroContainer.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    interactiveCracks.clear()
                                    viewModel.launchPrank(TriggerType.TAP)
                                },
                                modifier = Modifier.testTag("launch_prank_button"),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = com.alsaeeddev.ui.theme.PolishOnHeroContainer,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 11.dp)
                            ) {
                                Text(
                                    text = "TEST OVERLAY",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                            }
                        }
                    }
                }
            }

            // 1.5 System-Wide Overlay Mode Quick Action Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("system_prank_banner_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isSystemPrankModeActive)
                            com.alsaeeddev.ui.theme.PolishSuccess.copy(alpha = 0.12f)
                        else
                            com.alsaeeddev.ui.theme.PolishCardLight
                    ),
                    border = BorderStroke(
                        width = if (uiState.isSystemPrankModeActive) 1.5.dp else 1.dp,
                        color = if (uiState.isSystemPrankModeActive)
                            com.alsaeeddev.ui.theme.PolishSuccess
                        else
                            com.alsaeeddev.ui.theme.PolishBorderLight.copy(alpha = 0.8f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (uiState.isSystemPrankModeActive)
                                            com.alsaeeddev.ui.theme.PolishSuccess.copy(alpha = 0.2f)
                                        else
                                            com.alsaeeddev.ui.theme.PolishPrimary.copy(alpha = 0.12f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = if (uiState.isSystemPrankModeActive)
                                        com.alsaeeddev.ui.theme.PolishSuccess
                                    else
                                        com.alsaeeddev.ui.theme.PolishPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "System-Wide Prank",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (uiState.isSystemPrankModeActive)
                                        "Armed • Shake over any app"
                                    else
                                        "Trigger above any running app",
                                    fontSize = 12.sp,
                                    color = if (uiState.isSystemPrankModeActive)
                                        com.alsaeeddev.ui.theme.PolishSuccess
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.toggleSystemPrankMode() },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isSystemPrankModeActive)
                                    com.alsaeeddev.ui.theme.PolishAccent
                                else
                                    com.alsaeeddev.ui.theme.PolishPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("system_prank_action_button")
                        ) {
                            Text(
                                text = if (uiState.isSystemPrankModeActive) "DISARM" else "ARM",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 2. CONFIGURATION Section Header
            item {
                Text(
                    text = "CONFIGURATION",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            // 3. Shake Trigger Configuration Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shake_trigger_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = com.alsaeeddev.ui.theme.PolishCardLight
                    ),
                    border = if (uiState.isShakeDetectionArmed)
                        BorderStroke(1.dp, com.alsaeeddev.ui.theme.PolishBorderLight)
                    else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (uiState.isShakeDetectionArmed) com.alsaeeddev.ui.theme.PolishPrimaryContainer
                                        else Color.White,
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📳", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Shake Trigger",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (uiState.isShakeDetectionArmed)
                                        "ENABLED • ${settings.shakeSensitivity.thresholdG} G-FORCE"
                                    else
                                        "DISABLED",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = uiState.isShakeDetectionArmed,
                            onCheckedChange = { viewModel.toggleShakeArmed(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = com.alsaeeddev.ui.theme.PolishPrimary,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFD1D5DB)
                            ),
                            modifier = Modifier.testTag("toggle_shake_armed")
                        )
                    }
                }
            }

            // 4. Shatter Sound Configuration Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = com.alsaeeddev.ui.theme.PolishCardLight
                    ),
                    border = if (settings.soundEnabled)
                        BorderStroke(1.dp, com.alsaeeddev.ui.theme.PolishBorderLight)
                    else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (settings.soundEnabled) com.alsaeeddev.ui.theme.PolishPrimaryContainer
                                        else Color.White,
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔊", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Shatter Sound",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (settings.soundEnabled)
                                        "${settings.soundVariant.displayName.uppercase()} • ${(settings.soundVolume * 100).toInt()}% VOL"
                                    else
                                        "MUTED",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Sound Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 5. Delayed Start Configuration Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("timer_trigger_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = com.alsaeeddev.ui.theme.PolishCardLight
                    ),
                    border = if (uiState.isCountdownActive)
                        BorderStroke(1.dp, com.alsaeeddev.ui.theme.PolishBorderLight)
                    else null
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color.White, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("⏱️", fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Delayed Start",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (uiState.isCountdownActive && uiState.countdownRemainingSeconds != null)
                                            "COUNTDOWN IN ${uiState.countdownRemainingSeconds}S"
                                        else
                                            "OFF",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (uiState.isCountdownActive) {
                                OutlinedButton(
                                    onClick = { viewModel.cancelCountdown() },
                                    shape = CircleShape,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier.testTag("cancel_countdown_button")
                                ) {
                                    Text("Cancel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (!uiState.isCountdownActive) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(5, 10, 15, 30).forEach { seconds ->
                                    Button(
                                        onClick = {
                                            interactiveCracks.clear()
                                            viewModel.startDelayedCountdown(seconds)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .testTag("timer_${seconds}s_button"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.5.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            "${seconds}s",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Crack Style Selection Carousel
            item {
                Text(
                    text = "CRACK STYLES",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(CrackStyle.entries) { style ->
                        val isSelected = style == settings.crackStyle
                        Card(
                            onClick = { viewModel.setCrackStyle(style) },
                            modifier = Modifier
                                .width(135.dp)
                                .height(148.dp)
                                .testTag("home_style_${style.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    com.alsaeeddev.ui.theme.PolishPrimaryContainer.copy(alpha = 0.55f)
                                else com.alsaeeddev.ui.theme.PolishCardLight
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected)
                                    com.alsaeeddev.ui.theme.PolishPrimary
                                else com.alsaeeddev.ui.theme.PolishBorderLight.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(78.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1F1B1B))
                                ) {
                                    CrackCanvas(
                                        crackStyle = style,
                                        glintEnabled = false,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Text(
                                    text = style.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 7. Safe Exit Gesture Reminder Banner ("Professional Polish" black pill container)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF000000),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Hold down screen for 2 seconds or double-tap top-right corner to exit the prank overlay once active.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }

    // Full-screen Prank Overlay Dialog when active
    if (uiState.isPrankActive) {
        PrankOverlayDialog(
            crackStyle = settings.crackStyle,
            glintEnabled = settings.glintAnimationEnabled,
            tapToCrackMore = settings.tapToCrackMoreEnabled,
            extraCracks = interactiveCracks,
            showTutorial = !settings.hasSeenExitTutorial && !isTutorialDismissedLocally,
            autoDismissRemainingSeconds = settings.autoDismissSeconds.takeIf { it > 0 },
            onScreenTap = { tapOffset ->
                if (settings.tapToCrackMoreEnabled) {
                    val impact = CrackVectorPaths.generateTouchImpact(
                        touchX = tapOffset.x,
                        touchY = tapOffset.y,
                        seed = System.currentTimeMillis()
                    )
                    interactiveCracks.add(impact)
                    if (interactiveCracks.size > 8) {
                        interactiveCracks.removeAt(0)
                    }
                }
            },
            onDismissTutorial = {
                isTutorialDismissedLocally = true
                viewModel.dismissExitTutorial()
            },
            onExitPrank = {
                viewModel.dismissPrank()
            }
        )
    }

    // Overlay Permission Explanation Dialog
    if (uiState.showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPermissionRationale() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Display Over Other Apps Permission",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "To simulate a cracked screen on top of your home screen or other apps when shaken, Android requires the 'Display over other apps' permission.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• Purely for entertainment prank visual effects\n• NEVER locks or traps you out of your device\n• Safely disarmed at any time via notification or screen long-press",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onConfirmPermissionRationale() },
                    modifier = Modifier.testTag("confirm_overlay_permission_button")
                ) {
                    Text("Grant in Settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissPermissionRationale() },
                    modifier = Modifier.testTag("cancel_overlay_permission_button")
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
