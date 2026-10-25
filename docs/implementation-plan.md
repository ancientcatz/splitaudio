# Implementation Plan

## Stage A — Foundations

- [A1] Add `com.google.android.material:material:1.12.0` dependency.
- [A2] Create `res/values/themes.xml` and `res/values-night/themes.xml` with
      `Theme.Material3.DayNight.NoActionBar` parent, dynamic color enabled.
- [A3] Create `res/values/colors.xml` and `res/values-night/colors.xml` with seeded M3
      tonal palette (light/dark schemes).
- [A4] Update `AndroidManifest.xml` `<application android:theme="@style/Theme.SplitAudio">`.
- [A5] Create `M3.kt` helper object that resolves `?attr/color*` to ARGB ints for use in
      programmatic UI.
- [A6] Build & verify theme loads (no functional changes yet).
- [A7] Push docs update.

## Stage B — Primary screens

- [B1] `MainActivity`: replace `C_*` literals with `M3.color(this, ...)`. Hero gradient
      uses primary + tertiary (not purple-pink rainbow).
- [B2] Status card: state-driven color via `M3.statusColor(state)` returning a semantic
      role.
- [B3] Start/Stop: `MaterialButton` filled (Start) + outlined (Stop, error-toned).
- [B4] Required-setup rows: `MaterialCardView` + `MaterialButton` text style.
- [B5] Build & launch sanity test (aapt, install on emulator if available).
- [B6] Push docs update.

## Stage C — Secondary screens

- [C1] `AppsActivity`: replace gradient background with `?attr/colorSurface`; replace
      `EditText` with `TextInputLayout` + `TextInputEditText`.
- [C2] List rows: `MaterialCardView` background, icon + label + output chip.
- [C3] Output picker: `MaterialAlertDialogBuilder`.
- [C4] `MainActivity` mode toggle: `MaterialButtonToggleGroup` (Simple/Custom).
- [C5] Multi-focus switch: `MaterialSwitch`.
- [C6] Apps button: `MaterialButton` tonal.
- [C7] Build & verify.

## Stage D — Integration

- [D1] Edge-to-edge window insets (no more hardcoded `dp(20)` top).
- [D2] Verify back navigation, theme switching, orientation change.
- [D3] Verify `MonitorService` foreground notification icon still renders.
- [D4] Verify Shizuku flow + `Route.start` works as before (no behavioral changes).
- [D5] Verify `VolKeyService` still intercepts volume keys.

## Stage E — Regression testing

- [E1] Build debug APK, compare size to baseline.
- [E2] Build release APK with `isMinifyEnabled=true` + R8 full mode + resource shrinking.
- [E3] Inspect R8 mapping/usage/configuration reports.
- [E4] Run lint on release variant.
- [E5] Update `testing.md` with results.
- [E6] Push docs.

## Stage F — Release

- [F1] Add `github/workflows/android.yml` rewritten with: JDK 17, Gradle 8.9,
      build approute.dex, debug + release builds, R8 verification, artifact uploads,
      draft release job.
- [F2] Document required GitHub secrets: `ANDROID_KEYSTORE_BASE64`,
      `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`.
- [F3] Push, monitor CI to completion.
- [F4] Create draft GitHub release (or workflow creates it) — leave as draft.
- [F5] Verify release + asset.

## Risks

| Risk | Mitigation |
| ---- | ---------- |
| Shizuku reflection on `newProcess` is removed by R8 | Keep rule on `rikka.shizuku.Shizuku` and the `newProcess` method. |
| `MaterialSwitch` requires Material 1.7+; we use 1.12.0 | Safe — 1.12.0 is well above the threshold. |
| Edge-to-edge on Android 10 (minSdk 29) | Use `WindowCompat.setDecorFitsSystemWindows` (compatible back to API 14 via appcompat-windows) — but we don't depend on appcompat. Apply insets manually via `WindowInsets`. |
| `MaterialButtonToggleGroup` exists in 1.7+ | Safe. |
| R8 full mode (`android.enableR8.fullMode`) on AGP 8.5 | Supported since AGP 8.0; safe on 8.5.2. |
| No emulator available for runtime verification | Documented in `testing.md`; rely on aapt + R8 mapping + CI green. |

## Acceptance criteria

- Debug APK builds.
- Release APK builds with `isMinifyEnabled=true`, `isShrinkResources=true`,
  `android.enableR8.fullMode=true`.
- R8 mapping file contains obfuscated class names (proof obfuscation ran).
- APK installs (verified by `aapt dump badging` showing correct applicationId).
- Existing functionality (Shizuku, AppRoute, services) preserved — verified by code
  review.
- Material 3 theme applied — verified by `aapt dump resources` showing M3 styles.
- CI workflow runs green on push.
- Draft release contains the verified APK.
