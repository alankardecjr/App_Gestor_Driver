package br.com.gestordriver.notification

/**
 * Quando o card da oferta some na 99, a tela seguinte pode ser
 * aceite (ponto de encontro) ou recusa/expiração (mapa online).
 * A primeira leitura sem oferta costuma ser OCR vazio no meio da animação.
 */
enum class TransicaoTelaOferta {
    ACEITE,
    EXPIRAR,
    AGUARDAR,
}

object OfertaTelaTransicao {
    const val LEITURAS_PARA_EXPIRAR = 4
    const val LEITURAS_MAPA_PARA_EXPIRAR = 1

    fun decidir(
        textoAtual: String,
        leiturasSemOferta: Int,
        pacote: String = "",
    ): TransicaoTelaOferta {
        val notification = NotificationData(
            packageName = pacote,
            title = "",
            text = textoAtual,
            key = "tela:$pacote",
        )
        if (RideEventClassifier.pareceAceite(notification)) {
            return TransicaoTelaOferta.ACEITE
        }
        val normal = OfertaTextoFiltro.normalizar(textoAtual)
        if (normal.contains("continuar conectado") ||
            normal.contains("política de cancelamento") ||
            normal.contains("politica de cancelamento")
        ) {
            return TransicaoTelaOferta.AGUARDAR
        }
        if (OfertaTextoFiltro.ehMapaSemCard(textoAtual) &&
            leiturasSemOferta >= LEITURAS_MAPA_PARA_EXPIRAR
        ) {
            return TransicaoTelaOferta.EXPIRAR
        }
        if (leiturasSemOferta >= LEITURAS_PARA_EXPIRAR) {
            return TransicaoTelaOferta.EXPIRAR
        }
        return TransicaoTelaOferta.AGUARDAR
    }
}
