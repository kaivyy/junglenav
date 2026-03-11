# JungleNav JNavPack Mission Bundles Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace the current demo-style offline region flow with a real offline package system based on mission-area `.jnavpack` bundles, supporting local import, server catalog download, trust-aware activation, and MapLibre rendering of vector topo, hillshade, and satellite layers entirely offline.

**Architecture:** Introduce a single package contract, `.jnavpack`, that contains bundle metadata, style resources, fonts/sprites, and tile archives for a mission area. Because the official MapLibre Android documentation currently documents direct local PMTiles support but does not document direct local MBTiles sources for Android, JungleNav should render MBTiles by serving unpacked bundle contents through an embedded loopback tile server inside the app; this is an inference from the official MapLibre Android examples and API docs rather than a direct statement from MapLibre. Both local import and remote download must feed the same validation, staging, installation, inventory, and activation pipeline so field behavior stays identical regardless of source.

**Tech Stack:** Kotlin, coroutines/Flow, Room, Storage Access Framework, Android `DownloadManager`, `DocumentFile`, MapLibre OpenGL, embedded HTTP server (`NanoHTTPD` or equivalent), SQLite tile readers, kotlinx serialization JSON, unit tests, and Android instrumentation tests.

---

## Current Baseline

- `PackageManagerScreen` and `PackageManagerViewModel` already support searching a small built-in library and downloading asset-backed offline packs.
- `AssetOfflineRegionService` currently creates a generated local style and stores package metadata, but it does not support real vector tile archives, trust metadata, or dual import/download sources.
- `MapViewModel` can switch active styles and label live fallback versus offline cache, but it does not know about multiple offline layers or trust state.
- `MapPackage` and `map_packages` persistence are still shaped around the temporary region-cache model, so schema expansion is required.
- The app already has connected smoke tests, a stable shell, and a working MapLibre integration on the emulator.

## Technical Note About MBTiles Rendering

- Official MapLibre Android examples currently document direct local PMTiles support through `pmtiles://file://...`, and the Android API docs describe vector sources via TileJSON/URI rather than local MBTiles archives.
- Source used for this implementation choice:
  - `https://maplibre.org/maplibre-native/android/examples/data/PMTiles/`
  - `https://maplibre.org/maplibre-native/android/api/-map-libre%20-native%20-android/org.maplibre.android.style.sources/-vector-source/index.html`
- Therefore, this plan uses an embedded loopback tile server to expose unpacked MBTiles bundle contents to MapLibre as regular tile endpoints.

## Bundle Contract

Each `.jnavpack` mission bundle should unpack into a structure like:

```text
bundle/
  manifest.json
  style/
    style.json
    sprites/
    glyphs/
  tiles/
    vector.mbtiles
    hillshade.mbtiles
    imagery.mbtiles
  preview/
    thumbnail.webp
```

The `manifest.json` should minimally contain:

```json
{
  "id": "gunung-gede-mission-a",
  "name": "Gunung Gede Mission A",
  "version": "2026.03.11",
  "packageFormat": 1,
  "bounds": [-106.98, -6.84, 107.01, -6.71],
  "center": [106.91, -6.77],
  "minZoom": 9,
  "maxZoom": 16,
  "layers": {
    "topoVector": true,
    "hillshadeRaster": true,
    "imageryRaster": true
  },
  "tiles": {
    "vector": "tiles/vector.mbtiles",
    "hillshade": "tiles/hillshade.mbtiles",
    "imagery": "tiles/imagery.mbtiles"
  },
  "stylePath": "style/style.json",
  "thumbnailPath": "preview/thumbnail.webp",
  "publisher": "JungleNav Labs",
  "signature": null,
  "checksumSha256": "..."
}
```

## Milestones

- **Milestone A:** Bundle domain model, persistence, and validation contract.
- **Milestone B:** Local import pipeline for `.jnavpack`.
- **Milestone C:** Remote catalog and mission-pack download pipeline.
- **Milestone D:** Embedded tile server and MapLibre offline rendering for vector, hillshade, and imagery.
- **Milestone E:** Trust UX, package manager UX, and field verification hardening.

## Execution Rules

- Follow TDD for every parser, validator, repository mapper, trust policy, layer resolver, and tile-serving helper.
- Keep import and download flows converged after the file reaches the staging area.
- Do not silently activate unverified packages. Every activation attempt must require explicit confirmation.
- Preserve current emulator stability by keeping MapLibre on the OpenGL backend.
- Prefer vertical slices over massive partial refactors.

## Task 1: Expand the offline package domain and persistence model

**Files:**
- Modify: `app/src/main/java/com/example/junglenav/core/model/MapPackage.kt`
- Create: `app/src/main/java/com/example/junglenav/core/model/MapPackSource.kt`
- Create: `app/src/main/java/com/example/junglenav/core/model/MapPackTrust.kt`
- Create: `app/src/main/java/com/example/junglenav/core/model/MapPackLayerSet.kt`
- Modify: `app/src/main/java/com/example/junglenav/data/db/entity/MapPackageEntity.kt`
- Modify: `app/src/main/java/com/example/junglenav/data/repository/OfflineMapPackageRepository.kt`
- Modify: `app/src/main/java/com/example/junglenav/data/db/JungleNavDatabase.kt`
- Test: `app/src/test/java/com/example/junglenav/data/repository/OfflineMapPackageRepositoryTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun packageRoundTripPreservesTrustSourceAndLayerAvailability() = runTest {
    val model = MapPackage(
        id = "mission-a",
        name = "Mission A",
        version = "1",
        sizeBytes = 10_000L,
        isActive = false,
        filePath = "file:///packs/mission-a/style.json",
        checksum = "abc",
        isDownloaded = true,
        centerLatitude = -6.77,
        centerLongitude = 106.91,
        source = MapPackSource.REMOTE_CATALOG,
        trust = MapPackTrust.UNVERIFIED,
        layers = MapPackLayerSet(topoVector = true, hillshadeRaster = true, imageryRaster = false),
    )

    repository.save(model)

    assertEquals(MapPackTrust.UNVERIFIED, repository.observePackages().first().single().trust)
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.data.repository.OfflineMapPackageRepositoryTest"`

Expected: FAIL because `MapPackage` and the entity do not yet store source, trust, or layer metadata.

**Step 3: Write minimal implementation**

- Expand `MapPackage` with:
  - `installRootPath`
  - `manifestPath`
  - `source: MapPackSource`
  - `trust: MapPackTrust`
  - `publisher`
  - `requiresActivationConfirmation`
  - `layers: MapPackLayerSet`
  - `thumbnailPath`
- Add matching columns to `MapPackageEntity`.
- Bump Room DB version and keep `fallbackToDestructiveMigration` until a real migration is worth adding.
- Update repository mappers so these fields survive round-trip persistence.

**Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.data.repository.OfflineMapPackageRepositoryTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/core/model app/src/main/java/com/example/junglenav/data/db app/src/main/java/com/example/junglenav/data/repository app/src/test/java/com/example/junglenav/data/repository/OfflineMapPackageRepositoryTest.kt
git commit -m "feat: expand offline package persistence for jnavpack"
```

If Git is not initialized, record: `Task 1 complete - package model and persistence expanded for jnavpack`.

## Task 2: Add `.jnavpack` manifest parsing and validation

**Files:**
- Create: `app/src/main/java/com/example/junglenav/system/offline/jnavpack/JnavPackManifest.kt`
- Create: `app/src/main/java/com/example/junglenav/system/offline/jnavpack/JnavPackManifestParser.kt`
- Create: `app/src/main/java/com/example/junglenav/system/offline/jnavpack/JnavPackValidationResult.kt`
- Create: `app/src/main/java/com/example/junglenav/system/offline/jnavpack/JnavPackValidator.kt`
- Test: `app/src/test/java/com/example/junglenav/system/offline/jnavpack/JnavPackValidatorTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun validatorRejectsBundleWithoutVectorTilesOrStyle() {
    val manifest = JnavPackManifest(
        id = "broken-pack",
        name = "Broken Pack",
        version = "1",
        packageFormat = 1,
        stylePath = "",
        vectorTilesPath = null,
    )

    val result = JnavPackValidator().validate(manifest)

    assertFalse(result.isValid)
    assertTrue(result.errors.contains("Missing stylePath"))
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.system.offline.jnavpack.JnavPackValidatorTest"`

Expected: FAIL because no manifest or validator exists.

**Step 3: Write minimal implementation**

- Define `JnavPackManifest` with fields for identity, bounds, center, zoom range, layer availability, tile archive paths, style path, publisher, signature block, and checksum.
- Implement JSON parsing with kotlinx serialization.
- Implement validator rules for supported `packageFormat`, required `stylePath`, required vector tile archive, consistent bounds, and optional hillshade or imagery paths only when declared available.

**Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.system.offline.jnavpack.JnavPackValidatorTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/system/offline/jnavpack app/src/test/java/com/example/junglenav/system/offline/jnavpack/JnavPackValidatorTest.kt
git commit -m "feat: add jnavpack manifest parser and validator"
```

If Git is not initialized, record: `Task 2 complete - jnavpack parsing and validation added`.

## Task 3: Build the installation pipeline and trust resolver

**Files:**
- Create: `app/src/main/java/com/example/junglenav/system/offline/jnavpack/JnavPackArchiveExtractor.kt`
- Create: `app/src/main/java/com/example/junglenav/system/offline/jnavpack/JnavPackInstaller.kt`
- Create: `app/src/main/java/com/example/junglenav/system/offline/jnavpack/JnavPackTrustResolver.kt`
- Create: `app/src/main/java/com/example/junglenav/system/offline/jnavpack/JnavPackInstallResult.kt`
- Modify: `app/src/main/java/com/example/junglenav/system/offline/OfflineRegionDownloadService.kt`
- Test: `app/src/test/java/com/example/junglenav/system/offline/jnavpack/JnavPackInstallerTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun installerMarksUnsignedBundleAsUnverifiedAndRequiresActivationConfirmation() = runTest {
    val result = installer.install(stagedBundle)

    assertEquals(MapPackTrust.UNVERIFIED, result.mapPackage.trust)
    assertTrue(result.mapPackage.requiresActivationConfirmation)
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.system.offline.jnavpack.JnavPackInstallerTest"`

Expected: FAIL because there is no install pipeline or trust resolver.

**Step 3: Write minimal implementation**

- Extract a staged `.jnavpack` archive to a temporary install directory.
- Parse and validate the manifest before any DB write.
- Resolve trust with these rules:
  - valid trusted signature -> `VERIFIED`
  - missing or unknown signature -> `UNVERIFIED`
- Create the final `MapPackage` model, including `requiresActivationConfirmation = trust != VERIFIED`.
- Rename or broaden `OfflineRegionDownloadService` responsibilities if needed so it can install real bundle files rather than temporary catalog items only.

**Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.system.offline.jnavpack.JnavPackInstallerTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/system/offline app/src/test/java/com/example/junglenav/system/offline/jnavpack/JnavPackInstallerTest.kt
git commit -m "feat: add jnavpack installer and trust resolver"
```

If Git is not initialized, record: `Task 3 complete - install pipeline and trust model added`.

## Task 4: Add local file import for `.jnavpack`

**Files:**
- Create: `app/src/main/java/com/example/junglenav/system/offline/imports/ImportedMapPackSource.kt`
- Create: `app/src/main/java/com/example/junglenav/system/offline/imports/LocalMapPackImportService.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/package_manager/PackageManagerViewModel.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/package_manager/PackageManagerScreen.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/home/JungleNavAppRoot.kt`
- Test: `app/src/test/java/com/example/junglenav/feature/package_manager/PackageManagerViewModelTest.kt`
- Test: `app/src/androidTest/java/com/example/junglenav/feature/package_manager/LocalMapPackImportSmokeTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun importActionAddsInstalledBundleToInventory() = runTest {
    viewModel.onImportCompleted(fakeImportUri)
    advanceUntilIdle()

    assertEquals("Gunung Gede Mission A", viewModel.uiState.value.items.single().name)
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.feature.package_manager.PackageManagerViewModelTest"`

Expected: FAIL because import actions and install wiring do not exist.

**Step 3: Write minimal implementation**

- Add an `Import .jnavpack` button to `PackageManagerScreen`.
- Use `ActivityResultContracts.OpenDocument()` in `JungleNavAppRoot`.
- Feed the selected `Uri` into `LocalMapPackImportService`, which copies the archive into a staging directory and calls the installer.
- Show import progress and failure messaging in `PackageManagerUiState`.

**Step 4: Run test to verify it passes**

Run:
- `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.feature.package_manager.PackageManagerViewModelTest"`
- `.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.junglenav.feature.package_manager.LocalMapPackImportSmokeTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/feature/package_manager app/src/main/java/com/example/junglenav/feature/home/JungleNavAppRoot.kt app/src/main/java/com/example/junglenav/system/offline/imports app/src/test/java/com/example/junglenav/feature/package_manager/PackageManagerViewModelTest.kt app/src/androidTest/java/com/example/junglenav/feature/package_manager/LocalMapPackImportSmokeTest.kt
git commit -m "feat: add local jnavpack import flow"
```

If Git is not initialized, record: `Task 4 complete - local jnavpack import added`.

## Task 5: Add remote catalog metadata and mission-pack downloads

**Files:**
- Create: `app/src/main/java/com/example/junglenav/core/model/RemoteMapPackCatalogItem.kt`
- Create: `app/src/main/java/com/example/junglenav/system/offline/catalog/RemoteMapPackCatalogService.kt`
- Create: `app/src/main/java/com/example/junglenav/system/offline/catalog/RemoteMapPackDownloadService.kt`
- Create: `app/src/main/java/com/example/junglenav/system/offline/catalog/RemoteMapPackCatalogParser.kt`
- Modify: `app/src/main/java/com/example/junglenav/app/AppContainer.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/package_manager/PackageManagerViewModel.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/package_manager/PackageManagerScreen.kt`
- Test: `app/src/test/java/com/example/junglenav/system/offline/catalog/RemoteMapPackCatalogParserTest.kt`
- Test: `app/src/test/java/com/example/junglenav/feature/package_manager/PackageManagerViewModelTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun parserBuildsCatalogEntriesWithDownloadUrlsAndTrustHints() {
    val entries = RemoteMapPackCatalogParser().parse(sampleJson)

    assertEquals("https://example.com/packs/gede-a.jnavpack", entries.single().downloadUrl)
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.system.offline.catalog.RemoteMapPackCatalogParserTest"`

Expected: FAIL because there is no remote catalog model or parser.

**Step 3: Write minimal implementation**

- Add a remote catalog JSON contract with name, mission summary, bounds, estimated size, publisher, trust hint, and download URL.
- Fetch catalog metadata with a small network service.
- Download `.jnavpack` archives into staging and hand them to the same installer from Task 3.
- Expose remote items and progress in `PackageManagerUiState`.

**Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.system.offline.catalog.RemoteMapPackCatalogParserTest" --tests "com.example.junglenav.feature.package_manager.PackageManagerViewModelTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/core/model/RemoteMapPackCatalogItem.kt app/src/main/java/com/example/junglenav/system/offline/catalog app/src/main/java/com/example/junglenav/app/AppContainer.kt app/src/main/java/com/example/junglenav/feature/package_manager app/src/test/java/com/example/junglenav/system/offline/catalog/RemoteMapPackCatalogParserTest.kt app/src/test/java/com/example/junglenav/feature/package_manager/PackageManagerViewModelTest.kt
git commit -m "feat: add remote jnavpack catalog and downloads"
```

If Git is not initialized, record: `Task 5 complete - remote catalog and download flow added`.

## Task 6: Implement MBTiles readers and embedded loopback tile server

**Files:**
- Create: `app/src/main/java/com/example/junglenav/system/offline/tiles/MbtilesMetadata.kt`
- Create: `app/src/main/java/com/example/junglenav/system/offline/tiles/MbtilesReader.kt`
- Create: `app/src/main/java/com/example/junglenav/system/offline/tiles/MbtilesTileRequest.kt`
- Create: `app/src/main/java/com/example/junglenav/system/offline/tiles/JnavTileServer.kt`
- Create: `app/src/main/java/com/example/junglenav/system/offline/tiles/JnavTileServerRegistry.kt`
- Test: `app/src/test/java/com/example/junglenav/system/offline/tiles/MbtilesReaderTest.kt`
- Test: `app/src/androidTest/java/com/example/junglenav/system/offline/tiles/JnavTileServerSmokeTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun readerReturnsCompressedVectorTileForXYZRequest() {
    val tile = reader.readTile(
        archivePath = sampleMbtiles.absolutePath,
        z = 12,
        x = 3351,
        y = 2079,
    )

    assertNotNull(tile)
    assertEquals("application/vnd.mapbox-vector-tile", tile!!.contentType)
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.system.offline.tiles.MbtilesReaderTest"`

Expected: FAIL because there is no tile reader or loopback server.

**Step 3: Write minimal implementation**

- Read MBTiles metadata and tile blobs with SQLite.
- Support vector PBF and raster PNG/JPEG/WebP archives.
- Expose endpoints like:
  - `/packs/{packId}/vector/{z}/{x}/{y}.pbf`
  - `/packs/{packId}/hillshade/{z}/{x}/{y}.webp`
  - `/packs/{packId}/imagery/{z}/{x}/{y}.jpg`
  - `/packs/{packId}/sprites/...`
  - `/packs/{packId}/glyphs/...`
- Keep the tile server bound to loopback only.

**Step 4: Run test to verify it passes**

Run:
- `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.system.offline.tiles.MbtilesReaderTest"`
- `.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.junglenav.system.offline.tiles.JnavTileServerSmokeTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/system/offline/tiles app/src/test/java/com/example/junglenav/system/offline/tiles/MbtilesReaderTest.kt app/src/androidTest/java/com/example/junglenav/system/offline/tiles/JnavTileServerSmokeTest.kt
git commit -m "feat: add embedded tile server for offline mbtiles"
```

If Git is not initialized, record: `Task 6 complete - mbtiles tile reader and server added`.

## Task 7: Resolve bundle styles for MapLibre and switch map rendering to the tile server

**Files:**
- Create: `app/src/main/java/com/example/junglenav/system/offline/style/JnavStyleResolver.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/map/MapViewModel.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/map/MapUiState.kt`
- Modify: `app/src/main/java/com/example/junglenav/app/AppContainer.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/home/JungleNavAppRoot.kt`
- Test: `app/src/test/java/com/example/junglenav/system/offline/style/JnavStyleResolverTest.kt`
- Test: `app/src/test/java/com/example/junglenav/feature/map/MapViewModelTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun resolverRewritesBundleStyleToLoopbackTileEndpoints() {
    val resolved = resolver.resolve(
        packId = "mission-a",
        manifest = manifest,
        serverBaseUrl = "http://127.0.0.1:38479",
    )

    assertTrue(resolved.styleJson.contains("http://127.0.0.1:38479/packs/mission-a/vector"))
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.system.offline.style.JnavStyleResolverTest"`

Expected: FAIL because no style resolver exists.

**Step 3: Write minimal implementation**

- Load the bundle `style.json`.
- Rewrite vector, hillshade, imagery, glyph, and sprite URLs to the embedded tile server endpoints.
- Update `MapViewModel` to carry:
  - active pack name
  - active pack trust label
  - layer availability
  - active layer toggles
  - map source label such as `Offline jnavpack`
- Start the tile server before activating a bundle-backed style.

**Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.system.offline.style.JnavStyleResolverTest" --tests "com.example.junglenav.feature.map.MapViewModelTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/system/offline/style app/src/main/java/com/example/junglenav/feature/map app/src/main/java/com/example/junglenav/app/AppContainer.kt app/src/main/java/com/example/junglenav/feature/home/JungleNavAppRoot.kt app/src/test/java/com/example/junglenav/system/offline/style/JnavStyleResolverTest.kt app/src/test/java/com/example/junglenav/feature/map/MapViewModelTest.kt
git commit -m "feat: render jnavpack styles through local tile server"
```

If Git is not initialized, record: `Task 7 complete - MapLibre rendering now uses installed jnavpack bundles`.

## Task 8: Add trust-aware activation UX and package manager hardening

**Files:**
- Modify: `app/src/main/java/com/example/junglenav/feature/package_manager/PackageManagerViewModel.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/package_manager/PackageManagerScreen.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/home/JungleNavAppRoot.kt`
- Create: `app/src/main/java/com/example/junglenav/feature/package_manager/ActivationConfirmationState.kt`
- Test: `app/src/test/java/com/example/junglenav/feature/package_manager/PackageManagerViewModelTest.kt`
- Test: `app/src/androidTest/java/com/example/junglenav/MvpSmokeTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun unverifiedPackageRequiresConfirmationBeforeActivation() = runTest {
    viewModel.activate("unverified-pack")

    assertEquals("unverified-pack", viewModel.uiState.value.pendingActivationId)
    assertNull(viewModel.uiState.value.activePackage)
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.feature.package_manager.PackageManagerViewModelTest"`

Expected: FAIL because activation is immediate and has no trust gate.

**Step 3: Write minimal implementation**

- Show clear package badges:
  - `Verified`
  - `Unverified`
  - `Imported`
  - `Catalog`
- When user activates an unverified pack, present a modal confirm dialog with publisher, checksum summary, and a warning.
- Only activate after explicit confirm.
- Add storage and size metadata to the package cards.

**Step 4: Run test to verify it passes**

Run:
- `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.feature.package_manager.PackageManagerViewModelTest"`
- `.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.junglenav.MvpSmokeTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/feature/package_manager app/src/main/java/com/example/junglenav/feature/home/JungleNavAppRoot.kt app/src/test/java/com/example/junglenav/feature/package_manager/PackageManagerViewModelTest.kt app/src/androidTest/java/com/example/junglenav/MvpSmokeTest.kt
git commit -m "feat: gate unverified package activation"
```

If Git is not initialized, record: `Task 8 complete - trust-aware activation UX added`.

## Task 9: Add map layer controls for topo, hillshade, and imagery

**Files:**
- Modify: `app/src/main/java/com/example/junglenav/feature/map/MapUiState.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/map/MapViewModel.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/map/MapScreen.kt`
- Modify: `app/src/main/java/com/example/junglenav/feature/map/MapLibreMapSurface.kt`
- Test: `app/src/test/java/com/example/junglenav/feature/map/MapViewModelTest.kt`
- Test: `app/src/androidTest/java/com/example/junglenav/MvpSmokeTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun imageryToggleOnlyAppearsWhenImageryLayerExists() {
    val state = MapUiState(
        availableLayers = MapPackLayerSet(topoVector = true, hillshadeRaster = true, imageryRaster = false),
    )

    assertFalse(state.canShowImageryToggle)
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.feature.map.MapViewModelTest"`

Expected: FAIL because the map state does not model layer toggles or availability.

**Step 3: Write minimal implementation**

- Add map UI state for:
  - `isTopoEnabled`
  - `isHillshadeEnabled`
  - `isImageryEnabled`
  - available layers
- Add compact layer chips or bottom-sheet toggles on the map page.
- Only expose toggles that the active bundle actually contains.
- Keep the map usable even if a requested optional layer is missing.

**Step 4: Run test to verify it passes**

Run:
- `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.feature.map.MapViewModelTest"`
- `.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.junglenav.MvpSmokeTest"`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/example/junglenav/feature/map app/src/test/java/com/example/junglenav/feature/map/MapViewModelTest.kt app/src/androidTest/java/com/example/junglenav/MvpSmokeTest.kt
git commit -m "feat: add offline topo hillshade and imagery controls"
```

If Git is not initialized, record: `Task 9 complete - map layer controls added`.

## Task 10: Hardening, smoke coverage, and docs for field testing

**Files:**
- Modify: `app/src/androidTest/java/com/example/junglenav/MvpSmokeTest.kt`
- Create: `app/src/androidTest/java/com/example/junglenav/feature/package_manager/RemoteCatalogSmokeTest.kt`
- Create: `docs/testing/jnavpack-field-checklist.md`
- Modify: `docs/testing/hybrid-field-checklist.md`
- Modify: `CHANGELOG.md`

**Step 1: Write the failing test**

```kotlin
@Test
fun packageFlowSupportsImportDownloadAndActivationWarning() {
    rule.onNodeWithText("Packages").performClick()
    rule.onNodeWithText("Import .jnavpack").assertIsDisplayed()
    rule.onNodeWithText("Remote Catalog").assertIsDisplayed()
}
```

**Step 2: Run test to verify it fails**

Run: `.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.junglenav.MvpSmokeTest"`

Expected: FAIL because the package manager does not yet surface the new offline product flow.

**Step 3: Write minimal implementation**

- Expand smoke coverage for:
  - package import entrypoint
  - remote catalog entrypoint
  - unverified activation warning
  - map layer toggles
  - active package label on map
- Add a field checklist for:
  - import from storage
  - download over network
  - activate unverified pack
  - airplane mode verification
  - topo/hillshade/imagery toggle checks
- Update changelog with the new offline package system.

**Step 4: Run test to verify it passes**

Run:
- `.\gradlew.bat testDebugUnitTest`
- `.\gradlew.bat connectedDebugAndroidTest`
- `.\gradlew.bat installDebug`
- `adb shell am start -n com.example.junglenav/.MainActivity`
- `adb shell dumpsys activity top | Select-String 'junglenav|MainActivity'`

Expected: PASS

**Step 5: Commit**

```bash
git add app/src/androidTest/java/com/example/junglenav docs/testing CHANGELOG.md
git commit -m "docs: add jnavpack field verification and smoke coverage"
```

If Git is not initialized, record: `Task 10 complete - jnavpack flow documented and verified`.

## Acceptance Targets

After Task 4:

- User can import a `.jnavpack` from local storage.
- App validates structure before installation.
- Installed bundle appears in inventory with trust metadata.

After Task 5:

- User can browse a remote mission-pack catalog.
- Downloaded packs install through the same pipeline as imported packs.

After Task 7:

- Active bundle renders through MapLibre while fully offline.
- Bundle style, glyphs, sprites, and tiles are served from loopback endpoints inside the app.

After Task 8 and Task 9:

- Unverified packs always require manual activation confirmation.
- Map can independently toggle topo, hillshade, and imagery when the active bundle supports them.

## Not Ready To Claim Yet

- Cryptographically strong publisher-signature infrastructure with key rotation.
- Delta bundle updates or binary diff patching.
- Province-scale or nationwide packs.
- Live team sync or encrypted mission distribution.

## Verification Commands

Run after each completed task:

- `.\gradlew.bat testDebugUnitTest`
- `.\gradlew.bat connectedDebugAndroidTest`
- `.\gradlew.bat installDebug`
- `adb shell am start -n com.example.junglenav/.MainActivity`
- `adb shell dumpsys activity top | Select-String 'junglenav|MainActivity'`

Focused runs:

- `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.system.offline.jnavpack.JnavPackValidatorTest"`
- `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.system.offline.tiles.MbtilesReaderTest"`
- `.\gradlew.bat testDebugUnitTest --tests "com.example.junglenav.feature.package_manager.PackageManagerViewModelTest"`
- `.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.junglenav.MvpSmokeTest"`

## Follow-On Notes

- If MapLibre Android later adds direct local MBTiles source support, the embedded tile server can be retired behind the same style-resolution boundary.
- Keep `.jnavpack` as the public contract even if the internal archive mix evolves from MBTiles to PMTiles or MLT later.
- Mission-area packs should remain the primary optimization target; avoid optimizing early for giant region packs that hurt device reliability.
