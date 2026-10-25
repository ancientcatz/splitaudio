# Security

## GitHub personal access token

- The PAT was supplied ROT13-encoded in the mission brief.
- It is decoded in memory by `/home/z/my-project/scripts/run_gh.sh` using
  `tr 'A-Za-z' 'N-ZA-Mn-za-m'`; the decoded value is exported as `GH_TOKEN` and the
  encoded file is `shred`-removed immediately after decoding.
- The PAT is **never** printed to stdout/stderr.
- The PAT is **never** committed to either branch (the on-disk encoded file lives at
  `/home/z/my-project/.pat_rot13` which is outside both `splitaudio/` and
  `splitaudio-docs/`, and is listed in `/home/z/my-project/.gitignore`).
- The PAT is **never** placed in `git remote` URLs (we use the `GH_TOKEN` env var with
  `gh`, which uses HTTPS with bearer auth).
- The PAT is **never** embedded in the APK, in CI workflow files, or in any
  documentation.

## Workflow permissions

- `permissions: contents: write` — needed only by the release workflow for creating a
  draft release. The build/test workflow uses `permissions: contents: read`.
- The release workflow does not run on `pull_request` (prevents a malicious PR from
  accessing release secrets).
- Secrets are not exposed to pull-request runs (GitHub's default behavior for
  `pull_request` triggers from forks).

## Release signing

- The keystore is never committed to the repository.
- The keystore is decoded from a GitHub Actions secret into `$RUNNER_TEMP`, which is
  wiped at the end of the job.
- No signing password is logged.

## Sensitive data in the APK

- The APK contains no signing credentials, no tokens, no internal endpoint URLs.
- The `INTERNET` permission is used only for `127.0.0.1` IPC with the AppRoute helper
  (per `SECURITY.md` in the original repo).
- The accessibility service reads only the foreground package name; it does not read
  screen content.

## Reporting

Security issues are reported via GitHub issues on the original
`ancientcatz/splitaudio` repository.
