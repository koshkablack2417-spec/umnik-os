package com.umnik.os

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast

/** Главный экран «Умник ОС»: часы, приложения Умника и док. */
class HomeActivity : Activity() {

    private class Tile(
        val emoji: String,
        val label: String,
        val color: Int,
        val action: () -> Unit
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            Tile("🌐", "Vexo", 0xFF2563EB.toInt()) {
                open(Intent(this@HomeActivity, VexoActivity::class.java))
            },
            Tile("📝", "Заметки", 0xFFF59E0B.toInt()) {
                open(Intent(this@HomeActivity, NotesActivity::class.java))
            },
            Tile("⚙️", "Настройки", 0xFF64748B.toInt()) {
                open(Intent(Settings.ACTION_SETTINGS))
            }
        )

        val dockApps = listOf(
            Tile("📞", "Телефон", 0xFF16A34A.toInt()) {
                open(Intent(Intent.ACTION_DIAL))
            },
            Tile("💬", "Сообщения", 0xFF0EA5E9.toInt()) {
                open(Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MESSAGING))
            },
            Tile("📷", "Камера", 0xFFDB2777.toInt()) {
                open(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
            },
            Tile("🗂", "Приложения", 0xFF7C3AED.toInt()) {
                open(Intent(this@HomeActivity, AppsActivity::class.java))
            }
        )

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Ui.BG_TOP, Ui.BG_BOTTOM)
            )
            setPadding(dp(20), dp(28), dp(20), dp(16))
        }

        root.addView(TextView(this).apply {
            text = "Умник ОС"
            setTextColor(Ui.MUTED)
            textSize = 14f
            letterSpacing = 0.1f
        })

        root.addView(TextClock(this).apply {
            format12Hour = "HH:mm"
            format24Hour = "HH:mm"
            textSize = 72f
            setTextColor(Ui.WHITE)
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        })

        root.addView(TextClock(this).apply {
            format12Hour = "EEEE, d MMMM"
            format24Hour = "EEEE, d MMMM"
            textSize = 18f
            setTextColor(Ui.MUTED)
        })

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
            LinearLayout.LayoutParams(matchParent, wrapContent).apply { topMargin = dp(36) }
        )

        // Распорка: прижимает док к низу экрана.
        root.addView(View(this), LinearLayout.LayoutParams(matchParent, 0, 1f))

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

    private fun open(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Не удалось открыть приложение", Toast.LENGTH_SHORT).show()
        }
    }
}
