package br.com.gestordriver.notification

import android.app.Notification
import android.service.notification.StatusBarNotification

object NotificationMapper {
    fun de(sbn: StatusBarNotification): NotificationData {
        val extras = sbn.notification.extras
        val title = extras.texto(Notification.EXTRA_TITLE)
        val partes = listOf(
            extras.texto(Notification.EXTRA_TEXT),
            extras.texto(Notification.EXTRA_BIG_TEXT),
            extras.texto(Notification.EXTRA_SUB_TEXT),
            extras.texto(Notification.EXTRA_INFO_TEXT),
            extras.linhas(Notification.EXTRA_TEXT_LINES),
        ).filter { it.isNotBlank() }.distinct()

        return NotificationData(
            packageName = sbn.packageName.orEmpty(),
            title = title,
            text = partes.joinToString("\n"),
            key = sbn.key,
        )
    }

    private fun android.os.Bundle.texto(chave: String): String =
        getCharSequence(chave)?.toString().orEmpty().trim()

    private fun android.os.Bundle.linhas(chave: String): String =
        getCharSequenceArray(chave)
            ?.mapNotNull { it?.toString()?.trim() }
            ?.filter { it.isNotBlank() }
            ?.joinToString("\n")
            .orEmpty()
}
