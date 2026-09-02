package br.com.gestordriver.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfertaTextoFiltroTest {
    @Test
    fun ignora_missao_e_abastece() {
        assertTrue(
            OfertaTextoFiltro.ehPromocaoOuStatus(
                "Ganhe mais com missões\nVocê pode ganhar R$ 88",
            ),
        )
        assertTrue(
            OfertaTextoFiltro.ehPromocaoOuStatus(
                "Até R$60 OFF para encher o tanque com o 99Abastece",
            ),
        )
    }

    @Test
    fun reconhece_card_99() {
        assertTrue(
            OfertaTextoFiltro.pareceOferta(
                "R$ 15,40\n1,2 km\n6,8 km\n14 min\nAceitar\nRecusar",
            ),
        )
        assertFalse(
            OfertaTextoFiltro.ehPromocaoOuStatus(
                "R$ 15,40\n1,2 km\n6,8 km\n14 min\nAceitar\nRecusar",
            ),
        )
    }

    @Test
    fun reconhece_card_99_do_print() {
        val texto = """
            R${'$'}10,50
            R${'$'}2,70/km
            6min (971m)
            8min (2,9km)
        """.trimIndent()
        assertTrue(OfertaTextoFiltro.pareceOferta(texto))
        assertFalse(OfertaTextoFiltro.ehPromocaoOuStatus(texto))
    }

    @Test
    fun reconhece_solicitacao_uber() {
        assertTrue(
            OfertaTextoFiltro.pareceOferta(
                "1 solicitação encontrada...\nToque para ver as informações.",
            ),
        )
    }

    @Test
    fun ignora_overlay_do_gestor_como_oferta() {
        val texto = """
            DATA | HORA | R$/KM| VALOR| KM| TEMPO |NOTA
            31/08 | 10:20 | 1,68 | 8,90 | 5,3 KM | 18
            HISTÓRICO
            Até o passageiro
            Até o destino
            Total percorrido
            Custos estimados
            Lucro estimado
            Chegada prevista: 10:31
        """.trimIndent()
        assertTrue(OfertaTextoFiltro.ehInterfaceGestor(texto))
        assertFalse(OfertaTextoFiltro.temDadosParseaveis(texto))
        assertFalse(OfertaTextoFiltro.pareceOferta(texto))
        assertTrue(OfertaTextoFiltro.ehPromocaoOuStatus(texto))
    }

    @Test
    fun tabela_historico_ocr_nao_e_oferta() {
        val texto = "01/09 23:20 R$1,57 R$9,1\n5,8Km 20Min"
        assertTrue(OfertaTextoFiltro.pareceTabelaHistorico(texto))
        assertTrue(OfertaTextoFiltro.ehInterfaceGestor(texto))
        assertFalse(OfertaTextoFiltro.temDadosParseaveis(texto))
    }

    @Test
    fun tela_cancelou_nao_e_aceite() {
        val texto = "Você cancelou a corrida\nContinuar conectado\nDesconectar"
        assertTrue(OfertaTextoFiltro.ehTelaCancelamento(texto))
        assertFalse(
            RideEventClassifier.pareceAceite(
                NotificationData("com.app99.driver", "", texto),
            ),
        )
    }

    @Test
    fun mapa_online_sem_card_nao_e_oferta() {
        val texto = """
            Você está online
            +R${'$'} 2,75
            1-3 min
        """.trimIndent()
        assertTrue(OfertaTextoFiltro.ehMapaSemCard(texto))
        assertFalse(OfertaTextoFiltro.temDadosParseaveis(texto))
        assertFalse(OfertaTextoFiltro.pareceOferta(texto))
    }

    @Test
    fun card_uber_com_minutos_nao_e_mapa() {
        val texto = """
            R${'$'} 11,74
            5 min (1.2 km)
            5 minutos (1.3 km)
            Aceitar
        """.trimIndent()
        assertFalse(OfertaTextoFiltro.ehMapaSemCard(texto))
        assertTrue(OfertaTextoFiltro.temDadosParseaveis(texto))
        assertFalse(OfertaTextoFiltro.cardKmIncompleto(texto))
    }
}

class OfertaTelaTransicaoTest {
    @Test
    fun mapa_home_expira_na_hora() {
        assertEquals(
            TransicaoTelaOferta.EXPIRAR,
            OfertaTelaTransicao.decidir("Você está conectado", leiturasSemOferta = 1),
        )
        assertEquals(
            TransicaoTelaOferta.EXPIRAR,
            OfertaTelaTransicao.decidir("Você está online", leiturasSemOferta = 1),
        )
    }

    @Test
    fun primeira_leitura_vazia_aguarda() {
        assertEquals(
            TransicaoTelaOferta.AGUARDAR,
            OfertaTelaTransicao.decidir("", leiturasSemOferta = 1),
        )
    }

    @Test
    fun duas_leituras_vazias_expiram() {
        assertEquals(
            TransicaoTelaOferta.EXPIRAR,
            OfertaTelaTransicao.decidir("", leiturasSemOferta = 2),
        )
    }

    @Test
    fun quatro_leituras_sem_oferta_expiram() {
        assertEquals(
            TransicaoTelaOferta.EXPIRAR,
            OfertaTelaTransicao.decidir("", leiturasSemOferta = 4),
        )
    }

    @Test
    fun menu_desconectar_expira_na_hora() {
        assertEquals(
            TransicaoTelaOferta.EXPIRAR,
            OfertaTelaTransicao.decidir(
                "Política de cancelamento\nContinuar conectado\nDesconectar",
                leiturasSemOferta = 1,
            ),
        )
    }

    @Test
    fun cancelou_a_corrida_expira_na_hora() {
        assertEquals(
            TransicaoTelaOferta.EXPIRAR,
            OfertaTelaTransicao.decidir(
                "Você cancelou a corrida\nContinuar conectado",
                leiturasSemOferta = 1,
            ),
        )
    }

    @Test
    fun chegue_antes_e_aceite_na_hora() {
        assertEquals(
            TransicaoTelaOferta.ACEITE,
            OfertaTelaTransicao.decidir(
                "Chegue antes de 06:50\n9 min 2,3 km",
                leiturasSemOferta = 1,
            ),
        )
    }

    @Test
    fun ponto_de_encontro_e_aceite_na_hora() {
        assertEquals(
            TransicaoTelaOferta.ACEITE,
            OfertaTelaTransicao.decidir(
                "Ponto de encontro\nEstou no local",
                leiturasSemOferta = 1,
            ),
        )
    }
}
