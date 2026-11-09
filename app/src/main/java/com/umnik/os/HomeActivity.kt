package com.umnik.os

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView

/** Главный экран «Умник ОС»: часы, погода, приложения Умника, поиск и док. */
class HomeActivity : Activity() {

    private class Tile(
        val emoji: String,
        val label: String,
        val color: Int,
        val action: () -> Unit
    )

    // Экран пересобирается при каждом возвращении, чтобы подхватить новую тему и настройки.
    override fun onResume() {
        super.onResume()
        Ui.load(this)
        window.statusBarColor = Ui.BG_TOP
        window.navigationBarColor = Ui.BG_BOTTOM
        setContentView(buildHome())
    }

    // Главный экран - дно стека, «назад» здесь ничего не делает.
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
    }

    private fun buildHome(): View {
        val matchParent = ViewGroup.LayoutParams.MATCH_PARENT
        val wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT

        val umnikApps = listOf(
            Tile("🌐", "Vexo", 0xFF2563EB.toInt()) { Launch.vexo(this) },
            Tile("🛍", "Умник Store", 0xFF10B981.toInt()) { open(StoreActivity::class.java) },
            Tile("🌤", "Погода", 0xFF0EA5E9.toInt()) { open(WeatherActivity::class.java) },
            Tile("🎙", "Умник", 0xFF8B5CF6.toInt()) { open(AssistantActivity::class.java) },
            Tile("💬", "Trio", 0xFF6366F1.toInt()) { open(TrioActivity::class.java) },
            Tile("🚗", "Umplay", 0xFFEF4444.toInt()) { open(UmplayActivity::class.java) },
            Tile("📝", "Заметки", 0xFFF59E0B.toInt()) { open(NotesActivity::class.java) },
            Tile("⚙️", "Настройки", 0xFF64748B.toInt()) { open(SettingsActivity::class.java) }
        )

        val dockApps = listOf(
            Tile("📞", "Телефон", 0xFF16A34A.toInt()) {
                Launch.safe(this, Intent(Intent.ACTION_DIAL))
            },
            Tile("✉️", "Сообщения", 0xFF0284C7.toInt()) {
                Launch.safe(
                    this,
                    Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MESSAGING)
                )
            },
            Tile("📷", "Камера", 0xFFDB2777.toInt()) {
                Launch.safe(this, Intent(MediaStore.ACTION_IMAGE_CAPTURE))
            },
            Tile("🗂", "Приложения", 0xFF7C3AED.toInt()) { open(AppsActivity::class.java) }
        )

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = Ui.gradient()
            setPadding(dp(20), dp(24), dp(20), dp(16))
        }

        val name = Prefs.name(this)
        root.addView(label(if (name.isNotBlank()) "Привет, $name" else "Умник ОС", 14f, Ui.MUTED))

        root.addView(TextClock(this).apply {
            val pattern = if (Prefs.clock24(this@HomeActivity)) "HH:mm" else "h:mm a"
            format12Hour = pattern
            format24Hour = pattern
            textSize = 68f
            setTextColor(Ui.WHITE)
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        })

        root.addView(TextClock(this).apply {
            format12Hour = "EEEE, d MMMM"
            format24Hour = "EEEE, d MMMM"
            textSize = 18f
            setTextColor(Ui.MUTED)
        })

        val weatherLine = label("", 16f, Ui.WHITE)
        root.addView(weatherLine, LinearLayout.LayoutParams(wrapContent, wrapContent).apply { topMargin = dp(6) })
        refreshWeather(weatherLine)

        val grid = GridLayout(this).apply { columnCount = 4 }
        umnikApps.forEach { tile ->
            val params = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED),
                GridLayout.spec(GridLayout.UNDEFINED, 1f)
            ).apply { width = 0 }
            grid.addView(tileView(tile, showLabel = true), params)
        }
        root.addView(
            grid,
            LinearLayout.LayoutParams(matchParent, wrapContent).apply { topMargin = dp(24) }
        )

        // Распорка: прижимает поиск и док к низу экрана.
        root.addView(View(this), LinearLayout.LayoutParams(matchParent, 0, 1f))

        val search = EditText(this).apply {
            setSingleLine(true)
            hint = "🔍  Поиск в Google или адрес сайта"
            setHintTextColor(Ui.MUTED)
            setTextColor(Ui.WHITE)
            textSize = 15f
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            background = Ui.rounded(Ui.GLASS, dp(24).toFloat())
            setPadding(dp(18), 0, dp(18), 0)
            setOnEditorActionListener { _, actionId, event ->
                val enter = event != null &&
                    event.keyCode == KeyEvent.KEYCODE_ENTER &&
                    event.action == KeyEvent.ACTION_DOWN
                if (actionId == EditorInfo.IME_ACTION_SEARCH || enter) {
                    val query = text.toString().trim()
                    if (query.isNotEmpty()) {
                        Launch.vexo(this@HomeActivity, Launch.toUrl(query))
                        setText("")
                    }
                    true
                } else {
                    false
                }
            }
        }
        root.addView(
            search,
            LinearLayout.LayoutParams(matchParent, dp(48)).apply { bottomMargin = dp(12) }
        )

        val dock = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = Ui.rounded(Ui.GLASS, dp(28).toFloat())
            setPadding(dp(8), dp(10), dp(8), dp(10))
        }
        dockApps.forEach { tile ->
            dock.addView(
                tileView(tile, showLabel = false),
                LinearLayout.LayoutParams(0, wrapContent, 1f)
            )
        }
        root.addView(dock, LinearLayout.LayoutParams(matchParent, wrapContent))

        return root
    }

    private fun refreshWeather(line: TextView) {
        line.setOnClickListener { open(WeatherActivity::class.java) }
        val city = Prefs.city(this)
        if (city.isBlank()) {
            line.text = "🌤 Выбери город в Погоде"
            return
        }
        Weather.loadAsync(this, city) { report, _ ->
            if (report != null) {
                val (emoji, _) = Weather.describe(report.code)
                line.text = "$emoji ${Math.round(report.temp)}°C · ${report.city}"
            }
        }
    }

    private fun tileView(tile: Tile, showLabel: Boolean): View {
        val cell = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, dp(6), 0, dp(6))
            setOnClickListener { tile.action() }
        }

        cell.addView(
            TextView(this).apply {
                text = tile.emoji
                textSize = 28f
                gravity = Gravity.CENTER
                background = Ui.rounded(tile.color, dp(18).toFloat())
            },
            LinearLayout.LayoutParams(dp(60), dp(60))
        )

        if (showLabel) {
            cell.addView(
                TextView(this).apply {
                    text = tile.label
                    textSize = 12f
                    setTextColor(Ui.WHITE)
                    gravity = Gravity.CENTER
                    maxLines = 1
                },
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(6) }
            )
        }
        return cell
    }

    private fun open(cls: Class<*>) {
        Launch.safe(this, Intent(this, cls), "Не удалось открыть приложение")
    }
}
