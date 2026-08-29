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

    val dataLista: String
        get() = dataHora.split(" ").getOrElse(0) { "—" }

    val horaLista: String
        get() = dataHora.split(" ").getOrElse(1) { "—" }

    val linhaHorizontal: String
        get() = HistoricoFormatacao.linhaHorizontal(
            valorPorKm = valorPorKm,
            valorTotal = valorTotal,
            kmTotal = kmTotal,
            tempoEstimado = tempoEstimado,
            notaPassageiro = notaPassageiro,
        )

    val linhaHistorico: String
        get() = HistoricoFormatacao.linhaHistorico(
            data = dataLista,
            hora = horaLista,
            valorPorKm = valorPorKm,
            valorTotal = valorTotal,
            kmTotal = kmTotal,
            tempoEstimado = tempoEstimado,
            notaPassageiro = notaPassageiro,
        )

    fun pertenceAba(aba: String): Boolean {
        return when (aba.lowercase()) {
            "uber" -> plataforma.contains("Uber", ignoreCase = true)
            "99" -> plataforma.contains("99")
            else -> plataforma.contains("inDrive", ignoreCase = true) ||
                plataforma.contains("indrive", ignoreCase = true)
        }
    }

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
            append(" │ ")
            append(formatDecimal(valorTotal, 2))
            append(" │ ")
            append(formatKm(kmTotal))
            append(" │ ")
            append(tempo)
            append(" │ ")
            append(nota)
        }
    }

    fun linhaHistorico(
        data: String,
        hora: String,
        valorPorKm: Double,
        valorTotal: Double,
        kmTotal: Double,
        tempoEstimado: Int?,
        notaPassageiro: Double?,
    ): String {
        return "$data │ $hora │ ${linhaHorizontal(
            valorPorKm,
            valorTotal,
            kmTotal,
            tempoEstimado,
            notaPassageiro,
        )}"
    }

    private fun formatDecimal(valor: Double, casas: Int): String =
        "%.${casas}f".format(Locale.US, valor).replace(".", ",")

    private fun formatKm(valor: Double): String =
        "%.1f".format(Locale.US, valor).replace(".", ",") + " KM"
}
