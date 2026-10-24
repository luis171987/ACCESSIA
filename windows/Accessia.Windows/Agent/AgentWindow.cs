using System.Text.Json;
using System.Text.Json.Nodes;
namespace Accessia.Windows;
public sealed partial class MainWindow
{
    private readonly Agent.LocalModel localModel=new();
    private readonly Agent.AgentEpoch agentEpoch=new();
    private CancellationTokenSource? agentCancel;
    private JsonObject? pendingAgent;
    private Form? agentReview;
    private bool agentEnabled,agentBusy;
    private readonly TextBox agentInput=new() {Width=350,AccessibleName="Petición al asistente local",MaxLength=1000};
    private void AddAgentControls(FlowLayoutPanel controls) {
        controls.Controls.Add(NewButton("Asistente local",ConfigureAgent));
        controls.Controls.Add(agentInput);
        controls.Controls.Add(NewButton("Pedir al asistente",()=>_ = AskAgent(agentInput.Text)));
        controls.Controls.Add(NewButton("Confirmar acción IA",()=>_ = ConfirmAgent()));
        controls.Controls.Add(NewButton("Cancelar IA",()=>{CancelAgent();SetStatus("Petición cancelada.");}));
        agentInput.KeyDown+=(_,e)=>{if(e.KeyCode==Keys.Enter){e.SuppressKeyPress=true;_ = AskAgent(agentInput.Text);}};
    }
    private void CancelAgent() {agentEpoch.Invalidate();pendingAgent=null;agentCancel?.Cancel();var review=agentReview;agentReview=null;review?.Close();}
    private async void ConfigureAgent() {
        if(agentBusy) {SetStatus("Cancela y espera a que termine la operación actual.");return;}
        var choice=MessageBox.Show(this,"Asistente experimental local; puede equivocarse. Se ejecuta en CPU sin nube ni cuentas. La voz de Windows puede usar red.\n\nSí: activar y, si falta, descargar Qwen3-0.6B Q8_0 (639.446.688 bytes, Apache-2.0) desde Hugging Face, con SHA256 verificado en datos de tu cuenta.\nNo: desactivar y ofrecer borrar el modelo.\nCancelar: no cambiar.","Asistente local opcional",MessageBoxButtons.YesNoCancel,MessageBoxIcon.Information);
        if(choice==DialogResult.Cancel) return;
        CancelAgent();agentEnabled=false;
        if(choice==DialogResult.No) {
            if(File.Exists(Agent.LocalModel.ModelPath) && MessageBox.Show(this,"¿Borrar el modelo local de esta cuenta?", "Borrar modelo",MessageBoxButtons.YesNo)==DialogResult.Yes) File.Delete(Agent.LocalModel.ModelPath);
            SetStatus("Asistente desactivado. Los comandos habituales siguen disponibles.");return;
        }
        agentBusy=true;agentCancel=new CancellationTokenSource();var ticket=agentEpoch.Current;
        try {
            if(!await Agent.LocalModel.Verified(Agent.LocalModel.ModelPath,agentCancel.Token)) {SetStatus("Descargando y verificando modelo. Puedes cancelar IA.");await localModel.Download(agentCancel.Token);}
            if(!agentEpoch.IsCurrent(ticket)) return;agentEnabled=true;SetStatus("Asistente local activado para esta sesión. Escribe o di una petición.");
        }catch(OperationCanceledException) {SetStatus("Descarga cancelada.");}
        catch(Exception e){SetStatus("No se pudo activar el asistente: "+e.Message);}
        finally {agentBusy=false;agentCancel?.Dispose();agentCancel=null;}
    }
    private async Task<JsonObject> AgentRpc(string expression) {
        // Shared agentObserve/agentExecute are synchronous; only this user-initiated
        // host RPC consumes the return value, never web message events.
        var raw=await browser.CoreWebView2.ExecuteScriptAsync(expression);
        return JsonNode.Parse(raw) as JsonObject ?? throw new InvalidOperationException("Respuesta de página inválida.");
    }
    private async Task AskAgent(string request) {
        if(!agentEnabled) {SetStatus("Activa primero Asistente local. No se descarga nada automáticamente.");return;}
        if(!browserReady || agentBusy) {SetStatus("El asistente está ocupado o el navegador no está preparado.");return;}
        request=request.Trim();if(request.Length==0 || request.Length>1000) {SetStatus("Escribe una petición de hasta 1000 caracteres.");return;}
        CancelAgent();agentBusy=true;agentCancel=new CancellationTokenSource(TimeSpan.FromSeconds(90));var ticket=agentEpoch.Current;
        try {
            var observation=await AgentRpc("window.Accessia?.agentObserve() ?? {ok:false}");
            if(observation["ok"]?.GetValue<bool>()!=true || observation["snapshot"] is not JsonValue) throw new InvalidOperationException("No se puede observar esta página.");
            var snapshot=observation["snapshot"]!.GetValue<string>();
            var data=observation.ToJsonString();if(data.Length>30000) throw new InvalidOperationException("Observación demasiado grande.");
            SetStatus("Interpretando petición localmente. Puedes cancelar IA.");
            var modelObservation=(JsonObject)observation.DeepClone();
            if(modelObservation["text"] is JsonValue pageText) {var text=pageText.GetValue<string>();modelObservation["text"]=text[..Math.Min(text.Length,2500)];}
            if(modelObservation["controls"] is JsonArray items) {
                while(items.Count>20) items.RemoveAt(items.Count-1);
                foreach(var item in items.OfType<JsonObject>()) foreach(var field in new[]{"name","href"}) if(item[field] is JsonValue fieldValue) {var text=fieldValue.GetValue<string>();item[field]=text[..Math.Min(text.Length,field=="href"?300:80)];}
            }
            if(modelObservation.ToJsonString().Length>12000) throw new InvalidOperationException("Observación excede el contexto local.");
            var output=await localModel.Infer(JsonSerializer.Serialize(new {request,observation=modelObservation}),agentCancel.Token);
            if(!agentEpoch.IsCurrent(ticket)) return;
            agentCancel.Token.ThrowIfCancellationRequested();
            var proposal=Agent.AgentPolicy.CanonicalizeOpen(Agent.AgentPolicy.Normalize(output,snapshot),observation);
            if(Agent.AgentPolicy.NeedsConfirmation(proposal["action"]!.GetValue<string>())) {
                pendingAgent=proposal;
                ShowAgentProposal(proposal,observation);
            } else await ExecuteAgent(proposal,false,ticket);
        } catch(OperationCanceledException) {if(agentEpoch.IsCurrent(ticket)) SetStatus("La inferencia se canceló o superó el límite de 90 segundos.");}
        catch(Exception e) {if(agentEpoch.IsCurrent(ticket)) SetStatus("Asistente: "+e.Message);}
        finally {agentBusy=false;agentCancel?.Dispose();agentCancel=null;}
    }
    private void ShowAgentProposal(JsonObject proposal,JsonObject observation) {
        var target=proposal["target"]?.GetValue<string>();
        var observed=(observation["controls"] as JsonArray)?.OfType<JsonObject>().FirstOrDefault(c=>c["id"]?.GetValue<string>()==target);
        var targetName=observed?["name"]?.ToString()??target??"sin control";
        var description="Acción: "+proposal["action"]+"\r\nControl: "+targetName+"\r\nDestino: "+(Agent.AgentPolicy.Navigation(proposal)??observed?["href"]?.ToString()??"sin navegación")+"\r\nValor: "+proposal["value"]?.ToString()+"\r\n\r\nEsta acción puede modificar la página. Solo confirma si coincide con tu petición.";
        var dialog=new Form {Text="Confirmar propuesta del asistente",Width=650,Height=340,Owner=this,StartPosition=FormStartPosition.CenterParent};
        var content=new TextBox {Text=description,Multiline=true,ReadOnly=true,Dock=DockStyle.Fill,ScrollBars=ScrollBars.Vertical,AccessibleName="Detalles de la acción propuesta"};
        var buttons=new FlowLayoutPanel {Dock=DockStyle.Bottom,Height=55};
        buttons.Controls.Add(NewButton("Confirmar",()=>_ = ConfirmAgent()));
        buttons.Controls.Add(NewButton("Cancelar",()=>{CancelAgent();SetStatus("Propuesta cancelada.");}));
        dialog.Controls.Add(content);dialog.Controls.Add(buttons);
        dialog.FormClosed+=(_,_)=>{if(ReferenceEquals(agentReview,dialog)){agentReview=null;CancelAgent();}};
        agentReview=dialog;dialog.Show(this);
        SetStatus(description.Replace("\r\n"," ")+" Di confirmar o cancelar.");
    }
    private async Task ConfirmAgent() {
        var proposal=pendingAgent;if(proposal==null) {SetStatus("No hay una acción IA pendiente.");return;}
        pendingAgent=null;var review=agentReview;agentReview=null;review?.Close();var ticket=agentEpoch.Current;
        try {await ExecuteAgent(proposal,true,ticket);}catch(Exception e){if(agentEpoch.IsCurrent(ticket)) SetStatus("Acción no disponible: "+e.Message);}
    }
    private async Task ExecuteAgent(JsonObject proposal,bool confirmed,long ticket) {
        if(!agentEpoch.IsCurrent(ticket)) return;
        var result=await AgentRpc("window.Accessia.agentExecute("+proposal.ToJsonString()+","+(confirmed?"true":"false")+")");
        if(!agentEpoch.IsCurrent(ticket)) return;
        if(result["ok"]?.GetValue<bool>()==true && confirmed && Agent.AgentPolicy.Navigation(proposal) is string destination) {
            if(!Agent.AgentPolicy.MatchesNavigation(destination,result["navigation"]?["url"]?.GetValue<string>())) throw new InvalidOperationException("El destino validado por la página no coincide con la propuesta confirmada.");
            Navigate(destination);return;
        }
        var message=result["message"]?.GetValue<string>()??"Sin resultado verificable.";
        if(result["text"] is JsonValue text) message+=" "+text.GetValue<string>();
        if(result["sources"] is JsonArray sources) foreach(var source in sources.OfType<JsonObject>()) message+=" "+source["url"]?.ToString();
        SetStatus(message[..Math.Min(message.Length,6000)]);
    }
}
