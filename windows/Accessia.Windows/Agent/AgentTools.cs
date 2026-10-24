using System.Text.Json;
using System.Text.Json.Nodes;
using System.Text.RegularExpressions;
namespace Accessia.Windows.Agent;
public static class AgentTools
{
    public static readonly JsonSerializerOptions ModelJson=new() {Encoder=System.Text.Encodings.Web.JavaScriptEncoder.UnsafeRelaxedJsonEscaping};
    private static readonly Dictionary<string,string[]> Arguments=new() {
        ["read"]=[],["scroll_up"]=[],["scroll_down"]=[],["focus"]=["target"],["click"]=["target"],
        ["fill"]=["target","value"],["search"]=["value"],["open"]=["target"],["answer"]=["text","citations"],["clarify"]=["text"]
    };
    public static string SystemPrompt(string instruction,string descriptors) {
        var array=JsonNode.Parse(descriptors) as JsonArray??throw new ArgumentException("Herramientas inválidas.");
        var tools=string.Join("\n",array.Select(tool=>new JsonObject {["type"]="function",["function"]=tool!.DeepClone()}.ToJsonString(ModelJson)));
        return instruction+"\n\n# Tools\n\nYou may call one or more functions to assist with the user query.\n\nYou are provided with function signatures within <tools></tools> XML tags:\n<tools>\n"+tools+"\n</tools>\n\nFor each function call, return a json object with function name and arguments within <tool_call></tool_call> XML tags:\n<tool_call>\n{\"name\": <function-name>, \"arguments\": <args-json-object>}\n</tool_call>";
    }
    public static string Proposal(string output) {
        if(output.Length>8192) throw new ArgumentException("Respuesta demasiado larga.");
        output=Regex.Replace(output,@"^\s*<think>\s*</think>\s*","").Trim();
        const string start="<tool_call>",end="</tool_call>";
        if(!output.StartsWith(start,StringComparison.Ordinal) || !output.EndsWith(end,StringComparison.Ordinal)) throw new ArgumentException("Se requiere exactamente una llamada a herramienta.");
        var body=output[start.Length..^end.Length];
        if(body.Contains(start,StringComparison.Ordinal) || body.Contains(end,StringComparison.Ordinal)) throw new ArgumentException("Solo una herramienta por turno.");
        JsonObject call;
        try {call=JsonNode.Parse(body) as JsonObject??throw new ArgumentException("Herramienta inválida.");}
        catch(JsonException e){throw new ArgumentException("Herramienta JSON inválida.",e);}
        if(call.Count!=2 || call["name"] is not JsonValue nameValue || !nameValue.TryGetValue<string>(out var name) || !Arguments.TryGetValue(name,out var fields) || call["arguments"] is not JsonObject args) throw new ArgumentException("Herramienta desconocida o inválida.");
        if(args.Count!=fields.Length || fields.Any(key=>!args.ContainsKey(key))) throw new ArgumentException("Argumentos no permitidos.");
        var result=new JsonObject {["action"]=name};foreach(var key in fields) result[key]=args[key]?.DeepClone();
        // Apply the same strict type and length bounds before returning a proposal.
        var validated=AgentPolicy.Normalize(result.ToJsonString(),"host-pending");validated.Remove("snapshot");return validated.ToJsonString();
    }
}
