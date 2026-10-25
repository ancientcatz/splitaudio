# Accessibility

## Requirements

- Minimum 48 dp touch targets on all interactive controls.
- Text contrast ≥ 4.5:1 for body text, ≥ 3:1 for large text (WCAG AA).
- Content descriptions on icon-only buttons.
- Logical focus order (top-to-bottom, left-to-right).
- Readable at font scale 1.3 (Android "Large" setting) and 2.0 (Android "Largest").
- Dark theme support (system-managed via `Theme.Material3.DayNight`).
- Reduced-motion: M3 default stateListAnimator is short and state-explaining; no
  long-running animation in the app.
- RTL: layouts use `Gravity.START`/`END` (replacing `LEFT`/`RIGHT`) and symmetric padding.

## Checks performed

| Check                              | Result |
| ---------------------------------- | ------ |
| Touch targets ≥ 48 dp              | M3 `MaterialButton` min height 48 dp; `MaterialSwitch` thumb area 48 dp; row tap area ≥ 56 dp. |
| Content descriptions               | Icon-only buttons get `contentDescription`; icon+text buttons get both. Status chips are non-interactive, so `contentDescription` carries the full state sentence. |
| Contrast                            | M3 theme builder palettes guarantee AA contrast for primary/onPrimary, secondary/onSecondary, surface/onSurface pairings. Status card text on primary/secondary/error surfaces all use `on*` role counterparts. |
| Focus order                         | Declared in source order: hero (non-focusable), status buttons, required-setup buttons, mode toggle, multi-focus switch. |
| Font scale 1.3 / 2.0                | All text uses `?attr/textAppearance*` or sp-based sizes; layout uses `WRAP_CONTENT` vertically; no fixed-height rows. |
| RTL                                 | `Gravity.CENTER_VERTICAL` + symmetric padding; `LinearLayout.HORIZONTAL` children order is logical (icon → text → chip) and flips correctly. |
| Reduced motion                      | No custom animators; M3 defaults respect `Settings.Global.ANIMATOR_DURATION_SCALE`. |
| Dialog accessibility                | `MaterialAlertDialogBuilder` produces a focusable, scrollable, dismissable dialog with single-choice items. |

## Findings and fixes

| Finding | Fix |
| ------- | --- |
| `Switch` is deprecated in API 34 (replaced by `SwitchCompat`/`MaterialSwitch`). | Migrated to `MaterialSwitch`. |
| `MainActivity.row()` icon column was a fixed 40 dp with no contentDescription on the icon (icons are emoji in `TextView`, so technically accessible). | Left as emoji in TextView — they are part of the visible label and screen readers read them. |
| Touch target on chip was previously `dp(10)` horizontal + `dp(4)` vertical padding → ~30 dp tall. | Increased to `dp(12)` + `dp(6)` (still a status badge, not interactive). |
| Search field had no `imeOptions` and no `actionSearch`. | Added `imeOptions = IME_ACTION_SEARCH`. |
| AppsActivity list rows had no `contentDescription`. | Row `contentDescription` set to "<app name>: <output label>". |
| Status card used low-contrast white-on-light-blue. | M3 `onSecondary` / `onError` / `onPrimary` guaranteed contrast. |

## Remaining issues

- The foreground notification on Android 13+ requires `POST_NOTIFICATIONS` permission;
  the user is prompted at first launch but the permission state is not surfaced in the
  UI. Out of scope for the redesign.
