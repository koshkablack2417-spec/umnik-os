package com.umnik.os

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.text.InputType
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Умник - голосовой помощник Умник ОС.
 * Понимает простые команды: время, дата, погода, открыть приложение, найти в интернете.
 */
class AssistantActivity : Activity() {

    private lateinit var log: LinearLayout
    private lateinit var scroll: ScrollView
    private lateinit var input: EditText
    private lateinit var status: TextView

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var recognizer: SpeechRecognizer? = null

    private val aliases: Map<String, List<String>> = mapOf(
        "вк" to listOf("вконтакте", "vk"),
        "вконтакте" to listOf("вконтакте", "vk"),
        "макс" to listOf("max"),
        "ватсап" to listOf("whatsapp"),
        "вацап" to listOf("whatsapp"),
        "телеграм" to listOf("telegram"),
        "телеграмм" to listOf("telegram"),
        "ютуб" to listOf("youtube"),
        "инстаграм" to listOf("instagram"),
        "тикток" to listOf("tiktok"),
        "хром" to listOf("chrome"),
        "роблокс" to listOf("roblox"),
        "майнкрафт" to listOf("minecraft"),
        "дискорд" to listOf("discord"),
        "спотифай" to listOf("spotify")
    )

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
        root.addView(label("🎙 Умник", 24f, Ui.WHITE))
        status = label("Нажми 🎤 и говори или пиши команду", 13f, Ui.MUTED)
        root.addView(status)

        log = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll = ScrollView(this).apply { addView(log) }
        root.addView(scroll, LinearLayout.LayoutParams(matchParent, 0, 1f))

        input = EditText(this).apply {
            setSingleLine(true)
            hint = "Напиши команду"
            setHintTextColor(Ui.MUTED)
            setTextColor(Ui.WHITE)
            textSize = 15f
            inputType = InputType.TYPE_CLASS_TEXT
            imeOptions = EditorInfo.IME_ACTION_SEND
            background = Ui.rounded(Ui.GLASS, dp(24).toFloat())
            setPadding(dp(16), 0, dp(16), 0)
            setOnEditorActionListener { _, actionId, event ->
                val enter = event != null &&
                    event.keyCode == KeyEvent.KEYCODE_ENTER &&
                    event.action == KeyEvent.ACTION_DOWN
                if (actionId == EditorInfo.IME_ACTION_SEND || enter) {
                    sendTyped()
                    true
                } else {
                    false
                }
            }
        }
        val send = chip("➤", false).apply {
            textSize = 20f
            setOnClickListener { sendTyped() }
        }
        val mic = TextView(this).apply {
            text = "🎤"
            textSize = 24f
            gravity = Gravity.CENTER
            background = Ui.rounded(Ui.ACCENT, dp(26).toFloat())
            setOnClickListener { startListening() }
        }

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        bottom.addView(input, LinearLayout.LayoutParams(0, dp(48), 1f))
        bottom.addView(send, LinearLayout.LayoutParams(dp(48), dp(48)).apply { leftMargin = dp(8) })
        bottom.addView(mic, LinearLayout.LayoutParams(dp(52), dp(52)).apply { leftMargin = dp(8) })
        root.addView(bottom, LinearLayout.LayoutParams(matchParent, wrapContent).apply { topMargin = dp(10) })

        setContentView(root)

        tts = TextToSpeech(this) { code ->
            if (code == TextToSpeech.SUCCESS) {
                ttsReady = true
                tts?.setLanguage(Locale("ru", "RU"))
            }
        }

        val name = Prefs.name(this)
        val hello = if (name.isNotBlank()) "Привет, $name!" else "Привет!"
        addBubble(
            "$hello Я Умник. Скажи или напиши: «который час», «открой ВК», «найди рецепт борща», «погода».",
            false
        )

        if (intent.getBooleanExtra(EXTRA_LISTEN, false)) startListening()
    }

    private fun sendTyped() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) return
        input.setText("")
        handle(text)
    }

    private fun startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQ_MIC)
            return
        }
        if (SpeechRecognizer.isRecognitionAvailable(this)) listenInApp() else listenViaSystem()
    }

    private fun speechIntent(): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")

    private fun listenInApp() {
        val r = recognizer ?: SpeechRecognizer.createSpeechRecognizer(this).also { recognizer = it }
        r.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                status.text = "Слушаю…"
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                status.text = "Думаю…"
            }

            override fun onError(error: Int) {
                status.text = "Не расслышал. Нажми 🎤 ещё раз или напиши команду."
            }

            override fun onResults(results: Bundle?) {
                status.text = "Нажми 🎤 и говори или пиши команду"
                val said = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (said != null) handle(said)
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        r.startListening(speechIntent())
    }

    @Suppress("DEPRECATION")
    private fun listenViaSystem() {
        try {
            startActivityForResult(speechIntent(), REQ_SPEECH)
        } catch (e: Exception) {
            addBubble(
                "На этом телефоне нет службы распознавания речи, поэтому слушать я не могу. " +
                    "Пиши команды текстом, я отвечу.",
                false
            )
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_SPEECH && resultCode == RESULT_OK) {
            val said = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (said != null) handle(said)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_MIC) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening()
            } else {
                addBubble("Без доступа к микрофону я не услышу тебя. Пиши команды текстом.", false)
            }
        }
    }

    private fun handle(raw: String) {
        addBubble(raw, true)
        val reply = answer(raw.lowercase().trim())
        addBubble(reply, false)
        if (ttsReady) tts?.speak(reply, TextToSpeech.QUEUE_FLUSH, null, "umnik")
    }

    private fun answer(t: String): String {
        val now = Date()
        val ru = Locale("ru")
        return when {
            t.contains("привет") || t.contains("здравствуй") -> {
                val name = Prefs.name(this)
                if (name.isNotBlank()) "Привет, $name! Чем помочь?" else "Привет! Чем помочь?"
            }
            t.contains("как дела") -> "Отлично, работаю в Умник ОС. Чем помочь?"
            t.contains("кто ты") || t.contains("как тебя зовут") ->
                "Я Умник, голосовой помощник Умник ОС."
            t.contains("который час") || t.contains("сколько времени") || t == "время" ->
                "Сейчас " + SimpleDateFormat("HH:mm", ru).format(now)
            t.contains("какое сегодня число") || t.contains("какой сегодня день") || t.contains("какая сегодня дата") ->
                "Сегодня " + SimpleDateFormat("EEEE, d MMMM", ru).format(now)
            t.contains("погод") -> {
                Launch.safe(this, Intent(this, WeatherActivity::class.java))
                "Открываю погоду"
            }
            t.startsWith("найди ") || t.startsWith("поищи ") || t.startsWith("загугли ") -> {
                val q = t.substringAfter(' ').trim()
                Launch.search(this, q)
                "Ищу: $q"
            }
            t.startsWith("открой ") || t.startsWith("запусти ") || t.startsWith("включи ") ->
                openByName(t.substringAfter(' ').trim())
            else -> {
                Launch.search(this, t)
                "Такой команды я не знаю, поищу в интернете: $t"
            }
        }
    }

    private fun openByName(name: String): String {
        fun go(cls: Class<*>, text: String): String {
            Launch.safe(this, Intent(this, cls))
            return text
        }
        return when {
            name.contains("браузер") || name.contains("vexo") || name.contains("векс") || name.contains("интернет") -> {
                Launch.vexo(this)
                "Открываю Vexo"
            }
            name.contains("заметк") -> go(NotesActivity::class.java, "Открываю заметки")
            name.contains("настройк") -> go(SettingsActivity::class.java, "Открываю настройки")
            name.contains("магазин") || name.contains("store") || name.contains("стор") ->
                go(StoreActivity::class.java, "Открываю Умник Store")
            name.contains("погод") -> go(WeatherActivity::class.java, "Открываю погоду")
            name.contains("umplay") || name.contains("амплей") || name.contains("авто") || name.contains("машин") ->
                go(UmplayActivity::class.java, "Включаю Umplay")
            name.contains("trio") || name.contains("трио") -> go(TrioActivity::class.java, "Открываю Trio")
            name.contains("камер") -> {
                Launch.safe(this, Intent(MediaStore.ACTION_IMAGE_CAPTURE))
                "Открываю камеру"
            }
            name.contains("телефон") || name.contains("звонил") -> {
                Launch.safe(this, Intent(Intent.ACTION_DIAL))
                "Открываю телефон"
            }
            else -> {
                val candidates = (aliases[name] ?: emptyList()) + name
                if (candidates.any { Launch.openAppByName(this, it) }) {
                    "Открываю $name"
                } else {
                    "Не нашёл приложение «$name». Загляни в Умник Store."
                }
            }
        }
    }

    private fun addBubble(text: String, mine: Boolean) {
        val bubble = TextView(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(Ui.WHITE)
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = Ui.rounded(if (mine) Ui.ACCENT else Ui.GLASS, dp(18).toFloat())
            maxWidth = (resources.displayMetrics.widthPixels * 0.8f).toInt()
        }
        log.addView(
            bubble,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                gravity = if (mine) Gravity.END else Gravity.START
                topMargin = dp(8)
            }
        )
        scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
    }

    override fun onDestroy() {
        tts?.shutdown()
        recognizer?.destroy()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_LISTEN = "listen"
        private const val REQ_MIC = 1
        private const val REQ_SPEECH = 2
    }
}
