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
}
