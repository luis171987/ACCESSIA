using System.Security.Cryptography;
namespace Accessia.Windows.Agent;
public static class ModelIntegrity
{
    public static async Task<bool> Verify(string path,long expectedSize,string expectedHash,CancellationToken ct) {
        ct.ThrowIfCancellationRequested();
        if(!File.Exists(path) || new FileInfo(path).Length!=expectedSize) return false;
        await using var stream=File.OpenRead(path);
        return Convert.ToHexString(await SHA256.HashDataAsync(stream,ct)).Equals(expectedHash,StringComparison.OrdinalIgnoreCase);
    }
}
