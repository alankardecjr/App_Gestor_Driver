package br.com.gestordriver.overlay

/**
 * Posiciona o card de Atalhos em relação ao selo (§44).
 * Direção: direita | esquerda | abaixo | acima — conforme a posição do selo na tela.
 */
object AtalhosPosicao {

    enum class Direcao { DIREITA, ESQUERDA, ABAIXO, ACIMA }

    data class Resultado(
        val x: Int,
        val y: Int,
        val direcao: Direcao,
    )

    /**
     * 1) Prefere o eixo da **borda mais próxima** do selo (perto da esquerda → abre na horizontal).
     * 2) Nesse eixo, escolhe o lado com **mais espaço livre** que caiba o card.
     * 3) Se nenhum lado do eixo couber, tenta o outro eixo; por fim força dentro da área.
     */
    fun calcular(
        seloX: Int,
        seloY: Int,
        seloTam: Int,
        menuW: Int,
        menuH: Int,
        areaMinX: Int,
        areaMinY: Int,
        areaMaxX: Int,
        areaMaxY: Int,
        gap: Int,
    ): Resultado {
        val seloDir = seloX + seloTam
        val seloBai = seloY + seloTam
        val livreDir = (areaMaxX - (seloDir + gap)).coerceAtLeast(0)
        val livreEsq = ((seloX - gap) - areaMinX).coerceAtLeast(0)
        val livreBai = (areaMaxY - (seloBai + gap)).coerceAtLeast(0)
        val livreAci = ((seloY - gap) - areaMinY).coerceAtLeast(0)

        val distEsq = (seloX - areaMinX).coerceAtLeast(0)
        val distDir = (areaMaxX - seloDir).coerceAtLeast(0)
        val distAci = (seloY - areaMinY).coerceAtLeast(0)
        val distBai = (areaMaxY - seloBai).coerceAtLeast(0)
        val pertoHorizontal = minOf(distEsq, distDir) <= minOf(distAci, distBai)

        data class Candidato(
            val direcao: Direcao,
            val x: Int,
            val y: Int,
            val livre: Int,
            val precisa: Int,
            val horizontal: Boolean,
        ) {
            val cabe: Boolean get() = livre >= precisa
        }

        val candidatos = listOf(
            Candidato(
                Direcao.DIREITA,
                x = seloDir + gap,
                y = alinharEixo(seloY, menuH, areaMinY, areaMaxY),
                livre = livreDir,
                precisa = menuW,
                horizontal = true,
            ),
            Candidato(
                Direcao.ESQUERDA,
                x = seloX - gap - menuW,
                y = alinharEixo(seloY, menuH, areaMinY, areaMaxY),
                livre = livreEsq,
                precisa = menuW,
                horizontal = true,
            ),
            Candidato(
                Direcao.ABAIXO,
                x = alinharEixo(seloX, menuW, areaMinX, areaMaxX),
                y = seloBai + gap,
                livre = livreBai,
                precisa = menuH,
                horizontal = false,
            ),
            Candidato(
                Direcao.ACIMA,
                x = alinharEixo(seloX, menuW, areaMinX, areaMaxX),
                y = seloY - gap - menuH,
                livre = livreAci,
                precisa = menuH,
                horizontal = false,
            ),
        )

        val queCabem = candidatos.filter { it.cabe }
        val noEixoPreferido = queCabem.filter { it.horizontal == pertoHorizontal }
        val escolhido = when {
            noEixoPreferido.isNotEmpty() -> noEixoPreferido.maxBy { it.livre }
            queCabem.isNotEmpty() -> queCabem.maxBy { it.livre }
            else -> candidatos.maxBy { it.livre }
        }

        val x = escolhido.x.coerceIn(areaMinX, (areaMaxX - menuW).coerceAtLeast(areaMinX))
        val y = escolhido.y.coerceIn(areaMinY, (areaMaxY - menuH).coerceAtLeast(areaMinY))
        return Resultado(x = x, y = y, direcao = escolhido.direcao)
    }

    private fun alinharEixo(ancora: Int, tamanho: Int, min: Int, max: Int): Int {
        val maxInicio = (max - tamanho).coerceAtLeast(min)
        return ancora.coerceIn(min, maxInicio)
    }
}
