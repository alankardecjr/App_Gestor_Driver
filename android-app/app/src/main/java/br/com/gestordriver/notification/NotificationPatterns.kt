package br.com.gestordriver.notification

object NotificationPatterns {
    val VALOR = Regex(
        """(?:R\$|RS)\s*([\d.,]+)""",
        RegexOption.IGNORE_CASE,
    )

    val DISTANCIA_COM_UNIDADE = Regex(
        """([\d.,]+)\s*(km|m)\b""",
        RegexOption.IGNORE_CASE,
    )

    val TEMPO = Regex(
        """(\d+)\s*(min|mins|minuto|minutos)\b""",
        RegexOption.IGNORE_CASE,
    )
}
