package br.com.gestordriver.model

import br.com.gestordriver.core.AnaliseCorrida
import br.com.gestordriver.core.Classificacao
import br.com.gestordriver.core.Corrida
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class HistoricoItemPresentation(
    val dataHora: String,
    val plataforma: String,
    val valorPorKm: Double,
    val valorTotal: Double,
    val kmTotal: Double,
    val tempoEstimado: Int?,
    val notaPassageiro: Double?,
    val classificacao: ClassificacaoVisual,
    val corClassificacao: String,
    val kmAtePassageiro: Double,
    val kmViagem: Double,
    val combustivelEstimado: Double?,
    val custoCombustivel: Double?,
    val dataHoraRegistro: LocalDateTime? = null,
    val enderecoEmbarque: String? = null,
    val enderecoDestino: String? = null,
) {
    val data: String
        get() = dataHora

    val linhaHorizontal: String
        get() = HistoricoFormatacao.linhaHorizontal(
            valorPorKm = valorPorKm,
            valorTotal = valorTotal,
            kmTotal = kmTotal,
            tempoEstimado = tempoEstimado,
            notaPassageiro = notaPassageiro,
        )

    fun paraAnalise(): AnaliseCorrida = AnaliseCorrida(
        corrida = Corrida(
            valorTotal = valorTotal,
            kmAtePassageiro = kmAtePassageiro,
            kmViagem = kmViagem,
            tempoEstimado = tempoEstimado,
            enderecoEmbarque = enderecoEmbarque,
            enderecoDestino = enderecoDestino,
        ),
        valorTotal = valorTotal,
        kmAtePassageiro = kmAtePassageiro,
        kmViagem = kmViagem,
        tempoEstimado = tempoEstimado,
        notaPassageiro = notaPassageiro,
        plataforma = plataforma,
        dataHora = dataHoraRegistro,
        kmTotal = kmTotal,
        valorPorKm = valorPorKm,
        combustivelEstimado = combustivelEstimado,
        custoCombustivel = custoCombustivel,
        classificacao = Classificacao.valueOf(classificacao.name),
        corClassificacao = corClassificacao,
    )

    companion object {
        private val formatterData = DateTimeFormatter.ofPattern("dd/MM HH:mm")

        fun de(analise: AnaliseCorrida): HistoricoItemPresentation = HistoricoItemPresentation(
            dataHora = analise.dataHora?.format(formatterData) ?: "—",
            plataforma = analise.plataforma ?: "—",
            valorPorKm = analise.valorPorKm,
            valorTotal = analise.valorTotal,
            kmTotal = analise.kmTotal,
            tempoEstimado = analise.tempoEstimado,
            notaPassageiro = analise.notaPassageiro,
            classificacao = ClassificacaoVisual.from(analise.classificacao),
            corClassificacao = analise.corClassificacao,
            kmAtePassageiro = analise.kmAtePassageiro,
            kmViagem = analise.kmViagem,
            combustivelEstimado = analise.combustivelEstimado,
            custoCombustivel = analise.custoCombustivel,
            dataHoraRegistro = analise.dataHora,
            enderecoEmbarque = analise.corrida.enderecoEmbarque,
            enderecoDestino = analise.corrida.enderecoDestino,
        )
    }
}

object HistoricoFormatacao {
    fun linhaHorizontal(
        valorPorKm: Double,
        valorTotal: Double,
        kmTotal: Double,
        tempoEstimado: Int?,
        notaPassageiro: Double?,
    ): String {
        val nota = notaPassageiro?.let { formatDecimal(it, 2) } ?: "—"
        val tempo = tempoEstimado?.toString() ?: "—"

        return buildString {
            append(formatDecimal(valorPorKm, 2))
            append(" │ R$ ")
            append(formatDecimal(valorTotal, 2))
            append(" │ ")
            append(formatKm(kmTotal))
            append(" │ ")
            append(tempo)
            append(" min │ ")
            append(nota)
        }
    }

    private fun formatDecimal(valor: Double, casas: Int): String =
        "%.${casas}f".format(Locale.US, valor).replace(".", ",")

    private fun formatKm(valor: Double): String {
        val texto = if (valor % 1.0 == 0.0) {
            "%.0f".format(Locale.US, valor)
        } else {
            "%.1f".format(Locale.US, valor)
        }
        return "$texto km"
    }
}
