package br.com.gestordriver.presentation

import br.com.gestordriver.core.AnaliseCorrida
import br.com.gestordriver.core.CalculadoraCorrida
import br.com.gestordriver.core.ConfiguracaoUsuario
import br.com.gestordriver.core.Corrida
import br.com.gestordriver.model.CampoApresentacao
import br.com.gestordriver.model.ClassificacaoVisual
import br.com.gestordriver.model.ControlePlano
import br.com.gestordriver.model.CorridaPresentation
import br.com.gestordriver.model.HistoricoItemPresentation
import br.com.gestordriver.model.ModoApresentacao
import br.com.gestordriver.model.PlanoAcesso
import br.com.gestordriver.model.RecursosPlano
import br.com.gestordriver.ui.AppState
import br.com.gestordriver.ui.EstadoInterfaceSalvo

object PresentationBuilder {
    private val calculadora = CalculadoraCorrida(configuracaoUsuario = ConfiguracaoUsuario.padrao())
    private val controlePlano = ControlePlano()

    private val corridaDemonstracao = Corrida(
        valorTotal = 38.10,
        kmAtePassageiro = 3.21,
        kmViagem = 12.82,
        tempoEstimado = 24,
    )

    private val historicoDemonstracao: List<HistoricoItemPresentation> by lazy {
        val uber = calculadora.calcular(
            corrida = Corrida(
                valorTotal = 38.10,
                kmAtePassageiro = 3.21,
                kmViagem = 12.82,
                tempoEstimado = 24,
            ),
            plataforma = "Uber",
            notaPassageiro = 4.98,
        ).let { analise ->
            HistoricoItemPresentation.de(analise).copy(
                dataHora = "03/08 12:00",
            )
        }

        val noveNove = calculadora.calcular(
            corrida = Corrida(
                valorTotal = 22.50,
                kmAtePassageiro = 1.5,
                kmViagem = 7.0,
                tempoEstimado = 18,
            ),
            plataforma = "99",
        ).let { analise ->
            HistoricoItemPresentation.de(analise).copy(
                dataHora = "03/08 11:00",
            )
        }

        listOf(uber, noveNove)
    }

    fun criarEstadoInicial(plano: PlanoAcesso = PlanoAcesso.BETA): AppState {
        val analise = calculadora.calcular(
            corrida = corridaDemonstracao,
            plataforma = "Uber",
            notaPassageiro = 4.98,
        )
        return criarEstado(
            analise = analise,
            plano = plano,
            historico = historicoDemonstracao,
        )
    }

    fun criarEstado(
        analise: AnaliseCorrida,
        plano: PlanoAcesso,
        historico: List<HistoricoItemPresentation> = emptyList(),
        historicoSelecionado: HistoricoItemPresentation? = null,
        modo: ModoApresentacao = ModoApresentacao.COMPACTA,
        historicoVisivel: Boolean = false,
        configuracoesVisivel: Boolean = false,
        interfaceOculta: Boolean = false,
        overlayAtivo: Boolean = true,
        notificacaoDisponivel: Boolean = true,
        seloFlutuante: Boolean = false,
        monitorando: Boolean = true,
        confirmacaoFecharVisivel: Boolean = false,
        seloOffsetX: Float = 0f,
        seloOffsetY: Float = 0f,
        estadoSalvo: EstadoInterfaceSalvo? = null,
    ): AppState {
        val recursos = controlePlano.aplicar(analise, plano)
        val corrida = montarCorridaPresentation(analise, plano, recursos, modo)

        return AppState(
            corrida = corrida,
            analiseAtual = analise,
            plano = plano,
            historico = historico,
            historicoSelecionado = historicoSelecionado,
            historicoVisivel = historicoVisivel,
            configuracoesVisivel = configuracoesVisivel,
            interfaceOculta = interfaceOculta,
            overlayAtivo = overlayAtivo,
            notificacaoDisponivel = notificacaoDisponivel,
            seloFlutuante = seloFlutuante,
            monitorando = monitorando,
            confirmacaoFecharVisivel = confirmacaoFecharVisivel,
            seloOffsetX = seloOffsetX,
            seloOffsetY = seloOffsetY,
            estadoSalvo = estadoSalvo,
        )
    }

    fun historicoDe(analise: AnaliseCorrida): HistoricoItemPresentation =
        HistoricoItemPresentation.de(analise)

    private fun montarCorridaPresentation(
        analise: AnaliseCorrida,
        plano: PlanoAcesso,
        recursos: RecursosPlano,
        modo: ModoApresentacao,
    ): CorridaPresentation {
        val nota = analise.notaPassageiro

        val camposCompactos = listOf(
            CampoApresentacao(
                id = "valor_por_km",
                titulo = "R$/KM",
                valor = if (recursos.exibeValorPorKm) {
                    formatDecimal(analise.valorPorKm, 2)
                } else {
                    "🔒"
                },
                disponivel = recursos.exibeValorPorKm,
                destaque = true,
            ),
            CampoApresentacao(
                id = "valor_total",
                titulo = "R$/TOTAL",
                valor = formatMoney(analise.valorTotal),
            ),
            CampoApresentacao(
                id = "km_total",
                titulo = "KM/TOTAL",
                valor = formatKm(analise.kmTotal),
            ),
            CampoApresentacao(
                id = "tempo_estimado",
                titulo = "TEMPO",
                valor = analise.corrida.tempoEstimado?.let { "$it min" } ?: "—",
            ),
            CampoApresentacao(
                id = "nota_passageiro",
                titulo = "NOTA",
                valor = nota?.let { "${formatDecimal(it, 2)} ⭐" } ?: "—",
            ),
        )

        val camposDetalhes = listOf(
            CampoApresentacao(
                id = "km_ate_passageiro",
                titulo = "Até o Passageiro",
                valor = formatKm(analise.kmAtePassageiro),
            ),
            CampoApresentacao(
                id = "km_viagem",
                titulo = "Até o destino",
                valor = formatKm(analise.kmViagem),
            ),
            CampoApresentacao(
                id = "combustivel_estimado",
                titulo = "Combustível estimado",
                valor = if (recursos.exibeCombustivelEstimado) {
                    analise.combustivelEstimado?.let { formatLiters(it) } ?: "—"
                } else {
                    "🔒"
                },
                disponivel = recursos.exibeCombustivelEstimado,
            ),
            CampoApresentacao(
                id = "custo_combustivel",
                titulo = "Gasto estimado",
                valor = if (recursos.exibeCustoCombustivel) {
                    analise.custoCombustivel?.let { formatMoney(it) } ?: "—"
                } else {
                    "🔒"
                },
                disponivel = recursos.exibeCustoCombustivel,
            ),
            CampoApresentacao(
                id = "recursos_avancados",
                titulo = "Recursos avançados",
                valor = if (recursos.recursosAvancados) "Ativo" else "Bloqueado",
                disponivel = recursos.recursosAvancados,
            ),
        )

        return CorridaPresentation(
            plano = plano,
            modo = modo,
            classificacao = ClassificacaoVisual.from(analise.classificacao),
            corClassificacao = analise.corClassificacao,
            acaoDetalhes = if (modo == ModoApresentacao.DETALHES) "Menos detalhes" else "ⓘ",
            camposCompactos = camposCompactos,
            camposDetalhes = camposDetalhes,
        )
    }

    private fun formatDecimal(valor: Double, casas: Int): String =
        "%.${casas}f".format(valor).replace(".", ",")

    private fun formatMoney(valor: Double): String =
        "R$ %.2f".format(valor).replace(".", ",")

    private fun formatKm(valor: Double): String {
        val texto = if (valor % 1.0 == 0.0) {
            "%.0f".format(valor)
        } else {
            "%.1f".format(valor)
        }
        return "$texto km"
    }

    private fun formatLiters(valor: Double): String =
        "%.2f L".format(valor).replace(".", ",")
}
