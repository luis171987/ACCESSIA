package org.accessia.browser

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.speech.tts.UtteranceProgressListener
import android.view.Gravity
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.WebExtension
import java.net.URLEncoder
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import java.text.Normalizer
import java.util.Locale
import kotlin.math.min

/** ACCESSIA: navegador nativo con controles opcionales. Voz solo mientras la actividad está visible. */
class MainActivity : Activity() {
    private lateinit var session: GeckoSession
    private lateinit var runtime: GeckoRuntime
    private lateinit var address: EditText
    private lateinit var status: TextView
    private lateinit var voiceButton: Button
    private lateinit var welcomeVoiceButton: Button
    private lateinit var floatingButton: Button
    private lateinit var floatingPanel: ScrollView
    private lateinit var welcomePanel: LinearLayout
    private val handler = Handler(Looper.getMainLooper())
    private val prefs by lazy { getSharedPreferences("accessia_v02", MODE_PRIVATE) }
    private var contentPort: WebExtension.Port? = null
    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var voiceEnabled = false
    private var voiceActive = false
    private var speechGeneration = 0
    private var speaking = false
    private var ttsReady = false
    private var consecutiveErrors = 0
    private var destroyed = false
    private var canGoBack = false
    private var canGoForward = false
    private var currentUrl = "about:blank"
    private var autoConsentPending = false
    // Dictado acumulativo: solo texto editable, nunca grabaciones persistentes.
    private var longDictationEnabled = false
    private var lastPartialTranscript = ""
    private val restartListening = Runnable { if (voiceEnabled && !speaking) listenOnce() }
    private val navy = Color.rgb(7, 20, 48)
    private val cyan = Color.rgb(2, 195, 238)

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()

    private fun surface(color: Int, radius: Int = 14, border: Int = 0): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (border != 0) setStroke(dp(1), border)
        }

    private fun actionButton(label: String, task: () -> Unit): Button = Button(this).apply {
        text = label
        textSize = 13f
        isAllCaps = false
        setTextColor(Color.WHITE)
        background = surface(Color.rgb(20, 48, 88), 12, Color.rgb(45, 91, 143))
        minHeight = dp(48)
        minimumHeight = dp(48)
        contentDescription = when (label) {
            "⋯" -> "Abrir menú ACCESSIA"
            "⟳" -> "Recargar página"
            else -> label
        }
        setOnClickListener { task() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = navy
        window.navigationBarColor = navy

        val webView = buildInterface()

        runtime = GeckoRuntime.create(this)
        runtime.settings.setLocales(arrayOf(effectiveLanguage("site_language")))
        session = GeckoSession()
        session.setContentDelegate(object : GeckoSession.ContentDelegate {})
        session.setNavigationDelegate(object : GeckoSession.NavigationDelegate {
            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                this@MainActivity.canGoBack = canGoBack
            }
            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                this@MainActivity.canGoForward = canGoForward
            }
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: List<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                if (!url.isNullOrBlank()) {
                    currentUrl = url
                    welcomePanel.visibility = if (url == "about:blank") View.VISIBLE else View.GONE
                    if (!address.hasFocus()) address.setText(if (url == "about:blank") "" else url)
                    if (url.startsWith("https://")) remember("historial", url, 120)
                }
            }
        })
        session.open(runtime)
        webView.setSession(session)
        setupExtension()
        // Inicio rápido: página local vacía; no fuerza red ni descargas de Wikipedia.
        session.loadUri("about:blank")
        setupSpeech()
        setupTts()
        handler.post { offerRegionLanguageSuggestion() }
    }

    private fun buildInterface(): GeckoView {
        val root = FrameLayout(this).apply { setBackgroundColor(Color.WHITE) }
        val main = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(main, FrameLayout.LayoutParams(-1, -1))

        val heading = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(8))
            setBackgroundColor(navy)
        }
        heading.addView(ImageView(this).apply {
            setImageResource(R.drawable.accessia_icon)
            contentDescription = "Logotipo oficial ACCESSIA"
            scaleType = ImageView.ScaleType.FIT_CENTER
        }, LinearLayout.LayoutParams(dp(42), dp(42)))
        heading.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), 0, 0, 0)
            addView(TextView(context).apply {
                text = "ACCESSIA"
                textSize = 21f
                setTextColor(Color.WHITE)
                typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            })
            addView(TextView(context).apply {
                text = "Creado por GENAI INDUSTRIES"
                textSize = 10f
                setTextColor(Color.rgb(160, 221, 247))
            })
        }, LinearLayout.LayoutParams(0, -2, 1f))
        heading.addView(actionButton("Ayuda") { showCommandGuide() }, LinearLayout.LayoutParams(dp(72), dp(48)))
        heading.addView(actionButton("⋯") { showMenu() }, LinearLayout.LayoutParams(dp(52), dp(48)))
        main.addView(heading)

        val addressBar = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(9), dp(6), dp(9), dp(6))
            setBackgroundColor(Color.rgb(243, 247, 252))
        }
        address = EditText(this).apply {
            hint = "Buscar o escribir dirección"
            contentDescription = "Búsqueda o dirección web"
            textSize = 15f
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_GO
            background = surface(Color.WHITE, 14, Color.rgb(190, 207, 224))
            setPadding(dp(12), dp(8), dp(12), dp(8))
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    submitAddress(); true
                } else false
            }
        }
        addressBar.addView(address, LinearLayout.LayoutParams(0, dp(49), 1f))
        addressBar.addView(actionButton("Dictar +") { toggleLongDictation() },
            LinearLayout.LayoutParams(dp(87), dp(48)).apply { leftMargin = dp(5) })
        addressBar.addView(actionButton("Ir") { submitAddress() }.apply {
            setBackgroundColor(navy)
        }, LinearLayout.LayoutParams(dp(57), dp(48)).apply { leftMargin = dp(6) })
        main.addView(addressBar)

        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(4), dp(4), dp(4), dp(4))
            setBackgroundColor(navy)
        }
        listOf(
            "← Atrás" to { goBack() },
            "→ Adelante" to { goForward() },
            "⟳" to { session.reload() },
            "★ Favorito" to { toggleFavorite() },
            "Historial" to { showLinks("historial", "Historial") },
            "Descargas" to { showDownloads() }
        ).forEach { (name, task) ->
            buttons.addView(actionButton(name, task), LinearLayout.LayoutParams(-2, dp(48)).apply {
                leftMargin = dp(3); rightMargin = dp(3)
            })
        }
        main.addView(HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(buttons)
        })

        val pageHolder = FrameLayout(this)
        val webView = GeckoView(this).apply { contentDescription = "Página web de ACCESSIA" }
        pageHolder.addView(webView, FrameLayout.LayoutParams(-1, -1))
        welcomePanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(20), dp(24), dp(20))
            setBackgroundColor(Color.rgb(244, 249, 252))
            addView(TextView(context).apply {
                text = "Bienvenido a ACCESSIA"
                textSize = 23f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(navy)
                gravity = Gravity.CENTER
            })
            addView(TextView(context).apply {
                text = "Aquí puedes buscar, navegar por voz y aprender los comandos. " +
                    "Di «ayuda» o pulsa el botón Guía de comandos."
                textSize = 16f
                setTextColor(Color.rgb(32, 60, 92))
                gravity = Gravity.CENTER
                setPadding(0, dp(18), 0, dp(18))
            })
            addView(actionButton("📖 Guía de comandos") { showCommandGuide() })
            welcomeVoiceButton = actionButton("🎙 Activar micrófono") { toggleVoice() }
            addView(welcomeVoiceButton)
        }
        pageHolder.addView(welcomePanel, FrameLayout.LayoutParams(-1, -1))
        main.addView(pageHolder, LinearLayout.LayoutParams(-1, 0, 1f))

        val bottom = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(navy)
            setPadding(dp(10), dp(5), dp(10), dp(5))
        }
        status = TextView(this).apply {
            text = "Listo · voz apagada"
            setTextColor(Color.WHITE)
            textSize = 12f
            maxLines = 2
        }
        bottom.addView(status, LinearLayout.LayoutParams(0, -2, 1f))
        voiceButton = actionButton("🎙 Activar voz") { toggleVoice() }
        bottom.addView(voiceButton, LinearLayout.LayoutParams(-2, dp(48)))
        main.addView(bottom)

        val floatingActions = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
            background = surface(navy, 18, cyan)
            elevation = dp(12).toFloat()
        }
        listOf(
            "Control siguiente" to { sendWebCommand("cursor siguiente") },
            "Seleccionar" to { sendWebCommand("seleccionar") },
            "Pausar vídeo" to { sendWebCommand("pausar") },
            "Continuar vídeo" to { sendWebCommand("continuar") },
            "Estudio automático" to { askAutoStudy() },
            "Favoritos" to { showLinks("favoritos", "Favoritos") },
            "Salir de ACCESSIA" to { exitApp() }
        ).forEach { (name, task) ->
            floatingActions.addView(actionButton(name, task), LinearLayout.LayoutParams(dp(190), -2).apply {
                bottomMargin = dp(3)
            })
        }
        floatingPanel = ScrollView(this).apply {
            addView(floatingActions)
            visibility = View.GONE
            elevation = dp(12).toFloat()
        }
        root.addView(floatingPanel, FrameLayout.LayoutParams(dp(206), -2, Gravity.RIGHT or Gravity.BOTTOM).apply {
            rightMargin = dp(14); bottomMargin = dp(136)
        })
        floatingButton = actionButton("☰") { setFloatingExpanded(floatingPanel.visibility != View.VISIBLE) }.apply {
            contentDescription = "Mostrar controles flotantes"
            background = surface(cyan, 28)
            setTextColor(navy)
            textSize = 23f
            elevation = dp(10).toFloat()
        }
        root.addView(floatingButton, FrameLayout.LayoutParams(dp(54), dp(54), Gravity.RIGHT or Gravity.BOTTOM).apply {
            rightMargin = dp(16); bottomMargin = dp(70)
        })
        floatingButton.visibility = if (prefs.getBoolean("floating_enabled", true)) View.VISIBLE else View.GONE
        // Measure the actual voice bar, including font scaling, instead of covering its controls.
        val positionFloatingControls = {
            val buttonParams = floatingButton.layoutParams as FrameLayout.LayoutParams
            val buttonMargin = bottom.height + dp(12)
            if (buttonParams.bottomMargin != buttonMargin) {
                buttonParams.bottomMargin = buttonMargin
                floatingButton.layoutParams = buttonParams
            }
            val panelParams = floatingPanel.layoutParams as FrameLayout.LayoutParams
            val panelMargin = buttonMargin + dp(54 + 12)
            floatingActions.measure(
                View.MeasureSpec.makeMeasureSpec(dp(206), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val panelHeight = min(floatingActions.measuredHeight,
                (root.height - panelMargin - dp(12)).coerceAtLeast(1))
            if (panelParams.bottomMargin != panelMargin || panelParams.height != panelHeight) {
                panelParams.bottomMargin = panelMargin
                panelParams.height = panelHeight
                floatingPanel.layoutParams = panelParams
            }
        }
        root.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> positionFloatingControls() }
        bottom.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> positionFloatingControls() }
        setContentView(root)

        return webView
    }

    private fun setFloatingExpanded(open: Boolean) {
        if (open && floatingButton.visibility != View.VISIBLE) return
        floatingPanel.visibility = if (open) View.VISIBLE else View.GONE
        floatingButton.text = if (open) "×" else "☰"
        floatingButton.contentDescription = if (open) "Ocultar controles flotantes" else "Mostrar controles flotantes"
    }

    private fun showMenu() {
        val items = arrayOf("Mostrar/ocultar botón flotante", "Favoritos", "Historial", "Descargas", "Idioma para escuchar", "Estudio automático", "Acerca de ACCESSIA", "Salir", "Guía de comandos", "Elegir voz de respuesta", "Dictado largo por fragmentos", "Idioma de páginas web", "Traducir página", "Sugerir idioma por IP")
        AlertDialog.Builder(this).setTitle("Menú ACCESSIA").setItems(items) { _, n ->
            when (n) {
                0 -> {
                    val newValue = floatingButton.visibility != View.VISIBLE
                    prefs.edit().putBoolean("floating_enabled", newValue).apply()
                    setFloatingExpanded(false)
                    floatingButton.visibility = if (newValue) View.VISIBLE else View.GONE
                }
                1 -> showLinks("favoritos", "Favoritos")
                2 -> showLinks("historial", "Historial")
                3 -> showDownloads()
                4 -> chooseVoiceLanguage()
                5 -> askAutoStudy()
                6 -> showCredits()
                7 -> exitApp()
                8 -> showCommandGuide()
                9 -> chooseResponseVoice()
                10 -> toggleLongDictation()
                11 -> chooseSiteLanguage()
                12 -> translateCurrentPage()
                13 -> askIpRegionPermission()
            }
        }.show()
    }

    private fun stored(key: String): MutableList<String> {
        val out = mutableListOf<String>()
        val arr = try { JSONArray(prefs.getString(key, "[]")) } catch (_: Exception) { JSONArray() }
        for (i in 0 until arr.length()) {
            val value = arr.optString(i)
            if (value.startsWith("https://") && !out.contains(value)) out.add(value)
        }
        return out
    }

    private fun remember(key: String, url: String, limit: Int) {
        if (!url.startsWith("https://")) return
        val list = stored(key)
        list.remove(url)
        list.add(0, url)
        val array = JSONArray()
        list.take(limit).forEach { array.put(it) }
        prefs.edit().putString(key, array.toString()).apply()
    }

    private fun toggleFavorite() {
        if (!currentUrl.startsWith("https://")) { announce("Abre primero una página HTTPS."); return }
        val list = stored("favoritos")
        if (list.contains(currentUrl)) {
            list.remove(currentUrl)
            prefs.edit().putString("favoritos", JSONArray(list).toString()).apply()
            announce("Página eliminada de favoritos.")
        } else {
            remember("favoritos", currentUrl, 80)
            announce("Página guardada en favoritos.")
        }
    }

    private fun showLinks(key: String, heading: String) {
        val links = stored(key)
        if (links.isEmpty()) { announce("No hay elementos en $heading."); return }
        val builder = AlertDialog.Builder(this).setTitle(heading).setItems(links.toTypedArray()) { _, n -> navigate(links[n]) }
        if (key == "historial") {
            builder.setNeutralButton("Borrar historial") { _, _ ->
                AlertDialog.Builder(this).setMessage("¿Eliminar el historial local de ACCESSIA?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Eliminar") { _, _ -> prefs.edit().remove("historial").apply(); announce("Historial eliminado.") }
                    .show()
            }
        }
        builder.setNegativeButton("Cerrar", null).show()
    }

    private fun showDownloads() {
        try { startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)) }
        catch (_: Exception) {
            try {
                startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE); type = "*/*"
                }, 43)
            } catch (_: Exception) { announce("No se encontró un administrador de archivos.") }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 43 || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        if (uri.scheme != "content") { announce("Documento no válido."); return }
        try {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, contentResolver.getType(uri) ?: "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            })
        } catch (_: Exception) { announce("No se pudo abrir el documento con una aplicación compatible.") }
    }

    // Idiomas BCP 47: lista de acceso rápido + entrada libre para otros idiomas.
    private val languageTags = arrayOf(
        "es-BO", "es-MX", "es-ES", "en-US", "en-GB", "pt-BR",
        "pt-PT", "fr-FR", "de-DE", "it-IT", "ar-SA", "hi-IN",
        "zh-CN", "ja-JP", "ko-KR", "ru-RU", "tr-TR", "qu-BO"
    )

    private fun deviceLanguage(): String =
        resources.configuration.locales.get(0)?.toLanguageTag() ?: Locale.getDefault().toLanguageTag()

    private fun effectiveLanguage(key: String): String =
        prefs.getString(key, null)?.takeIf { it.isNotBlank() } ?: deviceLanguage()

    private fun pickLanguage(title: String, key: String, onSelected: (String) -> Unit) {
        val available = (languageTags.toList() + listOf(effectiveLanguage(key))).distinct()
        val options = (available.map {
            val locale = Locale.forLanguageTag(it)
            locale.getDisplayName(Locale.forLanguageTag("es")) + " (" + it + ")"
        } + "Introducir otro idioma (BCP 47)…").toTypedArray()
        AlertDialog.Builder(this).setTitle(title).setItems(options) { _, i ->
            if (i < available.size) onSelected(available[i])
            else {
                val input = EditText(this).apply {
                    hint = "es-BO, en-US, pt-BR, fr-FR…"
                    setSingleLine(true)
                }
                AlertDialog.Builder(this).setTitle("Código de idioma BCP 47")
                    .setView(input)
                    .setPositiveButton("Aplicar") { _, _ ->
                        val tag = input.text.toString().trim()
                        val locale = Locale.forLanguageTag(tag)
                        if (tag.matches(Regex("[A-Za-z]{2,3}(-[A-Za-z0-9]{2,8})*")) &&
                            locale.language.isNotEmpty()) onSelected(locale.toLanguageTag())
                        else updateStatus("Código de idioma no válido.")
                    }.setNegativeButton("Cancelar", null).show()
            }
        }.setNegativeButton("Cancelar", null).show()
    }

    private fun chooseVoiceLanguage() {
        pickLanguage("Idioma del reconocimiento", "voice_language") { tag ->
            applyVoiceLanguage(tag)
        }
    }

    private fun applyVoiceLanguage(tag: String) {
        if (voiceEnabled || longDictationEnabled) stopVoice("Idioma cambiado. Activa otra vez el micrófono.")
        prefs.edit().putString("voice_language", tag).apply()
        configureResponseVoice()
        updateStatus("Reconocimiento: " + tag)
    }

    private fun chooseSiteLanguage() {
        pickLanguage("Idioma preferido de sitios web", "site_language") { tag ->
            prefs.edit().putString("site_language", tag).apply()
            runtime.settings.setLocales(arrayOf(tag))
            updateStatus("Idioma preferido de páginas: " + tag + ". Algunas requieren recargar.")
        }
    }

    private fun translateCurrentPage() {
        if (!currentUrl.startsWith("https://")) {
            updateStatus("Abre primero una página HTTPS para traducir.")
            return
        }
        pickLanguage("Traducir esta página al idioma…", "translate_language") { tag ->
            prefs.edit().putString("translate_language", tag).apply()
            AlertDialog.Builder(this).setTitle("Traducción con servicio externo")
                .setMessage("ACCESSIA enviará la dirección de esta página a Google Traductor. " +
                    "Evita traducir páginas privadas, autenticadas o con datos sensibles. " +
                    "La traducción no funciona en todas las páginas. ¿Continuar?")
                .setPositiveButton("Continuar") { _, _ ->
                    val link = "https://translate.google.com/translate?sl=auto&tl=" +
                        URLEncoder.encode(tag.substringBefore('-'), "UTF-8") + "&u=" +
                        URLEncoder.encode(currentUrl, "UTF-8")
                    navigate(link)
                }.setNegativeButton("Cancelar", null).show()
        }
    }

    // Sin consentimiento se utiliza el idioma de Android; no se consulta la IP.
    private fun offerRegionLanguageSuggestion() {
        if (!prefs.contains("ip_region_consent")) askIpRegionPermission()
    }

    private fun askIpRegionPermission() {
        AlertDialog.Builder(this).setTitle("Idioma sugerido según país")
            .setMessage("Podemos sugerir un idioma usando el país aproximado de tu IP. " +
                "Si aceptas, ipapi.co recibirá tu IP pública en una conexión HTTPS; " +
                "ACCESSIA solo usará el código del país y no almacenará la IP. " +
                "Una VPN, un viaje o un país multilingüe pueden dar una sugerencia incorrecta. " +
                "También puedes usar simplemente el idioma de tu teléfono.")
            .setPositiveButton("Consultar país") { _, _ ->
                prefs.edit().putBoolean("ip_region_consent", true).apply()
                suggestCountryLanguage()
            }
            .setNegativeButton("No consultar IP") { _, _ ->
                prefs.edit().putBoolean("ip_region_consent", false).apply()
                updateStatus("Idioma inicial: " + deviceLanguage())
            }.show()
    }

    private fun languageForCountry(code: String): String? = when (code) {
        "BO" -> "es-BO"
        "MX" -> "es-MX"
        "ES" -> "es-ES"
        "AR", "CL", "CO", "PE", "EC", "UY", "PY", "VE",
        "CR", "PA", "GT", "SV", "HN", "NI", "CU", "DO" -> "es-" + code
        "BR" -> "pt-BR"
        "PT" -> "pt-PT"
        "US" -> "en-US"
        "GB" -> "en-GB"
        "FR" -> "fr-FR"
        "DE" -> "de-DE"
        "IT" -> "it-IT"
        "CN" -> "zh-CN"
        "JP" -> "ja-JP"
        "KR" -> "ko-KR"
        "IN" -> "hi-IN"
        "SA" -> "ar-SA"
        "RU" -> "ru-RU"
        "TR" -> "tr-TR"
        else -> null
    }

    private fun suggestCountryLanguage() {
        updateStatus("Consultando país aproximado mediante IP…")
        Thread {
            val country = try {
                val conn = URL("https://ipapi.co/country/").openConnection() as HttpsURLConnection
                conn.connectTimeout = 3500
                conn.readTimeout = 3500
                try {
                    if (conn.responseCode != 200) null
                    else conn.inputStream.bufferedReader().use {
                        it.readText().trim().uppercase(Locale.ROOT)
                    }
                } finally { conn.disconnect() }
            } catch (_: Exception) { null }
            handler.post {
                if (destroyed) return@post
                val suggested = country?.let { languageForCountry(it) }
                if (suggested == null) {
                    updateStatus("Sin sugerencia por IP. Se conserva el idioma del dispositivo.")
                } else {
                    AlertDialog.Builder(this).setTitle("Sugerencia de idioma")
                        .setMessage("País aproximado: " + country + ". ¿Preferís usar " +
                            suggested + "? Tu idioma personal puede ser diferente.")
                        .setPositiveButton("Seleccionar idioma") { _, _ ->
                            applyVoiceLanguage(suggested)
                            prefs.edit().putString("site_language", suggested).apply()
                            runtime.settings.setLocales(arrayOf(suggested))
                            updateStatus("Idioma seleccionado: " + suggested)
                        }.setNegativeButton("Conservar el actual", null).show()
                }
            }
        }.start()
    }

    private fun showCommandGuide() {
        val categories = arrayOf(
            "🔎 Buscar y abrir sitios", "🧭 Navegar por páginas",
            "⭐ Favoritos, historial y descargas", "🎙 Voz e interfaz",
            "♿ Accesibilidad y vídeos", "🔐 Privacidad y límites"
        )
        val instructions = arrayOf(
            """
            Di: «buscar universidades de Santa Cruz con becas».
            Di: «buscar en google accesibilidad para estudiantes».
            Di: «abrir wikipedia.org».
            También puedes escribir búsquedas largas en la barra superior.

            Consejo: primero activa el micrófono. Espera a que aparezca
            «Escuchando» y habla a un volumen normal, sin gritar.
            """.trimIndent(),
            """
            «atrás» o «volver»
            «adelante»
            «recargar» o «actualizar página»
            «cerrar página» (deja la portada visible)
            «salir» o «cerrar navegador» (cierra ACCESSIA)

            Los comandos de navegación funcionan sin abrir el menú.
            """.trimIndent(),
            """
            «guardar en favoritos» (solo páginas HTTPS)
            «ver favoritos»
            «ver historial»
            «abrir descargas» o «ver descargas»

            El historial se puede borrar desde su ventana, con confirmación.
            Descargas abre el administrador de Android. Esta versión
            todavía no implementa todas las descargas desde GeckoView.
            """.trimIndent(),
            """
            «ayuda», «mostrar comandos» o «guía de comandos»
            «mostrar botones» o «mostrar controles flotantes»
            «ocultar botones»
            «desactivar voz»
            «elegir voz» (escucha una muestra de cada voz disponible)
            «idioma de voz» (idioma del reconocimiento)

            También puedes cambiar estas opciones desde ⋯.
            El reconocimiento requiere permiso de micrófono y un motor Android.
            La voz de respuesta depende de las voces instaladas.
            """.trimIndent(),
            """
            «cursor siguiente»
            «cursor anterior»
            «seleccionar»
            «pausar» o «pausar vídeo»
            «continuar» o «continuar vídeo»
            «activar estudio automático»

            Estas órdenes necesitan una página compatible con la
            extensión de ACCESSIA. No funcionan en la portada vacía.
            La continuidad educativa necesita confirmación; no resuelve
            exámenes ni sortea restricciones.
            """.trimIndent(),
            """
            ACCESSIA escucha solo mientras la app está visible y
            se ha activado el micrófono. No conserva grabaciones de voz
            desde esta función. El reconocimiento puede depender de
            servicios del sistema y de Internet.

            Si una orden falla, prueba desde el botón del menú y anota
            la frase y el mensaje de error. «Ayuda» siempre es local.
            """.trimIndent()
        )
        AlertDialog.Builder(this)
            .setTitle("Guía de voz y funciones · ACCESSIA")
            .setItems(categories) { _, index ->
                AlertDialog.Builder(this)
                    .setTitle(categories[index])
                    .setMessage(instructions[index])
                    .setPositiveButton("Entendido", null)
                    .setNeutralButton("Más categorías") { _, _ -> showCommandGuide() }
                    .show()
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun availableResponseVoices(): List<Voice> =
        try {
            val language = effectiveLanguage("voice_language").substringBefore('-')
            tts?.voices?.filter { it.locale.language == language }
                ?.sortedWith(compareByDescending<Voice> { it.quality }.thenBy { it.name })
                ?: emptyList()
        } catch (_: Exception) { emptyList() }

    private fun configureResponseVoice() {
        val engine = tts ?: return
        engine.setSpeechRate(0.96f)
        engine.setPitch(1.0f)
        val available = availableResponseVoices()
        val saved = prefs.getString("tts_voice_name", null)
        val selected = available.firstOrNull { it.name == saved }
            ?: available.firstOrNull { it.locale.toLanguageTag().equals(effectiveLanguage("voice_language"), true) }
            ?: available.firstOrNull { it.locale.toLanguageTag().equals("es-MX", true) }
            ?: available.firstOrNull()
        if (selected != null) {
            if (engine.setVoice(selected) != TextToSpeech.SUCCESS) {
                engine.language = Locale.forLanguageTag(effectiveLanguage("voice_language"))
            }
        } else {
            engine.language = Locale.forLanguageTag(effectiveLanguage("voice_language"))
        }
    }

    private fun chooseResponseVoice() {
        val engine = tts
        if (!ttsReady || engine == null) {
            updateStatus("La síntesis de voz todavía no está lista.")
            return
        }
        val voices = availableResponseVoices()
        if (voices.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Voces disponibles")
                .setMessage("No hay voces instaladas para el idioma seleccionado. " +
                    "Revisa el motor de texto a voz y descarga voces desde los ajustes de Android.")
                .setPositiveButton("Entendido", null).show()
            return
        }
        val names = voices.map {
            val quality = if (it.quality >= Voice.QUALITY_HIGH) "alta calidad" else "estándar"
            it.locale.toLanguageTag() + " · " + it.name + " · " + quality
        }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Elegir y probar voz de respuesta")
            .setView(TextView(this).apply {
                text = "Escucha las opciones y selecciona la voz femenina que prefieras. " +
                    "Android no informa de forma fiable el género de cada voz."
                setPadding(dp(24), dp(12), dp(24), dp(12))
            })
            .setItems(names) { _, index ->
                val chosen = voices[index]
                if (engine.setVoice(chosen) == TextToSpeech.SUCCESS) {
                    prefs.edit().putString("tts_voice_name", chosen.name).apply()
                    handler.removeCallbacks(restartListening)
                    if (voiceActive) { voiceActive = false; recognizer?.cancel() }
                    speaking = true
                    val preview = when (chosen.locale.language) {
                        "en" -> "Hello, I am ACCESSIA. I can help you browse."
                        "pt" -> "Olá, sou a ACCESSIA. Posso ajudar você a navegar."
                        "fr" -> "Bonjour, je suis ACCESSIA. Je peux vous aider."
                        "de" -> "Hallo, ich bin ACCESSIA. Ich helfe dir beim Surfen."
                        "it" -> "Ciao, sono ACCESSIA. Posso aiutarti a navigare."
                        else -> "Hola, soy ACCESSIA. Estoy lista para ayudarte a navegar."
                    }
                    val spoken = engine.speak(
                        preview, TextToSpeech.QUEUE_FLUSH, null, "accessia-voice-preview"
                    )
                    if (spoken == TextToSpeech.ERROR) {
                        speaking = false
                        updateStatus("No se pudo reproducir esa voz. Prueba otra.")
                        scheduleListen()
                    } else updateStatus("Voz seleccionada: " + chosen.locale.toLanguageTag())
                } else updateStatus("Esta voz no está disponible. Elige otra.")
            }.setNegativeButton("Cancelar", null).show()
    }

    private fun showCredits(spoken: Boolean = false) {
        val credits = "Navegador accesible. Creado por GENAI INDUSTRIES, de Luis Carlos Moreno Jiménez, Santa Cruz de la Sierra, Bolivia. Proyecto gratuito, código MIT. Versión de pruebas: la compatibilidad de voz y las descargas deben validarse en cada dispositivo."
        if (spoken) { announce(credits); return }
        AlertDialog.Builder(this).setTitle("ACCESSIA")
            .setMessage(credits)
            .setPositiveButton("Cerrar", null).show()
    }

    private fun exitApp() { stopVoice("Cerrando ACCESSIA."); finishAndRemoveTask() }
    private fun goBack() { if (canGoBack) session.goBack() else announce("No hay una página anterior.") }
    private fun goForward() { if (canGoForward) session.goForward() else announce("No hay una página siguiente.") }

    private fun setupExtension() {
        runtime.webExtensionController.installBuiltIn("resource://android/assets/accessia_ext/").accept(
            { extension ->
                if (extension == null) {
                    updateStatus("Extensión de accesibilidad no disponible.")
                } else {
                    session.webExtensionController.setMessageDelegate(extension, object : WebExtension.MessageDelegate {
                        override fun onConnect(port: WebExtension.Port) {
                            contentPort = port
                            port.setDelegate(object : WebExtension.PortDelegate {
                                override fun onPortMessage(message: Any, port: WebExtension.Port) {
                                    val obj = message as? JSONObject ?: return
                                    if (obj.optString("type") != "result") return
                                    val response = obj.optJSONObject("payload") ?: return
                                    val spoken = response.optString("text").ifBlank { response.optString("message") }
                                    runOnUiThread { announce(spoken.take(650)) }
                                }
                                override fun onDisconnect(port: WebExtension.Port) {
                                    if (contentPort === port) contentPort = null
                                }
                            })
                        }
                    }, "accessia")
                    updateStatus("Listo · motor de accesibilidad disponible")
                }
            },
            { error -> updateStatus("Extensión: ${error?.message ?: "no disponible"}") }
        )
    }

    private fun sendWebCommand(command: String) {
        val port = contentPort
        if (port == null) { announce("Aún no hay controles accesibles en esta página."); return }
        try { port.postMessage(JSONObject().put("type", "command").put("command", command)) }
        catch (_: Exception) { announce("No se pudo ejecutar el control en esta página.") }
    }

    private fun navigate(value: String) {
        val input = value.trim()
        if (input.isEmpty()) return
        val destination = when {
            input.startsWith("https://", true) -> input
            input.startsWith("http://", true) -> input.replaceFirst(Regex("^http://", RegexOption.IGNORE_CASE), "https://")
            input.contains(' ') || !input.contains('.') -> "https://www.google.com/search?q=" + URLEncoder.encode(input, "UTF-8")
            else -> "https://$input"
        }
        val parsed = Uri.parse(destination)
        if (parsed.scheme != "https" || parsed.host.isNullOrBlank()) { announce("Dirección no válida."); return }
        currentUrl = destination
        contentPort = null
        welcomePanel.visibility = View.GONE
        session.loadUri(destination)
        updateStatus("Abriendo ${parsed.host}…")
    }

    private fun submitAddress() {
        if (longDictationEnabled) finishLongDictation()
        address.clearFocus()
        navigate(address.text.toString())
    }

    private fun askAutoStudy() {
        AlertDialog.Builder(this).setTitle("Continuidad educativa")
            .setMessage("Solo en esta página, ACCESSIA podrá pulsar un botón 'Siguiente' al finalizar el vídeo. No responde exámenes ni elude controles. ¿Activar?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Activar") { _, _ -> sendWebCommand("activar estudio automatico") }.show()
    }

    private fun toggleLongDictation() {
        if (longDictationEnabled) { finishLongDictation(); return }
        longDictationEnabled = true
        lastPartialTranscript = ""
        updateStatus("Dictado largo activo: habla por partes. Pulsa Dictar + al terminar; luego Ir.")
        if (!voiceEnabled) toggleVoice()
    }

    private fun finishLongDictation() {
        longDictationEnabled = false
        stopVoice("Dictado terminado. Revisa la consulta y pulsa Ir.")
    }

    private fun appendDictatedSegment(segment: String) {
        val old = address.text.toString().trimEnd()
        val fragment = segment.trim()
        if (fragment.isEmpty()) return
        address.setText(if (old.isEmpty()) fragment else old + " " + fragment)
        address.setSelection(address.text.length)
        updateStatus("Fragmento añadido. Continúa hablando o termina el dictado.")
    }

    private fun setupSpeech() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            updateStatus("No hay reconocedor de voz instalado.")
            return
        }
        recognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(createSpeechListener())
        }
    }

    private fun createSpeechListener(): RecognitionListener {
        val generation = speechGeneration
        fun acceptsCallbacks() = voiceEnabled && !destroyed && generation == speechGeneration
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                if (!acceptsCallbacks()) return
                voiceActive = true; lastPartialTranscript = ""; updateStatus("🎙 Escuchando · habla normalmente")
            }
            override fun onBeginningOfSpeech() { if (acceptsCallbacks()) updateStatus("🎙 Capturando tu voz…") }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { if (acceptsCallbacks()) updateStatus("Procesando frase…") }
            override fun onError(error: Int) {
                if (!acceptsCallbacks()) return
                voiceActive = false
                if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                    stopVoice("Permiso de micrófono denegado.")
                    return
                }
                consecutiveErrors++
                if (consecutiveErrors >= 8) {
                    stopVoice("Motor de voz detenido tras varios errores. Comprueba la red y el idioma.")
                    return
                }
                val cause = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> if (lastPartialTranscript.isNotBlank()) "Texto parcial: " + lastPartialTranscript.take(100) + ". No confirmado" else "No se comprendió la frase"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó voz"
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Error de red del reconocedor"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Micrófono ocupado"
                    else -> "Error del reconocimiento ($error)"
                }
                updateStatus("$cause · reintentando")
                scheduleListen(min(1000L * consecutiveErrors, 5500L))
            }
            override fun onResults(results: Bundle?) {
                if (!acceptsCallbacks()) return
                voiceActive = false
                consecutiveErrors = 0
                val alternatives = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.filter { it.isNotBlank() }.orEmpty()
                val spoken = if (longDictationEnabled) alternatives.maxByOrNull { it.length }
                    else alternatives.firstOrNull()
                if (spoken.isNullOrBlank()) { scheduleListen(); return }
                if (longDictationEnabled) appendDictatedSegment(spoken)
                else {
                    updateStatus("Escuché: " + spoken.take(160))
                    handleSpoken(spoken)
                }
                if (!speaking) scheduleListen()
            }
            override fun onPartialResults(partialResults: Bundle?) {
                if (!acceptsCallbacks()) return
                val phrase = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!phrase.isNullOrBlank()) { lastPartialTranscript = phrase; updateStatus("🎙 " + phrase.take(140) + "…") }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun setupTts() {
        tts = TextToSpeech(this) { code ->
            if (code == TextToSpeech.SUCCESS) {
                ttsReady = true
                configureResponseVoice()
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { handler.post { speaking = true } }
                    override fun onDone(utteranceId: String?) { handler.post { speaking = false; scheduleListen() } }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) { handler.post { speaking = false; scheduleListen() } }
                })
            }
        }
    }

    private fun toggleVoice() {
        if (voiceEnabled) { stopVoice("Reconocimiento desactivado."); return }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 42); return
        }
        if (recognizer == null) setupSpeech()
        if (recognizer == null) { stopVoice("No hay motor de voz. Revisa servicios de voz de Android."); return }
        voiceEnabled = true
        consecutiveErrors = 0
        updateVoiceButtons()
        listenOnce()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 42 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) toggleVoice()
        else if (requestCode == 42) stopVoice("Permiso de micrófono no concedido.")
    }

    private fun listenOnce() {
        if (!voiceEnabled || voiceActive || speaking || destroyed) return
        voiceActive = true
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, prefs.getString("voice_language", null) ?: resources.configuration.locales.get(0)?.toLanguageTag() ?: "es-BO")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            // Petición orientativa: algunos servicios ignoran los tiempos de silencio.
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2100L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1600L)
            if (android.os.Build.VERSION.SDK_INT >= 34) {
                putExtra(RecognizerIntent.EXTRA_ENABLE_LANGUAGE_DETECTION, true)
            }
        }
        try { recognizer?.startListening(intent) }
        catch (_: Exception) {
            voiceActive = false
            consecutiveErrors++
            if (consecutiveErrors >= 8) stopVoice("Motor de voz detenido tras varios errores. Comprueba la red y el idioma.")
            else { updateStatus("No fue posible iniciar el micrófono."); scheduleListen(2500) }
        }
    }

    private fun scheduleListen(delay: Long = 1050L) {
        handler.removeCallbacks(restartListening)
        if (voiceEnabled && !destroyed) handler.postDelayed(restartListening, delay)
    }

    private fun stopVoice(message: String) {
        voiceEnabled = false
        speechGeneration++
        voiceActive = false
        speaking = false
        longDictationEnabled = false
        lastPartialTranscript = ""
        handler.removeCallbacks(restartListening)
        updateVoiceButtons()
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
        tts?.stop()
        announce(message)
    }

    private fun updateVoiceButtons() {
        voiceButton.text = if (voiceEnabled) "🎙 Desactivar voz" else "🎙 Activar voz"
        voiceButton.contentDescription = voiceButton.text
        welcomeVoiceButton.text = if (voiceEnabled) "🎙 Desactivar micrófono" else "🎙 Activar micrófono"
        welcomeVoiceButton.contentDescription = welcomeVoiceButton.text
    }

    // Strip only command prefixes; keep the original query/domain intact.
    private fun stripVoicePrefixes(raw: String): String {
        val prefix = Regex(
            "^(?:(?:oye[\\s\\p{Punct}¿¡]+)?accessia|por[\\s\\p{Punct}¿¡]+favor|quiero|puedes|podr[ií]as|me[\\s\\p{Punct}¿¡]+puedes)(?:[\\s\\p{Punct}¿¡]+|$)",
            RegexOption.IGNORE_CASE
        )
        var result = raw.trim()
        while (true) {
            val next = result.replaceFirst(prefix, "").trim()
            if (next == result) return result
            result = next
        }
    }

    private fun normalizeVoiceCommand(raw: String): String = Normalizer.normalize(
        stripVoicePrefixes(raw).lowercase(Locale.ROOT), Normalizer.Form.NFD
    ).replace(Regex("\\p{M}+"), "")
        .replace(Regex("[^a-z0-9 ]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun handleSpoken(raw: String) {
        val command = normalizeVoiceCommand(raw)
        if (autoConsentPending) {
            autoConsentPending = false
            if (command in setOf("confirmar", "si confirmar", "si")) {
                sendWebCommand("activar estudio automatico")
            } else announce("Continuidad no autorizada.")
            return
        }
        // Prioridad para las funciones locales: deben funcionar incluso en about:blank.
        when {
            command in setOf("ayuda", "comandos", "ver comandos", "mostrar comandos",
                "abrir ayuda", "abrir comandos", "guia", "guia de comandos",
                "lista de comandos", "que puedo decir", "que comandos puedo usar",
                "informacion", "informacion de comandos", "manual",
                "dime los comandos", "cuales son los comandos", "abrir la guia",
                "mostrar la guia", "abrir el manual", "lista de funciones") -> showCommandGuide()

            command in setOf("salir", "salir de accessia", "cerrar accessia",
                "cerrar aplicacion", "cerrar navegador", "terminar navegador",
                "cerrar el navegador", "salir del navegador") -> exitApp()

            command in setOf("desactivar voz", "apagar voz", "detener escucha",
                "apagar microfono", "desactivar microfono") ->
                stopVoice("Reconocimiento de voz desactivado.")

            command in setOf("elegir voz", "cambiar voz", "voz femenina",
                "voz de respuesta", "seleccionar voz", "probar voces") -> chooseResponseVoice()

            command in setOf("idioma de voz", "cambiar idioma",
                "idioma del reconocimiento") -> chooseVoiceLanguage()

            command in setOf("acerca de", "creditos") -> showCredits(spoken = true)

            command in setOf("cerrar pagina", "cerrar pestana") -> {
                contentPort = null
                currentUrl = "about:blank"
                canGoBack = false
                canGoForward = false
                session.purgeHistory()
                session.load(GeckoSession.Loader().uri("about:blank").flags(GeckoSession.LOAD_FLAGS_REPLACE_HISTORY))
                address.setText("")
                address.clearFocus()
                welcomePanel.visibility = View.VISIBLE
                updateStatus("Página cerrada. La guía está disponible.")
            }

            command in setOf("volver", "atras", "volver atras", "ir atras",
                "pagina anterior", "retroceder") -> goBack()

            command in setOf("adelante", "ir adelante", "pagina siguiente", "avanzar") -> goForward()

            command in setOf("recargar", "recargar pagina", "actualizar pagina",
                "actualizar", "refrescar pagina") -> session.reload()

            command in setOf("historial", "mostrar historial", "ver historial",
                "abrir historial", "abrir el historial", "muestrame el historial") ->
                showLinks("historial", "Historial")

            command in setOf("favoritos", "mostrar favoritos", "ver favoritos",
                "abrir favoritos", "muestrame los favoritos") ->
                showLinks("favoritos", "Favoritos")

            command in setOf("guardar favorito", "guardar en favoritos",
                "agregar a favoritos", "anadir a favoritos",
                "marcar favorito", "guardar esta pagina") -> toggleFavorite()

            command in setOf("descargas", "mostrar descargas", "ver descargas",
                "abrir descargas", "abrir las descargas", "ver las descargas",
                "mostrar las descargas", "muestrame las descargas",
                "gestor de descargas", "abrir el gestor de descargas",
                "acceder a descargas", "ir a descargas") -> showDownloads()

            command in setOf("mostrar botones", "mostrar controles",
                "mostrar controles flotantes", "abrir botones",
                "abrir controles flotantes", "activar botones flotantes") -> {
                floatingButton.visibility = View.VISIBLE
                prefs.edit().putBoolean("floating_enabled", true).apply()
                setFloatingExpanded(true)
            }

            command in setOf("ocultar botones", "ocultar controles",
                "ocultar controles flotantes", "cerrar botones",
                "cerrar controles flotantes") -> setFloatingExpanded(false)

            command in setOf("activar estudio automatico", "activar continuidad") -> {
                autoConsentPending = true
                announce("¿Activar continuidad educativa en esta página? Di confirmar o cancelar.")
            }

            command in setOf("pausar video", "pausa el video") -> sendWebCommand("pausar")
            command in setOf("continuar video", "reanudar video") -> sendWebCommand("continuar")

            command.startsWith("buscar ") || command.startsWith("busca ") ||
                command.startsWith("buscar en google ") || command.startsWith("buscame ") -> {
                val original = stripVoicePrefixes(raw)
                val match = Regex(
                    "^(?:buscar(?:\\s+en\\s+google)?|busca|b[uú]scame)\\s+",
                    RegexOption.IGNORE_CASE
                ).find(original)
                if (match != null) navigate(original.substring(match.range.last + 1))
                else announce("Dime qué quieres buscar.")
            }

            command.startsWith("abrir ") || command.startsWith("abre ") -> {
                // Conservar puntos, guiones y acentos en dominios como wikipedia.org.
                val term = stripVoicePrefixes(raw).replaceFirst(
                    Regex("^(?:abrir|abre)\\s+", RegexOption.IGNORE_CASE), ""
                ).trim()
                if (term.isNotEmpty()) navigate(term)
                else announce("Dime qué página quieres abrir.")
            }

            contentPort != null -> sendWebCommand(command)

            else -> announce("No reconocí esa función. Di ayuda para conocer los comandos disponibles.")
        }
    }

    private fun updateStatus(message: String) {
        if (::status.isInitialized && !destroyed) {
            status.text = message
            status.contentDescription = message
        }
    }

    private fun announce(message: String) {
        if (message.isBlank() || destroyed) return
        updateStatus(message)
        status.contentDescription = message
        status.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        if (voiceEnabled && ttsReady && tts != null) {
            handler.removeCallbacks(restartListening)
            // Evita que la escucha y la síntesis compitan simultáneamente por el audio.
            if (voiceActive) {
                voiceActive = false
                recognizer?.cancel()
            }
            speaking = true
            try {
                val result = tts?.speak(message.take(280), TextToSpeech.QUEUE_FLUSH, null, "accessia-message")
                if (result == TextToSpeech.ERROR) { speaking = false; scheduleListen() }
            } catch (_: Exception) { speaking = false; scheduleListen() }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { if (canGoBack) goBack() else super.onBackPressed() }

    override fun onStop() {
        super.onStop()
        if (voiceEnabled || longDictationEnabled) stopVoice("Voz detenida en segundo plano.")
    }
    override fun onDestroy() {
        destroyed = true
        voiceEnabled = false
        handler.removeCallbacksAndMessages(null)
        recognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        contentPort = null
        if (::session.isInitialized) session.close()
        super.onDestroy()
    }
}
