package br.com.gestordriver.data

import br.com.gestordriver.core.Classificacao
import br.com.gestordriver.core.MotorClassificacao
import br.com.gestordriver.model.Combustivel
import br.com.gestordriver.model.ConfiguracaoUsuario
import br.com.gestordriver.notification.NotificationData
import br.com.gestordriver.notification.RideNotificationEvent
import br.com.gestordriver.notification.RideNotificationProcessor
import br.com.gestordriver.ui.ConfiguracoesViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfiguracaoPersistenciaTest {

    @Test
    fun salvar_e_reabrir_viewmodel_mantem_veiculo_e_faixas() {
        val store = MemoriaConfiguracaoStore()
        val antes = ConfiguracoesViewModel(store)
        antes.atualizarMarca("Honda")
        antes.atualizarModelo("Civic")
        antes.atualizarVersao("EX")
        antes.atualizarAno("2020")
        antes.atualizarConsumoGasolina(11.0)
        antes.atualizarPrecoGasolina(6.49)
        antes.selecionarCombustivel(Combustivel.ETANOL)
        antes.atualizarLimiteOtimaMin(3.10)
        antes.atualizarLimiteBoaMin(2.40)
        antes.salvar()

        val depois = ConfiguracoesViewModel(store)
        assertEquals("Honda", depois.configuracao.marcaVeiculo)
        assertEquals("Civic", depois.configuracao.modeloVeiculo)
        assertEquals("EX", depois.configuracao.versaoVeiculo)
        assertEquals("2020", depois.configuracao.anoVeiculo)
        assertEquals(11.0, depois.configuracao.consumoGasolina, 0.0)
        assertEquals(6.49, depois.configuracao.precoGasolina, 0.0)
        assertEquals(Combustivel.ETANOL, depois.configuracao.combustivel)
        assertEquals(3.10, depois.configuracao.limiteOtimaMin, 0.0)
        assertEquals(2.40, depois.configuracao.limiteBoaMin, 0.0)
    }

    @Test
    fun mapa_de_preferencias_volta_aos_mesmos_valores() {
        val original = ConfiguracaoUsuario.padrao().copy(
            marcaVeiculo = "Fiat",
            limiteRegularMin = 1.8,
        )
        val restaurado = original.paraPreferencias().paraConfiguracaoUsuario()
        assertEquals(original, restaurado)
    }

    @Test
    fun processador_usa_configuracao_persistida_no_calculo() {
        val store = MemoriaConfiguracaoStore()
        store.salvar(
            ConfiguracaoUsuario.padrao().copy(
                consumoGasolina = 10.0,
                precoGasolina = 5.0,
                limiteOtimaMin = 4.0,
                limiteBoaMin = 3.0,
                limiteRegularMin = 2.0,
                limiteRuimMax = 1.0,
            ),
        )
        val processor = RideNotificationProcessor(
            configuracaoProvider = { store.carregar() },
        )
        val evento = processor.processar(
            NotificationData(
                packageName = "com.ubercab.driver",
                title = "Nova viagem disponivel",
                text = "R$ 38,00 • 3,2 km ate o passageiro • 12,8 km viagem • 24 min",
            ),
        )
        val analise = (evento as RideNotificationEvent.CorridaRecebida).analise
        assertEquals(1.6, analise.combustivelEstimado!!, 0.01)
        assertEquals(8.0, analise.custoCombustivel!!, 0.01)
        assertEquals(Classificacao.REGULAR, analise.classificacao)
    }

    @Test
    fun motor_respeita_faixas_salvas_pelo_usuario() {
        val configuracao = ConfiguracaoUsuario.padrao().copy(
            limiteOtimaMin = 4.0,
            limiteBoaMin = 3.0,
            limiteRegularMin = 2.0,
            limiteRuimMax = 1.0,
        )
        val motor = MotorClassificacao.daConfiguracao(configuracao)
        assertEquals(Classificacao.REGULAR, motor.classificarPorValorKm(2.375))
        assertEquals(Classificacao.EXCELENTE, motor.classificarPorValorKm(4.1))
        assertTrue(configuracao.faixasDefinidas())
    }
}
