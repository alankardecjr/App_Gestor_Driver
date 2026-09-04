package br.com.gestordriver.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Month

class TabelaIpvaPlacaTest {
    @Test
    fun final_define_mes_de_vencimento() {
        assertEquals(Month.JANUARY, TabelaIpvaPlaca.mesVencimento("1"))
        assertEquals(Month.MAY, TabelaIpvaPlaca.mesVencimento("5"))
        assertEquals(Month.MARCH, TabelaIpvaPlaca.mesVencimento("ABC5D23"))
        assertEquals(Month.OCTOBER, TabelaIpvaPlaca.mesVencimento("0"))
        assertEquals("Janeiro", TabelaIpvaPlaca.rotuloMesVencimento("1"))
        assertNull(TabelaIpvaPlaca.mesVencimento(""))
        assertNull(TabelaIpvaPlaca.digitoFinal("ABC"))
    }
}
