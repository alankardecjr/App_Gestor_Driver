package br.com.gestordriver.core

import org.junit.Assert.assertEquals
import org.junit.Test

class MotorClassificacaoTest {
    private val motor = MotorClassificacao()

    @Test
    fun deve_classificar_boa() {
        assertEquals(Classificacao.EXCELENTE, motor.classificarPorValorKm(2.375))
        assertEquals("#2E7D32", motor.corDe(Classificacao.BOA))
        assertEquals("#1E88E5", motor.corDe(Classificacao.EXCELENTE))
        assertEquals("#EF6C00", motor.corDe(Classificacao.REGULAR))
        assertEquals("#EF6C00", motor.corDe(Classificacao.BAIXA))
    }

    @Test
    fun deve_classificar_ruim() {
        assertEquals(Classificacao.RUIM, motor.classificarPorValorKm(0.5))
        assertEquals("#C62828", motor.corDe(Classificacao.RUIM))
    }
}
