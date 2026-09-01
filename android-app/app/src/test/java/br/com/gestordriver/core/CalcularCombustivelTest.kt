package br.com.gestordriver.core

import br.com.gestordriver.model.Combustivel
import br.com.gestordriver.model.ConfiguracaoUsuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalcularCombustivelTest {
    @Test
    fun preco_por_litro_divide_valor_pelos_litros() {
        assertEquals(6.20, CalcularCombustivel.precoPorLitro(62.0, 10.0)!!, 0.0)
    }

    @Test
    fun consumo_divide_km_percorrido_pelos_litros() {
        assertEquals(12.50, CalcularCombustivel.consumoKmPorLitro(1000.0, 1125.0, 10.0)!!, 0.0)
    }

    @Test
    fun litros_zero_ou_km_invertido_nao_calcula() {
        assertNull(CalcularCombustivel.precoPorLitro(50.0, 0.0))
        assertNull(CalcularCombustivel.consumoKmPorLitro(200.0, 100.0, 10.0))
    }

    @Test
    fun aplicar_preenche_so_o_combustivel_atual() {
        val base = ConfiguracaoUsuario.padrao().copy(
            combustivel = Combustivel.ETANOL,
            abastecimentoValor = 50.0,
            abastecimentoLitros = 10.0,
            abastecimentoKmInicial = 1000.0,
            abastecimentoKmFinal = 1100.0,
            consumoEtanol = 9.0,
            precoEtanol = 4.39,
            consumoGasolina = 12.5,
            precoGasolina = 6.19,
        )
        val atualizada = base.aplicarCalculoAbastecimento()
        assertEquals(5.00, atualizada.precoEtanol, 0.0)
        assertEquals(10.00, atualizada.consumoEtanol, 0.0)
        assertEquals(12.5, atualizada.consumoGasolina, 0.0)
        assertEquals(6.19, atualizada.precoGasolina, 0.0)
    }
}
