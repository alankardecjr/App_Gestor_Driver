package br.com.gestordriver.ui

object DecimalInput {
    fun parse(texto: String): Double? {
        val bruto = texto.trim().replace(" ", "")
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
            "," in bruto -> bruto.replace(',', '.')
            else -> bruto
        }
        return normalizado.toDoubleOrNull()
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
            valor.toInt().toString()
        } else {
            valor.toString().replace('.', ',')
        }
    }
}
