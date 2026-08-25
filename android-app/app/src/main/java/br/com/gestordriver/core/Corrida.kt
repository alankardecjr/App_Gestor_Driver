package br.com.gestordriver.core

data class Corrida(
    val valorTotal: Double,
    val kmAtePassageiro: Double,
    val kmViagem: Double,
    val tempoEstimado: Int? = null,
    val enderecoEmbarque: String? = null,
    val enderecoDestino: String? = null,
) {
    val kmTotal: Double
        get() = kmAtePassageiro + kmViagem

    val valorPorKm: Double
        get() = if (kmTotal <= 0) 0.0 else valorTotal / kmTotal
}
