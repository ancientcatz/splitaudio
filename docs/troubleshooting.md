# Troubleshooting

## T1 — `javac: not found` (baseline build)

**Symptom**: `route/build_dex.sh` fails with `route/build_dex.sh: 10: javac: not found`.

**Cause**: The sandbox's default JDK install is `openjdk-21-jre-headless` (JRE only,
no `javac`).

**Resolution**: Downloaded Temurin JDK 17 from Adoptium and installed to
`/home/z/jdk17`. Set `JAVA_HOME=/home/z/jdk17` and prepended `$JAVA_HOME/bin` to PATH.

## T2 — No Gradle wrapper in the repo

**Symptom**: No `gradlew`/`gradlew.bat`/`gradle/wrapper/` in the repository root.

**Cause**: The original maintainer chose to ship without a wrapper. CI masks this by
using `gradle/actions/setup-gradle@v3`.

**Resolution**: For local builds, install Gradle 8.9 from
`https://services.gradle.org/distributions/gradle-8.9-bin.zip`. No change to the
repository — see `decisions.md` D6.

## T3 — Malformed workflow YAML

**Symptom**: Existing `.github/workflows/android.yml` has `on.push.branches: ain]`
which is not valid YAML for a list.

**Cause**: Likely a previous edit stripped the opening `[`.

**Resolution**: The rewritten workflow uses `branches: [feat/material3-redesign, main]`
which is unambiguous YAML.

## T4 — Shizuku reflection and R8

**Symptom (hypothetical)**: After enabling R8, the app crashes when calling
`Shz.run(...)` with `NoSuchMethodException: newProcess`.

**Cause**: R8 strips the `newProcess` method on `rikka.shizuku.Shizuku` because it is
not statically referenced (it is invoked via `getDeclaredMethod`).

**Resolution**: Add an explicit `-keep` rule on `rikka.shizuku.Shizuku#newProcess` in
`app/proguard-rules.pro` (see `r8-optimization.md`). Verified post-build by inspecting
the R8 mapping file.

## T5 — No Android emulator available

**Symptom**: Cannot perform runtime smoke test of the APK.

**Cause**: The sandbox has no `emulator` binary, no AVD, and no KVM acceleration.

**Resolution**: Documented honestly. CI workflow does not configure a managed emulator
either (would need `reactivecircus/android-emulator-runner` and is brittle). User must
perform the device smoke test before publishing the release. See `testing.md`.
