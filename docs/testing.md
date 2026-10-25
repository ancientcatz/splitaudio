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

# 3. Release APK (R8 + resource shrinking)
gradle --no-daemon --console=plain assembleRelease

# 4. Lint
gradle --no-daemon --console=plain :app:lintRelease
```

## Baseline (commit `a0777eb`)

| Step                | Result      | Artifact size |
| ------------------- | ----------- | ------------- |
| `route/build_dex.sh`| SUCCESS     | 17,148 B dex  |
| `assembleDebug`      | SUCCESS     | 5,530,015 B   |
| `assembleRelease` (no minify) | SUCCESS | 4,685,254 B |
| `:app:lintRelease`   | SUCCESS     | —             |

## Unit tests

The project has no unit test sources (`app/src/test/`, `app/src/androidTest/` do not
exist). No unit tests are added by the redesign either — the app is a thin orchestration
layer over Shizuku + AppRoute, and meaningful unit tests would require mocking the
Shizuku binder and the AppRoute socket protocol, which is out of scope.

## Instrumentation tests

Not configured. The mission brief calls for instrumentation tests "when a suitable
emulator is available". The CI runner (ubuntu-latest) does not have a managed Android
emulator configured in the workflow, and the local sandbox has no emulator either.
Instrumentation is therefore explicitly out of scope; we document this in
`release-checklist.md` as a known limitation.

## CI runs

(CI run URLs and conclusions are appended after the workflow is pushed. See
`release-process.md` for the workflow definition.)

## Runtime verification

Runtime verification of the APK on an actual Android device/emulator is **not possible
in this sandbox** (no Android emulator or physical device available). Verification is
limited to:

1. `aapt dump badging` confirms applicationId, versionCode, min/target/compile SDK,
   permissions, launcher activity.
2. `aapt dump resources` confirms the Material 3 theme is packaged.
3. R8 mapping + usage + configuration reports confirm minification, obfuscation, and
   optimization ran.
4. CI green status on `feat/material3-redesign`.

Final verified application commit: (filled in after the final push.)

## Known limitations

- No emulator/device → "installs and launches without crashing" claim is **not**
  directly verified. We document this honestly rather than fabricate the result.
- The user (reviewer of the draft release) is expected to perform the on-device
  smoke test before publishing.
