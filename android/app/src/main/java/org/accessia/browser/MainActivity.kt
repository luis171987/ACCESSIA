package org.accessia.browser

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.Gravity
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.WebExtension
import java.net.URLEncoder
import java.util.Locale

/** PROTOTYPE: foreground-only speech recognizer; no persistent microphone service. */
class MainActivity : Activity() {
    private lateinit var session: GeckoSession
    private lateinit var runtime: GeckoRuntime
    private lateinit var address: EditText
    private lateinit var status: TextView
    private lateinit var voiceButton: Button
    private val handler = Handler(Looper.getMainLooper())
    private var contentPort: WebExtension.Port? = null
    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var voiceEnabled = false
    private var autoConsentPending = false
    private var awaitingSpeech = false
    private var speaking = false
    private var consecutiveErrors = 0
    private var destroyed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(12, 12, 12, 12)
        }
        address = EditText(this).apply {
            hint = "Dirección web o búsqueda"
            contentDescription = "Dirección web o término de búsqueda"
            setSingleLine(true)
            setText("https://www.wikipedia.org/")
        }
        column.addView(address, LinearLayout.LayoutParams(-1, -2))
        val toolbar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        fun addButton(label: String, onClick: () -> Unit) {
            val b = Button(this).apply {
                text = label
                contentDescription = label
                setOnClickListener { onClick() }
            }
            toolbar.addView(b, LinearLayout.LayoutParams(0, -2, 1f))
        }
        addButton("Ir") { navigate(address.text.toString()) }
        addButton("Atrás") { session.goBack() }
        addButton("Pausar") { sendWebCommand("pausar") }
        addButton("Seguir") { sendWebCommand("continuar") }
        column.addView(toolbar)
        val actions = LinearLayout(this)
        addAction(actions, "Siguiente control") { sendWebCommand("cursor siguiente") }
        addAction(actions, "Seleccionar") { sendWebCommand("seleccionar") }
        addAction(actions, "Estudio automático") { askAutoStudy() }
        column.addView(actions)
        voiceButton = Button(this).apply {
            text = "Activar voz"
            contentDescription = "Activar o desactivar reconocimiento de voz"
            setOnClickListener { toggleVoice() }
        }
        column.addView(voiceButton)
        status = TextView(this).apply {
            text = "ACCESSIA: listo. La automatización de lecciones está desactivada."
            textSize = 17f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER_VERTICAL
            contentDescription = text
        }
        column.addView(status)
        val webView = GeckoView(this).apply { contentDescription = "Contenido del navegador" }
        column.addView(webView, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(column)

        runtime = GeckoRuntime.create(this)
        session = GeckoSession()
        session.setContentDelegate(object : GeckoSession.ContentDelegate {})
        session.open(runtime)
        webView.setSession(session)
        setupExtension()
        session.loadUri("https://www.wikipedia.org/")
        setupSpeech()
        setupTts()
    }

    private fun addAction(row: LinearLayout, label: String, task: () -> Unit) {
        val button = Button(this).apply { text = label; contentDescription = label; setOnClickListener { task() } }
        row.addView(button, LinearLayout.LayoutParams(0, -2, 1f))
    }

    private fun setupExtension() {
        runtime.webExtensionController.installBuiltIn("resource://android/assets/accessia_ext/").accept(
            { extension ->
                session.webExtensionController.setMessageDelegate(extension, object : WebExtension.MessageDelegate {
                    override fun onConnect(port: WebExtension.Port) {
                        contentPort = port
                        port.setDelegate(object : WebExtension.PortDelegate {
                            override fun onPortMessage(message: Any, port: WebExtension.Port) {
                                val obj = message as? JSONObject ?: return
                                if (obj.optString("type") != "result") return
                                val response = obj.optJSONObject("payload") ?: return
                                val spoken = if (response.optString("text").isNotBlank())
                                    response.optString("text").take(1800) else response.optString("message")
                                runOnUiThread { announce(spoken) }
                            }
                            override fun onDisconnect(port: WebExtension.Port) {
                                if (contentPort === port) contentPort = null
                            }
                        })
                    }
                }, "accessia")
                announce("Extensión de accesibilidad instalada.")
            },
            { error -> announce("Extensión no disponible: ${error.message}") }
        )
    }

    private fun sendWebCommand(command: String) {
        val port = contentPort
        if (port == null) { announce("El contenido aún no está disponible o la página no admite este control."); return }
        try { port.postMessage(JSONObject().put("type", "command").put("command", command)) }
        catch (_: Exception) { announce("No se pudo enviar el comando a esta página.") }
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
        val uri = android.net.Uri.parse(destination)
        if (uri.scheme != "https" || uri.host.isNullOrBlank()) { announce("Dirección no válida."); return }
        // New document => its JavaScript engine starts with auto study disabled.
        contentPort = null
        session.loadUri(destination)
        announce("Abriendo: ${uri.host}")
    }

    private fun askAutoStudy() {
        AlertDialog.Builder(this)
            .setTitle("Continuidad educativa")
            .setMessage("Solo en esta página: al finalizar un video HTML5, ACCESSIA podrá pulsar un único botón 'Siguiente'. No responde evaluaciones ni evita controles del sitio. ¿Activar?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Activar") { _, _ -> sendWebCommand("activar estudio automatico") }
            .show()
    }

    private fun setupSpeech() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            announce("No hay reconocedor de voz instalado. Puedes usar botones o Voice Access del sistema.")
            return
        }
        recognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { awaitingSpeech = true; status.text = "🎙️ Escuchando comandos..." }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { awaitingSpeech = false }
                override fun onError(error: Int) {
                    awaitingSpeech = false
                    if (!voiceEnabled || destroyed) return
                    consecutiveErrors++
                    if (consecutiveErrors >= 3) { stopVoice("La escucha se detuvo tras varios errores."); return }
                    handler.postDelayed({ if (voiceEnabled && !speaking) listenOnce() }, 1300)
                }
                override fun onResults(results: Bundle?) {
                    awaitingSpeech = false
                    consecutiveErrors = 0
                    val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (spoken.isNullOrBlank()) { scheduleListen(); return }
                    handleSpoken(spoken)
                    if (!speaking) scheduleListen()
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    private fun setupTts() {
        tts = TextToSpeech(this) { code ->
            if (code == TextToSpeech.SUCCESS) {
                tts?.language = Locale.forLanguageTag("es-BO")
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { speaking = true }
                    override fun onDone(utteranceId: String?) { speaking = false; scheduleListen() }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) { speaking = false; scheduleListen() }
                })
            }
        }
    }

    private fun toggleVoice() {
        if (voiceEnabled) { stopVoice("Reconocimiento desactivado."); return }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 42)
            return
        }
        if (recognizer == null) { announce("No hay motor de voz disponible."); return }
        voiceEnabled = true
        consecutiveErrors = 0
        voiceButton.text = "Desactivar voz"
        listenOnce()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 42 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) toggleVoice()
        else if (requestCode == 42) announce("Sin permiso de micrófono; utiliza los botones.")
    }

    private fun listenOnce() {
        if (!voiceEnabled || awaitingSpeech || speaking || destroyed) return
        awaitingSpeech = true
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-BO")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        try { recognizer?.startListening(intent) } catch (_: Exception) { stopVoice("No se pudo activar el micrófono.") }
    }

    private fun scheduleListen() { handler.postDelayed({ if (voiceEnabled && !speaking) listenOnce() }, 850) }

    private fun stopVoice(message: String) {
        voiceEnabled = false
        awaitingSpeech = false
        voiceButton.text = "Activar voz"
        recognizer?.cancel()
        announce(message)
    }

    private fun handleSpoken(raw: String) {
        val command = raw.lowercase(Locale.ROOT).trim()
        if (autoConsentPending) {
            autoConsentPending = false
            if (command == "confirmar") sendWebCommand("activar estudio automatico")
            else announce("Continuidad no autorizada.")
            return
        }
        when {
            command == "cerrar página" || command == "cerrar pestaña" -> {
                contentPort = null
                session.loadUri("about:blank")
                announce("Página cerrada.")
            }
            command == "cerrar aplicación" || command == "cerrar navegador" -> {
                stopVoice("Cerrando ACCESSIA."); finish()
            }
            command == "volver" || command == "atrás" || command == "volver atrás" -> session.goBack()
            command == "adelante" -> session.goForward()
            command.startsWith("abrir ") -> navigate(raw.substringAfter(' '))
            command.startsWith("buscar ") -> navigate(raw.substringAfter(' '))
            // Permite confirmar completamente mediante voz (sin tocar pantalla).
            command == "activar estudio automático" || command == "activar continuidad" -> {
                autoConsentPending = true
                announce("¿Deseas continuar a la siguiente lección cuando termine el video? Di confirmar o cancelar.")
            }
            else -> sendWebCommand(command)
        }
    }

    private fun announce(message: String) {
        if (message.isBlank()) return
        status.text = message
        status.contentDescription = message
        status.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        if (voiceEnabled && tts != null) {
            speaking = true
            try { tts?.speak(message.take(450), TextToSpeech.QUEUE_FLUSH, null, "accessia-message") }
            catch (_: Exception) { speaking = false; scheduleListen() }
        }
    }

    override fun onStop() {
        super.onStop()
        if (voiceEnabled) stopVoice("Voz detenida al salir de ACCESSIA.")
    }
    override fun onDestroy() {
        destroyed = true
        voiceEnabled = false
        handler.removeCallbacksAndMessages(null)
        recognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        contentPort = null
        session.close()
        super.onDestroy()
    }
}
