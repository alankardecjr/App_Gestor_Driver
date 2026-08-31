package br.com.gestordriver.notification

import android.app.Notification
import android.os.Bundle
import android.service.notification.StatusBarNotification

object NotificationMapper {
    private val extrasIgnoradas = setOf(
        "android.template",
        "android.rebuild.iml",
        "android.support.v4.app.extra.COMPAT_TEMPLATE",
    )

    fun de(sbn: StatusBarNotification): NotificationData {
        val notification = sbn.notification
        val extras = notification.extras
        val title = listOf(
            extras.texto(Notification.EXTRA_TITLE),
            extras.texto(Notification.EXTRA_TITLE_BIG),
            extras.texto(Notification.EXTRA_CONVERSATION_TITLE),
            notification.tickerText?.toString().orEmpty().trim(),
        ).firstOrNull { it.isNotBlank() }.orEmpty()

        val partes = linkedSetOf<String>()
        listOf(
            extras.texto(Notification.EXTRA_TEXT),
            extras.texto(Notification.EXTRA_BIG_TEXT),
            extras.texto(Notification.EXTRA_SUB_TEXT),
            extras.texto(Notification.EXTRA_INFO_TEXT),
            extras.texto(Notification.EXTRA_SUMMARY_TEXT),
            extras.linhas(Notification.EXTRA_TEXT_LINES),
            mensagens(extras),
            notification.tickerText?.toString().orEmpty().trim(),
        ).filter { it.isNotBlank() }.forEach { partes.add(it) }

        coletarTextos(extras).forEach { partes.add(it) }
        RemoteViewsTexto.de(notification)
            .split('\n')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { partes.add(it) }

        return NotificationData(
            packageName = sbn.packageName.orEmpty(),
            title = title,
            text = partes.filter { it != title }.joinToString("\n"),
            key = sbn.key,
        )
    }

    private fun mensagens(extras: Bundle): String {
        val itens = extras.getParcelableArray(Notification.EXTRA_MESSAGES) ?: return ""
        return itens.mapNotNull { item ->
            val texto = when (item) {
                is Bundle -> item.getCharSequence("text")?.toString()
                else -> null
            }
            texto?.trim()?.takeIf { it.isNotBlank() }
        }.joinToString("\n")
    }

    private fun coletarTextos(extras: Bundle, profundidade: Int = 0): List<String> {
        if (profundidade > 3) {
            return emptyList()
        }
        val textos = mutableListOf<String>()
        for (chave in extras.keySet()) {
            if (chave in extrasIgnoradas) {
                continue
            }
            when (val valor = runCatching { extras.get(chave) }.getOrNull()) {
                is CharSequence -> {
                    val texto = valor.toString().trim()
                    if (texto.isNotBlank() && texto.length < 400) {
                        textos.add(texto)
                    }
                }
                is Bundle -> textos.addAll(coletarTextos(valor, profundidade + 1))
                is Array<*> -> {
                    valor.mapNotNull { item ->
                        (item as? CharSequence)?.toString()?.trim()?.takeIf { it.isNotBlank() }
                    }.joinToString("\n").takeIf { it.isNotBlank() }?.let { textos.add(it) }
                }
            }
        }
        return textos
    }

    private fun Bundle.texto(chave: String): String =
        getCharSequence(chave)?.toString().orEmpty().trim()

    private fun Bundle.linhas(chave: String): String =
        getCharSequenceArray(chave)
            ?.mapNotNull { it?.toString()?.trim() }
            ?.filter { it.isNotBlank() }
            ?.joinToString("\n")
            .orEmpty()
}
