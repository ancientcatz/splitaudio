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

    /** 部署＋啟動（要在背景執行緒叫）。已在跑就不動。 */
    @Synchronized fun start(ctx: Context): String {
        if (running()) return "already".also { lastStart = it }
        val dex = ctx.assets.open("approute.dex").use { it.readBytes() }
        val sh = ctx.assets.open("approute.sh").use { it.readBytes() }
        val a = Shz.run("cat > /data/local/tmp/approute.dex && chmod 644 /data/local/tmp/approute.dex", 15, dex)
        if (a.first != 0) return "deploy_fail:${a.first} ${a.second.take(60)}".also { lastStart = it }
        val b = Shz.run("cat > /data/local/tmp/approute.sh", 15, sh)
        if (b.first != 0) return "deploy_fail:${b.first} ${b.second.take(60)}".also { lastStart = it }
        val r = Shz.run("sh /data/local/tmp/approute.sh start", 30)
        return ("start rc=${r.first} " + r.second.trim().lines().lastOrNull().orEmpty().take(80)).also { lastStart = it }
    }

    fun stop(): String = Shz.run("sh /data/local/tmp/approute.sh stop", 15).second.trim()
}
