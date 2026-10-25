# Material 3 Design System (SplitAudio) — Compose edition

This document specifies the Material 3 design system applied to the SplitAudio redesign.
It is the result of applying the `material-design-3-ui` skill (v1.1.1) to a complete
Jetpack Compose rewrite.

## Design intent

- **Primary user goal**: route per-app audio output without root. The home screen must
  show, at a glance, whether routing is on, what the required permissions need, and how
  to switch between Simple and Custom modes.
- **Information hierarchy**: status first, required setup second, customization third.
- **Adaptive behavior**: single-pane phone app; layout remains readable from 360 dp to
  600 dp. `LargeTopAppBar` collapses on scroll.
- **M3 Expressive**: applied sparingly — a single state-colored status card and
  `AnimatedVisibility` on the "Choose output per app" button. All other surfaces stay calm.

## Implementation

- **Framework**: Jetpack Compose (`androidx.compose.material3:material3`)
- **Activity**: `ComponentActivity` + `setContent { SplitAudioTheme { Screen() } }`
- **Theme**: `SplitAudioTheme` composable — `MaterialTheme(colorScheme = …)`
- **Dynamic color**: Android 12+ uses `dynamicLightColorScheme` / `dynamicDarkColorScheme`
  derived from the user's wallpaper. Android 11 and below falls back to a seeded M3
  baseline purple palette (defined in `Theme.kt`).
- **Edge-to-edge**: `enableEdgeToEdge()` in each Activity's `onCreate`.

## Color roles

All colors resolve from `MaterialTheme.colorScheme.*` — zero hardcoded ARGB literals
in any screen. The hierarchy is **reference/system tokens → semantic roles → component
tokens**, applied automatically by the M3 Compose library.

| Role                             | Where it is used                                                     |
| -------------------------------- | -------------------------------------------------------------------- |
| `colorPrimary`                   | Start button (filled), section header tint, output chip (bt)        |
| `colorOnPrimary`                 | text on filled button                                               |
| `colorPrimaryContainer`         | status card (active state), Shizuku chip (ok), output chip (bt)      |
| `colorOnPrimaryContainer`       | text on primary container                                           |
| `colorSecondary`                 | output chip (wired), secondary accent                               |
| `colorSecondaryContainer`       | output chip (wired)                                                  |
| `colorTertiary`                  | Customize section header tint, output chip (speaker)                 |
| `colorTertiaryContainer`        | recommended-status chip, output chip (speaker)                       |
| `colorError`                     | (reserved for destructive/error emphasis)                            |
| `colorErrorContainer`           | error-status chip (Shizuku not running)                              |
| `colorSurface`                   | screen background, default card background                           |
| `colorOnSurface`                 | primary body text                                                    |
| `colorSurfaceVariant`           | status card (idle state), default output chip                        |
| `colorOnSurfaceVariant`         | secondary body text, icon tints on setup rows                        |
| `colorSurfaceContainerLow`      | setup/customize card backgrounds                                     |
| `colorOutline`                   | (reserved for outlined component borders)                            |
| `colorOutlineVariant`           | `HorizontalDivider` between setup rows and customize sections       |

### Status card color mapping

The status card uses `CardDefaults.cardColors(containerColor = …)` driven by state:

- **Active** (`"on"`) → `primaryContainer` + `onPrimaryContainer` + `Icons.Filled.GraphicEq`
- **Waiting** (`"off"`) → `secondaryContainer` + `onSecondaryContainer` + `Icons.Filled.Pending`
- **Idle** (`null`) → `surfaceVariant` + `onSurfaceVariant` + `Icons.Filled.RadioButtonUnchecked`

### Output chip color mapping

Each output key maps to a tonal container role:

| Key       | Label    | Icon                | Container             | Content              |
| --------- | -------- | ------------------- | --------------------- | -------------------- |
| `speaker` | Speaker  | `Smartphone`        | `tertiaryContainer`   | `onTertiaryContainer` |
| `bt`      | Bluetooth| `Bluetooth`         | `primaryContainer`    | `onPrimaryContainer`  |
| `wired`   | Wired    | `Headphones`        | `secondaryContainer`  | `onSecondaryContainer`|
| `default` | Default  | `Tune`              | `surfaceVariant`      | `onSurfaceVariant`    |

Color never communicates meaning alone — each chip also carries a leading icon and
a localized label.

## Typography

Uses the M3 default `Typography()` from `androidx.compose.material3`:

| Style              | Used for                                |
| ------------------ | --------------------------------------- |
| `headlineSmall`    | Status card title, AppsActivity app name in bottom sheet |
| `titleMedium`      | Section headers, setup row titles        |
| `bodyLarge`        | "Mode" label, bottom-sheet option labels |
| `bodyMedium`       | Status card supporting text             |
| `bodySmall`        | Setup row subtitles, mode description, multi-focus description |
| `labelMedium`      | Status chips, output chips              |

## Shapes

Uses the M3 default `Shapes()`:

| Component               | Corner radius |
| ----------------------- | ------------- |
| Cards                   | 16 dp (large)     |
| Buttons                 | 16 dp (large)     |
| Search field            | 28 dp (extraLarge)|
| Chips                   | 8 dp (small)      |

## Elevation

All cards use tonal elevation only (`surfaceContainerLow` container color) — no shadow
elevation. The M3 guidance is to prefer tonal relationships over shadows for routine
hierarchy. The status card stands out via its state-colored container, not via shadow.

## Components

| Existing UI element         | M3 Compose component chosen | Rationale |
| -------------------------- | --------------------------- | --------- |
| Hero gradient card         | (removed) — `LargeTopAppBar` replaces it | Cleaner M3 pattern; the title is the hero. |
| Status card                | `Card` with state-driven `cardColors` | Status-driven color remains; uses M3 semantic roles instead of hardcoded colors. |
| Start/Stop buttons         | `Button` (filled) + `OutlinedButton` | Filled = highest emphasis (Start); outlined = medium emphasis (Stop). |
| Required-setup rows        | `Card` containing custom `Row` + `StatusChip` + `TextButton` | Custom row gives precise layout; `TextButton` is the low-emphasis action. |
| Shizuku/Accessibility chips | `Surface` with `shape = small` + tonal container color | Status badges, not interactive — a `Chip` would imply interactivity. |
| Mode toggle (Simple/Custom) | `SingleChoiceSegmentedButtonRow` + `SegmentedButton` | Correct M3 component for a compact mutually-exclusive choice. |
| Multi-focus switch         | `Switch` (M3) inside a `ListItem` | Native M3 switch; `ListItem` provides the leading icon + headline + supporting text layout. |
| "Choose output per app"    | `FilledTonalButton` (inside `AnimatedVisibility`) | Tonal sets it apart from the filled Start button; animates in when Custom mode is selected. |
| AppsActivity search field  | `OutlinedTextField` with `shape = extraLarge` | M3 outlined text field with pill-shaped corners for search. |
| AppsActivity list          | `LazyColumn` of `ListItem`s | M3 list item with leading/trailing content slots. |
| Output picker dialog       | `ModalBottomSheet` + `RadioButton` rows | M3 bottom sheet is more modern and mobile-native than a center dialog. |

## States

| Component        | States covered                                  |
| ---------------- | ----------------------------------------------- |
| Start/Stop       | default, pressed, disabled (when starting/stopping), loading (`CircularProgressIndicator`) |
| SegmentedButton  | selected, unselected, pressed                   |
| Switch           | on, off, disabled (when Shizuku not granted), loading (`isTogglingMf`) |
| Chips             | ok (primaryContainer), warning (tertiaryContainer), error (errorContainer) |
| Status card      | active, waiting, idle — each with its own container color + icon + text |
| Loading          | `CircularProgressIndicator` inside buttons during async operations |
| Empty state      | Custom mode + no apps → inline hint text        |
| Error feedback   | `Snackbar` shown if `Route.start` returns a deploy/map failure |

## Motion

- `LargeTopAppBar` collapses/expands on scroll (built-in M3 motion).
- `AnimatedVisibility` with `fadeIn() + expandVertically()` for the "Choose output per
  app" button when Custom mode is selected.
- `ModalBottomSheet` uses the default M3 drag/dismiss animation.
- No custom motion — the app is a settings/status UI; motion would be noise.

## Accessibility

- All icon-only `IconButton`s have `contentDescription`.
- Status icons have `contentDescription = null` (they're decorative; the adjacent text
  conveys the status).
- Touch targets: `Button`/`OutlinedButton`/`FilledTonalButton` default to 48 dp min
  height; `ListItem` rows are 56 dp+; `Switch` thumb area is 48 dp.
- Contrast: M3 color scheme guarantees AA contrast for all `on*` / container pairings.
- Dark theme: `isSystemInDarkTheme()` drives the scheme selection; all colors adapt.
- Focus order: Compose's default traversal order (top-to-bottom, following the
  `LazyColumn` item order).
- Font scaling: all text uses `sp` units; layout uses `WRAP_CONTENT`-equivalent
  Compose defaults.

## Adaptive behavior

- Screen widths 360–600 dp: single-pane layout, `LargeTopAppBar` collapses on scroll.
- Screen widths > 600 dp: cards fill the available width (no max-width cap yet — a
  future iteration could center the content column).
- Edge-to-edge: `enableEdgeToEdge()` + `Scaffold` padding handles system bar insets
  automatically.

## Self-audit (per skill `material-design-3-ui`)

| Category              | Score | Notes |
| --------------------- | ----- | ----- |
| Task clarity          | 2     | Status + Start/Stop are the obvious first read. |
| Information hierarchy  | 2     | Status → setup → customize, in that order, in a `LazyColumn`. |
| Component semantics   | 2     | `Button`/`OutlinedButton`/`FilledTonalButton`/`SegmentedButton`/`Switch`/`Card`/`ListItem`/`ModalBottomSheet` — all match purpose. |
| Token discipline       | 2     | No raw ARGB literals in screens; all via `MaterialTheme.colorScheme.*`. |
| Adaptive behavior      | 2     | `LargeTopAppBar` collapses; `LazyColumn` scrolls; edge-to-edge insets. |
| States & feedback     | 2     | Loading (`CircularProgressIndicator`), empty (custom mode + no apps), error (chip + snackbar), disabled (switch/button), success (status card → primaryContainer). |
| Accessibility          | 2     | 48 dp targets, `contentDescription` on icon buttons, dark theme, dynamic color. |
| Expressive restraint   | 2     | One state-colored card, one `AnimatedVisibility`. No M3 Expressive APIs abused. |

Total: 16/16.
