# ---- Shizuku hidden-API reflection (Shz.kt) ------------------------------
# Shz.kt reflects on rikka.shizuku.Shizuku.getDeclaredMethod("newProcess", ...)
# to call the hidden static newProcess(String[], String[], String) method.
# The class and method must survive R8.
-keep class rikka.shizuku.Shizuku {
    public static android.os.IBinder getBinder();
    public static boolean pingBinder();
    public static boolean addBinderReceivedListenerSticky(java.lang.Runnable);
    public static boolean addBinderReceivedListener(java.lang.Runnable);
    public static boolean addRequestPermissionResultListener(java.util.function.Consumer);
    public static boolean removeRequestPermissionResultListener(java.util.function.Consumer);
    public static boolean shouldShowRequestPermissionRationale();
    public static void requestPermission(int);
    public static int checkSelfPermission();
    public static java.lang.Object newProcess(java.lang.String[], java.lang.String[], java.lang.String);
    public static void destroyProcess(java.lang.Object);
    public static boolean isPreV11();
}
# Shizuku's aidl stubs are referenced reflectively through IBinder.transact.
-keep class rikka.shizuku.aidl.** { *; }
-keep interface rikka.shizuku.aidl.** { *; }

# ---- AppRoute runtime DEX -------------------------------------------------
# approute.dex is shipped as an ASSET and loaded by `app_process` at runtime
# (NOT by the app's classloader). The classes inside it (AppRoute, etc.) are
# NOT on the app's compile/runtime classpath, so R8 cannot see them and does
# not need to keep them. No rule required.

# ---- Manifest components -------------------------------------------------
# AGP + R8 automatically keep Activities/Services/Receivers/Providers declared
# in AndroidManifest.xml. No explicit rule needed.

# ---- Kotlin metadata ----------------------------------------------------
# Companion-object constants (MainActivity.Companion.C_*) are compile-time
# inlined; no reflection on them. No rule needed.

# ---- Suppress noisy -dontwarn that would otherwise be required ----------
-dontwarn org.jetbrains.annotations.**
-dontwarn javax.annotation.**
-dontwarn android.support.**

# ---- Verification outputs -----------------------------------------------
# (Written to app/build/outputs/mapping/release/ by AGP automatically;
#  R8 full-mode printconfiguration is enabled via gradle.properties.)
