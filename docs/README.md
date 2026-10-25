# SplitAudio — Material Design 3 Redesign

This branch (`docs/material3-redesign`) is a **documentation-only orphan branch** that
tracks the design, implementation, build, and release of the Material Design 3 redesign
of the SplitAudio Android app.

## Repository branches

| Branch                       | Purpose                                                                       |
| ---------------------------- | ----------------------------------------------------------------------------- |
| `main`                       | Original application source — do not modify directly.                         |
| `feat/material3-redesign`   | Application source for the Material 3 redesign.                              |
| `docs/material3-redesign`   | This orphan branch — documentation only, no application source tree.         |

The `docs/material3-redesign` branch was created with `git checkout --orphan` so that its
first commit has no parent. It contains no application source.

## Current status

| Item                         | State                                                                                                  |
| ---------------------------- | ------------------------------------------------------------------------------------------------------ |
| Mandatory skill              | `material-design-3-ui` v1.1.1 (2026-10-01) installed locally at `/home/z/my-project/skills/material-design-3-ui`. Verified. |
| Application branch           | `feat/material3-redesign` created from `main` (commit `a0777eb`).                                       |
| Documentation branch         | `docs/material3-redesign` (orphan, no parent commit).                                                  |
| Baseline build               | Debug APK builds (5.3 MB); release APK builds (4.5 MB, unsigned). Lint passes.                          |
| Latest verified application commit | See `testing.md`.                                                                               |
| CI status                   | See `testing.md`.                                                                                       |

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
skill index. See `decisions.md` for the skill validation record.
