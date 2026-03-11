# MapLibre Premium Map Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace the current fake map surface with a real MapLibre-powered map route that prefers Vulkan, keeps a safe fallback path, and upgrades the screen to a more premium interactive map experience.

**Architecture:** Keep the existing `feature.map` boundary and introduce a MapLibre-backed composable implementation behind the current screen-level contract. Use `MapLibre Compose` for Compose-native integration, default to the Vulkan backend on Android, and preserve a graceful fallback UI whenever the map style fails to load or a real offline package is not ready yet.

**Tech Stack:** Jetpack Compose, MapLibre Compose, MapLibre Android Vulkan backend, optional Material 3 MapLibre ornaments, existing JungleNav app shell/state flows.

---

### Task 1: Add MapLibre dependencies and the first failing state test

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/test/java/com/example/junglenav/feature/map/MapViewModelTest.kt`

### Task 2: Extend map UI state for a real renderer

**Files:**
- Modify: `app/src/main/java/com/example/junglenav/feature/map/MapUiState.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/map/MapViewModel.kt`
- Test: `app/src/test/java/com/example/junglenav/feature/map/MapViewModelTest.kt`

### Task 3: Build a MapLibre-backed premium map surface

**Files:**
- Create: `app/src/main/java/com/example/junglenav/feature/map/MapLibreMapSurface.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/map/MapScreen.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/map/MapViewportState.kt`

### Task 4: Wire style fallback and final verification

**Files:**
- Modify: `app/src/main/java/com/example/junglenav/feature/home/JungleNavAppRoot.kt`
- Modify: `CHANGELOG.md`
- Verify: `.\gradlew.bat assembleDebug`
- Verify: `.\gradlew.bat connectedDebugAndroidTest installDebug`
