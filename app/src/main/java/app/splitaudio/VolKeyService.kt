package app.splitaudio

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

/** 音量鍵：分流規則掛著＋前景是走喇叭的 App → 調「手機喇叭」（問 AppRoute 本機指令口）；
 *  音樂 App／桌面／系統介面照常調藍牙。規則沒掛＝完全不攔。 */
class VolKeyService : AccessibilityService() {
    private val skip = setOf("app.morphe.android.apps.youtube.music", "com.google.android.apps.youtube.music",
        "com.miui.home", "com.android.systemui", "com.android.launcher3")
    @Volatile private var routeOn = false
    @Volatile private var alive = true
    private val consumed = HashSet<Int>()
    private val main = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        MonitorService.ensure(this)
        Thread {
            while (alive) {
                routeOn = Route.ask("state") == "on"
                try { Thread.sleep(if (routeOn) 2000 else 4000) } catch (_: InterruptedException) { return@Thread }
            }
        }.apply { isDaemon = true }.start()
    }
    override fun onDestroy() { alive = false; super.onDestroy() }
    override fun onAccessibilityEvent(e: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onKeyEvent(e: KeyEvent?): Boolean {
        if (e == null) return false
        val k = e.keyCode
        if (k != KeyEvent.KEYCODE_VOLUME_UP && k != KeyEvent.KEYCODE_VOLUME_DOWN) return false
        if (e.action == KeyEvent.ACTION_UP) return consumed.remove(k)
        if (e.action != KeyEvent.ACTION_DOWN || !routeOn) return false
        val fg = try { rootInActiveWindow?.packageName?.toString() } catch (_: Exception) { null } ?: return false
        if (fg in skip || fg == packageName) return false
        consumed.add(k)
        val dir = if (k == KeyEvent.KEYCODE_VOLUME_UP) "up" else "down"
        Thread {
            val r = Route.ask("vol $dir $fg")
            main.post {
                if (r != null && r.startsWith("ok ")) Toast.makeText(this, getString(R.string.toast_vol, r.substring(3)), Toast.LENGTH_SHORT).show()
                else try {   // 不是走喇叭的 App／規則剛好拿掉 → 這次按鍵已被攔，自己照原本調藍牙
                    (getSystemService(Context.AUDIO_SERVICE) as AudioManager).adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        if (dir == "up") AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                } catch (_: Exception) {}
            }
        }.start()
        return true
    }
}
