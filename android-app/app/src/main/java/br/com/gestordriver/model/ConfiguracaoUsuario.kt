package br.com.gestordriver.model

data class ConfiguracaoUsuario(
    val marcaVeiculo: String = "",
    val modeloVeiculo: String = "",
    val versaoVeiculo: String = "",
    val anoVeiculo: String = "",
    val finalPlaca: String = "",

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
) {
    fun faixasDefinidas(): Boolean {
        return limiteOtimaMin > 0.0 ||
            limiteBoaMin > 0.0 ||
            limiteRegularMin > 0.0 ||
            limiteRuimMax > 0.0
    }

    companion object {
        fun padrao(): ConfiguracaoUsuario = ConfiguracaoUsuario(
            marcaVeiculo = "Toyota",
            modeloVeiculo = "Corolla",
            versaoVeiculo = "XEi",
            anoVeiculo = "2021",
            consumoGasolina = 12.5,
            consumoEtanol = 9.0,
            combustivel = Combustivel.GASOLINA,
            precoGasolina = 6.19,
            precoEtanol = 4.39,
            navegacao = AppNavegacao.GOOGLE_MAPS,
            limiteRuimMin = 0.0,
            limiteRuimMax = 1.19,
            limiteRegularMin = 1.20,
            limiteRegularMax = 1.59,
            limiteBoaMin = 1.60,
            limiteBoaMax = 1.99,
            limiteOtimaMin = 2.00,
            limiteOtimaMax = 99.0,
        )
    }
}

enum class Combustivel {
    GASOLINA,
    ETANOL,
}

enum class AppNavegacao {
    GOOGLE_MAPS,
    WAZE,
}
