package br.com.gestordriver.core

enum class Combustivel {
    GASOLINA,
    ETANOL,
}

data class ConfiguracaoUsuario(
    val marca: String,
    val modelo: String,
    val versao: String,
    val ano: Int,
    val consumoGasolina: Double,
    val consumoEtanol: Double,
    val precoGasolina: Double,
    val precoEtanol: Double,
    val combustivel: Combustivel,
) {
    fun consumoAtivo(): Double = when (combustivel) {
        Combustivel.GASOLINA -> consumoGasolina
        Combustivel.ETANOL -> consumoEtanol
    }

    fun precoAtivo(): Double = when (combustivel) {
        Combustivel.GASOLINA -> precoGasolina
        Combustivel.ETANOL -> precoEtanol
    }

    companion object {
        fun padrao(): ConfiguracaoUsuario = ConfiguracaoUsuario(
            marca = "Toyota",
            modelo = "Corolla",
            versao = "XEi",
            ano = 2021,
            consumoGasolina = 12.5,
            consumoEtanol = 9.0,
            precoGasolina = 6.19,
            precoEtanol = 4.39,
            combustivel = Combustivel.GASOLINA,
        )
    }
}
