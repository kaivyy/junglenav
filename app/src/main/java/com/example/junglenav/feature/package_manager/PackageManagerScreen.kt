package com.example.junglenav.feature.package_manager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.junglenav.core.model.MapPackage
import com.example.junglenav.core.model.MapPackTrust
import com.example.junglenav.core.model.RemoteMapPackCatalogItem

@Composable
fun PackageManagerScreen(
    uiState: PackageManagerUiState,
    onImportPack: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onDownloadCatalogPack: (String) -> Unit,
    onActivatePackage: (String) -> Unit,
    onConfirmPendingActivation: () -> Unit,
    onDismissPendingActivation: () -> Unit,
    onDeletePackage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val installedPackageIds = uiState.items.mapTo(linkedSetOf(), MapPackage::id)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Packages",
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = "Import local bundles or pull mission packs from the remote catalog for offline field use.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
            )
        }

        OutlinedButton(
            onClick = onImportPack,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isImporting,
            shape = RoundedCornerShape(18.dp),
        ) {
            Text(if (uiState.isImporting) "Importing Bundle" else "Import .jnavpack")
        }

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("package_search_field"),
            label = { Text("Search mission catalog") },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
        )

        uiState.statusMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .testTag("package_list"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Remote Catalog",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (uiState.isCatalogRefreshing) {
                item {
                    Text(
                        text = "Refreshing mission catalog...",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else if (uiState.catalogItems.isEmpty()) {
                item {
                    Text(
                        text = uiState.catalogError ?: "No mission bundles match that search yet.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                items(uiState.catalogItems, key = RemoteMapPackCatalogItem::id) { catalogItem ->
                    CatalogCard(
                        catalogItem = catalogItem,
                        progressPercent = uiState.downloadProgressById[catalogItem.id],
                        isInstalled = installedPackageIds.contains(catalogItem.id),
                        onDownload = { onDownloadCatalogPack(catalogItem.id) },
                    )
                }
            }

            item {
                Text(
                    text = "Installed Bundles",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (uiState.items.isEmpty()) {
                item {
                    Text(
                        text = "No offline regions downloaded yet.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                items(uiState.items, key = MapPackage::id) { mapPackage ->
                    PackageCard(
                        mapPackage = mapPackage,
                        onActivate = { onActivatePackage(mapPackage.id) },
                        onDelete = { onDeletePackage(mapPackage.id) },
                    )
                }
            }
        }
    }

    uiState.pendingActivation?.let { pendingActivation ->
        AlertDialog(
            modifier = Modifier.testTag("activation_warning_dialog"),
            onDismissRequest = onDismissPendingActivation,
            title = {
                Text("Activate unverified bundle?")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${pendingActivation.packageName} is marked ${pendingActivation.trustLabel}.")
                    Text("Publisher ${pendingActivation.publisher ?: "Unknown"}")
                    Text("Checksum ${pendingActivation.checksum}")
                    Text(
                        "Only continue if you trust the source of this mission bundle.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                Button(onClick = onConfirmPendingActivation) {
                    Text("Activate")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismissPendingActivation) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun CatalogCard(
    catalogItem: RemoteMapPackCatalogItem,
    progressPercent: Int?,
    isInstalled: Boolean,
    onDownload: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = catalogItem.name,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = catalogItem.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Publisher ${catalogItem.publisher ?: "Unknown"} | ${catalogItem.trustHint.readableLabel()}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text("Estimated size ${catalogItem.estimatedSizeBytes / 1_000_000L} MB")
            Text("Zoom ${catalogItem.minZoom.toInt()}-${catalogItem.maxZoom.toInt()}")
            Text(
                "Layers ${catalogItem.layers.describe()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (progressPercent != null) {
                LinearProgressIndicator(
                    progress = { progressPercent / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Download $progressPercent%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            Button(
                onClick = onDownload,
                enabled = progressPercent == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("catalog_download_${catalogItem.id}"),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(
                    when {
                        progressPercent != null -> "Downloading"
                        isInstalled -> "Redownload Bundle"
                        else -> "Download Bundle"
                    },
                )
            }
        }
    }
}

@Composable
private fun PackageCard(
    mapPackage: MapPackage,
    onActivate: () -> Unit,
    onDelete: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "${mapPackage.source.readableLabel()} | ${mapPackage.trust.readableLabel()}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = if (mapPackage.isActive) "ACTIVE REGION" else "READY OFFLINE",
                style = MaterialTheme.typography.titleSmall,
                color = if (mapPackage.isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                },
            )
            Text(
                text = mapPackage.name,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text("Version ${mapPackage.version}")
            Text("Cached size ${mapPackage.sizeBytes / 1_000_000L} MB")
            Text(
                "Layers ${mapPackage.layers.describe()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                if (mapPackage.filePath.startsWith("file:")) {
                    "Style stored locally"
                } else {
                    "Style ${mapPackage.filePath}"
                },
            )
            Text(
                text = if (mapPackage.isActive) {
                    "Active on the map. Safe to test in offline conditions."
                } else {
                    "Stored locally and ready for activation."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onActivate,
                    enabled = !mapPackage.isActive,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text("Activate")
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

private fun com.example.junglenav.core.model.MapPackSource.readableLabel(): String {
    return when (this) {
        com.example.junglenav.core.model.MapPackSource.LOCAL_IMPORT -> "Imported"
        com.example.junglenav.core.model.MapPackSource.REMOTE_CATALOG -> "Catalog"
        com.example.junglenav.core.model.MapPackSource.BUNDLED_ASSET -> "Bundled"
    }
}

private fun MapPackTrust.readableLabel(): String {
    return when (this) {
        MapPackTrust.VERIFIED -> "Verified"
        MapPackTrust.UNVERIFIED -> "Unverified"
    }
}

private fun com.example.junglenav.core.model.MapPackLayerSet.describe(): String {
    val labels = buildList {
        if (topoVector) add("topo")
        if (hillshadeRaster) add("hillshade")
        if (imageryRaster) add("imagery")
    }
    return if (labels.isEmpty()) "none" else labels.joinToString()
}
