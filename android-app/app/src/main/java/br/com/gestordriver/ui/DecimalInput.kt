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
