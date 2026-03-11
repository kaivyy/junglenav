# JungleNav Live Gap Roadmap

As of: 2026-03-12

This document is the continuation roadmap for JungleNav. It is meant to be the single file to read before continuing feature work.

## Status Legend

- `[x] Live` means usable in the app now and covered by at least basic verification.
- `[~] Partial` means implemented as MVP, alpha, demo, placeholder, or internal seam, but not ready to be claimed as full product capability.
- `[ ] Not live` means missing, only planned, or still research.

## Source Of Truth Used For This Audit

- `prd.md`
- `CHANGELOG.md`
- `docs/plans/2026-03-10-junglenav-mvp-foundation.md`
- `docs/plans/2026-03-10-junglenav-hybrid-navigation-roadmap.md`
- `docs/plans/2026-03-11-junglenav-jnavpack-mission-bundles.md`
- Current codebase under `app/src/main/java`

## Executive Snapshot

JungleNav already has a usable Android shell, offline-capable map route, waypoints, tracks, diagnostics, settings, package management, and mission-area offline bundles. The app can be installed and run on the emulator, and the current Android smoke suite is green.

The biggest truth gap is this: JungleNav is still much stronger as an offline field-map MVP than as a finished hybrid-navigation system. The package system, map rendering, and UI shell are ahead of the navigation intelligence. Sensor fusion, terrain correction, and multi-condition field accuracy are still alpha-grade. National-scale data coverage, real production catalog distribution, team features, and advanced navigation research are not live yet.

If work resumes later, the safest reading is:

- The app is ready for controlled MVP and alpha field testing.
- The app is not ready yet for strong product claims around forest-grade accuracy, nationwide offline coverage, or production-secure mission distribution.

## What Is Already Live

### Application Shell And UX

- `[x]` Android native app shell with Jetpack Compose
- `[x]` Top-level routes for `Map`, `Waypoints`, `Tracks`, `Packages`, `Diagnostics`, and `Settings`
- `[x]` Material 3 based UI refresh with compact shell chrome and safe-area handling
- `[x]` Bottom navigation with icon rail
- `[x]` Map page with full-view layout and compact overlays

### Core Offline Workflow

- `[x]` Local Room persistence for waypoints, tracks, and package inventory
- `[x]` DataStore-backed settings for operation mode, units, and low-light mode
- `[x]` Offline package inventory with activate and delete flow
- `[x]` Local `.jnavpack` import flow
- `[x]` Remote mission catalog download flow
- `[x]` Manual trust confirmation for unverified bundles

### Map And Package Rendering

- `[x]` MapLibre OpenGL integration on Android
- `[x]` Active offline bundle rendering through local resolved styles
- `[x]` Embedded loopback tile server for `vector.mbtiles`, `hillshade.mbtiles`, and `imagery.mbtiles`
- `[x]` Layer toggles for topo, hillshade, and imagery when supported by the active pack
- `[x]` Offline region detail panel with active package metadata

### Field Utilities

- `[x]` Waypoint add/list flow
- `[x]` Track start and stop flow
- `[x]` GPX export coverage
- `[x]` Follow GPS map action
- `[x]` Diagnostics screen with navigation readiness and current source summary
- `[x]` Operation modes: `PATROL`, `SURVEY`, `BATTERY_SAVER`, `EMERGENCY`

### Verification Baseline

- `[x]` Unit test suite passing
- `[x]` Full `connectedDebugAndroidTest` suite passing on emulator
- `[x]` App install and launch verified on emulator

## Master Feature Status Checklist

### 1. Map, Terrain, And Offline Data

- `[x]` Mission-area offline bundle format with manifest, style, and tile payloads
- `[x]` Offline package import and activation
- `[x]` Offline package download from in-app catalog
- `[x]` Offline map rendering from local package after activation
- `[x]` Hillshade and imagery toggles when bundle provides them
- `[~]` Remote catalog exists, but is still seeded/demo-backed rather than a real production catalog service
- `[~]` Coverage model works for mission-area packs, not all regions
- `[~]` Terrain query exists, but current `SrtmTerrainProvider` is still placeholder-grade rather than real terrain data ingestion
- `[~]` Topo map exists, but full production terrain stack from real contour, DEM, ridge, river, and vegetation sources is not complete
- `[ ]` Nationwide or all-daerah offline coverage
- `[ ]` Production package build pipeline for real areas
- `[ ]` Delta updates for large bundle revisions
- `[ ]` Strong signature infrastructure with trusted publishers and key rotation
- `[ ]` Storage migration, archive move, and package update UX that is truly production-ready

### 2. Waypoints And Navigation Targets

- `[x]` Create and manage waypoints offline
- `[x]` Set target labels and update compact target summary
- `[x]` Return-to-base quick action at UI level
- `[~]` Navigation-to-target UX exists as labels and quick actions, but not as a full bearing-and-guidance workflow with strong acceptance coverage
- `[ ]` Waypoint import
- `[ ]` Waypoint export
- `[ ]` Category/tagging model for operational waypoint types
- `[ ]` Rich waypoint metadata for survey workflows

### 3. Tracks And Route Recovery

- `[x]` Track start and stop
- `[x]` Track persistence in local repository
- `[x]` GPX export seam and test coverage
- `[~]` Track workflow exists, but there is no strong live proof yet for crash-safe resume after interruption
- `[ ]` Retrace mode
- `[ ]` Track playback
- `[ ]` Visual route-quality overlay by navigation mode along the full track
- `[ ]` Export beyond GPX, such as `KML` or `GeoJSON`

### 4. Positioning And Hybrid Navigation

- `[x]` Live GPS ingestion
- `[x]` GNSS metadata seams and location sample contracts
- `[x]` Inertial and barometer source seams
- `[x]` Diagnostics honesty for GNSS, IMU, and barometer readiness
- `[x]` Gyro-stabilized heading fusion boundary
- `[x]` Dead reckoning engine boundary
- `[x]` Adaptive sampling policy boundary
- `[~]` Weighted hybrid fusion exists, but it is still alpha and not ready for strong field-accuracy claims
- `[~]` Kalman-ready seam exists, but not a production-proven Kalman deployment
- `[~]` Map matching exists in basic form
- `[~]` Terrain plausibility correction exists in basic form
- `[~]` Position confidence modeling exists, but needs heavier field validation and tuning
- `[ ]` Motion classification for walking, running, idle, ascent, and descent
- `[ ]` Carry-mode adaptation for hand, pocket, vest, or bag placement
- `[ ]` Mature multi-GNSS weighting logic
- `[ ]` Stable heading-up map mode
- `[ ]` Uncertainty visualization on map
- `[ ]` Production field replay analysis workflow

### 5. Terrain Intelligence

- `[~]` Elevation and slope provider boundary exists
- `[~]` Terrain plausibility checks exist
- `[ ]` Real SRTM or equivalent terrain ingestion pipeline
- `[ ]` Ridge, valley, river, and vegetation-aware correction stack
- `[ ]` Terrain risk engine
- `[ ]` Route suggestions based on terrain context

### 6. UI And Operational Ergonomics

- `[x]` Outdoor-oriented shell layout
- `[x]` Low-light mode toggle
- `[x]` Compact overlays and bottom sheet behavior
- `[x]` Safe-area handling on current emulator target
- `[~]` UX is good enough for MVP demos, but still needs more field-hardening for glove use, panic flow, and prolonged outdoor sessions
- `[ ]` Dedicated red-mode or tactical low-light palette
- `[ ]` Large-button emergency return flow
- `[ ]` Dedicated heading-up and north-up controls
- `[ ]` Strong landscape optimization
- `[ ]` Accessibility audit for contrast, font scaling, and touch targets

### 7. Diagnostics, Safety, And Reliability

- `[x]` Diagnostics page
- `[x]` Honest source labels in map and diagnostics
- `[x]` Readiness lines for GNSS, inertial, and barometer
- `[~]` Background tracking scaffolding exists, but production-grade reliability under all Android background limits still needs hardening
- `[ ]` Structured crash recovery flow
- `[ ]` Persistent fault/event log for field incident analysis
- `[ ]` App self-check screen for package integrity, sensor health, and storage
- `[ ]` Battery and thermal diagnostics deep dive

### 8. Team, Sync, And Mission Operations

- `[ ]` Team sync
- `[ ]` Mesh position sharing
- `[ ]` Shared waypoint distribution
- `[ ]` Shared track exchange
- `[ ]` Mission package distribution backend
- `[ ]` Role-based or signed mission package approval workflow

### 9. Advanced Research Features

- `[ ]` Visual navigation
- `[ ]` SLAM
- `[ ]` Camera-based terrain recognition
- `[ ]` High-confidence forest navigation during prolonged GNSS denial
- `[ ]` AI-assisted motion classification at production quality

## Features That Must Not Be Claimed As Fully Live Yet

- `[ ]` Forest-grade hybrid navigation accuracy
- `[ ]` Production Kalman fusion
- `[ ]` Robust carry-mode adaptation
- `[ ]` Real terrain-aware correction using authoritative DEM and landform data
- `[ ]` Nationwide offline mapping
- `[ ]` Production-secure package trust model
- `[ ]` Team collaboration features
- `[ ]` Visual navigation

## Roadmap Phases

### Phase 0: Truthful Productization Baseline

Purpose: remove ambiguity between what exists for demos and what is safe to claim publicly.

Targets:

- Freeze the current MVP baseline in docs and tester checklists
- Add a product status matrix to release notes and internal docs
- Harden package, track, and diagnostics regression coverage
- Finish missing smoke paths for import, offline relaunch, and settings persistence

Exit gate:

- Full unit and Android suites green
- Roadmap and checklist docs current
- No unclear claims in product copy about hybrid accuracy

### Phase 1: Real Offline Map Productization

Purpose: make JungleNav strong first as an offline mapping product, not just a demo catalog.

Build next:

- Real remote catalog service instead of seeded demo catalog
- Real package build pipeline for new areas
- Area expansion strategy for Indonesia by mission-area packs
- Package versioning, update, and replace flow
- Package integrity and publish tooling
- Better offline metadata, previews, and storage accounting

Dependencies:

- Existing `jnavpack` importer and tile server are already the right foundation

Exit gate:

- New real areas can be produced and installed without code changes
- Offline map works for imported and downloaded real mission packs
- Package management survives repeat install, replace, delete, and airplane-mode checks

### Phase 2: Hybrid Navigation Alpha To Beta

Purpose: lift navigation from alpha plumbing to field-testable reliability.

Build next:

- Replace placeholder terrain provider with real terrain ingestion
- Improve fusion solver and confidence logic
- Strengthen map matching with better path candidates
- Add heading-up map mode and uncertainty overlay
- Build replay-based tuning workflow from recorded field logs
- Validate dead reckoning continuity and relock behavior in scripted test sets

Dependencies:

- Phase 1 is not strictly required, but real map data makes this phase much more useful

Exit gate:

- Recorded field logs can be replayed through the pipeline
- Hybrid source transitions are visible and believable
- Accuracy behavior is measured and documented, not guessed

### Phase 3: Operational Navigation Workflows

Purpose: turn the app from map viewer plus alpha positioning into a stronger field tool.

Build next:

- Full navigate-to-waypoint workflow with bearing and arrival behavior
- Retrace mode and playback
- Waypoint import/export
- More export formats for tracks
- Crash-safe track resume validation
- Better emergency return and route recovery UX

Dependencies:

- Phase 2 improves truthfulness of these workflows, but some items can begin earlier

Exit gate:

- Target navigation and return workflows are usable without operator guesswork
- Track recovery flows are validated after backgrounding or interruption

### Phase 4: Secure Distribution And Team Operations

Purpose: support organized deployments for relawan, SAR, ranger, or mil-style workflows.

Build next:

- Real package signing and trust policy
- Mission catalog backend with package metadata lifecycle
- Team sync for waypoints and active mission context
- Local-first sharing paths for disconnected teams
- Optional encryption and operator approval model

Dependencies:

- Phase 1 package productization is required first

Exit gate:

- Mission packages can be distributed and approved through a real trust model
- Teams can exchange key mission artifacts without relying on cloud-only behavior

### Phase 5: Advanced Navigation Research

Purpose: pursue the ambitious parts of the PRD without confusing them with the current product state.

Build next:

- Motion classification and carry-mode adaptation
- Richer multi-GNSS weighting
- Visual navigation research
- Camera correction and SLAM prototypes
- Terrain risk engine and advanced route hints

Dependencies:

- Strong replay harness and field logs from Phase 2

Exit gate:

- Research features have standalone acceptance criteria and do not destabilize core offline navigation

## Recommended Build Order From Here

If the goal is fastest product value:

1. Phase 1
2. Phase 3
3. Phase 2
4. Phase 4
5. Phase 5

If the goal is strongest navigation credibility:

1. Phase 2
2. Phase 1
3. Phase 3
4. Phase 4
5. Phase 5

Recommended for JungleNav now:

1. Finish Phase 1 enough to support real areas and real package operations
2. Then push Phase 2 to make positioning claims more truthful
3. Then build Phase 3 workflows on top of that stronger base

## Resume Checklist For The Next Session

Read these first:

- `prd.md`
- `docs/plans/2026-03-10-junglenav-hybrid-navigation-roadmap.md`
- `docs/plans/2026-03-11-junglenav-jnavpack-mission-bundles.md`
- This file

Then decide which stream you are continuing:

- `Offline map productization`
- `Hybrid navigation accuracy`
- `Operational workflow completion`

## Suggested Next Concrete Epics

### Epic A: Real Indonesia Area Pipeline

- `[ ]` Define source datasets for real target areas
- `[ ]` Build `.jnavpack` generation tooling
- `[ ]` Publish first real non-demo areas
- `[ ]` Add package update and replace logic

### Epic B: Hybrid Field Replay

- `[ ]` Record replay fixtures from live sessions
- `[ ]` Build replay runner for field logs
- `[ ]` Tune fusion and confidence using recorded paths
- `[ ]` Add acceptance metrics for relock and drift

### Epic C: Target And Return Workflows

- `[ ]` Full navigate-to-waypoint screen behavior
- `[ ]` Retrace flow from recorded track
- `[ ]` Playback and route quality timeline
- `[ ]` Waypoint import and export

### Epic D: Secure Mission Distribution

- `[ ]` Signed package model
- `[ ]` Publisher trust policy
- `[ ]` Real remote catalog backend
- `[ ]` Update channels for packages

## Current Highest-Value Next Step

The best next step is:

- Build the real offline map production pipeline for non-demo mission areas

Reason:

- It gives immediate practical value
- It removes the current biggest limitation in `Packages`
- It creates the real data foundation needed before navigation accuracy work can be validated honestly

## Final Truth Summary

JungleNav today is:

- `[x]` A working Android offline field-map MVP
- `[x]` A working mission-area offline package platform
- `[x]` A usable package import/download/activation workflow
- `[~]` An alpha hybrid-navigation prototype
- `[ ]` A finished terrain-aware production navigation system
- `[ ]` A nationwide offline map product
- `[ ]` A secure mission-distribution platform
