package app.splitaudio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.AudioPlaybackConfiguration
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import rikka.shizuku.Shizuku

/** 常駐（前景通知）：聽「播放變化」「輸出裝置變化」→ 藍牙連著＋有音樂在播＋路由沒在跑 → 經 Shizuku 啟動 AppRoute。
 *  沒有定時輪詢；只有藍牙連著時每 60 秒補查一次（防回呼漏掉）。啟動失敗 30 秒內不重試。 */
class MonitorService : Service() {
    private val h = Handler(Looper.getMainLooper())
    private var lastTry = 0L
    @Volatile private var state = "待命"
    private lateinit var am: AudioManager

    override fun onBind(i: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        startForeground(1, notif(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        am.registerAudioPlaybackCallback(object : AudioManager.AudioPlaybackCallback() {
            override fun onPlaybackConfigChanged(c: MutableList<AudioPlaybackConfiguration>?) { h.postDelayed({ check("play") }, 1200) }
        }, h)
        am.registerAudioDeviceCallback(object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(a: Array<out AudioDeviceInfo>?) { h.postDelayed({ check("bt") }, 2500) }
            override fun onAudioDevicesRemoved(r: Array<out AudioDeviceInfo>?) { h.postDelayed({ check("bt") }, 2500) }
        }, h)
        try { Shizuku.addBinderReceivedListenerSticky { h.post { check("shizuku") } } } catch (_: Throwable) {}
        tick()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int { h.post { check("start") }; return START_STICKY }

    private fun bt(): Boolean = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS).any {
        it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || it.type == AudioDeviceInfo.TYPE_BLE_HEADSET || it.type == AudioDeviceInfo.TYPE_BLE_SPEAKER }

    private fun tick() { h.postDelayed({ if (bt()) check("tick"); tick() }, 60_000) }

    private fun check(why: String) {
        val b = bt(); val music = am.isMusicActive
        if (!(b && music)) { Thread { setState(if (Route.ruleOn()) "分流中" else if (b) "藍牙已連（沒在播音樂）" else "待命") }.start(); return }
        Thread {
            if (Route.ruleOn()) { setState("分流中"); return@Thread }
            if (Route.running()) { setState("待命（路由在跑，等音樂 App 播放）"); return@Thread }
            val now = System.currentTimeMillis()
            if (now - lastTry < 30_000) return@Thread
            lastTry = now
            if (!Shz.granted()) { setState(if (Shz.alive()) "要授權 Shizuku（打開 App 按授權）" else "Shizuku 沒在跑"); return@Thread }
            val r = Route.start(this)
            Thread.sleep(2500)
            setState(if (Route.ruleOn()) "分流中" else "啟動結果：$r")
        }.start()
    }

    private fun setState(s: String) {
        if (s == state) return
        state = s
        try { (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(1, notif()) } catch (_: Exception) {}
    }

    private fun notif(): Notification {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel("route", "分流狀態", NotificationManager.IMPORTANCE_MIN))
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        return Notification.Builder(this, "route").setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setContentTitle("SplitAudio：$state").setContentIntent(pi).setOngoing(true).build()
    }

    companion object {
        fun ensure(c: Context) { try { c.startForegroundService(Intent(c, MonitorService::class.java)) } catch (_: Exception) {} }
    }
}
