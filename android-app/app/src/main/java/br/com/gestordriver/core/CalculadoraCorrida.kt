package br.com.gestordriver.core

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

        val combustivel = configuracaoAtiva?.let {
            CalculadoraCombustivel.calcularAtivo(corrida.kmTotal, it)
        }
        val operacional = configuracaoAtiva?.let {
            CalculadoraCustos.gastoOperacional(corrida.kmTotal, it)
        }
        val gasto = listOfNotNull(combustivel?.custo, operacional).sum().takeIf {
            combustivel != null || operacional != null
        }

        return AnaliseCorrida(
            corrida = corrida,
            valorTotal = corrida.valorTotal,
            kmAtePassageiro = corrida.kmAtePassageiro,
            kmViagem = corrida.kmViagem,
            tempoEstimado = corrida.tempoEstimado,
            notaPassageiro = notaPassageiro,
            plataforma = plataforma,
            dataHora = CalendarioApp.agora(),
            kmTotal = corrida.kmTotal,
            valorPorKm = valorPorKm,
            combustivelEstimado = combustivel?.litros,
            custoCombustivel = gasto,
            classificacao = classificacao,
            corClassificacao = classificador.corDe(classificacao),
        )
    }
}
