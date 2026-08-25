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
        aplicar(configuracao.copy(marcaVeiculo = valor))
    }

    fun atualizarModelo(valor: String) {
        aplicar(configuracao.copy(modeloVeiculo = valor))
    }

    fun atualizarVersao(valor: String) {
        aplicar(configuracao.copy(versaoVeiculo = valor))
    }

    fun atualizarAno(valor: String) {
        aplicar(configuracao.copy(anoVeiculo = valor))
    }

    fun atualizarConsumoGasolina(valor: Double) {
        aplicar(configuracao.copy(consumoGasolina = valor))
    }

    fun atualizarConsumoEtanol(valor: Double) {
        aplicar(configuracao.copy(consumoEtanol = valor))
    }

    fun selecionarCombustivel(
        combustivel: Combustivel,
    ) {
        aplicar(configuracao.copy(combustivel = combustivel))
    }

    fun atualizarPrecoGasolina(valor: Double) {
        aplicar(configuracao.copy(precoGasolina = valor))
    }

    fun atualizarPrecoEtanol(valor: Double) {
        aplicar(configuracao.copy(precoEtanol = valor))
    }

    fun selecionarNavegacao(
        navegacao: AppNavegacao,
    ) {
        aplicar(configuracao.copy(navegacao = navegacao))
    }

    fun atualizarLimiteRuimMin(valor: Double) {
        aplicar(configuracao.copy(limiteRuimMin = valor))
    }

    fun atualizarLimiteRuimMax(valor: Double) {
        aplicar(configuracao.copy(limiteRuimMax = valor))
    }

    fun atualizarLimiteRegularMin(valor: Double) {
        aplicar(configuracao.copy(limiteRegularMin = valor))
    }

    fun atualizarLimiteRegularMax(valor: Double) {
        aplicar(configuracao.copy(limiteRegularMax = valor))
    }

    fun atualizarLimiteBoaMin(valor: Double) {
        aplicar(configuracao.copy(limiteBoaMin = valor))
    }

    fun atualizarLimiteBoaMax(valor: Double) {
        aplicar(configuracao.copy(limiteBoaMax = valor))
    }

    fun atualizarLimiteOtimaMin(valor: Double) {
        aplicar(configuracao.copy(limiteOtimaMin = valor))
    }

    fun atualizarLimiteOtimaMax(valor: Double) {
        aplicar(configuracao.copy(limiteOtimaMax = valor))
    }

    fun salvar() {
        store.salvar(configuracao)
    }

    private fun aplicar(nova: ConfiguracaoUsuario) {
        configuracao = nova
        store.salvar(nova)
    }

    override fun onCleared() {
        store.salvar(configuracao)
        super.onCleared()
    }
}
