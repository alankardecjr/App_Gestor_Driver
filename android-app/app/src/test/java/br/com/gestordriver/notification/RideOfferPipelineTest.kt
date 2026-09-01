package br.com.gestordriver.notification

import org.junit.Assert.assertEquals
import org.junit.Test

class RideOfferPipelineTest {
    @Test
    fun texto_vazio_nao_quebra() {
        val eventos = mutableListOf<String>()
        val pipeline = RideOfferPipeline(
            processor = RideNotificationProcessor(),
            diagnostico = RegistroDiagnostico { _, evento -> eventos.add(evento) },
        )
        pipeline.processar(NotificationData("x", "", ""))
        assertEquals(listOf("VAZIA"), eventos)
    }

    @Test
    fun excecao_do_processador_vira_registro_e_nao_propaga() {
        val eventos = mutableListOf<String>()
        val pipeline = RideOfferPipeline(
            processor = object : RideNotificationProcessor() {
                override fun processar(notification: NotificationData): RideNotificationEvent {
                    error("falha controlada")
                }
            },
            diagnostico = RegistroDiagnostico { _, evento -> eventos.add(evento) },
        )
        pipeline.processar(
            NotificationData("com.ubercab.driver", "oferta", "R$ 20,00 • 2 km • 8 min"),
        )
        assertEquals(listOf("EXCECAO"), eventos)
    }

    @Test
    fun aceite_imediato_repetido_nao_publica_de_novo() {
        OfertaSessao.limpar()
        val eventos = mutableListOf<String>()
        val analise = br.com.gestordriver.core.CalculadoraCorrida(
            configuracaoUsuario = br.com.gestordriver.core.ConfiguracaoUsuario.padrao(),
        ).calcular(
            corrida = br.com.gestordriver.core.Corrida(
                valorTotal = 20.87,
                kmAtePassageiro = 2.0,
                kmViagem = 15.1,
                tempoEstimado = 23,
            ),
            plataforma = "Uber",
        )
        val pipeline = RideOfferPipeline(
            processor = object : RideNotificationProcessor() {
                override fun processar(notification: NotificationData) =
                    RideNotificationEvent.CorridaRecebida(analise, aceiteImediato = true)
            },
            diagnostico = RegistroDiagnostico { _, evento -> eventos.add(evento) },
        )
        val n = NotificationData("com.ubercab.driver", "Uber", "R$ 20,87")
        pipeline.processar(n)
        pipeline.processar(n)
        assertEquals(listOf("OFERTA", "ACEITE_IMEDIATO", "POS_ACEITE"), eventos)
        OfertaSessao.limpar()
    }
}
