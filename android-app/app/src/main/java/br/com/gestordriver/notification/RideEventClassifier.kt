package br.com.gestordriver.notification

/**
 * Classifica o evento de uma notificação de plataforma.
 *
 * Oferta parseável sem assinatura de aceite = nova oferta.
 * Oferta parseável com assinatura de aceite, se já há oferta na sessão = aceite
 * (Uber/99 costumam atualizar a mesma notificação com valor + "a caminho").
 * Oferta parseável com aceite e sessão vazia = mostra a corrida e grava histórico.
 * Aceite explícito sem métricas de oferta = só aceite.
 */
enum class TipoEventoCorrida {
    NOVA_OFERTA,
    OFERTA_E_ACEITE,
    ACEITE_DETECTADO,
    IGNORADO,
}

object RideEventClassifier {

    private val padroesAceite = listOf(
        "viagem aceita",
        "corrida aceita",
        "você aceitou",
        "voce aceitou",
        "you accepted",
        "trip accepted",
        "ride accepted",
        "a caminho do passageiro",
        "a caminho do local",
        "dirija até o passageiro",
        "dirija ate o passageiro",
        "vá buscar o passageiro",
        "va buscar o passageiro",
        "heading to pickup",
        "navigate to pickup",
        "go to pickup",
        "indo buscar o passageiro",
        "indo até o passageiro",
        "indo ate o passageiro",
        "siga para o local",
        "siga até o passageiro",
        "siga ate o passageiro",
        "navegar até o passageiro",
        "navegar ate o passageiro",
        "você confirmou",
        "voce confirmou",
        "corrida em andamento",
        "viagem em andamento",
        "em direção ao passageiro",
        "em direcao ao passageiro",
        "passenger pickup",
        "on the way to the rider",
        "on the way to pickup",
    )

    fun pareceAceite(notification: NotificationData): Boolean {
        val texto = notification.fullText.lowercase()
        return padroesAceite.any { padrao -> texto.contains(padrao) }
    }

    fun classificar(
        notification: NotificationData,
        ofertaParseavel: Boolean,
        ofertaEmAndamento: Boolean = false,
    ): TipoEventoCorrida {
        val aceite = pareceAceite(notification)
        if (ofertaParseavel && aceite && ofertaEmAndamento) {
            return TipoEventoCorrida.ACEITE_DETECTADO
        }
        if (ofertaParseavel && aceite) {
            return TipoEventoCorrida.OFERTA_E_ACEITE
        }
        if (ofertaParseavel) {
            return TipoEventoCorrida.NOVA_OFERTA
        }
        return if (aceite) {
            TipoEventoCorrida.ACEITE_DETECTADO
        } else {
            TipoEventoCorrida.IGNORADO
        }
    }
}
