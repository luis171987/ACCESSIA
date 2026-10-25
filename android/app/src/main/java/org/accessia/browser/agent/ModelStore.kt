package org.accessia.browser.agent
import java.io.File
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean
import javax.net.ssl.HttpsURLConnection

class ModelStore(private val root:File) {
 companion object {
  const val SIZE=613722264L
  const val SHA="c5f39b4191926786c3e6668b62af4d6eb15248dfc7a5348252a9b406f33f2108"
  const val URL_PIN="https://huggingface.co/litert-community/Qwen3-0.6B/resolve/e6687177e00897731e7716267eebd4eba0dde7c2/Qwen3-0.6B.litertlm"
  fun verify(file:File,size:Long=SIZE,sha:String=SHA):Boolean {
   if(!file.isFile || file.length()!=size) return false
   val hash=MessageDigest.getInstance("SHA-256")
   file.inputStream().use { stream -> val b=ByteArray(65536); while(true) { val n=stream.read(b); if(n<0)break; hash.update(b,0,n) } }
   return hash.digest().joinToString("") { "%02x".format(it) }==sha
  }
 }
 val file=File(root,"qwen3-0.6b.litertlm")
 fun download(cancel:AtomicBoolean) {
  check(!cancel.get()) { "Descarga cancelada" }
  root.mkdirs(); require(root.usableSpace>SIZE+64*1024*1024) { "Espacio privado insuficiente (se requieren unos 680 MB)" }
  val partial=File(root,"model.part")
  try {
   val connection=URL(URL_PIN).openConnection() as HttpsURLConnection
   connection.connectTimeout=30000; connection.readTimeout=30000
   try {
    require(connection.responseCode==200) { "Descarga HTTP ${connection.responseCode}" }
    connection.inputStream.use { input -> partial.outputStream().use { out ->
     val buffer=ByteArray(65536); var total=0L
     while(true) { check(!cancel.get()) { "Descarga cancelada" }; val n=input.read(buffer); if(n<0)break; total+=n; require(total<=SIZE) { "Tamaño del modelo inesperado" }; out.write(buffer,0,n) }
    } }
   } finally { connection.disconnect() }
   check(!cancel.get()) { "Descarga cancelada" }; require(verify(partial)) { "Modelo incompleto o SHA256 incorrecto" }
   check(!cancel.get()) { "Descarga cancelada" }
   check(partial.renameTo(file)) { "No se pudo guardar el modelo verificado" }
  } finally { partial.delete() }
 }
 fun delete() { for (candidate in listOf(file,File(root,"model.part"))) check(!candidate.exists() || candidate.delete()) { "No se pudo borrar el archivo privado ${candidate.name}" } }
}
