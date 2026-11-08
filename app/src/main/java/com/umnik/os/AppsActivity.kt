package com.umnik.os

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

/** Список всех приложений, установленных на телефоне. */
class AppsActivity : Activity() {

    private class AppItem(val label: String, val icon: Drawable, val intent: Intent)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Ui.BG_TOP
        window.navigationBarColor = Ui.BG_BOTTOM

        val items = loadApps()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Ui.BG_TOP, Ui.BG_BOTTOM)
            )
            setPadding(dp(8), dp(20), dp(8), 0)
        }

        root.addView(TextView(this).apply {
            text = "🗂 Все приложения"
            textSize = 22f
            setTextColor(Ui.WHITE)
            setPadding(dp(12), 0, 0, dp(8))
        })

        val grid = GridView(this).apply {
            numColumns = 4
            verticalSpacing = dp(14)
            clipToPadding = false
            setPadding(0, dp(8), 0, dp(16))
            selector = ColorDrawable(Color.TRANSPARENT)
            setAdapter(AppsAdapter(items))
            setOnItemClickListener { _, _, position, _ ->
                try {
                    startActivity(items[position].intent)
                } catch (e: Exception) {
                    Toast.makeText(this@AppsActivity, "Не удалось открыть приложение", Toast.LENGTH_SHORT).show()
                }
            }
        }
        root.addView(grid, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(root)
    }

    private fun loadApps(): List<AppItem> {
        val pm = packageManager
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(query, 0)
            .filter { it.activityInfo.packageName != packageName }
            .map { info ->
                val launch = Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .setClassName(info.activityInfo.packageName, info.activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                AppItem(info.loadLabel(pm).toString(), info.loadIcon(pm), launch)
            }
            .sortedBy { it.label.lowercase() }
    }

    private inner class AppsAdapter(private val items: List<AppItem>) : BaseAdapter() {
        override fun getCount(): Int = items.size

        override fun getItem(position: Int): Any = items[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val item = items[position]
            val cell = LinearLayout(this@AppsActivity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding(dp(4), dp(4), dp(4), dp(4))
            }
            cell.addView(
                ImageView(this@AppsActivity).apply { setImageDrawable(item.icon) },
                LinearLayout.LayoutParams(dp(52), dp(52))
            )
            cell.addView(
                TextView(this@AppsActivity).apply {
                    text = item.label
                    textSize = 11f
                    setTextColor(Ui.WHITE)
                    gravity = Gravity.CENTER
                    maxLines = 2
                    ellipsize = android.text.TextUtils.TruncateAt.END
                },
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(4) }
            )
            return cell
        }
    }
}
