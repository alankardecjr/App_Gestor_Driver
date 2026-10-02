package br.com.gestordriver.ui

import br.com.gestordriver.model.ModoApresentacao
import br.com.gestordriver.model.PlanoAcesso
import br.com.gestordriver.notification.RideNotificationBus
import br.com.gestordriver.notification.RideNotificationEvent
import br.com.gestordriver.overlay.OverlayAcao
import br.com.gestordriver.overlay.OverlayBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppViewModelTest {

    private val testScopes = mutableListOf<CoroutineScope>()

    @After
    fun cancelarEscoposDosViewModels() {
        testScopes.forEach(CoroutineScope::cancel)
        testScopes.clear()
    }

    private fun novoViewModel(): AppViewModel {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        testScopes += scope
        return AppViewModel(coroutineScope = scope)
    }

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

        viewModel.iniciarMonitoramento()
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
    fun estado_inicial_sem_chave_e_free() {
        assertEquals(PlanoAcesso.FREE, novoViewModel().state.plano)
    }

    @Test
    fun chave_pro_libera_a_instalacao() {
        val viewModel = novoViewModel()
        assertFalse(viewModel.liberarComChave("outra"))
        assertEquals(PlanoAcesso.FREE, viewModel.state.plano)
        assertTrue(viewModel.liberarComChave("GestorDrivePro"))
        assertEquals(PlanoAcesso.PRO, viewModel.state.plano)
    }

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

    @Test
    fun solicitar_limpar_historico_sem_selecao_nao_abre_confirmacao() {
        val viewModel = novoViewModel()
        viewModel.aplicarNovaCorrida(analiseFake())
        viewModel.registrarAceiteCorrida()
        viewModel.alternarHistorico()
        viewModel.solicitarLimparHistorico()
        assertFalse(viewModel.state.confirmacaoLimparHistoricoVisivel)
        assertTrue(viewModel.state.historicoVisivel)
        assertEquals(1, viewModel.state.historico.size)
    }

    @Test
    fun solicitar_limpar_historico_deve_exibir_confirmacao_sem_apagar() {
        val viewModel = novoViewModel()
        viewModel.aplicarNovaCorrida(analiseFake())
        viewModel.registrarAceiteCorrida()
        viewModel.alternarHistorico()
        viewModel.marcarItemHistorico(viewModel.state.historico.first())
        viewModel.solicitarLimparHistorico()
        assertTrue(viewModel.state.confirmacaoLimparHistoricoVisivel)
        assertTrue(viewModel.state.historicoVisivel)
        assertEquals(1, viewModel.state.historico.size)
        assertFalse(OverlayBridge.snapshot.value.expandidaVisivel)
        assertFalse(OverlayBridge.snapshot.value.historicoVisivel)
        assertFalse(OverlayBridge.snapshot.value.confirmacaoLimparHistoricoVisivel)
    }

    @Test
    fun cancelar_limpar_historico_deve_manter_itens() {
        val viewModel = novoViewModel()
        viewModel.aplicarNovaCorrida(analiseFake())
        viewModel.registrarAceiteCorrida()
        viewModel.alternarHistorico()
        viewModel.marcarItemHistorico(viewModel.state.historico.first())
        viewModel.solicitarLimparHistorico()
        viewModel.cancelarLimparHistorico()
        assertFalse(viewModel.state.confirmacaoLimparHistoricoVisivel)
        assertTrue(viewModel.state.historicoVisivel)
        assertEquals(1, viewModel.state.historico.size)
    }

    @Test
    fun confirmar_limpar_historico_deve_apagar_somente_selecionadas() {
        val viewModel = novoViewModel()
        viewModel.aplicarNovaCorrida(analiseFake(38.0, "Uber"))
        viewModel.registrarAceiteCorrida()
        viewModel.aplicarNovaCorrida(analiseFake(40.0, "99"))
        viewModel.registrarAceiteCorrida()
        val uber = viewModel.state.historico.first { it.plataforma == "Uber" }
        val noventaNove = viewModel.state.historico.first { it.plataforma == "99" }
        viewModel.marcarItemHistorico(uber)
        viewModel.solicitarLimparHistorico()
        viewModel.confirmarLimparHistorico()
        assertFalse(viewModel.state.confirmacaoLimparHistoricoVisivel)
        assertTrue(viewModel.state.historicoVisivel)
        assertEquals(listOf("99"), viewModel.state.historico.map { it.plataforma })
        assertTrue(viewModel.state.historicoChavesSelecionadas.isEmpty())
        assertEquals(noventaNove.plataforma, viewModel.state.historico.single().plataforma)
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
    fun esconder_selo_no_x_preserva_posicao_e_reabrir_restaura() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.atualizarPosicaoSelo(80f, 320f)
        viewModel.esconderSeloManterMonitor()
        assertTrue(viewModel.state.seloEscondido)
        assertFalse(viewModel.state.seloFlutuante)
        assertEquals(80f, viewModel.state.seloOffsetX)
        assertEquals(320f, viewModel.state.seloOffsetY)

        viewModel.restaurarTelaAposRecentes()
        assertTrue(viewModel.state.seloEscondido)
        assertFalse(viewModel.state.seloFlutuante)
        assertEquals(80f, viewModel.state.seloOffsetX)
        assertEquals(320f, viewModel.state.seloOffsetY)

        viewModel.desativarMonitoramento()
        viewModel.iniciarMonitoramento()
        assertFalse(viewModel.state.seloEscondido)
        assertTrue(viewModel.state.seloFlutuante)
        assertEquals(80f, viewModel.state.seloOffsetX)
        assertEquals(320f, viewModel.state.seloOffsetY)
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
        assertFalse(viewModel.state.compactaTemporaria)
        assertEquals(38.0, viewModel.state.analiseAtual?.valorTotal ?: 0.0, 0.001)
    }

    @Test
    fun aceite_uber_repetido_nao_duplica_historico() {
        val viewModel = novoViewModel()
        viewModel.aplicarNovaCorrida(analiseFake())
        viewModel.registrarAceiteCorrida()
        viewModel.aplicarNovaCorrida(analiseFake())
        viewModel.registrarAceiteCorrida()
        assertEquals(1, viewModel.state.historico.size)
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
        assertTrue(viewModel.state.ofertaAtiva)
        assertTrue(viewModel.state.interfaceOculta)
        assertFalse(viewModel.state.compactaTemporaria)
        assertFalse(viewModel.state.seloFlutuante)
        assertEquals(ModoApresentacao.COMPACTA, viewModel.state.corrida.modo)
    }

    @Test
    fun voltar_fecha_historico_para_menu_depois_selo() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.reabrirInterface()
        viewModel.abrirHistoricoPeloOverlay()
        OverlayBridge.emitir(OverlayAcao.VoltarBarra)
        assertFalse(viewModel.state.historicoVisivel)
        assertEquals(ModoApresentacao.DETALHES, viewModel.state.corrida.modo)
        assertTrue(viewModel.state.seloFlutuante)
        OverlayBridge.emitir(OverlayAcao.VoltarBarra)
        assertTrue(viewModel.state.seloFlutuante)
    }

    @Test
    fun voltar_fecha_config_do_overlay_para_menu_depois_selo() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.reabrirInterface()
        viewModel.abrirConfiguracoes()
        OverlayBridge.emitir(OverlayAcao.VoltarBarra)
        assertTrue(viewModel.state.configuracoesVisivel)
        assertEquals(-1, viewModel.state.abaConfiguracao)
        assertTrue(viewModel.state.seloFlutuante)
        OverlayBridge.emitir(OverlayAcao.VoltarBarra)
        assertTrue(viewModel.state.seloFlutuante)
    }

    @Test
    fun recentes_recolhe_ao_selo_e_guarda_historico() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.reabrirInterface()
        viewModel.abrirHistoricoPeloOverlay()
        OverlayBridge.emitir(OverlayAcao.RecentesBarra)
        assertTrue(viewModel.state.seloFlutuante)
        assertTrue(viewModel.state.interfaceOculta)
        assertFalse(viewModel.state.historicoVisivel)
        assertTrue(viewModel.state.estadoSalvo?.historicoVisivel == true)
    }

    @Test
    fun clique_nos_recentes_restaura_ultima_tela() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.reabrirInterface()
        viewModel.abrirHistoricoPeloOverlay()
        OverlayBridge.emitir(OverlayAcao.RecentesBarra)
        viewModel.restaurarTelaAposRecentes()
        assertTrue(viewModel.state.historicoVisivel)
        assertFalse(viewModel.state.seloFlutuante)
        assertFalse(viewModel.state.interfaceOculta)
        assertNull(viewModel.state.estadoSalvo)
    }

    @Test
    fun voltar_na_config_dos_recentes_vai_ao_selo() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.abrirAtalhoConfig(0)
        OverlayBridge.emitir(OverlayAcao.RecentesBarra)
        viewModel.restaurarTelaAposRecentes()
        OverlayBridge.emitir(OverlayAcao.VoltarBarra)
        assertTrue(viewModel.state.configuracoesVisivel)
        assertFalse(viewModel.state.interfaceOculta)
    }

    @Test
    fun home_descarta_tela_salva_dos_recentes() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.reabrirInterface()
        viewModel.abrirHistoricoPeloOverlay()
        OverlayBridge.emitir(OverlayAcao.RecentesBarra)
        OverlayBridge.emitir(OverlayAcao.RecolherParaSelo)
        viewModel.restaurarTelaAposRecentes()
        assertTrue(viewModel.state.seloFlutuante)
        assertFalse(viewModel.state.historicoVisivel)
        assertNull(viewModel.state.estadoSalvo)
    }

    @Test
    fun home_vai_ao_selo() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.abrirAtalhoConfig(0)
        OverlayBridge.emitir(OverlayAcao.RecolherParaSelo)
        assertTrue(viewModel.state.seloFlutuante)
        assertFalse(viewModel.state.configuracoesVisivel)
        assertTrue(viewModel.state.interfaceOculta)
    }

    @Test
    fun barra_inferior_recolhe_expandida_e_historico_ao_selo() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.aplicarNovaCorrida(analiseFake())
        viewModel.reabrirInterface()
        viewModel.abrirHistoricoPeloOverlay()
        assertFalse(viewModel.state.seloFlutuante)
        OverlayBridge.emitir(OverlayAcao.RecolherParaSelo)
        assertTrue(viewModel.state.seloFlutuante)
        assertFalse(viewModel.state.historicoVisivel)
        assertEquals(ModoApresentacao.COMPACTA, viewModel.state.corrida.modo)
    }

    @Test
    fun barra_inferior_no_selo_nao_muda() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        assertTrue(viewModel.state.seloFlutuante)
        OverlayBridge.emitir(OverlayAcao.RecolherParaSelo)
        assertTrue(viewModel.state.seloFlutuante)
    }

    @Test
    fun toque_fora_da_compacta_com_oferta_nao_recolhe() {
        val viewModel = novoViewModel()
        viewModel.aplicarNovaCorrida(analiseFake())
        assertTrue(viewModel.state.ofertaAtiva)
        assertFalse(viewModel.state.seloFlutuante)
        OverlayBridge.emitir(OverlayAcao.ToqueForaDaCompacta)
        assertFalse(viewModel.state.seloFlutuante)
        assertTrue(viewModel.state.ofertaAtiva)
        assertTrue(viewModel.state.interfaceOculta)
    }

    @Test
    fun toque_fora_apos_retrair_nao_vai_ao_selo() {
        val viewModel = novoViewModel()
        viewModel.aplicarNovaCorrida(analiseFake())
        viewModel.reabrirInterface()
        viewModel.alternarDetalhes()
        assertTrue(viewModel.state.compactaTemporaria)
        OverlayBridge.emitir(OverlayAcao.ToqueForaDaCompacta)
        assertTrue(viewModel.state.compactaTemporaria)
        assertFalse(viewModel.state.seloFlutuante)
    }

    @Test
    fun fechar_notificacao_ou_selo_nao_desliga_monitoramento() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.fecharNotificacao()
        assertTrue(viewModel.state.monitorando)
        assertTrue(viewModel.state.notificacaoFechada)
        assertTrue(viewModel.state.seloFlutuante)

        viewModel.esconderSeloManterMonitor()
        assertTrue(viewModel.state.monitorando)
        assertTrue(viewModel.state.seloEscondido)
        assertTrue(viewModel.state.notificacaoFechada)
    }

    @Test
    fun toque_no_selo_abre_e_fecha_menu_atalho_mantendo_selo_visivel() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        assertTrue(OverlayBridge.snapshot.value.seloVisivel)
        assertFalse(OverlayBridge.snapshot.value.expandidaVisivel)

        viewModel.alternarAtalhosPeloSelo()
        assertTrue(viewModel.state.atalhosAbertos)
        assertTrue(OverlayBridge.snapshot.value.expandidaVisivel)
        assertFalse(OverlayBridge.snapshot.value.seloVisivel)
        assertEquals(ModoApresentacao.COMPACTA, viewModel.state.corrida.modo)

        viewModel.alternarAtalhosPeloSelo()
        assertFalse(viewModel.state.atalhosAbertos)
        assertFalse(OverlayBridge.snapshot.value.expandidaVisivel)
        assertTrue(OverlayBridge.snapshot.value.seloVisivel)
        assertTrue(viewModel.state.seloFlutuante)
    }

    @Test
    fun selo_some_com_o_app_na_frente_e_volta_ao_sair() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.alternarAtalhosPeloSelo()
        assertTrue(OverlayBridge.snapshot.value.expandidaVisivel)

        viewModel.menuEntrouNaFrente()
        assertFalse(OverlayBridge.snapshot.value.seloVisivel)
        assertFalse(OverlayBridge.snapshot.value.expandidaVisivel)
        assertFalse(viewModel.state.atalhosAbertos)
        assertTrue(viewModel.state.monitorando)

        viewModel.menuSaiuDaFrente()
        assertTrue(OverlayBridge.snapshot.value.seloVisivel)
        assertFalse(OverlayBridge.snapshot.value.expandidaVisivel)
        assertTrue(viewModel.state.monitorando)
    }

    @Test
    fun home_mantem_monitoramento_com_selo_e_aviso_fechados() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.esconderSeloManterMonitor()
        viewModel.exibirOpcoesNosRecentes()
        assertTrue(viewModel.state.monitorando)
        assertTrue(viewModel.state.seloEscondido)
        assertFalse(OverlayBridge.snapshot.value.seloVisivel)

        viewModel.fecharNotificacao()
        assertTrue(viewModel.state.monitorando)
        assertFalse(viewModel.state.confirmacaoDesativarVisivel)
    }

    @Test
    fun remover_aba_dos_recentes_desliga_sem_confirmacao() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.fecharNotificacao()
        viewModel.esconderSeloManterMonitor()
        viewModel.exibirOpcoesNosRecentes()
        assertTrue(viewModel.state.monitorando)

        viewModel.encerrarSeAbaRemovidaDosRecentes()
        assertFalse(viewModel.state.monitorando)
        assertFalse(viewModel.state.confirmacaoDesativarVisivel)
        assertTrue(viewModel.state.configuracoesVisivel)
        assertEquals(-1, viewModel.state.abaConfiguracao)
    }

    @Test
    fun remover_aba_sem_selo_ou_sem_aviso_mantem_monitoramento() {
        val soAviso = novoViewModel()
        soAviso.iniciarMonitoramento()
        soAviso.fecharNotificacao()
        soAviso.encerrarSeAbaRemovidaDosRecentes()
        assertTrue(soAviso.state.monitorando)

        val soSelo = novoViewModel()
        soSelo.iniciarMonitoramento()
        soSelo.esconderSeloManterMonitor()
        soSelo.encerrarSeAbaRemovidaDosRecentes()
        assertTrue(soSelo.state.monitorando)
    }

    @Test
    fun voltar_do_historico_cai_em_opcoes_e_o_proximo_pede_home() {
        val viewModel = novoViewModel()
        viewModel.abrirHistoricoNoMenu()
        assertFalse(viewModel.voltarPelaBarra())
        assertTrue(viewModel.state.configuracoesVisivel)
        assertEquals(-1, viewModel.state.abaConfiguracao)
        assertFalse(viewModel.state.historicoVisivel)
        assertTrue(viewModel.voltarPelaBarra())
        assertEquals(-1, viewModel.state.abaConfiguracao)
        assertTrue(viewModel.state.configuracoesVisivel)
    }

    @Test
    fun atalhos_abrem_a_aba_e_a_retomada_nao_volta_para_opcoes() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        OverlayBridge.emitir(OverlayAcao.AbrirAtalhoConfig(0))
        assertEquals(0, viewModel.state.abaConfiguracao)
        OverlayBridge.emitir(OverlayAcao.AbrirAtalhoConfig(1))
        assertEquals(1, viewModel.state.abaConfiguracao)
        assertTrue(viewModel.state.configuracoesVisivel)
        viewModel.avaliarInicio(permissoesOk = true, temConta = true)
        assertEquals(1, viewModel.state.abaConfiguracao)

        OverlayBridge.emitir(OverlayAcao.AbrirAtalhoConfig(2))
        assertEquals(2, viewModel.state.abaConfiguracao)
        OverlayBridge.emitir(OverlayAcao.AbrirAtalhoConfig(3))
        assertEquals(3, viewModel.state.abaConfiguracao)

        OverlayBridge.emitir(OverlayAcao.AbrirHistorico)
        assertTrue(viewModel.state.historicoVisivel)
        viewModel.avaliarInicio(permissoesOk = true, temConta = true)
        assertTrue(viewModel.state.historicoVisivel)

        OverlayBridge.emitir(OverlayAcao.DashboardPro)
        assertTrue(viewModel.state.dashboardVisivel)
        viewModel.avaliarInicio(permissoesOk = true, temConta = true)
        assertTrue(viewModel.state.dashboardVisivel)
    }

    @Test
    fun sem_monitoramento_oferta_nao_abre_compacta() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.aplicarNovaCorrida(analiseFake())
        assertTrue(OverlayBridge.snapshot.value.compactaVisivel)

        viewModel.desativarMonitoramento()
        assertFalse(viewModel.state.monitorando)
        assertFalse(viewModel.state.ofertaAtiva)
        assertFalse(OverlayBridge.snapshot.value.compactaVisivel)

        RideNotificationBus.publish(
            RideNotificationEvent.CorridaRecebida(analiseFake()),
        )
        assertFalse(viewModel.state.monitorando)
        assertFalse(viewModel.state.ofertaAtiva)
        assertFalse(OverlayBridge.snapshot.value.compactaVisivel)
    }

    @Test
    fun oferta_aparece_com_selo_na_lixeira_aviso_fechado_e_app_aberto() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.fecharNotificacao()
        viewModel.esconderSeloManterMonitor()
        viewModel.abrirHistoricoNoMenu()
        assertTrue(viewModel.state.monitorando)
        assertTrue(viewModel.state.seloEscondido)
        assertTrue(viewModel.state.notificacaoFechada)
        assertFalse(viewModel.state.interfaceOculta)

        viewModel.aplicarNovaCorrida(analiseFake())
        assertTrue(viewModel.state.monitorando)
        assertTrue(viewModel.state.ofertaAtiva)
        assertTrue(OverlayBridge.snapshot.value.compactaVisivel)
        assertFalse(OverlayBridge.snapshot.value.seloVisivel)
        assertTrue(OverlayBridge.snapshot.value.valorTotal.any { it.isDigit() })
        assertTrue(OverlayBridge.snapshot.value.kmTotal.any { it.isDigit() })
        assertTrue(OverlayBridge.snapshot.value.valorPorHora.any { it.isDigit() })

        viewModel.registrarAceiteCorrida()
        assertFalse(viewModel.state.ofertaAtiva)
        assertFalse(OverlayBridge.snapshot.value.compactaVisivel)
        assertTrue(viewModel.state.seloEscondido)
    }

    @Test
    fun lixeira_so_devolve_o_selo_ao_desligar_e_religar() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.esconderSeloManterMonitor()
        viewModel.aplicarNovaCorrida(analiseFake())
        assertTrue(viewModel.state.seloEscondido)
        assertFalse(OverlayBridge.snapshot.value.seloVisivel)
        viewModel.registrarAceiteCorrida()
        assertTrue(viewModel.state.seloEscondido)
        assertFalse(OverlayBridge.snapshot.value.seloVisivel)
        viewModel.desativarMonitoramento()
        viewModel.iniciarMonitoramento()
        assertFalse(viewModel.state.seloEscondido)
        assertTrue(viewModel.state.seloFlutuante)
    }

    @Test
    fun retomada_mantem_a_aba_aberta() {
        val viewModel = novoViewModel()
        viewModel.abrirDashboard()
        viewModel.avaliarInicio(permissoesOk = true, temConta = true)
        assertTrue(viewModel.state.dashboardVisivel)
        assertFalse(viewModel.state.configuracoesVisivel)

        viewModel.definirAbaConfiguracao(2)
        viewModel.avaliarInicio(permissoesOk = true, temConta = true)
        assertEquals(2, viewModel.state.abaConfiguracao)
        assertTrue(viewModel.state.configuracoesVisivel)
        assertFalse(viewModel.state.dashboardVisivel)
    }

    @Test
    fun primeira_abertura_cai_em_opcoes() {
        val viewModel = novoViewModel()
        assertFalse(viewModel.state.configuracoesVisivel)
        viewModel.avaliarInicio(permissoesOk = true, temConta = true)
        assertTrue(viewModel.state.configuracoesVisivel)
        assertEquals(-1, viewModel.state.abaConfiguracao)
    }

    @Test
    fun home_com_monitoramento_ligado_mostra_o_selo() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.definirAbaConfiguracao(2)
        viewModel.menuEntrouNaFrente()
        assertFalse(OverlayBridge.snapshot.value.seloVisivel)

        viewModel.exibirOpcoesNosRecentes()
        assertTrue(viewModel.state.monitorando)
        assertTrue(OverlayBridge.snapshot.value.seloVisivel)
        assertTrue(viewModel.state.configuracoesVisivel)
        assertEquals(2, viewModel.state.abaConfiguracao)
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
    fun recolher_ao_sair_vai_ao_selo() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.reabrirInterface()
        viewModel.abrirHistoricoPeloOverlay()
        viewModel.recolherAoSairDoApp()
        assertTrue(viewModel.state.interfaceOculta)
        assertTrue(viewModel.state.seloFlutuante)
        assertFalse(viewModel.state.historicoVisivel)
    }

    @Test
    fun retrair_sem_oferta_inicia_espera_para_selo() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
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
        assertFalse(viewModel.state.compactaTemporaria)
        assertEquals(1, viewModel.state.historico.size)
        assertEquals(40.0, viewModel.state.historico.first().valorTotal, 0.001)
        assertEquals(ModoApresentacao.COMPACTA, viewModel.state.corrida.modo)
        assertEquals("—", viewModel.state.corrida.camposCompactos.first { it.id == "valor_total" }.valor)
        assertEquals("—", viewModel.state.corrida.camposDetalhes.first { it.id == "km_ate_passageiro" }.valor)
    }

    @Test
    fun oferta_no_overlay_expira_volta_ao_selo_na_mesma_posicao() {
        val viewModel = novoViewModel()
        viewModel.atualizarPosicaoSelo(80f, 160f)
        viewModel.aplicarNovaCorrida(analiseFake())
        viewModel.expirarOfertaAtual()
        assertTrue(viewModel.state.seloFlutuante)
        assertFalse(viewModel.state.compactaTemporaria)
        assertTrue(viewModel.state.interfaceOculta)
        assertEquals(80f, viewModel.state.seloOffsetX)
        assertEquals(160f, viewModel.state.seloOffsetY)
    }

    @Test
    fun nova_oferta_fecha_historico_e_abre_compacta() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.alternarHistorico()
        assertTrue(viewModel.state.historicoVisivel)
        viewModel.aplicarNovaCorrida(analiseFake())
        assertFalse(viewModel.state.historicoVisivel)
        assertFalse(viewModel.state.configuracoesVisivel)
        assertFalse(viewModel.state.dashboardVisivel)
        assertEquals(ModoApresentacao.COMPACTA, viewModel.state.corrida.modo)
        assertTrue(viewModel.state.ofertaAtiva)
        assertTrue(viewModel.state.interfaceOculta)
    }

    @Test
    fun nova_oferta_fecha_config_e_dashboard() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.abrirDashboard()
        assertTrue(viewModel.state.dashboardVisivel)
        viewModel.aplicarNovaCorrida(analiseFake())
        assertFalse(viewModel.state.dashboardVisivel)
        assertEquals(ModoApresentacao.COMPACTA, viewModel.state.corrida.modo)
        viewModel.abrirConfiguracoes()
        assertTrue(viewModel.state.configuracoesVisivel)
        viewModel.aplicarNovaCorrida(analiseFake(valor = 22.0))
        assertFalse(viewModel.state.configuracoesVisivel)
        assertTrue(viewModel.state.ofertaAtiva)
    }

    @Test
    fun calendario_mes_e_semana_usam_data_civil() {
        val viewModel = novoViewModel()
        viewModel.selecionarDiaHistorico(java.time.LocalDate.of(2026, 1, 31).toEpochDay())
        viewModel.avancarMesHistorico(1)
        assertEquals(java.time.LocalDate.of(2026, 2, 28), viewModel.state.historicoDia)
        viewModel.avancarSemanaHistorico(1)
        assertEquals(java.time.LocalDate.of(2026, 3, 7), viewModel.state.historicoDia)
    }

    @Test
    fun abrir_historico_inicia_dia_atual_todos_sem_mexer_periodo_dashboard() {
        val viewModel = novoViewModel()
        viewModel.iniciarMonitoramento()
        viewModel.selecionarPeriodoHistorico("MES")
        val diaDashboard = viewModel.state.historicoDia
        viewModel.alternarHistorico()
        assertTrue(viewModel.state.historicoVisivel)
        assertEquals("Todos", viewModel.state.abaHistorico)
        assertEquals(br.com.gestordriver.core.CalendarioPeriodo.MES, viewModel.state.calendarioPeriodo)
        assertEquals(
            br.com.gestordriver.core.CalendarioApp.hoje(),
            viewModel.state.historicoDia,
        )
        viewModel.alternarHistorico()
        assertFalse(viewModel.state.historicoVisivel)
        assertEquals(br.com.gestordriver.core.CalendarioPeriodo.MES, viewModel.state.calendarioPeriodo)
        assertEquals(diaDashboard, viewModel.state.historicoDia)
    }

    @Test
    fun setas_do_dashboard_seguem_modo_dia_semana_mes() {
        val viewModel = novoViewModel()
        viewModel.selecionarDiaHistorico(java.time.LocalDate.of(2026, 9, 2).toEpochDay())
        viewModel.selecionarPeriodoHistorico("DIA")
        viewModel.avancarPeriodoHistorico(1)
        assertEquals(java.time.LocalDate.of(2026, 9, 3), viewModel.state.historicoDia)
        viewModel.selecionarPeriodoHistorico("SEMANA")
        viewModel.avancarPeriodoHistorico(1)
        assertEquals(java.time.LocalDate.of(2026, 9, 10), viewModel.state.historicoDia)
        viewModel.selecionarPeriodoHistorico("MES")
        viewModel.avancarPeriodoHistorico(1)
        assertEquals(java.time.LocalDate.of(2026, 10, 10), viewModel.state.historicoDia)
        assertEquals(br.com.gestordriver.core.CalendarioPeriodo.MES, viewModel.state.calendarioPeriodo)
    }

    @Test
    fun abas_do_historico_andam_com_seta_e_modo() {
        val viewModel = novoViewModel()
        assertEquals(br.com.gestordriver.core.CalendarioPeriodo.DIA, viewModel.state.calendarioPeriodo)
        OverlayBridge.emitir(
            OverlayAcao.HistoricoModo(
                br.com.gestordriver.core.CalendarioPeriodo.DIA.vizinho(1).name,
            ),
        )
        assertEquals(br.com.gestordriver.core.CalendarioPeriodo.SEMANA, viewModel.state.calendarioPeriodo)
        OverlayBridge.emitir(OverlayAcao.HistoricoModo("MES"))
        assertEquals(br.com.gestordriver.core.CalendarioPeriodo.MES, viewModel.state.calendarioPeriodo)
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
        assertTrue(viewModel.state.seloFlutuante)
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