package br.com.gestordriver.data

import br.com.gestordriver.model.HistoricoItemPresentation
import java.util.Locale

interface HistoricoRepository {
    fun listar(): List<HistoricoItemPresentation>

    fun salvar(item: HistoricoItemPresentation)

    fun limpar()
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

    override fun limpar() {
        itens.clear()
    }
}

fun HistoricoItemPresentation.chaveHistorico(): String =
    listOf(
        plataforma.trim().lowercase(),
        "%.2f".format(Locale.US, valorTotal),
        "%.2f".format(Locale.US, kmAtePassageiro),
        "%.2f".format(Locale.US, kmViagem),
        tempoEstimado?.toString().orEmpty(),
        dataLista,
    ).joinToString("|")
