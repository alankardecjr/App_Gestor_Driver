package br.com.gestordriver.notification

data class EnderecosCorrida(
    val embarque: String? = null,
    val destino: String? = null,
)

object EnderecoExtractor {
    private val linhaOrigem = Regex(
        """(?im)^\s*(?:origem|embarque|pickup|coleta|coletar\s+em|passageiro\s+em)\b\s*[:\-–]?\s*(.+)$""",
    )
    private val linhaDestino = Regex(
        """(?im)^\s*(?:destino|desembarque|drop[- ]?off|deixar\s+em|até o destino|ate o destino)\b\s*[:\-–]?\s*(.+)$""",
    )
    private val dePara = Regex(
        """(?i)(?:de|from)\s+(.+?)\s+(?:para|p/|to)\s+(.+)""",
    )

    /** Card Uber e 99: "7 min (1.8 km)" e o endereço na linha seguinte. */
    private val trechoTempoDistancia = Regex(
        """(?i)^\s*\d+[\s\u00A0]*min(?:uto)?s?[\s\u00A0]*\([\s\u00A0]*[\d.,]+[\s\u00A0]*(?:km|m)\)\s*$""",
    )
    private val fimDoCard = Regex(
        """(?i)^\s*(?:aceitar|recusar|selecionar)\b""",
    )
    private val inicioDeRua = Regex(
        """(?i)^(rua|r\.|avenida|av\.|av|travessa|trav\.|rodovia|rod\.|alameda|estrada|pra[cç]a|largo|beco)(?=\s|$)""",
    )

    fun extrair(texto: String): EnderecosCorrida {
        val origem = endereco(primeira(linhaOrigem, texto))
        val destino = endereco(primeira(linhaDestino, texto))
        if (origem != null || destino != null) {
            return EnderecosCorrida(embarque = origem, destino = destino)
        }
        val porTrecho = extrairAposTrecho(texto)
        if (porTrecho.embarque != null || porTrecho.destino != null) {
            return porTrecho
        }
        val caminho = dePara.find(texto)
        if (caminho != null) {
            return EnderecosCorrida(
                embarque = endereco(caminho.groupValues[1]),
                destino = endereco(caminho.groupValues[2]),
            )
        }
        return EnderecosCorrida()
    }

    private fun extrairAposTrecho(texto: String): EnderecosCorrida {
        val linhas = texto.lines()
        val achados = mutableListOf<Pair<Int, String>>()
        var indiceTrecho = 0
        for (i in linhas.indices) {
            if (fimDoCard.containsMatchIn(linhas[i])) {
                break
            }
            if (!trechoTempoDistancia.matches(linhas[i].trim())) {
                continue
            }
            for (j in i + 1 until linhas.size) {
                val linha = linhas[j].trim()
                if (linha.isEmpty()) {
                    continue
                }
                if (fimDoCard.containsMatchIn(linha) || trechoTempoDistancia.matches(linha)) {
                    break
                }
                val candidato = endereco(linha)
                if (candidato != null) {
                    achados.add(indiceTrecho to candidato)
                    break
                }
            }
            indiceTrecho++
        }
        if (achados.isEmpty()) {
            return EnderecosCorrida()
        }
        return EnderecosCorrida(
            embarque = achados.firstOrNull { it.first == 0 }?.second,
            destino = achados.lastOrNull { it.first > 0 }?.second,
        )
    }

    private fun endereco(valor: String?): String? {
        val texto = limpar(valor) ?: return null
        if (!pareceEndereco(texto)) {
            return null
        }
        return texto
    }

    private fun pareceEndereco(texto: String): Boolean {
        if (!texto.any { it.isLetter() }) {
            return false
        }
        if (texto.contains(',')) {
            return true
        }
        if (texto.contains(" - ")) {
            return true
        }
        return inicioDeRua.containsMatchIn(texto)
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
