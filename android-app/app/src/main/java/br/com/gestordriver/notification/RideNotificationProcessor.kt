package br.com.gestordriver.notification

import br.com.gestordriver.core.CalculadoraCorrida
import br.com.gestordriver.core.ConfiguracaoUsuario

class RideNotificationProcessor(
    private val parser: CorridaParser = CorridaParser(),
    private val calculadora: CalculadoraCorrida = CalculadoraCorrida(
        configuracaoUsuario = ConfiguracaoUsuario.padrao(),
    ),
) {
    fun processar(notification: NotificationData): RideNotificationEvent {
        return try {
            val (corrida, plataforma) = parser.parseComPlataforma(notification)
            val analise = calculadora.calcular(
                corrida = corrida,
                plataforma = plataforma.label,
            )
            RideNotificationEvent.CorridaRecebida(analise)
        } catch (_: NotificationError) {
            RideNotificationEvent.NotificacaoNaoReconhecida
        }
    }
}
