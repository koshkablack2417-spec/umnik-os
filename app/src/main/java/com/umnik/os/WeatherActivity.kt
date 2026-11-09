package com.umnik.os

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Погода: текущая и на 5 дней. */
class WeatherActivity : Activity() {

    private lateinit var input: EditText
    private lateinit var status: TextView
    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.load(this)
        window.statusBarColor = Ui.BG_TOP
        window.navigationBarColor = Ui.BG_BOTTOM

        val matchParent = ViewGroup.LayoutParams.MATCH_PARENT
        val wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = Ui.gradient()
            setPadding(dp(16), dp(20), dp(16), dp(12))
        }
        root.addView(label("🌤 Погода", 24f, Ui.WHITE))

        input = EditText(this).apply {
            setSingleLine(true)
            hint = "Город, например Москва"
            setHintTextColor(Ui.MUTED)
            setTextColor(Ui.WHITE)
            textSize = 16f
            inputType = InputType.TYPE_CLASS_TEXT
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            background = Ui.rounded(Ui.GLASS, dp(22).toFloat())
            setPadding(dp(16), 0, dp(16), 0)
            setOnEditorActionListener { _, actionId, event ->
                val enter = event != null &&
                    event.keyCode == KeyEvent.KEYCODE_ENTER &&
                    event.action == KeyEvent.ACTION_DOWN
                if (actionId == EditorInfo.IME_ACTION_SEARCH || enter) {
                    search()
                    true
                } else {
                    false
                }
            }
        }
        val find = chip("Найти", true).apply { setOnClickListener { search() } }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        row.addView(input, LinearLayout.LayoutParams(0, dp(44), 1f))
        row.addView(find, LinearLayout.LayoutParams(wrapContent, dp(44)).apply { leftMargin = dp(8) })
        root.addView(row, LinearLayout.LayoutParams(matchParent, wrapContent).apply { topMargin = dp(12) })

        status = label("", 14f, Ui.MUTED)
        root.addView(status, LinearLayout.LayoutParams(matchParent, wrapContent).apply { topMargin = dp(10) })

        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(content) }
        root.addView(scroll, LinearLayout.LayoutParams(matchParent, 0, 1f))

        setContentView(root)

        val saved = Prefs.city(this)
        input.setText(saved)
        if (saved.isNotBlank()) load(saved) else status.text = "Введи город и нажми «Найти»"
    }

    private fun search() {
        val city = input.text.toString().trim()
        if (city.isEmpty()) {
            status.text = "Введи название города"
            return
        }
        load(city)
    }

    private fun load(city: String) {
        status.text = "Загружаю…"
        content.removeAllViews()
        Weather.loadAsync(this, city) { report, error ->
            if (report == null) {
                status.text = error ?: "Ошибка"
            } else {
                status.text = ""
                Prefs.setCity(this, city)
                show(report)
            }
        }
    }

    private fun show(report: Weather.Report) {
        content.removeAllViews()
        val (emoji, description) = Weather.describe(report.code)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = Ui.rounded(Ui.GLASS, dp(24).toFloat())
            setPadding(dp(16), dp(24), dp(16), dp(24))
        }
        card.addView(label(report.city, 20f, Ui.WHITE))
        card.addView(label(emoji, 64f, Ui.WHITE))
        card.addView(label("${Math.round(report.temp)}°C", 56f, Ui.WHITE))
        card.addView(label(description, 18f, Ui.MUTED))
        card.addView(
            label(
                "Ощущается как ${Math.round(report.feels)}°C · ветер ${Math.round(report.wind)} м/с · влажность ${report.humidity}%",
                13f,
                Ui.MUTED
            ).apply { gravity = Gravity.CENTER }
        )
        content.addView(
            card,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )

        content.addView(
            label("Ближайшие дни", 16f, Ui.WHITE),
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply { topMargin = dp(20); bottomMargin = dp(6) }
        )
        report.days.forEach { day ->
            val (dayEmoji, _) = Weather.describe(day.code)
            val line = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = Ui.rounded(Ui.GLASS, dp(14).toFloat())
                setPadding(dp(14), dp(10), dp(14), dp(10))
            }
            line.addView(label(day.label, 15f, Ui.WHITE), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            line.addView(label(dayEmoji, 22f, Ui.WHITE))
            line.addView(
                label("${Math.round(day.min)}° / ${Math.round(day.max)}°", 15f, Ui.WHITE),
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    .apply { leftMargin = dp(14) }
            )
            content.addView(
                line,
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    .apply { bottomMargin = dp(8) }
            )
        }
    }
}
