package br.com.gestordriver.notification

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

fun interface RegistroDiagnostico {
    fun registrar(notification: NotificationData, evento: String)
}

class NotificationDiagnosticLog(
    private val context: Context,
) : RegistroDiagnostico {
    private val arquivo = File(context.filesDir, ARQUIVO)
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val io = Executors.newSingleThreadExecutor()

    override fun registrar(notification: NotificationData, evento: String) {
        io.execute {
            runCatching {
                gravar(notification, evento)
            }
        }
    }

    fun registrarCrash(erro: Throwable) {
        runCatching {
            arquivo.appendText(
                buildString {
                    append(LocalDateTime.now().format(formatter))
                    append('\t')
                    append("CRASH")
                    append('\t')
                    append(erro.javaClass.simpleName)
                    append('\n')
                    append(erro.stackTraceToString())
                    append("\n---\n")
                },
            )
            limitarTamanho()
        }
    }

    fun compartilhar(origem: Context) {
        runCatching {
            if (!arquivo.exists() || arquivo.length() == 0L) {
                arquivo.writeText("Sem registros de diagnóstico ainda.\n")
            }
            val uri = FileProvider.getUriForFile(
                origem,
                "${origem.packageName}.files",
                arquivo,
            )
            val envio = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Gestor Driver diagnóstico")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            origem.startActivity(
                Intent.createChooser(envio, "Enviar diagnóstico").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    private fun gravar(notification: NotificationData, evento: String) {
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
