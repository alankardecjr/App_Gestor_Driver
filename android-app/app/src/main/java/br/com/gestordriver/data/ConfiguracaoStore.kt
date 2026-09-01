package br.com.gestordriver.data

import br.com.gestordriver.model.AppNavegacao
import br.com.gestordriver.model.Combustivel
import br.com.gestordriver.model.ConfiguracaoUsuario
import br.com.gestordriver.model.TipoContaVinculada

interface ConfiguracaoStore {
    fun carregar(): ConfiguracaoUsuario

    fun salvar(configuracao: ConfiguracaoUsuario)
}

class MemoriaConfiguracaoStore(
    inicial: ConfiguracaoUsuario = ConfiguracaoUsuario.padrao(),
) : ConfiguracaoStore {

    private var atual = inicial

    override fun carregar(): ConfiguracaoUsuario = atual

    override fun salvar(configuracao: ConfiguracaoUsuario) {
        atual = configuracao
    }
}

fun ConfiguracaoUsuario.paraPreferencias(): Map<String, String> = mapOf(
    "marcaVeiculo" to marcaVeiculo,
    "modeloVeiculo" to modeloVeiculo,
    "versaoVeiculo" to versaoVeiculo,
    "anoVeiculo" to anoVeiculo,
    "finalPlaca" to finalPlaca,
    "consumoGasolina" to consumoGasolina.toString(),
    "consumoEtanol" to consumoEtanol.toString(),
    "combustivel" to combustivel.name,
    "precoGasolina" to precoGasolina.toString(),
    "precoEtanol" to precoEtanol.toString(),
    "oleoValor" to oleoValor.toString(),
    "oleoKilometragem" to oleoKilometragem.toString(),
    "oleoData" to oleoData,
    "pneuDianteiroValor" to pneuDianteiroValor.toString(),
    "pneuDianteiroRodagem" to pneuDianteiroRodagem.toString(),
    "pneuDianteiroData" to pneuDianteiroData,
    "pneuTraseiroValor" to pneuTraseiroValor.toString(),
    "pneuTraseiroRodagem" to pneuTraseiroRodagem.toString(),
    "pneuTraseiroData" to pneuTraseiroData,
    "abastecimentoValor" to abastecimentoValor.toString(),
    "abastecimentoLitros" to abastecimentoLitros.toString(),
    "abastecimentoKmInicial" to abastecimentoKmInicial.toString(),
    "abastecimentoKmFinal" to abastecimentoKmFinal.toString(),
    "ipvaVencimento" to ipvaVencimento,
    "navegacao" to navegacao.name,
    "contaTipo" to contaTipo.name,
    "contaEmail" to contaEmail,
    "limiteRuimMin" to limiteRuimMin.toString(),
    "limiteRuimMax" to limiteRuimMax.toString(),
    "limiteRegularMin" to limiteRegularMin.toString(),
    "limiteRegularMax" to limiteRegularMax.toString(),
    "limiteBoaMin" to limiteBoaMin.toString(),
    "limiteBoaMax" to limiteBoaMax.toString(),
    "limiteOtimaMin" to limiteOtimaMin.toString(),
    "limiteOtimaMax" to limiteOtimaMax.toString(),
)

fun Map<String, String>.paraConfiguracaoUsuario(): ConfiguracaoUsuario {
    val padrao = ConfiguracaoUsuario.padrao()
    return ConfiguracaoUsuario(
        marcaVeiculo = this["marcaVeiculo"] ?: padrao.marcaVeiculo,
        modeloVeiculo = this["modeloVeiculo"] ?: padrao.modeloVeiculo,
        versaoVeiculo = this["versaoVeiculo"] ?: padrao.versaoVeiculo,
        anoVeiculo = this["anoVeiculo"] ?: padrao.anoVeiculo,
        finalPlaca = this["finalPlaca"] ?: padrao.finalPlaca,
        consumoGasolina = this["consumoGasolina"]?.toDoubleOrNull() ?: padrao.consumoGasolina,
        consumoEtanol = this["consumoEtanol"]?.toDoubleOrNull() ?: padrao.consumoEtanol,
        combustivel = this["combustivel"]?.let {
            runCatching { Combustivel.valueOf(it) }.getOrDefault(padrao.combustivel)
        } ?: padrao.combustivel,
        precoGasolina = this["precoGasolina"]?.toDoubleOrNull() ?: padrao.precoGasolina,
        precoEtanol = this["precoEtanol"]?.toDoubleOrNull() ?: padrao.precoEtanol,
        oleoValor = this["oleoValor"]?.toDoubleOrNull() ?: padrao.oleoValor,
        oleoKilometragem = this["oleoKilometragem"]?.toDoubleOrNull() ?: padrao.oleoKilometragem,
        oleoData = this["oleoData"] ?: padrao.oleoData,
        pneuDianteiroValor = this["pneuDianteiroValor"]?.toDoubleOrNull() ?: padrao.pneuDianteiroValor,
        pneuDianteiroRodagem = this["pneuDianteiroRodagem"]?.toDoubleOrNull() ?: padrao.pneuDianteiroRodagem,
        pneuDianteiroData = this["pneuDianteiroData"] ?: padrao.pneuDianteiroData,
        pneuTraseiroValor = this["pneuTraseiroValor"]?.toDoubleOrNull() ?: padrao.pneuTraseiroValor,
        pneuTraseiroRodagem = this["pneuTraseiroRodagem"]?.toDoubleOrNull() ?: padrao.pneuTraseiroRodagem,
        pneuTraseiroData = this["pneuTraseiroData"] ?: padrao.pneuTraseiroData,
        abastecimentoValor = this["abastecimentoValor"]?.toDoubleOrNull() ?: padrao.abastecimentoValor,
        abastecimentoLitros = this["abastecimentoLitros"]?.toDoubleOrNull() ?: padrao.abastecimentoLitros,
        abastecimentoKmInicial = this["abastecimentoKmInicial"]?.toDoubleOrNull() ?: padrao.abastecimentoKmInicial,
        abastecimentoKmFinal = this["abastecimentoKmFinal"]?.toDoubleOrNull() ?: padrao.abastecimentoKmFinal,
        ipvaVencimento = this["ipvaVencimento"] ?: padrao.ipvaVencimento,
        navegacao = this["navegacao"]?.let {
            runCatching { AppNavegacao.valueOf(it) }.getOrDefault(padrao.navegacao)
        } ?: padrao.navegacao,
        contaTipo = this["contaTipo"]?.let {
            runCatching { TipoContaVinculada.valueOf(it) }.getOrDefault(padrao.contaTipo)
        } ?: padrao.contaTipo,
        contaEmail = this["contaEmail"] ?: padrao.contaEmail,
        limiteRuimMin = this["limiteRuimMin"]?.toDoubleOrNull() ?: padrao.limiteRuimMin,
        limiteRuimMax = this["limiteRuimMax"]?.toDoubleOrNull() ?: padrao.limiteRuimMax,
        limiteRegularMin = this["limiteRegularMin"]?.toDoubleOrNull() ?: padrao.limiteRegularMin,
        limiteRegularMax = this["limiteRegularMax"]?.toDoubleOrNull() ?: padrao.limiteRegularMax,
        limiteBoaMin = this["limiteBoaMin"]?.toDoubleOrNull() ?: padrao.limiteBoaMin,
        limiteBoaMax = this["limiteBoaMax"]?.toDoubleOrNull() ?: padrao.limiteBoaMax,
        limiteOtimaMin = this["limiteOtimaMin"]?.toDoubleOrNull() ?: padrao.limiteOtimaMin,
        limiteOtimaMax = this["limiteOtimaMax"]?.toDoubleOrNull() ?: padrao.limiteOtimaMax,
    )
}

fun ConfiguracaoUsuario.paraMotor(): br.com.gestordriver.core.ConfiguracaoUsuario {
    return br.com.gestordriver.core.ConfiguracaoUsuario(
        marca = marcaVeiculo,
        modelo = modeloVeiculo,
        versao = versaoVeiculo,
        ano = anoVeiculo.toIntOrNull() ?: 0,
        consumoGasolina = consumoGasolina,
        consumoEtanol = consumoEtanol,
        precoGasolina = precoGasolina,
        precoEtanol = precoEtanol,
        combustivel = when (combustivel) {
            Combustivel.GASOLINA -> br.com.gestordriver.core.Combustivel.GASOLINA
            Combustivel.ETANOL -> br.com.gestordriver.core.Combustivel.ETANOL
        },
    )
}
