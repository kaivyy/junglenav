# JungleNav Hybrid Navigation Roadmap Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Evolve the current JungleNav MVP into a field-testable hybrid navigation stack that truthfully supports GNSS + IMU + barometer ingestion, heading stabilization, dead reckoning, adaptive sampling, terrain-aware plausibility checks, and a clean upgrade path toward Kalman-grade fusion and motion classification.

**Architecture:** Keep the single `app` module, but extend the existing boundaries so raw Android adapters live in `system/*`, normalized samples and estimators live in `engine/*`, repositories stay in `data/*`, and the UI only consumes honest navigation state. Build the pipeline in this order: raw GNSS and sensor samples -> heading and motion inference -> dead reckoning -> confidence scoring -> map matching and terrain correction -> UI and diagnostics.

**Tech Stack:** Kotlin, coroutines/Flow, Android `LocationManager` and `GnssStatus`, Android `SensorManager`, Room, DataStore, MapLibre OpenGL, unit tests, Android instrumentation tests, and replayable field-log fixtures.

---

## Current Baseline

- The repo already has a working Compose shell, top-level navigation, MapLibre rendering, waypoint management, track recording, package inventory, settings, diagnostics, and an Android-tested launcher flow.
- The positioning layer is still an MVP scaffold. `PositionFusionEngine` is a thin wrapper around `PositionStateMachine`, not a real hybrid sensor-fusion engine.
- `DiagnosticsViewModel` still reports placeholder sensor readiness, so the app is not yet truthful about live hardware.
- `BatteryPolicy` already supports `PATROL`, `SURVEY`, `BATTERY_SAVER`, and `EMERGENCY`, but it is not yet driven by actual motion state or sensor workload.
- The workspace still appears to be outside Git, so each commit step below should be treated as a checkpoint unless a repository is initialized first.

## Brainstorming Framing

### Approaches considered

1. **Heuristic-first hybrid pipeline with clean upgrade seams** (recommended)
   - Build the raw data contracts, weighted fusion, complementary heading stabilization, dead reckoning, and terrain plausibility first.
   - Add an interface seam so the weighted solver can later be replaced by an EKF/Kalman solver without rewriting the UI or Android adapters.

2. **Kalman-first implementation immediately**
   - Attractive on paper because it matches the long-term goal.
   - Too risky right now because the project does not yet have stable raw sensor adapters, replay logs, or confidence instrumentation.

3. **AI-first motion classification and visual navigation upfront**
   - Good long-term differentiation.
   - Bad first step because there is no labeled motion dataset, no replay harness, and no camera navigation boundary yet.

### Chosen direction

Use approach 1. Build a truthful hybrid pipeline that can be tested in the field and replayed offline, then add Kalman-grade fusion and motion classification only after the sample stream, diagnostics, and correction layers are stable.

## Capability Mapping From The User Requirements

| Requirement | Roadmap phase | Truthful status after this roadmap |
|---|---|---|
| Multi-sensor fusion (`GPS`, accelerometer, gyroscope, magnetometer, barometer) | Phase B | Alpha implementation with upgrade seam for EKF/Kalman |
| Motion classification for phone carry differences | Phase D | Future work after logs and replay tooling exist |
| Gyro-stabilized heading with magnetometer correction | Phase B | Implemented and testable |
| Drift reduction via map matching | Phase C | Implemented with plausibility correction |
| Battery drain control via adaptive sampling and operation modes | Phase B | Implemented and testable |
| Terrain ambiguity reduction with elevation and slope context | Phase C | Initial terrain-aware correction implemented |
| Multi-GNSS awareness | Phase A/B | Basic metadata and constellation-aware confidence weighting |
| Visual navigation / SLAM | Phase D | Research spike only, not productized |
| Full hybrid pipeline | Phase B/C | Field alpha, not final production accuracy |

## Milestones

- **Milestone A: Truthful sensor and GNSS plumbing**
  Normalize raw samples, expose live hardware readiness in diagnostics, and create replayable adapters.
- **Milestone B: Alpha hybrid fusion**
  Add heading stabilization, dead reckoning, adaptive sampling, confidence scoring, and a replaceable fusion solver.
- **Milestone C: Terrain-aware correction**
  Add terrain queries, path plausibility, and map matching so drift can be corrected instead of merely displayed.
- **Milestone D: Advanced adaptation**
  Add motion classification, richer GNSS weighting, and visual navigation research only after the alpha pipeline is stable.

## Execution Rules

- Follow TDD for every pure Kotlin rule, reducer, scorer, estimator, and classifier.
- Use unit tests for all fusion, dead reckoning, confidence, map-matching, and terrain rules.
- Use Android instrumentation only for adapter registration, sensor permission flows, and end-to-end UI checks.
- Keep each task deployable and diagnosable on its own.
- Preserve honest naming in the UI. If the app is using fallback style, fallback heading, or replayed data, say that explicitly.

## Phase A: Truthful Sensor And GNSS Plumbing

### Task 1: Expand positioning contracts for hybrid navigation

**Files:**
- Modify: `app/src/main/java/com/example/junglenav/engine/positioning/LocationSample.kt`
- Modify: `app/src/main/java/com/example/junglenav/engine/positioning/PositionEstimate.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/positioning/GnssStatusSample.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/sensors/InertialSample.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/sensors/BarometerSample.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/positioning/NavigationSource.kt`
- Test: `app/src/test/java/com/example/junglenav/engine/positioning/PositionEstimateTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun fromSampleCarriesNavigationSourceAndGnssMetadata() {
    val sample = LocationSample(
        latitude = -6.2,
        longitude = 106.8,
        accuracyMeters = 8f,
        speedMps = 1.2f,
        bearingDegrees = 45f,
        timestampEpochMs = 10_000L,
        gnssSatellitesUsed = 14,
        gnssConstellations = setOf("GPS", "GLONASS", "Galileo"),
    )

    val estimate = PositionEstimate.fromSample(sample)

    assertEquals(NavigationSource.GNSS, estimate.source)
    assertEquals(14, estimate.gnssSatellitesUsed)
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.engine.positioning.PositionEstimateTest"`

Expected: FAIL because the new fields and types do not exist yet.

**Step 3: Write minimal implementation**

- Add GNSS metadata to `LocationSample`.
- Add `source`, `headingAccuracyDegrees`, `driftSeconds`, and GNSS metadata to `PositionEstimate`.
- Create raw sensor sample types for inertial and barometer streams with timestamps.

**Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.engine.positioning.PositionEstimateTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/engine/positioning app/src/main/java/com/example/junglenav/engine/sensors app/src/test/java/com/example/junglenav/engine/positioning/PositionEstimateTest.kt
git commit -m "feat: expand navigation sample contracts"
```

If Git is not initialized, record: `Task 1 complete - hybrid navigation contracts expanded`.

### Task 2: Add replayable source interfaces and Android adapter seams

**Files:**
- Modify: `app/src/main/java/com/example/junglenav/engine/positioning/LocationSource.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/sensors/InertialSource.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/sensors/BarometerSource.kt`
- Create: `app/src/main/java/com/example/junglenav/system/location/AndroidLocationSource.kt`
- Create: `app/src/main/java/com/example/junglenav/system/location/AndroidGnssStatusSource.kt`
- Create: `app/src/main/java/com/example/junglenav/system/sensors/AndroidInertialSource.kt`
- Create: `app/src/main/java/com/example/junglenav/system/sensors/AndroidBarometerSource.kt`
- Create: `app/src/main/java/com/example/junglenav/system/replay/ReplayLocationSource.kt`
- Test: `app/src/test/java/com/example/junglenav/system/replay/ReplayLocationSourceTest.kt`
- Test: `app/src/androidTest/java/com/example/junglenav/system/location/AndroidLocationSourceSmokeTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun replaySourceEmitsSamplesInRecordedOrder() = runBlocking {
    val source = ReplayLocationSource(
        samples = listOf(
            RecordedLocationSample(timestampEpochMs = 100L, latitude = 1.0, longitude = 2.0),
            RecordedLocationSample(timestampEpochMs = 200L, latitude = 3.0, longitude = 4.0),
        ),
    )

    val firstSample = source.observeLocationSamples().first()

    assertEquals(100L, firstSample.timestampEpochMs)
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.system.replay.ReplayLocationSourceTest"`

Expected: FAIL because replay sources do not exist.

**Step 3: Write minimal implementation**

- Turn `LocationSource` into a seam that can expose both live and replayed data.
- Add matching interfaces for inertial and barometer streams.
- Create Android adapter classes that wrap `LocationManager`, `GnssStatus.Callback`, and `SensorManager`.

**Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.system.replay.ReplayLocationSourceTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/engine/positioning/LocationSource.kt app/src/main/java/com/example/junglenav/engine/sensors app/src/main/java/com/example/junglenav/system/location app/src/main/java/com/example/junglenav/system/sensors app/src/main/java/com/example/junglenav/system/replay app/src/test/java/com/example/junglenav/system/replay/ReplayLocationSourceTest.kt app/src/androidTest/java/com/example/junglenav/system/location/AndroidLocationSourceSmokeTest.kt
git commit -m "feat: add live and replay sensor source seams"
```

If Git is not initialized, record: `Task 2 complete - live and replay sources added`.

### Task 3: Make diagnostics tell the truth about active hardware and source quality

**Files:**
- Modify: `app/src/main/java/com/example/junglenav/feature/diagnostics/DiagnosticsViewModel.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/diagnostics/DiagnosticsScreen.kt`
- Modify: `app/src/main/java/com/example/junglenav/app/AppContainer.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/positioning/NavigationReadiness.kt`
- Test: `app/src/test/java/com/example/junglenav/feature/diagnostics/DiagnosticsViewModelTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun syncStateShowsHardwareReadinessInsteadOfPendingPlaceholders() {
    val viewModel = DiagnosticsViewModel()

    viewModel.syncSensorReadiness(
        gnssReady = true,
        inertialReady = true,
        barometerReady = false,
    )

    assertTrue(viewModel.uiState.value.sensorAvailability.contains("GNSS adapter ready"))
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.feature.diagnostics.DiagnosticsViewModelTest"`

Expected: FAIL because readiness syncing does not exist.

**Step 3: Write minimal implementation**

- Replace the placeholder sensor strings with real readiness rows driven by `AppContainer`.
- Track whether data is `live`, `replay`, `fallback`, or `pending`.
- Surface GNSS constellation count and last reliable fix age when available.

**Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.feature.diagnostics.DiagnosticsViewModelTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/feature/diagnostics app/src/main/java/com/example/junglenav/app/AppContainer.kt app/src/main/java/com/example/junglenav/engine/positioning/NavigationReadiness.kt app/src/test/java/com/example/junglenav/feature/diagnostics/DiagnosticsViewModelTest.kt
git commit -m "feat: expose truthful navigation readiness"
```

If Git is not initialized, record: `Task 3 complete - diagnostics now report live readiness`.

## Phase B: Alpha Hybrid Fusion

### Task 4: Implement gyro-stabilized heading with magnetometer correction

**Files:**
- Create: `app/src/main/java/com/example/junglenav/engine/fusion/HeadingFusionEngine.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/fusion/HeadingEstimate.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/fusion/MagneticCalibrationGate.kt`
- Test: `app/src/test/java/com/example/junglenav/engine/fusion/HeadingFusionEngineTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun gyroDominatesShortTermHeadingWhileMagnetometerCorrectsSlowly() {
    val engine = HeadingFusionEngine()

    val estimate = engine.update(
        inertialSample = InertialSample.gyroTurn(yawRateDegPerSec = 10f, timestampEpochMs = 1000L),
        magneticHeadingDegrees = 80f,
    )

    assertTrue(estimate.sourceLabel.contains("gyro"))
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.engine.fusion.HeadingFusionEngineTest"`

Expected: FAIL because heading fusion classes do not exist.

**Step 3: Write minimal implementation**

- Add a complementary-filter style heading engine.
- Use gyro integration for short-term heading continuity.
- Use magnetometer only as low-frequency correction when readings are sane.

**Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.engine.fusion.HeadingFusionEngineTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/engine/fusion/HeadingFusionEngine.kt app/src/main/java/com/example/junglenav/engine/fusion/HeadingEstimate.kt app/src/main/java/com/example/junglenav/engine/fusion/MagneticCalibrationGate.kt app/src/test/java/com/example/junglenav/engine/fusion/HeadingFusionEngineTest.kt
git commit -m "feat: add gyro stabilized heading fusion"
```

If Git is not initialized, record: `Task 4 complete - heading stabilization added`.

### Task 5: Add dead reckoning, confidence decay, and adaptive sampling

**Files:**
- Create: `app/src/main/java/com/example/junglenav/engine/deadreckoning/DeadReckoningEngine.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/deadreckoning/StepStrideModel.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/fusion/ConfidenceScorer.kt`
- Modify: `app/src/main/java/com/example/junglenav/system/battery/BatteryPolicy.kt`
- Create: `app/src/main/java/com/example/junglenav/system/battery/AdaptiveSamplingPolicy.kt`
- Test: `app/src/test/java/com/example/junglenav/engine/deadreckoning/DeadReckoningEngineTest.kt`
- Test: `app/src/test/java/com/example/junglenav/system/battery/AdaptiveSamplingPolicyTest.kt`

**Step 1: Write the failing tests**

```kotlin
@Test
fun confidenceDropsAsDeadReckoningRunsWithoutFreshGnss() {
    val engine = DeadReckoningEngine()

    val estimate = engine.projectWithoutGnss(
        elapsedMs = 20_000L,
        stepCountDelta = 24,
        headingDegrees = 90f,
    )

    assertTrue(estimate.confidence < 60)
}
```

**Step 2: Run tests to verify they fail**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.engine.deadreckoning.DeadReckoningEngineTest" --tests "com.example.junglenav.system.battery.AdaptiveSamplingPolicyTest"`

Expected: FAIL because the DR engine and adaptive sampling policy do not exist.

**Step 3: Write minimal implementation**

- Create a short-horizon dead reckoning engine driven by step delta, heading, and last reliable fix.
- Add a confidence scorer that decays as DR time, heading uncertainty, and GNSS staleness rise.
- Create an adaptive sampling policy that combines operation mode with coarse motion state such as `still`, `walking`, and `running`.

**Step 4: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.engine.deadreckoning.DeadReckoningEngineTest" --tests "com.example.junglenav.system.battery.AdaptiveSamplingPolicyTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/engine/deadreckoning app/src/main/java/com/example/junglenav/engine/fusion/ConfidenceScorer.kt app/src/main/java/com/example/junglenav/system/battery app/src/test/java/com/example/junglenav/engine/deadreckoning/DeadReckoningEngineTest.kt app/src/test/java/com/example/junglenav/system/battery/AdaptiveSamplingPolicyTest.kt
git commit -m "feat: add dead reckoning and adaptive sampling"
```

If Git is not initialized, record: `Task 5 complete - dead reckoning and adaptive sampling added`.

### Task 6: Replace the thin fusion facade with a real hybrid pipeline

**Files:**
- Modify: `app/src/main/java/com/example/junglenav/engine/fusion/PositionFusionEngine.kt`
- Modify: `app/src/main/java/com/example/junglenav/engine/fusion/PositionStateMachine.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/fusion/FusionSolver.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/fusion/WeightedFusionSolver.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/fusion/HybridNavigationSnapshot.kt`
- Test: `app/src/test/java/com/example/junglenav/engine/fusion/PositionFusionEngineTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun degradedGnssFallsBackToFusedThenDeadReckoningInsteadOfFreezing() {
    val engine = PositionFusionEngine()

    engine.onTick(nowEpochMs = 18_000L)

    assertTrue(engine.estimate.value.mode.name in setOf("FUSED", "DR_ACTIVE"))
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.engine.fusion.PositionFusionEngineTest"`

Expected: FAIL because the current engine is only a thin wrapper.

**Step 3: Write minimal implementation**

- Introduce `FusionSolver` as the seam between the orchestrator and the math implementation.
- Build `WeightedFusionSolver` to combine GNSS freshness, heading quality, stride-based DR, and barometer trend.
- Update `PositionStateMachine` so it reacts to actual fusion confidence and source quality, not only elapsed time.

**Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.engine.fusion.PositionFusionEngineTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/engine/fusion app/src/test/java/com/example/junglenav/engine/fusion/PositionFusionEngineTest.kt
git commit -m "feat: add hybrid fusion pipeline"
```

If Git is not initialized, record: `Task 6 complete - hybrid fusion pipeline added`.

### Task 7: Add an EKF and Kalman-ready solver seam, but do not switch by default yet

**Files:**
- Create: `app/src/main/java/com/example/junglenav/engine/fusion/KalmanFusionSolver.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/fusion/FusionReplayScenario.kt`
- Modify: `app/src/main/java/com/example/junglenav/engine/fusion/FusionSolver.kt`
- Test: `app/src/test/java/com/example/junglenav/engine/fusion/KalmanFusionSolverTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun kalmanSolverProducesStableEstimateForReplayScenario() {
    val solver = KalmanFusionSolver()

    val estimate = solver.solve(FusionReplayScenario.basicForestWalk())

    assertTrue(estimate.confidence >= 0)
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.engine.fusion.KalmanFusionSolverTest"`

Expected: FAIL because the Kalman solver does not exist.

**Step 3: Write minimal implementation**

- Add a solver class behind the same `FusionSolver` interface.
- Keep it off by default until replay logs and field data are available.
- Document that `WeightedFusionSolver` remains the production default until the Kalman branch is proven.

**Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.engine.fusion.KalmanFusionSolverTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/engine/fusion/KalmanFusionSolver.kt app/src/main/java/com/example/junglenav/engine/fusion/FusionReplayScenario.kt app/src/main/java/com/example/junglenav/engine/fusion/FusionSolver.kt app/src/test/java/com/example/junglenav/engine/fusion/KalmanFusionSolverTest.kt
git commit -m "feat: add kalman solver seam"
```

If Git is not initialized, record: `Task 7 complete - Kalman-ready seam added`.

## Phase C: Terrain-Aware Correction

### Task 8: Add terrain query interfaces and SRTM-backed plausibility checks

**Files:**
- Create: `app/src/main/java/com/example/junglenav/engine/terrain/TerrainSample.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/terrain/TerrainProvider.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/terrain/SrtmTerrainProvider.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/terrain/TerrainPlausibilityChecker.kt`
- Test: `app/src/test/java/com/example/junglenav/engine/terrain/TerrainPlausibilityCheckerTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun estimateInRiverChannelIsMarkedImplausibleWhenNearbyTrailExists() {
    val checker = TerrainPlausibilityChecker()

    val plausible = checker.isPlausible(
        elevation = 120f,
        slopeDegrees = 3f,
        onRiver = true,
        onTrail = false,
    )

    assertFalse(plausible)
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.engine.terrain.TerrainPlausibilityCheckerTest"`

Expected: FAIL because the terrain checker does not exist.

**Step 3: Write minimal implementation**

- Create a terrain provider interface that can answer elevation, slope, ridge, river, and vegetation hints.
- Build a first SRTM-backed provider for elevation and slope.
- Add a plausibility checker that can reject impossible positions and return penalty scores instead of silently snapping.

**Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.engine.terrain.TerrainPlausibilityCheckerTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/engine/terrain app/src/test/java/com/example/junglenav/engine/terrain/TerrainPlausibilityCheckerTest.kt
git commit -m "feat: add terrain plausibility checks"
```

If Git is not initialized, record: `Task 8 complete - terrain plausibility added`.

### Task 9: Add map matching and correction labels

**Files:**
- Create: `app/src/main/java/com/example/junglenav/engine/matching/MapMatchingEngine.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/matching/MatchedPosition.kt`
- Modify: `app/src/main/java/com/example/junglenav/engine/fusion/PositionFusionEngine.kt`
- Modify: `app/src/main/java/com/example/junglenav/engine/positioning/PositionEstimate.kt`
- Test: `app/src/test/java/com/example/junglenav/engine/matching/MapMatchingEngineTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun offTrailEstimateIsPulledTowardNearestValidPath() {
    val engine = MapMatchingEngine()

    val matched = engine.match(
        latitude = -6.2001,
        longitude = 106.8012,
        candidatePaths = listOf(PathCandidate("ridge-trail", -6.2000, 106.8010)),
    )

    assertEquals("ridge-trail", matched.pathId)
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.engine.matching.MapMatchingEngineTest"`

Expected: FAIL because the matcher does not exist.

**Step 3: Write minimal implementation**

- Build a nearest-path matcher with confidence penalties instead of hard jumps.
- Feed matched outputs back into `PositionFusionEngine`.
- Extend `PositionEstimate` so the UI can show whether the position is `raw fused`, `map matched`, or `terrain corrected`.

**Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.engine.matching.MapMatchingEngineTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/engine/matching app/src/main/java/com/example/junglenav/engine/fusion/PositionFusionEngine.kt app/src/main/java/com/example/junglenav/engine/positioning/PositionEstimate.kt app/src/test/java/com/example/junglenav/engine/matching/MapMatchingEngineTest.kt
git commit -m "feat: add map matching correction"
```

If Git is not initialized, record: `Task 9 complete - map matching added`.

### Task 10: Wire the hybrid truth into the map, diagnostics, and field test workflow

**Files:**
- Modify: `app/src/main/java/com/example/junglenav/app/AppContainer.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/map/MapViewModel.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/map/MapUiState.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/map/MapScreen.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/diagnostics/DiagnosticsScreen.kt`
- Modify: `app/src/androidTest/java/com/example/junglenav/MvpSmokeTest.kt`
- Create: `docs/testing/hybrid-field-checklist.md`
- Modify: `CHANGELOG.md`

**Step 1: Write the failing test**

```kotlin
@Test
fun diagnosticsShowsNavigationSourceInsteadOfGenericModeOnly() {
    rule.onNodeWithText("Diagnostics").performClick()
    rule.onNodeWithText("Source FUSED").assertIsDisplayed()
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.junglenav.MvpSmokeTest"`

Expected: FAIL because the UI does not yet expose the full hybrid source truth.

**Step 3: Write minimal implementation**

- Feed hybrid estimate fields through `AppContainer` into the map and diagnostics view models.
- Show source labels such as `GNSS`, `FUSED`, `DR_ACTIVE`, `MAP_MATCHED`, or `TERRAIN_CORRECTED`.
- Add a field checklist that tells the tester exactly which scenarios are safe to validate now.

**Step 4: Run test to verify it passes**

Run: `.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.junglenav.MvpSmokeTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/app/AppContainer.kt app/src/main/java/com/example/junglenav/feature/map app/src/main/java/com/example/junglenav/feature/diagnostics app/src/androidTest/java/com/example/junglenav/MvpSmokeTest.kt docs/testing/hybrid-field-checklist.md CHANGELOG.md
git commit -m "feat: surface hybrid navigation truth in ui"
```

If Git is not initialized, record: `Task 10 complete - hybrid truth wired into UI`.

## Phase D: Advanced Adaptation And Research

These items should not block field alpha, but they are the next layer once replay logs and confidence instrumentation are trustworthy.

### Future Task A: Motion classification for carry mode adaptation

- Create: `app/src/main/java/com/example/junglenav/engine/motion/MotionMode.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/motion/MotionClassifier.kt`
- Create: `app/src/main/java/com/example/junglenav/engine/motion/StrideAdaptor.kt`
- Test: `app/src/test/java/com/example/junglenav/engine/motion/MotionClassifierTest.kt`
- Goal: distinguish `still`, `walking`, `running`, `ascending`, and `descending`, then feed stride and sampling adjustments back into DR and battery policy.

### Future Task B: Richer multi-GNSS quality weighting

- Create: `app/src/main/java/com/example/junglenav/engine/positioning/GnssQualityScorer.kt`
- Modify: `app/src/main/java/com/example/junglenav/system/location/AndroidGnssStatusSource.kt`
- Test: `app/src/test/java/com/example/junglenav/engine/positioning/GnssQualityScorerTest.kt`
- Goal: factor satellite count, constellation diversity, freshness, and reported accuracy into fusion confidence.

### Future Task C: Visual navigation and SLAM spike

- Create: `docs/plans/2026-03-10-junglenav-visual-navigation-spike.md`
- Goal: define a research-only boundary for camera-driven localization without contaminating the core navigation stack.

## Acceptance Targets For The User's Test Session

After Milestone A and Milestone B, the app should be ready for these honest tests:

- `PATROL`, `SURVEY`, `BATTERY_SAVER`, and `EMERGENCY` visibly change sampling policy and UI state.
- Diagnostics can tell whether GNSS, inertial sensors, and barometer are live, replayed, or unavailable.
- The map can say whether the current position is `GNSS`, `FUSED`, or `DR_ACTIVE`.
- Heading remains usable for short windows even when the magnetometer is noisy.
- Dead reckoning continues for short GNSS dropouts and clearly shows confidence decay.

After Milestone C, the app should also be ready for:

- Trail or ridge plausibility checks.
- Terrain-aware confidence penalties.
- Map-matched correction labels instead of silent snapping.

The app should still NOT claim these until Phase D is implemented and field-validated:

- Accurate phone carry-mode adaptation.
- Production-grade Kalman superiority over the weighted solver.
- Camera-based visual localization.

## Verification Commands

Run after each completed task:

- `.\gradlew.bat testDebugUnitTest`
- `.\gradlew.bat connectedDebugAndroidTest`
- `.\gradlew.bat installDebug`
- `adb shell am start -n com.example.junglenav/.MainActivity`
- `adb shell dumpsys activity top | Select-String 'junglenav|MainActivity'`

For focused development:

- `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.engine.fusion.PositionFusionEngineTest"`
- `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.engine.deadreckoning.DeadReckoningEngineTest"`
- `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.engine.terrain.TerrainPlausibilityCheckerTest"`
- `.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.junglenav.MvpSmokeTest"`

## Suggested Execution Order

1. Phase A completely
2. Task 4, Task 5, and Task 6 from Phase B
3. Field alpha test round
4. Task 7 only if replay logs justify the Kalman branch
5. Phase C
6. Another field test round
7. Phase D only after enough real logs exist

## Follow-On Notes

- Do not skip replay tooling. It is what makes future Kalman work debuggable instead of mystical.
- Keep UI wording brutally honest. If the system is interpolating, say so.
- Prefer shipping a stable weighted hybrid pipeline over shipping a fragile Kalman implementation too early.
