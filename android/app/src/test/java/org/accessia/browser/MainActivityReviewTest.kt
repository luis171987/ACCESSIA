package org.accessia.browser

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.net.Uri
import android.speech.SpeechRecognizer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.mozilla.geckoview.GeckoSession
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MainActivityReviewTest {
    private lateinit var activity: MainActivity

    @Before fun setup() {
        activity = Robolectric.buildActivity(MainActivity::class.java).get()
        invoke("buildInterface")
        field("session", mock(GeckoSession::class.java))
    }

    private fun field(name: String): Any? = MainActivity::class.java.getDeclaredField(name).run {
        isAccessible = true; get(activity)
    }
    private fun field(name: String, value: Any?) = MainActivity::class.java.getDeclaredField(name).run {
        isAccessible = true; set(activity, value)
    }
    private fun invoke(name: String, vararg args: Any?): Any? {
        val method = MainActivity::class.java.declaredMethods.first {
            it.name == name && it.parameterCount == args.size
        }
        method.isAccessible = true
        return method.invoke(activity, *args)
    }
    private fun status() = field("status") as TextView
    private fun address() = field("address") as EditText
    private fun button(name: String) = field(name) as Button

    @Test fun stackedPrefixesDoNotPreventLocalCommands() {
        val cases = mapOf(
            "oye ACCESSIA por favor buscar bibliotecas" to "buscar bibliotecas",
            "por favor ACCESSIA buscar wikipedia" to "buscar wikipedia",
            "me puedes por favor abrir wikipedia.org" to "abrir wikipedia org",
            "ACCESSIA: por favor, abrir wikipedia.org" to "abrir wikipedia org"
        )
        for ((phrase, expected) in cases) assertEquals(expected, invoke("normalizeVoiceCommand", phrase))
    }

    @Test fun politeOpenPreservesDomainPunctuation() {
        for (phrase in listOf("Podrías abrir wikipedia.org", "me puedes abrir wikipedia.org",
            "oye ACCESSIA por favor abre wikipedia.org", "ACCESSIA: por favor, abrir wikipedia.org")) {
            field("currentUrl", "about:blank")
            invoke("handleSpoken", phrase)
            assertEquals("https://wikipedia.org", field("currentUrl"))
        }
    }

    @Test fun googleSearchPreservesCaseAccentsAndAllPrefixes() {
        invoke("handleSpoken", "Por favor ACCESSIA buscar en Google Educación Inclusiva")
        assertEquals("https://www.google.com/search?q=Educaci%C3%B3n+Inclusiva", field("currentUrl"))
    }

    @Test fun bothMicrophonesDescribeTheActualActionAfterToggling() {
        shadowOf(activity.application).grantPermissions(android.Manifest.permission.RECORD_AUDIO)
        field("recognizer", mock(SpeechRecognizer::class.java))
        invoke("toggleVoice")
        assertEquals("🎙 Desactivar voz", button("voiceButton").contentDescription)
        assertEquals("🎙 Desactivar micrófono", button("welcomeVoiceButton").contentDescription)
        invoke("toggleVoice")
        assertEquals("🎙 Activar voz", button("voiceButton").contentDescription)
        assertEquals("🎙 Activar micrófono", button("welcomeVoiceButton").contentDescription)
    }

    @Test fun stoppingVoiceClearsDictationAndAnnouncesTheReason() {
        field("longDictationEnabled", true)
        field("lastPartialTranscript", "texto pendiente")
        invoke("stopVoice", "Motor detenido tras errores.")
        assertEquals(false, field("longDictationEnabled"))
        assertEquals("", field("lastPartialTranscript"))
        assertEquals("Motor detenido tras errores.", status().contentDescription)
    }

    @Test fun keyboardSubmissionEndsDictationAndReleasesAddressFocus() {
        address().setText("wikipedia.org")
        address().requestFocus()
        field("voiceEnabled", true)
        field("longDictationEnabled", true)
        address().onEditorAction(EditorInfo.IME_ACTION_GO)
        assertEquals(false, field("longDictationEnabled"))
        assertEquals(false, field("voiceEnabled"))
        assertFalse(address().hasFocus())
        assertEquals("https://wikipedia.org", field("currentUrl"))
    }

    @Test fun startExceptionsStopAtTheSameBoundedThresholdAsCallbacks() {
        val recognizer = mock(SpeechRecognizer::class.java)
        doThrow(IllegalStateException("service gone")).`when`(recognizer).startListening(any(Intent::class.java))
        field("recognizer", recognizer)
        field("voiceEnabled", true)
        repeat(8) { invoke("listenOnce") }
        assertEquals(false, field("voiceEnabled"))
        assertTrue(status().text.toString().contains("detenido"))
        invoke("listenOnce")
        verify(recognizer, times(8)).startListening(any(Intent::class.java))
    }

    @Test fun creditsInvokedByVoiceAreAudibleWithoutATrappingModal() {
        invoke("handleSpoken", "acerca de")
        assertTrue(status().contentDescription.toString().contains("GENAI INDUSTRIES"))
        assertNull(ShadowAlertDialog.getLatestAlertDialog())
    }

    @Test fun voiceChoiceListRemainsVisibleAndSelectable() {
        val tts = mock(TextToSpeech::class.java)
        val voice = Voice("es-test", Locale.forLanguageTag("es-BO"), Voice.QUALITY_HIGH,
            Voice.LATENCY_NORMAL, false, emptySet())
        `when`(tts.voices).thenReturn(setOf(voice))
        field("tts", tts)
        field("ttsReady", true)
        activity.getSharedPreferences("accessia_v02", 0).edit().putString("voice_language", "es-BO").commit()
        invoke("chooseResponseVoice")
        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        assertNotNull(dialog.listView)
        assertEquals(1, dialog.listView.adapter.count)
        assertTrue("An AlertDialog message hides the voice list", dialog.listView.isShown)
    }

    @Test fun hidingExpandedFloatingControlResetsItsLabel() {
        invoke("setFloatingExpanded", true)
        invoke("showMenu")
        shadowOf(ShadowAlertDialog.getLatestAlertDialog()).clickOnItem(0)
        assertEquals("☰", button("floatingButton").text.toString())
        assertEquals("Mostrar controles flotantes", button("floatingButton").contentDescription)
    }

    @Test fun floatingControlsStayAboveVoiceAndScrollOnShortWindows() {
        val root = activity.findViewById<FrameLayout>(android.R.id.content).getChildAt(0)
        fun layout() {
            root.measure(View.MeasureSpec.makeMeasureSpec(640, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY))
            root.layout(0, 0, 640, 320)
        }
        layout()
        val floating = Rect(); val voice = Rect()
        button("floatingButton").getGlobalVisibleRect(floating)
        button("voiceButton").getGlobalVisibleRect(voice)
        assertFalse("Floating control intercepts microphone taps", Rect.intersects(floating, voice))
        invoke("setFloatingExpanded", true)
        layout()
        val panel = field("floatingPanel") as View
        assertTrue("Actions must be scrollable", panel is ScrollView)
        assertTrue("Panel clipped above the window", panel.top >= 0)
        assertTrue("Panel overlaps floating button", panel.bottom <= button("floatingButton").top)
    }

    @Test fun iconControlsHaveSemanticDescriptions() {
        val views = mutableListOf<View>()
        fun collect(view: View) {
            views.add(view)
            if (view is android.view.ViewGroup) for (i in 0 until view.childCount) collect(view.getChildAt(i))
        }
        collect(activity.findViewById(android.R.id.content))
        assertEquals("Abrir menú ACCESSIA", views.filterIsInstance<Button>().first { it.text == "⋯" }.contentDescription)
        assertEquals("Recargar página", views.filterIsInstance<Button>().first { it.text == "⟳" }.contentDescription)
    }

    @Test fun selectedDownloadOpensWithTemporaryReadPermission() {
        val uri = Uri.parse("content://downloads/document/42")
        invoke("onActivityResult", 43, Activity.RESULT_OK, Intent().setData(uri))
        val opened = shadowOf(activity).nextStartedActivity
        assertNotNull(opened)
        assertEquals(Intent.ACTION_VIEW, opened.action)
        assertEquals(uri, opened.data)
        assertTrue(opened.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test fun cancelledDocumentPickerDoesNotOpenAnything() {
        invoke("onActivityResult", 43, Activity.RESULT_CANCELED, null)
        assertNull(shadowOf(activity).nextStartedActivity)
    }

    @Test fun closedPageCannotBeReopenedWithBackOrForward() {
        field("canGoBack", true)
        field("canGoForward", true)
        invoke("handleSpoken", "cerrar página")
        assertEquals(false, field("canGoBack"))
        assertEquals(false, field("canGoForward"))
        verify(field("session") as GeckoSession).purgeHistory()
        verify(field("session") as GeckoSession).load(
            GeckoSession.Loader().uri("about:blank").flags(GeckoSession.LOAD_FLAGS_REPLACE_HISTORY))
    }

    @Test fun acceptingLanguageReconfiguresTtsAndStopsActiveDictation() {
        val tts = mock(TextToSpeech::class.java)
        `when`(tts.voices).thenReturn(emptySet())
        field("tts", tts)
        field("voiceEnabled", true)
        field("longDictationEnabled", true)
        invoke("applyVoiceLanguage", "pt-BR")
        assertEquals("pt-BR", activity.getSharedPreferences("accessia_v02", 0).getString("voice_language", null))
        verify(tts).setLanguage(Locale.forLanguageTag("pt-BR"))
        assertEquals(false, field("voiceEnabled"))
        assertEquals(false, field("longDictationEnabled"))
    }

    @Test fun lateRecognitionResultsAfterStopCannotNavigateOrChangeText() {
        val listener = invoke("createSpeechListener") as RecognitionListener
        address().setText("texto conservado")
        invoke("stopVoice", "Detenido")
        val results = Bundle().apply {
            putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, arrayListOf("abrir wikipedia.org"))
        }
        listener.onResults(results)
        listener.onPartialResults(results)
        listener.onReadyForSpeech(null)
        assertEquals("about:blank", field("currentUrl"))
        assertEquals("texto conservado", address().text.toString())
        assertEquals("Detenido", status().text.toString())
        assertEquals(false, field("voiceActive"))
    }

    @Test fun callbacksFromStoppedRecognizerCannotAffectRestartedSession() {
        val oldListener = invoke("createSpeechListener") as RecognitionListener
        invoke("stopVoice", "Detenido")
        field("voiceEnabled", true)
        field("voiceActive", true)
        address().setText("consulta nueva")
        val results = Bundle().apply {
            putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, arrayListOf("abrir wikipedia.org"))
        }
        oldListener.onResults(results)
        oldListener.onPartialResults(results)
        oldListener.onError(SpeechRecognizer.ERROR_NETWORK)
        assertEquals("about:blank", field("currentUrl"))
        assertEquals("consulta nueva", address().text.toString())
        assertEquals(true, field("voiceActive"))
        assertEquals(0, field("consecutiveErrors"))
    }

    @Test fun listeningStatusAfterRestartIsAlsoCurrentForTalkBack() {
        invoke("stopVoice", "Reconocimiento desactivado.")
        field("voiceEnabled", true)
        val currentListener = invoke("createSpeechListener") as RecognitionListener
        currentListener.onReadyForSpeech(null)
        assertEquals("🎙 Escuchando · habla normalmente", status().contentDescription)
    }

    @Test fun stoppingDisposesTheOldRecognizerCallbackBoundary() {
        val oldRecognizer = mock(SpeechRecognizer::class.java)
        field("recognizer", oldRecognizer)
        invoke("stopVoice", "Detenido")
        verify(oldRecognizer).destroy()
        assertNull(field("recognizer"))
    }

    @Test fun floatingActionLabelsFitAtDoubleFontScale() {
        val configuration = Configuration(activity.resources.configuration).apply { fontScale = 2f }
        @Suppress("DEPRECATION")
        activity.resources.updateConfiguration(configuration, activity.resources.displayMetrics)
        invoke("buildInterface")
        invoke("setFloatingExpanded", true)
        val root = activity.findViewById<FrameLayout>(android.R.id.content).getChildAt(0)
        repeat(2) {
            root.measure(View.MeasureSpec.makeMeasureSpec(640, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY))
            root.layout(0, 0, 640, 320)
        }
        val panel = field("floatingPanel") as ScrollView
        val actions = panel.getChildAt(0) as LinearLayout
        for (i in 0 until actions.childCount) {
            val action = actions.getChildAt(i) as Button
            assertTrue("Label clipped: ${action.text}",
                action.layout.height + action.compoundPaddingTop + action.compoundPaddingBottom <= action.height)
        }
    }

    @Test fun voiceInvokedHelpSpeaksCommandsWithoutOpeningASilentModal() {
        val tts = mock(TextToSpeech::class.java)
        field("tts", tts)
        field("ttsReady", true)
        field("voiceEnabled", true)
        invoke("handleSpoken", "ayuda")
        assertNull(ShadowAlertDialog.getLatestAlertDialog())
        val spoken = org.mockito.ArgumentCaptor.forClass(String::class.java)
        verify(tts).speak(spoken.capture(), eq(TextToSpeech.QUEUE_FLUSH), isNull(), eq("accessia-message"))
        assertTrue(spoken.value.contains("buscar becas"))
        assertTrue(spoken.value.contains("salir de ACCESSIA"))
        assertTrue("Help must fit the utterance limit", spoken.value.length <= 280)
    }

    @Test fun newDictationAfterBrowsingSearchesWithoutTheDisplayedUrl() {
        shadowOf(activity.application).grantPermissions(android.Manifest.permission.RECORD_AUDIO)
        field("recognizer", mock(SpeechRecognizer::class.java))
        field("currentUrl", "https://www.wikipedia.org/")
        address().setText("https://www.wikipedia.org/")
        invoke("toggleLongDictation")
        invoke("appendDictatedSegment", "becas universitarias")
        assertEquals("becas universitarias", address().text.toString())
        invoke("submitAddress")
        assertEquals("https://www.google.com/search?q=becas+universitarias", field("currentUrl"))
    }

    @Test fun newDictationPreservesDeliberatelyEnteredQueryFragments() {
        shadowOf(activity.application).grantPermissions(android.Manifest.permission.RECORD_AUDIO)
        field("recognizer", mock(SpeechRecognizer::class.java))
        field("currentUrl", "https://www.wikipedia.org/")
        address().setText("becas para")
        invoke("toggleLongDictation")
        invoke("appendDictatedSegment", "estudiantes")
        assertEquals("becas para estudiantes", address().text.toString())
    }
}
