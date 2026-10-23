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

/** 狀態＋設定頁（字串在 res/values*，英文預設、繁中 values-zh-rTW）：Shizuku 授權、無障礙（音量鍵）、手動啟動／停止分流、開影片不打斷音樂 */
class MainActivity : Activity() {
    private lateinit var st: TextView
    @Volatile private var mf: String? = null

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 1)
        MonitorService.ensure(this)
        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, pad, pad, pad) }
        root.addView(TextView(this).apply { textSize = 18f; text = getString(R.string.intro) })
        st = TextView(this).apply { textSize = 15f; setPadding(0, pad, 0, pad) }
        root.addView(st)
        fun btn(t: String, f: () -> Unit) = root.addView(Button(this).apply { text = t; setOnClickListener { f(); refreshSoon() } })
        btn(getString(R.string.btn_shizuku)) { Shz.request() }
        btn(getString(R.string.btn_acc)) { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        btn(getString(R.string.btn_start)) { Thread { Route.start(this) }.start() }
        btn(getString(R.string.btn_stop)) { Thread { Route.stop() }.start() }
        btn(getString(R.string.btn_mf)) { val on = mf != "on"; Thread { Route.lastStart = getString(R.string.mf_result, Route.setMultiFocus(this, on)) }.start() }
        btn(getString(R.string.btn_refresh)) { }
        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() { super.onResume(); refreshSoon() }

    private fun refreshSoon() {
        Thread {
            Thread.sleep(600)
            val acc = (Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: "").contains("$packageName/")
            val s = Route.ask("state")
            mf = if (Shz.granted()) Route.multiFocusState() else null
            fun g(id: Int) = getString(id)
            val txt = getString(R.string.st_shizuku, g(if (!Shz.alive()) R.string.v_not_running else if (Shz.granted()) R.string.v_granted else R.string.v_not_granted)) +
                "\n" + getString(R.string.st_acc, g(if (acc) R.string.v_on else R.string.v_off)) +
                "\n" + getString(R.string.st_split, g(when (s) { "on" -> R.string.split_on; "off" -> R.string.split_waiting; else -> R.string.split_none })) +
                "\n" + getString(R.string.st_mf, when (mf) { "on" -> g(R.string.v_on); "off" -> g(R.string.mf_off_auto); "off_user" -> g(R.string.mf_off_user); else -> "—" }) +
                "\n" + getString(R.string.st_last, Route.lastStart)
            runOnUiThread { st.text = txt }
        }.start()
    }
}
