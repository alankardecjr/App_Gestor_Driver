package br.com.gestordriver.model

import br.com.gestordriver.core.Classificacao

enum class ClassificacaoVisual {
    EXCELENTE,
    BOA,
    REGULAR,
    BAIXA,
    RUIM,
    ;

    val marcador: String
        get() = when (this) {
            EXCELENTE -> "🟢"
            BOA -> "🟡"
            REGULAR, BAIXA, RUIM -> "🔴"
        }

    val rotulo: String
        get() = when (this) {
            EXCELENTE -> "Ótima"
            BOA -> "Boa"
            REGULAR, BAIXA, RUIM -> "Ruim"
        }

    companion object {
        fun from(classificacao: Classificacao): ClassificacaoVisual =
            valueOf(classificacao.name)
    }
}
