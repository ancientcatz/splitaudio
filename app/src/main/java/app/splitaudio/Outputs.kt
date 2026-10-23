package app.splitaudio

import android.content.Context

/** 自訂模式設定：模式開關＋每個 App 的輸出（default 跟系統／speaker 手機喇叭／bt 藍牙／wired 有線或 USB 耳機），存 SharedPreferences。
 *  路由程式讀不到 App 的設定 → 啟動前經 Shizuku 寫成 /data/local/tmp/approute.pkgmap（approute.sh startmap 再把套件名換成 uid）。 */
object Outputs {
    val KEYS = arrayOf("default", "speaker", "bt", "wired")
    private val PKG = Regex("^[A-Za-z0-9_.]+$")   // 套件名才寫進給 shell 讀的檔
    private fun sp(c: Context) = c.getSharedPreferences("outputs", Context.MODE_PRIVATE)

    /** 開關本身（畫面上的「自訂」色塊） */
    fun customFlag(c: Context) = sp(c).getBoolean("custom", false)
    /** 實際生效：開了自訂但一個 App 都沒選＝照簡單模式跑（不然什麼都不分流，像壞掉） */
    fun custom(c: Context) = customFlag(c) && hasAny(c)
    fun setCustom(c: Context, on: Boolean) = sp(c).edit().putBoolean("custom", on).apply()

    fun get(c: Context, pkg: String) = sp(c).getString("o:$pkg", "default") ?: "default"
    fun set(c: Context, pkg: String, v: String) =
        sp(c).edit().apply { if (v == "default") remove("o:$pkg") else putString("o:$pkg", v) }.apply()

    /** 有指定輸出的 App：套件名 → speaker／bt／wired */
    fun all(c: Context): Map<String, String> = sp(c).all.filterKeys { it.startsWith("o:") }
        .map { it.key.substring(2) to it.value.toString() }.filter { PKG.matches(it.first) && it.second in KEYS && it.second != "default" }.toMap()
    fun hasAny(c: Context) = all(c).isNotEmpty()

    /** approute.pkgmap 內容：一個輸出一行「speaker:pkg,pkg」 */
    fun mapText(c: Context) = all(c).entries.groupBy({ it.value }, { it.key })
        .map { "${it.key}:${it.value.joinToString(",")}" }.joinToString("\n", postfix = "\n")

    fun label(c: Context, key: String) = c.getString(when (key) {
        "speaker" -> R.string.out_speaker; "bt" -> R.string.out_bt; "wired" -> R.string.out_wired; else -> R.string.out_default })
}
