# Testing

## Local build commands

```bash
export JAVA_HOME=/home/z/jdk17
export ANDROID_HOME=/home/z/android-sdk
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH

# 1. Build the routing helper DEX (route/AppRoute.java -> assets/approute.dex)
sh route/build_dex.sh

# 2. Debug APK (Compose + Material 3)
gradle --no-daemon --console=plain assembleDebug

# 3. Release APK (R8 full mode + resource shrinking)
gradle --no-daemon --console=plain assembleRelease

# 4. Lint
gradle --no-daemon --console=plain :app:lintRelease
```

## Baseline (commit `a0777eb` — pre-redesign)

| Step                | Result      | Artifact size |
| ------------------- | ----------- | ------------- |
| `route/build_dex.sh`| SUCCESS     | 17,148 B dex  |
| `assembleDebug`      | SUCCESS     | 5,530,015 B   |
| `assembleRelease` (no minify) | SUCCESS | 4,685,254 B |
| `:app:lintRelease`   | SUCCESS     | —             |

## v0.4.1 — Compose Material 3 rewrite (commit `c74f831`)

| Step                | Result      | Artifact size | Notes |
| ------------------- | ----------- | ------------- | ----- |
| `route/build_dex.sh`| SUCCESS     | 17,148 B dex  | unchanged from baseline |
| `assembleDebug`      | SUCCESS     | 62,500,276 B  | large due to Compose tooling in debug |
| `assembleRelease` (R8 full mode) | SUCCESS | 3,946,816 B | R8 strips Compose tooling; smaller than baseline |
| `:app:lintRelease`   | SUCCESS     | —             | no critical issues |
| R8 mapping generated | YES         | 29,850,572 B  | confirms obfuscation ran |
| R8 usage report      | YES         | 4,761,895 B   | confirms dead-code removal |
| R8 configuration     | YES         | 38,337 B      | contains `full-mode` flag |
| Resource shrinking report | YES    | 993,709 B     | confirms resource shrinking ran |

## CI workflow

**Workflow name**: `Android CI (Material 3)` (`.github/workflows/android.yml`).

### v0.4.1 run (Compose rewrite)

- Run ID: `40194565007`
- Branch: `feat/material3-redesign`
- Commit: `c74f831bc10bbdd88f5427461bc5bb550c38ccfe`
- Trigger: push
- Status: `completed`
- Conclusion: `success`
- Duration: 4m58s
- Build job: ✓ green. Builds approute.dex + debug APK + R8 release APK + lint +
  R8 verification + APK metadata sanity check. Uploads 4 artifacts.
- Release job: skipped (trigger was a push, not a tag — expected).

### v0.4.0 run (superseded View-based reskin)

- Run ID: `40190111791`
- Commit: `b68a5d9`
- Status: `completed` / `success` in 3m49s.
- Superseded by v0.4.1 after user review found broken layout.

## R8 verification (v0.4.1 post-build)

| Check | Result |
| ----- | ------ |
| R8 compiler version | 8.5.35 (matches AGP 8.5.2) |
| `android.enableR8.fullMode=true` | verified in `configuration.txt` |
| `isMinifyEnabled=true` | verified (mapping file produced) |
| `isShrinkResources=true` | verified (`resources.txt` produced) |
| Obfuscation | verified (mapping.txt 29.8 MB, contains `->` renames) |
| `rikka.shizuku.Shizuku` class kept | verified — `rikka.shizuku.Shizuku -> rikka.shizuku.Shizuku:` in mapping |
| `Shz.newProcess` method kept | verified — `app.splitaudio.Shz.newProcess(java.lang.String):16:16 -> c` in mapping |
| Manifest components preserved | verified (aapt dump xmltree shows all activities/services/receivers/providers) |
| `approute.dex` + `approute.sh` preserved as release APK assets | verified |
| Compose classes present in release dex | verified (122 compose-related strings found) |
| `Theme.Material3` styles packaged | verified |

## Release APK metadata (verified)

```
package: name='app.splitaudio' versionCode='8' versionName='0.4.1'
sdkVersion:'29'  targetSdkVersion:'34'  compileSdkVersion:'34'
uses-permission: android.permission.INTERNET
uses-permission: android.permission.FOREGROUND_SERVICE
uses-permission: android.permission.FOREGROUND_SERVICE_SPECIAL_USE
uses-permission: android.permission.RECEIVE_BOOT_COMPLETED
uses-permission: android.permission.POST_NOTIFICATIONS
uses-permission: app.splitaudio.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION
uses-permission: moe.shizuku.manager.permission.API_V23
```

## Unit tests

The project has no unit test sources. No unit tests are added — the app is a thin
orchestration layer over Shizuku + a runtime DEX, and meaningful unit tests would
require mocking the Shizuku binder and the AppRoute socket protocol. Documented as
out of scope in `decisions.md` D7.

## Instrumentation tests

Not configured. The CI runner (ubuntu-latest) does not have a managed Android
emulator, and the local sandbox has no emulator either. Documented as out of scope.

## Runtime verification

Runtime verification of the APK on an actual Android device/emulator is **not possible
in this sandbox** (no Android emulator binary, no AVD, no KVM acceleration).

Verification was limited to:

1. **APK structure**: `aapt dump badging` + `aapt dump xmltree` confirm applicationId,
   version, min/target/compile SDK, permissions, all manifest components.
2. **APK contents**: `unzip -l` + `aapt dump resources` confirm Material 3 theme +
   Compose runtime packaged, `approute.dex` + `approute.sh` intact.
3. **R8 mapping/usage/configuration reports**: confirm obfuscation, shrinking, and
   optimization all ran with `full-mode` enabled.
4. **CI green status** on `feat/material3-redesign`.

**The user is expected to perform the on-device smoke test before publishing the
draft release.** Specifically:
- Install the signed APK (after configuring signing secrets and re-running the
  workflow on the `v0.4.1` tag).
- Cold launch the app.
- Grant Shizuku permission.
- Simple mode: connect Bluetooth + play YT Music → confirm routing status changes to
  "Split active" and audio routes correctly.
- Custom mode: switch to Custom → choose per-app output for an app → confirm routing
  applies.
- Multi-focus: toggle "Videos don't pause the music" → confirm multi-audio-focus
  is enabled.
- Volume keys: in a speaker-routed app, confirm volume keys change phone speaker.
- Dark theme: toggle device dark mode → confirm the app respects it.
- Dynamic color: change wallpaper on Android 12+ → confirm the app's color scheme
  updates.

## Final verified application commit

`c74f831bc10bbdd88f5427461bc5bb550c38ccfe` on `feat/material3-redesign`.

## Known limitations

- **No emulator/device** → "installs and launches without crashing" claim is **not**
  directly verified. Documented honestly rather than fabricated.
- **No signing secrets** → the draft release carries an unsigned APK. The user must
  add the four `ANDROID_KEYSTORE_*` secrets (see `release-process.md`) and re-run the
  workflow on the tag to produce a signed APK.
