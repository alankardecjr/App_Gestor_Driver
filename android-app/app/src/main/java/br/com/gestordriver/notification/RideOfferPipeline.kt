package br.com.gestordriver.notification

class RideOfferPipeline(
    private val processor: RideNotificationProcessor,
    private val diagnostico: RegistroDiagnostico,
) {
    fun processar(notification: NotificationData) {
        if (notification.fullText.isBlank()) {
            diagnostico.registrar(notification, "VAZIA")
            return
        }
        if (OfertaTextoFiltro.ehInterfaceGestor(notification.fullText)) {
            diagnostico.registrar(notification, "IGNORADA_UI")
            return
        }
        val evento = runCatching { processor.processar(notification) }.getOrElse { erro ->
            diagnostico.registrar(
                notification.copy(text = erro.message.orEmpty()),
                "EXCECAO",
            )
            return
        }
        when (evento) {
            is RideNotificationEvent.CorridaRecebida -> {
                val assinatura = assinaturaDe(evento)
                if (OfertaSessao.chaveAtiva(notification.packageName) &&
                    OfertaSessao.mesmaOferta(assinatura, notification.packageName) &&
                    !evento.aceiteImediato
                ) {
                    diagnostico.registrar(notification, "OFERTA_IGUAL")
                    return
                }
                OfertaSessao.registrarOferta(notification.key, notification.packageName)
                OfertaSessao.registrarAssinatura(assinatura, notification.packageName)
                diagnostico.registrar(notification, "OFERTA")
                RideNotificationBus.publish(evento)
                if (evento.aceiteImediato) {
                    OfertaSessao.registrarAceite(notification.packageName)
                    diagnostico.registrar(notification, "ACEITE_IMEDIATO")
                    RideNotificationBus.publish(RideNotificationEvent.CorridaAceita)
                }
            }

            RideNotificationEvent.CorridaAceita -> {
                if (OfertaSessao.aceiteDetectado(notification.packageName)) {
                    diagnostico.registrar(notification, "ACEITE_IGUAL")
                    return
                }
                OfertaSessao.registrarAceite(notification.packageName)
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

    private fun assinaturaDe(evento: RideNotificationEvent.CorridaRecebida): String {
        val analise = evento.analise
        return listOf(
            analise.valorTotal,
            analise.kmAtePassageiro,
            analise.kmViagem,
            analise.tempoEstimado,
            analise.notaPassageiro,
        ).joinToString("|")
    }
}
