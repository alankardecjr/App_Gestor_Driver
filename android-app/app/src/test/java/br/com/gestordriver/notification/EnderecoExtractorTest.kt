package br.com.gestordriver.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EnderecoExtractorTest {
    @Test
    fun deve_extrair_origem_e_destino_por_linha() {
        val texto = """
            Origem: Av. Paulista, 1000
            Destino: Rua Augusta, 200
        """.trimIndent()

        val enderecos = EnderecoExtractor.extrair(texto)

        assertEquals("Av. Paulista, 1000", enderecos.embarque)
        assertEquals("Rua Augusta, 200", enderecos.destino)
    }

    @Test
    fun deve_extrair_de_para() {
        val enderecos = EnderecoExtractor.extrair("De Rua A, 10 para Rua B, 20")

        assertEquals("Rua A, 10", enderecos.embarque)
        assertEquals("Rua B, 20", enderecos.destino)
    }

    @Test
    fun sem_endereco_retorna_nulo() {
        val enderecos = EnderecoExtractor.extrair("R$ 38,00 • 3,2 km • 12,8 km • 24 min")

        assertNull(enderecos.embarque)
        assertNull(enderecos.destino)
    }
}
