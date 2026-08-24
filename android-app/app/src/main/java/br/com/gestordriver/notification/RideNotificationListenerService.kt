package br.com.gestordriver.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import br.com.gestordriver.GestorDriverApp

class RideNotificationListenerService : NotificationListenerService() {
    private val processor by lazy {
        RideNotificationProcessor(
            configuracaoProvider = {
                (application as GestorDriverApp).configuracaoStore.carregar()
            },
        )
    }

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
            key = sbn.key,
        )

        when (val evento = processor.processar(notification)) {
            is RideNotificationEvent.CorridaRecebida -> {
                OfertaSessao.registrarOferta(sbn.key)
                RideNotificationBus.publish(evento)
            }

            RideNotificationEvent.CorridaAceita -> {
                OfertaSessao.registrarAceite()
                RideNotificationBus.publish(evento)
            }

            RideNotificationEvent.CorridaExpirada,
            RideNotificationEvent.NotificacaoNaoReconhecida,
            -> {
                RideNotificationBus.publish(evento)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        val chave = sbn?.key ?: return
        if (OfertaSessao.deveExpirar(chave)) {
            OfertaSessao.limpar()
            RideNotificationBus.publish(RideNotificationEvent.CorridaExpirada)
        }
    }
}
