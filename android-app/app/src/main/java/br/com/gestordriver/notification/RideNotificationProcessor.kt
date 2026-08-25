package br.com.gestordriver.notification

import br.com.gestordriver.core.CalculadoraCorrida
import br.com.gestordriver.core.Corrida
import br.com.gestordriver.core.MotorClassificacao
import br.com.gestordriver.data.paraMotor
import br.com.gestordriver.model.ConfiguracaoUsuario

class RideNotificationProcessor(
    private val parser: CorridaParser = CorridaParser(),
    private val calculadora: CalculadoraCorrida? = null,
    private val configuracaoProvider: () -> ConfiguracaoUsuario = { ConfiguracaoUsuario.padrao() },
    private val ofertaEmAndamento: () -> Boolean = { OfertaSessao.chaveAtiva() },
) {
    fun processar(notification: NotificationData): RideNotificationEvent {
        val parseada = tentarParsearOferta(notification)
        val ofertaParseavel = parseada != null
        return when (
            RideEventClassifier.classificar(
                notification = notification,
                ofertaParseavel = ofertaParseavel,
                ofertaEmAndamento = ofertaEmAndamento(),
            )
        ) {
            TipoEventoCorrida.NOVA_OFERTA,
            TipoEventoCorrida.OFERTA_E_ACEITE,
            -> {
                val (corrida, plataforma) = parseada
                    ?: return RideNotificationEvent.NotificacaoNaoReconhecida
                val analise = calculadoraAtual().calcular(
                    corrida = corrida,
                    plataforma = plataforma.label,
                )
                RideNotificationEvent.CorridaRecebida(
                    analise = analise,
                    aceiteImediato = RideEventClassifier.pareceAceite(notification) &&
                        !ofertaEmAndamento(),
                )
            }

            TipoEventoCorrida.ACEITE_DETECTADO ->
                RideNotificationEvent.CorridaAceita

            TipoEventoCorrida.IGNORADO ->
                RideNotificationEvent.NotificacaoNaoReconhecida
        }
    }

    private fun calculadoraAtual(): CalculadoraCorrida {
        if (calculadora != null) {
            return calculadora
        }
        val configuracao = configuracaoProvider()
        return CalculadoraCorrida(
            classificador = MotorClassificacao.daConfiguracao(configuracao),
            configuracaoUsuario = configuracao.paraMotor(),
        )
    }

    private fun tentarParsearOferta(
        notification: NotificationData,
    ): Pair<Corrida, Plataforma>? {
        return try {
            parser.parseComPlataforma(notification)
        } catch (_: NotificationError) {
            null
        }
    }
}
