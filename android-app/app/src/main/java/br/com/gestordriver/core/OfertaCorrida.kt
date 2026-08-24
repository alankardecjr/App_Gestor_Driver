package br.com.gestordriver.core

fun AnaliseCorrida.chaveOferta(): String =
    listOf(
        plataforma.orEmpty(),
        valorTotal.toString(),
        kmAtePassageiro.toString(),
        kmViagem.toString(),
        tempoEstimado?.toString().orEmpty(),
    ).joinToString("|")
