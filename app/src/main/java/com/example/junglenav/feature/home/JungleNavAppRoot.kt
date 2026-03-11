package com.example.junglenav.feature.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.junglenav.app.AppContainer
import com.example.junglenav.app.AppRoute
import com.example.junglenav.feature.diagnostics.DiagnosticsScreen
import com.example.junglenav.feature.diagnostics.DiagnosticsViewModel
import com.example.junglenav.feature.map.MapScreen
import com.example.junglenav.feature.map.MapViewModel
import com.example.junglenav.feature.map.rememberMapViewportState
import com.example.junglenav.feature.package_manager.PackageManagerScreen
import com.example.junglenav.feature.package_manager.PackageManagerViewModel
import com.example.junglenav.feature.settings.SettingsScreen
import com.example.junglenav.feature.track.TrackScreen
import com.example.junglenav.feature.track.TrackViewModel
import com.example.junglenav.feature.waypoint.WaypointListScreen
import com.example.junglenav.feature.waypoint.WaypointListViewModel
import com.example.junglenav.system.offline.imports.ImportedMapPackSource
import com.example.junglenav.ui.theme.JungleNavTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun JungleNavAppRoot(
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lowLightModeEnabled by container.lowLightModeEnabled.collectAsState()
    val preferredUnits by container.units.collectAsState()
    val navigationReadiness by container.navigationReadiness.collectAsState()
    val latestPositionEstimate by container.latestPositionEstimate.collectAsState()
    val latestLocationSample by container.latestLocationSample.collectAsState()
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val gpsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasLocationPermission = granted
        container.refreshNavigationInputs()
    }

    JungleNavTheme(lowLightModeEnabled = lowLightModeEnabled) {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route ?: AppRoute.startDestination
        val routes = remember { AppRoute.topLevelRoutes }
        val fieldStatus by container.fieldStatus.collectAsState()
        val activeTargetLabel by container.activeTargetLabel.collectAsState()
        val distanceLabel by container.distanceLabel.collectAsState()
        val isRecording by container.isRecording.collectAsState()
        val viewportState = rememberMapViewportState()
        val waypointViewModel = remember(container.waypointRepository) {
            WaypointListViewModel(container.waypointRepository)
        }
        val waypointUiState by waypointViewModel.uiState.collectAsState()
        val trackViewModel = remember(container.trackRepository) {
            TrackViewModel(container.trackRepository)
        }
        val trackUiState by trackViewModel.uiState.collectAsState()
        val packageManagerViewModel = remember(
            container.mapPackageRepository,
            container.remoteMapPackCatalogService,
            container.mapPackDownloadService,
            container.localMapPackImportService,
        ) {
            PackageManagerViewModel(
                repository = container.mapPackageRepository,
                catalogService = container.remoteMapPackCatalogService,
                downloadService = container.mapPackDownloadService,
                importService = container.localMapPackImportService,
            )
        }
        val packageUiState by packageManagerViewModel.uiState.collectAsState()
        val mapPackImportLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            uri ?: return@rememberLauncherForActivityResult
            packageManagerViewModel.onImportCompleted(
                ImportedMapPackSource(
                    uriString = uri.toString(),
                    displayName = uri.lastPathSegment,
                ),
            )
        }
        val mapViewModel = remember { MapViewModel() }
        val mapUiState by mapViewModel.uiState.collectAsState()
        val diagnosticsViewModel = remember { DiagnosticsViewModel() }
        val diagnosticsUiState by diagnosticsViewModel.uiState.collectAsState()

        LaunchedEffect(fieldStatus.mode, fieldStatus.confidenceLevel) {
            mapViewModel.onPositionStateChanged(
                mode = fieldStatus.mode,
                confidenceLevel = fieldStatus.confidenceLevel,
            )
        }
        LaunchedEffect(
            latestPositionEstimate.mode,
            latestPositionEstimate.source,
            latestPositionEstimate.confidence,
        ) {
            mapViewModel.onPositionStateChanged(
                mode = latestPositionEstimate.mode,
                confidenceLevel = when {
                    latestPositionEstimate.confidence >= 75 -> com.example.junglenav.core.model.ConfidenceLevel.HIGH
                    latestPositionEstimate.confidence >= 45 -> com.example.junglenav.core.model.ConfidenceLevel.MEDIUM
                    else -> com.example.junglenav.core.model.ConfidenceLevel.LOW
                },
            )
            mapViewModel.onNavigationSourceChanged(
                source = latestPositionEstimate.source,
                confidence = latestPositionEstimate.confidence,
            )
        }
        LaunchedEffect(fieldStatus.operationMode) {
            mapViewModel.onOperationModeChanged(fieldStatus.operationMode)
        }
        LaunchedEffect(activeTargetLabel, distanceLabel) {
            mapViewModel.onTargetChanged(
                activeTargetName = activeTargetLabel,
                distanceLabel = distanceLabel,
            )
        }
        LaunchedEffect(isRecording) {
            mapViewModel.onRecordingChanged(isRecording)
        }
        LaunchedEffect(packageUiState.activePackage?.id) {
            val activePackage = packageUiState.activePackage
            activePackage?.let {
                viewportState.setCenter(activePackage.centerLatitude, activePackage.centerLongitude)
            }
            val resolvedOfflineStyleUri = activePackage?.let { mapPackage ->
                withContext(Dispatchers.IO) {
                    container.resolveOfflineStyleUri(mapPackage)
                }
            }
            if (activePackage != null && resolvedOfflineStyleUri != null) {
                mapViewModel.onOfflineBundleResolved(
                    mapPackage = activePackage,
                    resolvedStyleUri = resolvedOfflineStyleUri,
                )
            } else {
                mapViewModel.onActivePackageChanged(
                    activePackageName = activePackage?.name,
                    activePackagePath = activePackage?.filePath,
                    isOfflinePackageReady = activePackage?.isDownloaded == true,
                )
            }
        }
        LaunchedEffect(
            fieldStatus.operationMode,
            fieldStatus.mode,
            latestPositionEstimate.source,
            packageUiState.activePackage?.id,
        ) {
            diagnosticsViewModel.syncState(
                operationMode = fieldStatus.operationMode,
                positionMode = fieldStatus.mode,
                navigationSourceLabel = latestPositionEstimate.source.name,
                lastReliableFixAgeMs = if (fieldStatus.mode == com.example.junglenav.core.model.PositionMode.NO_FIX) {
                    null
                } else {
                    (System.currentTimeMillis() - latestPositionEstimate.timestampEpochMs).coerceAtLeast(0L)
                },
                activePackageId = packageUiState.activePackage?.id,
            )
        }
        LaunchedEffect(navigationReadiness) {
            diagnosticsViewModel.syncNavigationReadiness(navigationReadiness)
        }
        LaunchedEffect(hasLocationPermission) {
            if (hasLocationPermission) {
                container.refreshNavigationInputs()
            } else {
                mapViewModel.onGpsUnavailable("Grant location access")
            }
        }
        LaunchedEffect(latestLocationSample?.timestampEpochMs) {
            val sample = latestLocationSample
            if (sample != null) {
                mapViewModel.onLiveGpsFix(
                    latitude = sample.latitude,
                    longitude = sample.longitude,
                    source = latestPositionEstimate.source,
                    confidence = latestPositionEstimate.confidence,
                )
            } else if (!hasLocationPermission) {
                mapViewModel.onGpsUnavailable("Grant location access")
            } else {
                mapViewModel.onGpsUnavailable("Waiting for GPS fix")
            }
        }
        LaunchedEffect(latestLocationSample?.timestampEpochMs, mapUiState.isFollowingGps) {
            val sample = latestLocationSample ?: return@LaunchedEffect
            if (mapUiState.isFollowingGps) {
                viewportState.setCenter(sample.latitude, sample.longitude)
            }
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f),
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                ),
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                topBar = {
                    if (currentRoute != AppRoute.Map.route) {
                        FieldStatusBar(
                            activeTargetLabel = mapUiState.activeTargetName,
                            distanceLabel = mapUiState.distanceLabel,
                            positionMode = mapUiState.positionMode,
                            confidenceLabel = mapUiState.confidenceLabel,
                            operationMode = mapUiState.operationMode,
                            isRecording = mapUiState.isRecording,
                        )
                    }
                },
                bottomBar = {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                        shape = RoundedCornerShape(28.dp),
                        tonalElevation = 10.dp,
                        shadowElevation = 12.dp,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("route_bar")
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("route_single_row"),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                routes.forEach { route ->
                                    RoutePill(
                                        route = route,
                                        selected = currentRoute == route.route,
                                        onClick = {
                                            navController.navigate(route.route) {
                                                launchSingleTop = true
                                                restoreState = true
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }
                },
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = AppRoute.startDestination,
                    modifier = Modifier,
                ) {
                    routes.forEach { route ->
                        composable(route.route) {
                            when (route) {
                                AppRoute.Map -> MapScreen(
                                    uiState = mapUiState,
                                    viewportState = viewportState,
                                    onToggleRecording = {
                                        if (trackUiState.isRecording) {
                                            trackViewModel.stopRecording()
                                            container.setRecording(false)
                                            diagnosticsViewModel.recordEvent("Track recording stopped")
                                        } else {
                                            trackViewModel.startRecording()
                                            container.setRecording(true)
                                            diagnosticsViewModel.recordEvent("Track recording started")
                                        }
                                    },
                                    onAddWaypoint = {
                                        val nextIndex = waypointUiState.items.size + 1
                                        val label = "Field Point $nextIndex"
                                        waypointViewModel.saveWaypoint(
                                            name = label,
                                            latitude = viewportState.centerLatitude,
                                            longitude = viewportState.centerLongitude,
                                            note = "Created from map quick action",
                                        )
                                        container.setTarget(label, "0 m")
                                        diagnosticsViewModel.recordEvent("Waypoint added from map: $label")
                                    },
                                    onReturnToBase = {
                                        container.setTarget("Base Camp", "120 m")
                                        diagnosticsViewModel.recordEvent("Return to base selected")
                                    },
                                    onMapStyleLoaded = {
                                        mapViewModel.onMapStyleLoaded()
                                    },
                                    onMapStyleLoadFailed = { message ->
                                        mapViewModel.onMapStyleLoadFailed(message)
                                        diagnosticsViewModel.recordEvent("Map style load issue: $message")
                                    },
                                    onToggleOfflineRegionDetails = {
                                        mapViewModel.setOfflineRegionExpanded(!mapUiState.isOfflineRegionExpanded)
                                    },
                                    onCenterGps = {
                                        if (!hasLocationPermission) {
                                            gpsPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                        } else if (mapUiState.isFollowingGps) {
                                            mapViewModel.setGpsFollowing(false)
                                            diagnosticsViewModel.recordEvent("GPS follow paused")
                                        } else {
                                            container.refreshNavigationInputs()
                                            latestLocationSample?.let { sample ->
                                                mapViewModel.setGpsFollowing(true)
                                                viewportState.setCenter(sample.latitude, sample.longitude)
                                                diagnosticsViewModel.recordEvent("Map following live GPS")
                                            } ?: run {
                                                mapViewModel.onGpsUnavailable("Waiting for GPS fix")
                                                diagnosticsViewModel.recordEvent("Waiting for live GPS fix")
                                            }
                                        }
                                    },
                                    onToggleTopoLayer = { enabled ->
                                        mapViewModel.setTopoEnabled(enabled)
                                        diagnosticsViewModel.recordEvent("Topo layer ${if (enabled) "enabled" else "disabled"}")
                                    },
                                    onToggleHillshadeLayer = { enabled ->
                                        mapViewModel.setHillshadeEnabled(enabled)
                                        diagnosticsViewModel.recordEvent("Hillshade layer ${if (enabled) "enabled" else "disabled"}")
                                    },
                                    onToggleImageryLayer = { enabled ->
                                        mapViewModel.setImageryEnabled(enabled)
                                        diagnosticsViewModel.recordEvent("Imagery layer ${if (enabled) "enabled" else "disabled"}")
                                    },
                                    modifier = Modifier,
                                )

                                AppRoute.Waypoints -> WaypointListScreen(
                                    uiState = waypointUiState,
                                    onAddWaypoint = {
                                        val nextIndex = waypointUiState.items.size + 1
                                        val label = "Waypoint $nextIndex"
                                        waypointViewModel.saveWaypoint(
                                            name = label,
                                            latitude = -6.0 + nextIndex,
                                            longitude = 106.0 + nextIndex,
                                            note = "Created from shell",
                                        )
                                        container.setTarget(label, "${50 * nextIndex} m")
                                        diagnosticsViewModel.recordEvent("Waypoint added from list: $label")
                                    },
                                    modifier = Modifier.padding(innerPadding),
                                )

                                AppRoute.Tracks -> TrackScreen(
                                    uiState = trackUiState,
                                    onToggleRecording = {
                                        if (trackUiState.isRecording) {
                                            trackViewModel.stopRecording()
                                            container.setRecording(false)
                                            diagnosticsViewModel.recordEvent("Track recording stopped")
                                        } else {
                                            trackViewModel.startRecording()
                                            container.setRecording(true)
                                            diagnosticsViewModel.recordEvent("Track recording started")
                                        }
                                    },
                                    modifier = Modifier.padding(innerPadding),
                                )

                                AppRoute.Packages -> PackageManagerScreen(
                                    uiState = packageUiState,
                                    onImportPack = {
                                        mapPackImportLauncher.launch(arrayOf("*/*"))
                                    },
                                    onSearchQueryChanged = { query ->
                                        packageManagerViewModel.onSearchQueryChanged(query)
                                    },
                                    onDownloadCatalogPack = { regionId ->
                                        packageManagerViewModel.downloadCatalogPack(regionId)
                                        diagnosticsViewModel.recordEvent("Mission bundle download requested: $regionId")
                                    },
                                    onActivatePackage = {
                                        packageManagerViewModel.activate(it)
                                        diagnosticsViewModel.recordEvent("Active package changed to $it")
                                    },
                                    onConfirmPendingActivation = {
                                        packageManagerViewModel.confirmPendingActivation()
                                        diagnosticsViewModel.recordEvent("Unverified bundle activation confirmed")
                                    },
                                    onDismissPendingActivation = {
                                        packageManagerViewModel.dismissPendingActivation()
                                        diagnosticsViewModel.recordEvent("Unverified bundle activation dismissed")
                                    },
                                    onDeletePackage = {
                                        packageManagerViewModel.delete(it)
                                        diagnosticsViewModel.recordEvent("Package deleted $it")
                                    },
                                    modifier = Modifier.padding(innerPadding),
                                )

                                AppRoute.Diagnostics -> DiagnosticsScreen(
                                    uiState = diagnosticsUiState,
                                    modifier = Modifier.padding(innerPadding),
                                )

                                AppRoute.Settings -> SettingsScreen(
                                    operationMode = fieldStatus.operationMode,
                                    units = preferredUnits,
                                    lowLightModeEnabled = lowLightModeEnabled,
                                    activePackageName = packageUiState.activePackage?.name,
                                    onOperationModeSelected = { operationMode ->
                                        container.setOperationMode(operationMode)
                                        diagnosticsViewModel.recordEvent("Operation mode changed to ${operationMode.name}")
                                    },
                                    onUnitsSelected = { units ->
                                        container.setUnits(units)
                                        diagnosticsViewModel.recordEvent("Units set to $units")
                                    },
                                    onLowLightModeChanged = { enabled ->
                                        container.setLowLightModeEnabled(enabled)
                                        diagnosticsViewModel.recordEvent(
                                            if (enabled) {
                                                "Low-light mode enabled"
                                            } else {
                                                "Low-light mode disabled"
                                            },
                                        )
                                    },
                                    modifier = Modifier.padding(innerPadding),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutePill(
    route: AppRoute,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        label = "routeBackground",
    )
    val content by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "routeContent",
    )

    Box(
        modifier = modifier
            .testTag("route_icon_${route.route}")
            .clip(RoundedCornerShape(18.dp))
            .background(Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 4.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(background)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                Icon(
                    imageVector = route.icon(),
                    contentDescription = route.label,
                    modifier = Modifier
                        .size(18.dp),
                    tint = content,
                )
            }
            Text(
                text = route.label,
                style = MaterialTheme.typography.labelSmall,
                color = content,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

private fun AppRoute.icon(): ImageVector {
    return when (this) {
        AppRoute.Map -> Icons.Rounded.Explore
        AppRoute.Waypoints -> Icons.Rounded.Place
        AppRoute.Tracks -> Icons.Rounded.Route
        AppRoute.Packages -> Icons.Rounded.FolderOpen
        AppRoute.Diagnostics -> Icons.Rounded.BugReport
        AppRoute.Settings -> Icons.Rounded.Tune
    }
}
