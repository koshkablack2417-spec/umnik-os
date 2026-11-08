package com.umnik.os

import android.content.Context
import android.graphics.drawable.GradientDrawable

/** Общие цвета и мелкие помощники для интерфейса Умник ОС. */
object Ui {
    val BG_TOP: Int = 0xFF0F172A.toInt()
    val BG_BOTTOM: Int = 0xFF312E81.toInt()
    val ACCENT: Int = 0xFF6366F1.toInt()
    val WHITE: Int = 0xFFFFFFFF.toInt()
    val MUTED: Int = 0xFFA5B4FC.toInt()
    val GLASS: Int = 0x33FFFFFF

    fun rounded(color: Int, radiusPx: Float): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radiusPx
            setColor(color)
        }
}

/** dp -> пиксели. */
fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
