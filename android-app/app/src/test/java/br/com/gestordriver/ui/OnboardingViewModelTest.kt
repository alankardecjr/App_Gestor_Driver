package br.com.gestordriver.ui

import br.com.gestordriver.data.MemoriaOnboardingStore
import br.com.gestordriver.model.OnboardingEtapa
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingViewModelTest {
    private fun novo(concluido: Boolean): AppViewModel = AppViewModel(
        onboardingStore = MemoriaOnboardingStore(inicial = concluido),
        coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
    )

    @Test
    fun primeiro_uso_sem_permissao_abre_configurar_com_aviso() {
        val viewModel = novo(concluido = false)
        viewModel.avaliarInicio(permissoesOk = false, temConta = false)
        assertEquals(OnboardingEtapa.NENHUMA, viewModel.state.onboardingEtapa)
        assertTrue(viewModel.state.configuracoesVisivel)
        assertEquals(2, viewModel.state.abaConfiguracao)
        assertTrue(viewModel.state.avisoSemMonitoramento)
        assertTrue(viewModel.state.destacarPermissoes)
        assertFalse(viewModel.state.monitorando)
        assertFalse(viewModel.state.interfaceOculta)
    }

    @Test
    fun com_permissao_sem_conta_pede_conta() {
        val viewModel = novo(concluido = false)
        viewModel.avaliarInicio(permissoesOk = true, temConta = false)
        assertEquals(OnboardingEtapa.CONTA, viewModel.state.onboardingEtapa)
    }

    @Test
    fun com_permissao_e_conta_abre_opcoes_e_monitora() {
        val viewModel = novo(concluido = false)
        viewModel.avaliarInicio(permissoesOk = true, temConta = true)
        assertEquals(OnboardingEtapa.NENHUMA, viewModel.state.onboardingEtapa)
        assertTrue(viewModel.state.opcoesVisivel)
        assertTrue(viewModel.state.monitorando)
    }

    @Test
    fun onboarding_ja_concluido_com_permissao_abre_opcoes() {
        val viewModel = novo(concluido = true)
        viewModel.avaliarInicio(permissoesOk = true, temConta = true)
        assertEquals(OnboardingEtapa.NENHUMA, viewModel.state.onboardingEtapa)
        assertTrue(viewModel.state.monitorando)
        assertTrue(viewModel.state.opcoesVisivel)
    }

    @Test
    fun perms_ok_apos_destaque_mantem_configurar_e_monitora() {
        val viewModel = novo(concluido = true)
        viewModel.avaliarInicio(permissoesOk = false, temConta = true)
        assertTrue(viewModel.state.configuracoesVisivel)
        assertFalse(viewModel.state.monitorando)

        viewModel.avaliarInicio(permissoesOk = true, temConta = true)
        assertTrue(viewModel.state.monitorando)
        assertTrue(viewModel.state.configuracoesVisivel)
        assertFalse(viewModel.state.avisoSemMonitoramento)
        assertFalse(viewModel.state.opcoesVisivel)
    }

    @Test
    fun iniciar_monitoramento_nao_liga_sem_perms_resolvidas() {
        val viewModel = novo(concluido = false)
        viewModel.avaliarInicio(permissoesOk = false, temConta = false)
        viewModel.iniciarMonitoramento()
        assertTrue(viewModel.state.configuracoesVisivel)
        assertTrue(viewModel.state.avisoSemMonitoramento)
        assertFalse(viewModel.state.monitorando)
    }
}
