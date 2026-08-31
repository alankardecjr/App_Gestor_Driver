package br.com.gestordriver.ui

import br.com.gestordriver.model.ModoApresentacao
import br.com.gestordriver.model.PlanoAcesso
import br.com.gestordriver.overlay.OverlayAcao
import br.com.gestordriver.overlay.OverlayBridge
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

    @Test
    fun aba_custos_e_app_devem_atualizar_estado() {
        val viewModel = novoViewModel()
        viewModel.abrirConfiguracoes()
        OverlayBridge.emitir(OverlayAcao.AbaConfiguracao(1))
        assertEquals(1, viewModel.state.abaConfiguracao)
        OverlayBridge.emitir(OverlayAcao.AbaConfiguracao(3))
        assertEquals(3, viewModel.state.abaConfiguracao)
    }

    @Test
    fun alternar_config_abre_e_fecha() {
        val viewModel = novoViewModel()
        viewModel.abrirConfiguracoes()
        assertTrue(viewModel.state.configuracoesVisivel)
        viewModel.alternarConfiguracoes()
        assertFalse(viewModel.state.configuracoesVisivel)
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

    @Test
    fun solicitar_fechar_deve_manter_expandida_e_abrir_painel_abaixo() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.reabrirInterface()
        viewModel.solicitarFecharApp()

        assertTrue(viewModel.state.confirmacaoFecharVisivel)
        assertTrue(viewModel.state.interfaceOculta)
        assertFalse(viewModel.state.historicoVisivel)
        assertFalse(viewModel.state.configuracoesVisivel)
        assertFalse(viewModel.state.seloFlutuante)
        assertEquals(ModoApresentacao.DETALHES, viewModel.state.corrida.modo)
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

    @Test
    fun cancelar_fechar_deve_permanecer_na_expandida() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.reabrirInterface()
        viewModel.solicitarFecharApp()
        viewModel.cancelarFecharApp()

        assertFalse(viewModel.state.confirmacaoFecharVisivel)
        assertTrue(viewModel.state.monitorando)
        assertTrue(viewModel.state.interfaceOculta)
        assertFalse(viewModel.state.seloFlutuante)
        assertEquals(ModoApresentacao.DETALHES, viewModel.state.corrida.modo)
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
        assertEquals(analise.corClassificacao, viewModel.state.corrida.corClassificacao)
    }

    @Test
    fun aceite_detectado_grava_historico() {
        val viewModel = novoViewModel()
        viewModel.aplicarNovaCorrida(analiseFake())
        viewModel.registrarAceiteCorrida()
        assertEquals(1, viewModel.state.historico.size)
        assertTrue(viewModel.state.corridaAceita)
        assertFalse(viewModel.state.ofertaAtiva)
        assertTrue(viewModel.state.seloFlutuante)
        assertEquals(null, viewModel.state.analiseAtual)
        assertEquals("—", viewModel.state.corrida.camposCompactos.first { it.id == "valor_total" }.valor)
    }

    @Test
    fun aceite_depois_de_expirar_ainda_grava_historico() {
        val viewModel = novoViewModel()
        viewModel.aplicarNovaCorrida(analiseFake())
        viewModel.expirarOfertaAtual()
        assertEquals(null, viewModel.state.analiseAtual)
        viewModel.registrarAceiteCorrida()
        assertEquals(1, viewModel.state.historico.size)
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
        assertEquals(null, viewModel.state.analiseAtual)
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
        assertTrue(viewModel.state.compactaTemporaria)
        assertFalse(viewModel.state.seloFlutuante)
    }

    @Test
    fun toque_fora_da_compacta_com_oferta_vai_ao_selo() {
        val viewModel = novoViewModel()
        viewModel.aplicarNovaCorrida(analiseFake())
        assertTrue(viewModel.state.ofertaAtiva)
        assertFalse(viewModel.state.seloFlutuante)
        OverlayBridge.emitir(OverlayAcao.ToqueForaDaCompacta)
        assertTrue(viewModel.state.seloFlutuante)
        assertTrue(viewModel.state.ofertaAtiva)
        assertTrue(viewModel.state.interfaceOculta)
    }

    @Test
    fun toque_fora_apos_retrair_vai_ao_selo_na_hora() {
        val viewModel = novoViewModel()
        viewModel.aplicarNovaCorrida(analiseFake())
        viewModel.reabrirInterface()
        viewModel.alternarDetalhes()
        assertTrue(viewModel.state.compactaTemporaria)
        OverlayBridge.emitir(OverlayAcao.ToqueForaDaCompacta)
        assertTrue(viewModel.state.seloFlutuante)
        assertFalse(viewModel.state.compactaTemporaria)
    }

    @Test
    fun toque_fora_na_expandida_nao_recolhe() {
        val viewModel = novoViewModel()
        viewModel.aplicarNovaCorrida(analiseFake())
        viewModel.reabrirInterface()
        assertEquals(ModoApresentacao.DETALHES, viewModel.state.corrida.modo)
        OverlayBridge.emitir(OverlayAcao.ToqueForaDaCompacta)
        assertFalse(viewModel.state.seloFlutuante)
        assertEquals(ModoApresentacao.DETALHES, viewModel.state.corrida.modo)
    }

    @Test
    fun recolher_ao_sair_nao_fecha_overlay_ja_visivel() {
        val viewModel = novoViewModel()
        viewModel.reabrirInterface()
        viewModel.abrirHistoricoPeloOverlay()
        viewModel.recolherAoSairDoApp()
        assertTrue(viewModel.state.interfaceOculta)
        assertTrue(viewModel.state.historicoVisivel)
        assertFalse(viewModel.state.seloFlutuante)
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
        assertEquals(null, viewModel.state.analiseAtual)
        assertFalse(viewModel.state.ofertaAtiva)
        assertTrue(viewModel.state.interfaceOculta)
    }

    @Test
    fun oferta_na_expandida_expira_nao_restaura_corrida_aceita_nos_campos() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.reabrirInterface()
        val aceita = analiseFake(valor = 40.0)
        viewModel.aplicarNovaCorrida(aceita)
        viewModel.registrarAceiteCorrida()
        viewModel.reabrirInterface()
        viewModel.alternarHistorico()
        viewModel.selecionarHistorico(viewModel.state.historico.first())
        viewModel.aplicarNovaCorrida(analiseFake(valor = 22.0))
        assertEquals(22.0, viewModel.state.analiseAtual?.valorTotal ?: 0.0, 0.001)
        viewModel.expirarOfertaAtual()
        assertEquals(null, viewModel.state.analiseAtual)
        assertFalse(viewModel.state.ofertaAtiva)
        assertTrue(viewModel.state.seloFlutuante)
        assertEquals(1, viewModel.state.historico.size)
        assertEquals(40.0, viewModel.state.historico.first().valorTotal, 0.001)
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

    @Test
    fun aceite_99_mantem_oferta_uber() {
        val viewModel = novoViewModel()
        viewModel.aplicarNovaCorrida(analiseFake(valor = 40.0, plataforma = "Uber"))
        viewModel.aplicarNovaCorrida(analiseFake(valor = 8.9, plataforma = "99"))
        viewModel.registrarAceiteCorrida()
        assertEquals(1, viewModel.state.historico.size)
        assertEquals("99", viewModel.state.abaHistorico)
        assertTrue(viewModel.state.ofertaAtiva)
        assertEquals(40.0, viewModel.state.analiseAtual?.valorTotal ?: 0.0, 0.001)
        assertFalse(viewModel.state.seloFlutuante)
    }

    private fun analiseFake(valor: Double = 38.0, plataforma: String = "Uber") =
        br.com.gestordriver.core.CalculadoraCorrida(
            configuracaoUsuario = br.com.gestordriver.core.ConfiguracaoUsuario.padrao(),
        ).calcular(
            corrida = br.com.gestordriver.core.Corrida(
                valorTotal = valor,
                kmAtePassageiro = 3.2,
                kmViagem = 12.8,
                tempoEstimado = 24,
            ),
            plataforma = plataforma,
        )
}