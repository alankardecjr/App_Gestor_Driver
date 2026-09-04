package br.com.gestordriver.notification

data class CamposExtraidos(
    val valorTotal: Double,
    val kmAtePassageiro: Double,
    val kmViagem: Double,
    val tempoEstimado: Int?,
    val quantidadeParadas: Int = 0,
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
        val match = NotificationPatterns.VALOR.findAll(texto).firstOrNull { candidato ->
            if (prefixoESinalDeMais(texto, candidato.range.first)) {
                return@firstOrNull false
            }
            val apos = sufixoImediato(texto, candidato.range.last + 1)
            !ehTaxaPorKm(apos) && !ehTarifaBase(apos)
        } ?: throw ExtractionError("Valor da corrida nao encontrado.")
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

    fun extrairQuantidadeParadas(texto: String): Int {
        val mais = Regex(
            """\+\s*(\d+)\s*(?:parada|paradas|stop|stops)\b""",
            RegexOption.IGNORE_CASE,
        ).find(texto)
        if (mais != null) {
            return mais.groupValues[1].toIntOrNull()?.coerceAtLeast(0) ?: 0
        }
        val rotulo = Regex(
            """(\d+)\s*(?:parada|paradas|stop|stops)\b""",
            RegexOption.IGNORE_CASE,
        ).find(texto)
        return rotulo?.groupValues?.get(1)?.toIntOrNull()?.coerceAtLeast(0) ?: 0
    }

    fun extrairCamposPadrao(texto: String): CamposExtraidos {
        val valorTotal = extrairValor(texto)
        val paradas = extrairQuantidadeParadas(texto)
        val trechos99 = extrairTrechos99(texto)
        if (trechos99 != null) {
            return CamposExtraidos(
                valorTotal = valorTotal,
                kmAtePassageiro = trechos99.first,
                kmViagem = trechos99.second,
                tempoEstimado = trechos99.third,
                quantidadeParadas = paradas,
            )
        }

        val trechosUber = extrairParesTempoDistancia(texto)
        val kmAteRotulo = distanciaDe(NotificationPatterns.KM_ATE_PASSAGEIRO.find(texto))
        val kmViagemRotulo = distanciaDe(NotificationPatterns.KM_VIAGEM.find(texto))

        if (kmAteRotulo != null || kmViagemRotulo != null) {
            return CamposExtraidos(
                valorTotal = valorTotal,
                kmAtePassageiro = kmAteRotulo
                    ?: trechosUber?.first
                    ?: 0.0,
                kmViagem = kmViagemRotulo
                    ?: trechosUber?.second?.takeIf { it > 0.0 }
                    ?: segundaDistancia(texto, kmAteRotulo ?: trechosUber?.first ?: 0.0),
                tempoEstimado = trechosUber?.third ?: extrairTempo(texto),
                quantidadeParadas = paradas,
            )
        }

        if (trechosUber != null) {
            val kmViagem = if (trechosUber.second > 0.0) {
                trechosUber.second
            } else {
                segundaDistancia(texto, trechosUber.first)
            }
            return CamposExtraidos(
                valorTotal = valorTotal,
                kmAtePassageiro = trechosUber.first,
                kmViagem = kmViagem,
                tempoEstimado = trechosUber.third,
                quantidadeParadas = paradas,
            )
        }

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
            quantidadeParadas = paradas,
        )
    }

    fun extrairNotaPassageiro(texto: String): Double? {
        NotificationPatterns.NOTA_COM_CORRIDAS.find(texto)?.let { match ->
            return runCatching { normalizarNumero(match.groupValues[1]) }.getOrNull()
        }
        NotificationPatterns.NOTA_CORRIDAS_MESMA_LINHA.find(texto)?.let { match ->
            return runCatching { normalizarNumero(match.groupValues[1]) }.getOrNull()
        }
        NotificationPatterns.NOTA_COLADA_CORRIDAS.find(texto)?.let { match ->
            return runCatching { normalizarNumero(match.groupValues[1]) }.getOrNull()
        }
        NotificationPatterns.NOTA_COM_VIAGENS.find(texto)?.let { match ->
            return runCatching { normalizarNumero(match.groupValues[1]) }.getOrNull()
        }
        NotificationPatterns.NOTA_VIAGENS_MESMA_LINHA.find(texto)?.let { match ->
            return runCatching { normalizarNumero(match.groupValues[1]) }.getOrNull()
        }
        NotificationPatterns.NOTA_COM_PARENTESES.find(texto)?.let { match ->
            return runCatching { normalizarNumero(match.groupValues[1]) }.getOrNull()
        }
        NotificationPatterns.NOTA_COM_ESTRELA.find(texto)?.let { match ->
            val bruto = match.groupValues[1].ifBlank { match.groupValues[2] }
            return runCatching { normalizarNumero(bruto) }.getOrNull()
        }
        NotificationPatterns.NOTA_LINHA_ISOLADA.find(texto)?.let { match ->
            return runCatching { normalizarNumero(match.groupValues[1]) }.getOrNull()
        }
        return null
    }

    private fun extrairParesTempoDistancia(texto: String): Triple<Double, Double, Int>? {
        val matches = NotificationPatterns.PAR_TEMPO_DISTANCIA.findAll(texto).toList()
        if (matches.isEmpty()) {
            return null
        }
        val kms = matches.map { distanciaDe(it, valorGrupo = 2, unidadeGrupo = 3) ?: 0.0 }
        val minutos = matches.sumOf { it.groupValues[1].toIntOrNull() ?: 0 }
        val kmAte = kms.getOrElse(0) { 0.0 }
        val kmViagem = if (kms.size >= 2) kms[1] else 0.0
        return Triple(kmAte, kmViagem, minutos)
    }

    private fun segundaDistancia(texto: String, kmAte: Double): Double {
        val distancias = runCatching { extrairDistancias(texto) }.getOrDefault(emptyList())
        return distancias.firstOrNull { candidato ->
            kotlin.math.abs(candidato - kmAte) > 0.05
        } ?: 0.0
    }

    private fun distanciaDe(
        match: MatchResult?,
        valorGrupo: Int = 1,
        unidadeGrupo: Int = 2,
    ): Double? {
        if (match == null) {
            return null
        }
        val valor = runCatching { normalizarNumero(match.groupValues[valorGrupo]) }.getOrNull()
            ?: return null
        val unidade = match.groupValues[unidadeGrupo].lowercase()
        return if (unidade == "m") valor / 1000 else valor
    }

    private fun extrairTrechos99(texto: String): Triple<Double, Double, Int>? {
        val matches = NotificationPatterns.TRECHO_TEMPO_DISTANCIA.findAll(texto).toList()
        if (matches.isEmpty()) {
            return null
        }
        val kms = matches.map { match ->
            val valor = normalizarNumero(match.groupValues[2])
            val unidade = match.groupValues[3].lowercase()
            if (unidade == "m") valor / 1000 else valor
        }
        val minutos = matches.sumOf { it.groupValues[1].toIntOrNull() ?: 0 }
        val kmAte = kms.getOrElse(0) { 0.0 }
        val kmViagem = if (kms.size >= 2) kms[1] else 0.0
        return Triple(kmAte, kmViagem, minutos)
    }

    private fun prefixoESinalDeMais(texto: String, inicio: Int): Boolean {
        var i = inicio - 1
        while (i >= 0 && texto[i].isWhitespace()) {
            i--
        }
        return i >= 0 && texto[i] == '+'
    }

    private fun sufixoImediato(texto: String, inicio: Int): String {
        val resto = texto.substring(inicio)
        val mesmaLinha = resto.lineSequence().firstOrNull().orEmpty()
        val proximo = NotificationPatterns.VALOR.find(mesmaLinha)
        return if (proximo != null) {
            mesmaLinha.substring(0, proximo.range.first)
        } else {
            mesmaLinha
        }
    }

    private fun ehTaxaPorKm(apos: String): Boolean {
        val compacto = apos.lowercase().replace('\u00a0', ' ').replace(" ", "")
        return compacto.startsWith("/km") || compacto.startsWith("porkm")
    }

    private fun ehTarifaBase(apos: String): Boolean {
        val normal = apos.lowercase()
        return "tarifa" in normal || "dinâmica incl" in normal || "dinamica incl" in normal
    }
}
