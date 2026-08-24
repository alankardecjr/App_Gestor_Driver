package br.com.gestordriver.notification

import java.time.LocalDateTime

data class NotificationData(
    val packageName: String,
    val title: String,
    val text: String,
    val key: String? = null,
    val receivedAt: LocalDateTime = LocalDateTime.now(),
) {
    val fullText: String
        get() = listOf(title, text)
            .filter { it.isNotBlank() }
            .joinToString("\n")
}
