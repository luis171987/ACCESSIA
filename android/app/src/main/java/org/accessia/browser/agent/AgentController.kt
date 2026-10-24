package org.accessia.browser.agent
import android.app.Activity
import android.app.AlertDialog
import android.os.*
import android.widget.EditText
import org.json.JSONObject
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class AgentController(private val activity:Activity,private val announce:(String)->Unit,private val navigate:(String)->Unit,private val send:(JSONObject)->Boolean,private val runtime:AgentRuntime=LiteRuntime()) {
 private val handler=Handler(Looper.getMainLooper())
 private val worker=Executors.newSingleThreadExecutor()
 private val gate=AgentGeneration()
 private val store=ModelStore(File(activity.filesDir,"local-agent"))
 private var stop=AtomicBoolean(false)
 var enabled=false; private set
 private var busy=false
 @Volatile private var workerBusy=false
 private var pending:JSONObject?=null
 private var dialog:AlertDialog?=null
 private var rpcId:String?=null
 private var callback:((JSONObject)->Unit)?=null
 fun cancel() { gate.cancel(); stop.set(true); busy=false; pending=null; dialog?.dismiss(); dialog=null; rpcId=null; callback=null; handler.removeCallbacksAndMessages(null) }
 fun close() { cancel(); worker.shutdownNow() }
 fun settings() {
  AlertDialog.Builder(activity).setTitle("Asistente local experimental")
   .setMessage("Qwen3-0.6B · Apache-2.0 · 614 MB. Inferencia local CPU, Android 12+. La voz Android puede usar red. Apagado al iniciar. Revisa las acciones antes de confirmar.")
   .setItems(arrayOf(if(enabled) "Desactivar" else "Activar", "Escribir petición", "Descargar modelo", "Borrar modelo", "Cancelar petición")) { _,i -> when(i) {
    0 -> { cancel(); when { enabled -> { enabled=false; announce("Asistente apagado") }; Build.VERSION.SDK_INT<31 -> announce("El motor requiere Android 12 o posterior"); !store.file.isFile -> announce("Descarga primero el modelo opcional"); else -> { enabled=true; announce("Asistente activado") } } }
    1 -> { val input=EditText(activity).apply { hint="Petición sobre la página"; contentDescription=hint }; AlertDialog.Builder(activity).setTitle("Asistente local").setView(input).setPositiveButton("Enviar") { _,_-> submit(input.text.toString()) }.setNegativeButton("Cancelar",null).show() }
    2 -> AlertDialog.Builder(activity).setTitle("Descargar modelo privado").setMessage("¿Descargar 613.722.264 bytes desde Hugging Face? Puede consumir datos móviles. Se comprobará SHA256. Se guarda únicamente en datos privados de ACCESSIA.").setPositiveButton("Descargar") { _,_-> download() }.setNegativeButton("Cancelar",null).show()
    3 -> { enabled=false; cancel(); worker.execute { val result=runCatching { store.delete() }; activity.runOnUiThread { announce(result.fold({"Modelo privado borrado"},{"Borrado: ${it.message}"})) } } }
    4 -> { cancel(); announce("Petición cancelada") }
   } }.setNegativeButton("Cerrar",null).show()
 }
 private fun download() {
  if(workerBusy || busy) { announce("Espera a que termine la tarea local o cancélala"); return }
  cancel(); busy=true; stop=AtomicBoolean(false); val cancellation=stop; val ticket=gate.begin(); announce("Descargando modelo privado…")
  worker.execute { val result=runCatching { store.download(cancellation) }; activity.runOnUiThread { if(gate.accepts(ticket)) { busy=false; announce(result.fold({"Modelo verificado. Actívalo desde el menú."},{"Descarga: ${it.message}"})) } } }
 }
 fun submit(raw:String) {
  if(!enabled) { announce("Activa el asistente desde el menú"); return }
  if(busy || workerBusy) { announce("Hay una petición en curso. Di cancelar."); return }
  if(raw.isBlank() || raw.length>2000) { announce("Petición vacía o demasiado larga"); return }
  cancel(); busy=true; val ticket=gate.begin()
  rpc("agent_observe",JSONObject()) { observation ->
   if(!gate.accepts(ticket)) return@rpc
   val snapshot=observation.optString("snapshot")
   if(!observation.optBoolean("ok") || snapshot.isBlank()) { busy=false; announce(observation.optString("message","No se pudo observar esta página")); return@rpc }
   val bounded=JSONObject().put("url",observation.optString("url").take(2048)).put("title",observation.optString("title").take(300)).put("text",observation.optString("text").take(4500)).put("controls",observation.optJSONArray("controls")).put("sources",observation.optJSONArray("sources"))
   val request=JSONObject().put("request",raw).put("observation",bounded).toString()
   if(request.length>14000) { busy=false; announce("Página demasiado extensa para el contexto local"); return@rpc }
   announce("Interpretando en este dispositivo…")
   workerBusy=true
   handler.postDelayed({ if(gate.accepts(ticket) && workerBusy) { cancel(); announce("Inferencia cancelada por tiempo de espera; el motor terminará en segundo plano") } },90000)
   worker.execute {
    val result=runCatching { val prompt=activity.assets.open("agent-prompt.txt").bufferedReader().use { it.readText() }; AgentPolicy.proposal(runtime.infer(store.file.path,prompt,request),snapshot) }
    workerBusy=false
    activity.runOnUiThread { if(gate.accepts(ticket)) result.fold({ execute(it,false,ticket) },{ busy=false; announce("Asistente: ${it.message}") }) }
   }
  }
 }
 fun confirm() { val p=pending ?: run { announce("No hay acción pendiente"); return }; pending=null; dialog?.dismiss(); dialog=null; busy=true; execute(p,true,gate.begin()) }
 private fun execute(proposal:JSONObject,confirmed:Boolean,ticket:Long) {
  rpc("agent_execute",JSONObject().put("proposal",proposal).put("confirmed",confirmed)) { result ->
   if(!gate.accepts(ticket)) return@rpc
   busy=false
   if(result.optBoolean("requiresConfirmation") && !confirmed && AgentPolicy.needsConfirmation(proposal)) {
    pending=proposal
    val detail=(result.optString("message")+"\n"+"${proposal.optString("action")} ${proposal.optString("value")}").take(1200)
    announce("Confirma: $detail. Di confirmar o cancelar.")
    dialog=AlertDialog.Builder(activity).setTitle("Confirmar una acción").setMessage(detail).setPositiveButton("Confirmar") { _,_-> confirm() }.setNegativeButton("Cancelar") { _,_-> cancel(); announce("Acción cancelada") }.setOnCancelListener { cancel() }.show(); return@rpc
   }
   val message=result.optString("text").ifBlank { result.optString("message","Sin resultado") }
   val sources=result.optJSONArray("sources"); val urls=mutableListOf<String>(); if(sources!=null) for(i in 0 until sources.length()) urls.add(sources.optJSONObject(i)?.optString("url").orEmpty())
   if (proposal.optString("action") in setOf("answer","read","clarify")) AlertDialog.Builder(activity).setTitle("Respuesta del asistente local").setMessage(message+if(urls.isEmpty()) "" else "\nFuentes: "+urls.joinToString("\n")).setPositiveButton("Cerrar",null).show()
   announce((message+if(urls.isEmpty()) "" else "\nFuentes: "+urls.joinToString("\n")).take(5000))
   val url=result.optJSONObject("navigation")?.optString("url")
   if(result.optBoolean("ok") && confirmed && proposal.optString("action") in setOf("search","open") && !url.isNullOrBlank()) {
    val uri=android.net.Uri.parse(url); if(uri.scheme=="https" && !uri.host.isNullOrBlank() && uri.userInfo==null) navigate(url) else announce("Navegación rechazada")
   }
  }
 }
 private fun rpc(type:String,payload:JSONObject,done:(JSONObject)->Unit) {
  val id=UUID.randomUUID().toString(); rpcId=id; callback=done
  handler.postDelayed({ if(rpcId==id) { cancel(); announce("Tiempo de espera de la página agotado") } },15000)
  try { if(!send(payload.put("type",type).put("id",id))) { cancel(); announce("No hay conexión con esta página") } } catch(e:Exception) { cancel(); announce("Puente: ${e.message}") }
 }
 fun result(message:JSONObject) {
  if(message.optString("type")!="agent_result" || message.optString("id")!=rpcId) return
  val done=callback ?: return; rpcId=null; callback=null
  done(message.optJSONObject("payload") ?: JSONObject().put("ok",false).put("message","Respuesta inválida del puente"))
 }
}
