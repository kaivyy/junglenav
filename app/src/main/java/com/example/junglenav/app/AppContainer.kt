package com.example.junglenav.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.junglenav.core.model.ConfidenceLevel
import com.example.junglenav.core.model.FieldStatus
import com.example.junglenav.core.model.OperationMode
import com.example.junglenav.core.model.PositionMode
import com.example.junglenav.data.db.JungleNavDatabase
import com.example.junglenav.data.repository.MapPackageRepository
import com.example.junglenav.data.repository.OfflineMapPackageRepository
import com.example.junglenav.data.repository.OfflineTrackRepository
import com.example.junglenav.data.repository.OfflineWaypointRepository
import com.example.junglenav.data.repository.TrackRepository
import com.example.junglenav.data.repository.WaypointRepository
import com.example.junglenav.data.settings.SettingsRepository
import com.example.junglenav.engine.fusion.PositionFusionEngine
import com.example.junglenav.engine.matching.MapMatchingEngine
import com.example.junglenav.engine.matching.PathCandidate
import com.example.junglenav.engine.positioning.LocationSample
import com.example.junglenav.engine.positioning.NavigationFeedMode
import com.example.junglenav.engine.positioning.NavigationReadiness
import com.example.junglenav.engine.positioning.PositionEstimate
import com.example.junglenav.engine.terrain.SrtmTerrainProvider
import com.example.junglenav.engine.terrain.TerrainPlausibilityChecker
import com.example.junglenav.system.location.AndroidLocationSource
import com.example.junglenav.system.offline.AssetOfflineRegionService
import com.example.junglenav.system.offline.OfflineRegionDownloadService
import com.example.junglenav.system.offline.catalog.DefaultMapPackDownloadService
import com.example.junglenav.system.offline.catalog.MapPackDownloadService
import com.example.junglenav.system.offline.catalog.RemoteMapPackCatalogParser
import com.example.junglenav.system.offline.catalog.RemoteMapPackCatalogService
import com.example.junglenav.system.offline.catalog.SeededRemoteMapPackCatalogService
import com.example.junglenav.system.offline.demo.DemoMissionPackSeeder
import com.example.junglenav.system.offline.imports.LocalMapPackImportService
import com.example.junglenav.system.offline.imports.MapPackImportService
import com.example.junglenav.system.offline.jnavpack.JnavPackArchiveExtractor
import com.example.junglenav.system.offline.jnavpack.JnavPackInstaller
import com.example.junglenav.system.offline.jnavpack.JnavPackManifestParser
import com.example.junglenav.system.offline.jnavpack.JnavPackTrustResolver
import com.example.junglenav.system.offline.jnavpack.JnavPackValidator
import com.example.junglenav.system.offline.style.JnavStyleResolver
import com.example.junglenav.system.offline.tiles.JnavTileServerRegistry
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class AppContainer(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val database: JungleNavDatabase by lazy { JungleNavDatabase.create(appContext) }
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val positionFusionEngine = PositionFusionEngine()
    private val mapMatchingEngine = MapMatchingEngine()
    private val terrainProvider = SrtmTerrainProvider()
    private val terrainPlausibilityChecker = TerrainPlausibilityChecker()
    private val jnavPackManifestParser = JnavPackManifestParser()
    private val settingsDataStore by lazy {
        PreferenceDataStoreFactory.create(
            produceFile = { appContext.preferencesDataStoreFile("junglenav_settings.preferences_pb") },
        )
    }

    val waypointRepository: WaypointRepository by lazy {
        OfflineWaypointRepository(database.waypointDao())
    }

    val trackRepository: TrackRepository by lazy {
        OfflineTrackRepository(database.trackDao())
    }

    val mapPackageRepository: MapPackageRepository by lazy {
        OfflineMapPackageRepository(database.mapPackageDao())
    }

    val offlineRegionDownloadService: OfflineRegionDownloadService by lazy {
        AssetOfflineRegionService(appContext)
    }

    private val mapPackInstallRoot: File by lazy {
        File(appContext.filesDir, "installed-map-packs")
    }

    val jnavPackInstaller: JnavPackInstaller by lazy {
        JnavPackInstaller(
            extractor = JnavPackArchiveExtractor(),
            parser = jnavPackManifestParser,
            validator = JnavPackValidator(),
            trustResolver = JnavPackTrustResolver(),
            installRoot = mapPackInstallRoot,
        )
    }

    private val demoMissionPackSeeder: DemoMissionPackSeeder by lazy {
        DemoMissionPackSeeder(appContext)
    }

    val remoteMapPackCatalogService: RemoteMapPackCatalogService by lazy {
        SeededRemoteMapPackCatalogService(
            parser = RemoteMapPackCatalogParser(),
            catalogFileProvider = { demoMissionPackSeeder.ensureCatalogFile() },
        )
    }

    val mapPackDownloadService: MapPackDownloadService by lazy {
        DefaultMapPackDownloadService(
            context = appContext,
            installer = jnavPackInstaller,
        )
    }

    private val tileServerRegistry: JnavTileServerRegistry by lazy {
        JnavTileServerRegistry(appContext)
    }

    private val jnavStyleResolver: JnavStyleResolver by lazy {
        JnavStyleResolver(File(appContext.cacheDir, "resolved-jnav-styles"))
    }

    val localMapPackImportService: MapPackImportService by lazy {
        LocalMapPackImportService(
            context = appContext,
            installer = jnavPackInstaller,
        )
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(settingsDataStore)
    }

    private val _fieldStatus = MutableStateFlow(FieldStatus.forMode(OperationMode.PATROL))
    private val _activeTargetLabel = MutableStateFlow("No target")
    private val _distanceLabel = MutableStateFlow("--")
    private val _isRecording = MutableStateFlow(false)
    private val _units = MutableStateFlow("metric")
    private val _lowLightModeEnabled = MutableStateFlow(false)
    private val _navigationReadiness = MutableStateFlow(detectNavigationReadiness())
    private val _latestPositionEstimate = MutableStateFlow(PositionEstimate.noFix())
    private val _latestLocationSample = MutableStateFlow<LocationSample?>(null)
    private var liveLocationJob: Job? = null
    private var fusionTickerJob: Job? = null

    val fieldStatus: StateFlow<FieldStatus> = _fieldStatus.asStateFlow()
    val activeTargetLabel: StateFlow<String> = _activeTargetLabel.asStateFlow()
    val distanceLabel: StateFlow<String> = _distanceLabel.asStateFlow()
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    val units: StateFlow<String> = _units.asStateFlow()
    val lowLightModeEnabled: StateFlow<Boolean> = _lowLightModeEnabled.asStateFlow()
    val navigationReadiness: StateFlow<NavigationReadiness> = _navigationReadiness.asStateFlow()
    val latestPositionEstimate: StateFlow<PositionEstimate> = _latestPositionEstimate.asStateFlow()
    val latestLocationSample: StateFlow<LocationSample?> = _latestLocationSample.asStateFlow()

    init {
        applicationScope.launch {
            settingsRepository.operationMode.collectLatest { operationMode ->
                _fieldStatus.update {
                    it.copy(
                        operationMode = operationMode,
                        locationIntervalMs = operationMode.locationIntervalMs,
                    )
                }
                refreshNavigationInputs()
            }
        }
        applicationScope.launch {
            settingsRepository.units.collectLatest { units ->
                _units.value = units
            }
        }
        applicationScope.launch {
            settingsRepository.lowLightModeEnabled.collectLatest { enabled ->
                _lowLightModeEnabled.value = enabled
            }
        }
        startFusionTicker()
        refreshNavigationInputs()
    }

    fun setOperationMode(operationMode: OperationMode) {
        _fieldStatus.update {
            it.copy(
                operationMode = operationMode,
                locationIntervalMs = operationMode.locationIntervalMs,
            )
        }
        applicationScope.launch {
            settingsRepository.setOperationMode(operationMode)
        }
        refreshNavigationInputs()
    }

    fun setPositionMode(positionMode: PositionMode, confidenceLevel: ConfidenceLevel) {
        _fieldStatus.update {
            it.copy(
                mode = positionMode,
                confidenceLevel = confidenceLevel,
            )
        }
    }

    fun setRecording(recording: Boolean) {
        _isRecording.value = recording
    }

    fun setTarget(label: String, distanceLabel: String) {
        _activeTargetLabel.value = label
        _distanceLabel.value = distanceLabel
    }

    fun setUnits(units: String) {
        _units.value = units
        applicationScope.launch {
            settingsRepository.setUnits(units)
        }
    }

    fun setLowLightModeEnabled(enabled: Boolean) {
        _lowLightModeEnabled.value = enabled
        applicationScope.launch {
            settingsRepository.setLowLightModeEnabled(enabled)
        }
    }

    fun refreshNavigationInputs() {
        _navigationReadiness.value = detectNavigationReadiness()
        liveLocationJob?.cancel()

        if (!hasFineLocationPermission()) {
            _latestLocationSample.value = null
            publishPositionEstimate(PositionEstimate.noFix())
            return
        }

        val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return
        val source = AndroidLocationSource(
            locationManager = locationManager,
            minTimeMs = _fieldStatus.value.locationIntervalMs,
        )

        liveLocationJob = applicationScope.launch {
            source.observeLocationSamples().collectLatest { sample ->
                _latestLocationSample.value = sample

                val terrainPenalty = terrainProvider.sample(sample.latitude, sample.longitude)
                    ?.let(terrainPlausibilityChecker::penaltyFor)
                    ?: 0
                val matchedPosition = if (sample.accuracyMeters > 20f) {
                    mapMatchingEngine.match(
                        latitude = sample.latitude,
                        longitude = sample.longitude,
                        candidatePaths = listOf(
                            PathCandidate(
                                pathId = "nearest-trail",
                                latitude = sample.latitude.roundToGrid(),
                                longitude = sample.longitude.roundToGrid(),
                            ),
                        ),
                    )
                } else {
                    null
                }

                positionFusionEngine.onTerrainPenalty(terrainPenalty)
                positionFusionEngine.onMatchedPosition(matchedPosition)
                positionFusionEngine.onLocationSample(sample)
                publishPositionEstimate(positionFusionEngine.estimate.value)
                _navigationReadiness.value = detectNavigationReadiness(hasLiveGps = true)
            }
        }
    }

    private fun detectNavigationReadiness(): NavigationReadiness {
        return detectNavigationReadiness(hasLiveGps = false)
    }

    private fun detectNavigationReadiness(hasLiveGps: Boolean): NavigationReadiness {
        val packageManager = appContext.packageManager
        val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

        val hasGpsHardware = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS) &&
            locationManager != null
        val hasLocationPermission = hasFineLocationPermission()
        val inertialReady = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null &&
            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null
        val barometerReady = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE) != null

        return NavigationReadiness(
            gnssMode = when {
                !hasGpsHardware -> NavigationFeedMode.UNAVAILABLE
                !hasLocationPermission -> NavigationFeedMode.PENDING
                hasLiveGps -> NavigationFeedMode.LIVE
                else -> NavigationFeedMode.FALLBACK
            },
            inertialMode = if (inertialReady) NavigationFeedMode.LIVE else NavigationFeedMode.UNAVAILABLE,
            barometerMode = if (barometerReady) NavigationFeedMode.LIVE else NavigationFeedMode.UNAVAILABLE,
        )
    }

    private fun startFusionTicker() {
        if (fusionTickerJob != null) return
        fusionTickerJob = applicationScope.launch {
            while (isActive) {
                positionFusionEngine.onTick(System.currentTimeMillis())
                publishPositionEstimate(positionFusionEngine.estimate.value)
                delay(1_000L)
            }
        }
    }

    private fun publishPositionEstimate(estimate: PositionEstimate) {
        _latestPositionEstimate.value = estimate
        _fieldStatus.update {
            it.copy(
                mode = estimate.mode,
                confidenceLevel = estimate.confidence.toConfidenceLevel(),
            )
        }
    }

    private fun hasFineLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun Int.toConfidenceLevel(): ConfidenceLevel {
        return when {
            this >= 75 -> ConfidenceLevel.HIGH
            this >= 45 -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }
    }

    private fun Double.roundToGrid(): Double {
        return (this * 1_000.0).roundToInt() / 1_000.0
    }

    fun resolveOfflineStyleUri(mapPackage: com.example.junglenav.core.model.MapPackage): String? {
        val installRootPath = mapPackage.installRootPath ?: return null
        val manifestPath = mapPackage.manifestPath ?: return null
        val installRoot = File(installRootPath)
        val manifestFile = File(manifestPath)
        if (!installRoot.exists() || !manifestFile.exists()) {
            return null
        }

        val manifest = jnavPackManifestParser.parse(manifestFile.readText())
        val serverBaseUrl = tileServerRegistry.registerPack(
            packId = mapPackage.id,
            installRoot = installRoot,
        )
        return jnavStyleResolver.resolve(
            packId = mapPackage.id,
            installRoot = installRoot,
            manifest = manifest,
            serverBaseUrl = serverBaseUrl,
        ).styleUri
    }
}
