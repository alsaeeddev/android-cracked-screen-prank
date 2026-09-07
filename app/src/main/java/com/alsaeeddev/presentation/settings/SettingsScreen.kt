package com.alsaeeddev.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alsaeeddev.domain.model.CrackStyle
import com.alsaeeddev.domain.model.ShakeSensitivity
import com.alsaeeddev.domain.model.SoundVariant
import com.alsaeeddev.presentation.prank.CrackCanvas

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = uiState.settings
    val lifecycleOwner = LocalLifecycleOwner.current

    // Re-check overlay permission whenever the user returns to the settings screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkOverlayPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Prank Settings",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.resetToDefaults() },
                        modifier = Modifier.testTag("reset_defaults_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Defaults",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Crack Style Selection
            item {
                Text(
                    text = "CRACK STYLE VARIANT",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
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
                                .width(140.dp)
                                .height(160.dp)
                                .testTag("crack_style_${style.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    com.alsaeeddev.ui.theme.PolishPrimaryContainer.copy(alpha = 0.55f)
                                else com.alsaeeddev.ui.theme.PolishCardLight
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected)
                                    com.alsaeeddev.ui.theme.PolishPrimary
                                else com.alsaeeddev.ui.theme.PolishBorderLight.copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Miniature Vector Crack Preview Window
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(85.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1F1B1B))
                                ) {
                                    CrackCanvas(
                                        crackStyle = style,
                                        glintEnabled = false,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Column {
                                    Text(
                                        text = style.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = style.tag,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Visual Effects Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = com.alsaeeddev.ui.theme.PolishCardLight
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.alsaeeddev.ui.theme.PolishBorderLight.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Visual Effects",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Glint Animation Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Animation,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Glass Glint Sweep",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        "Smooth light reflection across fractures",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = settings.glintAnimationEnabled,
                                onCheckedChange = { viewModel.setGlintAnimationEnabled(it) },
                                modifier = Modifier.testTag("toggle_glint")
                            )
                        }

                        // Tap to crack more
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Tap To Shatter More",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        "Touching screen creates new impact points",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = settings.tapToCrackMoreEnabled,
                                onCheckedChange = { viewModel.setTapToCrackMoreEnabled(it) },
                                modifier = Modifier.testTag("toggle_tap_crack")
                            )
                        }
                    }
                }
            }

            // 3. Audio & Haptics Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = com.alsaeeddev.ui.theme.PolishCardLight
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.alsaeeddev.ui.theme.PolishBorderLight.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sound & Vibration",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedButton(
                                onClick = { viewModel.previewSound() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("preview_sound_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test", fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Sound master toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Glass Shatter Audio",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                )
                            }
                            Switch(
                                checked = settings.soundEnabled,
                                onCheckedChange = { viewModel.setSoundEnabled(it) },
                                modifier = Modifier.testTag("toggle_sound")
                            )
                        }

                        if (settings.soundEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "SOUND EFFECT PRESET",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SoundVariant.entries.forEach { variant ->
                                    FilterChip(
                                        selected = variant == settings.soundVariant,
                                        onClick = { viewModel.setSoundVariant(variant) },
                                        label = { Text(variant.displayName, fontSize = 12.sp) },
                                        leadingIcon = if (variant == settings.soundVariant) {
                                            {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        } else null,
                                        modifier = Modifier.testTag("sound_variant_${variant.id}")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Volume: ${(settings.soundVolume * 100).toInt()}%",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = settings.soundVolume,
                                onValueChange = { viewModel.setSoundVolume(it) },
                                valueRange = 0.1f..1.0f,
                                modifier = Modifier.testTag("sound_volume_slider")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Vibration toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Haptic Shockwave",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        "Physical tactile vibration burst on crack",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = settings.vibrationEnabled,
                                onCheckedChange = { viewModel.setVibrationEnabled(it) },
                                modifier = Modifier.testTag("toggle_vibration")
                            )
                        }
                    }
                }
            }

            // 4. Triggers & Auto-Dismiss Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = com.alsaeeddev.ui.theme.PolishCardLight
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.alsaeeddev.ui.theme.PolishBorderLight.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Triggers & Timers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Shake Sensitivity
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Shake Sensor Sensitivity",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ShakeSensitivity.entries.forEach { sensitivity ->
                                FilterChip(
                                    selected = sensitivity == settings.shakeSensitivity,
                                    onClick = { viewModel.setShakeSensitivity(sensitivity) },
                                    label = { Text(sensitivity.label, fontSize = 12.sp) },
                                    modifier = Modifier.testTag("shake_sensitivity_${sensitivity.name}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Auto-Dismiss Timer
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Auto-Dismiss Timer",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        val dismissOptions = listOf(
                            0 to "Manual Exit Only",
                            5 to "5 Seconds",
                            10 to "10 Seconds",
                            15 to "15 Seconds",
                            30 to "30 Seconds"
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            dismissOptions.forEach { (sec, label) ->
                                FilterChip(
                                    selected = settings.autoDismissSeconds == sec,
                                    onClick = { viewModel.setAutoDismissSeconds(sec) },
                                    label = { Text(label, fontSize = 12.sp) },
                                    modifier = Modifier.testTag("auto_dismiss_${sec}s")
                                )
                            }
                        }
                    }
                }
            }

            // 5. System-Wide Overlay Mode (Prank Any App)
            item {
                Text(
                    text = "SYSTEM-WIDE OVERLAY (PRANK ANY APP)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isPrankModeActive)
                            com.alsaeeddev.ui.theme.PolishSuccess.copy(alpha = 0.08f)
                        else
                            com.alsaeeddev.ui.theme.PolishCardLight
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (uiState.isPrankModeActive) 1.5.dp else 1.dp,
                        color = if (uiState.isPrankModeActive)
                            com.alsaeeddev.ui.theme.PolishSuccess
                        else
                            com.alsaeeddev.ui.theme.PolishBorderLight.copy(alpha = 0.7f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Title + Status Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(
                                            if (uiState.isPrankModeActive)
                                                com.alsaeeddev.ui.theme.PolishSuccess.copy(alpha = 0.18f)
                                            else
                                                com.alsaeeddev.ui.theme.PolishPrimary.copy(alpha = 0.12f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Layers,
                                        contentDescription = null,
                                        tint = if (uiState.isPrankModeActive)
                                            com.alsaeeddev.ui.theme.PolishSuccess
                                        else
                                            com.alsaeeddev.ui.theme.PolishPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "System Overlay Prank",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (uiState.isPrankModeActive) "Armed & Monitoring (FGS)" else "Disabled",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (uiState.isPrankModeActive)
                                            com.alsaeeddev.ui.theme.PolishSuccess
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = uiState.isPrankModeActive,
                                onCheckedChange = { viewModel.togglePrankMode() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = com.alsaeeddev.ui.theme.PolishSuccess
                                ),
                                modifier = Modifier.testTag("system_prank_mode_toggle")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "When armed, shaking your phone will crack the screen directly on top of the home screen or any other app. Prank is safely dismissible at any time.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        androidx.compose.material3.HorizontalDivider(
                            color = com.alsaeeddev.ui.theme.PolishBorderLight.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Permission Status Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (uiState.hasOverlayPermission)
                                        Icons.Default.CheckCircle
                                    else
                                        Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (uiState.hasOverlayPermission)
                                        com.alsaeeddev.ui.theme.PolishSuccess
                                    else
                                        com.alsaeeddev.ui.theme.PolishAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Display over other apps",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (uiState.hasOverlayPermission) "Permission Granted" else "Action Required",
                                        fontSize = 11.sp,
                                        color = if (uiState.hasOverlayPermission)
                                            com.alsaeeddev.ui.theme.PolishSuccess
                                        else
                                            com.alsaeeddev.ui.theme.PolishAccent
                                    )
                                }
                            }

                            if (!uiState.hasOverlayPermission) {
                                OutlinedButton(
                                    onClick = { viewModel.onConfirmPermissionRationale() },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("grant_overlay_permission_button"),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Grant", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        androidx.compose.material3.HorizontalDivider(
                            color = com.alsaeeddev.ui.theme.PolishBorderLight.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Battery Auto-Expire Duration Settings
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BatteryAlert,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Auto-Disarm Watchdog",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                            Switch(
                                checked = settings.overlayAutoExpireEnabled,
                                onCheckedChange = { viewModel.setOverlayAutoExpireEnabled(it) },
                                modifier = Modifier.testTag("auto_expire_switch")
                            )
                        }

                        if (settings.overlayAutoExpireEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Stops background monitoring after ${settings.overlayAutoExpireHours} hours to prevent battery drain:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            val hourOptions = listOf(1, 2, 4, 8)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                hourOptions.forEach { h ->
                                    FilterChip(
                                        selected = settings.overlayAutoExpireHours == h,
                                        onClick = { viewModel.setOverlayAutoExpireHours(h) },
                                        label = { Text("${h}h", fontSize = 12.sp) },
                                        modifier = Modifier.testTag("auto_expire_${h}h")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Play Store Compliance Notice
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = com.alsaeeddev.ui.theme.PolishHeroContainer.copy(alpha = 0.45f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.alsaeeddev.ui.theme.PolishBorderLight.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Safe Exit",
                            tint = com.alsaeeddev.ui.theme.PolishPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Safe Exit Guarantee (Play Policy)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = com.alsaeeddev.ui.theme.PolishOnHeroContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "This app complies with Google Play policies for prank apps. The screen is never permanently locked. You can always exit by holding down anywhere on the screen for 2 seconds or double-tapping the top-right corner.",
                                fontSize = 12.sp,
                                color = com.alsaeeddev.ui.theme.PolishOnHeroContainer.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
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
