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
            EXCELENTE -> "🔵"
            BOA -> "🟢"
            REGULAR, BAIXA -> "🟠"
            RUIM -> "🔴"
        }

    companion object {
        fun from(classificacao: Classificacao): ClassificacaoVisual =
            valueOf(classificacao.name)
    }
}
