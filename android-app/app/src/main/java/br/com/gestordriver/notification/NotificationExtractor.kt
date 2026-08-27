package br.com.gestordriver.notification

data class CamposExtraidos(
    val valorTotal: Double,
    val kmAtePassageiro: Double,
    val kmViagem: Double,
    val tempoEstimado: Int?,
)

object NotificationExtractor {
    private fun normalizarNumero(texto: String): Double {
        var valor = texto.trim().replace(" ", "")
        if (valor.isEmpty()) {
            throw ExtractionError("Valor numerico vazio na notificacao.")
        }

        valor = when {
            "," in valor && "." in valor -> valor.replace(".", "").replace(",", ".")
            "," in valor -> valor.replace(",", ".")
            else -> valor
        }

        return valor.toDoubleOrNull()
            ?: throw ExtractionError("Nao foi possivel converter numero: '$texto'.")
    }

    fun extrairValor(texto: String): Double {
        val match = NotificationPatterns.VALOR.find(texto)
            ?: throw ExtractionError("Valor da corrida nao encontrado.")
        return normalizarNumero(match.groupValues[1])
    }

    fun extrairDistancias(texto: String): List<Double> {
        val distanciasKm = NotificationPatterns.DISTANCIA_COM_UNIDADE
            .findAll(texto)
            .map { match ->
                val valor = normalizarNumero(match.groupValues[1])
                val unidade = match.groupValues[2].lowercase()
                if (unidade == "m") valor / 1000 else valor
            }
            .toList()

        if (distanciasKm.isEmpty()) {
            throw ExtractionError("Nenhuma distancia encontrada.")
        }

        return distanciasKm
    }

    fun extrairTempo(texto: String): Int? {
        val match = NotificationPatterns.TEMPO.find(texto) ?: return null
        return match.groupValues[1].toIntOrNull()
    }

    fun extrairCamposPadrao(texto: String): CamposExtraidos {
        val valorTotal = extrairValor(texto)
        val distancias = runCatching { extrairDistancias(texto) }.getOrDefault(emptyList())
        val tempoEstimado = extrairTempo(texto)

        val (kmAtePassageiro, kmViagem) = when {
            distancias.size >= 2 -> distancias[0] to distancias[1]
            distancias.size == 1 -> 0.0 to distancias[0]
            else -> 0.0 to 0.0
        }

        return CamposExtraidos(
            valorTotal = valorTotal,
            kmAtePassageiro = kmAtePassageiro,
            kmViagem = kmViagem,
            tempoEstimado = tempoEstimado,
        )
    }
}
