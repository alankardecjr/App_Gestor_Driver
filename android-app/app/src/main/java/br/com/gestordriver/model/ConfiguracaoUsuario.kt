package br.com.gestordriver.model

import br.com.gestordriver.core.CalcularCombustivel

data class ConfiguracaoUsuario(
    val tipoVeiculo: TipoVeiculo = TipoVeiculo.CARRO,
    val marcaVeiculo: String = "",
    val modeloVeiculo: String = "",
    val versaoVeiculo: String = "",
    val anoVeiculo: String = "",
    val finalPlaca: String = "",

    val consumoGasolina: Double = 0.0,
    val consumoEtanol: Double = 0.0,
    val consumoEnergia: Double = 0.0,

    val combustivel: Combustivel = Combustivel.GASOLINA,

    val precoGasolina: Double = 0.0,
    val precoEtanol: Double = 0.0,
    val precoEnergia: Double = 0.0,

    val oleoValor: Double = 0.0,
    val oleoKilometragem: Double = 0.0,
    val oleoData: String = "",
    val pneuDianteiroValor: Double = 0.0,
    val pneuDianteiroRodagem: Double = 0.0,
    val pneuDianteiroData: String = "",
    val pneuTraseiroValor: Double = 0.0,
    val pneuTraseiroRodagem: Double = 0.0,
    val pneuTraseiroData: String = "",
    val abastecimentoValor: Double = 0.0,
    val abastecimentoLitros: Double = 0.0,
    val abastecimentoKmInicial: Double = 0.0,
    val abastecimentoKmFinal: Double = 0.0,
    val ipvaVencimento: String = "",
    val ipvaValor: Double = 0.0,
    val seguroValor: Double = 0.0,
    val seguroData: String = "",
    val seguroRecorrencia: SeguroRecorrencia = SeguroRecorrencia.ANUAL,
    val kmAnual: Double = 0.0,

    val navegacao: AppNavegacao = AppNavegacao.GOOGLE_MAPS,
    val tema: TemaApp = TemaApp.CELULAR,

    val contaTipo: TipoContaVinculada = TipoContaVinculada.NENHUMA,
    val contaEmail: String = "",

    val limiteRuimMin: Double = 0.0,
    val limiteRuimMax: Double = 0.0,

    val limiteRegularMin: Double = 0.0,
    val limiteRegularMax: Double = 0.0,

    val limiteBoaMin: Double = 0.0,
    val limiteBoaMax: Double = 0.0,

    val limiteOtimaMin: Double = 0.0,
    val limiteOtimaMax: Double = 0.0,
) {
    fun faixasDefinidas(): Boolean {
        return limiteOtimaMin > 0.0 ||
            limiteBoaMin > 0.0 ||
            limiteRuimMax > 0.0
    }

    fun consumoAtivo(): Double = when (combustivel) {
        Combustivel.GASOLINA -> consumoGasolina
        Combustivel.ETANOL -> consumoEtanol
        Combustivel.ENERGIA -> consumoEnergia
    }

    fun precoAtivo(): Double = when (combustivel) {
        Combustivel.GASOLINA -> precoGasolina
        Combustivel.ETANOL -> precoEtanol
        Combustivel.ENERGIA -> precoEnergia
    }

    fun calculadoraCombustivelPronta(): Boolean =
        consumoAtivo() > 0.0 && precoAtivo() > 0.0

    fun aplicarCalculoAbastecimento(): ConfiguracaoUsuario {
        val preco = CalcularCombustivel.precoPorLitro(abastecimentoValor, abastecimentoLitros)
        val consumo = CalcularCombustivel.consumoKmPorLitro(
            abastecimentoKmInicial,
            abastecimentoKmFinal,
            abastecimentoLitros,
        )
        return when (combustivel) {
            Combustivel.GASOLINA -> copy(
                precoGasolina = preco ?: precoGasolina,
                consumoGasolina = consumo ?: consumoGasolina,
            )
            Combustivel.ETANOL -> copy(
                precoEtanol = preco ?: precoEtanol,
                consumoEtanol = consumo ?: consumoEtanol,
            )
            Combustivel.ENERGIA -> this
        }
    }

    companion object {
        fun padrao(): ConfiguracaoUsuario = ConfiguracaoUsuario(
            tipoVeiculo = TipoVeiculo.CARRO,
            marcaVeiculo = "Toyota",
            modeloVeiculo = "Corolla",
            versaoVeiculo = "XEi",
            anoVeiculo = "2021",
            consumoGasolina = 12.5,
            consumoEtanol = 9.0,
            consumoEnergia = 6.0,
            combustivel = Combustivel.GASOLINA,
            precoGasolina = 6.19,
            precoEtanol = 4.39,
            precoEnergia = 0.85,
            navegacao = AppNavegacao.GOOGLE_MAPS,
            limiteRuimMin = 0.0,
            limiteRuimMax = 1.59,
            limiteRegularMin = 1.60,
            limiteRegularMax = 1.99,
            limiteBoaMin = 1.60,
            limiteBoaMax = 1.99,
            limiteOtimaMin = 2.00,
            limiteOtimaMax = 99.0,
        )
    }
}

enum class TipoVeiculo {
    CARRO,
    MOTO,
}

enum class Combustivel {
    GASOLINA,
    ETANOL,
    ENERGIA,
}

enum class SeguroRecorrencia {
    MENSAL,
    ANUAL,
}

enum class TemaApp {
    CELULAR,
    ESCURO,
    CLARO,
}

enum class AppNavegacao {
    GOOGLE_MAPS,
    WAZE,
}

enum class TipoContaVinculada {
    NENHUMA,
    GOOGLE,
    EMAIL,
}
