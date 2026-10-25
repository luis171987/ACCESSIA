package org.accessia.browser.agent
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28])
class AgentPolicyTest {
 @Test fun stripsModelAuthorityAndInjectsSnapshot() {
  val p=AgentPolicy.proposal("""{"action":"click","target":"c1","confirmed":true,"snapshot":"forged","code":"evil"}""", "real")
  assertEquals("real",p.getString("snapshot")); assertFalse(p.has("confirmed")); assertFalse(p.has("code")); assertTrue(AgentPolicy.needsConfirmation(p))
 }
 @Test fun rejectsUnknownAction() { assertThrows(IllegalArgumentException::class.java) { AgentPolicy.proposal("""{"action":"shell"}""","s") } }
 @Test fun cancellationRejectsLateRuntimeResult() { val gate=AgentGeneration(); val ticket=gate.begin(); gate.cancel(); assertFalse(gate.accepts(ticket)); assertTrue(gate.accepts(gate.begin())) }
 @Test fun rejectsTypedAndOversizeFields() {
  for(raw in listOf("""{"action":"fill","target":{},"value":"x"}""", """{"action":"answer","citations":[{}]}""", """{"action":"fill","value":"${"x".repeat(2001)}"}""")) assertThrows(IllegalArgumentException::class.java) { AgentPolicy.proposal(raw,"s") }
 }
 @Test fun verifiesBothSizeAndHash() {
  val f=java.io.File.createTempFile("model", ".part"); f.writeText("abc")
  assertTrue(ModelStore.verify(f,3,"ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"))
  assertFalse(ModelStore.verify(f,4,"ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")); assertFalse(ModelStore.verify(f,3,"wrong")); f.delete()
 }
}
