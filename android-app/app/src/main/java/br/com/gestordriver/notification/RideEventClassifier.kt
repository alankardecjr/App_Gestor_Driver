package br.com.gestordriver.notification

/**
 * Classifica o evento de uma notificação de plataforma.
 *
 * Sem oferta já na sessão, texto parseável só mostra a corrida.
 * Não grava histórico nessa primeira leitura, mesmo que o texto
 * também pareça embarque.
 * Aceite sem oferta na sessão é ignorado: aviso, mapa e promoção
 * não viram corrida.
 * Com oferta na sessão, a tela de embarque grava o histórico.
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
    )

    fun pareceAceite(notification: NotificationData): Boolean {
        if (OfertaTextoFiltro.ehInterfaceGestor(notification.fullText) ||
            OfertaTextoFiltro.ehTelaCancelamento(notification.fullText) ||
            OfertaTextoFiltro.ehTelaDeGanhos(notification.fullText)
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
        if (!ofertaEmAndamento) {
            return if (ofertaParseavel) {
                TipoEventoCorrida.NOVA_OFERTA
            } else {
                TipoEventoCorrida.IGNORADO
            }
        }
        if (pareceAceite(notification)) {
            return TipoEventoCorrida.ACEITE_DETECTADO
        }
        return if (ofertaParseavel) {
            TipoEventoCorrida.NOVA_OFERTA
        } else {
            TipoEventoCorrida.IGNORADO
        }
    }
}
