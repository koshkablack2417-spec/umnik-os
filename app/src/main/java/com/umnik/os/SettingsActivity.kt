package com.umnik.os

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView

/** Настройки Умник ОС: тема, время, имя, город и переход в системные настройки. */
class SettingsActivity : Activity() {

    private lateinit var nameInput: EditText
    private lateinit var cityInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.load(this)
        window.statusBarColor = Ui.BG_TOP
        window.navigationBarColor = Ui.BG_BOTTOM

        val matchParent = ViewGroup.LayoutParams.MATCH_PARENT
        val wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT

        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        body.addView(label("⚙️ Настройки", 24f, Ui.WHITE))

        // Тема
        body.addView(section("Тема оформления"))
        val themeRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val currentTheme = Prefs.theme(this)
        Ui.THEMES.forEachIndexed { index, theme ->
            themeRow.addView(
                chip(theme.name, index == currentTheme).apply {
                    setOnClickListener {
                        Prefs.setTheme(this@SettingsActivity, index)
                        recreate()
                    }
                },
                LinearLayout.LayoutParams(wrapContent, wrapContent).apply { rightMargin = dp(8) }
            )
        }
        val themeScroll = android.widget.HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(themeRow)
        }
        body.addView(themeScroll, LinearLayout.LayoutParams(matchParent, wrapContent))

        // Формат времени
        body.addView(section("Формат времени"))
        val clock24 = Prefs.clock24(this)
        val timeRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        timeRow.addView(
            chip("24 часа", clock24).apply {
                setOnClickListener {
                    Prefs.setClock24(this@SettingsActivity, true)
                    recreate()
                }
            },
            LinearLayout.LayoutParams(wrapContent, wrapContent).apply { rightMargin = dp(8) }
        )
        timeRow.addView(
            chip("12 часов", !clock24).apply {
                setOnClickListener {
                    Prefs.setClock24(this@SettingsActivity, false)
                    recreate()
                }
            }
        )
        body.addView(timeRow)

        // Имя и город
        body.addView(section("Как тебя называть"))
        nameInput = field("Твоё имя", Prefs.name(this))
        body.addView(nameInput, LinearLayout.LayoutParams(matchParent, dp(46)))

        body.addView(section("Город для погоды"))
        cityInput = field("Например, Москва", Prefs.city(this))
        body.addView(cityInput, LinearLayout.LayoutParams(matchParent, dp(46)))

        // Системное
        body.addView(section("Система"))
        body.addView(
            button("Настройки Android") { Launch.safe(this, Intent(Settings.ACTION_SETTINGS)) },
            LinearLayout.LayoutParams(matchParent, wrapContent).apply { bottomMargin = dp(8) }
        )
        body.addView(
            button("Выбрать главный экран") {
                try {
                    startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                } catch (e: Exception) {
                    Launch.safe(this, Intent(Settings.ACTION_SETTINGS))
                }
            },
            LinearLayout.LayoutParams(matchParent, wrapContent).apply { bottomMargin = dp(8) }
        )

        val version = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: ""
        } catch (e: Exception) {
            ""
        }
        body.addView(
            label("Умник ОС $version\nСделано компанией Умник", 13f, Ui.MUTED),
            LinearLayout.LayoutParams(wrapContent, wrapContent).apply { topMargin = dp(16) }
        )

        val scroll = ScrollView(this).apply {
            background = Ui.gradient()
            setPadding(dp(16), dp(20), dp(16), dp(20))
            addView(body)
        }
        setContentView(scroll)
    }

    private fun section(text: String) =
        label(text, 14f, Ui.MUTED).apply { setPadding(0, dp(20), 0, dp(8)) }

    private fun field(hintText: String, value: String): EditText =
        EditText(this).apply {
            setSingleLine(true)
            hint = hintText
            setHintTextColor(Ui.MUTED)
            setTextColor(Ui.WHITE)
            textSize = 16f
            inputType = InputType.TYPE_CLASS_TEXT
            background = Ui.rounded(Ui.GLASS, dp(14).toFloat())
            setPadding(dp(14), 0, dp(14), 0)
            setText(value)
        }

    private fun button(text: String, onClick: () -> Unit) =
        chip(text, false).apply {
            setPadding(dp(16), dp(14), dp(16), dp(14))
            setOnClickListener { onClick() }
        }

    override fun onPause() {
        Prefs.setName(this, nameInput.text.toString().trim())
        Prefs.setCity(this, cityInput.text.toString().trim())
        super.onPause()
    }
}
