package br.com.gestordriver.notification

import android.content.Context
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NotificationDiagnosticLog(
    context: Context,
) {
    private val arquivo = File(context.filesDir, ARQUIVO)
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun registrar(notification: NotificationData, evento: String) {
        runCatching {
            arquivo.appendText(
                buildString {
                    append(LocalDateTime.now().format(formatter))
                    append('\t')
                    append(evento)
                    append('\t')
                    append(notification.packageName)
                    append('\t')
                    append(notification.title.replace('\n', ' '))
                    append('\n')
                    append(notification.text)
                    append("\n---\n")
                },
            )
            limitarTamanho()
        }
    }

    private fun limitarTamanho() {
        if (!arquivo.exists() || arquivo.length() < LIMITE_BYTES) {
            return
        }
        val texto = arquivo.readText()
        val corte = texto.indexOf("---\n", startIndex = texto.length / 2)
        if (corte >= 0) {
            arquivo.writeText(texto.substring(corte + 4))
        }
    }

    companion object {
        const val ARQUIVO = "notificacoes_diagnostico.txt"
        private const val LIMITE_BYTES = 256 * 1024
    }
}
