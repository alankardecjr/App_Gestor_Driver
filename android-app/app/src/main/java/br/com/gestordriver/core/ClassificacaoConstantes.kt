package br.com.gestordriver.core

object ClassificacaoConstantes {
    const val COR_BORDA_NEUTRA = "#607D8B"

    val LIMITES_R_POR_KM: Map<Classificacao, Double> = mapOf(
        Classificacao.EXCELENTE to 2.00,
        Classificacao.BOA to 1.60,
        Classificacao.REGULAR to 1.20,
        Classificacao.BAIXA to 1.20,
    )

    val CORES: Map<Classificacao, String> = mapOf(
        Classificacao.EXCELENTE to "#2E7D32",
        Classificacao.BOA to "#F9A825",
        Classificacao.REGULAR to "#EF6C00",
        Classificacao.BAIXA to "#EF6C00",
        Classificacao.RUIM to "#C62828",
    )
}
