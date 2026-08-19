package br.com.gestordriver.ui

import br.com.gestordriver.model.ModoApresentacao
import br.com.gestordriver.model.PlanoAcesso
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppViewModelTest {

    // =====================================================================
    // ETAPA 1
    // NOVA OFERTA NÃO DEVE ENTRAR AUTOMATICAMENTE NO HISTÓRICO
    // =====================================================================

    @Test
    fun historico_inicial_deve_estar_vazio() {

        val viewModel =
            AppViewModel()

        assertTrue(
            viewModel.state.historico.isEmpty()
        )
    }

    // =====================================================================
    // INTERFACE
    // =====================================================================

    @Test
    fun deve_alternar_detalhes_e_historico() {

        val viewModel =
            AppViewModel()

        assertEquals(
            ModoApresentacao.COMPACTA,
            viewModel.state.corrida.modo,
        )

        assertFalse(
            viewModel.state.historicoVisivel
        )

        viewModel.alternarDetalhes()

        assertEquals(
            ModoApresentacao.DETALHES,
            viewModel.state.corrida.modo,
        )

        assertEquals(
            "Menos detalhes",
            viewModel.state.corrida.acaoDetalhes,
        )

        viewModel.alternarHistorico()

        assertTrue(
            viewModel.state.historicoVisivel
        )
    }

    // =====================================================================
    // PLANO / OVERLAY
    // =====================================================================

    @Test
    fun deve_selecionar_plano_e_controlar_overlay() {

        val viewModel =
            AppViewModel()

        viewModel.selecionarPlano(
            PlanoAcesso.PRO
        )

        assertEquals(
            PlanoAcesso.PRO,
            viewModel.state.plano,
        )

        assertEquals(
            "Ativo",
            viewModel
                .state
                .corrida
                .camposDetalhes
                .last()
                .valor,
        )

        viewModel.semNotificacao()

        assertFalse(
            viewModel.state.notificacaoDisponivel
        )

        assertTrue(
            viewModel.state.seloFlutuante
        )

        assertTrue(
            viewModel.state.monitorando
        )

        viewModel.reabrirInterface()

        assertFalse(
            viewModel.state.interfaceOculta
        )

        assertFalse(
            viewModel.state.seloFlutuante
        )
    }

    // =====================================================================
    // OCULTAR
    // =====================================================================

    @Test
    fun ocultar_mantem_monitoramento() {

        val viewModel =
            AppViewModel()

        viewModel.alternarDetalhes()
        viewModel.alternarHistorico()

        viewModel.ocultarInterface()

        assertTrue(
            viewModel.state.interfaceOculta
        )

        assertTrue(
            viewModel.state.seloFlutuante
        )

        assertTrue(
            viewModel.state.monitorando
        )

        assertFalse(
            viewModel.state.historicoVisivel
        )

        assertFalse(
            viewModel.state.configuracoesVisivel
        )
    }

    // =====================================================================
    // FECHAMENTO
    // =====================================================================

    @Test
    fun fechar_para_monitoramento() {

        val viewModel =
            AppViewModel()

        viewModel.solicitarFecharApp()

        assertTrue(
            viewModel.state.confirmacaoFecharVisivel
        )

        viewModel.confirmarFecharApp()

        assertFalse(
            viewModel.state.monitorando
        )

        assertFalse(
            viewModel.state.confirmacaoFecharVisivel
        )
    }

    // =====================================================================
    // CONFIGURAÇÕES
    // =====================================================================

    @Test
    fun config_e_historico_sao_exclusivos() {

        val viewModel =
            AppViewModel()

        viewModel.alternarDetalhes()
        viewModel.abrirConfiguracoes()

        assertTrue(
            viewModel.state.configuracoesVisivel
        )

        viewModel.alternarHistorico()

        assertTrue(
            viewModel.state.historicoVisivel
        )

        assertFalse(
            viewModel.state.configuracoesVisivel
        )
    }

    // =====================================================================
    // OCULTAR CONFIGURAÇÃO
    // =====================================================================

    @Test
    fun ocultar_fecha_configuracao_e_expande_para_selo() {

        val viewModel =
            AppViewModel()

        viewModel.alternarDetalhes()
        viewModel.abrirConfiguracoes()

        viewModel.ocultarInterface()

        assertTrue(
            viewModel.state.interfaceOculta
        )

        assertTrue(
            viewModel.state.seloFlutuante
        )

        assertFalse(
            viewModel.state.configuracoesVisivel
        )

        assertEquals(
            ModoApresentacao.COMPACTA,
            viewModel.state.corrida.modo,
        )

        assertTrue(
            viewModel.state.monitorando
        )
    }

    // =====================================================================
    // CANCELAMENTO DO FECHAMENTO
    // =====================================================================

    @Test
    fun cancelar_fechar_nao_altera_monitoramento() {

        val viewModel =
            AppViewModel()

        viewModel.solicitarFecharApp()

        assertTrue(
            viewModel.state.confirmacaoFecharVisivel
        )

        viewModel.cancelarFecharApp()

        assertFalse(
            viewModel.state.confirmacaoFecharVisivel
        )

        assertTrue(
            viewModel.state.monitorando
        )
    }
}