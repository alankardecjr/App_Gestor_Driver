package br.com.gestordriver.core

class MotorClassificacao(
    private val limites: Map<Classificacao, Double> = ClassificacaoConstantes.LIMITES_R_POR_KM,
    private val cores: Map<Classificacao, String> = ClassificacaoConstantes.CORES,
) {
    fun classificarPorValorKm(valorPorKm: Double): Classificacao {
        if (valorPorKm >= limites.getValue(Classificacao.EXCELENTE)) {
            return Classificacao.EXCELENTE
        }
        if (valorPorKm >= limites.getValue(Classificacao.BOA)) {
            return Classificacao.BOA
        }
        if (valorPorKm >= limites.getValue(Classificacao.REGULAR)) {
            return Classificacao.REGULAR
        }
        if (valorPorKm >= limites.getValue(Classificacao.BAIXA)) {
            return Classificacao.BAIXA
        }
        return Classificacao.RUIM
    }

    fun corDe(classificacao: Classificacao): String = cores.getValue(classificacao)

    companion object {
        fun daConfiguracao(
            configuracao: br.com.gestordriver.model.ConfiguracaoUsuario,
        ): MotorClassificacao {
            if (!configuracao.faixasDefinidas()) {
                return MotorClassificacao()
            }
            return MotorClassificacao(
                limites = mapOf(
                    Classificacao.EXCELENTE to configuracao.limiteOtimaMin,
                    Classificacao.BOA to configuracao.limiteBoaMin,
                    Classificacao.REGULAR to configuracao.limiteRegularMin,
                    Classificacao.BAIXA to configuracao.limiteRuimMax,
                ),
            )
        }
    }
}
