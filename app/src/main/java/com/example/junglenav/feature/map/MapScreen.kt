package com.example.junglenav.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MapScreen(
    uiState: MapUiState,
    viewportState: MapViewportState,
    onToggleRecording: () -> Unit,
    onAddWaypoint: () -> Unit,
    onReturnToBase: () -> Unit,
    onMapStyleLoaded: () -> Unit,
    onMapStyleLoadFailed: (String) -> Unit,
    onToggleOfflineRegionDetails: () -> Unit,
    onCenterGps: () -> Unit,
    onToggleTopoLayer: (Boolean) -> Unit,
    onToggleHillshadeLayer: (Boolean) -> Unit,
    onToggleImageryLayer: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(uiState.styleUri) {
        viewportState.loadStyle(uiState.styleUri)
    }
    LaunchedEffect(uiState.isRecording) {
        viewportState.showTrack(uiState.isRecording)
    }
    LaunchedEffect(Unit) {
        viewportState.showWaypoints(true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("map_full_view"),
    ) {
        MapLibreMapSurface(
            uiState = uiState,
            viewportState = viewportState,
            onMapStyleLoaded = onMapStyleLoaded,
            onMapStyleLoadFailed = onMapStyleLoadFailed,
            fullScreenMode = true,
            showMetadata = false,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                MapSignalBadge(text = uiState.operationMode.name)
                MapSignalBadge(text = navigationSourceBadgeLabel(uiState.navigationSourceLabel))
                MapSignalBadge(text = "MapLibre OpenGL")
                MapSignalBadge(text = mapSourceBadgeLabel(uiState.mapSourceLabel))
            }

            Surface(
                modifier = Modifier.testTag("compact_target_summary"),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 260.dp)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = uiState.activeTargetName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${uiState.confidenceLabel} | score ${uiState.navigationConfidenceScore}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            text = uiState.distanceLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp)
                .testTag("map_bottom_sheet"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            tonalElevation = 8.dp,
            shadowElevation = 10.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .background(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
                            shape = CircleShape,
                        )
                        .fillMaxWidth(0.18f)
                        .padding(vertical = 2.dp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledTonalButton(
                        onClick = onCenterGps,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("center_gps_button"),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Text(
                            when {
                                !uiState.hasGpsFix -> "Enable GPS"
                                uiState.isFollowingGps -> "Following GPS"
                                else -> "Follow GPS"
                            },
                        )
                    }
                    OutlinedButton(
                        onClick = onToggleOfflineRegionDetails,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("offline_region_toggle"),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Text(if (uiState.isOfflineRegionExpanded) "Hide Region" else "Show Region")
                    }
                }

                Text(
                    text = "Source ${uiState.navigationSourceLabel} | ${uiState.gpsStatusLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (uiState.availableLayers.topoVector) {
                        FilterChip(
                            selected = uiState.isTopoEnabled,
                            onClick = { onToggleTopoLayer(!uiState.isTopoEnabled) },
                            label = { Text("Topo") },
                            modifier = Modifier.testTag("topo_toggle_chip"),
                        )
                    }
                    if (uiState.canShowHillshadeToggle) {
                        FilterChip(
                            selected = uiState.isHillshadeEnabled,
                            onClick = { onToggleHillshadeLayer(!uiState.isHillshadeEnabled) },
                            label = { Text("Hillshade") },
                            modifier = Modifier.testTag("hillshade_toggle_chip"),
                        )
                    }
                    if (uiState.canShowImageryToggle) {
                        FilterChip(
                            selected = uiState.isImageryEnabled,
                            onClick = { onToggleImageryLayer(!uiState.isImageryEnabled) },
                            label = { Text("Imagery") },
                            modifier = Modifier.testTag("imagery_toggle_chip"),
                        )
                    }
                }

                if (uiState.isOfflineRegionExpanded) {
                    Column(
                        modifier = Modifier.testTag("offline_region_details"),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = uiState.activePackageName ?: "No offline region active",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Style source: ${uiState.mapSourceLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        uiState.activePackTrustLabel?.let { trustLabel ->
                            Text(
                                text = "Trust: $trustLabel",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = "Renderer: MapLibre OpenGL",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = if (viewportState.isTrackVisible) {
                                "Track layer live | Waypoints visible"
                            } else {
                                "Track layer idle | Waypoints visible"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }

                Button(
                    onClick = onToggleRecording,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Text(if (uiState.isRecording) "Stop Recording" else "Start Recording")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = onAddWaypoint,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Text("Add Waypoint")
                    }
                    OutlinedButton(
                        onClick = onReturnToBase,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Text("Return To Base")
                    }
                }
            }
        }
    }
}

@Composable
private fun MapSignalBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier
                .widthIn(max = 132.dp)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun navigationSourceBadgeLabel(label: String): String {
    return when (label) {
        "DEAD_RECKONING" -> "DR"
        "MAP_MATCHED" -> "Matched"
        "TERRAIN_CORRECTED" -> "Terrain"
        else -> label
    }
}

private fun mapSourceBadgeLabel(label: String): String {
    return when {
        label.contains("fallback", ignoreCase = true) -> "Fallback style"
        label.contains("offline", ignoreCase = true) -> "Offline style"
        else -> label
    }
}
