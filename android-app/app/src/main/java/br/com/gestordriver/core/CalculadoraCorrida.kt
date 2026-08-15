package br.com.gestordriver.core

import java.time.LocalDateTime

class CalculadoraCorrida(
    private val classificador: MotorClassificacao = MotorClassificacao(),
    private val configuracaoUsuario: ConfiguracaoUsuario? = null,
) {
    fun calcular(
        corrida: Corrida,
        plataforma: String? = null,
        notaPassageiro: Double? = null,
        configuracao: ConfiguracaoUsuario? = null,
    ): AnaliseCorrida {
        val valorPorKm = corrida.valorPorKm
        val classificacao = classificador.classificarPorValorKm(valorPorKm)
        val configuracaoAtiva = configuracao ?: configuracaoUsuario

        val resultadoCombustivel = configuracaoAtiva?.let {
            CalculadoraCombustivel.calcular(
                kmTotal = corrida.kmTotal,
                consumoKmL = it.consumoAtivo(),
                precoLitro = it.precoAtivo(),
            )
        }

        return AnaliseCorrida(
            corrida = corrida,
            valorTotal = corrida.valorTotal,
            kmAtePassageiro = corrida.kmAtePassageiro,
            kmViagem = corrida.kmViagem,
            tempoEstimado = corrida.tempoEstimado,
            notaPassageiro = notaPassageiro,
            plataforma = plataforma,
            dataHora = LocalDateTime.now(),
            kmTotal = corrida.kmTotal,
            valorPorKm = valorPorKm,
            combustivelEstimado = resultadoCombustivel?.litros,
            custoCombustivel = resultadoCombustivel?.custo,
            classificacao = classificacao,
            corClassificacao = classificador.corDe(classificacao),
        )
    }
}
