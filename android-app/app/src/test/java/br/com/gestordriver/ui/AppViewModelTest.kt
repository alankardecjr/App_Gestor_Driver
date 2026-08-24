package br.com.gestordriver.ui

import br.com.gestordriver.model.ModoApresentacao
import br.com.gestordriver.model.PlanoAcesso
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppViewModelTest {

    // =====================================================================
    // HISTÓRICO
    // =====================================================================
    //
    // Uma nova oferta não deve ser automaticamente considerada aceita.
    // Portanto, o estado inicial precisa possuir histórico vazio.
    // =====================================================================

    @Test
    fun historico_inicial_deve_estar_vazio() {

        val viewModel =
            AppViewModel()

        assertTrue(
            viewModel.state.historico.isEmpty(),
        )
    }

    // =====================================================================
    // CORRIDA
    // =====================================================================

    @Test
    fun deve_iniciar_em_modo_compacto() {

        val viewModel =
            AppViewModel()

        assertEquals(
            ModoApresentacao.COMPACTA,
            viewModel.state.corrida.modo,
        )

        assertEquals(
            "ⓘ",
            viewModel.state.corrida.acaoDetalhes,
        )
    }

    // =====================================================================
    // EXPANDIR / RETRAIR
    // =====================================================================

    @Test
    fun deve_alternar_detalhes_da_corrida() {

        val viewModel =
            AppViewModel()

        // Estado inicial.
        assertEquals(
            ModoApresentacao.COMPACTA,
            viewModel.state.corrida.modo,
        )

        // Expande.
        viewModel.alternarDetalhes()

        assertEquals(
            ModoApresentacao.DETALHES,
            viewModel.state.corrida.modo,
        )

        assertEquals(
            "Menos detalhes",
            viewModel.state.corrida.acaoDetalhes,
        )

        // Retrai.
        viewModel.alternarDetalhes()

        assertEquals(
            ModoApresentacao.COMPACTA,
            viewModel.state.corrida.modo,
        )

        assertEquals(
            "ⓘ",
            viewModel.state.corrida.acaoDetalhes,
        )
    }

    // =====================================================================
    // HISTÓRICO
    // =====================================================================

    @Test
    fun historico_deve_ser_aberto_e_fechado() {

        val viewModel =
            AppViewModel()

        viewModel.alternarDetalhes()

        assertFalse(
            viewModel.state.historicoVisivel,
        )

        viewModel.alternarHistorico()

        assertTrue(
            viewModel.state.historicoVisivel,
        )

        viewModel.alternarHistorico()

        assertFalse(
            viewModel.state.historicoVisivel,
        )
    }

    // =====================================================================
    // CONFIGURAÇÃO E HISTÓRICO
    // =====================================================================

    @Test
    fun configuracao_e_historico_devem_ser_exclusivos() {

        val viewModel =
            AppViewModel()

        viewModel.alternarDetalhes()

        // Abre configuração.
        viewModel.abrirConfiguracoes()

        assertTrue(
            viewModel.state.configuracoesVisivel,
        )

        assertFalse(
            viewModel.state.historicoVisivel,
        )

        // Abre histórico.
        viewModel.alternarHistorico()

        assertTrue(
            viewModel.state.historicoVisivel,
        )

        assertFalse(
            viewModel.state.configuracoesVisivel,
        )
    }

    // =====================================================================
    // PLANO
    // =====================================================================

    @Test
    fun deve_selecionar_plano() {

        val viewModel =
            AppViewModel()

        viewModel.selecionarPlano(
            PlanoAcesso.PRO,
        )

        assertEquals(
            PlanoAcesso.PRO,
            viewModel.state.plano,
        )
    }

    // =====================================================================
    // SEM NOTIFICAÇÃO
    // =====================================================================

    @Test
    fun sem_notificacao_deve_manter_monitoramento_e_mostrar_selo() {

        val viewModel =
            AppViewModel()

        viewModel.semNotificacao()

        assertFalse(
            viewModel.state.notificacaoDisponivel,
        )

        assertTrue(
            viewModel.state.seloFlutuante,
        )

        assertTrue(
            viewModel.state.monitorando,
        )

        assertTrue(
            viewModel.state.interfaceOculta,
        )

        assertFalse(
            viewModel.state.historicoVisivel,
        )

        assertFalse(
            viewModel.state.configuracoesVisivel,
        )
    }

    // =====================================================================
    // OCULTAR
    // =====================================================================
    //
    // REGRA:
    //
    // Ocultar NÃO encerra o monitoramento.
    //
    // Ele:
    //
    // histórico/configuração → fechados
    // corrida → compacta
    // interface → ocultada
    // selo → exibido
    // monitoramento → continua ativo
    // =====================================================================

    @Test
    fun ocultar_mantem_monitoramento_e_exibe_selo() {

        val viewModel =
            AppViewModel()

        viewModel.alternarDetalhes()
        viewModel.alternarHistorico()

        viewModel.ocultarInterface()

        assertTrue(
            viewModel.state.interfaceOculta,
        )

        assertTrue(
            viewModel.state.seloFlutuante,
        )

        assertTrue(
            viewModel.state.monitorando,
        )

        assertFalse(
            viewModel.state.historicoVisivel,
        )

        assertFalse(
            viewModel.state.configuracoesVisivel,
        )

        assertEquals(
            ModoApresentacao.COMPACTA,
            viewModel.state.corrida.modo,
        )
    }

    // =====================================================================
    // OCULTAR CONFIGURAÇÃO
    // =====================================================================

    @Test
    fun ocultar_deve_fechar_configuracao_e_mostrar_selo() {

        val viewModel =
            AppViewModel()

        viewModel.alternarDetalhes()
        viewModel.abrirConfiguracoes()

        assertTrue(
            viewModel.state.configuracoesVisivel,
        )

        viewModel.ocultarInterface()

        assertTrue(
            viewModel.state.interfaceOculta,
        )

        assertTrue(
            viewModel.state.seloFlutuante,
        )

        assertTrue(
            viewModel.state.monitorando,
        )

        assertFalse(
            viewModel.state.configuracoesVisivel,
        )

        assertFalse(
            viewModel.state.historicoVisivel,
        )

        assertEquals(
            ModoApresentacao.COMPACTA,
            viewModel.state.corrida.modo,
        )
    }

    // =====================================================================
    // REABRIR PELO SELO
    // =====================================================================

    @Test
    fun selo_deve_reabrir_interface() {

        val viewModel =
            AppViewModel()

        viewModel.alternarDetalhes()
        viewModel.abrirConfiguracoes()
        viewModel.ocultarInterface()

        assertTrue(
            viewModel.state.seloFlutuante,
        )

        viewModel.reabrirInterface()

        assertFalse(
            viewModel.state.interfaceOculta,
        )

        assertFalse(
            viewModel.state.seloFlutuante,
        )

        assertTrue(
            viewModel.state.overlayAtivo,
        )

        assertTrue(
            viewModel.state.monitorando,
        )
    }

    // =====================================================================
    // FECHAMENTO
    // =====================================================================

    @Test
    fun solicitar_fechar_deve_exibir_confirmacao() {

        val viewModel =
            AppViewModel()

        viewModel.solicitarFecharApp()

        assertTrue(
            viewModel.state.confirmacaoFecharVisivel,
        )

        assertTrue(
            viewModel.state.monitorando,
        )
    }

    // =====================================================================
    // CANCELAR FECHAMENTO
    // =====================================================================

    @Test
    fun cancelar_fechar_nao_deve_alterar_monitoramento() {

        val viewModel =
            AppViewModel()

        viewModel.solicitarFecharApp()

        assertTrue(
            viewModel.state.confirmacaoFecharVisivel,
        )

        viewModel.cancelarFecharApp()

        assertFalse(
            viewModel.state.confirmacaoFecharVisivel,
        )

        assertTrue(
            viewModel.state.monitorando,
        )

        assertTrue(
            viewModel.state.seloFlutuante.not(),
        )
    }

    // =====================================================================
    // CONFIRMAR FECHAMENTO
    // =====================================================================

    @Test
    fun confirmar_fechar_deve_encerrar_monitoramento() {

        val viewModel =
            AppViewModel()

        viewModel.solicitarFecharApp()

        viewModel.confirmarFecharApp()

        assertFalse(
            viewModel.state.monitorando,
        )

        assertFalse(
            viewModel.state.seloFlutuante,
        )

        assertFalse(
            viewModel.state.overlayAtivo,
        )

        assertFalse(
            viewModel.state.interfaceOculta,
        )

        assertFalse(
            viewModel.state.historicoVisivel,
        )

        assertFalse(
            viewModel.state.configuracoesVisivel,
        )

        assertFalse(
            viewModel.state.confirmacaoFecharVisivel,
        )
    }

    // =====================================================================
    // POSIÇÃO DO SELO
    // =====================================================================

    @Test
    fun deve_atualizar_posicao_do_selo() {

        val viewModel =
            AppViewModel()

        viewModel.atualizarPosicaoSelo(
            offsetX = 120f,
            offsetY = 240f,
        )

        assertEquals(
            120f,
            viewModel.state.seloOffsetX,
        )

        assertEquals(
            240f,
            viewModel.state.seloOffsetY,
        )
    }
}