package br.com.gestordriver.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NavegacaoLauncherTest {
    @Test
    fun oferta_aberta_prioriza_embarque() {
        val alvo = NavegacaoLauncher.destinoNavegacao(
            embarque = "Av. Paulista, 1000",
            destino = "Rua Augusta, 200",
            corridaAceita = false,
        )
        assertEquals("Av. Paulista, 1000", alvo)
    }

    @Test
    fun corrida_aceita_prioriza_destino() {
        val alvo = NavegacaoLauncher.destinoNavegacao(
            embarque = "Av. Paulista, 1000",
            destino = "Rua Augusta, 200",
            corridaAceita = true,
        )
        assertEquals("Rua Augusta, 200", alvo)
    }

    @Test
    fun sem_endereco_nao_abre_rota() {
        assertNull(
            NavegacaoLauncher.destinoNavegacao(
                embarque = null,
                destino = null,
                corridaAceita = false,
            ),
        )
    }
}
