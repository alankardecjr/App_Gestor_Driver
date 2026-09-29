package br.com.gestordriver.core

import org.junit.Assert.assertEquals
import org.junit.Test

class MotorClassificacaoTest {
    private val motor = MotorClassificacao()

    @Test
    fun deve_classificar_boa() {
        assertEquals(Classificacao.EXCELENTE, motor.classificarPorValorKm(2.375))
        assertEquals("#FFD600", motor.corDe(Classificacao.BOA))
        assertEquals("#00C853", motor.corDe(Classificacao.EXCELENTE))
        assertEquals(Classificacao.RUIM, motor.classificarPorValorKm(1.3))
        assertEquals("#FFD600", motor.corDe(Classificacao.BAIXA))
    }

    @Test
    fun deve_classificar_ruim() {
        assertEquals(Classificacao.RUIM, motor.classificarPorValorKm(0.5))
        assertEquals("#FF1744", motor.corDe(Classificacao.RUIM))
    }
}
