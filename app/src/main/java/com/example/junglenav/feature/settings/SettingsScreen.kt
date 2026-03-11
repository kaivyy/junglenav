package com.example.junglenav.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.junglenav.core.model.OperationMode

@Composable
fun SettingsScreen(
    operationMode: OperationMode,
    units: String,
    lowLightModeEnabled: Boolean,
    activePackageName: String?,
    onOperationModeSelected: (OperationMode) -> Unit,
    onUnitsSelected: (String) -> Unit,
    onLowLightModeChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = "Tune mission behavior and display comfort without leaving the shell.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
            )
        }

        SettingsBlock(
            title = "Operation profile",
            subtitle = "Changes update the field shell immediately.",
        ) {
            OperationMode.entries.chunked(2).forEach { rowModes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowModes.forEach { candidate ->
                        Button(
                            onClick = { onOperationModeSelected(candidate) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            enabled = candidate != operationMode,
                        ) {
                            Text(candidate.displayLabel())
                        }
                    }
                    if (rowModes.size == 1) {
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Text(
                text = "Current mode ${operationMode.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SettingsBlock(
            title = "Display comfort",
            subtitle = "Subdue colors for lower-light conditions.",
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Low-light mode",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = if (lowLightModeEnabled) {
                            "Muted palette is active."
                        } else {
                            "Bright expedition palette is active."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = lowLightModeEnabled,
                    onCheckedChange = onLowLightModeChanged,
                )
            }
        }

        SettingsBlock(
            title = "Units",
            subtitle = "Used for distance formatting and upcoming exports.",
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { onUnitsSelected("metric") },
                    modifier = Modifier.weight(1f),
                    enabled = units != "metric",
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text("Metric")
                }
                Button(
                    onClick = { onUnitsSelected("imperial") },
                    modifier = Modifier.weight(1f),
                    enabled = units != "imperial",
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text("Imperial")
                }
            }
            Text(
                text = "Current units ${units.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SettingsBlock(
            title = "Map package",
            subtitle = "Quick context from the active package inventory.",
        ) {
            Text(
                text = activePackageName ?: "No package active",
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}

@Composable
private fun SettingsBlock(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                content()
            },
        )
    }
}

private fun OperationMode.displayLabel(): String {
    return name
        .split('_')
        .joinToString(" ") { part ->
            part.lowercase().replaceFirstChar { it.uppercase() }
        }
}
