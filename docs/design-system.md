# Material 3 Design System (SplitAudio)

This document specifies the Material 3 design system applied to the SplitAudio redesign.
It is written by applying the `material-design-3-ui` skill (v1.1.1).

## Design intent

- **Primary user goal**: route per-app audio output without root. The home screen must
  show, at a glance, whether routing is on, what the required permissions need, and how
  to switch between Simple and Custom modes.
- **Information hierarchy**: status first, required setup second, customization third.
- **Adaptive behavior**: SplitAudio is a single-pane phone app; the layout must remain
  readable from 360 dp to 600 dp wide and in portrait/landscape. No tablet-specific
  panes are added (would be decorative only).
- **M3 Expressive**: applied sparingly — a single tonal hero gradient and color-coded
  status card. All other surfaces stay calm.

## Token hierarchy

We use the M3 hierarchy: **reference/system tokens → semantic/system roles → component
tokens**. Screens use semantic roles only — no raw ARGB literals.

## Color roles

The redesign adds `com.google.android.material:material:1.12.0` and uses
`Theme.Material3.DayNight` as the parent theme. Colors are emitted by the M3 theme
builder (a tonal palette seeded by a primary brand color, with light and dark schemes).
The app uses dynamic color on Android 12+ (`android:windowBackground` picks up
`?android:attr/colorBackground` from the dynamic system) and falls back to a fixed
seeded scheme on Android 11 and below.

Semantic roles used by the app:

| Role                  | Where it is used                                                     |
| --------------------- | -------------------------------------------------------------------- |
| `?attr/colorPrimary`  | hero gradient start, primary CTA filled buttons, "active" status    |
| `?attr/colorOnPrimary`| text/icons on primary surfaces, hero title text                      |
| `?attr/colorPrimaryContainer` | Custom mode selected segment, accent headers                  |
| `?attr/colorOnPrimaryContainer`| text on primary container                                  |
| `?attr/colorSecondary`| secondary chips, "waiting" status accent                            |
| `?attr/colorTertiary` | Custom mode button (when not selected)                              |
| `?attr/colorError`    | stop button, "not granted" chip, "Shizuku down" state               |
| `?attr/colorOnError`  | text on error surfaces                                              |
| `?attr/colorSurface`  | default card backgrounds                                            |
| `?attr/colorOnSurface`| primary body text                                                   |
| `?attr/colorOnSurfaceVariant`| secondary body text, chip borders                          |
| `?attr/colorSurfaceVariant`| dividers, secondary card backgrounds                          |
| `?attr/colorOutlineVariant`| thin separators                                          |
| `?attr/colorOutline`  | outlined-button borders                                             |

Status colors (semantic):

- "Split active" → `colorPrimary` (filled status card)
- "Waiting for music" → `colorSecondary`
- "Not running / idle" → `colorSurfaceVariant` + `colorOnSurfaceVariant`
- "Shizuku down" → `colorError`

Output chips (custom mode): speaker / bt / wired / default use secondaryContainer /
tertiaryContainer / primaryContainer / surfaceVariant respectively — color never
communicates meaning alone; each chip also carries a leading emoji (`📱 🎧 🔌 ⚙`) and
a localized label.

## Typography

M3 type scale (used via `TextAppearance.Material3.*` and through `material-button` styles):

| Style              | Used for                                |
| ------------------ | --------------------------------------- |
| HeadlineSmall       | Hero app title                          |
| TitleLarge          | Section headers, AppsActivity title    |
| TitleMedium         | Row titles, dialog title                |
| BodyLarge           | Primary body text                       |
| BodyMedium          | Secondary description text              |
| BodySmall           | Status sub-text, "Last start" line      |
| LabelLarge          | Buttons, segmented buttons              |
| LabelMedium         | Chips, badges                           |

## Shapes

| Component               | Corner radius |
| ----------------------- | ------------- |
| Cards                   | 16 dp (M3 large)  |
| Buttons                 | 20 dp (M3 full ≈ 50% of 40 dp height) |
| Chips                   | 8 dp (M3 small)   |
| Text fields             | 4 dp (M3 extra-small — outlined field corner) |
| Dialog                  | 28 dp (M3 extra-large) |

The previous version used 22 dp corner radius everywhere; we restore the proper M3 scale.

## Spacing & layout

| Token          | Value  | Used for                                |
| -------------- | ------ | --------------------------------------- |
| Screen edge    | 16 dp  | left/right padding on the root layout  |
| Top inset      | 24 dp  | extra top padding (status bar inset)   |
| Bottom inset   | 24 dp  | extra bottom padding (gesture nav)     |
| Card padding   | 16 dp  | internal padding on cards               |
| Card gap       | 12 dp  | margin between cards                    |
| Row vertical   | 8 dp   | padding on rows                          |
| Touch target   | 48 dp  | min height/width for any actionable     |
| Icon size      | 24 dp  | row icons                               |

## Elevation

| Surface           | Elevation |
| ----------------- | --------- |
| Cards             | 1 dp      |
| Status card (active) | 2 dp   |
| FAB / dialog      | 3 dp / 6 dp (default) |
| Hero gradient     | 0 dp (no shadow — uses tonal gradient for separation) |

## Components

| Existing UI element         | M3 component chosen | Rationale |
| -------------------------- | ------------------- | --------- |
| Hero gradient card         | `MaterialCardView` filled with primary→tertiary gradient | Maintains brand identity, but tonal rather than rainbow. |
| Status card                | `MaterialCardView` filled with status semantic color | Status-driven color remains; we use M3 semantic roles instead of hardcoded `C_GREEN`/`C_BLUE`. |
| Start/Stop buttons          | `MaterialButton` filled (Start=primary) + outlined (Stop=error-toned) | Distinguish primary from destructive action. |
| Required-setup rows        | `MaterialCardView` + `MaterialButton` text + custom chip | Text button keeps emphasis low; chip is custom (M3 `Chip` would need a `ChipGroup` for one-per-row, too heavy here). |
| Shizuku/Accessibility chips | custom `TextView` with shape drawable | Stayed custom because they are status badges, not interactive — M3 `Chip` implies interactivity. |
| Mode toggle (Simple/Custom) | `MaterialButton` toggle group (`MaterialButtonToggleGroup`) | M3 replacement for the two-button segment; correct component for binary choice. |
| Multi-focus switch         | `MaterialSwitch` (M3) | Native M3 switch replaces the deprecated `android.widget.Switch`. |
| "Choose output per app"    | `MaterialButton` tonal | Tonal sets it apart from primary Start button. |
| AppsActivity list          | `ListView` + custom row card | Not migrated to Compose; ListView is fine for a fixed-size app list. Each row gets a `MaterialCardView` background. |
| Search field                | `TextInputLayout` + `TextInputEditText` (outlined) | M3 outlined text field replaces custom `EditText` shape. |
| Output picker dialog       | `MaterialAlertDialogBuilder` | M3 dialog replaces platform `AlertDialog.Builder`. |

## States

| Component        | States covered                                  |
| ---------------- | ----------------------------------------------- |
| Start/Stop       | default, pressed, disabled (when Shizuku not granted) |
| Mode toggle      | selected, unselected, pressed                   |
| Switch           | on, off, disabled (when Shizuku down)           |
| Chips             | state 0=missing (error), 1=ok (primary), 2=recommended (secondary) |
| AppsActivity rows | default, pressed (selector)                     |
| Empty state      | Custom mode + no apps set → inline hint text    |

Loading: refresh thread polls every 600 ms with a 600 ms delay between calls; the status
card shows "…" until the first response, never a permanent spinner.

## Motion

- Cards animate elevation/alpha on press (M3 default stateListAnimator).
- Switch uses M3 default thumb animation.
- Dialog uses M3 default fade/scale transition.
- No custom motion: the app is a settings/status UI, motion would be noise.

## Accessibility

See `accessibility.md` for the full checklist.

## Dark theme

- `Theme.Material3.DayNight` provides a dark scheme automatically.
- All colors resolve from `?attr/color*` tokens, so dark mode works without per-screen
  branches.
- The hero gradient uses primary + tertiary in dark; in light it uses primary + tertiary
  at higher tonal levels so the title remains readable in both schemes.
- `MonitorService` notification uses `?attr/colorPrimary` for the channel, so dark-mode
  users see the same status color as light-mode users.

## Adaptive behavior

- Screen widths 360–600 dp: single-pane layout, no changes.
- Screen widths > 600 dp: cards max-width 600 dp, centered — wider space is unused rather
  than stretched, per M3 guidance (wider space must improve context, not just stretch).
- Edge-to-edge: enabled via `WindowCompat.setDecorFitsSystemWindows(window, false)` plus
  `WindowInsets` application on the root view — replaces the fixed `dp(20)`/`dp(24)`
  top/bottom padding.

## Self-audit (per skill `material-design-3-ui`)

| Category              | Score | Notes |
| --------------------- | ----- | ----- |
| Task clarity          | 2     | Status + start/stop are the obvious first read. |
| Information hierarchy | 2     | Status → required → custom, in that order. |
| Component semantics   | 2     | MaterialButton (filled/outlined/tonal), MaterialSwitch, MaterialCardView, MaterialAlertDialogBuilder — all match purpose. |
| Token discipline      | 2     | No raw ARGB literals in screens; all colors via `?attr/color*`. |
| Adaptive behavior     | 1     | Cards centered on >600 dp; no list-detail yet (would be over-engineering for this app). |
| States & feedback     | 2     | Loading (status text "…"), empty (custom mode + no apps), error (chip color + label), disabled (switch/button), success (status card → primary). |
| Accessibility         | 2     | 48 dp targets, content descriptions on icon buttons, dark theme, scaling tested. |
| Expressive restraint  | 2     | One tonal gradient (hero), one color-coded status card. No M3 Expressive APIs abused. |

Total: 15/16 (one 1 — adaptive — improves with a tablet mode in a future iteration).
