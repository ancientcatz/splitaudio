package app.splitaudio

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

/** Shizuku 小工具：以 shell 身分跑指令（路由規則要 shell 才有的系統權限）。newProcess 是隱藏方法，用反射叫。 */
object Shz {
    const val REQ = 1001
    fun alive(): Boolean = try { Shizuku.pingBinder() } catch (_: Throwable) { false }
    fun granted(): Boolean = try { alive() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED } catch (_: Throwable) { false }
    fun request() { try { if (alive() && !granted()) Shizuku.requestPermission(REQ) } catch (_: Throwable) {} }

    private fun newProcess(cmd: String): Process {
        val m = Shizuku::class.java.getDeclaredMethod("newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java)
        m.isAccessible = true
        return m.invoke(null, arrayOf("sh", "-c", cmd), null, null) as Process
    }

    /** 跑一條 shell，回 (rc, 輸出)；rc=-2 沒授權、-1 逾時 */
    fun run(cmd: String, timeoutSec: Int = 30, stdin: ByteArray? = null): Pair<Int, String> {
        if (!granted()) return Pair(-2, if (alive()) "not_granted" else "shizuku_dead")
        return try {
            val p = newProcess(cmd)
            if (stdin != null) { p.outputStream.use { it.write(stdin); it.flush() } } else p.outputStream.close()
            val sb = StringBuilder()
            val t = Thread { try { BufferedReader(InputStreamReader(p.inputStream)).use { r -> var l = r.readLine(); while (l != null && sb.length < 16384) { sb.append(l).append('\n'); l = r.readLine() } } } catch (_: Exception) {} }
            t.isDaemon = true; t.start()
            val deadline = System.currentTimeMillis() + timeoutSec * 1000L; var rc: Int? = null
            while (System.currentTimeMillis() < deadline) { try { rc = p.exitValue(); break } catch (_: Exception) { Thread.sleep(100) } }
            if (rc == null) try { p.destroy() } catch (_: Exception) {}
            t.join(1500)
            Pair(rc ?: -1, sb.toString())
        } catch (e: Throwable) { Pair(-3, e.javaClass.simpleName + ":" + (e.message ?: "").take(100)) }
    }
}
