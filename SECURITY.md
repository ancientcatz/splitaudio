# Security notes / 安全說明

SplitAudio is **alpha**. Please read the limits below before relying on it.

## Privileges（權限）
- The routing helper (`route/AppRoute.java`) runs as the **shell user** (started through Shizuku). That is what lets it
  register a system audio policy (`MODIFY_AUDIO_ROUTING`).
- The app writes `approute.dex` / `approute.sh` into `/data/local/tmp` and runs them there. Anything else that already has
  shell access could replace those files — this is the same trust level as shell/Shizuku itself, not an extra hole.
- The accessibility service filters volume keys and reads only the **foreground package name**
  (`rootInActiveWindow.packageName`). It is declared with `canRetrieveWindowContent` because Android requires that for
  `rootInActiveWindow`; screen text is never read.

## Local control port（本機指令口）
- The helper listens on **127.0.0.1:47821** only (not reachable from the network), with **no authentication**.
- Commands: `state` → `on`/`off`; `vol up|down <package>` → changes the phone speaker volume by one step, only while the
  rule is on and only for apps that are being routed to the speaker.
- Any app on the same phone could therefore read the on/off state or nudge the speaker volume. It cannot change which apps
  are routed, stop the helper, or run commands. Input is parsed strictly; nothing is passed to a shell.

## Known limits（已知極限）
- Native AudioPolicy keeps at most 20 uids per mix and 50 mixes per policy → at most 1000 app uids are routed; any extra
  apps stay on Bluetooth (user-installed apps are listed first).
- The music app list is hard-coded (YouTube Music + Morphe build). Other music apps need a code change.
- Verified on one device (Xiaomi 15, HyperOS 3, Android 16). Other ROMs may ignore or reject the policy; if the helper
  dies or the policy is rejected, audio simply stays on Bluetooth.
- Apps installed **after** routing started are not included until the next start (music stop → start again).

## Reporting
Please open a GitHub issue. There is no network-facing component, so there is no private disclosure channel.
