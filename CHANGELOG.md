# Changelog

## 2026-03-11

### Added

- Mission-area `jnavpack` bundle support with manifest parsing, validation, install metadata, trust modeling, and unified package inventory.
- Local `.jnavpack` import flow and remote mission catalog download flow that share the same installer pipeline.
- Embedded MBTiles loopback tile server, style URL resolver, and demo mission bundle seeding for vector topo, hillshade, and imagery rendering.
- Trust-aware activation dialog for unverified bundles.
- Dedicated `RemoteCatalogSmokeTest` coverage and a new field checklist in `docs/testing/jnavpack-field-checklist.md`.

### Changed

- Package cards now expose stable test tags for remote catalog download actions, making package smoke coverage deterministic on compact devices.
- Package smoke tests now close the soft keyboard, scroll directly to bundle actions, and wait on stable catalog-ready signals instead of brittle visible-text assumptions.
- Demo mission bundle seeding now uses synchronized, atomic staging so repeated catalog fetches do not corrupt temporary MBTiles generation during instrumentation runs.
- The hybrid field checklist now includes trust confirmation and `Offline jnavpack` verification steps.

### Verified

- Targeted Android instrumentation verification for `RemoteCatalogSmokeTest`.
- Targeted Android instrumentation verification for `MvpSmokeTest`, including verified-bundle download and offline map rendering.

## 2026-03-10

### Added

- Core JungleNav domain models for operation mode, position mode, confidence, field status, waypoints, tracks, and offline map packages.
- App shell with top-level navigation for map, waypoints, tracks, packages, diagnostics, and settings.
- Room-backed local persistence for waypoints, tracks, and offline package inventory.
- DataStore-backed settings repository for operation mode preferences.
- Offline waypoint workflow with list UI, editor state, and repository-backed view model.
- Track session workflow with local persistence, UI state, and GPX exporter coverage.
- Fake offline map adapter boundary, package manager screen, and first MVP map screen.
- Position state machine, fusion facade, diagnostics screen, tracking notification factory, foreground service scaffold, battery policy, and permission coordinator.
- Instrumentation smoke test covering add-waypoint and start-recording flows.
- Field alpha manual checklist in `docs/testing/field-alpha-checklist.md`.
- Real MapLibre-powered map surface with a compatible OpenGL backend, premium overlays, and runtime style fallback handling.
- Functional settings screen with operation profile controls, units selection, and low-light mode.
- Hybrid navigation groundwork: GNSS metadata contracts, inertial and barometer sample models, replayable sources, Android sensor adapter seams, gyro-stabilized heading, dead reckoning, confidence scoring, and adaptive sampling primitives.
- Hybrid navigation alpha pipeline with a weighted fusion solver, Kalman-ready seam, terrain plausibility scoring, map matching, and honest source labels flowing into the UI.
- Hybrid field checklist in `docs/testing/hybrid-field-checklist.md` for live GPS, fallback behavior, and source-truth validation.

### Changed

- Main activity now boots the real JungleNav Compose shell instead of the starter template screen.
- Shared shell state now keeps the top status bar, map route, track route, package route, and diagnostics route in sync.
- Emulator verification flow now works on the current Android 7.1.1 virtual device after disabling install-time package verification for ADB installs.
- The app UI now uses a colorful expedition-inspired visual system with refreshed typography, floating route pills, richer cards, and more polished feature screens.
- The map route now prefers a live style URI, falls back gracefully when package styles are placeholders or fail to load, and initializes MapLibre from the application layer.
- The Android baseline is now `minSdk 23` to satisfy the current MapLibre Android SDK line used by the app.
- The shell chrome now respects status bar and navigation bar insets, uses a more compact top status card, and exposes all route pills without horizontal overflow.
- The map route now prioritizes quick actions higher in the layout and keeps long pages scroll-safe on compact emulator screens.
- The bottom navigation now uses a single horizontal icon rail with smaller active emphasis, giving all six destinations a cleaner one-row layout on compact Android screens.
- The map route now behaves more like a full-view navigation canvas, with overlays that clear the status bar and stay above the bottom nav for easier scanning.
- The map route now consolidates renderer, operation mode, and style-source badges into one overlay group, removing duplicated fallback labels that were overlapping on the full-screen map.
- Diagnostics now report hardware readiness from the current device instead of static pending placeholders.
- The shared navigation container now drives hybrid position truth from live location samples, terrain plausibility penalties, map matching, and the fusion engine instead of a thin placeholder facade.
- The compact field status bar and map target summary now use a smaller Material 3 presentation so target and distance labels no longer cover content on small screens.
- The map route now starts with offline region details collapsed, exposes a `Show Region` toggle, and supports a `Follow GPS` action that recenters to the live device fix while online.
- Runtime location updates now register on the main looper, preventing the force close that occurred right after granting location permission.
- The package library now supports real search, download, activation, and deletion flows instead of the old demo-import placeholder.
- Offline map packages now resolve to local asset-backed region packs with generated `file:` MapLibre styles, so the active map can render without a live network style.
- The connected smoke suite now validates package search, offline region download, map activation, and full top-level route navigation against the current UI instead of the retired demo package flow.

### Verified

- Unit tests for field status, app routes, operation mode preferences, waypoint flow, GPX export, track flow, map package flow, map view state, position state machine, battery policy, and permission coordinator.
- Debug and androidTest builds.
- Connected smoke test on emulator for creating a waypoint and starting recording from the main shell.
- Connected smoke verification for the new MapLibre renderer badge and app launch on the emulator.
- Connected smoke verification for safe-area chrome, settings mode switching, and all top-level routes.
- Connected smoke verification for the single-row icon bottom nav and the full-view map overlay layout on the emulator.
- Connected smoke verification for the map header badge set, including a single visible fallback-style badge in full-screen mode.
- Targeted unit verification for GNSS metadata contracts, replay sources, diagnostics readiness, heading fusion, dead reckoning, confidence scoring, and adaptive sampling.
- Targeted Android instrumentation verification for `AndroidLocationSourceSmokeTest` on the emulator.
- Targeted unit verification for `PositionFusionEngine`, `KalmanFusionSolver`, `TerrainPlausibilityChecker`, `MapMatchingEngine`, and the updated `MapViewModel`.
- Targeted Android instrumentation verification for the compact map overlay, collapsible offline region details, and diagnostics navigation-source truth on the emulator.
- Targeted Android instrumentation verification for `AssetOfflineRegionServiceSmokeTest` on the emulator.
- Connected smoke verification for package search, offline region download, offline-style map activation, and top-level route health on the emulator.
