package br.com.gestordriver.core

import java.time.LocalDateTime

data class AnaliseCorrida(
    val corrida: Corrida,
    val valorTotal: Double,
    val kmAtePassageiro: Double,
    val kmViagem: Double,
    val tempoEstimado: Int?,
    val notaPassageiro: Double?,
    val plataforma: String?,
    val dataHora: LocalDateTime?,
    val kmTotal: Double,
    val valorPorKm: Double,
    val combustivelEstimado: Double?,
    val custoCombustivel: Double?,
    val classificacao: Classificacao,
    val corClassificacao: String,
)
