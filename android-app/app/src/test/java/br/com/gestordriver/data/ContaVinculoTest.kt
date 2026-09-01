package br.com.gestordriver.data

import br.com.gestordriver.model.ConfiguracaoUsuario
import br.com.gestordriver.model.TipoContaVinculada
import br.com.gestordriver.ui.ConfiguracoesViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContaVinculoTest {
    @Test
    fun email_valido_aceita_formato_comum() {
        assertTrue(ContaVinculo.emailValido("motorista@gmail.com"))
        assertTrue(ContaVinculo.emailValido("  a@b.co  "))
    }

    @Test
    fun email_invalido_rejeita() {
        assertFalse(ContaVinculo.emailValido(""))
        assertFalse(ContaVinculo.emailValido("sem-arroba"))
        assertFalse(ContaVinculo.emailValido("@dominio.com"))
        assertFalse(ContaVinculo.emailValido("nome@"))
        assertFalse(ContaVinculo.emailValido("nome@dominio"))
    }

    @Test
    fun persistir_email_nao_grava_rascunho_do_veiculo() {
        val store = MemoriaConfiguracaoStore()
        val viewModel = ConfiguracoesViewModel(store)
        viewModel.atualizarMarca("Honda")
        assertTrue(viewModel.conectarContaEmail("motorista@teste.com"))
        assertEquals("Honda", viewModel.configuracao.marcaVeiculo)
        assertEquals(TipoContaVinculada.EMAIL, viewModel.configuracao.contaTipo)
        assertEquals("motorista@teste.com", viewModel.configuracao.contaEmail)
        assertEquals("Toyota", store.carregar().marcaVeiculo)
        assertEquals(TipoContaVinculada.EMAIL, store.carregar().contaTipo)
    }

    @Test
    fun email_invalido_nao_altera_store() {
        val store = MemoriaConfiguracaoStore()
        val viewModel = ConfiguracoesViewModel(store)
        assertFalse(viewModel.conectarContaEmail("invalido"))
        assertEquals(TipoContaVinculada.NENHUMA, store.carregar().contaTipo)
        assertEquals("", store.carregar().contaEmail)
    }

    @Test
    fun mapa_inclui_conta_vinculada() {
        val original = ConfiguracaoUsuario.padrao().copy(
            contaTipo = TipoContaVinculada.GOOGLE,
            contaEmail = "user@gmail.com",
        )
        assertEquals(original, original.paraPreferencias().paraConfiguracaoUsuario())
    }
}
