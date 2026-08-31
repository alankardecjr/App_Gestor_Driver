package br.com.gestordriver.notification

object NotificationPatterns {
    val VALOR = Regex(
        "(?:R\\$|RS|\\$)[\\s\\u00A0]*([\\d.,]+)",
        RegexOption.IGNORE_CASE,
    )

    val DISTANCIA_COM_UNIDADE = Regex(
        "([\\d.,]+)[\\s\\u00A0]*(km|m)\\b",
        RegexOption.IGNORE_CASE,
    )

    val TEMPO = Regex(
        "(?<!-)(\\d+)[\\s\\u00A0]*(min|mins|minuto|minutos)\\b",
        RegexOption.IGNORE_CASE,
    )

    /** Card 99 e Uber: "6min (971m)", "3 min (0.4 km)", "15 minutos (3.3 km)". */
    val TRECHO_TEMPO_DISTANCIA = Regex(
        "(\\d+)[\\s\\u00A0]*min(?:uto)?s?[\\s\\u00A0]*\\([\\s\\u00A0]*([\\d.,]+)[\\s\\u00A0]*(km|m)\\)",
        RegexOption.IGNORE_CASE,
    )

    /** Nota do passageiro no card 99: linha "4,95" e em seguida "79 corridas". */
    val NOTA_COM_CORRIDAS = Regex(
        "(?mi)^\\s*([1-5][.,]\\d{1,2})\\s*(?:★|⭐|\\*)?\\s*\\r?\\n\\s*(\\d+)\\s*corridas",
    )

    /** OCR 99: "t4,93 · 435 corridas" na mesma linha. */
    val NOTA_CORRIDAS_MESMA_LINHA = Regex(
        "([1-5][.,]\\d{1,2})\\D{0,12}(\\d{1,5})\\s*corridas",
        RegexOption.IGNORE_CASE,
    )

    val NOTA_COLADA_CORRIDAS = Regex(
        "([1-5][.,]\\d{2})(\\d+)\\s*corridas",
        RegexOption.IGNORE_CASE,
    )

    val NOTA_COM_ESTRELA = Regex(
        "(?:★|⭐)\\s*([1-5][.,]\\d{1,2})|([1-5][.,]\\d{1,2})\\s*(?:★|⭐)",
    )

    val NOTA_LINHA_ISOLADA = Regex(
        "(?m)^\\s*([4-5][.,]\\d{2})\\s*$",
    )

    val NOTA_COM_VIAGENS = Regex(
        "(?mi)^\\s*([1-5][.,]\\d{1,2})\\s*(?:★|⭐|\\*)?\\s*\\r?\\n\\s*([\\d.]+)\\s*(?:viagens|trips)\\b",
    )

    val KM_ATE_PASSAGEIRO = Regex(
        "([\\d.,]+)[\\s\\u00A0]*(km|m)\\b[\\s\\u00A0]*(?:até você|ate voce|até o passageiro|ate o passageiro|ate passageiro|de você|de voce|away|pickup|pick-up|coleta)",
        RegexOption.IGNORE_CASE,
    )

    val KM_VIAGEM = Regex(
        "([\\d.,]+)[\\s\\u00A0]*(km|m)\\b[\\s\\u00A0]*(?:até o destino|ate o destino|de viagem|viagem|dropoff|drop-off|drop off|destino|desembarque|entrega)",
        RegexOption.IGNORE_CASE,
    )

    /** Uber: "3 min" e na linha seguinte "1,2 km", ou "3 min • 1,2 km". */
    val PAR_TEMPO_DISTANCIA = Regex(
        "(\\d+)[\\s\\u00A0]*min(?:uto)?s?\\b[\\s\\u00A0•·\\r\\n]{0,80}([\\d.,]+)[\\s\\u00A0]*(km|m)\\b",
        RegexOption.IGNORE_CASE,
    )

    /** Uber: nota e viagens na mesma linha ("4.98  234 trips"). */
    val NOTA_VIAGENS_MESMA_LINHA = Regex(
        "([1-5][.,]\\d{1,2})\\D{0,12}([\\d.]+)\\s*(?:viagens|trips|viagem)\\b",
        RegexOption.IGNORE_CASE,
    )

    /** Uber: "4,92 (157)" nota e quantidade de viagens. */
    val NOTA_COM_PARENTESES = Regex(
        "([1-5][.,]\\d{1,2})[\\s\\u00A0]*\\([\\s\\u00A0]*\\d+[\\s\\u00A0]*\\)",
    )
}
