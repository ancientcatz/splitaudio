# SplitAudio — Material Design 3 Redesign

This branch (`docs/material3-redesign`) is a **documentation-only orphan branch** that
tracks the design, implementation, build, and release of the Material Design 3 redesign
of the SplitAudio Android app.

## Repository branches

| Branch                       | Purpose                                                                       | Commit |
| ---------------------------- | ----------------------------------------------------------------------------- | ------ |
| `main`                       | Original application source — do not modify directly.                         | `a0777eb` |
| `feat/material3-redesign`   | Application source for the Material 3 redesign.                              | `b68a5d9` |
| `docs/material3-redesign`   | This orphan branch — documentation only, no application source tree.         | (latest commit on this branch) |

The `docs/material3-redesign` branch was created with `git checkout --orphan` so that its
first commit has no parent. It contains no application source.

## Current status

| Item                         | State                                                                                                  |
| ---------------------------- | ------------------------------------------------------------------------------------------------------ |
| Mandatory skill              | `material-design-3-ui` v1.1.1 (2026-10-01) installed at `/home/z/my-project/skills/material-design-3-ui`. Verified. |
| Application branch           | `feat/material3-redesign` @ `b68a5d9` (pushed, CI green).                                              |
| Documentation branch         | `docs/material3-redesign` (orphan, no parent commit, pushed).                                            |
| Baseline build (commit `a0777eb`) | Debug APK 5.3 MB ✓; release APK 4.5 MB ✓; lint ✓.                                                  |
| Material 3 redesign build (`b68a5d9`) | Debug APK 11.9 MB ✓; **R8 release APK 2.4 MB (47% reduction)** ✓; lint ✓; R8 verified. |
| CI workflow                  | `Android CI (Material 3)` run ID `40190111791`: `completed` / `success` in 3m49s.                     |
| Draft release                | `v0.4.0` (DRAFT, UNSIGNED) — created; asset `splitaudio-v0.4.0-release-UNSIGNED.apk` (2,408,055 B).  |
| Latest verified application commit | `b68a5d97f8f1e7cf1a6889ef29a6a62d060c500c` on `feat/material3-redesign`.                          |

## Outstanding (user must complete before publishing)

1. **Configure signing secrets** — add `ANDROID_KEYSTORE_BASE64`,
   `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD` to the
   repository's GitHub Actions secrets. Reuse the maintainer's existing release key
   (per the original `SECURITY.md`) so v0.4.0 is update-compatible with 0.2.x / 0.3.x.
2. **Re-run the release workflow** on the `v0.4.0` tag (Actions → "Android CI
   (Material 3)" → Run workflow, or push another `v*` tag) to produce a signed APK
   and replace the unsigned asset on the draft release.
3. **On-device smoke test** — install the signed APK and exercise the primary flow
   (Shizuku grant → Simple mode BT + YT Music → Custom mode per-app → multi-focus →
   volume keys). No emulator was available in the build environment.

## Documentation index

| File                          | Contents                                                                                  |
| ----------------------------- | ----------------------------------------------------------------------------------------- |
| `README.md`                   | This file — overview, branches, status, index.                                           |
| `baseline.md`                 | Original build/test results, pre-existing issues, environment constraints.                |
| `architecture.md`             | App architecture, screen map, navigation, dependencies, audio components, lifecycle.     |
| `design-system.md`            | M3 principles, semantic colors, typography, shapes, spacing, elevation, components.      |
| `implementation-plan.md`      | Prioritized tasks, dependencies, risks, acceptance criteria.                              |
| `implementation-log.md`       | Significant changes, commits, test results, remaining issues.                            |
| `testing.md`                  | Local build commands, CI runs, unit tests, lint, runtime verification.                   |
| `accessibility.md`            | Accessibility requirements, checks, findings, fixes.                                      |
| `r8-optimization.md`           | R8 configuration, keep-rule rationale, optimization verification.                         |
| `release-process.md`          | Workflow, versioning, signing, draft release procedure, artifact verification.            |
| `security.md`                 | Token-handling, workflow permissions, signing security.                                    |
| `decisions.md`                 | Significant UI/architecture/testing/release decisions with rationale.                     |
| `troubleshooting.md`           | Important failures, diagnostic evidence, root causes, resolutions.                       |
| `release-checklist.md`          | Final acceptance criteria checklist and verification status.                                |

## Skill

The official `material-design-3-ui` skill (from `skydashnet/material-design-3-ui-skill`)
is installed in the OpenClaw environment at
`/home/z/my-project/skills/material-design-3-ui`. Its `SKILL.md` is readable, it ships
with 13 reference documents under `references/`, and it is discoverable through the local
skill index. The design decisions in `design-system.md` were made by applying the skill's
workflow and self-audit rubric. See `decisions.md` for the skill validation record.

## Quick links

- Draft release: https://github.com/ancientcatz/splitaudio/releases/tag/untagged-711a9cb98e9371574af2
- CI workflow run: https://github.com/ancientcatz/splitaudio/actions/runs/40190111791
- Application branch: https://github.com/ancientcatz/splitaudio/tree/feat/material3-redesign
- Documentation branch: https://github.com/ancientcatz/splitaudio/tree/docs/material3-redesign
