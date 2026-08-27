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
}
