package br.com.gestordriver.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValorPorHoraTest {

    @Test
    fun deve_calcular_valor_por_hora_com_tempo_total() {
        // tempoEstimado ja e a soma das pernas do card (ate passageiro + viagem).
        val corrida = Corrida(
            valorTotal = 38.0,
            kmAtePassageiro = 3.2,
            kmViagem = 12.8,
            tempoEstimado = 24,
        )

        // 38,00 / (24 min / 60) = 95,00 por hora
        assertEquals(95.0, corrida.valorPorHora!!, 0.001)
    }

    @Test
    fun analise_deve_expor_valor_por_hora() {
        val corrida = Corrida(
            valorTotal = 30.0,
            kmAtePassageiro = 2.0,
            kmViagem = 10.0,
            tempoEstimado = 20,
        )

        val analise = CalculadoraCorrida().calcular(corrida)

        // 30,00 / (20 min / 60) = 90,00 por hora
        assertEquals(90.0, analise.valorPorHora!!, 0.001)
    }

    @Test
    fun valor_por_hora_deve_ser_null_sem_tempo() {
        val corrida = Corrida(
            valorTotal = 30.0,
            kmAtePassageiro = 2.0,
            kmViagem = 10.0,
            tempoEstimado = null,
        )

        assertNull(corrida.valorPorHora)
        assertNull(CalculadoraCorrida().calcular(corrida).valorPorHora)
    }

    @Test
    fun valor_por_hora_deve_ser_null_com_tempo_zero() {
        val corrida = Corrida(
            valorTotal = 30.0,
            kmAtePassageiro = 2.0,
            kmViagem = 10.0,
            tempoEstimado = 0,
        )

        assertNull(corrida.valorPorHora)
    }
}
