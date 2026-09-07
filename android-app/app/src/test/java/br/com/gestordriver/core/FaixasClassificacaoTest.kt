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

    @Test
    fun marcas_deslizantes_max_ruim_encadeia_min_boa_em_um_centavo() {
        val atualizada = FaixasClassificacao.aplicarMarcas(
            ConfiguracaoUsuario.padrao(),
            ruimMax = 1.85,
            boaMax = 1.99,
        )
        assertEquals(1.85, atualizada.limiteRuimMax, 0.0)
        assertEquals(1.86, atualizada.limiteBoaMin, 0.0)
        assertEquals(1.99, atualizada.limiteBoaMax, 0.0)
        assertEquals(2.00, atualizada.limiteOtimaMin, 0.0)
    }

    @Test
    fun marcas_deslizantes_max_boa_encadeia_min_otima_em_um_centavo() {
        val atualizada = FaixasClassificacao.aplicarMarcas(
            ConfiguracaoUsuario.padrao(),
            ruimMax = 1.59,
            boaMax = 2.10,
        )
        assertEquals(1.59, atualizada.limiteRuimMax, 0.0)
        assertEquals(1.60, atualizada.limiteBoaMin, 0.0)
        assertEquals(2.10, atualizada.limiteBoaMax, 0.0)
        assertEquals(2.11, atualizada.limiteOtimaMin, 0.0)
    }

    @Test
    fun marcas_deslizantes_baixar_boa_puxa_ruim_menos_um_centavo() {
        val atualizada = FaixasClassificacao.aplicarMarcas(
            ConfiguracaoUsuario.padrao(),
            ruimMax = 1.59,
            boaMax = 1.50,
        )
        assertEquals(1.50, atualizada.limiteBoaMax, 0.0)
        assertEquals(1.51, atualizada.limiteOtimaMin, 0.0)
        assertEquals(1.50, atualizada.limiteBoaMin, 0.0)
        assertEquals(1.49, atualizada.limiteRuimMax, 0.0)
    }

    @Test
    fun marcas_deslizantes_subir_ruim_acima_da_boa_empurra_cadeia() {
        val atualizada = FaixasClassificacao.aplicarMarcas(
            ConfiguracaoUsuario.padrao(),
            ruimMax = 2.05,
            boaMax = 2.06,
        )
        assertEquals(2.05, atualizada.limiteRuimMax, 0.0)
        assertEquals(2.06, atualizada.limiteBoaMin, 0.0)
        assertEquals(2.06, atualizada.limiteBoaMax, 0.0)
        assertEquals(2.07, atualizada.limiteOtimaMin, 0.0)
    }
}
