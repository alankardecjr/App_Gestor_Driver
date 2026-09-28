package br.com.gestordriver.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChaveProTest {
    @Test
    fun aceita_somente_a_chave_definida() {
        assertTrue(ChavePro.aceita("GestorDrivePro"))
        assertTrue(ChavePro.aceita("  GestorDrivePro  "))
        assertFalse(ChavePro.aceita("gestordrivepro"))
        assertFalse(ChavePro.aceita(""))
    }

    @Test
    fun plano_fica_pro_com_chave_ou_assinatura() {
        assertEquals(PlanoAcesso.FREE, ChavePro.plano(liberada = false))
        assertEquals(PlanoAcesso.PRO, ChavePro.plano(liberada = true))
        assertEquals(PlanoAcesso.PRO, ChavePro.plano(liberada = false, assinaturaPlayAtiva = true))
    }
}
