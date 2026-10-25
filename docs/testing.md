# Testing

## Local build commands

```bash
export JAVA_HOME=/home/z/jdk17
export ANDROID_HOME=/home/z/android-sdk
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH

# 1. Build the routing helper DEX (route/AppRoute.java -> assets/approute.dex)
sh route/build_dex.sh

# 2. Debug APK
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

## Material 3 redesign (commit `b68a5d9`)

| Step                | Result      | Artifact size | Notes |
| ------------------- | ----------- | ------------- | ----- |
| `route/build_dex.sh`| SUCCESS     | 17,148 B dex  | unchanged from baseline |
| `assembleDebug`      | SUCCESS     | 11,869,741 B  | +6.3 MB vs baseline (Material Components dep + AppCompat) |
| `assembleRelease` (R8 full mode) | SUCCESS | 2,408,055 B | **-47%** vs baseline release (4.7 MB → 2.4 MB) |
| `:app:lintRelease`   | SUCCESS     | —             | no critical issues |
| R8 mapping generated | YES         | 10,157,449 B  | confirms obfuscation ran |
| R8 usage report      | YES         | 1,975,489 B   | confirms dead-code removal |
| R8 configuration     | YES         | 33,541 B      | contains `full-mode` flag |
| Resource shrinking report | YES    | 948,189 B     | confirms resource shrinking ran |

## CI workflow

**Workflow name**: `Android CI (Material 3)` (`.github/workflows/android.yml`).

**Triggered on**:
- Push to `feat/material3-redesign` or `main`
- Pull request
- Tag push `v*`
- `workflow_dispatch`

**Verified run**:
- Run ID: `40190111791`
- Branch: `feat/material3-redesign`
- Commit: `b68a5d97f8f1e7cf1a6889ef29a6a62d060c500c`
- Trigger: push
- Status: `completed`
- Conclusion: `success`
- Duration: 3m49s
- Build job: ✓ green
- Release job: skipped (triggered by push, not tag — expected)

**Artifacts uploaded by the build job**:
- `splitaudio-debug-apk` — `app-debug.apk`
- `splitaudio-release-apk` — `app-release-unsigned.apk` (since signing secrets aren't set)
- `splitaudio-r8-reports` — `mapping.txt`, `usage.txt`, `configuration.txt`, `seeds.txt`, `resources.txt`
- `splitaudio-lint-report` — `lint-results-release.html`, `lint-results-release.xml`

## R8 verification (post-build)

| Check | Result |
| ----- | ------ |
| R8 compiler version | 8.5.35 (matches AGP 8.5.2) |
| `android.enableR8.fullMode=true` | verified in `configuration.txt` |
| `isMinifyEnabled=true` | verified (mapping file produced) |
| `isShrinkResources=true` | verified (`resources.txt` produced) |
| Obfuscation | verified (mapping.txt contains `->` renames; `MainActivity$$ExternalSyntheticLambda0 -> j0.h:` etc.) |
| `rikka.shizuku.Shizuku` class kept | verified (proguard rule worked) — class string `Lrikka/shizuku/Shizuku;` present in release `classes.dex` |
| `rikka.shizuku.ShizukuProvider` kept | verified (referenced by manifest) |
| `Shz.newProcess` wrapper method kept | verified (per R8 mapping: `app.splitaudio.Shz.newProcess(java.lang.String):16:16 -> c`) |
| Manifest components preserved | verified (aapt dump xmltree shows all activities/services/receivers/providers) |
| `approute.dex` + `approute.sh` preserved as release APK assets | verified (md5 identical to source) |
| `Theme.SplitAudio` + M3 styles in APK | verified (aapt dump resources shows `Base.Theme.Material3.*`, `isMaterial3Theme` attr) |
| All 5 app components (`MainActivity`, `AppsActivity`, `MonitorService`, `VolKeyService`, `BootReceiver`) in release dex | verified (class strings present) |

## Release APK metadata (verified)

```
package: name='app.splitaudio' versionCode='7' versionName='0.4.0'
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

The project has no unit test sources (`app/src/test/`, `app/src/androidTest/` do not
exist). No unit tests are added by the redesign — the app is a thin orchestration layer
over Shizuku + a runtime DEX, and meaningful unit tests would require mocking the
Shizuku binder and the AppRoute socket protocol. Documented as out of scope in
`decisions.md` D7.

## Instrumentation tests

Not configured. The CI runner (ubuntu-latest) does not have a managed Android
emulator configured in the workflow, and the local sandbox has no emulator either.
Instrumentation is explicitly out of scope; documented in `release-checklist.md`.

## Runtime verification

Runtime verification of the APK on an actual Android device/emulator is **not possible
in this sandbox** (no Android emulator binary, no AVD, no KVM acceleration).

Verification was limited to:

1. **APK structure**: `aapt dump badging` + `aapt dump xmltree` confirm
   applicationId, versionCode, min/target/compile SDK, permissions, launcher
   activity, all manifest components.
2. **APK contents**: `unzip -l` + `aapt dump resources` confirm Material 3 theme is
   packaged, `approute.dex` + `approute.sh` are intact, `classes.dex` contains the
   Shizuku classes kept by proguard.
3. **R8 mapping/usage/configuration reports**: confirm obfuscation, shrinking, and
   optimization all ran with `full-mode` enabled.
4. **CI green status** on `feat/material3-redesign`: build job passes; release job
   skips (correctly) because signing secrets are absent.

**The user is expected to perform the on-device smoke test before publishing the
draft release.** Specifically:
- Install the signed APK (after configuring signing secrets and re-running the
  workflow on the `v0.4.0` tag).
- Cold launch the app.
- Grant Shizuku permission.
- Simple mode: connect Bluetooth + play YT Music → confirm routing status changes to
  "Split active" and audio routes correctly.
- Custom mode: switch to Custom → choose per-app output for an app → confirm routing
  applies.
- Multi-focus: toggle "Videos don't pause the music" → confirm multi-audio-focus
  is enabled.
- Volume keys: in a speaker-routed app, confirm volume keys change phone speaker
  (not Bluetooth).
- Dark theme: toggle device dark mode → confirm the app respects it.

## Final verified application commit

`b68a5d97f8f1e7cf1a6889ef29a6a62d060c500c` on `feat/material3-redesign`.

## Known limitations

- **No emulator/device** → "installs and launches without crashing" claim is **not**
  directly verified. Documented honestly rather than fabricated.
- **No signing secrets** → the draft release carries an unsigned APK. The user must
  add the four `ANDROID_KEYSTORE_*` secrets (see `release-process.md`) and re-run the
  workflow on the tag to produce a signed APK.
