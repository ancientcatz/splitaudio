package app.splitaudio

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch

/**
 * Status + settings screen — Material 3 redesign.
 *
 * Structure is identical to the original (hero → status → required setup → customize)
 * but every color resolves from the M3 theme via [M3], every interactive control is a
 * Material 3 component (MaterialButton, MaterialCardView, MaterialSwitch,
 * MaterialButtonToggleGroup), the theme is `Theme.SplitAudio` (parent
 * `Theme.Material3.DayNight`), and the layout respects edge-to-edge window insets.
 *
 * Business logic (Shizuku permission, Route.start/stop, Outputs persistence,
 * MonitorService.ensure, refreshSoon polling) is unchanged.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var liveCard: MaterialCardView
    private lateinit var tvLive: TextView
    private lateinit var tvLiveSub: TextView
    private lateinit var tvLast: TextView
    private lateinit var shzChip: TextView
    private lateinit var shzBtn: MaterialButton
    private lateinit var accChip: TextView
    private lateinit var accBtn: MaterialButton
    private lateinit var segSimple: MaterialButton
    private lateinit var segCustom: MaterialButton
    private lateinit var appsBtn: MaterialButton
    private lateinit var mfSw: MaterialSwitch
    private lateinit var tvMf: TextView
    private lateinit var tvModeDesc: TextView
    @Volatile private var mf: String? = null
    private var mfBusy = false

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        com.google.android.material.color.DynamicColors.applyToActivityIfAvailable(this)
        val surfaceDark = M3.isColorDark(M3.surface(this))
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !surfaceDark
            isAppearanceLightNavigationBars = !surfaceDark
        }

        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 1)
        MonitorService.ensure(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(8))
            clipToPadding = false
        }

        // Hero — tonal primary → tertiary gradient (brand-coded, not rainbow).
        val hero = card(22, 0)
        hero.background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(M3.primary(this), M3.tertiary(this))).apply { cornerRadius = dp(22).toFloat() }
        hero.addView(text("🎧 " + getString(R.string.app_name), 26f, M3.onPrimary(this), true))
        hero.addView(text(getString(R.string.hero_sub), 14f, M3.onPrimary(this)).apply { setPadding(0, dp(6), 0, 0) })
        root.addView(hero)

        // Status card — color follows state.
        liveCard = card(16, 2)
        liveCard.setCardBackgroundColor(M3.surfaceVariant(this))
        tvLive = text("…", 20f, M3.onSurface(this), true)
        tvLiveSub = text("", 13f, M3.onSurface(this)).apply { setPadding(0, dp(4), 0, dp(6)) }
        liveCard.addView(tvLive); liveCard.addView(tvLiveSub)
        val btnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        btnRow.addView(btn(getString(R.string.btn_start), BTN_FILLED, M3.primary(this), M3.onPrimary(this), 1f) { Thread { Route.start(this@MainActivity) }.start() })
        btnRow.addView(btn(getString(R.string.btn_stop), BTN_OUTLINED, M3.error(this), M3.onError(this), 1f) { Thread { Route.stop() }.start() })
        liveCard.addView(btnRow)
        tvLast = text("", 11f, M3.onSurfaceVariant(this)).apply { setPadding(dp(4), dp(2), 0, 0) }
        liveCard.addView(tvLast)
        root.addView(liveCard)

        // ① Required setup card.
        val req = card(16, 1)
        req.setCardBackgroundColor(M3.surfaceContainer(this))
        req.addView(section(getString(R.string.sec_required), M3.primary(this)))
        shzChip = chip()
        shzBtn = btn(getString(R.string.btn_grant), BTN_TEXT, M3.primary(this), M3.onPrimary(this), 0f) { Shz.request() }
        req.addView(row("🔑", getString(R.string.row_shizuku), getString(R.string.row_shizuku_d), shzChip, shzBtn))
        req.addView(divider())
        accChip = chip()
        accBtn = btn(getString(R.string.btn_open), BTN_TEXT, M3.primary(this), M3.onPrimary(this), 0f) { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        req.addView(row("🔊", getString(R.string.row_acc), getString(R.string.row_acc_d), accChip, accBtn))
        root.addView(req)

        // ② Customize card.
        val cus = card(16, 1)
        cus.setCardBackgroundColor(M3.surfaceContainer(this))
        cus.addView(section(getString(R.string.sec_custom), M3.tertiary(this)))
        cus.addView(text(getString(R.string.mode_title), 15f, M3.onSurface(this), true))
        val toggle = MaterialButtonToggleGroup(this).apply {
            setSingleSelection(true)
            setSelectionRequired(true)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        segSimple = btn(getString(R.string.mode_simple), BTN_OUTLINED, M3.primary(this), M3.onPrimary(this), 0f) { setMode(false) }
        segCustom = btn(getString(R.string.mode_custom), BTN_OUTLINED, M3.tertiary(this), M3.onTertiary(this), 0f) { setMode(true) }
        toggle.addView(segSimple, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        toggle.addView(segCustom, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        cus.addView(toggle)
        tvModeDesc = text("", 13f, M3.onSurfaceVariant(this)).apply { setPadding(dp(4), dp(8), dp(4), dp(4)) }
        cus.addView(tvModeDesc)
        appsBtn = btn("🎛️ " + getString(R.string.btn_apps), BTN_FILLED, M3.secondaryContainer(this), M3.onSecondaryContainer(this), 0f) { startActivity(Intent(this, AppsActivity::class.java)) }
        cus.addView(appsBtn)
        cus.addView(divider())
        mfSw = MaterialSwitch(this).apply {
            text = "🎬 " + getString(R.string.mf_title); textSize = 15f; setTextColor(M3.onSurface(this@MainActivity)); setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(6), 0, dp(2))
            setOnCheckedChangeListener { _, on ->
                if (mfBusy || on == (mf == "on")) return@setOnCheckedChangeListener
                Thread { Route.lastStart = getString(R.string.mf_result, Route.setMultiFocus(this@MainActivity, on)); refreshSoon() }.start()
            }
        }
        cus.addView(mfSw)
        tvMf = text("", 13f, M3.onSurfaceVariant(this)).apply { setPadding(0, 0, 0, dp(4)) }
        cus.addView(tvMf)
        root.addView(cus)

        // Edge-to-edge insets.
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            v.setPadding(dp(16), dp(8) + sys.top, dp(16), dp(8) + sys.bottom)
            insets
        }

        setContentView(ScrollView(this).apply {
            background = null
            addView(root)
        })
    }

    override fun onResume() { super.onResume(); refreshSoon() }

    private fun setMode(custom: Boolean) {
        if (custom == Outputs.customFlag(this)) return
        Outputs.setCustom(this, custom); val c = applicationContext
        Thread { Route.apply(c) }.start()
        if (custom && !Outputs.hasAny(this)) startActivity(Intent(this, AppsActivity::class.java))
        refreshSoon()
    }

    private fun refreshSoon() {
        Thread {
            Thread.sleep(600)
            val acc = (Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: "").contains("$packageName/")
            val s = Route.ask("state")
            val alive = Shz.alive(); val granted = Shz.granted()
            mf = if (granted) Route.multiFocusState() else null
            val custom = Outputs.customFlag(this); val nApps = Outputs.all(this).size
            runOnUiThread {
                val (live, onColor) = when (s) {
                    "on" -> Pair("🟢 " + getString(R.string.live_on), M3.onPrimary(this@MainActivity))
                    "off" -> Pair("🔵 " + getString(R.string.live_wait), M3.onSecondary(this@MainActivity))
                    else -> Pair("⚪ " + getString(R.string.live_none), M3.onSurfaceVariant(this@MainActivity))
                }
                tvLive.text = live
                tvLive.setTextColor(onColor)
                tvLiveSub.setTextColor(onColor)
                liveCard.setCardBackgroundColor(M3.statusColor(this@MainActivity, s))
                tvLiveSub.text = if (custom && nApps > 0) getString(R.string.st_mode_custom, nApps) else getString(R.string.st_mode_simple)
                tvLast.text = getString(R.string.st_last, Route.lastStart)

                setChip(shzChip, if (granted) 1 else 0, getString(if (!alive) R.string.v_not_running else if (granted) R.string.v_granted else R.string.v_not_granted))
                shzBtn.visibility = if (granted) View.GONE else View.VISIBLE
                setChip(accChip, if (acc) 1 else 2, getString(if (acc) R.string.v_on else R.string.chip_recommended))
                accBtn.visibility = if (acc) View.GONE else View.VISIBLE

                segPaint(segSimple, !custom, M3.primary(this@MainActivity), M3.onPrimary(this@MainActivity))
                segPaint(segCustom, custom, M3.tertiary(this@MainActivity), M3.onTertiary(this@MainActivity))
                tvModeDesc.text = getString(if (custom && nApps == 0) R.string.mode_custom_empty else if (custom) R.string.mode_custom_d else R.string.mode_simple_d)
                appsBtn.visibility = if (custom) View.VISIBLE else View.GONE

                mfBusy = true; mfSw.isChecked = mf == "on"; mfSw.isEnabled = granted; mfBusy = false
                tvMf.text = when (mf) { "on" -> getString(R.string.mf_desc); "off" -> getString(R.string.mf_off_auto); "off_user" -> getString(R.string.mf_off_user); else -> getString(R.string.mf_desc) }
            }
        }.start()
    }

    // ---- UI helpers ----
    private fun card(cornerRadius: Int, elevation: Int): MaterialCardView = MaterialCardView(this).apply {
        radius = dp(cornerRadius).toFloat()
        cardElevation = dp(elevation).toFloat()
        strokeWidth = 0
        setContentPadding(dp(18), dp(16), dp(18), dp(16))
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(14) }
    }

    private fun text(t: String, size: Float, color: Int, bold: Boolean = false): TextView = TextView(this).apply {
        text = t; textSize = size; setTextColor(color); if (bold) setTypeface(typeface, Typeface.BOLD)
    }

    private fun section(t: String, color: Int): TextView = text(t, 19f, color, true).apply { setPadding(0, 0, 0, dp(10)) }

    private fun divider() = View(this).apply {
        setBackgroundColor(M3.outlineVariant(this@MainActivity))
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1)).apply { setMargins(0, dp(10), 0, dp(10)) }
    }

    private fun row(icon: String, title: String, desc: String, chip: TextView, action: MaterialButton): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        addView(LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            addView(text(icon, 24f, M3.onSurface(this@MainActivity)), LinearLayout.LayoutParams(dp(40), LinearLayout.LayoutParams.WRAP_CONTENT))
            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                addView(text(title, 16f, M3.onSurface(this@MainActivity), true))
                addView(text(desc, 12f, M3.onSurfaceVariant(this@MainActivity)))
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(chip)
        })
        addView(action)
    }

    private fun chip(): TextView = text("", 12f, Color.WHITE, true).apply {
        setPadding(dp(12), dp(6), dp(12), dp(6))
        background = GradientDrawable().apply { cornerRadius = dp(8).toFloat(); setColor(M3.surfaceVariant(this@MainActivity)) }
    }

    /** level: 1=ok(primary), 0=missing(error), 2=recommended(secondary) */
    private fun setChip(c: TextView, level: Int, t: String) {
        c.text = t
        val bg = M3.chipColor(this, level)
        (c.background as GradientDrawable).setColor(bg)
        c.setTextColor(when (level) { 0 -> M3.onError(this); 1 -> M3.onPrimary(this); else -> M3.onSecondary(this) })
    }

    private fun segPaint(b: MaterialButton, on: Boolean, color: Int, onColor: Int) {
        if (on) {
            b.setBackgroundColor(color)
            b.setTextColor(onColor)
        } else {
            b.setBackgroundColor(M3.surface(this))
            b.setTextColor(color)
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    companion object {
        const val BTN_FILLED = 0
        const val BTN_OUTLINED = 1
        const val BTN_TEXT = 2
    }

    /**
     * MaterialButton factory.
     * @param style one of [BTN_FILLED], [BTN_OUTLINED], [BTN_TEXT]
     * @param accentColor the brand color (background for FILLED, stroke+text for OUTLINED, text for TEXT)
     * @param onAccentColor text/icon color when on accentColor (used for FILLED)
     */
    private fun btn(label: String, style: Int, accentColor: Int, onAccentColor: Int, weight: Float, onClick: () -> Unit): MaterialButton {
        val mb = when (style) {
            BTN_FILLED -> MaterialButton(this, null, com.google.android.material.R.attr.materialButtonStyle)
            BTN_OUTLINED -> MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle)
            else -> MaterialButton(this, null, android.R.attr.borderlessButtonStyle)
        }
        mb.text = label
        mb.isAllCaps = false
        mb.textSize = 15f
        mb.setTypeface(mb.typeface, Typeface.BOLD)
        mb.cornerRadius = dp(20)
        when (style) {
            BTN_FILLED -> {
                mb.setBackgroundColor(accentColor)
                mb.setTextColor(onAccentColor)
            }
            BTN_OUTLINED -> {
                mb.strokeColor = android.content.res.ColorStateList.valueOf(accentColor)
                mb.strokeWidth = dp(2)
                mb.setTextColor(accentColor)
                mb.setBackgroundColor(M3.surface(this))
            }
            else -> {  // TEXT
                mb.setTextColor(accentColor)
                mb.setBackgroundColor(M3.surface(this))
            }
        }
        mb.setPadding(dp(14), dp(10), dp(14), dp(10))
        mb.layoutParams = LinearLayout.LayoutParams(
            if (weight > 0f) 0 else LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            weight
        ).apply { setMargins(dp(4), dp(6), dp(4), dp(6)) }
        mb.minimumHeight = dp(48)
        mb.setOnClickListener { onClick(); refreshSoon() }
        return mb
    }
}
