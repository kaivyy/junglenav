package com.example.junglenav.feature.package_manager

import android.util.Log
import com.example.junglenav.core.model.MapPackage
import com.example.junglenav.core.model.RemoteMapPackCatalogItem
import com.example.junglenav.data.repository.MapPackageRepository
import com.example.junglenav.system.offline.imports.ImportedMapPackSource
import com.example.junglenav.system.offline.imports.MapPackImportService
import com.example.junglenav.system.offline.catalog.MapPackDownloadService
import com.example.junglenav.system.offline.catalog.RemoteMapPackCatalogService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PackageManagerUiState(
    val items: List<MapPackage> = emptyList(),
    val catalogItems: List<RemoteMapPackCatalogItem> = emptyList(),
    val searchQuery: String = "",
    val downloadProgressById: Map<String, Int> = emptyMap(),
    val isImporting: Boolean = false,
    val isCatalogRefreshing: Boolean = false,
    val catalogError: String? = null,
    val pendingActivation: ActivationConfirmationState? = null,
    val statusMessage: String? = null,
) {
    val activePackage: MapPackage?
        get() = items.firstOrNull { it.isActive }
}

class PackageManagerViewModel(
    private val repository: MapPackageRepository,
    private val catalogService: RemoteMapPackCatalogService,
    private val downloadService: MapPackDownloadService,
    private val importService: MapPackImportService,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val logTag = "PackageManagerVM"
    private val _uiState = MutableStateFlow(PackageManagerUiState())
    val uiState: StateFlow<PackageManagerUiState> = _uiState.asStateFlow()
    private var allCatalogItems: List<RemoteMapPackCatalogItem> = emptyList()

    init {
        scope.launch {
            repository.observePackages().collectLatest { packages ->
                _uiState.update { state ->
                    state.copy(items = packages.sortedBy(MapPackage::name))
                }
            }
        }
        refreshCatalog()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                catalogItems = filterCatalog(query),
            )
        }
    }

    fun refreshCatalog() {
        if (_uiState.value.isCatalogRefreshing) return

        scope.launch {
            _uiState.update {
                it.copy(
                    isCatalogRefreshing = true,
                    catalogError = null,
                )
            }

            runCatching { catalogService.fetchCatalog() }
                .onSuccess { catalogItems ->
                    allCatalogItems = catalogItems.sortedBy(RemoteMapPackCatalogItem::name)
                    _uiState.update { state ->
                        state.copy(
                            catalogItems = filterCatalog(state.searchQuery),
                            isCatalogRefreshing = false,
                            statusMessage = if (allCatalogItems.isEmpty()) {
                                "No mission bundles available yet"
                            } else {
                                "Mission catalog ready"
                            },
                        )
                    }
                }
                .onFailure { throwable ->
                    Log.e(logTag, "Catalog fetch failed", throwable)
                    _uiState.update { state ->
                        state.copy(
                            isCatalogRefreshing = false,
                            catalogError = throwable.message ?: "Unable to refresh mission catalog",
                            statusMessage = throwable.message ?: "Unable to refresh mission catalog",
                        )
                    }
                }
        }
    }

    fun downloadCatalogPack(id: String) {
        val catalogItem = allCatalogItems.firstOrNull { it.id == id } ?: return
        if (_uiState.value.downloadProgressById.containsKey(id)) return

        scope.launch {
            _uiState.update {
                it.copy(
                    downloadProgressById = it.downloadProgressById + (id to 0),
                    statusMessage = "Downloading ${catalogItem.name}",
                )
            }

            runCatching {
                downloadService.download(catalogItem) { progress ->
                    _uiState.update { state ->
                        state.copy(
                            downloadProgressById = state.downloadProgressById + (id to progress.progressPercent),
                        )
                    }
                }
            }.onSuccess { mapPackage ->
                repository.save(mapPackage)
                handlePostInstallActivation(
                    mapPackage = mapPackage,
                    statusMessage = "${catalogItem.name} ready for offline use",
                    onProgressDone = { state ->
                        state.copy(downloadProgressById = state.downloadProgressById + (id to 100))
                    },
                )
            }.onFailure { throwable ->
                Log.e(logTag, "Download failed for ${catalogItem.id}", throwable)
                _uiState.update { state ->
                    state.copy(
                        downloadProgressById = state.downloadProgressById - id,
                        statusMessage = throwable.message ?: "Offline download failed",
                    )
                }
            }
        }
    }

    fun onImportCompleted(source: ImportedMapPackSource) {
        if (_uiState.value.isImporting) return

        scope.launch {
            _uiState.update {
                it.copy(
                    isImporting = true,
                    statusMessage = "Importing ${source.displayName ?: "bundle"}",
                )
            }

            runCatching {
                importService.import(source)
            }.onSuccess { mapPackage ->
                repository.save(mapPackage)
                handlePostInstallActivation(
                    mapPackage = mapPackage,
                    statusMessage = "${mapPackage.name} imported for offline use",
                    onProgressDone = { state ->
                        state.copy(isImporting = false)
                    },
                )
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(
                        isImporting = false,
                        statusMessage = throwable.message ?: "Bundle import failed",
                    )
                }
            }
        }
    }

    fun activate(id: String) {
        val mapPackage = _uiState.value.items.firstOrNull { it.id == id } ?: return
        if (mapPackage.requiresActivationConfirmation) {
            _uiState.update {
                it.copy(
                    pendingActivation = mapPackage.toActivationConfirmationState(),
                    statusMessage = "Review ${mapPackage.name} before activation",
                )
            }
            return
        }

        scope.launch {
            repository.activate(id)
            val activeName = _uiState.value.items.firstOrNull { it.id == id }?.name ?: "Offline region"
            _uiState.update {
                it.copy(
                    pendingActivation = null,
                    statusMessage = "$activeName activated for map use",
                )
            }
        }
    }

    fun confirmPendingActivation() {
        val pendingActivation = _uiState.value.pendingActivation ?: return
        scope.launch {
            repository.activate(pendingActivation.packageId)
            _uiState.update {
                it.copy(
                    pendingActivation = null,
                    statusMessage = "${pendingActivation.packageName} activated for map use",
                )
            }
        }
    }

    fun dismissPendingActivation() {
        _uiState.update {
            it.copy(
                pendingActivation = null,
                statusMessage = "Activation cancelled",
            )
        }
    }

    fun delete(id: String) {
        val mapPackage = _uiState.value.items.firstOrNull { it.id == id } ?: return

        scope.launch {
            downloadService.delete(mapPackage)
            repository.delete(id)
            _uiState.update { state ->
                state.copy(
                    downloadProgressById = state.downloadProgressById - id,
                    statusMessage = "${mapPackage.name} removed",
                )
            }
        }
    }

    private fun filterCatalog(query: String): List<RemoteMapPackCatalogItem> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) {
            return allCatalogItems.sortedBy(RemoteMapPackCatalogItem::name)
        }

        return allCatalogItems
            .filter { item ->
                item.name.contains(normalizedQuery, ignoreCase = true) ||
                    item.summary.contains(normalizedQuery, ignoreCase = true) ||
                    (item.publisher?.contains(normalizedQuery, ignoreCase = true) == true)
            }
            .sortedBy(RemoteMapPackCatalogItem::name)
    }

    private suspend fun handlePostInstallActivation(
        mapPackage: MapPackage,
        statusMessage: String,
        onProgressDone: (PackageManagerUiState) -> PackageManagerUiState,
    ) {
        if (mapPackage.requiresActivationConfirmation) {
            _uiState.update { state ->
                onProgressDone(
                    state.copy(
                        pendingActivation = mapPackage.toActivationConfirmationState(),
                        statusMessage = "$statusMessage. Review trust before activation.",
                    ),
                )
            }
            return
        }

        repository.activate(mapPackage.id)
        _uiState.update { state ->
            onProgressDone(
                state.copy(
                    pendingActivation = null,
                    statusMessage = statusMessage,
                ),
            )
        }
    }
}

private fun MapPackage.toActivationConfirmationState(): ActivationConfirmationState {
    return ActivationConfirmationState(
        packageId = id,
        packageName = name,
        publisher = publisher,
        checksum = checksum,
        trustLabel = trust.readableLabel(),
    )
}

private fun com.example.junglenav.core.model.MapPackTrust.readableLabel(): String {
    return when (this) {
        com.example.junglenav.core.model.MapPackTrust.VERIFIED -> "Verified"
        com.example.junglenav.core.model.MapPackTrust.UNVERIFIED -> "Unverified"
    }
}
