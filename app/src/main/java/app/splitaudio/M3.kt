package app.splitaudio

import android.content.Context
import android.util.TypedValue
import androidx.core.content.ContextCompat

/**
 * Material 3 semantic color resolver.
 *
 * Screens should never hardcode ARGB literals. Instead they call [M3.color] (or one of the
 * typed accessors) with the current Activity context; the color is resolved from the current
 * theme (`Theme.SplitAudio`, parent `Theme.Material3.DayNight`). On Android 12+ the Material
 * library overrides these roles with the user's dynamic color scheme at runtime, so colors
 * automatically follow the system palette without any per-screen branching.
 *
 * The accessors below resolve `?attr/color*` (Material 3 theme attributes from
 * `com.google.android.material.R.attr`) to ARGB Ints that the existing programmatic UI can
 * pass to `setColor(...)`, `setStroke(...)`, gradient constructors, etc.
 */
object M3 {
    /** Resolve a theme attribute to an ARGB Int. */
    fun color(ctx: Context, attrId: Int): Int {
        val tv = TypedValue()
        if (!ctx.theme.resolveAttribute(attrId, tv, true)) {
            // Fall back to black so a missing attribute is visible rather than crashing.
            return 0xFF000000.toInt()
        }
        return if (tv.type >= TypedValue.TYPE_FIRST_COLOR_INT && tv.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            tv.data
        } else if (tv.resourceId != 0) {
            ContextCompat.getColor(ctx, tv.resourceId)
        } else {
            0xFF000000.toInt()
        }
    }

    fun primary(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorPrimary)
    fun onPrimary(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorOnPrimary)
    fun primaryContainer(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorPrimaryContainer)
    fun onPrimaryContainer(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorOnPrimaryContainer)
    fun secondary(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorSecondary)
    fun onSecondary(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorOnSecondary)
    fun secondaryContainer(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorSecondaryContainer)
    fun onSecondaryContainer(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorOnSecondaryContainer)
    fun tertiary(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorTertiary)
    fun onTertiary(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorOnTertiary)
    fun tertiaryContainer(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorTertiaryContainer)
    fun onTertiaryContainer(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorOnTertiaryContainer)
    fun error(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorError)
    fun onError(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorOnError)
    fun errorContainer(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorErrorContainer)
    fun onErrorContainer(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorOnErrorContainer)
    fun background(ctx: Context) = color(ctx, android.R.attr.colorBackground)
    fun onBackground(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorOnBackground)
    fun surface(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorSurface)
    fun onSurface(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorOnSurface)
    fun surfaceVariant(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorSurfaceVariant)
    fun onSurfaceVariant(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorOnSurfaceVariant)
    fun surfaceContainer(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorSurfaceContainer)
    fun surfaceContainerHigh(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorSurfaceContainerHigh)
    fun outline(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorOutline)
    fun outlineVariant(ctx: Context) = color(ctx, com.google.android.material.R.attr.colorOutlineVariant)

    /** Status-driven color: live "on" → primary, "off" (waiting) → secondary, else → surfaceVariant. */
    fun statusColor(ctx: Context, state: String?): Int = when (state) {
        "on" -> primary(ctx)
        "off" -> secondary(ctx)
        else -> surfaceVariant(ctx)
    }

    /** Status-driven on-color (text/icon color on top of [statusColor]). */
    fun statusOnColor(ctx: Context, state: String?): Int = when (state) {
        "on" -> onPrimary(ctx)
        "off" -> onSecondary(ctx)
        else -> onSurfaceVariant(ctx)
    }

    /** Chip-state semantic colors: 0=missing(error), 1=ok(primary), 2=recommended(secondary). */
    fun chipColor(ctx: Context, level: Int): Int = when (level) {
        0 -> error(ctx)
        1 -> primary(ctx)
        else -> secondary(ctx)
    }

    /** Output chip color per Outputs.KEYS index. */
    fun outputColor(ctx: Context, key: String): Int = when (key) {
        "speaker" -> tertiary(ctx)
        "bt" -> primary(ctx)
        "wired" -> secondary(ctx)
        else -> surfaceVariant(ctx)
    }

    /** Output chip text color per Outputs.KEYS index. */
    fun outputOnColor(ctx: Context, key: String): Int = when (key) {
        "speaker" -> onTertiary(ctx)
        "bt" -> onPrimary(ctx)
        "wired" -> onSecondary(ctx)
        else -> onSurfaceVariant(ctx)
    }

    /** Perceptual darkness test (luminance < 0.5 → "dark"). Used for status-bar icon tint. */
    fun isColorDark(color: Int): Boolean {
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF
        val lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
        return lum < 0.5
    }
}
