package org.accessia.browser.agent

import com.google.ai.edge.litertlm.*
import org.json.JSONArray
import org.json.JSONObject

fun interface AgentRuntime { fun infer(path:String,prompt:String,request:String):String }

/** Convert one manual native tool call into data; the runtime never executes browser tools. */
object ToolProposal {
 fun parse(calls:List<Pair<String,Map<String,Any?>>>):String {
  require(calls.size==1) { "El modelo debe proponer exactamente una herramienta" }
  val call=calls.single()
  val raw=JSONObject(call.second).put("action",call.first).toString()
  return AgentPolicy.proposal(raw,"").apply { remove("snapshot") }.toString()
 }
}

class LiteRuntime(private val loadTools:()->String):AgentRuntime {
 override fun infer(path:String,prompt:String,request:String):String {
  require(ModelStore.verify(java.io.File(path))) { "Modelo ausente o no verificado" }
  val descriptors=JSONArray(loadTools())
  val providers=(0 until descriptors.length()).map { index ->
   val descriptor=descriptors.getJSONObject(index).toString()
   tool(object:OpenApiTool {
    override fun getToolDescriptionJsonString()=descriptor
    override fun execute(paramsJsonString:String):String = throw IllegalStateException("El motor local no puede ejecutar herramientas")
   })
  }
  require(providers.isNotEmpty()) { "No hay herramientas locales configuradas" }
  Engine(EngineConfig(modelPath=path,backend=Backend.CPU(),maxNumTokens=4096)).use { engine ->
   engine.initialize()
   val config=ConversationConfig(
    maxOutputToken=256,
    systemInstruction=Contents.of(prompt),
    tools=providers,
    automaticToolCalling=false,
    samplerConfig=SamplerConfig(topK=1,topP=1.0,temperature=0.0)
   )
   engine.createConversation(config).use { conversation ->
    val response=conversation.sendMessage(request,extraContext=mapOf("enable_thinking" to false))
    return ToolProposal.parse(response.toolCalls.map { it.name to it.arguments })
   }
  }
 }
}
