package org.accessia.browser.agent
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28])
class ToolProposalTest {
 @Test fun requiresExactlyOnePair() {
  for(calls in listOf(emptyList(),listOf(Pair("read",emptyMap()),Pair("click",mapOf("target" to "c1"))))) assertThrows(IllegalArgumentException::class.java) { ToolProposal.parse(calls) }
 }
 @Test fun toolNameDefinesActionAndCannotGrantAuthority() {
  val proposal=JSONObject(ToolProposal.parse(listOf(Pair("search",mapOf("value" to "educación inclusiva","action" to "click","confirmed" to true,"snapshot" to "forged")))))
  assertEquals("search",proposal.getString("action")); assertEquals("educación inclusiva",proposal.getString("value")); assertFalse(proposal.has("snapshot")); assertFalse(proposal.has("confirmed"))
 }
 @Test fun rejectsUnknownToolsAndInvalidArguments() {
  for(call in listOf(Pair("shell",emptyMap()),Pair("click",mapOf("target" to mapOf("selector" to "*"))),Pair("answer",mapOf("citations" to listOf(mapOf("url" to "invented")))))) assertThrows(IllegalArgumentException::class.java) { ToolProposal.parse(listOf(call)) }
 }
 @Test fun preservesAnswerEvidenceAndText() {
  val proposal=JSONObject(ToolProposal.parse(listOf(Pair("answer",mapOf("text" to "Resumen","citations" to listOf("page"))))))
  assertEquals("Resumen",proposal.getString("text")); assertEquals("page",proposal.getJSONArray("citations").getString(0))
 }
}
