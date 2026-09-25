package br.com.gestordriver.core

import br.com.gestordriver.model.ConfiguracaoUsuario
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class CorridaParaResumo(
    val valorTotal: Double,
    val kmTotal: Double,
    val minutos: Int,
    val gastoCorrida: Double?,
    val litros: Double? = null,
)

data class NumerosDashboard(
    val corridas: Int,
    val receitas: Double,
    val despesas: Double,
    val saldo: Double,
    val ganhoPorKm: Double,
    val custoPorKm: Double,
    val ganhoPorHora: Double,
    val custoPorHora: Double,
    val custoPorCorrida: Double,
    val lucroPorCorrida: Double,
    val combustivel: Double?,
    val oleo: Double?,
    val pneuDianteiro: Double?,
    val pneuTraseiro: Double?,
    val seguro: Double?,
    val ipva: Double?,
    val kmTotal: Double,
    val minutos: Int,
    val litros: Double?,
)

object DashboardNumeros {
    fun de(
        corridas: List<CorridaParaResumo>,
        config: ConfiguracaoUsuario,
        dia: LocalDate,
        periodo: CalendarioPeriodo,
    ): NumerosDashboard {
        val receitas = corridas.sumOf { it.valorTotal }
        val kmTotal = corridas.sumOf { it.kmTotal }
        val minutos = corridas.sumOf { it.minutos }
        val horas = minutos / 60.0
        val litros = corridas.mapNotNull { it.litros }.takeIf { it.isNotEmpty() }?.sum()
        val combustivel = corridas.mapNotNull { it.gastoCorrida }.takeIf { it.isNotEmpty() }?.sum()
        val oleo = parcelaKm(config.oleoValor, config.oleoKilometragem, kmTotal)
        val pneuD = parcelaKm(config.pneuDianteiroValor, config.pneuDianteiroRodagem, kmTotal)
        val pneuT = parcelaKm(config.pneuTraseiroValor, config.pneuTraseiroRodagem, kmTotal)
        val seguro = parcelaSeguro(config.seguroValor, dia, periodo)
        val ipva = parcelaIpva(config.ipvaValor, dia, periodo)
        val despesas = listOfNotNull(combustivel, seguro, ipva).sum()
        val saldo = receitas - despesas
        val n = corridas.size
        return NumerosDashboard(
            corridas = n,
            receitas = receitas,
            despesas = despesas,
            saldo = saldo,
            ganhoPorKm = if (kmTotal > 0) receitas / kmTotal else 0.0,
            custoPorKm = if (kmTotal > 0) despesas / kmTotal else 0.0,
            ganhoPorHora = if (horas > 0) receitas / horas else 0.0,
            custoPorHora = if (horas > 0) despesas / horas else 0.0,
            custoPorCorrida = if (n > 0) despesas / n else 0.0,
            lucroPorCorrida = if (n > 0) saldo / n else 0.0,
            combustivel = combustivel,
            oleo = oleo,
            pneuDianteiro = pneuD,
            pneuTraseiro = pneuT,
            seguro = seguro,
            ipva = ipva,
            kmTotal = kmTotal,
            minutos = minutos,
            litros = litros,
        )
    }

    /** Seguro mensal. Mês = valor. Ano = × 12. Dia e semana usam os dias daquele mês. */
    fun parcelaSeguro(valor: Double, dia: LocalDate, periodo: CalendarioPeriodo): Double? {
        if (valor <= 0.0) {
            return null
        }
        val diasMes = YearMonth.from(dia).lengthOfMonth().toDouble()
        val parcela = when (periodo) {
            CalendarioPeriodo.DIA -> valor / diasMes
            CalendarioPeriodo.SEMANA -> valor * 7.0 / diasMes
            CalendarioPeriodo.MES -> valor
            CalendarioPeriodo.ANO -> valor * 12.0
        }
        return parcela.takeIf { it.isFinite() && it > 0.0 }
    }

    /** IPVA anual. Ano = valor. Mês = ÷ 12. Dia e semana usam os dias daquele ano. */
    fun parcelaIpva(valor: Double, dia: LocalDate, periodo: CalendarioPeriodo): Double? {
        if (valor <= 0.0) {
            return null
        }
        val diasAno = if (dia.isLeapYear) 366.0 else 365.0
        val parcela = when (periodo) {
            CalendarioPeriodo.DIA -> valor / diasAno
            CalendarioPeriodo.SEMANA -> valor * 7.0 / diasAno
            CalendarioPeriodo.MES -> valor / 12.0
            CalendarioPeriodo.ANO -> valor
        }
        return parcela.takeIf { it.isFinite() && it > 0.0 }
    }

    private fun parcelaKm(valor: Double, baseKm: Double, kmPeriodo: Double): Double? {
        if (valor <= 0.0 || baseKm <= 0.0 || kmPeriodo <= 0.0) {
            return null
        }
        return (valor / baseKm) * kmPeriodo
    }
}

object AlertaOleo {
    const val MARGEM_KM = 500.0

    enum class Nivel { OK, AVISO, VENCIDO }

    fun nivel(intervaloKm: Double, kmDesdeTroca: Double): Nivel {
        if (intervaloKm <= 0.0) {
            return Nivel.OK
        }
        val restante = intervaloKm - kmDesdeTroca
        return when {
            restante <= 0.0 -> Nivel.VENCIDO
            restante <= MARGEM_KM -> Nivel.AVISO
            else -> Nivel.OK
        }
    }

    fun kmDesdeTroca(
        dataTexto: String,
        pontos: List<Pair<LocalDate?, Double>>,
    ): Double {
        val inicio = parseData(dataTexto)
        return pontos.sumOf { (dia, km) ->
            if (dia == null) {
                0.0
            } else if (inicio == null || !dia.isBefore(inicio)) {
                km
            } else {
                0.0
            }
        }
    }

    fun parseData(texto: String): LocalDate? {
        val limpo = texto.trim()
        if (limpo.isEmpty()) {
            return null
        }
        val formatos = listOf("dd/MM/yyyy", "dd/MM/yy", "dd-MM-yyyy", "yyyy-MM-dd")
        for (padrao in formatos) {
            val data = runCatching {
                LocalDate.parse(limpo, DateTimeFormatter.ofPattern(padrao))
            }.getOrNull()
            if (data != null) {
                return data
            }
        }
        return null
    }
}
