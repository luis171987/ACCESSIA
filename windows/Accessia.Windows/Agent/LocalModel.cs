using System.Security.Cryptography;
using System.Text;
using LLama;
using LLama.Common;
using LLama.Sampling;
namespace Accessia.Windows.Agent;
public sealed class LocalModel
{
    public const long Bytes=639446688;
    public const string Sha256="9465e63a22add5354d9bb4b99e90117043c7124007664907259bd16d043bb031";
    public const string Url="https://huggingface.co/Qwen/Qwen3-0.6B-GGUF/resolve/23749fefcc72300e3a2ad315e1317431b06b590a/Qwen3-0.6B-Q8_0.gguf";
    public static string DirectoryPath=>Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),"ACCESSIA","models");
    public static string ModelPath=>Path.Combine(DirectoryPath,"Qwen3-0.6B-Q8_0.gguf");
    public static Task<bool> Verified(string path,CancellationToken ct)=>ModelIntegrity.Verify(path,Bytes,Sha256,ct);
    public async Task Download(CancellationToken ct) {
        Directory.CreateDirectory(DirectoryPath);
        if(new DriveInfo(Path.GetPathRoot(DirectoryPath)!).AvailableFreeSpace<Bytes+256L*1024*1024) throw new IOException("Espacio insuficiente: se necesitan al menos 900 MB libres.");
        var partial=ModelPath+".partial";
        try {
            using var client=new HttpClient {Timeout=Timeout.InfiniteTimeSpan};
            using var response=await client.GetAsync(Url,HttpCompletionOption.ResponseHeadersRead,ct);response.EnsureSuccessStatusCode();
            if(response.Content.Headers.ContentLength is long size && size!=Bytes) throw new IOException("Tamaño remoto incorrecto.");
            await using(var input=await response.Content.ReadAsStreamAsync(ct))
            await using(var output=new FileStream(partial,FileMode.Create,FileAccess.Write,FileShare.None,81920,true)) {
                byte[] buffer=new byte[81920]; long written=0;int length;
                while((length=await input.ReadAsync(buffer,ct))>0) {written+=length;if(written>Bytes) throw new IOException("Modelo excede tamaño permitido.");await output.WriteAsync(buffer.AsMemory(0,length),ct);}
            }
            if(!await Verified(partial,ct)) throw new IOException("El modelo no supera la verificación SHA256/tamaño.");
            ct.ThrowIfCancellationRequested();File.Move(partial,ModelPath,true);
        } finally {if(File.Exists(partial)) File.Delete(partial);}
    }
    public async Task<string> Infer(string input,CancellationToken ct)=>AgentTools.Proposal(await InferRaw(input,ct));
    internal async Task<string> InferRaw(string input,CancellationToken ct) {
        if(!await Verified(ModelPath,ct)) throw new IOException("Descarga o verifica el modelo desde Asistente local.");
        var system=await File.ReadAllTextAsync(Path.Combine(AppContext.BaseDirectory,"assets","agent-prompt.txt"),ct);
        var descriptors=await File.ReadAllTextAsync(Path.Combine(AppContext.BaseDirectory,"assets","agent-tools.json"),ct);
        system=AgentTools.SystemPrompt(system,descriptors);
        return await Task.Run(async ()=>{
            var parameters=new ModelParams(ModelPath) {ContextSize=8192,GpuLayerCount=0};
            using var weights=LLamaWeights.LoadFromFile(parameters);
            ct.ThrowIfCancellationRequested();
            var executor=new StatelessExecutor(weights,parameters) {ApplyTemplate=false};
            // JSON page data must not inject tokenizer role/control tokens.
            input=input.Replace("<",@"\u003C");
            var prompt="<|im_start|>system\n"+system+"<|im_end|>\n<|im_start|>user\n"+input+"\n/no_think<|im_end|>\n<|im_start|>assistant\n<think>\n\n</think>\n\n";
            var inference=new InferenceParams {MaxTokens=256,AntiPrompts=new List<string>{"<|im_end|>"},DecodeSpecialTokens=true,SamplingPipeline=new DefaultSamplingPipeline {Temperature=0f}};
            var result=new StringBuilder();
            await foreach(var token in executor.InferAsync(prompt,inference,ct)) {result.Append(token);if(result.Length>8192) throw new InvalidOperationException("Respuesta demasiado larga.");}
            return result.ToString();
        },ct);
    }
}
