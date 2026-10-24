using Accessia.Windows.Agent;
using System.Text.Json.Nodes;
int count=0;
void Check(bool value) { if(!value) throw new Exception("Policy failure " + count); count++; }
Check(AgentPolicy.Normalize("{\"action\":\"click\",\"target\":\"c1\",\"snapshot\":\"evil\",\"confirmed\":true}","trusted")["snapshot"]!.ToString()=="trusted");
Check(!AgentPolicy.Normalize("{\"action\":\"read\",\"confirmed\":true}","s").ContainsKey("confirmed"));
foreach(var invalid in new[]{"{\"action\":\"shell\"}","[]","{\"action\":\"read\"} trailing","{\"action\":\"click\",\"target\":55}"}) {try {AgentPolicy.Normalize(invalid,"s");throw new Exception("accepted");}catch(ArgumentException) {count++;}}
Check(AgentPolicy.NeedsConfirmation("click")); Check(!AgentPolicy.NeedsConfirmation("read"));
var epoch=new AgentEpoch(); var ticket=epoch.Current;epoch.Invalidate();Check(!epoch.IsCurrent(ticket));
Console.WriteLine($"{count} policy checks passed");
Check(AgentPolicy.Navigation(AgentPolicy.Normalize("{\"action\":\"search\",\"value\":\"a&b\"}","s"))=="https://www.google.com/search?q=a%26b");
foreach(var url in new[]{"javascript:alert(1)","http://example.com","https://user:pass@example.com"}) Check(AgentPolicy.Navigation(AgentPolicy.Normalize(System.Text.Json.JsonSerializer.Serialize(new{action="open",value=url}),"s"))==null);
Console.WriteLine($"{count} policy checks passed (final)");

var modelFile=Path.GetTempFileName();
try {
    await File.WriteAllTextAsync(modelFile,"abc");
    Check(!await ModelIntegrity.Verify(modelFile,4,"wrong",CancellationToken.None));
    Check(!await ModelIntegrity.Verify(modelFile,3,new string('0',64),CancellationToken.None));
    Check(await ModelIntegrity.Verify(modelFile,3,"ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",CancellationToken.None));
} finally {File.Delete(modelFile);}
Console.WriteLine($"{count} checks passed including size/hash validation");
Check(AgentPolicy.Normalize("<think>\n\n</think>\n{\"action\":\"read\"}","s")["action"]!.GetValue<string>()=="read");
Console.WriteLine($"{count} final checks passed");
var observedPage=JsonNode.Parse("{\"url\":\"https://current.example/\",\"controls\":[{\"id\":\"c1\",\"href\":\"https://good.example/\"}]}")!.AsObject();
var canonical=AgentPolicy.CanonicalizeOpen(AgentPolicy.Normalize("{\"action\":\"open\",\"target\":\"c1\",\"value\":\"https://evil.example/\"}","s"),observedPage);
Check(AgentPolicy.Navigation(canonical)=="https://good.example/");
try { AgentPolicy.CanonicalizeOpen(AgentPolicy.Normalize("{\"action\":\"open\",\"value\":\"https://evil.example/\"}","s"),observedPage);throw new Exception("Unobserved accepted"); }catch(ArgumentException){count++;}
Check(AgentPolicy.Navigation(AgentPolicy.CanonicalizeOpen(AgentPolicy.Normalize("{\"action\":\"open\",\"value\":\"https://current.example/\"}","s"),observedPage))=="https://current.example/");
Check(AgentPolicy.Navigation(AgentPolicy.CanonicalizeOpen(AgentPolicy.Normalize("{\"action\":\"open\",\"target\":\"c1\"}","s"),observedPage))=="https://good.example/");
Console.WriteLine($"{count} final canonicalization checks passed");
Check(!AgentPolicy.MatchesNavigation("https://good.example/","https://evil.example/"));
Check(AgentPolicy.MatchesNavigation("https://good.example/","https://good.example/"));
Console.WriteLine($"{count} final checks passed");
Check(AgentPolicy.Normalize(AgentTools.Proposal("<tool_call>{\"name\":\"search\",\"arguments\":{\"value\":\"bibliotecas\"}}</tool_call>"),"s")["value"]!.GetValue<string>()=="bibliotecas");
foreach(var invalidTool in new[]{"<tool_call>{\"name\":\"read\",\"arguments\":{}}</tool_call><tool_call>{\"name\":\"read\",\"arguments\":{}}</tool_call>","<tool_call>{\"name\":\"shell\",\"arguments\":{}}</tool_call>","<tool_call>{\"name\":\"click\",\"arguments\":{\"target\":\"c1\",\"confirmed\":true}}</tool_call>"}) {try {AgentTools.Proposal(invalidTool);throw new Exception("Accepted tool call");} catch(ArgumentException){count++;}}
Console.WriteLine($"{count} total checks passed");
var forgedObs=JsonNode.Parse("{\"url\":\"https://real.example/\",\"title\":\"Real\",\"sources\":[{\"id\":\"page\",\"url\":\"https://bank.example/\"}]}")!.AsObject();
Check(AgentPolicy.CanonicalObservation(forgedObs,"https://real.example/")["sources"]![0]!["url"]!.GetValue<string>()=="https://real.example/");
try {AgentPolicy.CanonicalObservation(forgedObs,"https://other.example/");throw new Exception("URL drift accepted");}catch(ArgumentException){count++;}
try {AgentPolicy.AnswerOutput(AgentPolicy.Normalize("{\"action\":\"answer\",\"text\":\"answer\",\"citations\":[\"bank\"]}","s"),"https://real.example/");throw new Exception("Citation accepted");}catch(ArgumentException){count++;}
Console.WriteLine($"{count} provenance checks passed");
