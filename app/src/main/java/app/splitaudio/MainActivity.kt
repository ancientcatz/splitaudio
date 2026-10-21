package app.splitaudio

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** 狀態＋設定頁：Shizuku 授權、無障礙（音量鍵）、手動啟動／停止分流 */
class MainActivity : Activity() {
    private lateinit var st: TextView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 1)
        MonitorService.ensure(this)
        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, pad, pad, pad) }
        root.addView(TextView(this).apply { textSize = 18f; text = "SplitAudio 分流\n藍牙只放音樂 App（YT Music），其他 App 的聲音走手機喇叭。" })
        st = TextView(this).apply { textSize = 15f; setPadding(0, pad, 0, pad) }
        root.addView(st)
        fun btn(t: String, f: () -> Unit) = root.addView(Button(this).apply { text = t; setOnClickListener { f(); refreshSoon() } })
        btn("① Shizuku 授權") { Shz.request() }
        btn("② 開啟無障礙（音量鍵調手機喇叭）") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        btn("立即啟動分流") { Thread { Route.start(this) }.start() }
        btn("停止分流") { Thread { Route.stop() }.start() }
        btn("重新整理") { }
        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() { super.onResume(); refreshSoon() }

    private fun refreshSoon() {
        Thread {
            Thread.sleep(600)
            val acc = (Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: "").contains("$packageName/")
            val s = Route.ask("state")
            val txt = "Shizuku：" + (if (!Shz.alive()) "沒在跑" else if (Shz.granted()) "已授權 ✓" else "未授權") +
                "\n無障礙（音量鍵）：" + (if (acc) "已開 ✓" else "未開") +
                "\n分流：" + (when (s) { "on" -> "分流中 ✓"; "off" -> "路由在跑，等音樂播放"; else -> "沒在跑（藍牙＋音樂播放時自動啟動）" }) +
                "\n上次啟動：" + Route.lastStart
            runOnUiThread { st.text = txt }
        }.start()
    }
}
