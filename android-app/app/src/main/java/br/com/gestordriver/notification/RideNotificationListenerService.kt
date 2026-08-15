package br.com.gestordriver.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class RideNotificationListenerService : NotificationListenerService() {
    private val processor = RideNotificationProcessor()

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) {
            return
        }

        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val text = extras.getCharSequence("android.text")?.toString().orEmpty()

        if (title.isBlank() && text.isBlank()) {
            return
        }

        val notification = NotificationData(
            packageName = sbn.packageName,
            title = title,
            text = text,
        )

        val evento = processor.processar(notification)
        RideNotificationBus.publish(evento)
    }
}
