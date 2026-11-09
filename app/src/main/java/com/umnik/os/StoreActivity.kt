package com.umnik.os

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

/** Умник Store: каталог приложений. Установленные открываются, остальные ищутся в магазинах. */
class StoreActivity : Activity() {

    private var query = ""
    private var category = StoreCatalog.ALL
    private var shown: List<StoreApp> = StoreCatalog.apps

    private lateinit var adapter: StoreAdapter
    private lateinit var more: TextView
    private val chips = mutableListOf<Pair<String, TextView>>()

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
            setPadding(dp(12), dp(20), dp(12), 0)
        }

        root.addView(label("🛍 Умник Store", 24f, Ui.WHITE))
        root.addView(label("Открывай установленные приложения и находи новые", 13f, Ui.MUTED))

        val search = EditText(this).apply {
            setSingleLine(true)
            hint = "🔍  Найти приложение"
            setHintTextColor(Ui.MUTED)
            setTextColor(Ui.WHITE)
            textSize = 15f
            inputType = InputType.TYPE_CLASS_TEXT
            background = Ui.rounded(Ui.GLASS, dp(22).toFloat())
            setPadding(dp(16), 0, dp(16), 0)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    query = s?.toString() ?: ""
                    refresh()
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
        root.addView(search, LinearLayout.LayoutParams(matchParent, dp(44)).apply { topMargin = dp(12) })

        val chipRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        StoreCatalog.categories.forEach { name ->
            val c = chip(name, name == category)
            c.setOnClickListener {
                category = name
                styleChips()
                refresh()
            }
            chips.add(name to c)
            chipRow.addView(c, LinearLayout.LayoutParams(wrapContent, wrapContent).apply { rightMargin = dp(8) })
        }
        val chipScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(chipRow)
        }
        root.addView(
            chipScroll,
            LinearLayout.LayoutParams(matchParent, wrapContent).apply { topMargin = dp(10); bottomMargin = dp(6) }
        )

        adapter = StoreAdapter()
        val list = ListView(this).apply {
            divider = null
            dividerHeight = 0
            setAdapter(adapter)
            setOnItemClickListener { _, _, position, _ -> onAppClick(shown[position]) }
        }
        root.addView(list, LinearLayout.LayoutParams(matchParent, 0, 1f))

        more = label("", 14f, Ui.WHITE).apply {
            gravity = Gravity.CENTER
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = Ui.rounded(Ui.ACCENT, dp(16).toFloat())
            visibility = View.GONE
            setOnClickListener { askSearchOnline() }
        }
        root.addView(
            more,
            LinearLayout.LayoutParams(matchParent, wrapContent).apply { topMargin = dp(8); bottomMargin = dp(12) }
        )

        setContentView(root)
        refresh()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) adapter.notifyDataSetChanged()
    }

    private fun styleChips() {
        for ((name, view) in chips) {
            view.background = Ui.rounded(if (name == category) Ui.ACCENT else Ui.GLASS, dp(18).toFloat())
        }
    }

    private fun refresh() {
        val q = query.trim()
        shown = StoreCatalog.apps.filter { app ->
            (category == StoreCatalog.ALL || app.category == category) &&
                (q.isEmpty() || app.name.contains(q, true) || app.desc.contains(q, true))
        }
        adapter.notifyDataSetChanged()
        if (q.isEmpty()) {
            more.visibility = View.GONE
        } else {
            more.visibility = View.VISIBLE
            more.text = "Не нашёл? Искать «$q» в RuStore и Google Play"
        }
    }

    private fun onAppClick(app: StoreApp) {
        val reason = app.unavailable
        when {
            reason != null -> Toast.makeText(this, "${app.name}: $reason", Toast.LENGTH_SHORT).show()
            Launch.installed(this, app.pkg) -> Launch.openPackage(this, app.pkg)
            else -> {
                AlertDialog.Builder(this)
                    .setTitle("Получить ${app.name}")
                    .setItems(arrayOf("В RuStore", "В Google Play")) { _, which ->
                        val url = if (which == 0) {
                            "https://www.rustore.ru/catalog/app/${app.pkg}"
                        } else {
                            "https://play.google.com/store/apps/details?id=${app.pkg}"
                        }
                        Launch.safe(this, Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                    .show()
            }
        }
    }

    private fun askSearchOnline() {
        val text = query.trim()
        val q = Uri.encode(text)
        AlertDialog.Builder(this)
            .setTitle("Искать «$text»")
            .setItems(arrayOf("В RuStore", "В Google Play")) { _, which ->
                val url = if (which == 0) {
                    "https://www.rustore.ru/catalog/search?query=$q"
                } else {
                    "https://play.google.com/store/search?q=$q&c=apps"
                }
                Launch.safe(this, Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            .show()
    }

    private inner class StoreAdapter : BaseAdapter() {
        override fun getCount(): Int = shown.size

        override fun getItem(position: Int): Any = shown[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val app = shown[position]
            val installed = Launch.installed(this@StoreActivity, app.pkg)

            val row = LinearLayout(this@StoreActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(8), dp(10), dp(8), dp(10))
            }
            row.addView(
                TextView(this@StoreActivity).apply {
                    text = app.emoji
                    textSize = 24f
                    gravity = Gravity.CENTER
                    background = Ui.rounded(Ui.GLASS, dp(14).toFloat())
                },
                LinearLayout.LayoutParams(dp(52), dp(52))
            )

            val column = LinearLayout(this@StoreActivity).apply { orientation = LinearLayout.VERTICAL }
            column.addView(
                label(app.name, 16f, Ui.WHITE).apply { typeface = Typeface.DEFAULT_BOLD }
            )
            column.addView(label(app.unavailable ?: app.desc, 12f, Ui.MUTED))
            row.addView(
                column,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = dp(12) }
            )

            val buttonText = when {
                app.unavailable != null -> "Недоступно"
                installed -> "Открыть"
                else -> "Получить"
            }
            row.addView(chip(buttonText, app.unavailable == null))
            return row
        }
    }
}
