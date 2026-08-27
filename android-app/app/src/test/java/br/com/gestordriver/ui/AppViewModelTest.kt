package br.com.gestordriver.ui

import br.com.gestordriver.model.ModoApresentacao
import br.com.gestordriver.model.PlanoAcesso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppViewModelTest {

    private fun novoViewModel(): AppViewModel = AppViewModel(
        coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
    )

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
            novoViewModel()

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
            novoViewModel()

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
            novoViewModel()

        viewModel.reabrirInterface()

        assertEquals(
            ModoApresentacao.DETALHES,
            viewModel.state.corrida.modo,
        )

        assertTrue(viewModel.state.interfaceOculta)

        viewModel.alternarDetalhes()

        assertTrue(viewModel.state.interfaceOculta)
        assertTrue(viewModel.state.compactaTemporaria)
        assertEquals(
            ModoApresentacao.COMPACTA,
            viewModel.state.corrida.modo,
        )
    }

    // =====================================================================
    // HISTÓRICO
    // =====================================================================

    @Test
    fun historico_deve_ser_aberto_e_fechado() {

        val viewModel =
            novoViewModel()

        viewModel.reabrirInterface()

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
            novoViewModel()

        viewModel.reabrirInterface()
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
            novoViewModel()

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
            novoViewModel()

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
            novoViewModel()

        viewModel.reabrirInterface()
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
            novoViewModel()

        viewModel.reabrirInterface()
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
            novoViewModel()

        viewModel.reabrirInterface()
        viewModel.abrirConfiguracoes()
        viewModel.ocultarInterface()

        assertTrue(
            viewModel.state.seloFlutuante,
        )

        viewModel.reabrirInterface()

        assertTrue(viewModel.state.interfaceOculta)
        assertFalse(viewModel.state.seloFlutuante)

        assertFalse(
            viewModel.state.configuracoesVisivel,
        )

        assertFalse(
            viewModel.state.historicoVisivel,
        )

        assertEquals(
            ModoApresentacao.DETALHES,
            viewModel.state.corrida.modo,
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
            novoViewModel()

        viewModel.iniciarMonitoramento()
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
            novoViewModel()

        viewModel.iniciarMonitoramento()
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
            viewModel.state.seloFlutuante,
        )
    }

    // =====================================================================
    // CONFIRMAR FECHAMENTO
    // =====================================================================

    @Test
    fun confirmar_fechar_deve_encerrar_monitoramento() {

        val viewModel =
            novoViewModel()

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
            novoViewModel()

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

    @Test
    fun nova_oferta_nao_entra_no_historico() {
        val viewModel = novoViewModel()
        val analise = analiseFake()
        viewModel.aplicarNovaCorrida(analise)
        assertTrue(viewModel.state.historico.isEmpty())
        assertEquals(analise.valorTotal, viewModel.state.analiseAtual?.valorTotal)
        assertFalse(viewModel.state.corridaAceita)
        assertTrue(viewModel.state.ofertaAtiva)
        assertTrue(viewModel.state.interfaceOculta)
        assertFalse(viewModel.state.seloFlutuante)
    }

    @Test
    fun aceite_detectado_grava_historico() {
        val viewModel = novoViewModel()
        viewModel.aplicarNovaCorrida(analiseFake())
        viewModel.registrarAceiteCorrida()
        assertEquals(1, viewModel.state.historico.size)
        assertTrue(viewModel.state.corridaAceita)
        assertFalse(viewModel.state.ofertaAtiva)
    }

    @Test
    fun oferta_expirada_nao_entra_no_historico_e_mantem_ultima_aceita() {
        val viewModel = novoViewModel()
        val aceita = analiseFake(valor = 40.0)
        viewModel.aplicarNovaCorrida(aceita)
        viewModel.registrarAceiteCorrida()
        val novaOferta = analiseFake(valor = 22.0)
        viewModel.aplicarNovaCorrida(novaOferta)
        viewModel.expirarOfertaAtual()
        assertEquals(1, viewModel.state.historico.size)
        assertEquals(40.0, viewModel.state.ultimaCorridaAceita?.valorTotal ?: 0.0, 0.001)
        assertFalse(viewModel.state.ofertaAtiva)
    }

    @Test
    fun iniciar_monitoramento_nao_esconde_oferta_ativa() {
        val viewModel = novoViewModel()
        viewModel.aplicarNovaCorrida(analiseFake())
        viewModel.iniciarMonitoramento()
        assertTrue(viewModel.state.ofertaAtiva)
        assertTrue(viewModel.state.interfaceOculta)
        assertFalse(viewModel.state.seloFlutuante)
    }

    @Test
    fun iniciar_monitoramento_exibe_selo_sem_encerrar_ciclo() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        assertTrue(viewModel.state.monitorando)
        assertTrue(viewModel.state.seloFlutuante)
        assertTrue(viewModel.state.interfaceOculta)
    }

    @Test
    fun retrair_com_oferta_permanece_compacta_sem_timer() {
        val viewModel = novoViewModel()
        viewModel.aplicarNovaCorrida(analiseFake())
        viewModel.reabrirInterface(origemCompacta = true)
        viewModel.alternarDetalhes()
        assertTrue(viewModel.state.ofertaAtiva)
        assertTrue(viewModel.state.interfaceOculta)
        assertFalse(viewModel.state.compactaTemporaria)
        assertFalse(viewModel.state.seloFlutuante)
    }

    @Test
    fun recolher_ao_sair_volta_ao_selo() {
        val viewModel = novoViewModel()
        viewModel.reabrirInterface()
        viewModel.abrirHistoricoPeloOverlay()
        viewModel.recolherAoSairDoApp()
        assertTrue(viewModel.state.interfaceOculta)
        assertTrue(viewModel.state.seloFlutuante)
    }

    @Test
    fun retrair_sem_oferta_inicia_espera_para_selo() {
        val viewModel = novoViewModel()
        viewModel.reabrirInterface()
        viewModel.alternarDetalhes()
        assertTrue(viewModel.state.compactaTemporaria)
        assertTrue(viewModel.state.interfaceOculta)
        assertFalse(viewModel.state.seloFlutuante)
    }

    @Test
    fun oferta_na_expandida_expira_para_ultima_exibida() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.reabrirInterface()
        val aceita = analiseFake(valor = 40.0)
        viewModel.aplicarNovaCorrida(aceita)
        viewModel.registrarAceiteCorrida()
        viewModel.aplicarNovaCorrida(analiseFake(valor = 22.0))
        assertEquals(22.0, viewModel.state.analiseAtual?.valorTotal ?: 0.0, 0.001)
        assertTrue(viewModel.state.interfaceOculta)
        viewModel.expirarOfertaAtual()
        assertEquals(40.0, viewModel.state.analiseAtual?.valorTotal ?: 0.0, 0.001)
        assertFalse(viewModel.state.ofertaAtiva)
        assertTrue(viewModel.state.interfaceOculta)
    }

    @Test
    fun oferta_no_overlay_expira_volta_ao_selo_na_mesma_posicao() {
        val viewModel = novoViewModel()
        viewModel.atualizarPosicaoSelo(80f, 160f)
        viewModel.aplicarNovaCorrida(analiseFake())
        viewModel.expirarOfertaAtual()
        assertTrue(viewModel.state.seloFlutuante)
        assertTrue(viewModel.state.interfaceOculta)
        assertEquals(80f, viewModel.state.seloOffsetX)
        assertEquals(160f, viewModel.state.seloOffsetY)
    }

    private fun analiseFake(valor: Double = 38.0) =
        br.com.gestordriver.core.CalculadoraCorrida(
            configuracaoUsuario = br.com.gestordriver.core.ConfiguracaoUsuario.padrao(),
        ).calcular(
            corrida = br.com.gestordriver.core.Corrida(
                valorTotal = valor,
                kmAtePassageiro = 3.2,
                kmViagem = 12.8,
                tempoEstimado = 24,
            ),
            plataforma = "Uber",
        )
}