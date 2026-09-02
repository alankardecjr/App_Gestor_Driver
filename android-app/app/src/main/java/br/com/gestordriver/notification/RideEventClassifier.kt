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
        "ponto de partida",
        "local de partida",
        "dirija até o local",
        "dirija ate o local",
        "vá até o local de partida",
        "va ate o local de partida",
        "ponto de encontro",
        "encontre o passageiro",
        "encontrar o passageiro",
        "estou no local",
        "cheguei ao ponto",
        "chegar ao ponto",
        "siga até o ponto",
        "siga ate o ponto",
        "dirija até o ponto",
        "dirija ate o ponto",
        "ligar para o passageiro",
        "mensagem para o passageiro",
        "cancelar corrida",
        "deslize para iniciar",
        "deslizar para iniciar",
        "iniciar corrida",
        "passageiro aguardando",
        "aguarde o passageiro",
        "chegue antes",
        "chegada prevista",
        "chegar até",
        "chegar ate",
        "ir para o ponto",
        "ir ao ponto",
        "heading to pickup",
        "navigate to pickup",
        "go to pickup",
        "passenger pickup",
        "on the way to the rider",
        "on the way to pickup",
        "swipe to start",
        "deslize para começar",
        "deslize para comecar",
        "deslize para iniciar",
        "deslizar para iniciar",
        "iniciar viagem",
        "começar viagem",
        "comecar viagem",
        "iniciar corrida",
        "você está a caminho",
        "voce esta a caminho",
        "em viagem",
        "on trip",
        "start trip",
        "encontro com",
        "aceitei por engano",
        "cancelar viagem",
        "continuar viagem",
        "quer cancelar a viagem",
        "local de embarque",
    )

    fun pareceAceite(notification: NotificationData): Boolean {
        if (OfertaTextoFiltro.ehInterfaceGestor(notification.fullText) ||
            OfertaTextoFiltro.ehTelaCancelamento(notification.fullText)
        ) {
            return false
        }
        val texto = notification.fullText.lowercase()
        if (padroesAceite.any { padrao -> texto.contains(padrao) }) {
            return true
        }
        return OfertaTextoFiltro.pareceTelaAposAceiteUber(notification.fullText)
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
