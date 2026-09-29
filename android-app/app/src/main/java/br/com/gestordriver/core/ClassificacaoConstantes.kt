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
        Classificacao.EXCELENTE to "#00C853",
        Classificacao.BOA to "#FFD600",
        Classificacao.REGULAR to "#FFD600",
        Classificacao.BAIXA to "#FFD600",
        Classificacao.RUIM to "#FF1744",
    )
}
