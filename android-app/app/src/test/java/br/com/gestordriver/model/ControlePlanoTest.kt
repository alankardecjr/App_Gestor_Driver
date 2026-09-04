package br.com.gestordriver.model

import br.com.gestordriver.core.CalculadoraCorrida
import br.com.gestordriver.core.Corrida
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ControlePlanoTest {

    private val analise = CalculadoraCorrida().calcular(
        Corrida(valorTotal = 38.0, kmAtePassageiro = 3.0, kmViagem = 12.0),
    )

    @Test
    fun pro_e_beta_liberam_calculadora() {
        val controle = ControlePlano()
        val pro = controle.aplicar(analise, PlanoAcesso.PRO)
        val beta = controle.aplicar(analise, PlanoAcesso.BETA)
        assertTrue(pro.exibeValorPorKm)
        assertTrue(pro.exibeCustoCombustivel)
        assertTrue(pro.recursosAvancados)
        assertTrue(beta.exibeValorPorKm)
        assertTrue(beta.recursosAvancados)
        assertTrue(PlanoAcesso.PRO.ehPro)
        assertTrue(PlanoAcesso.BETA.ehPro)
        assertFalse(PlanoAcesso.PRO.travaCalculadora)
    }

    @Test
    fun free_trava_calculadora() {
        val recursos = ControlePlano().aplicar(analise, PlanoAcesso.FREE)
        assertFalse(recursos.exibeValorPorKm)
        assertFalse(recursos.exibeCombustivelEstimado)
        assertFalse(recursos.exibeCustoCombustivel)
        assertFalse(recursos.recursosAvancados)
        assertTrue(PlanoAcesso.FREE.travaCalculadora)
        assertFalse(PlanoAcesso.FREE.ehPro)
    }
}
