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
        assertEquals(0, campos.quantidadeParadas)
    }

    @Test
    fun deve_extrair_paradas_quando_a_plataforma_informa() {
        val texto = "R$ 38,00 • 3,2 km ate o passageiro • 12,8 km viagem • 24 min • +1 parada"

        val campos = NotificationExtractor.extrairCamposPadrao(texto)

        assertEquals(1, campos.quantidadeParadas)
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

    @Test
    fun deve_aceitar_oferta_so_com_valor() {
        val campos = NotificationExtractor.extrairCamposPadrao("Nova corrida R$ 18,40")
        assertEquals(18.40, campos.valorTotal, 0.001)
        assertEquals(0.0, campos.kmAtePassageiro, 0.001)
        assertEquals(0.0, campos.kmViagem, 0.001)
    }

    @Test
    fun deve_aceitar_nbsp_e_quebra_de_linha() {
        val texto = "R$\u00A018,40\n350 m\n6,5 km\n12 min"
        val campos = NotificationExtractor.extrairCamposPadrao(texto)
        assertEquals(18.40, campos.valorTotal, 0.001)
        assertEquals(0.35, campos.kmAtePassageiro, 0.001)
        assertEquals(6.5, campos.kmViagem, 0.001)
        assertEquals(12, campos.tempoEstimado)
    }

    @Test
    fun card_99_print_nao_usa_rs_por_km_da_plataforma() {
        val texto = """
            Dinheiro
            R${'$'}10,50
            1,3x
            R${'$'}2,70/km
            R${'$'}2,14 Tarifa base dinâmica incl.
            4,95
            79 corridas
            CPF verif.
            6min (971m)
            Arautos do Evangelho, Rua 15 de Janeiro, 249 - Recreio Ipitanga
            8min (2,9km)
            Rua Horto Florestal, 23, Lot. Jardim Metropole
        """.trimIndent()

        val campos = NotificationExtractor.extrairCamposPadrao(texto)
        val corrida = Corrida(
            valorTotal = campos.valorTotal,
            kmAtePassageiro = campos.kmAtePassageiro,
            kmViagem = campos.kmViagem,
            tempoEstimado = campos.tempoEstimado,
        )

        assertEquals(10.50, campos.valorTotal, 0.001)
        assertEquals(0.971, campos.kmAtePassageiro, 0.001)
        assertEquals(2.9, campos.kmViagem, 0.001)
        assertEquals(14, campos.tempoEstimado)
        assertEquals(10.50 / (0.971 + 2.9), corrida.valorPorKm, 0.01)
        assertEquals(4.95, NotificationExtractor.extrairNotaPassageiro(texto)!!, 0.001)
    }

    @Test
    fun nao_usa_tarifa_como_nota() {
        val texto = """
            R${'$'}10,50
            R${'$'}2,14 Tarifa base dinâmica incl.
            4,95
            79 corridas
        """.trimIndent()
        assertEquals(4.95, NotificationExtractor.extrairNotaPassageiro(texto)!!, 0.001)
    }

    @Test
    fun nota_ocr_99_mesma_linha() {
        assertEquals(
            4.93,
            NotificationExtractor.extrairNotaPassageiro("t4,93 · 435 corridas")!!,
            0.001,
        )
        assertEquals(
            4.98,
            NotificationExtractor.extrairNotaPassageiro("t4,98 809 corridas")!!,
            0.001,
        )
    }

    @Test
    fun nota_com_estrela() {
        assertEquals(4.87, NotificationExtractor.extrairNotaPassageiro("⭐ 4,87")!!, 0.001)
        assertEquals(4.9, NotificationExtractor.extrairNotaPassageiro("4.9 ★")!!, 0.001)
    }

    @Test
    fun card_uber_min_km_em_linhas_e_nota() {
        val texto = """
            R${'$'}24,81
            UberX
            4.98
            3 min
            1,2 km
            18 min
            9,4 km
            Aceitar
        """.trimIndent()
        val campos = NotificationExtractor.extrairCamposPadrao(texto)
        assertEquals(24.81, campos.valorTotal, 0.001)
        assertEquals(1.2, campos.kmAtePassageiro, 0.001)
        assertEquals(9.4, campos.kmViagem, 0.001)
        assertEquals(21, campos.tempoEstimado)
        assertEquals(4.98, NotificationExtractor.extrairNotaPassageiro(texto)!!, 0.001)
    }

    @Test
    fun card_uber_km_rotulado_destino() {
        val texto = "R${'$'} 38,00 • 3,2 km ate o passageiro • 12,8 km viagem • 24 min"
        val campos = NotificationExtractor.extrairCamposPadrao(texto)
        assertEquals(3.2, campos.kmAtePassageiro, 0.001)
        assertEquals(12.8, campos.kmViagem, 0.001)
        assertEquals(24, campos.tempoEstimado)
    }

    @Test
    fun card_uber_priority_minutos_e_nota_parenteses() {
        val texto = """
            Priority
            UberX
            R${'$'} 11,74
            R${'$'} 4,70/km aprox.
            4,99 (165)
            +R${'$'} 2,75 incluído
            5 min (1.2 km)
            Avenida Exemplo, 100
            5 minutos (1.3 km)
            Aceitar
            Selecionar
        """.trimIndent()
        val campos = NotificationExtractor.extrairCamposPadrao(texto)
        assertEquals(11.74, campos.valorTotal, 0.001)
        assertEquals(1.2, campos.kmAtePassageiro, 0.001)
        assertEquals(1.3, campos.kmViagem, 0.001)
        assertEquals(10, campos.tempoEstimado)
        assertEquals(4.99, NotificationExtractor.extrairNotaPassageiro(texto)!!, 0.001)
    }

    @Test
    fun bonus_com_mais_rs_nao_e_valor_da_corrida() {
        val texto = """
            R${'$'} 11,74
            + R${'$'} 2,75 incluído
            5 min (1.2 km)
            5 minutos (1.3 km)
        """.trimIndent()
        assertEquals(11.74, NotificationExtractor.extrairValor(texto), 0.001)
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
            packageName = "com.app99.driver",
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

    @Test
    fun promocao_com_reais_nao_e_oferta() {
        val processor = RideNotificationProcessor()
        val evento = processor.processar(
            NotificationData(
                packageName = "com.app99.driver",
                title = "Posto 24h pertinho de você!",
                text = "Até R$60 OFF para encher o tanque com o 99Abastece",
            ),
        )
        assertTrue(evento is RideNotificationEvent.NotificacaoNaoReconhecida)
    }

    @Test
    fun status_conectado_nao_e_oferta() {
        val processor = RideNotificationProcessor()
        val evento = processor.processar(
            NotificationData(
                packageName = "com.app99.driver",
                title = "99 Motorista",
                text = "Você está conectado",
            ),
        )
        assertTrue(evento is RideNotificationEvent.NotificacaoNaoReconhecida)
    }

    @Test
    fun card_99_na_tela_gera_oferta() {
        val processor = RideNotificationProcessor(
            calculadora = CalculadoraCorrida(configuracaoUsuario = ConfiguracaoUsuario.padrao()),
        )
        val evento = processor.processar(
            NotificationData(
                packageName = "com.app99.driver",
                title = "R$ 22,50",
                text = "1,5 km\n7,0 km\n18 min\nAceitar\nRecusar",
            ),
        )
        val analise = (evento as RideNotificationEvent.CorridaRecebida).analise
        assertEquals(22.5, analise.valorTotal, 0.001)
        assertEquals("99", analise.plataforma)
    }

    @Test
    fun card_99_do_print_usa_valor_gestor_nao_taxa_da_plataforma() {
        val processor = RideNotificationProcessor(
            calculadora = CalculadoraCorrida(configuracaoUsuario = ConfiguracaoUsuario.padrao()),
        )
        val evento = processor.processar(
            NotificationData(
                packageName = "com.app99.driver",
                title = "R${'$'}10,50",
                text = """
                    R${'$'}2,70/km
                    R${'$'}2,14 Tarifa base dinâmica incl.
                    4,95
                    79 corridas
                    6min (971m)
                    8min (2,9km)
                """.trimIndent(),
            ),
        )
        val analise = (evento as RideNotificationEvent.CorridaRecebida).analise
        assertEquals(10.50, analise.valorTotal, 0.001)
        assertEquals(0.971, analise.kmAtePassageiro, 0.001)
        assertEquals(2.9, analise.kmViagem, 0.001)
        assertEquals(14, analise.tempoEstimado)
        assertEquals(10.50 / (0.971 + 2.9), analise.valorPorKm, 0.01)
        assertEquals("99", analise.plataforma)
        assertEquals(4.95, analise.notaPassageiro!!, 0.001)
    }

    @Test
    fun tela_99_apos_aceite_grava_evento() {
        val processor = RideNotificationProcessor(
            ofertaEmAndamento = { _ -> true },
        )
        val evento = processor.processar(
            NotificationData(
                packageName = "com.app99.driver",
                title = "Ponto de encontro",
                text = "Estou no local\nLigar para o passageiro",
            ),
        )
        assertTrue(evento is RideNotificationEvent.CorridaAceita)
    }

    @Test
    fun tela_99_conectada_com_ponto_de_encontro_e_aceite() {
        val processor = RideNotificationProcessor(
            ofertaEmAndamento = { _ -> true },
        )
        val evento = processor.processar(
            NotificationData(
                packageName = "com.app99.driver",
                title = "99 Motorista",
                text = "Você está conectado\nPonto de encontro\nEstou no local",
            ),
        )
        assertTrue(evento is RideNotificationEvent.CorridaAceita)
    }

    @Test
    fun tela_99_chegue_antes_grava_aceite() {
        val processor = RideNotificationProcessor(
            ofertaEmAndamento = { _ -> true },
        )
        val evento = processor.processar(
            NotificationData(
                packageName = "com.app99.driver",
                title = "AEROPORTO",
                text = "9 min 2,3 km\nChegue antes de 06:50",
            ),
        )
        assertTrue(evento is RideNotificationEvent.CorridaAceita)
    }
}

class PlatformDetectorTest {
    @Test
    fun reconhece_app_99_motorista() {
        assertEquals(
            Plataforma.NOVE_NOVE,
            PlatformDetector.resolver("com.app99.driver"),
        )
        assertTrue(PlatformDetector.ehSuportada("com.app99.driver"))
    }

    @Test
    fun reconhece_uber_driver() {
        assertEquals(
            Plataforma.UBER,
            PlatformDetector.resolver("com.ubercab.driver"),
        )
    }

    @Test
    fun reconhece_indrive_e_99_alternativos() {
        assertEquals(
            Plataforma.INDRIVE,
            PlatformDetector.resolver("sinet.startup.inDriver"),
        )
        assertEquals(
            Plataforma.NOVE_NOVE,
            PlatformDetector.resolver("com.taxis99"),
        )
        assertTrue(PlatformDetector.ehSuportada("com.sis.android.indriver"))
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
        assertEquals(br.com.gestordriver.core.Classificacao.EXCELENTE, resultado.classificacao)
        assertEquals("#2E7D32", resultado.corClassificacao)
    }
}
