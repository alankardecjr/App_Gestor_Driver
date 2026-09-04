package br.com.gestordriver.core

import br.com.gestordriver.model.ConfiguracaoUsuario
import br.com.gestordriver.model.SeguroRecorrencia
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class CorridaParaResumo(
    val valorTotal: Double,
    val kmTotal: Double,
    val minutos: Int,
    val gastoCorrida: Double?,
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
)

object DashboardNumeros {
    fun de(
        corridas: List<CorridaParaResumo>,
        config: ConfiguracaoUsuario,
        diasPeriodo: Int = 1,
    ): NumerosDashboard {
        val receitas = corridas.sumOf { it.valorTotal }
        val kmTotal = corridas.sumOf { it.kmTotal }
        val horas = corridas.sumOf { it.minutos.toLong() } / 60.0
        val combustivel = corridas.mapNotNull { it.gastoCorrida }.takeIf { it.isNotEmpty() }?.sum()
        val oleo = parcelaKm(config.oleoValor, config.oleoKilometragem, kmTotal)
        val pneuD = parcelaKm(config.pneuDianteiroValor, config.pneuDianteiroRodagem, kmTotal)
        val pneuT = parcelaKm(config.pneuTraseiroValor, config.pneuTraseiroRodagem, kmTotal)
        val seguro = rateioSeguro(config.seguroValor, config.seguroRecorrencia, diasPeriodo)
        val ipva = rateioAnual(config.ipvaValor, diasPeriodo)
        val despesas = listOfNotNull(combustivel, oleo, pneuD, pneuT, seguro, ipva).sum()
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
        )
    }

    /** Mensal: valor × (dias/30). Anual: valor × (dias/365). */
    fun rateioSeguro(
        valor: Double,
        recorrencia: SeguroRecorrencia,
        diasPeriodo: Int,
    ): Double? {
        if (valor <= 0.0 || diasPeriodo <= 0) {
            return null
        }
        val parcela = when (recorrencia) {
            SeguroRecorrencia.MENSAL -> valor * (diasPeriodo / 30.0)
            SeguroRecorrencia.ANUAL -> valor * (diasPeriodo / 365.0)
        }
        return parcela.takeIf { it.isFinite() && it > 0.0 }
    }

    /** IPVA é anual: valor × (dias do período / 365). */
    fun rateioAnual(valor: Double, diasPeriodo: Int): Double? {
        if (valor <= 0.0 || diasPeriodo <= 0) {
            return null
        }
        val parcela = valor * (diasPeriodo / 365.0)
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
