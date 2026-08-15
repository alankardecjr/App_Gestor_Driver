package br.com.gestordriver.core

data class ResultadoCombustivel(
    val litros: Double,
    val custo: Double,
)

object CalculadoraCombustivel {
    fun calcular(
        kmTotal: Double,
        consumoKmL: Double,
        precoLitro: Double,
    ): ResultadoCombustivel {
        val litros = kmTotal / consumoKmL
        val custo = litros * precoLitro
        return ResultadoCombustivel(litros = litros, custo = custo)
    }
}
