package org.accessia.browser.agent
object VoiceRouting {
 fun isPageCommand(command:String)=command in setOf("pausar","pausar video","pausa el video","continuar","continuar video","reanudar video","reproducir","reproducir video","desactivar estudio automatico","desactivar continuidad","detener estudio automatico","desactivar automatico","cursor siguiente","cursor anterior","seleccionar","leer pagina","bajar","subir","estado","siguiente leccion")
}
