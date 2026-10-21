using System.Globalization;
using System.Speech.Recognition;
using System.Speech.Synthesis;
using System.Text.Json;
using Microsoft.Web.WebView2.Core;
using Microsoft.Web.WebView2.WinForms;

namespace Accessia.Windows;

internal static class Program
{
    [STAThread]
    private static void Main()
    {
        ApplicationConfiguration.Initialize();
        Application.Run(new MainWindow());
    }
}

public sealed class MainWindow : Form
{
    private readonly WebView2 browser = new() { Dock = DockStyle.Fill, AccessibleName = "Contenido del navegador" };
    private readonly TextBox address = new() { Dock = DockStyle.Fill, AccessibleName = "Dirección o búsqueda" };
    private readonly Label status = new() { Dock = DockStyle.Fill, Text = "ACCESSIA: preparando navegador.", AutoSize = false, AccessibleName = "Estado" };
    private readonly Button voiceButton = new() { Text = "Activar voz", AutoSize = true, AccessibleName = "Activar o desactivar control por voz" };
    private SpeechRecognitionEngine? recognition;
    private SpeechSynthesizer? speech;
    private bool voiceOn;
    private bool autoConsentPending;
    private bool browserReady;
    private bool isSpeaking;
    private bool disposed;

    public MainWindow()
    {
        Text = "ACCESSIA — Navegador accesible (prototipo)";
        Width = 1100;
        Height = 800;
        MinimumSize = new System.Drawing.Size(750, 500);
        Font = new System.Drawing.Font("Segoe UI", 12);
        KeyPreview = true;

        var page = new TableLayoutPanel { Dock = DockStyle.Fill, RowCount = 4, ColumnCount = 1 };
        page.RowStyles.Add(new RowStyle(SizeType.Absolute, 48));
        page.RowStyles.Add(new RowStyle(SizeType.Absolute, 58));
        page.RowStyles.Add(new RowStyle(SizeType.Absolute, 44));
        page.RowStyles.Add(new RowStyle(SizeType.Percent, 100));
        var top = new TableLayoutPanel { Dock = DockStyle.Fill, RowCount = 1, ColumnCount = 2 };
        top.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100));
        top.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 115));
        address.Text = "https://www.wikipedia.org/";
        address.KeyDown += (_, e) => { if (e.KeyCode == Keys.Enter) { e.SuppressKeyPress = true; Navigate(address.Text); } };
        top.Controls.Add(address, 0, 0);
        top.Controls.Add(NewButton("Abrir", () => Navigate(address.Text)), 1, 0);
        page.Controls.Add(top, 0, 0);

        var controls = new FlowLayoutPanel { Dock = DockStyle.Fill, AutoScroll = true, WrapContents = false };
        controls.Controls.Add(NewButton("Atrás", () => { if (browser.CoreWebView2?.CanGoBack == true) browser.CoreWebView2.GoBack(); }));
        controls.Controls.Add(NewButton("Siguiente control", () => _ = Command("cursor siguiente")));
        controls.Controls.Add(NewButton("Seleccionar", () => _ = Command("seleccionar")));
        controls.Controls.Add(NewButton("Pausar", () => _ = Command("pausar")));
        controls.Controls.Add(NewButton("Continuar", () => _ = Command("continuar")));
        controls.Controls.Add(NewButton("Estudio automático", () => RequestAutoStudy()));
        controls.Controls.Add(NewButton("Cerrar página", () => browser.CoreWebView2?.Navigate("about:blank")));
        voiceButton.Click += (_, _) => ToggleVoice();
        controls.Controls.Add(voiceButton);
        page.Controls.Add(controls, 0, 1);
        page.Controls.Add(status, 0, 2);
        page.Controls.Add(browser, 0, 3);
        Controls.Add(page);

        Shown += async (_, _) => await InitializeBrowser();
        FormClosed += (_, _) => Cleanup();
    }

    private static Button NewButton(string label, Action action)
    {
        var button = new Button { Text = label, AutoSize = true, AccessibleName = label };
        button.Click += (_, _) => action();
        return button;
    }

    private async Task InitializeBrowser()
    {
        try
        {
            await browser.EnsureCoreWebView2Async();
            browser.CoreWebView2.Settings.IsStatusBarEnabled = false;
            browser.CoreWebView2.Settings.AreDevToolsEnabled = false;
            browser.CoreWebView2.Settings.AreDefaultContextMenusEnabled = true;
            browser.CoreWebView2.NavigationStarting += (_, e) =>
            {
                if (e.Uri != "about:blank" && (!Uri.TryCreate(e.Uri, UriKind.Absolute, out var u) || u.Scheme != Uri.UriSchemeHttps))
                {
                    e.Cancel = true;
                    SetStatus("ACCESSIA abre solo páginas HTTPS en esta versión.");
                }
                autoConsentPending = false;
            };
            browser.CoreWebView2.NewWindowRequested += (_, e) =>
            {
                e.Handled = true;
                SetStatus("Se bloqueó una ventana emergente; abre el enlace en esta pestaña si es necesario.");
            };
            browser.CoreWebView2.WebMessageReceived += (_, e) =>
            {
                // Untrusted web messages: display informational results ONLY, never execute host actions.
                try
                {
                    using var obj = JsonDocument.Parse(e.WebMessageAsJson);
                    if (!obj.RootElement.TryGetProperty("type", out var type) || type.GetString() != "accessia_result") return;
                    if (!obj.RootElement.TryGetProperty("payload", out var payload)) return;
                    var sentence = payload.TryGetProperty("message", out var m) ? m.GetString() : null;
                    var readText = payload.TryGetProperty("text", out var t) ? t.GetString() : null;
                    string message = !string.IsNullOrWhiteSpace(readText) ? readText! : (sentence ?? "Acción completada.");
                    SetStatus(message[..Math.Min(800, message.Length)]);
                }
                catch (JsonException) { SetStatus("Respuesta de página no interpretable."); }
            };
            string script = await File.ReadAllTextAsync(Path.Combine(AppContext.BaseDirectory, "assets", "accessia.js"));
            await browser.CoreWebView2.AddScriptToExecuteOnDocumentCreatedAsync(script);
            browserReady = true;
            Navigate(address.Text);
            SetupVoice();
        }
        catch (Exception ex)
        {
            SetStatus("No se pudo preparar WebView2. Comprueba el Runtime instalado: " + ex.Message);
        }
    }

    private void Navigate(string input)
    {
        if (!browserReady) return;
        input = input.Trim();
        if (input.Length == 0) return;
        string destination;
        if (input.StartsWith("https://", StringComparison.OrdinalIgnoreCase)) destination = input;
        else if (input.StartsWith("http://", StringComparison.OrdinalIgnoreCase)) destination = "https://" + input[7..];
        else if (input.Contains(' ') || !input.Contains('.')) destination = "https://www.google.com/search?q=" + Uri.EscapeDataString(input);
        else destination = "https://" + input;
        if (!Uri.TryCreate(destination, UriKind.Absolute, out var url) || url.Scheme != Uri.UriSchemeHttps)
        {
            SetStatus("Dirección no válida.");
            return;
        }
        address.Text = destination;
        browser.CoreWebView2.Navigate(destination);
        SetStatus("Abriendo " + url.Host + ". Continuidad automática desactivada para la nueva página.");
    }

    private async Task Command(string value)
    {
        if (!browserReady) { SetStatus("El navegador todavía no está disponible."); return; }
        // Only trusted host-side code invokes execute; user-controlled values are JSON encoded.
        string encoded = JsonSerializer.Serialize(value);
        string code = "(async()=>{try{if(window.Accessia){window.Accessia.setFeedbackHandler(" +
                      "r=>window.chrome.webview.postMessage({type:'accessia_result',payload:r}));}" +
                      "const r=window.Accessia?await window.Accessia.execute(" + encoded + "):" +
                      "{ok:false,message:'Esta página todavía no admite comandos.'};" +
                      "window.chrome.webview.postMessage({type:'accessia_result',payload:r});}" +
                      "catch(e){window.chrome.webview.postMessage({type:'accessia_result',payload:{ok:false,message:'Acción no disponible.'}});}})();";
        try { await browser.CoreWebView2.ExecuteScriptAsync(code); }
        catch (Exception) { SetStatus("No se pudo ejecutar el comando en este documento."); }
    }

    private void RequestAutoStudy()
    {
        var response = MessageBox.Show(this,
            "ACCESSIA podrá continuar al terminar un video HTML5 solo si encuentra una única opción 'Siguiente'. " +
            "No resolverá exámenes ni saltará contenido. ¿Activar solo en esta página?",
            "Autorización de continuidad", MessageBoxButtons.YesNo, MessageBoxIcon.Question);
        if (response == DialogResult.Yes) _ = Command("activar estudio automatico");
    }

    private void SetupVoice()
    {
        try
        {
            speech = new SpeechSynthesizer();
            speech.SpeakCompleted += (_, _) =>
            {
                if (IsDisposed || disposed) return;
                BeginInvoke((Action)(() => { isSpeaking = false; StartRecognizing(); }));
            };
            var info = SpeechRecognitionEngine.InstalledRecognizers()
                .FirstOrDefault(r => r.Culture.Name.StartsWith("es", StringComparison.OrdinalIgnoreCase));
            if (info is null)
            {
                voiceButton.Enabled = false;
                SetStatus("Falta el motor de reconocimiento de español de Windows. Los botones funcionan normalmente.");
                return;
            }
            recognition = new SpeechRecognitionEngine(info);
            var phrases = new Choices(new[] {
                "pausar", "continuar", "reproducir", "cursor siguiente", "cursor anterior",
                "seleccionar", "siguiente lección", "subir", "bajar", "leer página",
                "activar estudio automático", "desactivar estudio automático",
                "confirmar", "cancelar", "cerrar navegador", "cerrar página", "volver", "adelante", "ayuda"
            });
            var grammar = new GrammarBuilder { Culture = info.Culture };
            grammar.Append(phrases);
            recognition.LoadGrammar(new Grammar(grammar));
            try { recognition.LoadGrammar(new DictationGrammar()); }
            catch (Exception) { /* Dictation may be unavailable for this installed language. */ }
            recognition.SpeechRecognized += (_, e) =>
            {
                if (!voiceOn || e.Result.Confidence < 0.65f) return;
                BeginInvoke((Action)(() => HandleSpeech(e.Result.Text)));
            };
            recognition.SetInputToDefaultAudioDevice();
        }
        catch (Exception ex)
        {
            recognition?.Dispose(); recognition = null;
            voiceButton.Enabled = false;
            SetStatus("El reconocimiento de voz no se pudo iniciar: " + ex.Message);
        }
    }

    private void ToggleVoice()
    {
        if (recognition is null) { SetStatus("No existe reconocedor de español instalado."); return; }
        voiceOn = !voiceOn;
        voiceButton.Text = voiceOn ? "Desactivar voz" : "Activar voz";
        if (voiceOn) { StartRecognizing(); SetStatus("Micrófono activado en primer plano. Di 'ayuda'."); }
        else { try { recognition.RecognizeAsyncCancel(); } catch (InvalidOperationException) { } SetStatus("Micrófono desactivado."); }
    }

    private void StartRecognizing()
    {
        if (!voiceOn || isSpeaking || recognition is null || disposed) return;
        try { recognition.RecognizeAsync(RecognizeMode.Multiple); }
        catch (InvalidOperationException) { /* Recognition session already running. */ }
    }

    private void HandleSpeech(string spoken)
    {
        string command = spoken.ToLowerInvariant().Trim();
        if (autoConsentPending)
        {
            autoConsentPending = false;
            if (command == "confirmar") _ = Command("activar estudio automatico");
            else SetStatus("Continuidad no autorizada.");
            return;
        }
        switch (command)
        {
            case "cerrar navegador": Close(); break;
            case "cerrar página": browser.CoreWebView2?.Navigate("about:blank"); break;
            case "volver": if (browser.CoreWebView2?.CanGoBack == true) browser.CoreWebView2.GoBack(); break;
            case "adelante": if (browser.CoreWebView2?.CanGoForward == true) browser.CoreWebView2.GoForward(); break;
            case "activar estudio automático":
                autoConsentPending = true;
                SetStatus("¿Activar continuación al finalizar esta lección? Di confirmar o cancelar.");
                break;
            default:
                if (command.StartsWith("buscar ") || command.StartsWith("abrir ")) Navigate(spoken[(spoken.IndexOf(' ') + 1)..]);
                else _ = Command(command);
                break;
        }
    }

    private void SetStatus(string text)
    {
        if (disposed || IsDisposed) return;
        status.Text = text;
        status.AccessibleDescription = text;
        if (!voiceOn || speech is null) return;
        // Prevent speech recognizer from interpreting synthesized speech as user commands.
        isSpeaking = true;
        try { recognition?.RecognizeAsyncCancel(); } catch (InvalidOperationException) { }
        try { speech.SpeakAsyncCancelAll(); speech.SpeakAsync(text[..Math.Min(text.Length, 350)]); }
        catch (InvalidOperationException) { isSpeaking = false; StartRecognizing(); }
    }

    private void Cleanup()
    {
        disposed = true;
        voiceOn = false;
        recognition?.RecognizeAsyncCancel();
        recognition?.Dispose();
        speech?.SpeakAsyncCancelAll();
        speech?.Dispose();
    }
}
