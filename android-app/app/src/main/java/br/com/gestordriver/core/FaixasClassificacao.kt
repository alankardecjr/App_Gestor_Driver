package br.com.gestordriver.core

import br.com.gestordriver.model.ConfiguracaoUsuario
import java.util.Locale
import kotlin.math.round

object FaixasClassificacao {
    const val PASSO = 0.01
    const val MIN_ABSOLUTO = 0.0
    const val MAX_ABSOLUTO = 99.0

    enum class Campo {
        RUIM_MIN,
        RUIM_MAX,
        REGULAR_MIN,
        REGULAR_MAX,
        BOA_MIN,
        BOA_MAX,
        OTIMA_MIN,
        OTIMA_MAX,
    }

    fun padrao(): ConfiguracaoUsuario = ConfiguracaoUsuario.padrao()

    fun formatar(valor: Double): String =
        "%.2f".format(Locale.US, arredondar(valor)).replace('.', ',')

    fun rotuloMin(campo: Campo, valor: Double): String =
        if (campo == Campo.RUIM_MIN) "MIN" else formatar(valor)

    fun rotuloMax(campo: Campo, valor: Double): String =
        if (campo == Campo.OTIMA_MAX) "MAX" else formatar(valor)

    fun campoEditavel(campo: Campo): Boolean =
        campo != Campo.RUIM_MIN && campo != Campo.OTIMA_MAX

    fun valorDe(atual: ConfiguracaoUsuario, campo: Campo): Double =
        when (campo) {
            Campo.RUIM_MIN -> atual.limiteRuimMin
            Campo.RUIM_MAX -> atual.limiteRuimMax
            Campo.REGULAR_MIN -> atual.limiteRegularMin
            Campo.REGULAR_MAX -> atual.limiteRegularMax
            Campo.BOA_MIN -> atual.limiteBoaMin
            Campo.BOA_MAX -> atual.limiteBoaMax
            Campo.OTIMA_MIN -> atual.limiteOtimaMin
            Campo.OTIMA_MAX -> atual.limiteOtimaMax
        }

    fun aplicar(
        atual: ConfiguracaoUsuario,
        campo: Campo,
        valor: Double,
    ): ConfiguracaoUsuario {
        if (!campoEditavel(campo)) {
            return atual
        }
        var ruimMax = atual.limiteRuimMax
        var regularMin = atual.limiteRegularMin
        var regularMax = atual.limiteRegularMax
        var boaMin = atual.limiteBoaMin
        var boaMax = atual.limiteBoaMax
        var otimaMin = atual.limiteOtimaMin
        val v = arredondar(valor).coerceIn(MIN_ABSOLUTO, MAX_ABSOLUTO)

        when (campo) {
            Campo.RUIM_MAX -> ruimMax = v
            Campo.REGULAR_MIN -> regularMin = v
            Campo.REGULAR_MAX -> regularMax = v
            Campo.BOA_MIN -> boaMin = v
            Campo.BOA_MAX -> boaMax = v
            Campo.OTIMA_MIN -> otimaMin = v
            else -> return atual
        }

        when (campo) {
            Campo.RUIM_MAX,
            Campo.REGULAR_MAX,
            Campo.BOA_MAX,
            -> {
                if (campo == Campo.REGULAR_MAX && regularMax < regularMin) {
                    regularMin = regularMax
                    ruimMax = anterior(regularMin)
                }
                if (campo == Campo.BOA_MAX && boaMax < boaMin) {
                    boaMin = boaMax
                    regularMax = anterior(boaMin)
                    if (regularMax < regularMin) {
                        regularMin = regularMax
                        ruimMax = anterior(regularMin)
                    }
                }
                regularMin = seguinte(ruimMax)
                if (regularMax < regularMin) {
                    regularMax = regularMin
                }
                boaMin = seguinte(regularMax)
                if (boaMax < boaMin) {
                    boaMax = boaMin
                }
                otimaMin = seguinte(boaMax)
            }

            Campo.REGULAR_MIN,
            Campo.BOA_MIN,
            Campo.OTIMA_MIN,
            -> {
                if (campo == Campo.REGULAR_MIN) {
                    ruimMax = anterior(regularMin)
                    if (regularMax < regularMin) {
                        regularMax = regularMin
                    }
                }
                if (campo == Campo.BOA_MIN) {
                    regularMax = anterior(boaMin)
                    if (regularMax < regularMin) {
                        regularMin = regularMax
                        ruimMax = anterior(regularMin)
                    }
                    if (boaMax < boaMin) {
                        boaMax = boaMin
                    }
                }
                if (campo == Campo.OTIMA_MIN) {
                    boaMax = anterior(otimaMin)
                    if (boaMax < boaMin) {
                        boaMin = boaMax
                        regularMax = anterior(boaMin)
                        if (regularMax < regularMin) {
                            regularMin = regularMax
                            ruimMax = anterior(regularMin)
                        }
                    }
                }
                regularMin = seguinte(ruimMax)
                if (regularMax < regularMin) {
                    regularMax = regularMin
                }
                boaMin = seguinte(regularMax)
                if (boaMax < boaMin) {
                    boaMax = boaMin
                }
                otimaMin = seguinte(boaMax)
            }

            else -> Unit
        }

        return atual.copy(
            limiteRuimMin = MIN_ABSOLUTO,
            limiteRuimMax = arredondar(ruimMax.coerceAtLeast(MIN_ABSOLUTO)),
            limiteRegularMin = arredondar(regularMin),
            limiteRegularMax = arredondar(regularMax),
            limiteBoaMin = arredondar(boaMin),
            limiteBoaMax = arredondar(boaMax),
            limiteOtimaMin = arredondar(otimaMin),
            limiteOtimaMax = MAX_ABSOLUTO,
        )
    }

    fun normalizar(atual: ConfiguracaoUsuario): ConfiguracaoUsuario {
        var ruimMax = arredondar(atual.limiteRuimMax.coerceAtLeast(MIN_ABSOLUTO))
        var regularMin = arredondar(atual.limiteRegularMin)
        var regularMax = arredondar(atual.limiteRegularMax)
        var boaMin = arredondar(atual.limiteBoaMin)
        var boaMax = arredondar(atual.limiteBoaMax)
        var otimaMin = arredondar(atual.limiteOtimaMin)
        if (regularMin < seguinte(ruimMax)) {
            regularMin = seguinte(ruimMax)
        }
        if (regularMax < regularMin) {
            regularMax = regularMin
        }
        if (boaMin < seguinte(regularMax)) {
            boaMin = seguinte(regularMax)
        }
        if (boaMax < boaMin) {
            boaMax = boaMin
        }
        if (otimaMin < seguinte(boaMax)) {
            otimaMin = seguinte(boaMax)
        }
        if (otimaMin > MAX_ABSOLUTO) {
            otimaMin = MAX_ABSOLUTO
        }
        if (boaMax > anterior(otimaMin) && otimaMin > MIN_ABSOLUTO) {
            boaMax = anterior(otimaMin)
            if (boaMax < boaMin) {
                boaMin = boaMax
            }
        }
        if (regularMax > anterior(boaMin)) {
            regularMax = anterior(boaMin)
            if (regularMax < regularMin) {
                regularMin = regularMax
            }
        }
        if (ruimMax > anterior(regularMin)) {
            ruimMax = anterior(regularMin)
        }
        return atual.copy(
            limiteRuimMin = MIN_ABSOLUTO,
            limiteRuimMax = ruimMax,
            limiteRegularMin = regularMin,
            limiteRegularMax = regularMax,
            limiteBoaMin = boaMin,
            limiteBoaMax = boaMax,
            limiteOtimaMin = otimaMin,
            limiteOtimaMax = MAX_ABSOLUTO,
        )
    }

    private fun seguinte(valor: Double): Double = arredondar(valor + PASSO)

    private fun anterior(valor: Double): Double =
        arredondar((valor - PASSO).coerceAtLeast(MIN_ABSOLUTO))

    private fun arredondar(valor: Double): Double = round(valor * 100.0) / 100.0
}
