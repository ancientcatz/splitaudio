package app.splitaudio

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket

/** 路由本體＝AppRoute（app_process 小程式，shell 身分註冊系統路由規則：音樂 App 以外的 App 走手機喇叭）。
 *  本 App 只負責：把 assets 裡的 approute.dex／approute.sh 經 Shizuku 寫到 /data/local/tmp 並啟動，之後用本機指令口 127.0.0.1:47821 溝通。
 *  AppRoute 用 setsid 脫離，本 App 被系統殺掉路由照跑；藍牙斷 2 分／音樂 10 分沒播它自己結束。 */
object Route {
    const val PORT = 47821
    @Volatile var lastStart = "none"

    fun ask(cmd: String, timeoutMs: Int = 400): String? = try {
        Socket().use { s ->
            s.connect(InetSocketAddress("127.0.0.1", PORT), timeoutMs); s.soTimeout = timeoutMs * 3
            s.getOutputStream().write((cmd + "\n").toByteArray()); s.getOutputStream().flush()
            BufferedReader(InputStreamReader(s.getInputStream())).readLine()?.trim()
        }
    } catch (_: Exception) { null }

    fun running(): Boolean = ask("state") != null
    fun ruleOn(): Boolean = ask("state") == "on"

    /** 把 assets 寫到 /data/local/tmp（先寫 .new 再改名：正在跑的 AppRoute 還拿著舊檔，不能原地覆蓋）。成功回 null。 */
    private fun deploy(ctx: Context): String? {
        for (n in listOf("approute.dex", "approute.sh")) {
            val data = ctx.assets.open(n).use { it.readBytes() }
            val f = "/data/local/tmp/$n"
            val a = Shz.run("cat > $f.new && chmod 644 $f.new && mv -f $f.new $f", 15, data)
            if (a.first != 0) return "deploy_fail:${a.first} ${a.second.take(60)}"
        }
        return null
    }

    /** 部署＋啟動（要在背景執行緒叫）。已在跑就不動。 */
    @Synchronized fun start(ctx: Context): String {
        if (running()) return "already".also { lastStart = it }
        deploy(ctx)?.let { return it.also { lastStart = it } }
        val r = Shz.run("sh /data/local/tmp/approute.sh start", 30)
        return ("start rc=${r.first} " + r.second.trim().lines().lastOrNull().orEmpty().take(80)).also { lastStart = it }
    }

    fun stop(): String = Shz.run("sh /data/local/tmp/approute.sh stop", 15).second.trim()

    /** 「開影片不打斷音樂」（Android 內建 multi audio focus）：這個系統設定 App 讀不到，經 Shizuku 讀。
     *  回 "on"／"off"／"off_user"（用戶按過關＝分流不會自動打開）／null（讀不到） */
    fun multiFocusState(): String? {
        val r = Shz.run("settings get system multi_audio_focus_enabled; [ -e /data/local/tmp/approute.nomf ] && echo nomf", 10)
        if (r.first < 0 || r.second.isBlank()) return null
        return if (r.second.trim().startsWith("1")) "on" else if (r.second.contains("nomf")) "off_user" else "off"
    }

    /** 手動開／關（要在背景執行緒叫）。關＝之後分流也不會自動打開。 */
    @Synchronized fun setMultiFocus(ctx: Context, on: Boolean): String {
        deploy(ctx)?.let { return it }
        return Shz.run("sh /data/local/tmp/approute.sh multifocus " + (if (on) "on" else "off"), 30).second.trim().lines().lastOrNull().orEmpty()
    }
}
