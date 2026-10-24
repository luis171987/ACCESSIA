using Accessia.Windows.Agent;
using System.Text.Json;
using System.Text.Json.Nodes;
// Explicit opt-in console probe: downloads only with --download-consented.
var model=new LocalModel();using var cancellation=new CancellationTokenSource(TimeSpan.FromMinutes(8));
if(!await LocalModel.Verified(LocalModel.ModelPath,cancellation.Token)) {
    if(!args.Contains("--download-consented")) throw new InvalidOperationException("Model absent. Pass --download-consented to consent to the documented 639446688-byte model download.");
    Console.WriteLine("Explicit cloud probe consent: downloading verified model into "+LocalModel.ModelPath);
    await model.Download(cancellation.Token);
}
var observation=JsonNode.Parse("{\"url\":\"https://example.org/\",\"title\":\"Biblioteca municipal\",\"text\":\"La biblioteca abre de lunes a viernes de 9 a 18 horas.\",\"controls\":[{\"id\":\"c1\",\"name\":\"Horario de biblioteca\",\"role\":\"link\",\"href\":\"https://example.org/horario\"}],\"sources\":[{\"id\":\"page\",\"url\":\"https://example.org/\",\"title\":\"Biblioteca municipal\"}]}")!;
var cases=new[]{("Lee la página","read"),("Encuentra horarios de bibliotecas en Sucre","search"),("Abre el enlace Horario de biblioteca","open"),("Baja un poco","scroll_down")};
foreach(var (request,expected) in cases) {
    using var deadline=CancellationTokenSource.CreateLinkedTokenSource(cancellation.Token);deadline.CancelAfter(TimeSpan.FromSeconds(90));
    var timer=System.Diagnostics.Stopwatch.StartNew();
    var raw=await model.InferRaw(JsonSerializer.Serialize(new {request,observation},AgentTools.ModelJson),deadline.Token);
    var result=AgentTools.Proposal(raw);
    var proposal=AgentPolicy.CanonicalizeOpen(AgentPolicy.Normalize(result,"probe"),observation.AsObject());
    Console.WriteLine(JsonSerializer.Serialize(new {request,proposal,elapsedSeconds=timer.Elapsed.TotalSeconds}));
    if(proposal["action"]?.GetValue<string>()!=expected) throw new Exception("Unexpected intent: "+expected);
    if(expected=="search" && !proposal["value"]!.GetValue<string>().Contains("Sucre",StringComparison.OrdinalIgnoreCase)) throw new Exception("Search omitted actual requested location");
    if(expected=="open" && proposal["target"]?.GetValue<string>()!="c1") throw new Exception("Open omitted observed target");
}
Console.WriteLine("4 real GGUF CPU intent probes passed; this is a Linux cloud probe, not Windows UI/device verification.");
