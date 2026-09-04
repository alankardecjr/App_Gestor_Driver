package br.com.gestordriver.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class CalendarioAppTest {
    @Test
    fun semana_comeca_no_domingo_e_termina_no_sabado() {
        val quarta = LocalDate.of(2026, 9, 2)
        val dias = CalendarioApp.diasDaSemana(quarta)
        assertEquals(7, dias.size)
        assertEquals(DayOfWeek.SUNDAY, dias.first().dayOfWeek)
        assertEquals(DayOfWeek.SATURDAY, dias.last().dayOfWeek)
        assertEquals(LocalDate.of(2026, 8, 30), dias.first())
        assertEquals(LocalDate.of(2026, 9, 5), dias.last())
    }

    @Test
    fun avancar_mes_respeita_ultimo_dia() {
        val trintaUm = LocalDate.of(2026, 1, 31)
        assertEquals(LocalDate.of(2026, 2, 28), CalendarioApp.avancarMes(trintaUm, 1))
        assertEquals(LocalDate.of(2026, 3, 31), CalendarioApp.avancarMes(trintaUm, 2))
    }

    @Test
    fun grade_do_mes_tem_seis_semanas_reais() {
        val grade = CalendarioApp.gradeMes(LocalDate.of(2026, 9, 2))
        assertEquals(42, grade.size)
        assertEquals(DayOfWeek.SUNDAY, grade.first().dayOfWeek)
        assertTrue(grade.contains(LocalDate.of(2026, 9, 1)))
        assertTrue(grade.contains(LocalDate.of(2026, 9, 30)))
    }

    @Test
    fun epoch_zero_volta_para_hoje() {
        assertEquals(CalendarioApp.hoje(), CalendarioApp.diaDe(0L))
        assertEquals(LocalDate.of(2026, 9, 2), CalendarioApp.diaDe(LocalDate.of(2026, 9, 2).toEpochDay()))
    }

    @Test
    fun cabecalho_do_historico_mostra_semana_completa() {
        val dia = LocalDate.of(2026, 9, 2)
        val titulo = CalendarioApp.rotuloPeriodoCabecalho(dia, CalendarioPeriodo.SEMANA)
        assertTrue(titulo.contains("2026"))
        assertTrue(titulo.contains("–"))
        val sub = CalendarioApp.subtituloPeriodo(dia, CalendarioPeriodo.SEMANA)
        if (CalendarioApp.noPeriodo(CalendarioApp.hoje(), dia, CalendarioPeriodo.SEMANA)) {
            assertEquals("Semana atual", sub)
        } else {
            assertEquals("", sub)
        }
        assertEquals(7, CalendarioApp.faixaDiasVisivel(dia, CalendarioPeriodo.SEMANA).size)
        assertEquals(42, CalendarioApp.faixaDiasVisivel(dia, CalendarioPeriodo.MES).size)
    }

    @Test
    fun setas_das_abas_mudam_dia_semana_mes_ano() {
        assertEquals(CalendarioPeriodo.SEMANA, CalendarioPeriodo.DIA.vizinho(1))
        assertEquals(CalendarioPeriodo.MES, CalendarioPeriodo.SEMANA.vizinho(1))
        assertEquals(CalendarioPeriodo.ANO, CalendarioPeriodo.MES.vizinho(1))
        assertEquals(CalendarioPeriodo.ANO, CalendarioPeriodo.ANO.vizinho(1))
        assertEquals(CalendarioPeriodo.DIA, CalendarioPeriodo.DIA.vizinho(-1))
        assertEquals(CalendarioPeriodo.DIA, CalendarioPeriodo.SEMANA.vizinho(-1))
    }

    @Test
    fun setas_seguem_o_periodo_escolhido() {
        val dia = LocalDate.of(2026, 9, 2)
        assertEquals(LocalDate.of(2026, 9, 3), CalendarioApp.avancarPeriodo(dia, CalendarioPeriodo.DIA, 1))
        assertEquals(LocalDate.of(2026, 9, 9), CalendarioApp.avancarPeriodo(dia, CalendarioPeriodo.SEMANA, 1))
        assertEquals(LocalDate.of(2026, 10, 2), CalendarioApp.avancarPeriodo(dia, CalendarioPeriodo.MES, 1))
        assertEquals(LocalDate.of(2027, 9, 2), CalendarioApp.avancarPeriodo(dia, CalendarioPeriodo.ANO, 1))
        assertTrue(CalendarioApp.noPeriodo(LocalDate.of(2026, 8, 30), dia, CalendarioPeriodo.SEMANA))
        assertFalse(CalendarioApp.noPeriodo(LocalDate.of(2026, 9, 6), dia, CalendarioPeriodo.SEMANA))
        assertTrue(CalendarioApp.noPeriodo(LocalDate.of(2026, 9, 30), dia, CalendarioPeriodo.MES))
        assertTrue(CalendarioApp.noPeriodo(LocalDate.of(2026, 12, 31), dia, CalendarioPeriodo.ANO))
        assertFalse(CalendarioApp.noPeriodo(LocalDate.of(2027, 1, 1), dia, CalendarioPeriodo.ANO))
    }

    @Test
    fun meses_do_ano_para_seletor_do_dashboard() {
        val meses = CalendarioApp.mesesDoAno(LocalDate.of(2026, 9, 15))
        assertEquals(12, meses.size)
        assertEquals(LocalDate.of(2026, 1, 1), meses.first())
        assertEquals(LocalDate.of(2026, 12, 1), meses.last())
        assertEquals("SET", CalendarioApp.rotuloMesChip(LocalDate.of(2026, 9, 1)))
    }

    @Test
    fun rotulos_usam_mes_e_semana_civis() {
        val dia = LocalDate.of(2026, 9, 2)
        val mesAno = CalendarioApp.rotuloMesAno(dia)
        assertTrue(mesAno.contains("2026"))
        assertTrue(mesAno.first().isUpperCase())
        assertTrue(CalendarioApp.rotuloSemana(dia).contains("30"))
        assertTrue(CalendarioApp.rotuloSemana(dia).contains("5"))
        assertTrue(CalendarioApp.noMes(dia, LocalDate.of(2026, 9, 15)))
        assertFalse(CalendarioApp.noMes(dia, LocalDate.of(2026, 8, 31)))
    }
}
