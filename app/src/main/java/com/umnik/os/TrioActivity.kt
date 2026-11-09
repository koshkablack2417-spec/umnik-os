package com.umnik.os

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Trio: мессенджер Умника (сообщения и видеозвонки).
 * Сам мессенджер - отдельное приложение «Трио», здесь он открывается или предлагается к установке.
 */
class TrioActivity : Activity() {

    private lateinit var status: TextView

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
            setPadding(dp(16), dp(20), dp(16), dp(16))
        }

        root.addView(label("💬 Trio", 24f, Ui.WHITE))
        root.addView(label("Сообщения и видеозвонки от Умника", 13f, Ui.MUTED))

        status = label("", 14f, Ui.WHITE).apply {
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = Ui.rounded(Ui.GLASS, dp(16).toFloat())
        }
        root.addView(status, LinearLayout.LayoutParams(matchParent, wrapContent).apply { topMargin = dp(16) })

        root.addView(
            bigCard("💬", "Сообщения", "Личные чаты, группы и каналы", 0xFF6366F1.toInt()),
            LinearLayout.LayoutParams(matchParent, dp(120)).apply { topMargin = dp(16) }
        )
        root.addView(
            bigCard("📹", "Видеозвонки", "Звонки с видео в приложении Трио", 0xFF0EA5E9.toInt()),
            LinearLayout.LayoutParams(matchParent, dp(120)).apply { topMargin = dp(12) }
        )

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        status.text = if (Launch.installed(this, TRIO_PACKAGE)) {
            "✅ Приложение Трио установлено"
        } else {
            "⬇️ Приложение Трио ещё не установлено. Нажми на любую кнопку ниже, чтобы скачать."
        }
    }

    private fun bigCard(emoji: String, title: String, subtitle: String, color: Int): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = Ui.rounded(color, dp(24).toFloat())
            setPadding(dp(20), dp(12), dp(20), dp(12))
            setOnClickListener { openTrio() }
        }
        card.addView(label(emoji, 40f, Ui.WHITE))
        val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        texts.addView(label(title, 20f, Ui.WHITE).apply { typeface = Typeface.DEFAULT_BOLD })
        texts.addView(label(subtitle, 13f, Ui.WHITE))
        card.addView(
            texts,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = dp(16) }
        )
        return card
    }

    private fun openTrio() {
        if (Launch.openPackage(this, TRIO_PACKAGE)) return
        AlertDialog.Builder(this)
            .setTitle("Трио не установлен")
            .setMessage("Скачать приложение Трио и установить его?")
            .setPositiveButton("Скачать") { _, _ ->
                Launch.safe(this, Intent(Intent.ACTION_VIEW, Uri.parse(TRIO_APK_URL)))
            }
            .setNegativeButton("Позже", null)
            .show()
    }

    companion object {
        private const val TRIO_PACKAGE = "app.trio.messenger"
        private const val TRIO_APK_URL =
            "https://github.com/koshkablack2417-spec/trio-mesenger/releases/latest/download/trio.apk"
    }
}
