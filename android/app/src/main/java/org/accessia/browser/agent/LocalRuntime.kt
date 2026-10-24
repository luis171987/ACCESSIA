package org.accessia.browser.agent
import com.google.ai.edge.litertlm.*

fun interface AgentRuntime { fun infer(path:String,prompt:String,request:String):String }
class LiteRuntime:AgentRuntime {
 override fun infer(path:String,prompt:String,request:String):String {
  require(ModelStore.verify(java.io.File(path))) { "Modelo ausente o no verificado" }
  Engine(EngineConfig(modelPath=path,backend=Backend.CPU(),maxNumTokens=4096)).use { engine ->
   engine.initialize()
   engine.createConversation(ConversationConfig(maxOutputToken=512,systemInstruction=Contents.of(prompt),samplerConfig=SamplerConfig(topK=1,topP=1.0,temperature=0.0))).use { conversation ->
    return conversation.sendMessage(request,extraContext=mapOf("enable_thinking" to false)).toString().also { require(it.length<=8192) { "Respuesta del modelo demasiado larga" } }
   }
  }
 }
}
