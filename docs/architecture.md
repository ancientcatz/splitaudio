# Architecture

## Application identity

- Application ID: `app.splitaudio`
- Package layout: `app.splitaudio` (single package, 8 Kotlin files + 1 Java helper)
- Version: `0.3.0` (versionCode 6)
- Permissions: `INTERNET`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`,
  `RECEIVE_BOOT_COMPLETED`, `POST_NOTIFICATIONS`.
- Manifest `<queries>`: only apps with a launcher `MAIN`/`LAUNCHER` intent — no
  `QUERY_ALL_PACKAGES`.

## Source map

| File              | Lines | Role |
| ----------------- | ----- | ---- |
| `MainActivity.kt` | 235   | Status + settings screen. Programmatic UI. Hardcoded colors (`C_*`). |
| `AppsActivity.kt` | 120   | Per-app output picker. `ListView` + `EditText` search + `AlertDialog`. |
| `MonitorService.kt`| 106  | Foreground service. AudioPlayback + AudioDevice callbacks + 60 s tick. |
| `Route.kt`        | 73    | Talks to AppRoute over `127.0.0.1:47821`. Deploys dex/sh/pkgmap. |
| `Outputs.kt`      | 33    | SharedPreferences-backed per-app output store. `KEYS = [default,speaker,bt,wired]`. |
| `VolKeyService.kt`| 59    | AccessibilityService. Intercepts `VOLUME_UP/DOWN`; routes to AppRoute. |
| `Shz.kt`          | 37    | Shizuku permission + reflection on hidden `newProcess`. |
| `BootReceiver.kt` | 10    | Auto-starts `MonitorService` on boot / package replace. |
| `route/AppRoute.java`| 600+ | Standalone helper compiled to `approute.dex`. Runs as shell user. |
| `route/build_dex.sh`| 22   | `javac` + `d8` → `approute.dex`. |
| `assets/approute.sh`| 50   | `start` / `startmap` / `stop` / `status` / `multifocus` driver. |

## Screen map

```
MainActivity (launcher)
├── Hero card         — app title + tagline
├── Status card       — live status text + start/stop buttons + last-start line
├── Required setup    — Shizuku row, Accessibility row (chip + action button)
└── Customize         — mode toggle (Simple/Custom) + "Choose output per app"
                       button + multi-focus switch + status text

AppsActivity (internal)
├── Title + hint
├── Search EditText
└── ListView of apps  — icon + label + output-chip; tap → AlertDialog single-choice
```

## Navigation

`MainActivity` is the only launcher entry point. `AppsActivity` is reached via
`startActivity(Intent(this, AppsActivity::class.java))` from MainActivity's
"Choose output per app" button (Custom mode only) and from `setMode(true)` when
switching to Custom mode with no apps set. Back is the system back button; there is no
explicit up affordance.

## Audio-related components

- `MonitorService` registers two callbacks:
  - `AudioManager.AudioPlaybackCallback` — playback config changes → 1.2 s delayed
    `check("play")`.
  - `AudioManager.AudioDeviceCallback` — device add/remove → 2.5 s delayed `check("bt")`.
  - 60 s tick that re-checks when Bluetooth is connected or Custom mode is on.
- `Route.start(ctx)` deploys `approute.dex` + `approute.sh` to `/data/local/tmp/`,
  writes `approute.pkgmap` (Custom mode only), then runs `approute.sh start|startmap`.
  The helper uses `setsid`/`nohup` to detach from the parent process.
- `Route.ask(cmd)` opens a TCP socket to `127.0.0.1:47821`, sends `cmd\n`, reads one
  line.
- `VolKeyService` polls `Route.ask("state")` every 2 s (when routing on) or 4 s (off).
  On `VOLUME_UP/DOWN`, it sends `vol <dir> <pkg>` to AppRoute; if AppRoute responds
  `ok <n>`, it shows a toast; otherwise it adjusts `STREAM_MUSIC` locally (so the key
  still works for apps the rule does not capture).
- `Outputs` is the persistent configuration store. `Outputs.mapText()` produces
  `approute.pkgmap` content (`speaker:pkg,pkg\nbt:pkg\n...`).

## Lifecycle-sensitive code

- `MonitorService.ensure(c)` is called from `MainActivity.onCreate`,
  `VolKeyService.onServiceConnected`, and `BootReceiver.onReceive`.
- `MainActivity.onResume()` triggers a `refreshSoon()` refresh on a background thread.
- `AppsActivity.onPause()` flushes any pending per-app changes via `Route.apply(c)`,
  which stops the current route and re-arms `MonitorService` to restart under the new
  config.

## Dependencies

| Coordinate                               | Version | Purpose                              |
| ---------------------------------------- | ------- | ------------------------------------ |
| `androidx.core:core-ktx`                 | 1.13.1  | Kotlin extensions on AndroidX core.  |
| `dev.rikka.shizuku:api`                  | 13.1.5  | Shizuku IPC + permission API.        |
| `dev.rikka.shizuku:provider`             | 13.1.5  | Shizuku content provider.             |

The redesign adds Material Components for Android (see `design-system.md`).

## Reflection / serialization surface (relevant for R8)

- `Shz.newProcess(cmd)` reflects on `rikka.shizuku.Shizuku#getDeclaredMethod("newProcess", ...)`
  — this is a hidden Shizuku API and **must** be kept by R8.
- `Route.deploy(ctx)` uses `ctx.assets.open(n).readBytes()` to read `approute.dex` and
  `approute.sh`. These are assets, not class loaders, so no keep rule is needed for them
  specifically; the AppRoute classes inside the dex are not on the app's classpath at all.
- No `Gson`, `Moshi`, `Serializable`, `Parcelable` usages in the app source.
- No JNI / native code in the app.

## Error handling

- `MonitorService` swallows `Throwable` around `Shizuku.addBinderReceivedListenerSticky`
  and around the notification `notify` call.
- `Shz.run` returns `(-2, ...)` for missing permission, `(-1, ...)` for timeout, and
  `(-3, ClassName:msg)` for exceptions. `Route.lastStart` records the last attempt's
  status text.
- `Route.ask` returns `null` on any socket error — callers treat `null` as "not running".
- `MonitorService.ensure` swallows exceptions (the app never crashes the launcher if
  the foreground service cannot start).
