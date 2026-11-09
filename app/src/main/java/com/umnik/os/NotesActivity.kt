package com.umnik.os

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

/** Простые заметки: текст сохраняется на телефоне автоматически. */
class NotesActivity : Activity() {

    private lateinit var edit: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.load(this)
        window.statusBarColor = Ui.BG_TOP
        window.navigationBarColor = Ui.BG_BOTTOM

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Ui.BG_TOP)
            setPadding(dp(16), dp(20), dp(16), dp(16))
        }

        root.addView(TextView(this).apply {
            text = "📝 Заметки"
            textSize = 24f
            setTextColor(Ui.WHITE)
        })

        edit = EditText(this).apply {
            hint = "Пиши здесь…"
            setHintTextColor(Ui.MUTED)
            setTextColor(Ui.WHITE)
            textSize = 16f
            gravity = Gravity.TOP or Gravity.START
            background = Ui.rounded(Ui.GLASS, dp(16).toFloat())
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setText(getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_TEXT, ""))
        }
        root.addView(
            edit,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f).apply {
                topMargin = dp(16)
            }
        )

        setContentView(root)
    }

    override fun onPause() {
        getSharedPreferences(PREFS, MODE_PRIVATE)
            .edit()
            .putString(KEY_TEXT, edit.text.toString())
            .apply()
        super.onPause()
    }

    companion object {
        private const val PREFS = "umnik_notes"
        private const val KEY_TEXT = "text"
    }
}
