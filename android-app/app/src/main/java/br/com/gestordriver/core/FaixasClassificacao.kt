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
        formatar(valor)

    fun campoEditavel(campo: Campo): Boolean =
        campo != Campo.RUIM_MIN

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

    fun aplicarMarcas(
        atual: ConfiguracaoUsuario,
        ruimMax: Double,
        boaMax: Double,
    ): ConfiguracaoUsuario {
        val ruim = arredondar(ruimMax).coerceIn(MIN_ABSOLUTO, MAX_ABSOLUTO)
        val boa = arredondar(boaMax).coerceIn(MIN_ABSOLUTO, MAX_ABSOLUTO)
        val deltaRuim = kotlin.math.abs(ruim - arredondar(atual.limiteRuimMax))
        val deltaBoa = kotlin.math.abs(boa - arredondar(atual.limiteBoaMax))
        // Uma marca por vez (como −/+): max de uma faixa puxa min da próxima em +PASSO
        // (e o inverso em −PASSO). Só a marca que mais mudou é aplicada.
        return when {
            deltaRuim < PASSO / 2 && deltaBoa < PASSO / 2 -> atual
            deltaRuim >= deltaBoa -> aplicar(atual, Campo.RUIM_MAX, ruim)
            else -> aplicar(atual, Campo.BOA_MAX, boa)
        }
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
        var boaMin = atual.limiteBoaMin
        var boaMax = atual.limiteBoaMax
        var otimaMin = atual.limiteOtimaMin
        var otimaMax = atual.limiteOtimaMax
        val v = arredondar(valor).coerceIn(MIN_ABSOLUTO, MAX_ABSOLUTO)
        val campoEfetivo = when (campo) {
            Campo.REGULAR_MAX -> Campo.BOA_MAX
            Campo.REGULAR_MIN -> Campo.BOA_MIN
            else -> campo
        }

        when (campoEfetivo) {
            Campo.RUIM_MAX -> ruimMax = v
            Campo.BOA_MIN -> boaMin = v
            Campo.BOA_MAX -> boaMax = v
            Campo.OTIMA_MIN -> otimaMin = v
            Campo.OTIMA_MAX -> otimaMax = v
            else -> return atual
        }

        when (campoEfetivo) {
            Campo.RUIM_MAX -> {
                boaMin = seguinte(ruimMax)
                if (boaMax < boaMin) {
                    boaMax = boaMin
                }
                otimaMin = seguinte(boaMax)
            }
            Campo.BOA_MAX -> {
                if (boaMax < boaMin) {
                    boaMin = boaMax
                    ruimMax = anterior(boaMin)
                }
                otimaMin = seguinte(boaMax)
            }
            Campo.BOA_MIN -> {
                ruimMax = anterior(boaMin)
                if (boaMax < boaMin) {
                    boaMax = boaMin
                }
                otimaMin = seguinte(boaMax)
            }
            Campo.OTIMA_MIN -> {
                boaMax = anterior(otimaMin)
                if (boaMax < boaMin) {
                    boaMin = boaMax
                    ruimMax = anterior(boaMin)
                }
            }
            Campo.OTIMA_MAX -> {
                otimaMax = otimaMax.coerceAtLeast(otimaMin)
            }
            else -> Unit
        }

        return copiarFaixas(
            atual = atual,
            ruimMax = ruimMax,
            boaMin = boaMin,
            boaMax = boaMax,
            otimaMin = otimaMin,
            otimaMax = otimaMax,
        )
    }

    fun normalizar(atual: ConfiguracaoUsuario): ConfiguracaoUsuario {
        var ruimMax = arredondar(atual.limiteRuimMax.coerceAtLeast(MIN_ABSOLUTO))
        var boaMin = arredondar(atual.limiteBoaMin)
        var boaMax = arredondar(atual.limiteBoaMax)
        var otimaMin = arredondar(atual.limiteOtimaMin)
        var otimaMax = arredondar(atual.limiteOtimaMax.coerceIn(MIN_ABSOLUTO, MAX_ABSOLUTO))
        if (boaMin < seguinte(ruimMax)) {
            boaMin = seguinte(ruimMax)
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
        if (otimaMax < otimaMin) {
            otimaMax = otimaMin
        }
        if (boaMax > anterior(otimaMin) && otimaMin > MIN_ABSOLUTO) {
            boaMax = anterior(otimaMin)
            if (boaMax < boaMin) {
                boaMin = boaMax
            }
        }
        if (ruimMax > anterior(boaMin)) {
            ruimMax = anterior(boaMin)
        }
        return copiarFaixas(
            atual = atual,
            ruimMax = ruimMax,
            boaMin = boaMin,
            boaMax = boaMax,
            otimaMin = otimaMin,
            otimaMax = otimaMax,
        )
    }

    private fun copiarFaixas(
        atual: ConfiguracaoUsuario,
        ruimMax: Double,
        boaMin: Double,
        boaMax: Double,
        otimaMin: Double,
        otimaMax: Double,
    ): ConfiguracaoUsuario {
        val ruim = arredondar(ruimMax.coerceAtLeast(MIN_ABSOLUTO))
        val boaIni = arredondar(boaMin)
        val boaFim = arredondar(boaMax)
        val otima = arredondar(otimaMin)
        return atual.copy(
            limiteRuimMin = MIN_ABSOLUTO,
            limiteRuimMax = ruim,
            limiteRegularMin = boaIni,
            limiteRegularMax = boaFim,
            limiteBoaMin = boaIni,
            limiteBoaMax = boaFim,
            limiteOtimaMin = otima,
            limiteOtimaMax = arredondar(otimaMax.coerceIn(otima, MAX_ABSOLUTO)),
        )
    }

    private fun seguinte(valor: Double): Double = arredondar(valor + PASSO)

    private fun anterior(valor: Double): Double =
        arredondar((valor - PASSO).coerceAtLeast(MIN_ABSOLUTO))

    private fun arredondar(valor: Double): Double = round(valor * 100.0) / 100.0
}
