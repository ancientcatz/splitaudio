# Implementation Log

(Reverse chronological — most recent first.)

## 2026-10-25 — v0.4.2: UI polish pass

The user reviewed the v0.4.1 Compose Material 3 rewrite on a real device and
asked for further polish — explicitly *without* redesigning the UI. This commit
delivers visual refinement only: no architectural changes, no new dependencies,
no string changes.

### Changes

**Home screen (MainActivity):**
- **Status card**: icon now sits in a circular tonal "halo" (`onContainer` @ 12% alpha)
  instead of floating bare. Icon switches per state — `GraphicEq` (active), `Pending`
  (waiting), `MusicOff` (idle). `AnimatedContent` smoothly transitions the icon
  (`scaleIn + fadeIn`) and status text (`fadeIn`) when state changes.
- **Start/Stop buttons**: leading icons added (`PlayArrow` / `Stop`) — explicit
  affordance beyond the label. `CircularProgressIndicator` stays as the spinner.
- **Hero subtitle** restored as the first `LazyColumn` item (was present in original
  strings.xml, accidentally removed in v0.4.0).
- **Setup + Customize section headers**: now sit in a small circular
  `primaryContainer` / `tertiaryContainer` "badge" with the section icon. The title
  has a visual anchor instead of being a bare colored string.
- **Setup row icons**: now live inside a 40 dp circular `surfaceContainerHighest`
  "avatar" — visually consistent with the section header badges.
- **StatusChip**: now carries a leading mini-icon (`CheckCircle` for OK, `Pending`
  for warning, `RadioButtonUnchecked` for error). Triple-coded state (color + icon +
  label) instead of color-only — important for accessibility.

**Apps screen (AppsActivity):**
- **App icons** in the list: clipped to a 10 dp rounded corner instead of bare
  square — matches M3 card/list shape language.
- **Search field**: added a trailing clear-search `IconButton` (`Close` icon) that
  appears only when the query is non-empty.
- **Loading state**: while apps are being enumerated, shows a centered
  `CircularProgressIndicator` + "Loading apps…" label instead of an empty list.
- **Empty state**: when search returns nothing, shows a centered `Search` icon +
  `No apps match "<query>"` + `Try a different search term.` instead of an empty list.
- **Bottom sheet output picker**: added a small "Output" section label below the app
  name. Each option row is wrapped in a tonal `secondaryContainer` highlight when
  selected (with a `CheckCircle` icon at the trailing edge). Selected option's label
  is bolded and uses `onSecondaryContainer` for emphasis; unselected rows stay
  transparent.

### Verification

- Build: debug APK 62.5 MB; **R8 release APK 3.97 MB** (essentially unchanged from v0.4.1's 3.95 MB).
- R8 mapping confirms `rikka.shizuku.Shizuku` class + `Shz.newProcess` method still kept.
- All manifest components preserved.
- CI run `40199651320`: `completed` / `success` in 6m45s.
- Draft release `v0.4.2` created with the unsigned APK asset (3,967,408 bytes).
- Old v0.4.1 draft release deleted (superseded).

### Outstanding

- Signing secrets still not configured (same as v0.4.0 / v0.4.1).
- On-device runtime verification still not possible (no emulator/device).

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
