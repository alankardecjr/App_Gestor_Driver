package br.com.gestordriver.core

import org.junit.Assert.assertEquals
import org.junit.Test

class SemaforoOfertaTest {
    @Test
    fun hora_verde_quando_atinge_a_meta() {
        assertEquals("#2E7D32", SemaforoOferta.corPorMeta(true))
    }

    @Test
    fun hora_vermelha_quando_fica_abaixo_da_meta() {
        assertEquals("#C62828", SemaforoOferta.corPorMeta(false))
    }

    @Test
    fun hora_neutra_sem_meta() {
        assertEquals(ClassificacaoConstantes.COR_BORDA_NEUTRA, SemaforoOferta.corPorMeta(null))
    }

    @Test
    fun hora_tem_tres_faixas_em_torno_da_meta() {
        val meta = 40.0
        assertEquals("#2E7D32", SemaforoOferta.corPorFaixaHora(40.0, meta))
        assertEquals("#F9A825", SemaforoOferta.corPorFaixaHora(32.0, meta))
        assertEquals("#C62828", SemaforoOferta.corPorFaixaHora(31.0, meta))
        assertEquals(ClassificacaoConstantes.COR_BORDA_NEUTRA, SemaforoOferta.corPorFaixaHora(50.0, 0.0))
        assertEquals(ClassificacaoConstantes.COR_BORDA_NEUTRA, SemaforoOferta.corPorFaixaHora(null, meta))
    }

    @Test
    fun borda_usa_a_pior_das_duas_faixas() {
        val verde = "#2E7D32"
        val vermelho = "#C62828"
        assertEquals(vermelho, SemaforoOferta.pior(verde, vermelho))
        assertEquals(verde, SemaforoOferta.pior(verde, ClassificacaoConstantes.COR_BORDA_NEUTRA))
    }
}
