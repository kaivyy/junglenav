# JungleNav Hybrid Field Checklist

Date: 2026-03-10

## Preflight

- Launch JungleNav with location permission enabled.
- Verify the `Map` route opens without overlap on the status bar or bottom nav.
- Verify the compact target summary stays clear of the bottom sheet.
- Verify the bottom sheet starts in a compact state with offline region details hidden.

## Live GPS And Follow Mode

- Stay online with device GPS enabled.
- On the `Map` route, tap `Follow GPS`.
- Verify the button changes to `Following GPS`.
- Walk or simulate movement.
- Verify the map center keeps tracking the live device position.
- Tap the button again.
- Verify follow mode stops and the button returns to `Follow GPS`.

## Offline Region Controls

- Open `Packages`.
- Wait for `Mission catalog ready`.
- Search for a known pack such as `Bogor`.
- Download or redownload the mission bundle.
- Verify verified bundles report ready for offline use and become active automatically.
- Search for an unsigned pack such as `Gunung`.
- Download the bundle.
- Verify an activation warning appears before the pack can become active.
- Confirm the warning once and verify the pack becomes active only after explicit approval.
- Return to `Map`.
- Verify the source badge changes to `Offline jnavpack` instead of relying on the live fallback style.
- On the `Map` route, tap `Show Region`.
- Verify offline region details appear and list the active package state, trust label, style source, and renderer.
- Disable Wi-Fi or mobile data for the test device.
- Verify the active local package still renders and the page does not crash when reopened offline.
- Toggle `hillshade` and `imagery` when available.
- Verify optional layer controls remain responsive offline.
- Tap `Hide Region`.
- Verify the region details disappear and the map area stays uncluttered.

## Navigation Truth

- Open `Diagnostics`.
- Verify the screen shows operation mode, position mode, navigation source, last reliable fix age, and package state.
- Verify sensor readiness lines honestly describe GNSS, inertial, and barometer availability.
- Return to `Map`.
- Verify the source badge can show values such as `GNSS`, `FUSED`, `Matched`, or `Terrain` depending on current correction state.

## Hybrid Fallback Behavior

- Start with a fresh live fix.
- Verify the source becomes `GNSS` or `FUSED` based on reported accuracy.
- Simulate stale or weak GNSS conditions in a debug session.
- Verify confidence drops instead of freezing the previous position.
- Verify the app can fall back toward fused or dead-reckoning-driven behavior without crashing.

## Terrain And Matching Signals

- Use a debug path or replay scenario with weaker accuracy.
- Verify the app can still expose corrected navigation states when map matching or terrain plausibility penalties apply.
- Confirm that corrections reduce confidence rather than silently claiming better precision.

## Shell And Workflow

- Add a waypoint from `Map`.
- Verify the compact target summary updates cleanly and stays readable.
- Start and stop a track recording.
- Verify track state updates in both the map flow and the `Tracks` route.
- Open `Packages`, `Diagnostics`, and `Settings`.
- Verify each top-level page remains usable with the compact shell chrome.

## Not Ready To Claim Yet

- Carry-mode motion classification for phone in hand, pocket, or bag.
- Production-grade Kalman superiority over the weighted solver.
- Camera-based visual navigation or SLAM.
- Forest-grade accuracy guarantees under prolonged GNSS loss.
