package br.com.gestordriver.core

/**
 * Cores da compacta: R$/km segue a faixa; R$/hora segue a meta do motorista.
 * A borda usa a pior das duas.
 */
object SemaforoOferta {
    /**
     * Três faixas a partir da meta: ótima >= meta, boa >= 80% da meta, ruim abaixo.
     * Meta 0 ou hora ausente não pinta.
     */
    /**
     * Até a primeira marca, vermelho. Da primeira + R$ 0,01 até a segunda, amarelo.
     * Da segunda + R$ 0,01 em diante, verde. Marca de cima em 0 não pinta.
     */
    fun corPorDuasMarcas(valor: Double?, abaixo: Double, acima: Double): String {
        if (acima <= 0.0 || valor == null) {
            return ClassificacaoConstantes.COR_BORDA_NEUTRA
        }
        val v = centavos(valor)
        val piso = centavos(abaixo.coerceAtMost(acima))
        val teto = centavos(acima)
        return when {
            v >= teto + 1 -> ClassificacaoConstantes.CORES.getValue(Classificacao.EXCELENTE)
            v >= piso + 1 -> ClassificacaoConstantes.CORES.getValue(Classificacao.BOA)
            else -> ClassificacaoConstantes.CORES.getValue(Classificacao.RUIM)
        }
    }

    private fun centavos(valor: Double): Int = kotlin.math.round(valor * 100.0).toInt()

    fun corPorFaixaHora(valorPorHora: Double?, meta: Double): String {
        if (meta <= 0.0 || valorPorHora == null) {
            return ClassificacaoConstantes.COR_BORDA_NEUTRA
        }
        return when {
            valorPorHora >= meta -> ClassificacaoConstantes.CORES.getValue(Classificacao.EXCELENTE)
            valorPorHora >= meta * 0.8 -> ClassificacaoConstantes.CORES.getValue(Classificacao.BOA)
            else -> ClassificacaoConstantes.CORES.getValue(Classificacao.RUIM)
        }
    }

    fun corPorMeta(atingeMeta: Boolean?): String = when (atingeMeta) {
        true -> ClassificacaoConstantes.CORES.getValue(Classificacao.EXCELENTE)
        false -> ClassificacaoConstantes.CORES.getValue(Classificacao.RUIM)
        null -> ClassificacaoConstantes.COR_BORDA_NEUTRA
    }

    fun pior(corKm: String, corHora: String): String {
        return if (gravidade(corHora) < gravidade(corKm)) corHora else corKm
    }

    private fun gravidade(cor: String): Int = when (cor.uppercase()) {
        "#C62828", "#EF6C00", "#FF1744" -> 0
        "#F9A825", "#FFB300", "#FFD600" -> 1
        "#2E7D32", "#8BC34A", "#00C853" -> 2
        else -> 3
    }
}
