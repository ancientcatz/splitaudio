# Release Process

## Versioning

- `versionCode` increments by 1 for every release. Current: 6 (0.3.0).
- The redesign uses `versionCode=7`, `versionName="0.4.0"` (Material 3 redesign release).

## Tagging

Tags follow the existing convention `v<versionName>` (e.g., `v0.4.0`). Tags are created
**only** by the release workflow on the `feat/material3-redesign` branch when a tag is
pushed or a `workflow_dispatch` is triggered.

## Signing

### Required GitHub Actions secrets

| Secret name                  | Purpose |
| ---------------------------- | ------- |
| `ANDROID_KEYSTORE_BASE64`    | Base64-encoded release keystore (e.g. `base64 < release.keystore`). |
| `ANDROID_KEYSTORE_PASSWORD`  | Keystore password. |
| `ANDROID_KEY_ALIAS`          | Key alias (e.g., `splitaudio`). |
| `ANDROID_KEY_PASSWORD`        | Key password. |

> **Status**: these secrets are not yet configured on the repository. The workflow
> is written to require them. If they are absent, the workflow uploads an **unsigned**
> release APK as a CI artifact only and does **not** create a draft release. The user
> must add the secrets and re-run the release workflow to produce a signed APK and a
> draft release. This is documented honestly rather than fabricated.

### Security

- The keystore is decoded from `ANDROID_KEYSTORE_BASE64` into a **temp file outside the
  source tree** (`$RUNNER_TEMP/keystore.jks`).
- After signing, the temp file is removed by the runner (it lives in `$RUNNER_TEMP`
  which is wiped at the end of the job).
- No secret is printed in CI logs (uses `::add-mask::`).
- Workflow permissions are scoped: `contents: write` (for the release), nothing else.
- The release workflow **does not** run on pull requests.

## Draft release procedure

The release workflow:

1. Checks out `feat/material3-redesign`.
2. Sets up JDK 17, Gradle 8.9, Android SDK.
3. Builds `approute.dex`.
4. Runs `assembleDebug` and uploads `splitaudio-debug-apk` artifact.
5. Runs `assembleRelease` (R8 full mode + resource shrinking).
6. If signing secrets are present:
   - Decodes keystore into `$RUNNER_TEMP/keystore.jks`.
   - Builds signed `app-release.apk`.
   - Uploads `splitaudio-release-signed-apk` artifact.
   - Creates a **draft** GitHub release with the signed APK as an asset.
7. If signing secrets are absent: uploads unsigned `app-release-unsigned.apk` artifact,
   does **not** create a release.

## Release verification

After the workflow runs:

```bash
gh release view v0.4.0 --repo ancientcatz/splitaudio
gh release view v0.4.0 --repo ancientcatz/splitaudio --json isDraft,tagName,assets
```

Confirm: `isDraft=true`, `tagName=v0.4.0`, `assets[].name` includes
`splitaudio-release-signed-apk` (or unsigned if secrets absent), and `targetCommitish`
points at the verified commit on `feat/material3-redesign`.
