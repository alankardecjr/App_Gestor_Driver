package br.com.gestordriver.notification

import android.content.ComponentName
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

    private val diagnostico by lazy {
        (application as GestorDriverApp).diagnosticLog
    }

    override fun onListenerConnected() {
        activeNotifications?.forEach { onNotificationPosted(it) }
    }

    override fun onListenerDisconnected() {
        requestRebind(ComponentName(this, RideNotificationListenerService::class.java))
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) {
            return
        }
        if (!PlatformDetector.ehSuportada(sbn.packageName.orEmpty())) {
            return
        }

        val notification = NotificationMapper.de(sbn)
        if (notification.fullText.isBlank()) {
            diagnostico.registrar(notification, "VAZIA")
            return
        }
        when (val evento = processor.processar(notification)) {
            is RideNotificationEvent.CorridaRecebida -> {
                OfertaSessao.registrarOferta(sbn.key)
                diagnostico.registrar(notification, "OFERTA")
                RideNotificationBus.publish(evento)
                if (evento.aceiteImediato) {
                    OfertaSessao.registrarAceite()
                    diagnostico.registrar(notification, "ACEITE_IMEDIATO")
                    RideNotificationBus.publish(RideNotificationEvent.CorridaAceita)
                }
            }

            RideNotificationEvent.CorridaAceita -> {
                OfertaSessao.registrarAceite()
                diagnostico.registrar(notification, "ACEITE")
                RideNotificationBus.publish(evento)
            }

            RideNotificationEvent.NotificacaoNaoReconhecida -> {
                diagnostico.registrar(notification, "IGNORADA")
            }

            RideNotificationEvent.CorridaExpirada -> {
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
