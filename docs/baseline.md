# Baseline (pre-redesign)

This document records the state of the SplitAudio repository **before** the Material 3
redesign. The objective is to distinguish pre-existing problems from regressions
introduced by the redesign.

## Environment

| Component          | Version                              |
| ------------------ | ------------------------------------ |
| OS                 | Debian 13 (bookworm) container       |
| JDK                | Temurin 17.0.13 (Temurin17)          |
| Android SDK        | cmdline-tools 12.0, platform-tools 35.0, build-tools 34.0.0, platforms;android-34 |
| Gradle             | 8.9 (no wrapper in repo — uses external Gradle) |
| Android Gradle Plugin | 8.5.2                            |
| Kotlin             | 1.9.24                               |

## Original application facts (verified from source)

- **Application ID**: `app.splitaudio`
- **Version**: `versionCode=6`, `versionName=0.3.0`
- **SDK**: `compileSdk=34`, `minSdk=29`, `targetSdk=34`
- **Theme**: `@android:style/Theme.DeviceDefault.Light` (system theme, not Material 3)
- **No XML layouts** — the UI is built entirely in Kotlin with `LinearLayout`, `TextView`,
  `Button`, `Switch`, `ListView`, `EditText`, `ScrollView`, `ImageView`.
- **Hardcoded colors**: `MainActivity` defines 13 hardcoded ARGB constants
  (`C_PURPLE`, `C_PINK`, `C_INDIGO`, `C_BLUE`, `C_TEAL`, `C_GREEN`, `C_RED`, `C_ORANGE`,
  `C_GREY`, `C_TEXT`, `C_TEXT_2`, `C_BG_TOP`, `C_BG_BOTTOM`). `AppsActivity` reuses these.
- **Gradle wrapper absent** — the project assumes an externally installed Gradle 8.9.
- **Existing CI workflow** at `.github/workflows/android.yml` builds debug and unsigned
  release APKs. The `on.push.branches` field is malformed in the checked-out file
  (`branches: ain]`) — this is a pre-existing bug we will fix in the redesigned workflow.
- **Existing R8/ProGuard rules**: none. The release build type has `isMinifyEnabled = false`.

## Build results (recorded at commit `a0777eb`)

| Variant   | Task                | Result        | Artifact size | Notes                            |
| --------- | ------------------- | ------------- | ------------- | -------------------------------- |
| Debug     | `assembleDebug`     | SUCCESS       | 5,530,015 B   | built after `route/build_dex.sh` |
| Release   | `assembleRelease`   | SUCCESS       | 4,685,254 B   | unsigned, `isMinifyEnabled=false` |
| Lint      | `:app:lintRelease`  | SUCCESS       | —             | no critical issues                |
| Unit tests| none defined         | N/A           | —             | the project has no `*Test` sources |

## Pre-existing issues (carried over, not introduced by the redesign)

1. **No Gradle wrapper** — the repository ships without `gradlew`, `gradlew.bat`, or
   `gradle/wrapper/`. CI uses `gradle/actions/setup-gradle@v3` with `gradle-version: 8.9`
   which masks this; local builds need an external Gradle install.
2. **Malformed workflow YAML** — `on.push.branches: ain]` is not valid YAML for a list.
   The intent appears to be `[main]`. CI may behave as if no branch filter is set.
3. **No tests** — there are no unit or instrumentation tests in `app/src/`. CI "test"
   steps degenerate to compile + assemble.
4. **No `AGENTS.md`** — the mission brief referenced `AGENTS.md` but the repository does
   not contain one.
5. **`MonitorService.kt:54` warning** — Gradle reports "Parameter 'why' is never used".
   Pre-existing; harmless.
6. **No dark theme** — `Theme.DeviceDefault.Light` is light-only; the app is unreadable
   in dark mode on devices that force dark.
7. **Accessibility limitations** — touch targets on the chip/badge widgets are below
   48 dp; section heading levels and content descriptions for status chips are absent.

## Environmental constraints

- **No Android emulator/device** available in the local sandbox. Runtime verification of
  the R8-optimized APK on a real device is performed via CI (where an Ubuntu runner is
  available, though still without a managed emulator — see `testing.md`). The redesign
  is verified through: (a) successful local build of debug and release APKs,
  (b) APK metadata inspection via `aapt`, (c) CI green status, (d) R8 mapping/usage
  inspection.

## Functional areas to preserve

- Shizuku permission request flow and reflection on the hidden
  `rikka.shizuku.Shizuku#newProcess` method (see `Shz.kt`).
- Deployment of `assets/approute.dex` + `assets/approute.sh` to `/data/local/tmp/` via
  Shizuku, including the `mv -f .new` atomic-rename dance.
- Socket protocol with the AppRoute helper at `127.0.0.1:47821`
  (`state`, `mode`, `vol <dir> <pkg>`).
- `MonitorService` foreground notification (specialUse FGS type), audio playback/device
  callbacks, 60 s tick, 30 s retry throttle.
- `VolKeyService` accessibility service — volume key filtering, foreground package match,
  consumed-key tracking.
- `BootReceiver` autostart on `BOOT_COMPLETED` / `MY_PACKAGE_REPLACED`.
- `Outputs` SharedPreferences schema (`outputs.xml`, keys `custom` and `o:<pkg>`).
- Per-app output selection dialog and the `Outputs.KEYS` ordering.
- Multi-focus toggle (`approute.sh multifocus on|off` → `multi_audio_focus_enabled`).
- Localization (en + zh-rTW).

## Conclusion

The baseline builds cleanly. The redesign starts from a known-good build, so any
build failure introduced later is a regression attributable to the redesign.
