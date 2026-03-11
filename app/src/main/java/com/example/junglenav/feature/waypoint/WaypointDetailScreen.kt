package com.example.junglenav.feature.waypoint

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.junglenav.core.model.Waypoint

@Composable
fun WaypointDetailScreen(
    waypoint: Waypoint,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = waypoint.name,
            style = MaterialTheme.typography.headlineSmall,
        )
        Text("Latitude ${waypoint.latitude}")
        Text("Longitude ${waypoint.longitude}")
        waypoint.note?.takeIf { it.isNotBlank() }?.let { note ->
            Text("Note $note")
        }
    }
}
