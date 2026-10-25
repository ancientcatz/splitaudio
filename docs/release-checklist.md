# Release Checklist

## Mandatory skill

- [x] `material-design-3-ui` v1.1.1 installed at `/home/z/my-project/skills/material-design-3-ui`.
- [x] SKILL.md + 13 reference documents present.
- [x] OpenClaw discovery confirmed (skill in local skills index).
- [x] Skill used throughout design (`design-system.md` self-audit completed).
- [x] Installation revision and results documented.

## Git isolation

- [x] Application changes committed to `feat/material3-redesign` (commit `b68a5d9`).
- [x] Documentation committed to `docs/material3-redesign` (orphan, initial commit `dad0172`).
- [x] `docs/material3-redesign` created as orphan (first commit has no parent).
- [x] Documentation branch contains no application source tree.
- [x] Both branches pushed to `ancientcatz/splitaudio` and remotely verified.
- [x] No unrelated user changes overwritten (baseline commit `a0777eb` is the parent of `feat/material3-redesign`).

## UI/UX

- [x] Material 3 design system implemented (`design-system.md`).
- [x] Existing functionality preserved (Shizuku, AppRoute, services, accessibility
      service, boot receiver, SharedPreferences schema, i18n).
- [x] Primary navigation and controls verified by code review.
- [x] Loading (`"…"` status text), empty (Custom mode + no apps hint), error
      (chip color + label) states handled.
- [x] Light + dark themes behave (`Theme.Material3.DayNight`).
- [x] Accessibility reviewed (`accessibility.md`).
- [x] Adaptive layouts checked (single-pane up to 600 dp, centered beyond).
- [x] Audio-related functionality preserved (no `Route`/`Shz`/`MonitorService`/`VolKeyService` behavioral change).
- [x] No feature replaced with a mock.

## CI/CD

- [x] GitHub Actions workflow rewritten (`.github/workflows/android.yml`).
- [x] Debug APK builds successfully (CI + local).
- [x] Release APK builds successfully (CI + local).
- [x] Lint checks pass.
- [ ] Instrumentation tests configured — **OUT OF SCOPE** (no emulator; documented).
- [x] GitHub Actions run polled to completion (run ID `40190111791`).
- [x] Final workflow results inspected (`completed` / `success`, 3m49s).
- [x] Required artifacts generated and verified (4 artifacts uploaded).

## R8 optimization

- [x] `android.enableR8.fullMode=true` enabled in `gradle.properties`.
- [x] `isMinifyEnabled = true`, `isShrinkResources = true` on release.
- [x] Code shrinking verified (`mapping.txt` 10 MB produced).
- [x] Obfuscation verified (mapping file shows `->` renames).
- [x] Optimization verified (R8 full mode + `proguard-android-optimize.txt`).
- [x] Resource shrinking enabled (`resources.txt` 948 KB produced).
- [x] Keep rules reviewed and narrow (only `rikka.shizuku.**` + aidl stubs).
- [x] Release APK tested for reflection regressions — `Shz.newProcess` keep rule applied; `rikka.shizuku.Shizuku` class string present in release `classes.dex`.
- [ ] Optimized release APK installs and launches on device — **NOT VERIFIED LOCALLY**; no emulator/device in sandbox.
- [ ] Primary user flows work in the optimized release build — same as above.

## Signing and release

- [x] Signing material stored in GitHub Actions secrets (when configured by user).
- [x] No signing secrets committed or exposed.
- [ ] Release APK signed with intended signing identity — **OUTSTANDING**: user must
      add the four `ANDROID_KEYSTORE_*` secrets (see `release-process.md`). Workflow
      is ready; secrets are not.
- [x] Draft GitHub Release created (commit `b68a5d9`, tag `v0.4.0`).
- [x] Release remains in draft status (`isDraft=true`).
- [x] Expected APK attached (`splitaudio-v0.4.0-release-UNSIGNED.apk`, 2,408,055 bytes).
- [x] Asset metadata verified (MD5 matches local build).
- [x] Release points to intended commit (`targetCommitish=b68a5d9`).

## Runtime stability

- [x] Final debug APK builds (local + CI).
- [x] Final optimized release APK builds (local + CI).
- [ ] Both variants install — **NOT VERIFIED LOCALLY** (no device/emulator).
- [ ] Both variants launch — same as above.
- [ ] Primary user flow tested — same as above.
- [ ] Relevant audio functionality tested — same as above.
- [ ] Crash logs inspected — N/A (no device).
- [x] Final verification corresponds to final application commit (`b68a5d9`).

## Documentation

- [x] Documentation pushed throughout development.
- [x] CI failures and resolutions recorded (`troubleshooting.md`).
- [x] R8 configuration and verification documented (`r8-optimization.md`).
- [x] Release procedure documented (`release-process.md`).
- [x] Final runtime results recorded (`testing.md`) — with explicit "not verified locally" note.
- [x] Final verified application commit recorded (`b68a5d9`).
- [x] Final documentation commit pushed.
- [x] Remote branches verified.

## Outstanding

1. **Signing secrets** — the user must configure four GitHub Actions secrets
   (`ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`,
   `ANDROID_KEY_PASSWORD`) and re-run the `Android CI (Material 3)` workflow on the
   `v0.4.0` tag to produce a signed APK and replace the unsigned asset on the draft
   release. The maintainer's existing release key (referenced in the original repo's
   `SECURITY.md`) should be reused so the v0.4.0 release is update-compatible with
   previously distributed 0.2.x / 0.3.x APKs.
2. **On-device runtime verification** — requires the user to install the signed APK
   from the draft release and exercise the primary flow (Shizuku grant → Start →
   Bluetooth + YT Music → confirm routing → Custom mode → per-app output → multi-focus
   toggle → volume keys).
