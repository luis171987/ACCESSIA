using System.Text.Json;
using System.Text.Json.Nodes;
namespace Accessia.Windows.Agent;
public static class AgentPolicy
{
    private static readonly HashSet<string> Actions = new(StringComparer.Ordinal) { "read","scroll_up","scroll_down","focus","click","fill","search","open","answer","clarify" };
    public static JsonObject Normalize(string output,string snapshot)
    {
        if(output.Length>8192) throw new ArgumentException("Respuesta demasiado larga.");
        // Qwen3 /no_think can emit an empty thinking wrapper. Accept only that
        // fixed empty prefix; nonempty reasoning, fences and trailing text fail JSON.
        output=System.Text.RegularExpressions.Regex.Replace(output,@"^\s*<think>\s*</think>\s*","");
        JsonObject source;
        try { source=JsonNode.Parse(output) as JsonObject ?? throw new ArgumentException("Se requiere un objeto JSON."); }
        catch(JsonException e) {throw new ArgumentException("El modelo no devolvió JSON válido.",e);}
        if(source["action"] is not JsonValue a || !a.TryGetValue<string>(out var action) || !Actions.Contains(action)) throw new ArgumentException("Acción desconocida.");
        var result=new JsonObject { ["action"]=action,["snapshot"]=snapshot };
        foreach(var key in new[]{"target","value","text"}) if(source.TryGetPropertyValue(key,out var n)) {
            if(n is not JsonValue v || !v.TryGetValue<string>(out var text) || text.Length>(key=="text"?4000:1000)) throw new ArgumentException("Campo inválido: "+key);
            result[key]=text;
        }
        if(source.TryGetPropertyValue("citations",out var citations)) {
            if(citations is not JsonArray array || array.Count>10 || array.Any(n=>n is not JsonValue v || !v.TryGetValue<string>(out var s) || s.Length>100)) throw new ArgumentException("Citas inválidas.");
            result["citations"]=citations.DeepClone();
        }
        return result;
    }
    public static bool NeedsConfirmation(string action)=>action is "click" or "fill" or "search" or "open";
    public static JsonObject CanonicalizeOpen(JsonObject proposal,JsonObject observation) {
        if(proposal["action"]?.GetValue<string>()!="open") return proposal;
        var controls=(observation["controls"] as JsonArray)?.OfType<JsonObject>().ToArray()??Array.Empty<JsonObject>();
        string? destination;
        if(proposal.ContainsKey("target")) {
            var target=proposal["target"]!.GetValue<string>();
            destination=controls.FirstOrDefault(c=>c["id"]?.GetValue<string>()==target)?["href"]?.GetValue<string>();
        } else {
            destination=proposal["value"]?.GetValue<string>();
            if(destination!=observation["url"]?.GetValue<string>() && !controls.Any(c=>c["href"]?.GetValue<string>()==destination)) destination=null;
        }
        if(!Uri.TryCreate(destination,UriKind.Absolute,out var uri) || uri.Scheme!="https" || uri.UserInfo.Length!=0) throw new ArgumentException("Solo se pueden abrir destinos HTTPS observados.");
        proposal["value"]=uri.AbsoluteUri;
        return proposal;
    }
    public static bool MatchesNavigation(string destination,string? returned)=>Uri.TryCreate(returned,UriKind.Absolute,out var uri) && uri.Scheme=="https" && uri.UserInfo.Length==0 && uri.AbsoluteUri==new Uri(destination).AbsoluteUri;
    public static string? Navigation(JsonObject proposal) {
        var action=proposal["action"]?.GetValue<string>(); var value=proposal["value"]?.GetValue<string>();
        if(action=="search" && !string.IsNullOrWhiteSpace(value)) return "https://www.google.com/search?q="+Uri.EscapeDataString(value).Replace("%21","!").Replace("%27", "\'").Replace("%28","(").Replace("%29",")").Replace("%2A","*");
        if(action=="open" && Uri.TryCreate(value,UriKind.Absolute,out var uri) && uri.Scheme=="https" && uri.UserInfo.Length==0) return uri.AbsoluteUri;
        return null;
    }
}
public sealed class AgentEpoch
{
    private long generation;
    public long Current=>Interlocked.Read(ref generation);
    public void Invalidate()=>Interlocked.Increment(ref generation);
    public bool IsCurrent(long value)=>value==Current;
}
