package com.umnik.os

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextClock
import android.widget.TextView

/** Umplay: автомобильный режим с большими кнопками. Экран не гаснет. */
class UmplayActivity : Activity() {

    private class Tile(val emoji: String, val label: String, val color: Int, val action: () -> Unit)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.load(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val dark = 0xFF05070D.toInt()
        window.statusBarColor = dark
        window.navigationBarColor = dark

        val matchParent = ViewGroup.LayoutParams.MATCH_PARENT
        val wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT

        val tiles = listOf(
            Tile("🧭", "Навигация", 0xFF2563EB.toInt()) {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0")))
                } catch (e: Exception) {
                    Launch.vexo(this, "https://www.google.com/maps")
                }
            },
            Tile("🎵", "Музыка", 0xFFDB2777.toInt()) {
                Launch.safe(
                    this,
                    Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC),
                    "Музыкальное приложение не найдено"
                )
            },
            Tile("📞", "Телефон", 0xFF16A34A.toInt()) {
                Launch.safe(this, Intent(Intent.ACTION_DIAL))
            },
            Tile("🎙", "Умник", 0xFF8B5CF6.toInt()) {
                Launch.safe(
                    this,
                    Intent(this, AssistantActivity::class.java).putExtra(AssistantActivity.EXTRA_LISTEN, true)
                )
            },
            Tile("🌤", "Погода", 0xFF0EA5E9.toInt()) {
                Launch.safe(this, Intent(this, WeatherActivity::class.java))
            },
            Tile("✕", "Выйти", 0xFF475569.toInt()) { finish() }
        )

        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        top.addView(
            TextClock(this).apply {
                format12Hour = "HH:mm"
                format24Hour = "HH:mm"
                textSize = 56f
                setTextColor(Ui.WHITE)
                typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            },
            LinearLayout.LayoutParams(0, wrapContent, 1f)
        )
        top.addView(label("🚗 Umplay", 22f, Ui.WHITE))
        body.addView(top, LinearLayout.LayoutParams(matchParent, wrapContent))

        val grid = GridLayout(this).apply { columnCount = 3 }
        tiles.forEach { tile ->
            val params = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED),
                GridLayout.spec(GridLayout.UNDEFINED, 1f)
            ).apply {
                width = 0
                height = dp(130)
                setMargins(dp(6), dp(6), dp(6), dp(6))
            }
            grid.addView(tileView(tile), params)
        }
        body.addView(grid, LinearLayout.LayoutParams(matchParent, wrapContent).apply { topMargin = dp(16) })

        body.addView(
            label("За рулём пользуйся голосом: нажми «Умник» и говори.", 13f, Ui.MUTED),
            LinearLayout.LayoutParams(wrapContent, wrapContent).apply { topMargin = dp(12) }
        )

        val scroll = ScrollView(this).apply {
            setBackgroundColor(dark)
            addView(body)
        }
        setContentView(scroll)
    }

    private fun tileView(tile: Tile): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = Ui.rounded(tile.color, dp(24).toFloat())
            setOnClickListener { tile.action() }
        }
        card.addView(label(tile.emoji, 40f, Ui.WHITE))
        card.addView(
            label(tile.label, 18f, Ui.WHITE).apply { typeface = Typeface.DEFAULT_BOLD },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply { topMargin = dp(6) }
        )
        return card
    }
}
