package br.com.gestordriver.core

import br.com.gestordriver.model.ConfiguracaoUsuario
import org.junit.Assert.assertEquals
import org.junit.Test

class FaixasClassificacaoTest {
    @Test
    fun padrao_tres_faixas_sem_sobreposicao() {
        val c = ConfiguracaoUsuario.padrao()
        assertEquals(1.59, c.limiteRuimMax, 0.0)
        assertEquals(1.60, c.limiteBoaMin, 0.0)
        assertEquals(1.99, c.limiteBoaMax, 0.0)
        assertEquals(2.00, c.limiteOtimaMin, 0.0)
        assertEquals(c.limiteBoaMin, c.limiteRuimMax + FaixasClassificacao.PASSO, 0.0001)
        assertEquals(c.limiteOtimaMin, c.limiteBoaMax + FaixasClassificacao.PASSO, 0.0001)
    }

    @Test
    fun editar_max_ruim_puxa_min_boa() {
        val atualizada = FaixasClassificacao.aplicar(
            ConfiguracaoUsuario.padrao(),
            FaixasClassificacao.Campo.RUIM_MAX,
            1.70,
        )
        assertEquals(1.70, atualizada.limiteRuimMax, 0.0)
        assertEquals(1.71, atualizada.limiteBoaMin, 0.0)
        assertEquals(atualizada.limiteOtimaMin, atualizada.limiteBoaMax + FaixasClassificacao.PASSO, 0.0001)
    }

    @Test
    fun editar_max_boa_puxa_min_otima() {
        val atualizada = FaixasClassificacao.aplicar(
            ConfiguracaoUsuario.padrao(),
            FaixasClassificacao.Campo.BOA_MAX,
            2.40,
        )
        assertEquals(2.40, atualizada.limiteBoaMax, 0.0)
        assertEquals(2.41, atualizada.limiteOtimaMin, 0.0)
    }

    @Test
    fun ruim_min_e_otima_max_nao_mudam() {
        val padrao = ConfiguracaoUsuario.padrao()
        val min = FaixasClassificacao.aplicar(padrao, FaixasClassificacao.Campo.RUIM_MIN, 5.0)
        val max = FaixasClassificacao.aplicar(padrao, FaixasClassificacao.Campo.OTIMA_MAX, 5.0)
        assertEquals(padrao, min)
        assertEquals(padrao, max)
    }

    @Test
    fun normalizar_corrige_boa_antes_do_fim_do_ruim() {
        val quebrada = ConfiguracaoUsuario.padrao().copy(
            limiteRuimMax = 1.80,
            limiteBoaMin = 1.20,
        )
        val corrigida = FaixasClassificacao.normalizar(quebrada)
        assertEquals(1.80, corrigida.limiteRuimMax, 0.0)
        assertEquals(1.81, corrigida.limiteBoaMin, 0.0)
        assertEquals(corrigida.limiteOtimaMin, corrigida.limiteBoaMax + FaixasClassificacao.PASSO, 0.0001)
    }
}
