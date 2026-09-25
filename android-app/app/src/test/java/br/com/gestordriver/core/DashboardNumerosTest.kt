package br.com.gestordriver.core

import br.com.gestordriver.model.ConfiguracaoUsuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DashboardNumerosTest {
    private val abril = LocalDate.of(2026, 4, 15)
    private val bissexto = LocalDate.of(2024, 2, 15)

    @Test
    fun seguro_mensal_na_semana_usa_os_dias_do_mes() {
        val config = ConfiguracaoUsuario.padrao().copy(
            seguroValor = 300.0,
            oleoValor = 0.0,
        )
        val numeros = DashboardNumeros.de(
            listOf(CorridaParaResumo(100.0, 10.0, 30, 5.0)),
            config,
            dia = abril,
            periodo = CalendarioPeriodo.SEMANA,
        )
        assertEquals(5.0, numeros.combustivel!!, 0.001)
        assertEquals(70.0, numeros.seguro!!, 0.001) // abril tem 30 dias: 300 * 7/30
        assertNull(numeros.oleo)
        assertEquals(75.0, numeros.despesas, 0.001)
    }

    @Test
    fun seguro_e_ipva_fecham_no_mes_e_no_ano() {
        assertEquals(300.0, DashboardNumeros.parcelaSeguro(300.0, abril, CalendarioPeriodo.MES)!!, 0.001)
        assertEquals(3600.0, DashboardNumeros.parcelaSeguro(300.0, abril, CalendarioPeriodo.ANO)!!, 0.001)
        assertEquals(10.0, DashboardNumeros.parcelaSeguro(300.0, abril, CalendarioPeriodo.DIA)!!, 0.001)
        assertNull(DashboardNumeros.parcelaSeguro(0.0, abril, CalendarioPeriodo.MES))
        assertEquals(100.0, DashboardNumeros.parcelaIpva(1200.0, abril, CalendarioPeriodo.MES)!!, 0.001)
        assertEquals(1200.0, DashboardNumeros.parcelaIpva(1200.0, abril, CalendarioPeriodo.ANO)!!, 0.001)
        assertEquals(1200.0 / 365.0, DashboardNumeros.parcelaIpva(1200.0, abril, CalendarioPeriodo.DIA)!!, 0.001)
        assertEquals(1200.0 / 366.0, DashboardNumeros.parcelaIpva(1200.0, bissexto, CalendarioPeriodo.DIA)!!, 0.001)
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
