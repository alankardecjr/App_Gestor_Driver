package br.com.gestordriver.core

data class ResultadoCombustivel(
    /** Litros (gasolina/etanol) ou kWh (energia), já com perda de recarga se elétrico. */
    val litros: Double,
    val custo: Double,
    val unidade: String = "L",
)

object CalculadoraCombustivel {
    /** Perda típica de recarga (~12%) usada em calculadoras de elétrico no BR. */
    const val FATOR_PERDA_RECARGA = 1.12

    fun calcular(
        kmTotal: Double,
        consumoKmL: Double,
        precoLitro: Double,
    ): ResultadoCombustivel? {
        if (kmTotal <= 0.0 || consumoKmL <= 0.0 || precoLitro <= 0.0) {
            return null
        }
        val litros = kmTotal / consumoKmL
        val custo = litros * precoLitro
        if (!litros.isFinite() || !custo.isFinite()) {
            return null
        }
        return ResultadoCombustivel(litros = litros, custo = custo, unidade = "L")
    }

    /**
     * Energia: consumo em km/kWh, preço em R$/kWh.
     * kWh = (km ÷ km/kWh) × 1,12 (perdas de recarga).
     */
    fun calcularEnergia(
        kmTotal: Double,
        consumoKmPorKwh: Double,
        precoKwh: Double,
    ): ResultadoCombustivel? {
        if (kmTotal <= 0.0 || consumoKmPorKwh <= 0.0 || precoKwh <= 0.0) {
            return null
        }
        val kwh = (kmTotal / consumoKmPorKwh) * FATOR_PERDA_RECARGA
        val custo = kwh * precoKwh
        if (!kwh.isFinite() || !custo.isFinite()) {
            return null
        }
        return ResultadoCombustivel(litros = kwh, custo = custo, unidade = "kWh")
    }

    fun calcularAtivo(
        kmTotal: Double,
        config: ConfiguracaoUsuario,
    ): ResultadoCombustivel? =
        when (config.combustivel) {
            Combustivel.ENERGIA -> calcularEnergia(
                kmTotal = kmTotal,
                consumoKmPorKwh = config.consumoAtivo(),
                precoKwh = config.precoAtivo(),
            )
            Combustivel.GASOLINA, Combustivel.ETANOL -> calcular(
                kmTotal = kmTotal,
                consumoKmL = config.consumoAtivo(),
                precoLitro = config.precoAtivo(),
            )
        }
}
