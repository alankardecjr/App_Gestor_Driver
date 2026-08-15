package br.com.gestordriver.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import br.com.gestordriver.model.ModoApresentacao
import br.com.gestordriver.model.PlanoAcesso

class AppViewModelTest {
    @Test
    fun deve_alternar_detalhes_e_historico() {
        val viewModel = AppViewModel()

        assertEquals(ModoApresentacao.COMPACTA, viewModel.state.corrida.modo)
        assertFalse(viewModel.state.historicoVisivel)

        viewModel.alternarDetalhes()

        assertEquals(ModoApresentacao.DETALHES, viewModel.state.corrida.modo)
        assertEquals("Menos detalhes", viewModel.state.corrida.acaoDetalhes)

        viewModel.alternarHistorico()

        assertTrue(viewModel.state.historicoVisivel)
    }

    @Test
    fun deve_selecionar_plano_e_controlar_overlay() {
        val viewModel = AppViewModel()

        viewModel.selecionarPlano(PlanoAcesso.PRO)
        assertEquals(PlanoAcesso.PRO, viewModel.state.plano)
        assertEquals("Ativo", viewModel.state.corrida.camposDetalhes.last().valor)

        viewModel.semNotificacao()
        assertFalse(viewModel.state.notificacaoDisponivel)
        assertTrue(viewModel.state.seloFlutuante)
        assertTrue(viewModel.state.monitorando)

        viewModel.reabrirInterface()
        assertFalse(viewModel.state.interfaceOculta)
        assertFalse(viewModel.state.seloFlutuante)
    }

    @Test
    fun ocultar_mantem_monitoramento_e_salva_estado() {
        val viewModel = AppViewModel()

        viewModel.alternarDetalhes()
        viewModel.alternarHistorico()

        viewModel.ocultarInterface()

        assertTrue(viewModel.state.interfaceOculta)
        assertTrue(viewModel.state.seloFlutuante)
        assertTrue(viewModel.state.monitorando)
        assertEquals(ModoApresentacao.DETALHES, viewModel.state.estadoSalvo?.modo)
        assertTrue(viewModel.state.estadoSalvo?.historicoVisivel == true)

        viewModel.reabrirInterface()

        assertFalse(viewModel.state.interfaceOculta)
        assertEquals(ModoApresentacao.DETALHES, viewModel.state.corrida.modo)
        assertTrue(viewModel.state.historicoVisivel)
    }

    @Test
    fun fechar_para_monitoramento() {
        val viewModel = AppViewModel()

        viewModel.solicitarFecharApp()
        assertTrue(viewModel.state.confirmacaoFecharVisivel)

        viewModel.confirmarFecharApp()

        assertFalse(viewModel.state.monitorando)
        assertFalse(viewModel.state.confirmacaoFecharVisivel)
    }

    @Test
    fun deve_selecionar_corrida_do_historico() {
        val viewModel = AppViewModel()

        viewModel.alternarDetalhes()
        viewModel.alternarHistorico()

        val item = viewModel.state.historico[1]
        viewModel.selecionarHistorico(item)

        assertFalse(viewModel.state.historicoVisivel)
        assertEquals(ModoApresentacao.DETALHES, viewModel.state.corrida.modo)
        assertEquals("Menos detalhes", viewModel.state.corrida.acaoDetalhes)
        assertEquals(item, viewModel.state.historicoSelecionado)
        assertEquals(item.valorTotal, viewModel.state.analiseAtual?.valorTotal)
        assertEquals(item.plataforma, viewModel.state.analiseAtual?.plataforma)
        assertEquals("1.5 km", viewModel.state.corrida.camposDetalhes[0].valor)
        assertTrue(viewModel.state.monitorando)
    }

    @Test
    fun fluxo_expandida_historico_selecionar() {
        val viewModel = AppViewModel()

        viewModel.alternarDetalhes()
        assertEquals(ModoApresentacao.DETALHES, viewModel.state.corrida.modo)

        viewModel.alternarHistorico()
        assertTrue(viewModel.state.historicoVisivel)

        viewModel.selecionarHistorico(viewModel.state.historico.first())

        assertFalse(viewModel.state.historicoVisivel)
        assertEquals(ModoApresentacao.DETALHES, viewModel.state.corrida.modo)
        assertEquals("Uber", viewModel.state.analiseAtual?.plataforma)
    }

    @Test
    fun fluxo_ocultar_apos_historico_reabre_compacta() {
        val viewModel = AppViewModel()

        viewModel.alternarDetalhes()
        viewModel.alternarHistorico()
        viewModel.alternarDetalhes()

        assertEquals(ModoApresentacao.COMPACTA, viewModel.state.corrida.modo)

        viewModel.ocultarInterface()
        assertTrue(viewModel.state.seloFlutuante)

        viewModel.reabrirInterface()

        assertFalse(viewModel.state.interfaceOculta)
        assertEquals(ModoApresentacao.COMPACTA, viewModel.state.corrida.modo)
        assertTrue(viewModel.state.historicoVisivel)
        assertTrue(viewModel.state.monitorando)
    }

    @Test
    fun fluxo_config_abrir_salvar_voltar() {
        val viewModel = AppViewModel()

        viewModel.alternarDetalhes()
        viewModel.abrirConfiguracoes()

        assertTrue(viewModel.state.configuracoesVisivel)
        assertFalse(viewModel.state.historicoVisivel)

        viewModel.fecharConfiguracoes()

        assertFalse(viewModel.state.configuracoesVisivel)
        assertEquals(ModoApresentacao.DETALHES, viewModel.state.corrida.modo)
    }

    @Test
    fun config_e_historico_sao_exclusivos() {
        val viewModel = AppViewModel()

        viewModel.alternarDetalhes()
        viewModel.abrirConfiguracoes()
        assertTrue(viewModel.state.configuracoesVisivel)

        viewModel.alternarHistorico()

        assertTrue(viewModel.state.historicoVisivel)
        assertFalse(viewModel.state.configuracoesVisivel)
    }

    @Test
    fun ocultar_fecha_configuracao_e_expandida() {
        val viewModel = AppViewModel()

        viewModel.alternarDetalhes()
        viewModel.abrirConfiguracoes()
        viewModel.ocultarInterface()

        assertTrue(viewModel.state.interfaceOculta)
        assertTrue(viewModel.state.seloFlutuante)
        assertFalse(viewModel.state.configuracoesVisivel)
        assertEquals(ModoApresentacao.COMPACTA, viewModel.state.corrida.modo)
        assertTrue(viewModel.state.estadoSalvo?.configuracoesVisivel == true)

        viewModel.reabrirInterface()

        assertTrue(viewModel.state.configuracoesVisivel)
        assertEquals(ModoApresentacao.DETALHES, viewModel.state.corrida.modo)
    }

    @Test
    fun fluxo_fechar_com_confirmacao() {
        val viewModel = AppViewModel()

        viewModel.solicitarFecharApp()
        assertTrue(viewModel.state.confirmacaoFecharVisivel)

        viewModel.cancelarFecharApp()
        assertFalse(viewModel.state.confirmacaoFecharVisivel)
        assertTrue(viewModel.state.monitorando)

        viewModel.solicitarFecharApp()
        viewModel.confirmarFecharApp()

        assertFalse(viewModel.state.monitorando)
    }
}
