package br.com.gestordriver.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import br.com.gestordriver.data.ConfiguracaoStore
import br.com.gestordriver.data.MemoriaConfiguracaoStore
import br.com.gestordriver.model.AppNavegacao
import br.com.gestordriver.model.Combustivel
import br.com.gestordriver.model.ConfiguracaoUsuario

class ConfiguracoesViewModel(
    private val store: ConfiguracaoStore = MemoriaConfiguracaoStore(),
) : ViewModel() {

    var configuracao by mutableStateOf(store.carregar())
        private set

    fun atualizarMarca(valor: String) {
        configuracao = configuracao.copy(
            marcaVeiculo = valor,
        )
    }

    fun atualizarModelo(valor: String) {
        configuracao = configuracao.copy(
            modeloVeiculo = valor,
        )
    }

    fun atualizarVersao(valor: String) {
        configuracao = configuracao.copy(
            versaoVeiculo = valor,
        )
    }

    fun atualizarAno(valor: String) {
        configuracao = configuracao.copy(
            anoVeiculo = valor,
        )
    }

    fun atualizarConsumoGasolina(valor: Double) {
        configuracao = configuracao.copy(
            consumoGasolina = valor,
        )
    }

    fun atualizarConsumoEtanol(valor: Double) {
        configuracao = configuracao.copy(
            consumoEtanol = valor,
        )
    }

    fun selecionarCombustivel(
        combustivel: Combustivel,
    ) {
        configuracao = configuracao.copy(
            combustivel = combustivel,
        )
    }

    fun atualizarPrecoGasolina(valor: Double) {
        configuracao = configuracao.copy(
            precoGasolina = valor,
        )
    }

    fun atualizarPrecoEtanol(valor: Double) {
        configuracao = configuracao.copy(
            precoEtanol = valor,
        )
    }

    fun selecionarNavegacao(
        navegacao: AppNavegacao,
    ) {
        configuracao = configuracao.copy(
            navegacao = navegacao,
        )
    }

    fun atualizarLimiteRuimMin(valor: Double) {
        configuracao = configuracao.copy(limiteRuimMin = valor)
    }

    fun atualizarLimiteRuimMax(valor: Double) {
        configuracao = configuracao.copy(limiteRuimMax = valor)
    }

    fun atualizarLimiteRegularMin(valor: Double) {
        configuracao = configuracao.copy(limiteRegularMin = valor)
    }

    fun atualizarLimiteRegularMax(valor: Double) {
        configuracao = configuracao.copy(limiteRegularMax = valor)
    }

    fun atualizarLimiteBoaMin(valor: Double) {
        configuracao = configuracao.copy(limiteBoaMin = valor)
    }

    fun atualizarLimiteBoaMax(valor: Double) {
        configuracao = configuracao.copy(limiteBoaMax = valor)
    }

    fun atualizarLimiteOtimaMin(valor: Double) {
        configuracao = configuracao.copy(limiteOtimaMin = valor)
    }

    fun atualizarLimiteOtimaMax(valor: Double) {
        configuracao = configuracao.copy(limiteOtimaMax = valor)
    }

    fun salvar() {
        store.salvar(configuracao)
    }

    override fun onCleared() {
        store.salvar(configuracao)
        super.onCleared()
    }
}
