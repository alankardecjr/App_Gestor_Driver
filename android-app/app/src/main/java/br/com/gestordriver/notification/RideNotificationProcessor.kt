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
    private val ofertaEmAndamento: (String) -> Boolean = { OfertaSessao.chaveAtiva(it) },
) {
    fun processar(notification: NotificationData): RideNotificationEvent {
        if (!RideEventClassifier.pareceAceite(notification) &&
            OfertaTextoFiltro.ehPromocaoOuStatus(notification.fullText)
        ) {
            return RideNotificationEvent.NotificacaoNaoReconhecida
        }
        val parseada = tentarParsearOferta(notification)
        val ofertaParseavel = parseada != null
        return when (
            RideEventClassifier.classificar(
                notification = notification,
                ofertaParseavel = ofertaParseavel,
                ofertaEmAndamento = ofertaEmAndamento(notification.packageName),
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
                    notaPassageiro = NotificationExtractor.extrairNotaPassageiro(
                        notification.fullText,
                    ),
                )
                RideNotificationEvent.CorridaRecebida(
                    analise = analise,
                    aceiteImediato = RideEventClassifier.pareceAceite(notification) &&
                        !ofertaEmAndamento(notification.packageName),
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
