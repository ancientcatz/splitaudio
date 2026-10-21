package app.splitaudio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** 開機／App 更新後自動起常駐監看 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) { MonitorService.ensure(c) }
}
