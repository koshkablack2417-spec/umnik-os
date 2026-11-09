package com.umnik.os

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.TextView

/** Общие цвета, темы и мелкие помощники для интерфейса Умник ОС. */
object Ui {
    class Theme(val name: String, val top: Int, val bottom: Int, val accent: Int)

    val THEMES: List<Theme> = listOf(
        Theme("Индиго", 0xFF0F172A.toInt(), 0xFF312E81.toInt(), 0xFF6366F1.toInt()),
        Theme("Океан", 0xFF0B1F33.toInt(), 0xFF0E7490.toInt(), 0xFF0891B2.toInt()),
        Theme("Закат", 0xFF2A1033.toInt(), 0xFFB45309.toInt(), 0xFFEA580C.toInt()),
        Theme("Лес", 0xFF0B1F14.toInt(), 0xFF166534.toInt(), 0xFF16A34A.toInt()),
        Theme("Графит", 0xFF111111.toInt(), 0xFF3F3F46.toInt(), 0xFF71717A.toInt())
    )

    var BG_TOP: Int = THEMES[0].top
    var BG_BOTTOM: Int = THEMES[0].bottom
    var ACCENT: Int = THEMES[0].accent
    val WHITE: Int = 0xFFFFFFFF.toInt()
    val MUTED: Int = 0xFFCBD5E1.toInt()
    val GLASS: Int = 0x33FFFFFF

    /** Читает выбранную тему из настроек. Вызывать в начале каждого экрана. */
    fun load(ctx: Context) {
        val theme = THEMES[Prefs.theme(ctx).coerceIn(0, THEMES.size - 1)]
        BG_TOP = theme.top
        BG_BOTTOM = theme.bottom
        ACCENT = theme.accent
    }

    fun gradient(): GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(BG_TOP, BG_BOTTOM))

    fun rounded(color: Int, radiusPx: Float): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radiusPx
            setColor(color)
        }
}

/** dp -> пиксели. */
fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

/** Обычная надпись. */
fun Context.label(text: String, size: Float = 14f, color: Int = Ui.MUTED): TextView =
    TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(color)
    }

/** Кнопка-«таблетка». */
fun Context.chip(text: String, selected: Boolean): TextView =
    TextView(this).apply {
        this.text = text
        textSize = 14f
        setTextColor(Ui.WHITE)
        gravity = Gravity.CENTER
        setPadding(dp(14), dp(8), dp(14), dp(8))
        background = Ui.rounded(if (selected) Ui.ACCENT else Ui.GLASS, dp(18).toFloat())
    }
