package br.com.gestordriver.core

object ClassificacaoConstantes {
    val LIMITES_R_POR_KM: Map<Classificacao, Double> = mapOf(
        Classificacao.EXCELENTE to 2.50,
        Classificacao.BOA to 2.00,
        Classificacao.REGULAR to 1.50,
        Classificacao.BAIXA to 1.20,
    )

    val CORES: Map<Classificacao, String> = mapOf(
        Classificacao.EXCELENTE to "#2E7D32",
        Classificacao.BOA to "#7CB342",
        Classificacao.REGULAR to "#F9A825",
        Classificacao.BAIXA to "#EF6C00",
        Classificacao.RUIM to "#C62828",
    )
}
