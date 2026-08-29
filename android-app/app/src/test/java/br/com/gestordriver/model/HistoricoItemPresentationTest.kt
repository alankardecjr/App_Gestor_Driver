package br.com.gestordriver.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import br.com.gestordriver.core.CalculadoraCorrida
import br.com.gestordriver.core.ConfiguracaoUsuario
import br.com.gestordriver.core.Corrida

class HistoricoItemPresentationTest {
    @Test
    fun deve_expor_dados_estruturados_e_linha_horizontal() {
        val calculadora = CalculadoraCorrida(configuracaoUsuario = ConfiguracaoUsuario.padrao())
        val analise = calculadora.calcular(
            corrida = Corrida(
                valorTotal = 38.0,
                kmAtePassageiro = 3.2,
                kmViagem = 12.8,
                tempoEstimado = 24,
            ),
            plataforma = "Uber",
            notaPassageiro = 4.98,
        )

        val item = HistoricoItemPresentation.de(analise).copy(dataHora = "03/08 12:00")

        assertEquals("03/08 12:00", item.dataHora)
        assertEquals("Uber", item.plataforma)
        assertEquals(2.375, item.valorPorKm, 0.001)
        assertEquals(38.0, item.valorTotal, 0.001)
        assertEquals(16.0, item.kmTotal, 0.001)
        assertEquals(24, item.tempoEstimado)
        assertEquals(4.98, item.notaPassageiro!!, 0.001)
        assertEquals(ClassificacaoVisual.EXCELENTE, item.classificacao)
        assertEquals("🔵", item.classificacao.marcador)
        assertEquals("#1E88E5", item.corClassificacao)
        assertEquals(3.2, item.kmAtePassageiro, 0.001)
        assertEquals(12.8, item.kmViagem, 0.001)
        assertEquals(1.28, item.combustivelEstimado!!, 0.01)
        assertEquals(7.9232, item.custoCombustivel!!, 0.01)
        assertEquals(
            "2,38 │ 38,00 │ 16,0 KM │ 24 │ 4,98",
            item.linhaHorizontal,
        )
    }

    @Test
    fun deve_converter_para_analise_corrida() {
        val calculadora = CalculadoraCorrida(configuracaoUsuario = ConfiguracaoUsuario.padrao())
        val original = calculadora.calcular(
            corrida = Corrida(
                valorTotal = 22.5,
                kmAtePassageiro = 1.5,
                kmViagem = 7.0,
                tempoEstimado = 18,
            ),
            plataforma = "99",
        )
        val item = HistoricoItemPresentation.de(original)

        val analise = item.paraAnalise()

        assertEquals(item.valorTotal, analise.valorTotal, 0.001)
        assertEquals(item.kmTotal, analise.kmTotal, 0.001)
        assertEquals(item.plataforma, analise.plataforma)
        assertNull(analise.notaPassageiro)
        assertEquals(item.combustivelEstimado, analise.combustivelEstimado)
        assertEquals(item.custoCombustivel, analise.custoCombustivel)
    }
}
