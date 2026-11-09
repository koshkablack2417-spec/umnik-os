package com.umnik.os

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

/** Vexo - браузер Умника. Внутри открывается Google. */
class VexoActivity : Activity() {

    private lateinit var web: WebView
    private lateinit var urlBar: EditText
    private lateinit var progress: ProgressBar

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.load(this)
        window.statusBarColor = Ui.BG_TOP
        window.navigationBarColor = Ui.BG_TOP

        val matchParent = ViewGroup.LayoutParams.MATCH_PARENT

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Ui.BG_TOP)
        }

        urlBar = EditText(this).apply {
            setSingleLine(true)
            hint = "Поиск Google или адрес сайта"
            setHintTextColor(Ui.MUTED)
            setTextColor(Ui.WHITE)
            textSize = 14f
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            imeOptions = EditorInfo.IME_ACTION_GO
            background = Ui.rounded(Ui.GLASS, dp(20).toFloat())
            setPadding(dp(14), 0, dp(14), 0)
            setSelectAllOnFocus(true)
            setOnEditorActionListener { _, actionId, event ->
                val enterPressed = event != null &&
                    event.keyCode == KeyEvent.KEYCODE_ENTER &&
                    event.action == KeyEvent.ACTION_DOWN
                if (actionId == EditorInfo.IME_ACTION_GO || enterPressed) {
                    go(text.toString())
                    true
                } else {
                    false
                }
            }
        }

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(6), dp(8), dp(6))
        }
        bar.addView(barButton("◀") { if (web.canGoBack()) web.goBack() }, LinearLayout.LayoutParams(dp(40), dp(40)))
        bar.addView(barButton("▶") { if (web.canGoForward()) web.goForward() }, LinearLayout.LayoutParams(dp(40), dp(40)))
        bar.addView(barButton("⟳") { web.reload() }, LinearLayout.LayoutParams(dp(40), dp(40)))
        bar.addView(
            urlBar,
            LinearLayout.LayoutParams(0, dp(40), 1f).apply {
                leftMargin = dp(4)
                rightMargin = dp(4)
            }
        )
        bar.addView(barButton("✕") { finish() }, LinearLayout.LayoutParams(dp(40), dp(40)))

        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progressTintList = ColorStateList.valueOf(Ui.ACCENT)
            visibility = View.GONE
        }

        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    if (url != null && !urlBar.hasFocus()) urlBar.setText(url)
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val uri = request?.url ?: return false
                    val scheme = uri.scheme ?: return false
                    if (scheme == "http" || scheme == "https") return false
                    // tel:, mailto: и другие ссылки отдаём системе.
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, uri))
                    } catch (e: Exception) {
                        // Нет подходящего приложения - просто игнорируем.
                    }
                    return true
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    val bar = this@VexoActivity.progress
                    bar.progress = newProgress
                    bar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
                }
            }
        }
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)

        root.addView(bar, LinearLayout.LayoutParams(matchParent, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(progress, LinearLayout.LayoutParams(matchParent, dp(3)))
        root.addView(web, LinearLayout.LayoutParams(matchParent, 0, 1f))
        setContentView(root)

        if (savedInstanceState == null) {
            go(intent?.dataString ?: START_PAGE)
        } else {
            web.restoreState(savedInstanceState)
        }
    }

    private fun barButton(label: String, onClick: () -> Unit): View =
        TextView(this).apply {
            text = label
            textSize = 18f
            setTextColor(Ui.WHITE)
            gravity = Gravity.CENTER
            setOnClickListener { onClick() }
        }

    private fun go(input: String) {
        web.loadUrl(resolve(input))
        urlBar.clearFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(urlBar.windowToken, 0)
    }

    /** Адрес сайта открываем как есть, всё остальное ищем в Google. */
    private fun resolve(input: String): String {
        val text = input.trim()
        if (text.isEmpty()) return START_PAGE
        if (text.startsWith("http://") || text.startsWith("https://")) return text
        val looksLikeSite = !text.contains(' ') && text.contains('.')
        return if (looksLikeSite) {
            "https://$text"
        } else {
            "https://www.google.com/search?q=" + Uri.encode(text)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        web.saveState(outState)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    override fun onPause() {
        web.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        web.onResume()
    }

    override fun onDestroy() {
        web.destroy()
        super.onDestroy()
    }

    companion object {
        private const val START_PAGE = "https://www.google.com"
    }
}
