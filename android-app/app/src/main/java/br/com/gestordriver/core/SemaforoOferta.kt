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
    /** Abaixo da primeira marca vermelho, entre as duas amarelo, da segunda para cima verde. Marca boa em 0 não pinta. */
    fun corPorDuasMarcas(valor: Double?, abaixo: Double, acima: Double): String {
        if (acima <= 0.0 || valor == null) {
            return ClassificacaoConstantes.COR_BORDA_NEUTRA
        }
        val piso = abaixo.coerceAtMost(acima)
        return when {
            valor >= acima -> ClassificacaoConstantes.CORES.getValue(Classificacao.EXCELENTE)
            valor > piso -> ClassificacaoConstantes.CORES.getValue(Classificacao.BOA)
            else -> ClassificacaoConstantes.CORES.getValue(Classificacao.RUIM)
        }
    }

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
        "#C62828", "#EF6C00" -> 0
        "#F9A825" -> 1
        "#2E7D32" -> 2
        else -> 3
    }
}
