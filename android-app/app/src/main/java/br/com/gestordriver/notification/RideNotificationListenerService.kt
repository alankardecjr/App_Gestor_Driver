package br.com.gestordriver.notification

import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import br.com.gestordriver.GestorDriverApp
import java.util.concurrent.Executors

class RideNotificationListenerService : NotificationListenerService() {
    private val io = Executors.newSingleThreadExecutor()

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
        val ativas = activeNotifications ?: return
        io.execute {
            ativas.forEach { processarSeguro(it) }
        }
    }

    override fun onListenerDisconnected() {
        requestRebind(ComponentName(this, RideNotificationListenerService::class.java))
    }

    override fun onDestroy() {
        io.shutdownNow()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) {
            return
        }
        io.execute { processarSeguro(sbn) }
    }

    private fun processarSeguro(sbn: StatusBarNotification) {
        if (!PlatformDetector.ehSuportada(sbn.packageName.orEmpty())) {
            return
        }
        runCatching {
            processarNotificacao(sbn)
        }.onFailure {
            diagnostico.registrar(
                NotificationData(
                    packageName = sbn.packageName.orEmpty(),
                    title = "erro",
                    text = it.message.orEmpty(),
                    key = sbn.key,
                ),
                "EXCECAO",
            )
        }
    }

    private val pipeline by lazy {
        RideOfferPipeline(
            processor = processor,
            diagnostico = diagnostico,
        )
    }

    private fun processarNotificacao(sbn: StatusBarNotification) {
        pipeline.processar(NotificationMapper.de(sbn))
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        val chave = sbn?.key ?: return
        if (OfertaSessao.deveExpirar(chave)) {
            OfertaSessao.limparPorChave(chave)
            RideNotificationBus.publish(RideNotificationEvent.CorridaExpirada)
        }
    }
}
