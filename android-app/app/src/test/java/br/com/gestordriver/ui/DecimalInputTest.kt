package br.com.gestordriver.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DecimalInputTest {
    @Test
    fun aceita_virgula_e_ponto() {
        assertEquals(6.19, DecimalInput.parse("6,19")!!, 0.0001)
        assertEquals(6.19, DecimalInput.parse("6.19")!!, 0.0001)
        assertEquals(12.5, DecimalInput.parse("12,5")!!, 0.0001)
    }

    @Test
    fun vazio_retorna_nulo() {
        assertNull(DecimalInput.parse(""))
        assertNull(DecimalInput.parse(","))
    }

    @Test
    fun reais_aceitam_inteiro_ponto_e_milhar() {
        assertEquals(50.0, DecimalInput.parse("50")!!, 0.0001)
        assertEquals(50.0, DecimalInput.parse("50.00")!!, 0.0001)
        assertEquals(50.0, DecimalInput.parse("50,00")!!, 0.0001)
        assertEquals(50000.0, DecimalInput.parse("50000")!!, 0.0001)
        assertEquals(50000.0, DecimalInput.parse("50.000,00")!!, 0.0001)
        assertEquals(5000.0, DecimalInput.parse("5.000")!!, 0.0001)
        assertEquals("50,00", DecimalInput.formatarReais(50.0))
        assertEquals("50,00", DecimalInput.formatarReais(50.00))
        assertEquals("50.000,00", DecimalInput.formatarReais(50000.0))
        assertEquals("", DecimalInput.formatarReais(0.0))
    }

    @Test
    fun compacta_mostra_duas_casas() {
        assertEquals("1,80", DecimalInput.formatarDuasCasasExibicao("R$1,8"))
        assertEquals("3,30", DecimalInput.formatarDuasCasasExibicao("3,3"))
        assertEquals("139,05", DecimalInput.formatarDuasCasasExibicao("R$ 139,05"))
        assertEquals("1.234,50", DecimalInput.formatarDuasCasasExibicao("1234,5"))
        assertEquals("0,00", DecimalInput.formatarDuasCasasExibicao("0"))
        assertEquals("—", DecimalInput.formatarDuasCasasExibicao("—"))
        assertEquals("🔒", DecimalInput.formatarDuasCasasExibicao("🔒"))
        assertEquals("4,90", DecimalInput.formatarDuasCasasExibicao("4,9"))
        assertEquals("4,99", DecimalInput.formatarDuasCasasExibicao("4,99"))
        assertEquals("4,94", DecimalInput.formatarDuasCasasExibicao("4,94"))
    }

    @Test
    fun compacta_lucro_mostra_uma_casa() {
        assertEquals("85,7", DecimalInput.formatarUmaCasaExibicao("85,71"))
        assertEquals("85,7", DecimalInput.formatarUmaCasaExibicao("85,7%"))
        assertEquals("85,8", DecimalInput.formatarUmaCasaExibicao("85,75"))
        assertEquals("—", DecimalInput.formatarUmaCasaExibicao("—"))
    }

    @Test
    fun quantidade_agrupa_milhar_e_so_mostra_fracao_quando_existe() {
        assertEquals("5.000", DecimalInput.formatarQuantidade(5000.0))
        assertEquals("50.000", DecimalInput.formatarQuantidade(50000.0))
        assertEquals("12,5", DecimalInput.formatarQuantidade(12.5))
        assertEquals("12,55", DecimalInput.formatarQuantidade(12.55))
        assertEquals("", DecimalInput.formatarQuantidade(0.0))
        assertEquals(12.5, DecimalInput.parse("12.5")!!, 0.0001)
        assertEquals(12.5, DecimalInput.parse("12,5")!!, 0.0001)
    }

    @Test
    fun data_digitos_viram_dia_mes_ano() {
        assertEquals("01/05/2026", DataInput.formatar("01052026"))
        assertEquals("01/05/2026", DataInput.formatar("010526"))
        assertEquals("01/05/2026", DataInput.formatar("01/05/2026"))
        assertEquals("01/05/2026", DataInput.formatar("01/05/26"))
        assertEquals("29/02/2024", DataInput.formatar("29022024"))
        assertEquals("29022023", DataInput.formatar("29022023"))
        assertEquals("32012026", DataInput.formatar("32012026"))
        assertEquals("", DataInput.formatar(""))
        assertEquals("", DataInput.formatar("   "))
    }
}
