# JungleNav MVP Foundation Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build the first usable JungleNav slice from the current Compose template: offline app shell, waypoint management, track recording, package inventory, position-mode visibility, and the technical scaffolding for later sensor fusion.

**Architecture:** Keep a single Android app module for now, but enforce package-level boundaries that mirror the PRD: `core`, `data`, `feature`, `engine`, and `system`. Put business rules behind interfaces and pure Kotlin reducers so the riskiest product logic is unit-testable before Android integrations such as MapLibre, location, sensors, and foreground services are fully wired.

**Tech Stack:** Kotlin, Jetpack Compose, Navigation Compose, Room, DataStore, Kotlin coroutines/Flow, kotlinx.serialization, Android location APIs, Android sensor APIs, foreground services, and a MapLibre-facing adapter boundary.

---

## Current Baseline

- The repository is an Android starter app with a single `app` module, Compose enabled, and only the default `MainActivity`.
- There is no existing domain, storage, navigation, or sensor code yet.
- The workspace is not currently a Git repository, so every "Commit" step below should be treated as a checkpoint unless Git is initialized first.
- The plan below intentionally targets Phase 1 plus the safest groundwork for Phase 2 from `prd.md`, because Phase 3 and Phase 4 depend on the earlier layers being stable first.

## Design Framing From Brainstorming

### Approaches considered

1. **Single app module with strict package boundaries** (recommended)
   - Fastest path from starter template to usable MVP.
   - Keeps refactors cheap while still creating clean seams for storage, map rendering, and positioning.
   - Best fit for the current codebase, which has no module structure yet.

2. **Full multi-module architecture immediately**
   - Strong boundaries from day one.
   - Too much overhead for a repo that still has only the starter screen.
   - High risk of spending time on Gradle plumbing before proving core field workflows.

3. **Map-first prototype**
   - Good for demo value.
   - Bad for delivery discipline because waypoints, tracks, confidence, and persistence become harder to test in isolation.
   - Would likely produce a flashy surface before the product logic is trustworthy.

### Chosen direction

Use approach 1. Build the app around testable product rules first, then plug platform-heavy adapters into those rules. This keeps the team moving toward a real MVP instead of a fragile prototype.

## Scope Assumptions

- Keep the current namespace `com.example.junglenav` until the product package ID is finalized.
- Start with one selected offline region at a time; multiple active regions can wait until later.
- Treat MapLibre integration as an adapter task, not as the center of the app architecture.
- Implement a truthful position state machine and diagnostics in MVP; do not promise full production-grade dead reckoning yet.
- Support local-only usage first. Cloud sync, team sharing, and mesh coordination stay out of scope for this plan.

## Milestones

- **Milestone A: App foundation**
  Build the package structure, core models, dependency baseline, and shell navigation so all later work lands in the right places.
- **Milestone B: Offline data workflows**
  Add Room, DataStore, waypoint CRUD, track persistence, export, and map-package inventory.
- **Milestone C: Field UX and position transparency**
  Add the map shell, status panels, operation modes, diagnostics, permission flows, and a position state machine.
- **Milestone D: Device integration hardening**
  Add foreground service behavior, location adapter wiring, acceptance tests, and field validation notes.

## Execution Rules

- Follow TDD for any domain rule, reducer, repository, mapper, or exporter.
- Prefer unit tests over instrumentation unless the behavior is Compose-only or Android-framework-only.
- Keep each task deployable on its own.
- Do not add visual complexity before the core data and state contracts exist.
- If MapLibre SDK integration blocks progress, finish the package manager and fake map adapter first, then replace the adapter implementation in a later pass.

## Task 1: Establish core models and dependency baseline

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Create: `app/src/main/java/com/example/junglenav/core/model/OperationMode.kt`
- Create: `app/src/main/java/com/example/junglenav/core/model/PositionMode.kt`
- Create: `app/src/main/java/com/example/junglenav/core/model/ConfidenceLevel.kt`
- Create: `app/src/main/java/com/example/junglenav/core/model/FieldStatus.kt`
- Test: `app/src/test/java/com/example/junglenav/core/model/FieldStatusTest.kt`

**Step 1: Write the failing test**

```kotlin
package com.example.junglenav.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FieldStatusTest {
    @Test
    fun emergencyModeStartsInFusedStateWithHighRefresh() {
        val status = FieldStatus.forMode(OperationMode.EMERGENCY)

        assertEquals(PositionMode.FUSED, status.mode)
        assertEquals(1_000L, status.locationIntervalMs)
        assertEquals(ConfidenceLevel.MEDIUM, status.confidenceLevel)
    }
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.core.model.FieldStatusTest"`

Expected: FAIL because `FieldStatus`, `OperationMode`, `PositionMode`, or `ConfidenceLevel` do not exist yet.

**Step 3: Write minimal implementation**

```kotlin
package com.example.junglenav.core.model

enum class OperationMode(val locationIntervalMs: Long) {
    PATROL(locationIntervalMs = 3_000L),
    SURVEY(locationIntervalMs = 1_500L),
    BATTERY_SAVER(locationIntervalMs = 10_000L),
    EMERGENCY(locationIntervalMs = 1_000L),
}

enum class PositionMode {
    NO_FIX,
    GNSS_LOCKED,
    FUSED,
    DR_ACTIVE,
    DR_LOW_CONF,
}

enum class ConfidenceLevel {
    LOW,
    MEDIUM,
    HIGH,
}

data class FieldStatus(
    val mode: PositionMode,
    val operationMode: OperationMode,
    val confidenceLevel: ConfidenceLevel,
    val locationIntervalMs: Long,
) {
    companion object {
        fun forMode(operationMode: OperationMode): FieldStatus {
            return FieldStatus(
                mode = PositionMode.FUSED,
                operationMode = operationMode,
                confidenceLevel = ConfidenceLevel.MEDIUM,
                locationIntervalMs = operationMode.locationIntervalMs,
            )
        }
    }
}
```

Also add baseline dependencies for:
- `androidx.navigation:navigation-compose`
- `androidx.lifecycle:lifecycle-viewmodel-compose`
- `androidx.room:room-runtime`
- `androidx.room:room-ktx`
- `androidx.room:room-compiler`
- `androidx.datastore:datastore-preferences`
- `org.jetbrains.kotlinx:kotlinx-serialization-json`
- `org.jetbrains.kotlinx:kotlinx-coroutines-test`

**Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.core.model.FieldStatusTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts app/src/main/java/com/example/junglenav/core/model app/src/test/java/com/example/junglenav/core/model/FieldStatusTest.kt
git commit -m "build: add JungleNav core model baseline"
```

If Git is not initialized yet, record a checkpoint note: `Task 1 complete - core models and dependencies added`.

## Task 2: Create the app shell and top-level navigation structure

**Files:**
- Modify: `app/src/main/java/com/example/junglenav/MainActivity.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/example/junglenav/JungleNavApp.kt`
- Create: `app/src/main/java/com/example/junglenav/app/AppRoute.kt`
- Create: `app/src/main/java/com/example/junglenav/app/AppContainer.kt`
- Create: `app/src/main/java/com/example/junglenav/feature/home/JungleNavAppRoot.kt`
- Create: `app/src/main/java/com/example/junglenav/feature/home/FieldStatusBar.kt`
- Test: `app/src/test/java/com/example/junglenav/app/AppRouteTest.kt`

**Step 1: Write the failing test**

```kotlin
package com.example.junglenav.app

import org.junit.Assert.assertEquals
import org.junit.Test

class AppRouteTest {
    @Test
    fun mapRouteIsTheDefaultStartDestination() {
        assertEquals(AppRoute.Map.route, AppRoute.startDestination)
    }
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.app.AppRouteTest"`

Expected: FAIL because `AppRoute` does not exist.

**Step 3: Write minimal implementation**

```kotlin
package com.example.junglenav.app

sealed class AppRoute(val route: String) {
    data object Map : AppRoute("map")
    data object Waypoints : AppRoute("waypoints")
    data object Tracks : AppRoute("tracks")
    data object Packages : AppRoute("packages")
    data object Diagnostics : AppRoute("diagnostics")
    data object Settings : AppRoute("settings")

    companion object {
        val startDestination: String = Map.route
    }
}
```

Create `JungleNavApp.kt` as `Application`, build an `AppContainer` that owns repository singletons, and replace the starter `Greeting` UI in `MainActivity.kt` with `JungleNavAppRoot(container = appContainer)`.

`FieldStatusBar` should show these always-visible MVP fields from the PRD:
- active target label placeholder
- distance placeholder
- current `PositionMode`
- current `ConfidenceLevel`
- recording state

**Step 4: Run test and do a smoke build**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.app.AppRouteTest"`

Expected: PASS

Run: `.\gradlew.bat assembleDebug`

Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/MainActivity.kt app/src/main/java/com/example/junglenav/JungleNavApp.kt app/src/main/java/com/example/junglenav/app app/src/main/java/com/example/junglenav/feature/home app/src/test/java/com/example/junglenav/app/AppRouteTest.kt app/src/main/AndroidManifest.xml
git commit -m "feat: add JungleNav app shell and navigation scaffold"
```

## Task 3: Add local storage with Room and DataStore

**Files:**
- Create: `app/src/main/java/com/example/junglenav/data/db/JungleNavDatabase.kt`
- Create: `app/src/main/java/com/example/junglenav/data/db/dao/WaypointDao.kt`
- Create: `app/src/main/java/com/example/junglenav/data/db/dao/TrackDao.kt`
- Create: `app/src/main/java/com/example/junglenav/data/db/dao/MapPackageDao.kt`
- Create: `app/src/main/java/com/example/junglenav/data/db/entity/WaypointEntity.kt`
- Create: `app/src/main/java/com/example/junglenav/data/db/entity/TrackSessionEntity.kt`
- Create: `app/src/main/java/com/example/junglenav/data/db/entity/TrackPointEntity.kt`
- Create: `app/src/main/java/com/example/junglenav/data/db/entity/MapPackageEntity.kt`
- Create: `app/src/main/java/com/example/junglenav/data/settings/SettingsRepository.kt`
- Create: `app/src/main/java/com/example/junglenav/data/settings/OperationModePreferences.kt`
- Test: `app/src/test/java/com/example/junglenav/data/settings/OperationModePreferencesTest.kt`
- Test: `app/src/androidTest/java/com/example/junglenav/data/db/JungleNavDatabaseTest.kt`

**Step 1: Write the failing tests**

```kotlin
package com.example.junglenav.data.settings

import com.example.junglenav.core.model.OperationMode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class OperationModePreferencesTest {
    @Test
    fun defaultsToPatrolMode() = runTest {
        val prefs = OperationModePreferences(storage = mutableMapOf())

        assertEquals(OperationMode.PATROL, prefs.readMode())
    }
}
```

```kotlin
package com.example.junglenav.data.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.junglenav.data.db.entity.WaypointEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JungleNavDatabaseTest {
    @Test
    fun waypointRoundTripPersistsFields() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = JungleNavDatabase.inMemory(context)
        val waypoint = WaypointEntity(
            id = "wp-1",
            name = "Camp",
            latitude = -6.2,
            longitude = 106.8,
            altitudeMeters = 120.0,
            note = "Base camp",
            createdAtEpochMs = 1L,
        )

        db.waypointDao().upsert(waypoint)

        assertEquals("Camp", db.waypointDao().observeAllOnce().single().name)
    }
}
```

**Step 2: Run tests to verify they fail**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.data.settings.OperationModePreferencesTest"`

Expected: FAIL because settings classes are missing.

Run: `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.junglenav.data.db.JungleNavDatabaseTest`

Expected: FAIL because Room schema and DAOs are missing.

**Step 3: Write minimal implementation**

Create the Room schema directly from the PRD:
- `waypoints`
- `tracks`
- `track_points`
- `map_packages`

Use `SettingsRepository` with DataStore to persist:
- operation mode
- units
- active package ID
- low-light mode toggle

Minimum DAO surface:

```kotlin
@Dao
interface WaypointDao {
    @Query("SELECT * FROM waypoints ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<WaypointEntity>>

    @Query("SELECT * FROM waypoints ORDER BY createdAtEpochMs DESC")
    suspend fun observeAllOnce(): List<WaypointEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WaypointEntity)

    @Query("DELETE FROM waypoints WHERE id = :id")
    suspend fun delete(id: String)
}
```

**Step 4: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.data.settings.OperationModePreferencesTest"`

Expected: PASS

Run: `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.junglenav.data.db.JungleNavDatabaseTest`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/data app/src/test/java/com/example/junglenav/data/settings/OperationModePreferencesTest.kt app/src/androidTest/java/com/example/junglenav/data/db/JungleNavDatabaseTest.kt
git commit -m "feat: add local persistence for JungleNav data"
```

## Task 4: Implement waypoint domain, repository, and UI

**Files:**
- Create: `app/src/main/java/com/example/junglenav/core/model/Waypoint.kt`
- Create: `app/src/main/java/com/example/junglenav/data/repository/WaypointRepository.kt`
- Create: `app/src/main/java/com/example/junglenav/data/repository/OfflineWaypointRepository.kt`
- Create: `app/src/main/java/com/example/junglenav/feature/waypoint/WaypointListViewModel.kt`
- Create: `app/src/main/java/com/example/junglenav/feature/waypoint/WaypointListScreen.kt`
- Create: `app/src/main/java/com/example/junglenav/feature/waypoint/WaypointEditorState.kt`
- Create: `app/src/main/java/com/example/junglenav/feature/waypoint/WaypointDetailScreen.kt`
- Test: `app/src/test/java/com/example/junglenav/feature/waypoint/WaypointListViewModelTest.kt`

**Step 1: Write the failing test**

```kotlin
package com.example.junglenav.feature.waypoint

import com.example.junglenav.core.model.Waypoint
import com.example.junglenav.data.repository.WaypointRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WaypointListViewModelTest {
    @Test
    fun addWaypointPushesItemIntoUiState() = runTest {
        val repo = FakeWaypointRepository()
        val viewModel = WaypointListViewModel(repo)

        viewModel.saveWaypoint(name = "Water Source", latitude = -6.0, longitude = 106.0)

        assertEquals("Water Source", viewModel.uiState.value.items.single().name)
    }
}

private class FakeWaypointRepository : WaypointRepository {
    private val state = MutableStateFlow<List<Waypoint>>(emptyList())

    override fun observeWaypoints(): Flow<List<Waypoint>> = state

    override suspend fun save(waypoint: Waypoint) {
        state.value = listOf(waypoint) + state.value
    }

    override suspend fun delete(id: String) = Unit
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.feature.waypoint.WaypointListViewModelTest"`

Expected: FAIL because `Waypoint`, `WaypointRepository`, or `WaypointListViewModel` do not exist.

**Step 3: Write minimal implementation**

The domain model should match the PRD:

```kotlin
data class Waypoint(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double?,
    val category: String?,
    val note: String?,
    val createdAtEpochMs: Long,
)
```

`WaypointListViewModel` should:
- expose `StateFlow<WaypointListUiState>`
- collect repository flow in `viewModelScope`
- create IDs locally
- support create and delete flows without network

`WaypointListScreen` should prioritize field usability:
- large create button
- list items with coordinates and quick navigate action
- no deep menu nesting

**Step 4: Run test and a build**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.feature.waypoint.WaypointListViewModelTest"`

Expected: PASS

Run: `.\gradlew.bat assembleDebug`

Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/core/model/Waypoint.kt app/src/main/java/com/example/junglenav/data/repository/WaypointRepository.kt app/src/main/java/com/example/junglenav/data/repository/OfflineWaypointRepository.kt app/src/main/java/com/example/junglenav/feature/waypoint app/src/test/java/com/example/junglenav/feature/waypoint/WaypointListViewModelTest.kt
git commit -m "feat: add offline waypoint workflow"
```

## Task 5: Implement track recording, retrace metadata, and export

**Files:**
- Create: `app/src/main/java/com/example/junglenav/core/model/TrackSession.kt`
- Create: `app/src/main/java/com/example/junglenav/core/model/TrackPoint.kt`
- Create: `app/src/main/java/com/example/junglenav/data/repository/TrackRepository.kt`
- Create: `app/src/main/java/com/example/junglenav/data/repository/OfflineTrackRepository.kt`
- Create: `app/src/main/java/com/example/junglenav/data/export/GpxExporter.kt`
- Create: `app/src/main/java/com/example/junglenav/feature/track/TrackViewModel.kt`
- Create: `app/src/main/java/com/example/junglenav/feature/track/TrackScreen.kt`
- Test: `app/src/test/java/com/example/junglenav/data/export/GpxExporterTest.kt`
- Test: `app/src/test/java/com/example/junglenav/feature/track/TrackViewModelTest.kt`

**Step 1: Write the failing tests**

```kotlin
package com.example.junglenav.data.export

import com.example.junglenav.core.model.PositionMode
import com.example.junglenav.core.model.TrackPoint
import org.junit.Assert.assertTrue
import org.junit.Test

class GpxExporterTest {
    @Test
    fun embedsPositionModeInsideTrackPointExtensions() {
        val xml = GpxExporter().export(
            name = "Patrol",
            points = listOf(
                TrackPoint(
                    latitude = -6.2,
                    longitude = 106.8,
                    altitudeMeters = 130.0,
                    speedMps = 1.3,
                    headingDegrees = 87.0,
                    recordedAtEpochMs = 1L,
                    positionMode = PositionMode.DR_ACTIVE,
                    confidence = 42,
                )
            )
        )

        assertTrue(xml.contains("<junglenav:position_mode>DR_ACTIVE</junglenav:position_mode>"))
    }
}
```

```kotlin
package com.example.junglenav.feature.track

import com.example.junglenav.core.model.TrackPoint
import com.example.junglenav.core.model.PositionMode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackViewModelTest {
    @Test
    fun startStopCycleProducesFinishedTrackState() = runTest {
        val viewModel = TrackViewModel(FakeTrackRepository())

        viewModel.startRecording()
        viewModel.onTrackPoint(
            TrackPoint(
                latitude = -6.2,
                longitude = 106.8,
                altitudeMeters = 120.0,
                speedMps = 1.0,
                headingDegrees = 90.0,
                recordedAtEpochMs = 1L,
                positionMode = PositionMode.GNSS_LOCKED,
                confidence = 92,
            )
        )
        viewModel.stopRecording()

        assertEquals(false, viewModel.uiState.value.isRecording)
        assertEquals(1, viewModel.uiState.value.lastSavedPointCount)
    }
}
```

**Step 2: Run tests to verify they fail**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.data.export.GpxExporterTest" --tests "com.example.junglenav.feature.track.TrackViewModelTest"`

Expected: FAIL because track models, exporter, and view model are missing.

**Step 3: Write minimal implementation**

The track layer must preserve what the PRD cares about:
- timestamps
- position mode at every point
- confidence at every point
- resumable session ID

Exporter contract:

```kotlin
class GpxExporter {
    fun export(name: String, points: List<TrackPoint>): String {
        val body = points.joinToString(separator = "") { point ->
            """
            <trkpt lat="${point.latitude}" lon="${point.longitude}">
              <ele>${point.altitudeMeters ?: 0.0}</ele>
              <extensions>
                <junglenav:position_mode>${point.positionMode.name}</junglenav:position_mode>
                <junglenav:confidence>${point.confidence}</junglenav:confidence>
              </extensions>
            </trkpt>
            """.trimIndent()
        }
        return "<gpx><trk><name>$name</name><trkseg>$body</trkseg></trk></gpx>"
    }
}
```

**Step 4: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.data.export.GpxExporterTest" --tests "com.example.junglenav.feature.track.TrackViewModelTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/core/model/TrackSession.kt app/src/main/java/com/example/junglenav/core/model/TrackPoint.kt app/src/main/java/com/example/junglenav/data/repository/TrackRepository.kt app/src/main/java/com/example/junglenav/data/repository/OfflineTrackRepository.kt app/src/main/java/com/example/junglenav/data/export/GpxExporter.kt app/src/main/java/com/example/junglenav/feature/track app/src/test/java/com/example/junglenav/data/export/GpxExporterTest.kt app/src/test/java/com/example/junglenav/feature/track/TrackViewModelTest.kt
git commit -m "feat: add track recording and GPX export"
```

## Task 6: Implement offline package inventory and map adapter boundary

**Files:**
- Create: `app/src/main/java/com/example/junglenav/core/model/MapPackage.kt`
- Create: `app/src/main/java/com/example/junglenav/data/repository/MapPackageRepository.kt`
- Create: `app/src/main/java/com/example/junglenav/data/repository/OfflineMapPackageRepository.kt`
- Create: `app/src/main/java/com/example/junglenav/feature/package_manager/PackageManagerViewModel.kt`
- Create: `app/src/main/java/com/example/junglenav/feature/package_manager/PackageManagerScreen.kt`
- Create: `app/src/main/java/com/example/junglenav/feature/map/MapViewportState.kt`
- Create: `app/src/main/java/com/example/junglenav/feature/map/MapSurface.kt`
- Create: `app/src/main/java/com/example/junglenav/feature/map/FakeOfflineMapSurface.kt`
- Test: `app/src/test/java/com/example/junglenav/feature/package_manager/PackageManagerViewModelTest.kt`

**Step 1: Write the failing test**

```kotlin
package com.example.junglenav.feature.package_manager

import com.example.junglenav.core.model.MapPackage
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PackageManagerViewModelTest {
    @Test
    fun activatePackageMarksOnlyOnePackageActive() = runTest {
        val viewModel = PackageManagerViewModel(
            repository = FakeMapPackageRepository(
                initial = listOf(
                    MapPackage(id = "a", name = "Region A", version = "1", sizeBytes = 100L, isActive = false, filePath = "a.bundle", checksum = "1"),
                    MapPackage(id = "b", name = "Region B", version = "1", sizeBytes = 100L, isActive = false, filePath = "b.bundle", checksum = "2"),
                )
            )
        )

        viewModel.activate("b")

        assertEquals("b", viewModel.uiState.value.items.single { it.isActive }.id)
    }
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.feature.package_manager.PackageManagerViewModelTest"`

Expected: FAIL because package models and repository are missing.

**Step 3: Write minimal implementation**

Build the package inventory first, then hide the actual renderer behind an interface:

```kotlin
interface MapSurface {
    fun loadStyle(packagePath: String)
    fun setCenter(latitude: Double, longitude: Double)
    fun showTrack(enabled: Boolean)
    fun showWaypoints(enabled: Boolean)
}
```

`PackageManagerViewModel` responsibilities:
- list installed packages
- activate exactly one package
- show size, version, checksum status
- expose import and delete actions even if import initially uses local file picker later

`FakeOfflineMapSurface` should render a placeholder card and current active package metadata so the rest of the UI can be built before the map SDK is fully integrated.

**Step 4: Run test and build**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.feature.package_manager.PackageManagerViewModelTest"`

Expected: PASS

Run: `.\gradlew.bat assembleDebug`

Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/core/model/MapPackage.kt app/src/main/java/com/example/junglenav/data/repository/MapPackageRepository.kt app/src/main/java/com/example/junglenav/data/repository/OfflineMapPackageRepository.kt app/src/main/java/com/example/junglenav/feature/package_manager app/src/main/java/com/example/junglenav/feature/map app/src/test/java/com/example/junglenav/feature/package_manager/PackageManagerViewModelTest.kt
git commit -m "feat: add offline package inventory and map surface boundary"
```

## Task 7: Build the map screen and field status UX

**Files:**
- Create: `app/src/main/java/com/example/junglenav/feature/map/MapScreen.kt`
- Create: `app/src/main/java/com/example/junglenav/feature/map/MapViewModel.kt`
- Create: `app/src/main/java/com/example/junglenav/feature/map/MapUiState.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/home/JungleNavAppRoot.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/home/FieldStatusBar.kt`
- Test: `app/src/test/java/com/example/junglenav/feature/map/MapViewModelTest.kt`

**Step 1: Write the failing test**

```kotlin
package com.example.junglenav.feature.map

import com.example.junglenav.core.model.ConfidenceLevel
import com.example.junglenav.core.model.PositionMode
import org.junit.Assert.assertEquals
import org.junit.Test

class MapViewModelTest {
    @Test
    fun statusBarUsesLowConfidenceWarningWhenDriftGrows() {
        val viewModel = MapViewModel()

        viewModel.onPositionStateChanged(
            mode = PositionMode.DR_LOW_CONF,
            confidenceLevel = ConfidenceLevel.LOW,
        )

        assertEquals("Low confidence", viewModel.uiState.value.confidenceLabel)
    }
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.feature.map.MapViewModelTest"`

Expected: FAIL because `MapViewModel` is missing.

**Step 3: Write minimal implementation**

The map screen should satisfy MVP UX requirements from the PRD:
- top field status strip
- central map surface
- quick actions for record, add waypoint, return to base
- always-visible active target section
- operation mode chip

Minimal view model contract:

```kotlin
data class MapUiState(
    val positionMode: PositionMode = PositionMode.NO_FIX,
    val confidenceLabel: String = "No fix",
    val activeTargetName: String? = null,
    val distanceMeters: Double? = null,
    val isRecording: Boolean = false,
)
```

Delay fancy overlays until after the renderer is stable. The first pass is about honest status communication, not visual polish.

**Step 4: Run test and instrumentation smoke**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.feature.map.MapViewModelTest"`

Expected: PASS

Run: `.\gradlew.bat assembleDebug`

Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/feature/map app/src/main/java/com/example/junglenav/feature/home/JungleNavAppRoot.kt app/src/main/java/com/example/junglenav/feature/home/FieldStatusBar.kt app/src/test/java/com/example/junglenav/feature/map/MapViewModelTest.kt
git commit -m "feat: add JungleNav map screen shell"
```

## Task 8: Implement position state machine, fusion facade, and diagnostics

**Files:**- Create: `app/src/main/java/com/example/junglenav/engine/positioning/LocationSample.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/positioning/PositionEstimate.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/positioning/LocationSource.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/fusion/PositionStateMachine.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/fusion/PositionFusionEngine.kt`
- Create: `app/src/main/java/com/example/junglenav/feature/diagnostics/DiagnosticsViewModel.kt`
- Create: `app/src/main/java/com/example/junglenav/feature/diagnostics/DiagnosticsScreen.kt`
- Test: `app/src/test/java/com/example/junglenav/engine/fusion/PositionStateMachineTest.kt`

**Step 1: Write the failing test**

```kotlin
package com.example.junglenav.engine.fusion

import com.example.junglenav.core.model.PositionMode
import com.example.junglenav.engine.positioning.LocationSample
import org.junit.Assert.assertEquals
import org.junit.Test

class PositionStateMachineTest {
    @Test
    fun staleGnssDropsIntoDeadReckoningState() {
        val machine = PositionStateMachine()
        machine.onLocationSample(
            LocationSample(
                latitude = -6.2,
                longitude = 106.8,
                accuracyMeters = 8f,
                speedMps = 1.2f,
                bearingDegrees = 90f,
                timestampEpochMs = 1_000L,
            )
        )

        machine.onTick(nowEpochMs = 20_000L)

        assertEquals(PositionMode.DR_ACTIVE, machine.currentState.mode)
    }
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.engine.fusion.PositionStateMachineTest"`

Expected: FAIL because the state machine and location models do not exist.

**Step 3: Write minimal implementation**

Keep the first version deterministic and transparent:

```kotlin
class PositionStateMachine(
    private val staleThresholdMs: Long = 15_000L,
    private val lowConfidenceThresholdMs: Long = 60_000L,
) {
    var currentState: PositionEstimate = PositionEstimate.noFix()
        private set

    fun onLocationSample(sample: LocationSample) {
        currentState = PositionEstimate.fromSample(sample)
    }

    fun onTick(nowEpochMs: Long) {
        val age = nowEpochMs - currentState.timestampEpochMs
        currentState = when {
            currentState.mode == PositionMode.NO_FIX -> currentState
            age >= lowConfidenceThresholdMs -> currentState.copy(mode = PositionMode.DR_LOW_CONF, confidence = 20)
            age >= staleThresholdMs -> currentState.copy(mode = PositionMode.DR_ACTIVE, confidence = 45)
            else -> currentState.copy(mode = PositionMode.GNSS_LOCKED)
        }
    }
}
```

`DiagnosticsViewModel` should expose:
- current operation mode
- current position mode
- age of last reliable fix
- active package ID
- sensor availability placeholders
- event log list

**Step 4: Run tests**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.engine.fusion.PositionStateMachineTest"`

Expected: PASS

Run: `.\gradlew.bat assembleDebug`

Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/engine app/src/main/java/com/example/junglenav/feature/diagnostics app/src/test/java/com/example/junglenav/engine/fusion/PositionStateMachineTest.kt
git commit -m "feat: add position state machine and diagnostics"
```

## Task 9: Add foreground tracking service, permissions, and battery policy

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/example/junglenav/system/background/TrackingService.kt`
- Create: `app/src/main/java/com/example/junglenav/system/permissions/PermissionCoordinator.kt`
- Create: `app/src/main/java/com/example/junglenav/system/battery/BatteryPolicy.kt`
- Create: `app/src/main/java/com/example/junglenav/system/background/TrackingNotificationFactory.kt`
- Test: `app/src/test/java/com/example/junglenav/system/battery/BatteryPolicyTest.kt`
- Test: `app/src/test/java/com/example/junglenav/system/permissions/PermissionCoordinatorTest.kt`

**Step 1: Write the failing tests**

```kotlin
package com.example.junglenav.system.battery

import com.example.junglenav.core.model.OperationMode
import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryPolicyTest {
    @Test
    fun batterySaverUsesSlowestLocationCadence() {
        assertEquals(10_000L, BatteryPolicy.forMode(OperationMode.BATTERY_SAVER).locationIntervalMs)
    }
}
```

```kotlin
package com.example.junglenav.system.permissions

import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionCoordinatorTest {
    @Test
    fun requiresBackgroundLocationOnlyWhenTrackRecordingEnabled() {
        val coordinator = PermissionCoordinator()
        assertEquals(false, coordinator.needsBackgroundLocation(isRecording = false))
        assertEquals(true, coordinator.needsBackgroundLocation(isRecording = true))
    }
}
```

**Step 2: Run tests to verify they fail**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.system.battery.BatteryPolicyTest" --tests "com.example.junglenav.system.permissions.PermissionCoordinatorTest"`

Expected: FAIL because service support classes are missing.

**Step 3: Write minimal implementation**

`TrackingService` first-pass behavior:
- starts in foreground when recording begins
- subscribes to `LocationSource`
- sends points into `TrackRepository`
- exposes a sticky notification with mode and confidence summary

`PermissionCoordinator` must separate:
- precise location
- notification permission
- background location for recording continuity

`BatteryPolicy` should map each `OperationMode` to:
- location interval
- sensor interval
- diagnostics verbosity

**Step 4: Run tests and manifest checks**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.system.battery.BatteryPolicyTest" --tests "com.example.junglenav.system.permissions.PermissionCoordinatorTest"`

Expected: PASS

Run: `.\gradlew.bat assembleDebug`

Expected: BUILD SUCCESSFUL

Manual verify:
- manifest contains location permissions
- manifest declares foreground service type
- app still launches

**Step 5: Commit**

```bash
git add app/src/main/AndroidManifest.xml app/src/main/java/com/example/junglenav/system app/src/test/java/com/example/junglenav/system/battery/BatteryPolicyTest.kt app/src/test/java/com/example/junglenav/system/permissions/PermissionCoordinatorTest.kt
git commit -m "feat: add tracking service permissions and battery policy"
```

## Task 10: Wire the first end-to-end MVP flow and acceptance checks

**Files:**
- Modify: `app/src/main/java/com/example/junglenav/app/AppContainer.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/home/JungleNavAppRoot.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/map/MapViewModel.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/track/TrackViewModel.kt`
- Create: `app/src/androidTest/java/com/example/junglenav/MvpSmokeTest.kt`
- Create: `docs/testing/field-alpha-checklist.md`

**Step 1: Write the failing instrumentation test**

```kotlin
package com.example.junglenav

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class MvpSmokeTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun createWaypointAndStartRecordingFromMainShell() {
        rule.onNodeWithText("Waypoints").performClick()
        rule.onNodeWithText("Add Waypoint").performClick()
        rule.onNodeWithText("Tracks").performClick()
        rule.onNodeWithText("Start Recording").performClick()
        rule.onNodeWithText("Recording").assertExists()
    }
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.junglenav.MvpSmokeTest`

Expected: FAIL because the full shell flow is not wired yet.

**Step 3: Write minimal integration**

Complete the dependency wiring so these flows work together:
- app starts on map route
- user can switch to waypoints, create a local waypoint, and see it in the list
- user can switch to tracks and start recording
- map status bar reflects recording state and active mode
- diagnostics screen reflects the same underlying state, not a separate copy

Create `docs/testing/field-alpha-checklist.md` with manual checks for:
- offline cold launch
- create waypoint without internet
- record track in background for 5 minutes
- switch operation modes and verify UI cadence changes
- force stale position and confirm `DR_ACTIVE` then `DR_LOW_CONF`

**Step 4: Run verification**

Run: `.\gradlew.bat testDebugUnitTest`

Expected: PASS

Run: `.\gradlew.bat connectedDebugAndroidTest`

Expected: PASS or only device-environment failures that are documented

Run: `.\gradlew.bat assembleDebug`

Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav app/src/androidTest/java/com/example/junglenav/MvpSmokeTest.kt docs/testing/field-alpha-checklist.md
git commit -m "feat: wire first JungleNav MVP flow"
```

## Follow-On Roadmap After This Plan

These are intentionally not part of the first execution batch, but they should be next once Task 10 is stable.

### Phase 3 follow-ons

- Replace `FakeOfflineMapSurface` with real MapLibre integration and active package loading.
- Add terrain query interfaces for elevation and slope lookup.
- Add plausibility checks that compare motion, barometer trend, and terrain features.
- Smooth re-lock transitions instead of jumping directly from stale state to `GNSS_LOCKED`.

### Phase 4 follow-ons

- Add local team markers and nearby waypoint sharing.
- Add richer terrain risk overlays.
- Add stride adaptation or motion classification only after field data exists.
- Explore camera-assisted correction only if target devices can support it without harming battery life.

## Testing Matrix

- `FieldStatusTest`, `AppRouteTest`, `WaypointListViewModelTest`, `TrackViewModelTest`, `GpxExporterTest`, `PackageManagerViewModelTest`, `MapViewModelTest`, `PositionStateMachineTest`, `BatteryPolicyTest`, and `PermissionCoordinatorTest` should stay in the unit suite.
- `JungleNavDatabaseTest` and `MvpSmokeTest` should stay in instrumentation because they validate Room and Compose shell behavior on device.
- Every milestone should end with `.\gradlew.bat assembleDebug`.
- Before any release candidate, run one outdoor manual pass with location and battery logging enabled.

## Risks To Watch During Execution

- Map SDK integration may pressure the architecture; keep `MapSurface` isolated so it can be swapped without rewriting feature logic.
- Foreground service behavior differs across Android versions; verify notification and background policy behavior on at least one Android 13+ device.
- The first position state machine should favor honesty over false precision. Show `DR_ACTIVE` and `DR_LOW_CONF` aggressively rather than hiding uncertainty.
- Track persistence must flush often enough to survive process death, but not so often that it drains battery. Measure this before optimizing.

## Definition of Done For This Plan

- App launches directly into a JungleNav shell instead of the starter Compose greeting.
- Waypoints can be created, listed, and deleted fully offline.
- A track can be started, stopped, persisted, and exported with position-mode metadata.
- One offline package can be activated and reflected in the map shell.
- UI always shows position mode, confidence, and recording status.
- Diagnostics screen exposes the same underlying system state.
- Unit tests pass, instrumentation tests are green on at least one device/emulator, and `docs/testing/field-alpha-checklist.md` exists.
