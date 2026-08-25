package br.com.gestordriver.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import br.com.gestordriver.core.CalculadoraCorrida
import br.com.gestordriver.core.ConfiguracaoUsuario
import br.com.gestordriver.core.Corrida

class NotificationExtractorTest {
    @Test
    fun deve_extrair_campos_padrao() {
        val texto = "R$ 38,00 • 3,2 km ate o passageiro • 12,8 km viagem • 24 min"

        val campos = NotificationExtractor.extrairCamposPadrao(texto)

        assertEquals(38.0, campos.valorTotal, 0.001)
        assertEquals(3.2, campos.kmAtePassageiro, 0.001)
        assertEquals(12.8, campos.kmViagem, 0.001)
        assertEquals(24, campos.tempoEstimado)
    }

    @Test
    fun deve_converter_metros_para_km() {
        val texto = "R$ 18,00 • 350 m ate passageiro • 6,5 km viagem • 12 min"

        val campos = NotificationExtractor.extrairCamposPadrao(texto)

        assertEquals(0.35, campos.kmAtePassageiro, 0.001)
        assertEquals(6.5, campos.kmViagem, 0.001)
    }

    @Test
    fun deve_extrair_tempo_em_minutos_por_extenso() {
        val texto = "R$ 20,00 • 2,0 km ate passageiro • 7,0 km viagem • 19 minutos"

        val campos = NotificationExtractor.extrairCamposPadrao(texto)

        assertEquals(19, campos.tempoEstimado)
    }
}

class CorridaParserTest {
    private val parser = CorridaParser()

    @Test
    fun deve_parsear_notificacao_uber() {
        val notification = NotificationData(
            packageName = "com.ubercab.driver",
            title = "Nova viagem disponivel",
            text = "R$ 38,00 • 3,2 km ate o passageiro • 12,8 km viagem • 24 min",
        )

        val corrida = parser.parse(notification)

        assertEquals(38.0, corrida.valorTotal, 0.001)
        assertEquals(3.2, corrida.kmAtePassageiro, 0.001)
        assertEquals(12.8, corrida.kmViagem, 0.001)
        assertEquals(24, corrida.tempoEstimado)
    }

    @Test
    fun deve_parsear_notificacao_99() {
        val notification = NotificationData(
            packageName = "com.taxis99.driver",
            title = "Corrida 99",
            text = "Ganhe R$ 22,50 em 1,5 km + 7,0 km. Tempo estimado: 18 min",
        )

        val corrida = parser.parse(notification)

        assertEquals(22.5, corrida.valorTotal, 0.001)
        assertEquals(1.5, corrida.kmAtePassageiro, 0.001)
        assertEquals(7.0, corrida.kmViagem, 0.001)
        assertEquals(18, corrida.tempoEstimado)
    }

    @Test
    fun deve_parsear_enderecos_quando_presentes() {
        val notification = NotificationData(
            packageName = "com.ubercab.driver",
            title = "Nova viagem",
            text = """
                R$ 38,00 • 3,2 km ate o passageiro • 12,8 km viagem • 24 min
                Origem: Av. Paulista, 1000
                Destino: Rua Augusta, 200
            """.trimIndent(),
        )

        val corrida = parser.parse(notification)

        assertEquals("Av. Paulista, 1000", corrida.enderecoEmbarque)
        assertEquals("Rua Augusta, 200", corrida.enderecoDestino)
    }

    @Test
    fun deve_falhar_para_plataforma_desconhecida() {
        val notification = NotificationData(
            packageName = "com.exemplo.outroapp",
            title = "Oferta",
            text = "R$ 20,00 5 km 10 min",
        )

        try {
            parser.parse(notification)
            error("Deveria falhar")
        } catch (_: UnsupportedPlatform) {
            assertTrue(true)
        }
    }
}

class RideNotificationProcessorTest {
    @Test
    fun deve_gerar_analise_para_notificacao_valida() {
        val processor = RideNotificationProcessor(
            calculadora = CalculadoraCorrida(configuracaoUsuario = ConfiguracaoUsuario.padrao()),
        )

        val evento = processor.processar(
            NotificationData(
                packageName = "com.ubercab.driver",
                title = "Nova viagem disponivel",
                text = "R$ 38,00 • 3,2 km ate o passageiro • 12,8 km viagem • 24 min",
            ),
        )

        val analise = (evento as RideNotificationEvent.CorridaRecebida).analise
        assertEquals(38.0, analise.valorTotal, 0.001)
        assertEquals("Uber", analise.plataforma)
    }

    @Test
    fun deve_retornar_nao_reconhecida_para_plataforma_invalida() {
        val processor = RideNotificationProcessor()

        val evento = processor.processar(
            NotificationData(
                packageName = "com.exemplo.outroapp",
                title = "Oferta",
                text = "R$ 20,00 5 km 10 min",
            ),
        )

        assertTrue(evento is RideNotificationEvent.NotificacaoNaoReconhecida)
    }

    @Test
    fun deve_detectar_aceite_sem_tratar_como_oferta() {
        val processor = RideNotificationProcessor()
        val evento = processor.processar(
            NotificationData(
                packageName = "com.ubercab.driver",
                title = "Viagem aceita",
                text = "Dirija ate o passageiro",
            ),
        )
        assertTrue(evento is RideNotificationEvent.CorridaAceita)
    }

    @Test
    fun oferta_com_texto_de_aceite_marca_aceite_imediato() {
        val processor = RideNotificationProcessor()
        val evento = processor.processar(
            NotificationData(
                packageName = "com.ubercab.driver",
                title = "R$ 38,00 • 3,2 km ate o passageiro • 12,8 km viagem • 24 min",
                text = "Dirija ate o passageiro",
            ),
        )
        val recebida = evento as RideNotificationEvent.CorridaRecebida
        assertTrue(recebida.aceiteImediato)
    }
}

class CalculadoraCorridaTest {
    @Test
    fun deve_calcular_analise_consolidada() {
        val calculadora = CalculadoraCorrida(configuracaoUsuario = ConfiguracaoUsuario.padrao())
        val corrida = Corrida(
            valorTotal = 38.0,
            kmAtePassageiro = 3.2,
            kmViagem = 12.8,
            tempoEstimado = 24,
        )

        val resultado = calculadora.calcular(corrida)

        assertEquals(16.0, resultado.kmTotal, 0.001)
        assertEquals(2.375, resultado.valorPorKm, 0.001)
        assertEquals(1.28, resultado.combustivelEstimado!!, 0.01)
        assertEquals(7.9232, resultado.custoCombustivel!!, 0.01)
        assertEquals(br.com.gestordriver.core.Classificacao.BOA, resultado.classificacao)
        assertEquals("#7CB342", resultado.corClassificacao)
    }
}
