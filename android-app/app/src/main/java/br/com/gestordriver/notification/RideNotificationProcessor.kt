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
) {
    fun processar(notification: NotificationData): RideNotificationEvent {
        val ofertaParseavel = tentarParsearOferta(notification) != null
        return when (RideEventClassifier.classificar(notification, ofertaParseavel)) {
            TipoEventoCorrida.NOVA_OFERTA -> {
                val parseada = tentarParsearOferta(notification)
                    ?: return RideNotificationEvent.NotificacaoNaoReconhecida
                val (corrida, plataforma) = parseada
                val analise = calculadoraAtual().calcular(
                    corrida = corrida,
                    plataforma = plataforma.label,
                )
                RideNotificationEvent.CorridaRecebida(analise)
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
