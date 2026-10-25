package app.splitaudio

import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Per-app output picker.
 *
 * Material 3 redesign — same flow as the original (search → tap → single-choice dialog →
 * flush on pause) but:
 *  - background resolves from `?attr/colorSurface` (light/dark),
 *  - list rows use MaterialCardView with M3 surfaceContainer color,
 *  - search field is a TextInputLayout + TextInputEditText (outlined M3 text field),
 *  - the output picker dialog is a MaterialAlertDialogBuilder (M3 dialog),
 *  - output chips use M3 semantic colors per output key.
 *
 * Business logic (Outputs.get/set, Outputs.mapText, Route.apply) is unchanged.
 */
class AppsActivity : AppCompatActivity() {
    private class App(val pkg: String, val label: String, val icon: Drawable)
    private var all = listOf<App>()
    private var shown = listOf<App>()
    private var query = ""
    private var changed = false
    private lateinit var adapter: BaseAdapter

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        com.google.android.material.color.DynamicColors.applyToActivityIfAvailable(this)
        val surfaceDark = M3.isColorDark(M3.surface(this))
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !surfaceDark
            isAppearanceLightNavigationBars = !surfaceDark
        }

        val dp = resources.displayMetrics.density
        val pad = (12 * dp).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, 0)
        }

        root.addView(TextView(this).apply {
            textSize = 22f; text = "🎛️ " + getString(R.string.apps_title)
            setTextColor(M3.primary(this@AppsActivity)); setTypeface(typeface, Typeface.BOLD)
        })
        root.addView(TextView(this).apply {
            textSize = 13f; text = getString(R.string.apps_hint)
            setTextColor(M3.onSurfaceVariant(this@AppsActivity)); setPadding(0, pad / 2, 0, pad / 2)
        })

        // Search field — M3 outlined text field.
        val cr = (8 * dp).toFloat()  // 8dp corner radius for outlined box (M3 extra-small)
        val til = TextInputLayout(this, null, com.google.android.material.R.attr.textInputOutlinedStyle).apply {
            hint = "🔍 " + getString(R.string.apps_search)
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            setBoxCornerRadii(cr, cr, cr, cr)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = pad / 2 }
        }
        val et = TextInputEditText(til.context).apply {
            isSingleLine = true
            textSize = 15f
            imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) { this@AppsActivity.query = s?.toString().orEmpty().trim(); this@AppsActivity.filter() }
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            })
        }
        til.addView(et)
        root.addView(til)

        adapter = object : BaseAdapter() {
            override fun getCount() = shown.size
            override fun getItem(i: Int): Any = shown[i]
            override fun getItemId(i: Int): Long = i.toLong()
            override fun getView(i: Int, v: View?, parent: ViewGroup?): View {
                val row = (v as? MaterialCardView) ?: row(dp)
                val a = shown[i]; val o = Outputs.get(this@AppsActivity, a.pkg)
                val inner = row.getChildAt(0) as LinearLayout
                (inner.getChildAt(0) as ImageView).setImageDrawable(a.icon)
                val col = inner.getChildAt(1) as LinearLayout
                (col.getChildAt(0) as TextView).text = a.label
                (col.getChildAt(1) as TextView).apply {
                    text = when (o) { "speaker" -> "📱 "; "bt" -> "🎧 "; "wired" -> "🔌 "; else -> "⚙ " } + Outputs.label(this@AppsActivity, o)
                    (background as GradientDrawable).setColor(M3.outputColor(this@AppsActivity, o))
                    setTextColor(M3.outputOnColor(this@AppsActivity, o))
                }
                row.contentDescription = "${a.label}: ${Outputs.label(this@AppsActivity, o)}"
                return row
            }
        }
        val lv = ListView(this).apply {
            adapter = this@AppsActivity.adapter; divider = null; dividerHeight = (8 * dp).toInt(); setPadding(0, pad, 0, pad); clipToPadding = false
            setOnItemClickListener { _, _, i, _ -> pick(shown[i]) }
        }
        root.addView(lv, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            v.setPadding(pad, pad + sys.top, pad, sys.bottom)
            insets
        }

        setContentView(root)
        Thread {
            val pm = packageManager
            val l = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
                .map { it.activityInfo.applicationInfo }.distinctBy { it.packageName }.filter { it.packageName != packageName }
                .map { App(it.packageName, pm.getApplicationLabel(it).toString(), pm.getApplicationIcon(it)) }
            runOnUiThread { all = l; filter() }
        }.start()
    }

    private fun row(dp: Float): MaterialCardView = MaterialCardView(this).apply {
        radius = 16 * dp
        cardElevation = 0f
        strokeWidth = 1
        strokeColor = M3.outlineVariant(this@AppsActivity)
        setContentPadding((16 * dp).toInt(), (10 * dp).toInt(), (16 * dp).toInt(), (10 * dp).toInt())
        val inner = LinearLayout(this@AppsActivity).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
        }
        addView(inner)
        inner.addView(ImageView(this@AppsActivity), LinearLayout.LayoutParams((40 * dp).toInt(), (40 * dp).toInt()).apply { rightMargin = (12 * dp).toInt() })
        inner.addView(LinearLayout(this@AppsActivity).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(this@AppsActivity).apply {
                textSize = 16f; setTextColor(M3.onSurface(this@AppsActivity)); setTypeface(typeface, Typeface.BOLD)
            })
            addView(TextView(this@AppsActivity).apply {
                textSize = 12f; setTypeface(typeface, Typeface.BOLD)
                val h = (10 * dp).toInt(); val v = (3 * dp).toInt(); setPadding(h, v, h, v)
                background = GradientDrawable().apply { cornerRadius = 8 * dp; setColor(M3.surfaceVariant(this@AppsActivity)) }
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = (4 * dp).toInt() })
        })
    }

    private fun filter() {
        shown = all.filter { query.isEmpty() || it.label.contains(query, true) || it.pkg.contains(query, true) }
            .sortedWith(compareBy<App>({ Outputs.get(this, it.pkg) == "default" }, { it.label.lowercase() }))
        adapter.notifyDataSetChanged()
    }

    private fun pick(a: App) {
        val cur = Outputs.KEYS.indexOf(Outputs.get(this, a.pkg)).coerceAtLeast(0)
        MaterialAlertDialogBuilder(this)
            .setTitle(a.label)
            .setSingleChoiceItems(Outputs.KEYS.map<String, CharSequence> { Outputs.label(this, it) }.toTypedArray(), cur) { d, w ->
                Outputs.set(this, a.pkg, Outputs.KEYS[w]); changed = true; adapter.notifyDataSetChanged(); d.dismiss()
            }
            .show()
    }

    override fun onPause() {
        super.onPause()
        if (changed) { changed = false; val c = applicationContext; Thread { Route.apply(c) }.start() }
    }
}
