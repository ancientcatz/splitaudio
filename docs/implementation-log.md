# Implementation Log

(Reverse chronological — most recent first.)

## 2026-10-25 — Stage F: CI green + draft release created

- Pushed `feat/material3-redesign` (commit `b68a5d9`) to remote.
- CI workflow `Android CI (Material 3)` triggered automatically.
- Run ID `40190111791`: `completed` / `success` in 3m49s.
  - Build job: ✓ green. Builds approute.dex + debug APK + R8 release APK + lint +
    R8 verification + APK metadata sanity check. Uploads 4 artifacts (debug APK,
    release APK, R8 reports, lint report).
  - Release job: skipped (trigger was a push, not a tag — expected).
- Created draft GitHub release `v0.4.0` on tag pointing at commit `b68a5d9`:
  - URL: https://github.com/ancientcatz/splitaudio/releases/tag/untagged-711a9cb98e9371574af2
  - Title: "SplitAudio v0.4.0 (Material 3 redesign — DRAFT, UNSIGNED)"
  - Asset: `splitaudio-v0.4.0-release-UNSIGNED.apk` (2,408,055 bytes)
  - MD5 of asset matches local build: `b3444b8b9f805153d14cf105e80e5a5b` ✓
  - `isDraft=true`, `tagName=v0.4.0`, `targetCommitish=b68a5d9...` ✓
- Verified the release APK's contents:
  - `classes.dex` (1,228,076 B) contains `Lrikka/shizuku/Shizuku;`,
    `Lrikka/shizuku/ShizukuProvider;`, and the strings `state` and `mode` (the
    AppRoute socket-protocol commands).
  - `assets/approute.dex` (17,148 B) MD5 matches source: `ec80121c2275f8c1b43d475b346b743c`.
  - `assets/approute.sh` (2,657 B) preserved.
  - `Theme.Material3.*` styles + `isMaterial3Theme` attr packaged.
  - All manifest components preserved (MainActivity, AppsActivity, MonitorService,
    VolKeyService, BootReceiver, ShizukuProvider, InitializationProvider).
- Outstanding: signing secrets (4 × `ANDROID_KEYSTORE_*`) — user must configure
  these and re-run the workflow on the `v0.4.0` tag to produce a signed APK and
  replace the unsigned asset on the draft release.

## 2026-10-25 — Stage A-E: Material 3 redesign + R8 + local build

- Stage A (Foundations):
  - Added `com.google.android.material:material:1.12.0` + `androidx.appcompat:appcompat:1.7.0`.
  - Created `Theme.SplitAudio` (parent `Theme.Material3.DayNight.NoActionBar`),
    edge-to-edge, transparent system bars, dynamic color on Android 12+.
  - Created `res/values{,-night}/colors.xml` with M3 baseline light/dark
    palettes (purple seed).
  - Created `M3.kt` — resolves `?attr/color*` M3 roles to ARGB Ints.
  - Updated `AndroidManifest.xml` `<application android:theme="@style/Theme.SplitAudio">`.
- Stage B (Primary screen — MainActivity):
  - Migrated from `Activity` to `AppCompatActivity`.
  - `MaterialCardView` for all cards (hero, status, required-setup, customize).
  - `MaterialButton` filled (Start) + outlined error-toned (Stop).
  - `MaterialButtonToggleGroup` (single-selection) for Simple/Custom mode.
  - `MaterialSwitch` for multi-focus toggle.
  - All colors via `M3.*` (no hardcoded ARGB literals).
  - Edge-to-edge via `WindowCompat` + `WindowInsetsCompat`.
  - Status-bar icon tint follows surface darkness.
- Stage C (Secondary screen — AppsActivity):
  - `AppCompatActivity`; `TextInputLayout` + `TextInputEditText` (M3 outlined).
  - `MaterialCardView` list rows with `outlineVariant` border + `surfaceContainer`
    background.
  - `MaterialAlertDialogBuilder` for output picker (replaces platform
    `AlertDialog.Builder`).
- Stage D (Integration):
  - Edge-to-edge window insets applied.
  - Light/dark theme switching via `Theme.Material3.DayNight`.
  - Status-bar / nav-bar icon tints follow surface darkness.
- Stage E (R8 + regression testing):
  - Enabled `isMinifyEnabled=true`, `isShrinkResources=true`,
    `android.enableR8.fullMode=true`.
  - Wrote `app/proguard-rules.pro` with `-keep` for `rikka.shizuku.Shizuku` +
    `rikka.shizuku.aidl.**` (Shz.kt reflects on hidden `newProcess` method).
  - Local build: debug APK 11.9 MB; release APK (R8) 2.4 MB.
  - R8 mapping (10 MB) confirms obfuscation. Shizuku class + Shz.newProcess method
    kept. All manifest components preserved.

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
