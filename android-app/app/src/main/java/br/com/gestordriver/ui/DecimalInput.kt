package br.com.gestordriver.ui

object DecimalInput {
    fun parse(texto: String): Double? {
        val bruto = texto.trim().replace(" ", "").replace("R$", "", ignoreCase = true)
        if (bruto.isEmpty() || bruto == "," || bruto == ".") {
            return null
        }
        val normalizado = when {
            "," in bruto && "." in bruto -> {
                if (bruto.lastIndexOf(',') > bruto.lastIndexOf('.')) {
                    bruto.replace(".", "").replace(',', '.')
                } else {
                    bruto.replace(",", "")
                }
            }
            "," in bruto -> bruto.replace('.', ' ').replace(" ", "").replace(',', '.')
            "." in bruto && pontosDeMilhar(bruto) -> bruto.replace(".", "")
            else -> bruto
        }
        return normalizado.toDoubleOrNull()
    }

    /** 5.000 e 1.234.567 são milhar. 50.00 e 12.5 são decimal. */
    private fun pontosDeMilhar(texto: String): Boolean {
        val partes = texto.split('.')
        if (partes.size < 2 || partes.first().isEmpty()) {
            return false
        }
        return partes.drop(1).all { it.length == 3 && it.all { digito -> digito.isDigit() } }
    }

    /** Duas casas e vírgula, sem depender do idioma do aparelho. */
    fun formatarFixo(valor: Double): String {
        if (!valor.isFinite()) {
            return "—"
        }
        val negativo = valor < 0.0
        val centavos = kotlin.math.round(kotlin.math.abs(valor) * 100.0).toLong()
        val inteiro = centavos / 100
        val frac = (centavos % 100).toString().padStart(2, '0')
        val texto = "$inteiro,$frac"
        return if (negativo) "-$texto" else texto
    }

    fun formatar(valor: Double): String {
        if (valor == 0.0) {
            return ""
        }
        return if (valor % 1.0 == 0.0) {
            valor.toLong().toString()
        } else {
            valor.toString().replace('.', ',')
        }
    }

    /** Compacta: 1,8 e R$1,8 viram 1,80. "—" e "🔒" permanecem. */
    fun formatarDuasCasasExibicao(texto: String): String {
        val limpo = texto.replace("R$", "", ignoreCase = true).trim()
        if (limpo.isEmpty() || limpo == "—" || limpo == "🔒") {
            return limpo.ifEmpty { "—" }
        }
        val numero = parse(limpo) ?: return limpo
        if (!numero.isFinite()) {
            return "—"
        }
        if (numero == 0.0) {
            return "0,00"
        }
        return formatarReais(numero)
    }

    /** Compacta: 85,71 e 85,7% viram 85,7. "—" e "🔒" permanecem. */
    fun formatarUmaCasaExibicao(texto: String): String {
        val limpo = texto.replace("%", "").replace("R$", "", ignoreCase = true).trim()
        if (limpo.isEmpty() || limpo == "—" || limpo == "🔒") {
            return limpo.ifEmpty { "—" }
        }
        val numero = parse(limpo) ?: return limpo
        if (!numero.isFinite()) {
            return "—"
        }
        val negativo = numero < 0.0
        val decimos = kotlin.math.round(kotlin.math.abs(numero) * 10.0).toLong()
        val textoCasa = "${decimos / 10},${decimos % 10}"
        return if (negativo) "-$textoCasa" else textoCasa
    }

    /** 50 → 50,00 e 50000 → 50.000,00. */
    fun formatarReais(valor: Double): String {
        if (!valor.isFinite() || valor == 0.0) {
            return if (valor == 0.0) "" else "—"
        }
        val negativo = valor < 0.0
        val centavos = kotlin.math.round(kotlin.math.abs(valor) * 100.0).toLong()
        val inteiro = agrupar(centavos / 100)
        val frac = (centavos % 100).toString().padStart(2, '0')
        val texto = "$inteiro,$frac"
        return if (negativo) "-$texto" else texto
    }

    /** 5000 → 5.000. Fração só aparece quando existe: 12,5. */
    fun formatarQuantidade(valor: Double): String {
        if (!valor.isFinite() || valor == 0.0) {
            return if (valor == 0.0) "" else "—"
        }
        val negativo = valor < 0.0
        val absoluto = kotlin.math.abs(valor)
        val centavos = kotlin.math.round(absoluto * 100.0).toLong()
        val inteiro = agrupar(centavos / 100)
        val frac = (centavos % 100).toInt()
        val texto = when {
            frac == 0 -> inteiro
            frac % 10 == 0 -> "$inteiro,${frac / 10}"
            else -> "$inteiro,${frac.toString().padStart(2, '0')}"
        }
        return if (negativo) "-$texto" else texto
    }

    private fun agrupar(inteiro: Long): String {
        val digitos = inteiro.toString()
        val saida = StringBuilder()
        digitos.reversed().forEachIndexed { indice, caractere ->
            if (indice > 0 && indice % 3 == 0) {
                saida.append('.')
            }
            saida.append(caractere)
        }
        return saida.reverse().toString()
    }
}

object DataInput {
    /** 01052026 e 010526 viram 01/05/2026. Texto inválido permanece como foi digitado. */
    fun formatar(texto: String): String {
        val limpo = texto.trim()
        if (limpo.isEmpty()) {
            return limpo
        }
        val digitos = limpo.filter { it.isDigit() }
        val dia: Int
        val mes: Int
        val ano: Int
        when (digitos.length) {
            8 -> {
                dia = digitos.substring(0, 2).toInt()
                mes = digitos.substring(2, 4).toInt()
                ano = digitos.substring(4, 8).toInt()
            }
            6 -> {
                dia = digitos.substring(0, 2).toInt()
                mes = digitos.substring(2, 4).toInt()
                ano = 2000 + digitos.substring(4, 6).toInt()
            }
            else -> return limpo
        }
        if (mes !in 1..12 || ano !in 1900..2100 || dia !in 1..diasNoMes(mes, ano)) {
            return limpo
        }
        return "%02d/%02d/%04d".format(dia, mes, ano)
    }

    private fun diasNoMes(mes: Int, ano: Int): Int = when (mes) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        else -> if (ano % 4 == 0 && (ano % 100 != 0 || ano % 400 == 0)) 29 else 28
    }
}
