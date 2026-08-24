package br.com.gestordriver.data

import br.com.gestordriver.model.HistoricoItemPresentation

interface HistoricoRepository {
    fun listar(): List<HistoricoItemPresentation>

    fun salvar(item: HistoricoItemPresentation)
}

class MemoriaHistoricoRepository(
    inicial: List<HistoricoItemPresentation> = emptyList(),
) : HistoricoRepository {

    private val itens = inicial.toMutableList()

    override fun listar(): List<HistoricoItemPresentation> = itens.toList()

    override fun salvar(item: HistoricoItemPresentation) {
        val chave = item.chaveHistorico()
        if (itens.any { it.chaveHistorico() == chave }) {
            return
        }
        itens += item
    }
}

fun HistoricoItemPresentation.chaveHistorico(): String =
    listOf(
        plataforma,
        valorTotal.toString(),
        kmAtePassageiro.toString(),
        kmViagem.toString(),
        tempoEstimado?.toString().orEmpty(),
        dataHora,
    ).joinToString("|")
