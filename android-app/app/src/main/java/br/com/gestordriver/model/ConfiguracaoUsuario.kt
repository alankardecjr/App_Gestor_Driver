package br.com.gestordriver.model

data class ConfiguracaoUsuario(
    val marcaVeiculo: String = "",
    val modeloVeiculo: String = "",
    val versaoVeiculo: String = "",
    val anoVeiculo: String = "",

    val consumoGasolina: Double = 0.0,
    val consumoEtanol: Double = 0.0,

    val combustivel: Combustivel = Combustivel.GASOLINA,

    val precoGasolina: Double = 0.0,
    val precoEtanol: Double = 0.0,

    val navegacao: AppNavegacao = AppNavegacao.GOOGLE_MAPS,

    val limiteRuimMin: Double = 0.0,
    val limiteRuimMax: Double = 0.0,

    val limiteRegularMin: Double = 0.0,
    val limiteRegularMax: Double = 0.0,

    val limiteBoaMin: Double = 0.0,
    val limiteBoaMax: Double = 0.0,

    val limiteOtimaMin: Double = 0.0,
    val limiteOtimaMax: Double = 0.0,
)

enum class Combustivel {
    GASOLINA,
    ETANOL,
}

enum class AppNavegacao {
    GOOGLE_MAPS,
    WAZE,
}
