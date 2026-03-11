# JungleNav JNavPack Field Checklist

Date: 2026-03-11

## Preflight

- Launch JungleNav on a device or emulator with storage access available.
- Verify the `Packages` route opens and shows `Import .jnavpack`, `Remote Catalog`, and the installed bundle list.
- Verify the `Map` route opens without overlapping the status bar, bottom nav, or compact map controls.

## Local Import

- Open `Packages`.
- Tap `Import .jnavpack`.
- Select a valid mission-area bundle from local storage or SD card.
- Verify the imported bundle appears under `Installed Bundles`.
- Verify imported unsigned bundles are labeled `Unverified`.

## Remote Catalog Download

- Stay online and open `Packages`.
- Wait for `Mission catalog ready`.
- Search for a known bundle such as `Bogor`.
- Download the bundle.
- Verify the package reports ready for offline use after installation completes.

## Trust-Aware Activation

- Search for an unsigned bundle such as `Gunung`.
- Download the bundle.
- Verify the activation warning dialog appears before the bundle becomes active.
- Verify the dialog shows bundle name, publisher, trust label, and checksum.
- Tap `Cancel` once and confirm the bundle does not become active.
- Activate the same bundle again and tap `Activate`.
- Verify the bundle becomes active only after confirmation.

## Offline Map Rendering

- Activate a downloaded bundle.
- Return to `Map`.
- Verify the source label changes to `Offline jnavpack`.
- Tap `Show Region`.
- Verify the active package name appears in the offline details panel.
- Verify `topo`, `hillshade`, and `imagery` controls appear when the bundle provides them.

## Airplane Mode

- With an offline bundle already active, enable airplane mode or disable Wi-Fi/mobile data.
- Reopen `Map`.
- Verify the active region still renders.
- Toggle `hillshade` and `imagery`.
- Verify the map stays responsive and does not fall back to the live style.

## Regression Sweep

- Visit `Waypoints`, `Tracks`, `Diagnostics`, and `Settings`.
- Verify all top-level pages still open and remain usable after bundle import or download.
- Start and stop a recording once.
- Add one waypoint from the map and confirm the compact target summary stays readable.

## Not Ready To Claim Yet

- Cryptographic publisher signature enforcement.
- Delta updates for bundle patches.
- Province-scale pack distribution.
