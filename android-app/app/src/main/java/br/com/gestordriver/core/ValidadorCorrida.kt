package br.com.gestordriver.core

object ValidadorCorrida {
    fun validarValor(valor: Double) {
        require(valor > 0) { "Valor da corrida inválido." }
    }

    fun validarKm(km: Double) {
        require(km >= 0) { "Quilometragem inválida." }
    }

    fun validar(corrida: Corrida) {
        validarValor(corrida.valorTotal)
        validarKm(corrida.kmAtePassageiro)
        validarKm(corrida.kmViagem)
    }
}
