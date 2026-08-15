package br.com.gestordriver.core

import org.junit.Assert.assertEquals
import org.junit.Test

class MotorClassificacaoTest {
    private val motor = MotorClassificacao()

    @Test
    fun deve_classificar_boa() {
        assertEquals(Classificacao.BOA, motor.classificarPorValorKm(2.375))
        assertEquals("#7CB342", motor.corDe(Classificacao.BOA))
    }

    @Test
    fun deve_classificar_ruim() {
        assertEquals(Classificacao.RUIM, motor.classificarPorValorKm(0.5))
        assertEquals("#C62828", motor.corDe(Classificacao.RUIM))
    }
}
