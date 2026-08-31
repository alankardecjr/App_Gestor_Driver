package br.com.gestordriver.notification

import android.app.Notification
import android.widget.RemoteViews

object RemoteViewsTexto {
    fun de(notification: Notification): String {
        val partes = linkedSetOf<String>()
        listOfNotNull(
            notification.contentView,
            notification.bigContentView,
            notification.headsUpContentView,
        ).forEach { views ->
            extrair(views).forEach { partes.add(it) }
        }
        notification.actions?.forEach { acao ->
            acao.title?.toString()?.trim()?.takeIf { it.isNotBlank() }?.let { partes.add(it) }
        }
        return partes.joinToString("\n")
    }

    private fun extrair(views: RemoteViews): List<String> {
        val textos = mutableListOf<String>()
        runCatching {
            val campo = RemoteViews::class.java.getDeclaredField("mActions")
            campo.isAccessible = true
            val acoes = campo.get(views) as? Collection<*> ?: return emptyList()
            acoes.forEach { acao ->
                if (acao == null) {
                    return@forEach
                }
                acao.javaClass.declaredFields.forEach { membro ->
                    membro.isAccessible = true
                    val valor = runCatching { membro.get(acao) }.getOrNull()
                    when (valor) {
                        is CharSequence -> {
                            val texto = valor.toString().trim()
                            if (texto.isNotBlank() && texto.length < 400) {
                                textos.add(texto)
                            }
                        }
                        is Array<*> -> valor.forEach { item ->
                            (item as? CharSequence)?.toString()?.trim()
                                ?.takeIf { it.isNotBlank() }
                                ?.let { textos.add(it) }
                        }
                    }
                }
            }
        }
        return textos
    }
}
