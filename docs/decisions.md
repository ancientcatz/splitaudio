# Decisions

## D1 — Skill verification approach

The mission brief requires "official validation checks provided by the repository".
The `material-design-3-ui-skill` repo's `SKILL.md` does not ship a binary validation
script; the skill defines its own self-audit (a scoring rubric in `SKILL.md`). We
treat the skill's self-audit as the canonical validation check and record the score
in `design-system.md`.

## D2 — Material 3 implementation: programmatic UI + Material Components

The existing app is built entirely with programmatic `View` calls (no XML layouts, no
Compose). Two options were considered:

- **Option A**: Migrate `MainActivity` and `AppsActivity` to Jetpack Compose with
  `androidx.compose.material3`. Pros: cleanest M3. Cons: requires Compose BOM, activity
  component change, lifecycle/view-model refactor, and a much larger diff. High risk of
  behavioral regression in the Shizuku/AppRoute interaction paths.
- **Option B**: Keep programmatic UI; add Material Components for Android dependency;
  use `MaterialButton`, `MaterialCardView`, `MaterialSwitch`, `MaterialButtonToggleGroup`,
  `MaterialAlertDialogBuilder`, `TextInputLayout`; resolve M3 color roles via
  `ContextCompat.getColor` + `?attr/color*` theme attributes; ship a real M3 theme in
  `themes.xml`. Pros: small diff, preserves all behavior, real M3 design. Cons: less
  "modern" than Compose.

**Decision**: Option B. The mission brief explicitly says "Do not replace functional
screens with mockups or fake interactions" and "Avoid unnecessary architectural
rewrites". Option B is the lowest-risk path to a real Material 3 design.

## D3 — Color system: dynamic color (Android 12+) with seeded fallback

The M3 theme uses `Theme.Material3.DayNight` parent with `android:colorSchemeDynamic`
enabled on Android 12+ (the OS supplies a per-wallpaper palette). On Android 11 and
below, a seeded palette (primary = a tonal purple that nods to the original brand color)
is supplied via `values/colors.xml` and `values-night/colors.xml`.

## D4 — Status card color mapping

The original app mapped live-status → hardcoded color (`C_GREEN`/`C_BLUE`/`C_GREY`).
The redesign maps to semantic M3 roles:
- Active → `colorPrimary` (filled card, `colorOnPrimary` text).
- Waiting → `colorSecondary`.
- Idle → `colorSurfaceVariant` + `colorOnSurfaceVariant`.
- Error (Shizuku down) → `colorError`.

This preserves the visual coding (status drives color) while using semantic roles.

## D5 — R8 full mode

`android.enableR8.fullMode=true` is enabled. This is supported on AGP 8.0+ and is
safe on AGP 8.5.2. It enables more aggressive optimizations than compat mode. We audit
keep rules narrowly: only `rikka.shizuku.Shizuku` (for the `newProcess` reflection) and
`rikka.shizuku.aidl.*` (for the IPC stubs).

## D6 — No Gradle wrapper added

The repository ships without a Gradle wrapper. Adding one would be a helpful change,
but it is out of scope for the Material 3 redesign (it is a build-tooling change, not a
UI/UX change). CI continues to use `gradle/actions/setup-gradle@v3` with
`gradle-version: "8.9"`. Documented in `troubleshooting.md`.

## D7 — No unit tests added

The app is a thin orchestration layer over Shizuku + a runtime DEX. Meaningful unit
tests would require mocking the Shizuku binder and the AppRoute socket protocol — out
of scope. CI continues to compile + assemble + lint.

## D8 — No emulator in sandbox

Runtime verification of the APK on a device/emulator is not possible in the local
sandbox. Verification is limited to: (1) successful build, (2) `aapt dump` metadata,
(3) R8 mapping/usage reports, (4) CI green status. This is documented honestly in
`testing.md` and `release-checklist.md`; the user is expected to do the on-device smoke
test before publishing the release.

## D9 — Workflow YAML fix

The original workflow has `on.push.branches: ain]`, which is invalid YAML (looks like a
typo for `[main]`). The rewritten workflow uses `branches: [feat/material3-redesign, main]`
and adds explicit `permissions`, R8 verification, and a release job gated on a `v*` tag
or `workflow_dispatch`.
