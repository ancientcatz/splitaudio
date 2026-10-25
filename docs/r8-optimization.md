# R8 Optimization

## Configuration

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true           // code shrinking + obfuscation
        isShrinkResources = true          // resource shrinking
        // proguardFiles defined below
        // signing only when RELEASE_SIGNING_JSON env var is set (CI provides secrets)
    }
}
```

`gradle.properties` adds:

```
android.enableR8.fullMode=true
```

R8 full mode is supported since AGP 8.0 and is safe on AGP 8.5.2. It enables more
aggressive optimizations than compat mode (e.g., more aggressive dead-code removal,
stricter reflection handling).

## ProGuard rules (`app/proguard-rules.pro`)

```proguard
# ---- Shizuku hidden API reflection (Shz.kt) -------------------------------
# Shz.kt reflects on rikka.shizuku.Shizuku.getDeclaredMethod("newProcess", ...)
# to call the hidden static newProcess(String[], String[], String) method.
# We must keep the class and the method signature.
-keep class rikka.shizuku.Shizuku {
    public static android.os.IBinder getBinder();
    public static boolean pingBinder();
    public static boolean addBinderReceivedListenerSticky(java.lang.Runnable);
    public static boolean addRequestPermissionResultListener(java.util.function.Consumer);
    public static boolean removeRequestPermissionResultListener(java.util.function.Consumer);
    public static boolean shouldShowRequestPermissionRationale();
    public static void requestPermission(int);
    public static int checkSelfPermission();
    public static android.os.IRemoteObject *** newProcess(java.lang.String[], java.lang.String[], java.lang.String);
    public static void destroyProcess(android.os.IRemoteObject);
    public static boolean isPreV11();
}
# Shizuku's IRemoteObject / IRemoteCallback are referenced via reflection too.
-keep class rikka.shizuku.aidl.* { *; }

# ---- AppRoute runtime DEX ---------------------------------------------------
# approute.dex is loaded by app_process (NOT by the app's classloader). The
# classes inside it (AppRoute, etc.) are NOT on the app's compile/runtime
# classpath. No keep rule needed for them. The app only ships the dex as an
# asset. R8 cannot see them and does not need to keep them.

# ---- Manifest components ----------------------------------------------------
# R8 + AGP automatically keep activities/services/receivers/providers declared
# in the manifest. No explicit rule needed.

# ---- Material Components ---------------------------------------------------
# The library ships its own consumer rules (in the AAR). No duplicates needed.

# ---- Kotlin metadata -------------------------------------------------------
# Kotlin reflection on companion objects (MainActivity.Companion.C_* constants)
# is not used; constants are inlined by the compiler. No rule needed.

# ---- Disable noisy warnings that would otherwise force -dontwarn ----------
-dontwarn org.jetbrains.annotations.**
-dontwarn javax.annotation.**
-dontwarn android.support.**

# ---- Print details for verification ----------------------------------------
-printusageusage app/build/outputs/r8/usage.txt
-printconfiguration app/build/outputs/r8/config.txt
```

## Audit targets inspected

| Target                              | Present? | Action |
| ----------------------------------- | -------- | ------ |
| Reflection                          | Yes (`Shz.newProcess`) | Keep rule above. |
| Java/Kotlin serialization           | No       | None. |
| `Gson` / `Moshi`                    | No       | None. |
| JNI / native                        | No       | None. |
| Dynamically loaded classes          | `approute.dex` — but loaded by `app_process`, not the app | None. |
| Manifest components                 | Yes — `keep` automatic. | None. |
| Runtime annotations                 | No runtime-annotation-driven reflection in app code. | None. |
| DI framework                        | None. | None. |

## Verification

After `assembleRelease`, the following outputs are inspected:

1. `app/build/outputs/mapping/release/mapping.txt` — class names must be obfuscated
   (e.g., `app.splitaudio.MainActivity` → `app.splitaudio.a` or similar).
2. `app/build/outputs/mapping/release/configuration.txt` — confirms `isMinifyEnabled`,
   full mode, optimization.
3. `app/build/outputs/r8/usage.txt` — confirms dead-code removal ran.
4. `app/build/outputs/mapping/release/resources.txt` — confirms resource shrinking ran.
5. `aapt dump badging app-release.apk` — applicationId and components intact.

## Runtime verification

See `testing.md` — local sandbox has no emulator/device. CI workflow performs the
release build; runtime verification on a device is left to the user before publishing
the release.
