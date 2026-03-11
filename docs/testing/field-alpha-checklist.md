# JungleNav Field Alpha Checklist

Date: 2026-03-10

## Offline Launch

- Put the emulator or device in airplane mode.
- Launch JungleNav from a cold start.
- Verify the app opens to the `Map` route without crashing.
- Verify the top status bar still shows target, distance, mode, confidence, and recording state.

## Waypoint Capture

- Open the `Waypoints` tab with no internet connection.
- Tap `Add Waypoint`.
- Verify a new waypoint appears in the list immediately.
- Return to `Map` and verify the active target label updates to the latest waypoint name.

## Recording Flow

- Open the `Tracks` tab.
- Tap `Start Recording`.
- Verify the `Recording` state appears in both the track screen and the status bar.
- Leave the app in the background for 5 minutes.
- Reopen the app and verify the recording session still appears in the tracks screen.

## Operation Modes

- Open `Settings` when mode controls are added to the UI.
- Switch between `PATROL`, `SURVEY`, `BATTERY_SAVER`, and `EMERGENCY`.
- Verify cadence-related labels and diagnostics output reflect the selected mode.

## Position Transparency

- Open the `Diagnostics` tab.
- Verify operation mode, position mode, active package ID, and event log are visible.
- Simulate stale location updates in a debug build.
- Verify the displayed position mode transitions from `GNSS_LOCKED` to `DR_ACTIVE`, then to `DR_LOW_CONF`.

## Offline Packages

- Open the `Packages` tab.
- Tap `Import Demo Package`.
- Verify the package list shows version, size, checksum, and active state.
- Activate a different package and verify only one package remains active.

## Export Readiness

- Start a recording session and add at least one track point in a debug scenario.
- Stop recording.
- Run the GPX export flow once it is connected to UI.
- Verify the exported file contains the expected session name and track points.
