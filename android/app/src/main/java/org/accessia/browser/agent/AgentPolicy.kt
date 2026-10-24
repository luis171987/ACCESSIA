package org.accessia.browser.agent
import org.json.JSONObject

object AgentPolicy {
 private val actions=setOf("read","scroll_up","scroll_down","focus","click","fill","search","open","answer","clarify")
 fun proposal(raw:String,snapshot:String):JSONObject {
  require(raw.length<=8192) { "Respuesta del modelo demasiado larga" }
  val input=JSONObject(raw.trim()); require(input.optString("action") in actions) { "Acción del modelo no permitida" }
  for ((key,limit) in mapOf("target" to 80,"value" to 2000,"text" to 4000)) if(input.has(key)) require(input.get(key) is String && input.getString(key).length<=limit) { "Campo $key inválido" }
  if(input.has("citations")) {
   val citations=input.optJSONArray("citations") ?: throw IllegalArgumentException("Citas inválidas")
   require(citations.length()<=4) { "Demasiadas citas" }
   for(i in 0 until citations.length()) require(citations.get(i) is String && citations.getString(i).length<=80) { "Cita inválida" }
  }
  return JSONObject().apply {
   put("action",input.getString("action")); put("snapshot",snapshot)
   for (key in listOf("target","value","text","citations")) if(input.has(key)) put(key,input.get(key))
  }
 }
 fun needsConfirmation(p:JSONObject)=p.optString("action") in setOf("click","fill","search","open")
}
class AgentGeneration {
 private var generation=0L
 @Synchronized fun begin():Long=++generation
 @Synchronized fun cancel() { generation++ }
 @Synchronized fun accepts(ticket:Long)=ticket==generation
}
