package br.com.gestordriver.core

enum class Combustivel {
    GASOLINA,
    ETANOL,
    ENERGIA,
}

enum class SeguroRecorrencia {
    MENSAL,
    ANUAL,
}

data class ConfiguracaoUsuario(
    val marca: String,
    val modelo: String,
    val versao: String,
    val ano: Int,
    val consumoGasolina: Double,
    val consumoEtanol: Double,
    val consumoEnergia: Double = 0.0,
    val precoGasolina: Double,
    val precoEtanol: Double,
    val precoEnergia: Double = 0.0,
    val combustivel: Combustivel,
    val oleoValor: Double = 0.0,
    val oleoKilometragem: Double = 0.0,
    val pneuDianteiroValor: Double = 0.0,
    val pneuDianteiroRodagem: Double = 0.0,
    val pneuTraseiroValor: Double = 0.0,
    val pneuTraseiroRodagem: Double = 0.0,
    val ipvaValor: Double = 0.0,
    val seguroValor: Double = 0.0,
    val seguroRecorrencia: SeguroRecorrencia = SeguroRecorrencia.ANUAL,
    val kmAnual: Double = 0.0,
) {
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

    companion object {
        fun padrao(): ConfiguracaoUsuario = ConfiguracaoUsuario(
            marca = "Toyota",
            modelo = "Corolla",
            versao = "XEi",
            ano = 2021,
            consumoGasolina = 12.5,
            consumoEtanol = 9.0,
            consumoEnergia = 6.0,
            precoGasolina = 6.19,
            precoEtanol = 4.39,
            precoEnergia = 0.85,
            combustivel = Combustivel.GASOLINA,
        )
    }
}
