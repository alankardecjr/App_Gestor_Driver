package br.com.gestordriver.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculadoraCustosTest {
    @Test
    fun combustivel_zero_nao_calcula() {
        assertNull(CalculadoraCombustivel.calcular(16.0, 0.0, 6.0))
        assertNull(CalculadoraCombustivel.calcular(16.0, 12.0, 0.0))
    }

    @Test
    fun gasto_soma_combustivel_oleo_e_pneu() {
        val config = ConfiguracaoUsuario.padrao().copy(
            consumoGasolina = 10.0,
            precoGasolina = 5.0,
            oleoValor = 100.0,
            oleoKilometragem = 1000.0,
            pneuDianteiroValor = 200.0,
            pneuDianteiroRodagem = 10000.0,
            pneuTraseiroValor = 300.0,
            pneuTraseiroRodagem = 10000.0,
            ipvaValor = 1000.0,
            seguroValor = 2000.0,
            kmAnual = 20000.0,
        )
        val corrida = Corrida(valorTotal = 38.0, kmAtePassageiro = 4.0, kmViagem = 6.0)
        val analise = CalculadoraCorrida(configuracaoUsuario = config).calcular(corrida)
        assertEquals(1.0, analise.combustivelEstimado!!, 0.001)
        val combustivel = 5.0
        val oleo = 1.0
        val pneus = 0.5
        // Seguro/IPVA não entram no gasto da corrida (só no Dashboard).
        assertEquals(combustivel + oleo + pneus, analise.custoCombustivel!!, 0.001)
        assertEquals(31.5, analise.valorTotal - analise.custoCombustivel!!, 0.001)
    }

    @Test
    fun energia_usa_km_por_kwh_com_perda_de_recarga() {
        val config = ConfiguracaoUsuario.padrao().copy(
            combustivel = Combustivel.ENERGIA,
            consumoEnergia = 5.0,
            precoEnergia = 1.0,
            oleoValor = 0.0,
            pneuDianteiroValor = 0.0,
            pneuTraseiroValor = 0.0,
        )
        val analise = CalculadoraCorrida(configuracaoUsuario = config).calcular(
            Corrida(valorTotal = 38.0, kmAtePassageiro = 4.0, kmViagem = 6.0),
        )
        // 10 km ÷ 5 km/kWh × 1,12 = 2,24 kWh × R$ 1 = R$ 2,24
        assertEquals(2.24, analise.combustivelEstimado!!, 0.001)
        assertEquals(2.24, analise.custoCombustivel!!, 0.001)
    }

    @Test
    fun sem_dados_gasto_fica_nulo() {
        val config = ConfiguracaoUsuario.padrao().copy(
            consumoGasolina = 0.0,
            precoGasolina = 0.0,
        )
        val analise = CalculadoraCorrida(configuracaoUsuario = config).calcular(
            Corrida(valorTotal = 38.0, kmAtePassageiro = 4.0, kmViagem = 6.0),
        )
        assertNull(analise.combustivelEstimado)
        assertNull(analise.custoCombustivel)
    }
}
