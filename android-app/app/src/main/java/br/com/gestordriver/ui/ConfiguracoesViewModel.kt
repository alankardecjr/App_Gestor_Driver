package br.com.gestordriver.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import br.com.gestordriver.core.FaixasClassificacao
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

    fun atualizarFinalPlaca(valor: String) {
        aplicar(configuracao.copy(finalPlaca = valor))
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
        aplicar(FaixasClassificacao.aplicar(configuracao, FaixasClassificacao.Campo.RUIM_MIN, valor))
    }

    fun atualizarLimiteRuimMax(valor: Double) {
        aplicar(FaixasClassificacao.aplicar(configuracao, FaixasClassificacao.Campo.RUIM_MAX, valor))
    }

    fun atualizarLimiteRegularMin(valor: Double) {
        aplicar(FaixasClassificacao.aplicar(configuracao, FaixasClassificacao.Campo.REGULAR_MIN, valor))
    }

    fun atualizarLimiteRegularMax(valor: Double) {
        aplicar(FaixasClassificacao.aplicar(configuracao, FaixasClassificacao.Campo.REGULAR_MAX, valor))
    }

    fun atualizarLimiteBoaMin(valor: Double) {
        aplicar(FaixasClassificacao.aplicar(configuracao, FaixasClassificacao.Campo.BOA_MIN, valor))
    }

    fun atualizarLimiteBoaMax(valor: Double) {
        aplicar(FaixasClassificacao.aplicar(configuracao, FaixasClassificacao.Campo.BOA_MAX, valor))
    }

    fun atualizarLimiteOtimaMin(valor: Double) {
        aplicar(FaixasClassificacao.aplicar(configuracao, FaixasClassificacao.Campo.OTIMA_MIN, valor))
    }

    fun atualizarLimiteOtimaMax(valor: Double) {
        aplicar(FaixasClassificacao.aplicar(configuracao, FaixasClassificacao.Campo.OTIMA_MAX, valor))
    }

    fun salvar() {
        val normalizada = FaixasClassificacao.normalizar(configuracao)
        store.salvar(normalizada)
        configuracao = normalizada
    }

    private fun aplicar(nova: ConfiguracaoUsuario) {
        configuracao = nova
    }

    fun cancelar() {
        configuracao = store.carregar()
    }
}
