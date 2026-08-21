package br.com.gestordriver.ui

import br.com.gestordriver.core.AnaliseCorrida
import br.com.gestordriver.core.Classificacao
import br.com.gestordriver.core.Corrida
import br.com.gestordriver.model.ModoApresentacao
import br.com.gestordriver.model.PlanoAcesso
import br.com.gestordriver.notification.RideNotificationEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppViewModelTest {

    private fun analiseTeste():
        AnaliseCorrida {

        return AnaliseCorrida(

            corrida = Corrida(
                valorTotal = 38.10,
                kmAtePassageiro = 3.21,
                kmViagem = 12.82,
                tempoEstimado = 24,
            ),

            valorTotal = 38.10,

            kmAtePassageiro = 3.21,

            kmViagem = 12.82,

            tempoEstimado = 24,

            notaPassageiro = 4.98,

            plataforma = "Uber",

            dataHora = null,

            kmTotal = 16.03,

            valorPorKm = 2.38,

            combustivelEstimado = 1.28,

            custoCombustivel = 7.92,

            classificacao =
                Classificacao.BOA,

            corClassificacao =
                "#7CB342",
        )
    }

    @Test
    fun estado_inicial_deve_ser_selo_com_historico_vazio() {

        val viewModel =
            AppViewModel()

        assertTrue(
            viewModel.state
                .historico
                .isEmpty()
        )

        assertTrue(
            viewModel.state
                .seloFlutuante
        )

        assertTrue(
            viewModel.state
                .interfaceOculta
        )

        assertFalse(
            viewModel.state
                .overlayAtivo
        )

        assertTrue(
            viewModel.state
                .monitorando
        )
    }

    @Test
    fun nova_oferta_nao_entra_no_historico() {

        val viewModel =
            AppViewModel()

        viewModel.processarEvento(

            RideNotificationEvent.CorridaRecebida(
                analiseTeste()
            )
        )

        assertTrue(
            viewModel.state
                .historico
                .isEmpty()
        )

        assertEquals(
            ModoApresentacao.COMPACTA,
            viewModel.state
                .corrida
                .modo
        )

        assertFalse(
            viewModel.state
                .interfaceOculta
        )

        assertFalse(
            viewModel.state
                .seloFlutuante
        )

        assertTrue(
            viewModel.state
                .overlayAtivo
        )
    }

    @Test
    fun expandir_e_retrair_corrida() {

        val viewModel =
            AppViewModel()

        viewModel.processarEvento(

            RideNotificationEvent.CorridaRecebida(
                analiseTeste()
            )
        )

        viewModel.alternarDetalhes()

        assertEquals(
            ModoApresentacao.DETALHES,
            viewModel.state
                .corrida
                .modo
        )

        assertEquals(
            "Menos detalhes",
            viewModel.state
                .corrida
                .acaoDetalhes
        )

        viewModel.alternarDetalhes()

        assertEquals(
            ModoApresentacao.COMPACTA,
            viewModel.state
                .corrida
                .modo
        )

        assertEquals(
            "ⓘ",
            viewModel.state
                .corrida
                .acaoDetalhes
        )
    }

    @Test
    fun historico_so_abre_na_tela_expandida() {

        val viewModel =
            AppViewModel()

        // Compacta: não abre.
        viewModel.alternarHistorico()

        assertFalse(
            viewModel.state
                .historicoVisivel
        )

        // Oferta recebida.
        viewModel.processarEvento(

            RideNotificationEvent.CorridaRecebida(
                analiseTeste()
            )
        )

        // Expande.
        viewModel.alternarDetalhes()

        // Agora pode abrir.
        viewModel.alternarHistorico()

        assertTrue(
            viewModel.state
                .historicoVisivel
        )
    }

    @Test
    fun ocultar_fecha_historico_e_configuracao() {

        val viewModel =
            AppViewModel()

        viewModel.processarEvento(

            RideNotificationEvent.CorridaRecebida(
                analiseTeste()
            )
        )

        viewModel.alternarDetalhes()

        viewModel.abrirConfiguracoes()

        viewModel.ocultarInterface()

        assertTrue(
            viewModel.state
                .interfaceOculta
        )

        assertTrue(
            viewModel.state
                .seloFlutuante
        )

        assertFalse(
            viewModel.state
                .historicoVisivel
        )

        assertFalse(
            viewModel.state
                .configuracoesVisivel
        )

        assertEquals(
            ModoApresentacao.COMPACTA,
            viewModel.state
                .corrida
                .modo
        )

        assertTrue(
            viewModel.state
                .monitorando
        )
    }

    @Test
    fun selo_reabre_somente_tela_compacta() {

        val viewModel =
            AppViewModel()

        viewModel.processarEvento(

            RideNotificationEvent.CorridaRecebida(
                analiseTeste()
            )
        )

        viewModel.alternarDetalhes()

        viewModel.alternarHistorico()

        viewModel.ocultarInterface()

        viewModel.reabrirInterface()

        assertFalse(
            viewModel.state
                .interfaceOculta
        )

        assertFalse(
            viewModel.state
                .seloFlutuante
        )

        assertFalse(
            viewModel.state
                .historicoVisivel
        )

        assertFalse(
            viewModel.state
                .configuracoesVisivel
        )

        assertEquals(
            ModoApresentacao.COMPACTA,
            viewModel.state
                .corrida
                .modo
        )

        assertTrue(
            viewModel.state
                .monitorando
        )
    }

    @Test
    fun cancelar_fechar_nao_altera_monitoramento() {

        val viewModel =
            AppViewModel()

        viewModel.solicitarFecharApp()

        assertTrue(
            viewModel.state
                .confirmacaoFecharVisivel
        )

        viewModel.cancelarFecharApp()

        assertFalse(
            viewModel.state
                .confirmacaoFecharVisivel
        )

        assertTrue(
            viewModel.state
                .monitorando
        )
    }

    @Test
    fun confirmar_fechar_para_monitoramento() {

        val viewModel =
            AppViewModel()

        viewModel.solicitarFecharApp()

        viewModel.confirmarFecharApp()

        assertFalse(
            viewModel.state
                .confirmacaoFecharVisivel
        )

        assertFalse(
            viewModel.state
                .monitorando
        )

        assertFalse(
            viewModel.state
                .seloFlutuante
        )

        assertFalse(
            viewModel.state
                .overlayAtivo
        )
    }

    @Test
    fun selecionar_plano_preserva_historico_vazio() {

        val viewModel =
            AppViewModel()

        viewModel.selecionarPlano(
            PlanoAcesso.PRO
        )

        assertEquals(
            PlanoAcesso.PRO,
            viewModel.state.plano
        )

        assertTrue(
            viewModel.state
                .historico
                .isEmpty()
        )

        assertTrue(
            viewModel.state
                .monitorando
        )
    }
}