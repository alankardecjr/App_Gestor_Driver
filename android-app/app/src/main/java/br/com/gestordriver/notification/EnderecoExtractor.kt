package br.com.gestordriver.notification

data class EnderecosCorrida(
    val embarque: String? = null,
    val destino: String? = null,
)

object EnderecoExtractor {
    private val linhaOrigem = Regex(
        """(?im)^\s*(?:origem|embarque|pickup|coleta|coletar(?:\s+em)?|passageiro em)\s*[:\-–]?\s*(.+)$""",
    )
    private val linhaDestino = Regex(
        """(?im)^\s*(?:destino|desembarque|drop[- ]?off|deixar em|até o destino|ate o destino)\s*[:\-–]?\s*(.+)$""",
    )
    private val dePara = Regex(
        """(?i)(?:de|from)\s+(.+?)\s+(?:para|p/|to)\s+(.+)""",
    )

    fun extrair(texto: String): EnderecosCorrida {
        val origem = primeira(linhaOrigem, texto)
        val destino = primeira(linhaDestino, texto)
        if (origem != null || destino != null) {
            return EnderecosCorrida(
                embarque = limpar(origem),
                destino = limpar(destino),
            )
        }
        val caminho = dePara.find(texto)
        if (caminho != null) {
            return EnderecosCorrida(
                embarque = limpar(caminho.groupValues[1]),
                destino = limpar(caminho.groupValues[2]),
            )
        }
        return EnderecosCorrida()
    }

    private fun primeira(regex: Regex, texto: String): String? =
        regex.find(texto)?.groupValues?.getOrNull(1)

    private fun limpar(valor: String?): String? {
        val texto = valor?.trim()?.trimEnd('.', ',', ';')?.trim().orEmpty()
        if (texto.length < 4) {
            return null
        }
        return texto
    }
}
