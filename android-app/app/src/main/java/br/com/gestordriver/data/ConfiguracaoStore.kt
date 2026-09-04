package br.com.gestordriver.data

import br.com.gestordriver.model.AppNavegacao
import br.com.gestordriver.model.Combustivel
import br.com.gestordriver.model.ConfiguracaoUsuario
import br.com.gestordriver.model.SeguroRecorrencia
import br.com.gestordriver.model.TipoContaVinculada
import br.com.gestordriver.model.TipoVeiculo

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
    "tipoVeiculo" to tipoVeiculo.name,
    "marcaVeiculo" to marcaVeiculo,
    "modeloVeiculo" to modeloVeiculo,
    "versaoVeiculo" to versaoVeiculo,
    "anoVeiculo" to anoVeiculo,
    "finalPlaca" to finalPlaca,
    "consumoGasolina" to consumoGasolina.toString(),
    "consumoEtanol" to consumoEtanol.toString(),
    "consumoEnergia" to consumoEnergia.toString(),
    "combustivel" to combustivel.name,
    "precoGasolina" to precoGasolina.toString(),
    "precoEtanol" to precoEtanol.toString(),
    "precoEnergia" to precoEnergia.toString(),
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
    "ipvaValor" to ipvaValor.toString(),
    "seguroValor" to seguroValor.toString(),
    "seguroData" to seguroData,
    "seguroRecorrencia" to seguroRecorrencia.name,
    "kmAnual" to kmAnual.toString(),
    "navegacao" to navegacao.name,
    "tema" to tema.name,
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
        tipoVeiculo = this["tipoVeiculo"]?.let {
            runCatching { TipoVeiculo.valueOf(it) }.getOrDefault(padrao.tipoVeiculo)
        } ?: padrao.tipoVeiculo,
        marcaVeiculo = this["marcaVeiculo"] ?: padrao.marcaVeiculo,
        modeloVeiculo = this["modeloVeiculo"] ?: padrao.modeloVeiculo,
        versaoVeiculo = this["versaoVeiculo"] ?: padrao.versaoVeiculo,
        anoVeiculo = this["anoVeiculo"] ?: padrao.anoVeiculo,
        finalPlaca = this["finalPlaca"] ?: padrao.finalPlaca,
        consumoGasolina = this["consumoGasolina"]?.toDoubleOrNull() ?: padrao.consumoGasolina,
        consumoEtanol = this["consumoEtanol"]?.toDoubleOrNull() ?: padrao.consumoEtanol,
        consumoEnergia = this["consumoEnergia"]?.toDoubleOrNull() ?: padrao.consumoEnergia,
        combustivel = this["combustivel"]?.let {
            runCatching { Combustivel.valueOf(it) }.getOrDefault(padrao.combustivel)
        } ?: padrao.combustivel,
        precoGasolina = this["precoGasolina"]?.toDoubleOrNull() ?: padrao.precoGasolina,
        precoEtanol = this["precoEtanol"]?.toDoubleOrNull() ?: padrao.precoEtanol,
        precoEnergia = this["precoEnergia"]?.toDoubleOrNull() ?: padrao.precoEnergia,
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
        ipvaValor = this["ipvaValor"]?.toDoubleOrNull() ?: padrao.ipvaValor,
        seguroValor = this["seguroValor"]?.toDoubleOrNull() ?: padrao.seguroValor,
        seguroData = this["seguroData"] ?: padrao.seguroData,
        seguroRecorrencia = this["seguroRecorrencia"]?.let {
            runCatching { SeguroRecorrencia.valueOf(it) }.getOrDefault(padrao.seguroRecorrencia)
        } ?: padrao.seguroRecorrencia,
        kmAnual = this["kmAnual"]?.toDoubleOrNull() ?: padrao.kmAnual,
        navegacao = this["navegacao"]?.let {
            runCatching { AppNavegacao.valueOf(it) }.getOrDefault(padrao.navegacao)
        } ?: padrao.navegacao,
        tema = this["tema"]?.let {
            runCatching { br.com.gestordriver.model.TemaApp.valueOf(it) }.getOrDefault(padrao.tema)
        } ?: padrao.tema,
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
        consumoEnergia = consumoEnergia,
        precoGasolina = precoGasolina,
        precoEtanol = precoEtanol,
        precoEnergia = precoEnergia,
        combustivel = when (combustivel) {
            Combustivel.GASOLINA -> br.com.gestordriver.core.Combustivel.GASOLINA
            Combustivel.ETANOL -> br.com.gestordriver.core.Combustivel.ETANOL
            Combustivel.ENERGIA -> br.com.gestordriver.core.Combustivel.ENERGIA
        },
        oleoValor = oleoValor,
        oleoKilometragem = oleoKilometragem,
        pneuDianteiroValor = pneuDianteiroValor,
        pneuDianteiroRodagem = pneuDianteiroRodagem,
        pneuTraseiroValor = pneuTraseiroValor,
        pneuTraseiroRodagem = pneuTraseiroRodagem,
        ipvaValor = ipvaValor,
        seguroValor = seguroValor,
        seguroRecorrencia = when (seguroRecorrencia) {
            SeguroRecorrencia.MENSAL -> br.com.gestordriver.core.SeguroRecorrencia.MENSAL
            SeguroRecorrencia.ANUAL -> br.com.gestordriver.core.SeguroRecorrencia.ANUAL
        },
        kmAnual = kmAnual,
    )
}

