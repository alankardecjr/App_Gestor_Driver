package br.com.gestordriver.ui

import br.com.gestordriver.data.MemoriaOnboardingStore
import br.com.gestordriver.model.OnboardingEtapa
import br.com.gestordriver.model.TutorialConteudo
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
    fun primeiro_uso_sem_permissao_abre_passo_permissoes() {
        val viewModel = novo(concluido = false)
        viewModel.avaliarInicio(permissoesOk = false, temConta = false)
        assertEquals(OnboardingEtapa.PERMISSOES, viewModel.state.onboardingEtapa)
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
    fun com_permissao_e_conta_abre_tutorial() {
        val viewModel = novo(concluido = false)
        viewModel.avaliarInicio(permissoesOk = true, temConta = true)
        assertEquals(OnboardingEtapa.TUTORIAL, viewModel.state.onboardingEtapa)
        assertEquals(0, viewModel.state.tutorialPasso)
    }

    @Test
    fun seguir_tutorial_ate_o_fim_inicia_monitoramento() {
        val viewModel = novo(concluido = false)
        viewModel.avaliarInicio(permissoesOk = true, temConta = true)
        repeat(TutorialConteudo.passos.size) {
            viewModel.tutorialSeguir()
        }
        assertEquals(OnboardingEtapa.NENHUMA, viewModel.state.onboardingEtapa)
        assertTrue(viewModel.state.monitorando)
    }

    @Test
    fun pular_tutorial_inicia_monitoramento() {
        val viewModel = novo(concluido = false)
        viewModel.avaliarInicio(permissoesOk = true, temConta = true)
        viewModel.tutorialPular()
        assertEquals(OnboardingEtapa.NENHUMA, viewModel.state.onboardingEtapa)
        assertTrue(viewModel.state.monitorando)
    }

    @Test
    fun onboarding_ja_concluido_com_permissao_monitora() {
        val viewModel = novo(concluido = true)
        viewModel.avaliarInicio(permissoesOk = true, temConta = true)
        assertEquals(OnboardingEtapa.NENHUMA, viewModel.state.onboardingEtapa)
        assertTrue(viewModel.state.monitorando)
    }

    @Test
    fun iniciar_monitoramento_nao_fura_onboarding() {
        val viewModel = novo(concluido = false)
        viewModel.avaliarInicio(permissoesOk = false, temConta = false)
        viewModel.iniciarMonitoramento()
        assertEquals(OnboardingEtapa.PERMISSOES, viewModel.state.onboardingEtapa)
        assertFalse(viewModel.state.monitorando)
    }
}
