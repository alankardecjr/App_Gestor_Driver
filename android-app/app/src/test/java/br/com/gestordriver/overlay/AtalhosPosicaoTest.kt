package br.com.gestordriver.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AtalhosPosicaoTest {

    private val menuW = 200
    private val menuH = 320
    private val selo = 60
    private val gap = 8
    private val minX = 8
    private val minY = 8
    private val maxX = 360
    private val maxY = 640

    private fun calc(seloX: Int, seloY: Int) = AtalhosPosicao.calcular(
        seloX = seloX,
        seloY = seloY,
        seloTam = selo,
        menuW = menuW,
        menuH = menuH,
        areaMinX = minX,
        areaMinY = minY,
        areaMaxX = maxX,
        areaMaxY = maxY,
        gap = gap,
    )

    @Test
    fun selo_a_esquerda_abre_a_direita() {
        val r = calc(seloX = 16, seloY = 280)
        assertEquals(AtalhosPosicao.Direcao.DIREITA, r.direcao)
        assertEquals(16 + selo + gap, r.x)
    }

    @Test
    fun selo_a_direita_abre_a_esquerda() {
        val r = calc(seloX = 300, seloY = 280)
        assertEquals(AtalhosPosicao.Direcao.ESQUERDA, r.direcao)
        assertEquals(300 - gap - menuW, r.x)
    }

    @Test
    fun selo_no_topo_abre_abaixo() {
        val r = calc(seloX = 150, seloY = 12)
        assertEquals(AtalhosPosicao.Direcao.ABAIXO, r.direcao)
        assertEquals(12 + selo + gap, r.y)
    }

    @Test
    fun selo_na_base_abre_acima() {
        val r = calc(seloX = 150, seloY = 560)
        assertEquals(AtalhosPosicao.Direcao.ACIMA, r.direcao)
        assertEquals(560 - gap - menuH, r.y)
    }

    @Test
    fun canto_superior_esquerdo_prioriza_direita_ou_abaixo() {
        val r = calc(seloX = 12, seloY = 12)
        assertTrue(
            r.direcao == AtalhosPosicao.Direcao.DIREITA ||
                r.direcao == AtalhosPosicao.Direcao.ABAIXO,
        )
        assertTrue(r.x >= minX)
        assertTrue(r.y >= minY)
        assertTrue(r.x + menuW <= maxX)
        assertTrue(r.y + menuH <= maxY)
    }

    @Test
    fun canto_inferior_direito_prioriza_esquerda_ou_acima() {
        val r = calc(seloX = 300, seloY = 560)
        assertTrue(
            r.direcao == AtalhosPosicao.Direcao.ESQUERDA ||
                r.direcao == AtalhosPosicao.Direcao.ACIMA,
        )
        assertTrue(r.x + menuW <= maxX)
        assertTrue(r.y + menuH <= maxY)
    }

    @Test
    fun menu_nao_sobrepoe_selo_em_direcao_horizontal() {
        val r = calc(seloX = 16, seloY = 280)
        assertEquals(AtalhosPosicao.Direcao.DIREITA, r.direcao)
        assertTrue(r.x >= 16 + selo + gap)
    }

    @Test
    fun menu_nao_sobrepoe_selo_em_direcao_vertical() {
        val r = calc(seloX = 150, seloY = 12)
        assertEquals(AtalhosPosicao.Direcao.ABAIXO, r.direcao)
        assertTrue(r.y >= 12 + selo + gap)
    }
}
