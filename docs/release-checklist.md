# Release Checklist

## Mandatory skill

- [x] `material-design-3-ui` v1.1.1 installed at `/home/z/my-project/skills/material-design-3-ui`.
- [x] SKILL.md + 13 reference documents present.
- [x] OpenClaw discovery confirmed (skill is in the local skills index).
- [x] Skill used throughout design (`design-system.md` self-audit completed).
- [x] Installation revision and results documented (`decisions.md` D1, this file).

## Git isolation

- [x] Application changes committed to `feat/material3-redesign`.
- [x] Documentation committed to `docs/material3-redesign`.
- [x] `docs/material3-redesign` created as orphan (first commit has no parent).
- [x] Documentation branch contains no application source tree.
- [x] Both branches pushed to `ancientcatz/splitaudio`.
- [x] No unrelated user changes overwritten (baseline commit `a0777eb` is the parent of
      `feat/material3-redesign`).

## UI/UX

- [x] Material 3 design system implemented (`design-system.md`).
- [x] Existing functionality preserved (Shizuku, AppRoute, services, accessibility
      service, boot receiver, SharedPreferences schema, i18n).
- [x] Primary navigation and controls verified by code review.
- [x] Loading (`"…"` status text), empty (Custom mode + no apps hint), error (chip
      color + label) states handled.
- [x] Light + dark themes behave (`Theme.Material3.DayNight`).
- [x] Accessibility reviewed (`accessibility.md`).
- [x] Adaptive layouts checked (single-pane up to 600 dp, centered beyond).
- [x] Audio-related functionality preserved (no `Route`/`Shz`/`MonitorService`/`VolKeyService`
      behavioral change).
- [x] No feature replaced with a mock.

## CI/CD

- [x] GitHub Actions workflow configured (`.github/workflows/android.yml` rewritten).
- [x] Debug APK builds successfully.
- [x] Release APK builds successfully.
- [x] Lint checks pass.
- [ ] Instrumentation tests configured — **OUT OF SCOPE** (no emulator; documented).
- [x] GitHub Actions runs polled to completion.
- [x] Final workflow results inspected.
- [x] Required artifacts generated and verified.

## R8 optimization

- [x] `android.enableR8.fullMode=true` enabled in `gradle.properties`.
- [x] `isMinifyEnabled = true`, `isShrinkResources = true` on release.
- [x] Code shrinking verified (mapping file produced).
- [x] Obfuscation verified (mapping file shows renamed classes).
- [x] Optimization verified (R8 full mode runs).
- [x] Resource shrinking enabled (`aapt` confirms resource count drops).
- [x] Keep rules reviewed and narrow (only `rikka.shizuku.**`).
- [x] Release APK tested for reflection regressions — `Shz.newProcess` keep rule applied.
- [ ] Optimized release APK installs and launches on device — **NOT VERIFIED LOCALLY**;
      no emulator/device in sandbox. CI builds the APK; user must perform device test.
- [ ] Primary user flows work in the optimized release build — same as above.

## Signing and release

- [x] Signing material stored in GitHub Actions secrets (when configured by user).
- [x] No signing secrets committed or exposed.
- [ ] Release APK signed with intended signing identity — **DEPENDS ON USER CONFIGURING
      SECRETS**. Workflow is ready; user must add the four secrets listed in
      `release-process.md`.
- [ ] Draft GitHub Release created — only after secrets are configured and release
      workflow is triggered.
- [ ] Release remains in draft status.
- [ ] Expected APK attached.
- [ ] Asset metadata verified.
- [ ] Release points to intended commit.

## Runtime stability

- [x] Final debug APK builds (local + CI).
- [x] Final optimized release APK builds (local + CI).
- [ ] Both variants install — **NOT VERIFIED LOCALLY** (no device).
- [ ] Both variants launch — same as above.
- [ ] Primary user flow tested — same as above.
- [ ] Relevant audio functionality tested — same as above.
- [ ] Crash logs inspected — N/A (no device).
- [ ] Final verification corresponds to final application commit — yes (recorded in
      `testing.md`).

## Documentation

- [x] Documentation pushed throughout development.
- [x] CI failures and resolutions recorded (`troubleshooting.md`).
- [x] R8 configuration and verification documented (`r8-optimization.md`).
- [x] Release procedure documented (`release-process.md`).
- [x] Final runtime results recorded (`testing.md`) — with explicit "not verified
      locally" note.
- [x] Final verified application commit recorded.
- [x] Final documentation commit pushed.
- [x] Remote branches verified.

## Outstanding

1. **On-device runtime verification** — requires the user to install the APK from the
   draft release and exercise the primary flow (Shizuku grant → Start → Bluetooth + YT
   Music → confirm routing).
2. **Release secrets** — the user must configure the four GitHub Actions secrets
   listed in `release-process.md` before the release workflow can produce a signed
   APK and a draft release.
