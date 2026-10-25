# Implementation Log

(Reverse chronological — most recent first.)

## 2026-10-25 — v0.4.1: Complete Compose Material 3 rewrite

The user reviewed the v0.4.0 implementation and found it was a superficial reskin
of programmatic Views, not a real Material 3 redesign — the screenshot showed
broken layout (text overlap, malformed cards). This is a **complete ground-up
rewrite** using Jetpack Compose + `androidx.compose.material3`.

### Changes

- **Build config**: added Compose BOM 2024.09.03, `material3`, `material-icons-extended`,
  `activity-compose:1.9.3`, `lifecycle-runtime-compose:2.8.6`. Enabled
  `buildFeatures.compose = true` + `composeOptions.kotlinCompilerExtensionVersion = "1.5.14"`
  (matches Kotlin 1.9.24). Removed `appcompat` (not needed with `ComponentActivity`).
  Bumped version to `versionCode=8`, `versionName=0.4.1`.
- **Theme.kt** (new): `SplitAudioTheme` composable — `MaterialTheme(colorScheme = …)`.
  Dynamic color on Android 12+ (`dynamicLightColorScheme` / `dynamicDarkColorScheme`);
  seeded M3 baseline purple palette on Android 11- (light + dark). Includes all M3
  surface container roles (`surfaceContainerLowest` through `surfaceContainerHighest`).
- **MainActivity.kt** (rewritten): `ComponentActivity` + `setContent`. Uses
  `Scaffold` + `LargeTopAppBar` (collapsible) + `LazyColumn`. Three cards:
  `StatusCard` (state-colored, `Button` + `OutlinedButton`), `SetupSection`
  (two rows with `StatusChip` + `TextButton`), `CustomizeSection`
  (`SingleChoiceSegmentedButtonRow` + `AnimatedVisibility` `FilledTonalButton` +
  `ListItem` with `Switch`). 2-second `LaunchedEffect` polling for live status.
  `SnackbarHost` for error feedback. All colors via `MaterialTheme.colorScheme.*`.
- **AppsActivity.kt** (rewritten): `ComponentActivity` + `setContent`. Uses
  `Scaffold` + `TopAppBar` (back button) + `OutlinedTextField` (search,
  `extraLarge` shape) + `LazyColumn` of `ListItem`s. `ModalBottomSheet` for output
  picker with `RadioButton` rows. `DisposableEffect` + `LifecycleEventObserver`
  to flush changes on pause (preserves original `onPause → Route.apply` behavior).
- **M3.kt** (deleted): no longer needed — Compose handles all color resolution.
- **strings.xml** (en + zh-rTW): cleaned up section headers (removed ① ② markers),
  shortened button labels (Start/Stop), removed ✓ glyphs from status texts (M3 uses
  color + component semantics, not glyphs).

### Verification

- Debug APK builds: 62.5 MB (includes Compose tooling).
- R8 release APK builds: 3.9 MB.
- R8 mapping (29.8 MB) confirms obfuscation.
- `rikka.shizuku.Shizuku` class + `Shz.newProcess` method kept (proguard rule worked).
- All manifest components preserved.
- `approute.dex` + `approute.sh` preserved as release APK assets.
- Compose classes present in release dex (122 compose-related strings).
- CI run `40194565007`: `completed` / `success` in 4m58s.
- Draft release `v0.4.1` created with the unsigned APK asset (3,946,816 bytes).

### Outstanding

- Signing secrets still not configured (same as v0.4.0).
- On-device runtime verification still not possible (no emulator/device).

## 2026-10-25 — v0.4.0 (superseded): View-based Material 3 reskin

This was the first attempt — a View-based reskin using MaterialButton, MaterialCardView,
MaterialSwitch, MaterialButtonToggleGroup, TextInputLayout, MaterialAlertDialogBuilder.
It built and passed CI, but the user reviewed the screenshot and found the layout was
broken (text overlap, malformed cards). The implementation was a superficial reskin,
not a real M3 redesign. Superseded by v0.4.1.

## 2026-10-25 — Stage 0: Baseline + skill verification

- Installed `gh` CLI v2.65.0.
- Verified `material-design-3-ui` skill v1.1.1 (13 reference docs).
- Authenticated to GitHub as `ancientcatz` (PAT ROT13-decoded in memory, never printed).
- Cloned `ancientcatz/splitaudio`; inspected source.
- Installed Android SDK (platform-34, build-tools 34.0.0, platform-tools), Gradle
  8.9, Temurin JDK 17 (sandbox JRE lacked `javac`).
- Baseline build: `route/build_dex.sh` ✓; `assembleDebug` → 5.3 MB ✓;
  `assembleRelease` → 4.5 MB ✓; `:app:lintRelease` ✓.
- Created `feat/material3-redesign` from `main` (`a0777eb`).
- Created orphan `docs/material3-redesign` in separate worktree. First commit
  `dad0172` has no parent — verified orphan. Pushed to remote.
- Initial 14-file documentation commit pushed to `docs/material3-redesign`.
