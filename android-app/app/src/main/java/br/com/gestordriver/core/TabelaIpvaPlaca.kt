package br.com.gestordriver.core

import java.time.Month
import java.time.format.TextStyle

/**
 * Referência amplamente usada no Brasil: final da placa → mês de vencimento do IPVA.
 * (Calendários oficiais variam por estado; esta tabela indica o mês típico.)
 *
 * 1→jan, 2→fev, …, 9→set, 0→out.
 */
object TabelaIpvaPlaca {
    fun digitoFinal(placaOuFinal: String): Int? {
        val digitos = placaOuFinal.filter { it.isDigit() }
        if (digitos.isEmpty()) {
            return null
        }
        return digitos.last().digitToInt()
    }

    fun mesVencimento(placaOuFinal: String): Month? {
        val digito = digitoFinal(placaOuFinal) ?: return null
        return when (digito) {
            1 -> Month.JANUARY
            2 -> Month.FEBRUARY
            3 -> Month.MARCH
            4 -> Month.APRIL
            5 -> Month.MAY
            6 -> Month.JUNE
            7 -> Month.JULY
            8 -> Month.AUGUST
            9 -> Month.SEPTEMBER
            0 -> Month.OCTOBER
            else -> null
        }
    }

    fun rotuloMesVencimento(placaOuFinal: String): String {
        val mes = mesVencimento(placaOuFinal) ?: return "—"
        return mes.getDisplayName(TextStyle.FULL, CalendarioApp.localePtBr)
            .replaceFirstChar { it.titlecase(CalendarioApp.localePtBr) }
    }

    fun textoVencimento(placaOuFinal: String): String {
        val mes = rotuloMesVencimento(placaOuFinal)
        return if (mes == "—") {
            "Informe o final da placa (0–9)"
        } else {
            "IPVA vence em $mes"
        }
    }
}
