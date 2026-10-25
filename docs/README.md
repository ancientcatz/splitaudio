# SplitAudio — Material Design 3 Redesign

This branch (`docs/material3-redesign`) is a **documentation-only orphan branch** that
tracks the design, implementation, build, and release of the Material Design 3 redesign
of the SplitAudio Android app.

## Repository branches

| Branch                       | Purpose                                                                       | Commit |
| ---------------------------- | ----------------------------------------------------------------------------- | ------ |
| `main`                       | Original application source — do not modify directly.                         | `a0777eb` |
| `feat/material3-redesign`   | Application source for the Material 3 redesign (v0.4.1 Compose rewrite).      | `c74f831` |
| `docs/material3-redesign`   | This orphan branch — documentation only, no application source tree.         | (latest) |

The `docs/material3-redesign` branch was created with `git checkout --orphan` so that its
first commit has no parent. It contains no application source.

## Current status (v0.4.1)

| Item                         | State                                                                                                  |
| ---------------------------- | ------------------------------------------------------------------------------------------------------ |
| Mandatory skill              | `material-design-3-ui` v1.1.1 installed, used throughout design.                                       |
| Implementation               | **Complete Compose Material 3 rewrite** — `Scaffold` + `LargeTopAppBar` + `LazyColumn` + M3 components. |
| Application branch           | `feat/material3-redesign` @ `c74f831` (pushed, CI green).                                              |
| Documentation branch         | `docs/material3-redesign` (orphan, no parent commit, pushed).                                            |
| Baseline build (commit `a0777eb`) | Debug APK 5.3 MB ✓; release APK 4.5 MB ✓; lint ✓.                                                  |
| v0.4.0 (superseded)          | View-based reskin — built and passed CI, but user review found broken layout. Superseded by v0.4.1. |
| **v0.4.1 (current)**         | Compose Material 3 rewrite — debug 62.5 MB; **R8 release 3.9 MB**; lint ✓; R8 verified.              |
| CI workflow                  | `Android CI (Material 3)` run ID `40194565007`: `completed` / `success` in 4m58s.                     |
| Draft release                | `v0.4.1` (DRAFT, UNSIGNED) — created; asset `splitaudio-v0.4.1-release-UNSIGNED.apk` (3,946,816 B). |
| Latest verified application commit | `c74f831bc10bbdd88f5427461bc5bb550c38ccfe` on `feat/material3-redesign`.                          |

## Outstanding (user must complete before publishing)

1. **Configure signing secrets** — add `ANDROID_KEYSTORE_BASE64`,
   `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD` to the
   repository's GitHub Actions secrets.
2. **Re-run the release workflow** on the `v0.4.1` tag to produce a signed APK and
   replace the unsigned asset on the draft release.
3. **On-device smoke test** — install the signed APK and exercise the primary flow.

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
is installed at `/home/z/my-project/skills/material-design-3-ui`. Its `SKILL.md` is
readable, it ships with 13 reference documents, and it is discoverable through the local
skill index. The design decisions in `design-system.md` were made by applying the skill's
workflow and self-audit rubric — the v0.4.1 Compose rewrite scores 16/16 on the self-audit.

## Quick links

- Draft release: https://github.com/ancientcatz/splitaudio/releases/tag/untagged-c32d926ca92247b3c61a
- CI workflow run (v0.4.1): https://github.com/ancientcatz/splitaudio/actions/runs/40194565007
- Application branch: https://github.com/ancientcatz/splitaudio/tree/feat/material3-redesign
- Documentation branch: https://github.com/ancientcatz/splitaudio/tree/docs/material3-redesign
