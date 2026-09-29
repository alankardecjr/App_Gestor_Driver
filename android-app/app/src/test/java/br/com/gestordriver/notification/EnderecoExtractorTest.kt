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

    @Test
    fun card_uber_endereco_na_linha_abaixo_do_tempo() {
        val texto = """
            UberX
            Exclusivo
            R$ 19,10
            R$1,50/km aprox.
            4,88 (195)
            7 min (1.8${'\u00A0'}km)
            Rua Floriano Peixoto, Lauro de Freitas, Lauro de Freitas
            20 minutos (10.9${'\u00A0'}km)
            R. Da Prosa, Salvador
            Aceitar
            Página inicial
            Tendências de ganhos
        """.trimIndent()

        val enderecos = EnderecoExtractor.extrair(texto)

        assertEquals("Rua Floriano Peixoto, Lauro de Freitas, Lauro de Freitas", enderecos.embarque)
        assertEquals("R. Da Prosa, Salvador", enderecos.destino)
    }

    @Test
    fun card_uber_com_parada_e_viagem_longa() {
        val comParada = """
            14 min (5.5 km)
            R. Manoel Silvestre Leite, Centro, Lauro de Freitas
            1 parada
            16 minutos (2.7 km)
            R. Manoel Silvestre Leite, 190, Centro, Lauro de Freitas
            Aceitar
        """.trimIndent()
        val parada = EnderecoExtractor.extrair(comParada)
        assertEquals("R. Manoel Silvestre Leite, Centro, Lauro de Freitas", parada.embarque)
        assertEquals("R. Manoel Silvestre Leite, 190, Centro, Lauro de Freitas", parada.destino)

        val longa = """
            7 min (2.5 km)
            Rua Dejanira Maria Bastos, Lauro de Freitas
            42 minutos (24.9 km)
            Rua Boa Vista, 170, Ilha Amarela, Salvador
            Viagem longa (mais de 30 min)
            Aceitar
        """.trimIndent()
        val enderecos = EnderecoExtractor.extrair(longa)
        assertEquals("Rua Dejanira Maria Bastos, Lauro de Freitas", enderecos.embarque)
        assertEquals("Rua Boa Vista, 170, Ilha Amarela, Salvador", enderecos.destino)
    }

    @Test
    fun card_99_endereco_na_linha_abaixo_do_tempo() {
        val texto = """
            R$10,50
            6min (971m)
            Arautos do Evangelho, Rua 15 de Janeiro, 249 - Recreio Ipitanga
            8min (2,9km)
            Rua Horto Florestal, 23, Lot. Jardim Metropole
        """.trimIndent()

        val enderecos = EnderecoExtractor.extrair(texto)

        assertEquals(
            "Arautos do Evangelho, Rua 15 de Janeiro, 249 - Recreio Ipitanga",
            enderecos.embarque,
        )
        assertEquals("Rua Horto Florestal, 23, Lot. Jardim Metropole", enderecos.destino)
    }

    @Test
    fun tela_de_ganhos_nao_vira_endereco() {
        val enderecos = EnderecoExtractor.extrair("de ganhos para viagens")

        assertNull(enderecos.embarque)
        assertNull(enderecos.destino)
    }

    @Test
    fun coletar_pagamento_nao_vira_endereco() {
        val enderecos = EnderecoExtractor.extrair("Coletar pagamento")

        assertNull(enderecos.embarque)
        assertNull(enderecos.destino)
    }
}
