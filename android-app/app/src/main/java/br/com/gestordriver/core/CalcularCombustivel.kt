package br.com.gestordriver.core

import kotlin.math.round

/**
 * Calculadora da aba VEÍCULO (Pro). Usa o combustível atual:
 * R$/L = valor pago ÷ litros; km/L = (km final − km inicial) ÷ litros.
 * O resultado preenche só os campos daquele combustível (consumo e preço).
 */
object CalcularCombustivel {
    fun precoPorLitro(valorPago: Double, litros: Double): Double? {
        if (valorPago < 0.0 || litros <= 0.0) {
            return null
        }
        return arredondar(valorPago / litros)
    }

    fun consumoKmPorLitro(kmInicial: Double, kmFinal: Double, litros: Double): Double? {
        val percorrido = kmFinal - kmInicial
        if (litros <= 0.0 || percorrido <= 0.0) {
            return null
        }
        return arredondar(percorrido / litros)
    }

    private fun arredondar(valor: Double): Double = round(valor * 100.0) / 100.0
}
