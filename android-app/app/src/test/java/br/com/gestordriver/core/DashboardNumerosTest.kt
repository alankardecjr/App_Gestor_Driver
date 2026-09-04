package br.com.gestordriver.core

import br.com.gestordriver.model.ConfiguracaoUsuario
import br.com.gestordriver.model.SeguroRecorrencia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DashboardNumerosTest {
    @Test
    fun seguro_mensal_rateia_por_dias_do_periodo() {
        val config = ConfiguracaoUsuario.padrao().copy(
            seguroValor = 300.0,
            seguroRecorrencia = SeguroRecorrencia.MENSAL,
            oleoValor = 0.0,
        )
        val numeros = DashboardNumeros.de(
            listOf(CorridaParaResumo(100.0, 10.0, 30, 5.0)),
            config,
            diasPeriodo = 7,
        )
        assertEquals(5.0, numeros.combustivel!!, 0.001)
        assertEquals(70.0, numeros.seguro!!, 0.001) // 300 * 7/30
        assertNull(numeros.oleo)
        assertEquals(75.0, numeros.despesas, 0.001)
    }

    @Test
    fun seguro_anual_rateia_por_dias_do_ano() {
        val config = ConfiguracaoUsuario.padrao().copy(
            seguroValor = 3650.0,
            seguroRecorrencia = SeguroRecorrencia.ANUAL,
        )
        assertEquals(10.0, DashboardNumeros.rateioSeguro(3650.0, SeguroRecorrencia.ANUAL, 1)!!, 0.001)
        assertEquals(70.0, DashboardNumeros.rateioSeguro(300.0, SeguroRecorrencia.MENSAL, 7)!!, 0.001)
        assertNull(DashboardNumeros.rateioSeguro(0.0, SeguroRecorrencia.ANUAL, 30))
        assertEquals(10.0, DashboardNumeros.rateioAnual(3650.0, 1)!!, 0.001)
    }

    @Test
    fun alerta_oleo_500_km_antes() {
        assertEquals(AlertaOleo.Nivel.OK, AlertaOleo.nivel(10000.0, 9000.0))
        assertEquals(AlertaOleo.Nivel.AVISO, AlertaOleo.nivel(10000.0, 9600.0))
        assertEquals(AlertaOleo.Nivel.VENCIDO, AlertaOleo.nivel(10000.0, 10000.0))
        assertEquals(AlertaOleo.Nivel.OK, AlertaOleo.nivel(0.0, 100.0))
    }

    @Test
    fun km_desde_troca_soma_apos_data() {
        val pontos = listOf(
            java.time.LocalDate.of(2026, 1, 1) to 5.0,
            java.time.LocalDate.of(2026, 2, 1) to 7.0,
            java.time.LocalDate.of(2025, 12, 1) to 100.0,
        )
        assertEquals(12.0, AlertaOleo.kmDesdeTroca("01/01/2026", pontos), 0.001)
        assertEquals(null, AlertaOleo.parseData(""))
    }
}
