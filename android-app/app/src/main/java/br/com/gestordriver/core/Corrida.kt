package br.com.gestordriver.core

data class Corrida(
    val valorTotal: Double,
    val kmAtePassageiro: Double,
    val kmViagem: Double,
    val tempoEstimado: Int? = null,
    val enderecoEmbarque: String? = null,
    val enderecoDestino: String? = null,
    val quantidadeParadas: Int = 0,
) {
    val kmTotal: Double
        get() = kmAtePassageiro + kmViagem

    val valorPorKm: Double
        get() = if (kmTotal <= 0) 0.0 else valorTotal / kmTotal

    /**
     * Ganho por hora usando o tempo total da oferta (deslocamento até o
     * passageiro + viagem). `tempoEstimado` já é a soma das pernas do card.
     * Retorna null quando o tempo não está disponível.
     */
    val valorPorHora: Double?
        get() {
            val minutos = tempoEstimado ?: return null
            if (minutos <= 0) return null
            return valorTotal / (minutos / 60.0)
        }
}
