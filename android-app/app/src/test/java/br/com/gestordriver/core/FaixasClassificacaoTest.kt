package br.com.gestordriver.core

import br.com.gestordriver.model.ConfiguracaoUsuario
import org.junit.Assert.assertEquals
import org.junit.Test

class FaixasClassificacaoTest {
    @Test
    fun padrao_nao_tem_sobreposicao() {
        val c = ConfiguracaoUsuario.padrao()
        assertEquals(0.0, c.limiteRuimMin, 0.0)
        assertEquals(1.19, c.limiteRuimMax, 0.0)
        assertEquals(1.20, c.limiteRegularMin, 0.0)
        assertEquals(1.59, c.limiteRegularMax, 0.0)
        assertEquals(1.60, c.limiteBoaMin, 0.0)
        assertEquals(1.99, c.limiteBoaMax, 0.0)
        assertEquals(2.00, c.limiteOtimaMin, 0.0)
        assertEquals(c.limiteRegularMin, c.limiteRuimMax + FaixasClassificacao.PASSO, 0.0001)
        assertEquals(c.limiteBoaMin, c.limiteRegularMax + FaixasClassificacao.PASSO, 0.0001)
        assertEquals(c.limiteOtimaMin, c.limiteBoaMax + FaixasClassificacao.PASSO, 0.0001)
    }

    @Test
    fun editar_max_ruim_sugere_min_regular() {
        val atualizada = FaixasClassificacao.aplicar(
            ConfiguracaoUsuario.padrao(),
            FaixasClassificacao.Campo.RUIM_MAX,
            1.50,
        )
        assertEquals(1.50, atualizada.limiteRuimMax, 0.0)
        assertEquals(1.51, atualizada.limiteRegularMin, 0.0)
        assertEquals(1.59, atualizada.limiteRegularMax, 0.0)
        assertEquals(1.60, atualizada.limiteBoaMin, 0.0)
    }

    @Test
    fun editar_max_regular_sugere_min_boa() {
        val atualizada = FaixasClassificacao.aplicar(
            ConfiguracaoUsuario.padrao(),
            FaixasClassificacao.Campo.REGULAR_MAX,
            1.80,
        )
        assertEquals(1.80, atualizada.limiteRegularMax, 0.0)
        assertEquals(1.81, atualizada.limiteBoaMin, 0.0)
        assertEquals(1.99, atualizada.limiteBoaMax, 0.0)
        assertEquals(2.00, atualizada.limiteOtimaMin, 0.0)
    }

    @Test
    fun editar_max_boa_sugere_min_otima() {
        val atualizada = FaixasClassificacao.aplicar(
            ConfiguracaoUsuario.padrao(),
            FaixasClassificacao.Campo.BOA_MAX,
            2.40,
        )
        assertEquals(2.40, atualizada.limiteBoaMax, 0.0)
        assertEquals(2.41, atualizada.limiteOtimaMin, 0.0)
    }

    @Test
    fun editar_min_regular_ajusta_max_ruim() {
        val atualizada = FaixasClassificacao.aplicar(
            ConfiguracaoUsuario.padrao(),
            FaixasClassificacao.Campo.REGULAR_MIN,
            1.40,
        )
        assertEquals(1.39, atualizada.limiteRuimMax, 0.0)
        assertEquals(1.40, atualizada.limiteRegularMin, 0.0)
    }

    @Test
    fun max_menor_que_min_da_mesma_faixa_reorganiza_cadeia() {
        val atualizada = FaixasClassificacao.aplicar(
            ConfiguracaoUsuario.padrao(),
            FaixasClassificacao.Campo.REGULAR_MAX,
            1.10,
        )
        assertEquals(1.10, atualizada.limiteRegularMax, 0.0)
        assertEquals(1.10, atualizada.limiteRegularMin, 0.0)
        assertEquals(1.09, atualizada.limiteRuimMax, 0.0)
        assertEquals(1.11, atualizada.limiteBoaMin, 0.0)
    }

    @Test
    fun ruim_min_e_otima_max_nao_mudam() {
        val padrao = ConfiguracaoUsuario.padrao()
        val min = FaixasClassificacao.aplicar(padrao, FaixasClassificacao.Campo.RUIM_MIN, 5.0)
        val max = FaixasClassificacao.aplicar(padrao, FaixasClassificacao.Campo.OTIMA_MAX, 5.0)
        assertEquals(padrao, min)
        assertEquals(padrao, max)
        assertEquals("MIN", FaixasClassificacao.rotuloMin(FaixasClassificacao.Campo.RUIM_MIN, 0.0))
        assertEquals("MAX", FaixasClassificacao.rotuloMax(FaixasClassificacao.Campo.OTIMA_MAX, 99.0))
    }
}
