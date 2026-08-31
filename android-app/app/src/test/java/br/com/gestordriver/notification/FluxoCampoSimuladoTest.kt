package br.com.gestordriver.notification

import br.com.gestordriver.core.CalculadoraCorrida
import br.com.gestordriver.core.ConfiguracaoUsuario
import br.com.gestordriver.ui.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Textos reais de campo (A14): card 99 do print e card Uber Priority/UberX.
 */
class FluxoCampoSimuladoTest {

    private val calculadora = CalculadoraCorrida(
        configuracaoUsuario = ConfiguracaoUsuario.padrao(),
    )

    @Before
    fun limparSessao() {
        OfertaSessao.limpar()
    }

    @Test
    fun fluxo_99_oferta_nao_grava_historico_ate_ponto_de_encontro() {
        val processor = RideNotificationProcessor(
            calculadora = calculadora,
            ofertaEmAndamento = { OfertaSessao.chaveAtiva(it) },
        )
        val oferta = NotificationData(
            packageName = "com.app99.driver",
            title = "R\$10,50",
            text = """
                R${'$'}2,70/km
                4,95
                79 corridas
                6min (971m)
                8min (2,9km)
                Aceitar
                Recusar
            """.trimIndent(),
        )
        val recebida = processor.processar(oferta) as RideNotificationEvent.CorridaRecebida
        assertFalse(recebida.aceiteImediato)
        assertEquals(10.50, recebida.analise.valorTotal, 0.001)
        assertEquals(0.971, recebida.analise.kmAtePassageiro, 0.001)
        assertEquals(2.9, recebida.analise.kmViagem, 0.001)
        assertEquals("99", recebida.analise.plataforma)
        assertFalse(
            RideEventClassifier.pareceAceite(oferta),
        )

        OfertaSessao.registrarOferta("tela:99", "com.app99.driver")
        val aceite = processor.processar(
            NotificationData(
                packageName = "com.app99.driver",
                title = "Ponto de encontro",
                text = "Estou no local\nLigar para o passageiro",
            ),
        )
        assertTrue(aceite is RideNotificationEvent.CorridaAceita)
    }

    @Test
    fun fluxo_uber_card_priority_parseia_e_botao_aceitar_nao_e_historico() {
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
        val processor = RideNotificationProcessor(calculadora = calculadora)
        val evento = processor.processar(
            NotificationData(
                packageName = "com.ubercab.driver",
                title = "Priority",
                text = texto,
            ),
        ) as RideNotificationEvent.CorridaRecebida
        assertFalse(evento.aceiteImediato)
        assertEquals(11.74, evento.analise.valorTotal, 0.001)
        assertEquals(1.2, evento.analise.kmAtePassageiro, 0.001)
        assertEquals(1.3, evento.analise.kmViagem, 0.001)
        assertEquals(4.99, evento.analise.notaPassageiro!!, 0.001)
        assertEquals("Uber", evento.analise.plataforma)
        assertFalse(OfertaTextoFiltro.ehMapaSemCard(texto))
        assertFalse(OfertaTextoFiltro.cardKmIncompleto(texto))
    }

    @Test
    fun fluxo_uber_mapa_online_expira_e_nao_e_oferta() {
        val mapa = """
            Você está online
            +R${'$'} 2,75
            1-3 min
        """.trimIndent()
        assertTrue(OfertaTextoFiltro.ehMapaSemCard(mapa))
        assertFalse(OfertaTextoFiltro.pareceOferta(mapa))
        assertEquals(
            TransicaoTelaOferta.EXPIRAR,
            OfertaTelaTransicao.decidir(mapa, leiturasSemOferta = 1, pacote = "com.ubercab.driver"),
        )
        val processor = RideNotificationProcessor(calculadora = calculadora)
        assertTrue(
            processor.processar(
                NotificationData(
                    packageName = "com.ubercab.driver",
                    title = "Uber",
                    text = mapa,
                ),
            ) is RideNotificationEvent.NotificacaoNaoReconhecida,
        )
    }

    @Test
    fun fluxo_uber_aceite_apos_oferta_grava_no_viewmodel_mesmo_se_compacta_expirou() {
        val processor = RideNotificationProcessor(calculadora = calculadora)
        val oferta = processor.processar(
            NotificationData(
                packageName = "com.ubercab.driver",
                title = "Priority",
                text = """
                    R${'$'} 11,74
                    5 min (1.2 km)
                    5 minutos (1.3 km)
                    Aceitar
                """.trimIndent(),
            ),
        ) as RideNotificationEvent.CorridaRecebida
        val viewModel = AppViewModel(
            coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
        )
        viewModel.aplicarNovaCorrida(oferta.analise)
        viewModel.expirarOfertaAtual()
        assertTrue(viewModel.state.historico.isEmpty())

        val posAceite = NotificationData(
            packageName = "com.ubercab.driver",
            title = "Avenida",
            text = "Maria\n4,88\nUberX\nAceitei por engano\nlocal de partida",
        )
        assertTrue(RideEventClassifier.pareceAceite(posAceite))
        viewModel.registrarAceiteCorrida()
        assertEquals(1, viewModel.state.historico.size)
        assertEquals(11.74, viewModel.state.historico.first().valorTotal, 0.001)
    }

    @Test
    fun overlay_gestor_e_menu_99_nao_quebram_sessao() {
        val overlay = """
            DATA | HORA | Nenhuma corrida aceita
            HISTÓRICO
        """.trimIndent()
        assertTrue(OfertaTextoFiltro.ehInterfaceGestor(overlay))
        assertFalse(
            RideEventClassifier.pareceAceite(
                NotificationData("com.ubercab.driver", "", overlay),
            ),
        )
        assertEquals(
            TransicaoTelaOferta.AGUARDAR,
            OfertaTelaTransicao.decidir(
                "Política de cancelamento\nContinuar conectado",
                leiturasSemOferta = 4,
                pacote = "com.app99.driver",
            ),
        )
    }
}
