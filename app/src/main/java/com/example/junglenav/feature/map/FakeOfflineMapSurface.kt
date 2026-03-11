package com.example.junglenav.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

@Composable
fun FakeOfflineMapSurface(
    viewportState: MapViewportState,
    activePackageName: String?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.90f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.72f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.70f)
                        )
                    )
                )
        ) {
            FloatingLabel()
            SurfaceMetadata(
                activePackageName = activePackageName,
                viewportState = viewportState,
            )
        }
    }
}

@Composable
private fun BoxScope.FloatingLabel() {
    Box(
        modifier = Modifier
            .padding(start = 24.dp, top = 34.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.18f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.32f),
                shape = CircleShape
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Offline map surface",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun BoxScope.SurfaceMetadata(
    activePackageName: String?,
    viewportState: MapViewportState,
) {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .align(Alignment.BottomStart),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = activePackageName ?: "No active package selected",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = "Style ${viewportState.loadedStylePath ?: "not loaded"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
        )
        Text(
            text = "Center ${viewportState.centerLatitude}, ${viewportState.centerLongitude}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
        )
        Text(
            text = if (viewportState.isTrackVisible) "Track overlay enabled" else "Track overlay hidden",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = if (viewportState.areWaypointsVisible) "Waypoints visible" else "Waypoints hidden",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
