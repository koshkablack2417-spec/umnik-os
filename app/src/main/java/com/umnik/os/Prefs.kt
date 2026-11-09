package com.umnik.os

import android.content.Context

/** Настройки Умник ОС: хранятся на телефоне. */
object Prefs {
    private const val FILE = "umnik_settings"

    private fun sp(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun theme(ctx: Context): Int = sp(ctx).getInt("theme", 0)
    fun setTheme(ctx: Context, value: Int) = sp(ctx).edit().putInt("theme", value).apply()

    fun clock24(ctx: Context): Boolean = sp(ctx).getBoolean("clock24", true)
    fun setClock24(ctx: Context, value: Boolean) = sp(ctx).edit().putBoolean("clock24", value).apply()

    fun name(ctx: Context): String = sp(ctx).getString("name", "") ?: ""
    fun setName(ctx: Context, value: String) = sp(ctx).edit().putString("name", value).apply()

    fun city(ctx: Context): String = sp(ctx).getString("city", "") ?: ""
    fun setCity(ctx: Context, value: String) = sp(ctx).edit().putString("city", value).apply()
}
