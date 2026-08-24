package br.com.gestordriver.notification

/**
 * Classifica o evento de uma notificação de plataforma.
 *
 * Regra congelada:
 * - uma notificação parseável com valor/km é SEMPRE oferta;
 * - aceite só é emitido com assinatura explícita e sem métricas de oferta;
 * - nenhuma notificação desconhecida vira aceite.
 *
 * Os padrões de aceite abaixo são provisórios até o teste real
 * (Uber / 99 / inDrive) confirmar os textos de cada plataforma.
 */
enum class TipoEventoCorrida {
    NOVA_OFERTA,
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
        "dirija até o passageiro",
        "dirija ate o passageiro",
        "vá buscar o passageiro",
        "va buscar o passageiro",
        "heading to pickup",
        "navigate to pickup",
        "go to pickup",
        "indo buscar o passageiro",
    )

    fun classificar(
        notification: NotificationData,
        ofertaParseavel: Boolean,
    ): TipoEventoCorrida {
        if (ofertaParseavel) {
            return TipoEventoCorrida.NOVA_OFERTA
        }

        val texto = notification.fullText.lowercase()
        val pareceAceite = padroesAceite.any { padrao ->
            texto.contains(padrao)
        }

        return if (pareceAceite) {
            TipoEventoCorrida.ACEITE_DETECTADO
        } else {
            TipoEventoCorrida.IGNORADO
        }
    }
}
