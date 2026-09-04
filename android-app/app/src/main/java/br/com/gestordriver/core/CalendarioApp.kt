package br.com.gestordriver.core

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.min

/**
 * Calendário civil do aparelho. Histórico e dashboard usam o mesmo dia,
 * a mesma semana (domingo a sábado) e o mesmo mês.
 */
object CalendarioApp {
    val localePtBr: Locale = Locale.forLanguageTag("pt-BR")

    fun zona(): ZoneId = ZoneId.systemDefault()

    fun hoje(): LocalDate = LocalDate.now(zona())

    fun agora(): LocalDateTime = LocalDateTime.now(zona())

    fun diaDe(epochDay: Long): LocalDate =
        if (epochDay == 0L) hoje() else LocalDate.ofEpochDay(epochDay)

    fun domingoDaSemana(dia: LocalDate): LocalDate =
        dia.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

    fun sabadoDaSemana(dia: LocalDate): LocalDate =
        dia.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))

    fun diasDaSemana(dia: LocalDate): List<LocalDate> {
        val domingo = domingoDaSemana(dia)
        return (0L..6L).map { domingo.plusDays(it) }
    }

    fun avancarDia(dia: LocalDate, deltaDias: Int): LocalDate =
        dia.plusDays(deltaDias.toLong())

    fun avancarSemana(dia: LocalDate, deltaSemanas: Int): LocalDate =
        dia.plusWeeks(deltaSemanas.toLong())

    fun avancarMes(dia: LocalDate, deltaMeses: Int): LocalDate {
        val alvo = dia.plusMonths(deltaMeses.toLong())
        val ultimo = alvo.with(TemporalAdjusters.lastDayOfMonth()).dayOfMonth
        return alvo.withDayOfMonth(min(dia.dayOfMonth, ultimo))
    }

    fun primeiroDoMes(dia: LocalDate): LocalDate = dia.withDayOfMonth(1)

    fun ultimoDoMes(dia: LocalDate): LocalDate =
        dia.with(TemporalAdjusters.lastDayOfMonth())

    fun mesDe(dia: LocalDate): YearMonth = YearMonth.from(dia)

    fun gradeMes(dia: LocalDate): List<LocalDate> {
        val inicio = domingoDaSemana(primeiroDoMes(dia))
        return (0L until 42L).map { inicio.plusDays(it) }
    }

    fun noMes(dia: LocalDate, referencia: LocalDate): Boolean =
        dia.month == referencia.month && dia.year == referencia.year

    fun rotuloMesAno(dia: LocalDate): String {
        val mes = dia.month.getDisplayName(TextStyle.FULL, localePtBr)
        return "${mes.replaceFirstChar { it.titlecase(localePtBr) }} ${dia.year}"
    }

    fun rotuloDiaCurto(dia: LocalDate): String {
        val semana = dia.dayOfWeek.getDisplayName(TextStyle.SHORT, localePtBr)
        return "${semana.replaceFirstChar { it.titlecase(localePtBr) }} ${dia.dayOfMonth}"
    }

    fun rotuloSemana(dia: LocalDate): String {
        val inicio = domingoDaSemana(dia)
        val fim = sabadoDaSemana(dia)
        return "${inicio.dayOfMonth} ${mesCurto(inicio)} – ${fim.dayOfMonth} ${mesCurto(fim)}"
    }

    fun avancarAno(dia: LocalDate, deltaAnos: Int): LocalDate {
        val alvo = dia.plusYears(deltaAnos.toLong())
        val ultimo = alvo.with(TemporalAdjusters.lastDayOfMonth()).dayOfMonth
        return alvo.withDayOfMonth(min(dia.dayOfMonth, ultimo))
    }

    fun noAno(dia: LocalDate, referencia: LocalDate): Boolean =
        dia.year == referencia.year

    fun rotuloAno(dia: LocalDate): String = "${dia.year}"

    fun avancarPeriodo(dia: LocalDate, periodo: CalendarioPeriodo, delta: Int): LocalDate =
        when (periodo) {
            CalendarioPeriodo.DIA -> avancarDia(dia, delta)
            CalendarioPeriodo.SEMANA -> avancarSemana(dia, delta)
            CalendarioPeriodo.MES -> avancarMes(dia, delta)
            CalendarioPeriodo.ANO -> avancarAno(dia, delta)
        }

    fun noPeriodo(dia: LocalDate, referencia: LocalDate, periodo: CalendarioPeriodo): Boolean =
        when (periodo) {
            CalendarioPeriodo.DIA -> dia == referencia
            CalendarioPeriodo.SEMANA ->
                !dia.isBefore(domingoDaSemana(referencia)) &&
                    !dia.isAfter(sabadoDaSemana(referencia))
            CalendarioPeriodo.MES -> noMes(dia, referencia)
            CalendarioPeriodo.ANO -> noAno(dia, referencia)
        }

    fun rotuloPeriodo(dia: LocalDate, periodo: CalendarioPeriodo): String =
        when (periodo) {
            CalendarioPeriodo.DIA -> rotuloDiaCurto(dia)
            CalendarioPeriodo.SEMANA -> rotuloSemana(dia)
            CalendarioPeriodo.MES -> rotuloMesAno(dia)
            CalendarioPeriodo.ANO -> rotuloAno(dia)
        }

    fun rotuloPeriodoCabecalho(dia: LocalDate, periodo: CalendarioPeriodo): String =
        when (periodo) {
            CalendarioPeriodo.DIA -> "${rotuloDiaCurto(dia)} ${dia.year}"
            CalendarioPeriodo.SEMANA -> {
                val inicio = domingoDaSemana(dia)
                val fim = sabadoDaSemana(dia)
                "${inicio.dayOfMonth} ${mesCurto(inicio).uppercase(localePtBr)} – " +
                    "${fim.dayOfMonth} ${mesCurto(fim).uppercase(localePtBr)} ${fim.year}"
            }
            CalendarioPeriodo.MES -> rotuloMesAno(dia)
            CalendarioPeriodo.ANO -> rotuloAno(dia)
        }

    fun subtituloPeriodo(dia: LocalDate, periodo: CalendarioPeriodo): String {
        val hoje = hoje()
        return when (periodo) {
            CalendarioPeriodo.DIA -> if (dia == hoje) "Hoje" else ""
            CalendarioPeriodo.SEMANA ->
                if (noPeriodo(hoje, dia, CalendarioPeriodo.SEMANA)) "Semana atual" else ""
            CalendarioPeriodo.MES ->
                if (noMes(hoje, dia)) "Mês atual" else ""
            CalendarioPeriodo.ANO ->
                if (noAno(hoje, dia)) "Ano atual" else ""
        }
    }

    fun faixaDiasVisivel(dia: LocalDate, periodo: CalendarioPeriodo): List<LocalDate> =
        when (periodo) {
            CalendarioPeriodo.MES -> gradeMes(dia)
            CalendarioPeriodo.ANO -> emptyList()
            else -> diasDaSemana(dia)
        }

    fun textoVazio(periodo: CalendarioPeriodo): String =
        when (periodo) {
            CalendarioPeriodo.DIA -> "Nenhuma corrida aceita neste dia."
            CalendarioPeriodo.SEMANA -> "Nenhuma corrida aceita nesta semana."
            CalendarioPeriodo.MES -> "Nenhuma corrida aceita neste mês."
            CalendarioPeriodo.ANO -> "Nenhuma corrida aceita neste ano."
        }

    fun diasDoPeriodo(dia: LocalDate, periodo: CalendarioPeriodo): Int =
        when (periodo) {
            CalendarioPeriodo.DIA -> 1
            CalendarioPeriodo.SEMANA -> 7
            CalendarioPeriodo.MES -> mesDe(dia).lengthOfMonth()
            CalendarioPeriodo.ANO -> if (dia.isLeapYear) 366 else 365
        }

    fun rotulosCabecalhoSemana(): List<String> =
        diasDaSemana(hoje()).map {
            it.dayOfWeek.getDisplayName(TextStyle.SHORT, localePtBr)
                .uppercase(localePtBr)
                .take(3)
        }

    /** Primeiro dia de cada mês do ano da referência (para seletor do dashboard). */
    fun mesesDoAno(dia: LocalDate): List<LocalDate> =
        (1..12).map { YearMonth.of(dia.year, it).atDay(1) }

    fun rotuloMesChip(dia: LocalDate): String =
        dia.month.getDisplayName(TextStyle.SHORT, localePtBr)
            .uppercase(localePtBr)
            .take(3)
            .trimEnd('.')

    private fun mesCurto(dia: LocalDate): String =
        dia.month.getDisplayName(TextStyle.SHORT, localePtBr)
            .replaceFirstChar { it.titlecase(localePtBr) }
            .trimEnd('.')
}

enum class CalendarioPeriodo {
    DIA,
    SEMANA,
    MES,
    ANO,
    ;

    val rotulo: String
        get() = when (this) {
            DIA -> "Dia"
            SEMANA -> "Semana"
            MES -> "Mês"
            ANO -> "Ano"
        }

    fun vizinho(delta: Int): CalendarioPeriodo =
        entries[(ordinal + delta).coerceIn(0, entries.lastIndex)]

    companion object {
        fun de(nome: String?): CalendarioPeriodo =
            entries.firstOrNull { it.name.equals(nome, ignoreCase = true) } ?: DIA
    }
}
