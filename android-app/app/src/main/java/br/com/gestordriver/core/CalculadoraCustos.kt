package br.com.gestordriver.core

object CalculadoraCustos {
    /**
     * Custo por km da corrida (óleo + pneus).
     * Seguro fica no Dashboard (rateio por recorrência mensal/anual).
     */
    fun custoPorKm(config: ConfiguracaoUsuario): Double {
        var porKm = 0.0
        porKm += parcelaPorKm(config.oleoValor, config.oleoKilometragem)
        porKm += parcelaPorKm(config.pneuDianteiroValor, config.pneuDianteiroRodagem)
        porKm += parcelaPorKm(config.pneuTraseiroValor, config.pneuTraseiroRodagem)
        return porKm
    }

    fun gastoOperacional(kmTotal: Double, config: ConfiguracaoUsuario): Double? {
        if (kmTotal <= 0.0) {
            return null
        }
        val valor = custoPorKm(config) * kmTotal
        if (valor <= 0.0 || !valor.isFinite()) {
            return null
        }
        return valor
    }

    private fun parcelaPorKm(valor: Double, baseKm: Double): Double {
        if (valor <= 0.0 || baseKm <= 0.0) {
            return 0.0
        }
        val parcela = valor / baseKm
        return if (parcela.isFinite()) parcela else 0.0
    }
}
