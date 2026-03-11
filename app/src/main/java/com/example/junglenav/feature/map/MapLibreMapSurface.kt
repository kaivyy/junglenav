package com.example.junglenav.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.Locale
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.visibility

@Composable
fun MapLibreMapSurface(
    uiState: MapUiState,
    viewportState: MapViewportState,
    onMapStyleLoaded: () -> Unit,
    onMapStyleLoadFailed: (String) -> Unit,
    mapHeight: androidx.compose.ui.unit.Dp = 310.dp,
    fullScreenMode: Boolean = false,
    showMetadata: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapViewResult = remember(context) {
        runCatching {
            MapView(context).apply {
                onCreate(null)
            }
        }
    }
    val mapView = mapViewResult.getOrNull()
    var mapLibreMap by remember(mapView) { mutableStateOf<MapLibreMap?>(null) }
    var lastStyleUri by remember { mutableStateOf<String?>(null) }

    DisposableEffect(mapView, lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView?.onStart()
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                Lifecycle.Event.ON_STOP -> mapView?.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView?.onDestroy()
                else -> Unit
            }
        }

        lifecycle.addObserver(observer)
        if (mapView != null) {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                mapView.onStart()
            }
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                mapView.onResume()
            }
        }

        onDispose {
            lifecycle.removeObserver(observer)
            mapLibreMap = null
        }
    }

    DisposableEffect(mapView) {
        if (mapView == null) {
            onMapStyleLoadFailed(
                mapViewResult.exceptionOrNull()?.message ?: "Map renderer unavailable on this device",
            )
            onDispose { }
        } else {
            val styleListener = MapView.OnDidFinishLoadingStyleListener {
                onMapStyleLoaded()
            }
            val failListener = MapView.OnDidFailLoadingMapListener { errorMessage ->
                onMapStyleLoadFailed(errorMessage.ifBlank { "Map style failed to load" })
            }

            mapView.addOnDidFinishLoadingStyleListener(styleListener)
            mapView.addOnDidFailLoadingMapListener(failListener)
            mapView.getMapAsync { map ->
                map.uiSettings.isCompassEnabled = false
                map.uiSettings.isRotateGesturesEnabled = false
                mapLibreMap = map
            }

            onDispose {
                mapView.removeOnDidFinishLoadingStyleListener(styleListener)
                mapView.removeOnDidFailLoadingMapListener(failListener)
            }
        }
    }

    LaunchedEffect(mapLibreMap, uiState.styleUri) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (lastStyleUri != uiState.styleUri) {
            lastStyleUri = uiState.styleUri
            map.setStyle(uiState.styleUri)
        }
    }

    LaunchedEffect(
        mapLibreMap,
        viewportState.centerLatitude,
        viewportState.centerLongitude,
        uiState.styleUri,
    ) {
        val map = mapLibreMap ?: return@LaunchedEffect
        map.easeCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(LatLng(viewportState.centerLatitude, viewportState.centerLongitude))
                    .zoom(if (uiState.isUsingFallbackStyle) 11.8 else 13.0)
                    .tilt(18.0)
                    .build(),
            ),
            700,
        )
    }

    LaunchedEffect(
        mapLibreMap,
        uiState.isMapReady,
        uiState.isTopoEnabled,
        uiState.isHillshadeEnabled,
        uiState.isImageryEnabled,
        uiState.availableLayers,
    ) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (!uiState.isMapReady) return@LaunchedEffect
        val style = map.style ?: return@LaunchedEffect

        style.setLayerVisibility(
            layerId = "imagery-layer",
            visible = uiState.availableLayers.imageryRaster && uiState.isImageryEnabled,
        )
        style.setLayerVisibility(
            layerId = "hillshade-layer",
            visible = uiState.availableLayers.hillshadeRaster && uiState.isHillshadeEnabled,
        )
        listOf("topo-fill", "topo-line", "topo-point").forEach { layerId ->
            style.setLayerVisibility(
                layerId = layerId,
                visible = uiState.availableLayers.topoVector && uiState.isTopoEnabled,
            )
        }
    }

    val containerModifier = if (fullScreenMode) {
        modifier.fillMaxSize()
    } else {
        modifier
            .fillMaxWidth()
            .height(mapHeight)
    }

    val mapContent: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxSize()) {
            if (mapView == null) {
                GradientFallbackSurface(viewportState = viewportState)
            } else {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            if (!fullScreenMode) {
                TopSignalChips(
                    mapSourceLabel = uiState.mapSourceLabel,
                    isUsingFallbackStyle = uiState.isUsingFallbackStyle,
                )
            }
            if (showMetadata) {
                BottomMetadata(
                    uiState = uiState,
                    viewportState = viewportState,
                )
            }

            if (!uiState.isMapReady) {
                LoadingBanner()
            }
            if (uiState.mapLoadError != null) {
                ErrorBanner(uiState.mapLoadError)
            }
        }
    }

    if (fullScreenMode) {
        Box(modifier = containerModifier) {
            mapContent()
        }
    } else {
        Card(
            modifier = containerModifier,
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        ) {
            mapContent()
        }
    }
}

@Composable
private fun GradientFallbackSurface(
    viewportState: MapViewportState,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.90f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.72f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.70f),
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Renderer standby",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                text = "Center ${formatCoordinate(viewportState.centerLatitude)}, ${formatCoordinate(viewportState.centerLongitude)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f),
            )
        }
    }
}

@Composable
private fun BoxScope.TopSignalChips(
    mapSourceLabel: String,
    isUsingFallbackStyle: Boolean,
) {
    Row(
        modifier = Modifier
            .padding(start = 20.dp, top = 20.dp)
            .align(Alignment.TopStart),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SignalChip(text = "MapLibre OpenGL")
        SignalChip(text = mapSourceLabel)
        if (isUsingFallbackStyle) {
            SignalChip(text = "Network style")
        }
    }
}

@Composable
private fun SignalChip(text: String) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.28f),
                shape = CircleShape,
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun BoxScope.BottomMetadata(
    uiState: MapUiState,
    viewportState: MapViewportState,
) {
    Surface(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(20.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
        tonalElevation = 6.dp,
        shadowElevation = 10.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = uiState.activePackageName ?: "OpenFreeMap Liberty",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Style ${viewportState.loadedStylePath ?: uiState.styleUri}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Center ${formatCoordinate(viewportState.centerLatitude)}, ${formatCoordinate(viewportState.centerLongitude)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (viewportState.isTrackVisible) "Track overlay live" else "Track overlay idle",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun BoxScope.LoadingBanner() {
    Surface(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 8.dp,
    ) {
        Text(
            text = "Loading terrain preview",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun BoxScope.ErrorBanner(message: String) {
    Surface(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(20.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.92f),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 8.dp,
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

private fun formatCoordinate(value: Double): String = String.format(Locale.US, "%.4f", value)

private fun Style.setLayerVisibility(
    layerId: String,
    visible: Boolean,
) {
    getLayer(layerId)?.setProperties(
        visibility(if (visible) Property.VISIBLE else Property.NONE),
    )
}
