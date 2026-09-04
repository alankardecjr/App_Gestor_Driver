package br.com.gestordriver.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import br.com.gestordriver.core.FaixasClassificacao
import br.com.gestordriver.core.TabelaIpvaPlaca
import br.com.gestordriver.data.ConfiguracaoStore
import br.com.gestordriver.data.ContaVinculo
import br.com.gestordriver.data.MemoriaConfiguracaoStore
import br.com.gestordriver.model.AppNavegacao
import br.com.gestordriver.model.Combustivel
import br.com.gestordriver.model.ConfiguracaoUsuario
import br.com.gestordriver.model.SeguroRecorrencia
import br.com.gestordriver.model.TipoContaVinculada
import br.com.gestordriver.model.TipoVeiculo

class ConfiguracoesViewModel(
    private val store: ConfiguracaoStore = MemoriaConfiguracaoStore(),
) : ViewModel() {

    var configuracao by mutableStateOf(store.carregar())
        private set

    fun atualizarTipoVeiculo(tipo: TipoVeiculo) {
        aplicar(configuracao.copy(tipoVeiculo = tipo))
    }

    fun atualizarMarcasDeslizantes(ruimMax: Double, boaMax: Double) {
        aplicar(FaixasClassificacao.aplicarMarcas(configuracao, ruimMax, boaMax))
    }

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
        val limpo = valor.filter { it.isDigit() }.takeLast(1)
        aplicar(
            configuracao.copy(
                finalPlaca = limpo,
                ipvaVencimento = TabelaIpvaPlaca.rotuloMesVencimento(limpo).takeIf { it != "—" }.orEmpty(),
            ),
        )
    }

    fun atualizarIpva(valor: String) {
        aplicar(configuracao.copy(ipvaVencimento = valor))
    }

    fun atualizarAbastecimentoValor(valor: Double) {
        aplicar(configuracao.copy(abastecimentoValor = valor))
    }

    fun atualizarAbastecimentoLitros(valor: Double) {
        aplicar(configuracao.copy(abastecimentoLitros = valor))
    }

    fun atualizarAbastecimentoKmInicial(valor: Double) {
        aplicar(configuracao.copy(abastecimentoKmInicial = valor))
    }

    fun atualizarAbastecimentoKmFinal(valor: Double) {
        aplicar(configuracao.copy(abastecimentoKmFinal = valor))
    }

    fun atualizarOleoValor(valor: Double) {
        aplicar(configuracao.copy(oleoValor = valor))
    }

    fun atualizarOleoKm(valor: Double) {
        aplicar(configuracao.copy(oleoKilometragem = valor))
    }

    fun atualizarOleoData(valor: String) {
        aplicar(configuracao.copy(oleoData = valor))
    }

    fun atualizarPneuDianteiroValor(valor: Double) {
        aplicar(configuracao.copy(pneuDianteiroValor = valor))
    }

    fun atualizarPneuDianteiroRodagem(valor: Double) {
        aplicar(configuracao.copy(pneuDianteiroRodagem = valor))
    }

    fun atualizarPneuDianteiroData(valor: String) {
        aplicar(configuracao.copy(pneuDianteiroData = valor))
    }

    fun atualizarPneuTraseiroValor(valor: Double) {
        aplicar(configuracao.copy(pneuTraseiroValor = valor))
    }

    fun atualizarPneuTraseiroRodagem(valor: Double) {
        aplicar(configuracao.copy(pneuTraseiroRodagem = valor))
    }

    fun atualizarPneuTraseiroData(valor: String) {
        aplicar(configuracao.copy(pneuTraseiroData = valor))
    }

    fun atualizarIpvaValor(valor: Double) {
        aplicar(configuracao.copy(ipvaValor = valor))
    }

    fun atualizarSeguroValor(valor: Double) {
        aplicar(configuracao.copy(seguroValor = valor))
    }

    fun atualizarSeguroData(valor: String) {
        aplicar(configuracao.copy(seguroData = valor))
    }

    fun atualizarSeguroRecorrencia(valor: SeguroRecorrencia) {
        aplicar(configuracao.copy(seguroRecorrencia = valor))
    }

    fun atualizarConsumoGasolina(valor: Double) {
        aplicar(configuracao.copy(consumoGasolina = valor))
    }

    fun atualizarConsumoEtanol(valor: Double) {
        aplicar(configuracao.copy(consumoEtanol = valor))
    }

    fun atualizarConsumoEnergia(valor: Double) {
        aplicar(configuracao.copy(consumoEnergia = valor))
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

    fun atualizarPrecoEnergia(valor: Double) {
        aplicar(configuracao.copy(precoEnergia = valor))
    }

    fun selecionarTema(tema: br.com.gestordriver.model.TemaApp) {
        aplicar(configuracao.copy(tema = tema))
    }

    fun selecionarNavegacao(
        navegacao: AppNavegacao,
    ) {
        aplicar(configuracao.copy(navegacao = navegacao))
    }

    fun conectarContaGoogle(email: String): Boolean {
        return persistirConta(TipoContaVinculada.GOOGLE, email)
    }

    fun conectarContaEmail(email: String): Boolean {
        if (!ContaVinculo.emailValido(email)) {
            return false
        }
        return persistirConta(TipoContaVinculada.EMAIL, email)
    }

    private fun persistirConta(tipo: TipoContaVinculada, email: String): Boolean {
        val persistida = ContaVinculo.aplicar(store.carregar(), tipo, email)
        store.salvar(persistida)
        aplicar(configuracao.copy(contaTipo = persistida.contaTipo, contaEmail = persistida.contaEmail))
        return true
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

    fun salvar(aplicarAbastecimento: Boolean = true) {
        val comAbastecimento = if (aplicarAbastecimento) {
            configuracao.aplicarCalculoAbastecimento()
        } else {
            configuracao
        }
        val normalizada = FaixasClassificacao.normalizar(comAbastecimento)
        store.salvar(normalizada)
        configuracao = normalizada
    }

    fun temCalculoAbastecimento(): Boolean {
        val cfg = configuracao
        return br.com.gestordriver.core.CalcularCombustivel.precoPorLitro(
            cfg.abastecimentoValor,
            cfg.abastecimentoLitros,
        ) != null || br.com.gestordriver.core.CalcularCombustivel.consumoKmPorLitro(
            cfg.abastecimentoKmInicial,
            cfg.abastecimentoKmFinal,
            cfg.abastecimentoLitros,
        ) != null
    }

    private fun aplicar(nova: ConfiguracaoUsuario) {
        configuracao = nova
    }

    fun cancelar() {
        configuracao = store.carregar()
    }
}
