package br.com.gestordriver.presentation

import br.com.gestordriver.core.AnaliseCorrida
import br.com.gestordriver.core.CalculadoraCorrida
import br.com.gestordriver.core.ClassificacaoConstantes
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
import java.util.Locale

object PresentationBuilder {

    // =====================================================================
    // DEPENDÊNCIAS
    // =====================================================================

    private val calculadora =
        CalculadoraCorrida(
            configuracaoUsuario = ConfiguracaoUsuario.padrao(),
        )

    private val controlePlano =
        ControlePlano()

    // =====================================================================
    // CORRIDA DE DEMONSTRAÇÃO
    // =====================================================================
    //
    // Mantida temporariamente para permitir a validação visual da interface.
    //
    // IMPORTANTE:
    //
    // Esta corrida NÃO representa uma corrida aceita pelo usuário.
    // Portanto, ela NUNCA deve ser adicionada automaticamente ao histórico.
    //
    // A substituição definitiva dessa demonstração pelo fluxo real:
    //
    // permissões → monitoramento → notificação → corrida atual
    //
    // será feita nas etapas posteriores.
    // =====================================================================

    private val corridaDemonstracao =
        Corrida(
            valorTotal = 38.10,
            kmAtePassageiro = 3.21,
            kmViagem = 12.82,
            tempoEstimado = 24,
        )

    // =====================================================================
    // ESTADO INICIAL
    // =====================================================================

    fun criarEstadoInicial(
        plano: PlanoAcesso = PlanoAcesso.BETA,
    ): AppState {
        return criarEstado(
            analise = null,
            plano = plano,
            historico = emptyList(),
            modo = ModoApresentacao.COMPACTA,
            historicoVisivel = false,
            configuracoesVisivel = false,
            interfaceOculta = false,
            overlayAtivo = false,
            notificacaoDisponivel = false,
            seloFlutuante = false,
            monitorando = false,
            ofertaAtiva = false,
            corridaAceita = false,
            confirmacaoFecharVisivel = false,
            seloOffsetX = 0f,
            seloOffsetY = 0f,
            estadoSalvo = null,
        )
    }

    // =====================================================================
    // CRIAÇÃO DO ESTADO
    // =====================================================================
    //
    // Esta função apenas monta o estado.
    //
    // Ela NÃO:
    // - grava corrida;
    // - adiciona corrida ao histórico;
    // - interpreta aceite;
    // - altera banco de dados.
    //
    // O histórico sempre é recebido explicitamente pelo parâmetro
    // "historico".
    // =====================================================================

    fun criarEstado(
        analise: AnaliseCorrida?,
        plano: PlanoAcesso,
        historico: List<HistoricoItemPresentation> = emptyList(),
        historicoSelecionado: HistoricoItemPresentation? = null,
        abaHistorico: String = "Uber",
        abaConfiguracao: Int = 0,
        destacarPermissoes: Boolean = false,
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
        corridaAceita: Boolean = false,
        ultimaCorridaAceita: AnaliseCorrida? = null,
        ofertaAtiva: Boolean = analise != null && !corridaAceita,
        compactaTemporaria: Boolean = false,
        corridaAntesDaOferta: AnaliseCorrida? = null,
        ofertasPendentes: Map<String, AnaliseCorrida> = emptyMap(),
        onboardingEtapa: br.com.gestordriver.model.OnboardingEtapa =
            br.com.gestordriver.model.OnboardingEtapa.NENHUMA,
        tutorialPasso: Int = 0,
    ): AppState {

        val corrida =
            if (analise == null) {
                montarCorridaVazia(
                    plano = plano,
                    modo = modo,
                )
            } else {
                val recursos =
                    controlePlano.aplicar(
                        analise = analise,
                        plano = plano,
                    )
                montarCorridaPresentation(
                    analise = analise,
                    plano = plano,
                    recursos = recursos,
                    modo = modo,
                )
            }

        return AppState(
            corrida = corrida,
            analiseAtual = analise,
            ultimaCorridaAceita = ultimaCorridaAceita,
            ofertaAtiva = ofertaAtiva,
            corridaAceita = corridaAceita,
            plano = plano,

            historico = historico,

            historicoSelecionado =
                historicoSelecionado,

            abaHistorico =
                abaHistorico,

            abaConfiguracao =
                abaConfiguracao,

            destacarPermissoes =
                destacarPermissoes,

            historicoVisivel =
                historicoVisivel,

            configuracoesVisivel =
                configuracoesVisivel,

            interfaceOculta =
                interfaceOculta,

            overlayAtivo =
                overlayAtivo,

            notificacaoDisponivel =
                notificacaoDisponivel,

            seloFlutuante =
                seloFlutuante,

            compactaTemporaria =
                compactaTemporaria,

            corridaAntesDaOferta =
                corridaAntesDaOferta,

            monitorando =
                monitorando,

            confirmacaoFecharVisivel =
                confirmacaoFecharVisivel,

            seloOffsetX =
                seloOffsetX,

            seloOffsetY =
                seloOffsetY,

            estadoSalvo =
                estadoSalvo,

            ofertasPendentes =
                ofertasPendentes,

            onboardingEtapa =
                onboardingEtapa,

            tutorialPasso =
                tutorialPasso,
        )
    }

    // =====================================================================
    // CONVERSÃO PARA HISTÓRICO
    // =====================================================================
    //
    // ATENÇÃO:
    //
    // Esta função NÃO significa "aceitar corrida".
    //
    // Ela somente transforma uma análise já existente em um objeto de
    // apresentação de histórico.
    //
    // A chamada correta deverá ocorrer futuramente quando o sistema
    // detectar que o usuário aceitou a corrida dentro do Uber,
    // 99 ou inDrive.
    // =====================================================================

    fun historicoDe(
        analise: AnaliseCorrida,
    ): HistoricoItemPresentation =
        HistoricoItemPresentation.de(
            analise,
        )

    fun formatarLiquidoPorKm(analise: AnaliseCorrida?): String {
        val custo = analise?.custoCombustivel ?: return "—"
        if (analise.kmTotal <= 0.0) {
            return "—"
        }
        return formatDecimal((analise.valorTotal - custo) / analise.kmTotal, 2) + " /km"
    }

    fun formatarDecimalPublico(valor: Double): String = formatDecimal(valor, 2)

    fun formatarKmPublico(valor: Double): String = formatKm(valor)

    // =====================================================================
    // APRESENTAÇÃO DA CORRIDA
    // =====================================================================

    private fun montarCorridaVazia(
        plano: PlanoAcesso,
        modo: ModoApresentacao,
    ): CorridaPresentation {
        val camposCompactos = listOf(
            CampoApresentacao(id = "valor_por_km", titulo = "R$/KM", valor = "—", destaque = true),
            CampoApresentacao(id = "valor_total", titulo = "VALOR", valor = "—"),
            CampoApresentacao(id = "km_total", titulo = "DIST.", valor = "—"),
            CampoApresentacao(id = "tempo_estimado", titulo = "TEMPO", valor = "—"),
            CampoApresentacao(id = "nota_passageiro", titulo = "NOTA", valor = "—"),
        )
        val camposDetalhes = listOf(
            CampoApresentacao(id = "km_ate_passageiro", titulo = "Até o passageiro", valor = "—"),
            CampoApresentacao(id = "km_viagem", titulo = "Até o destino", valor = "—"),
            CampoApresentacao(id = "km_total_detalhe", titulo = "Total percorrido", valor = "—"),
            CampoApresentacao(id = "combustivel_estimado", titulo = "Combustível estimado", valor = "—"),
            CampoApresentacao(id = "custo_combustivel", titulo = "Gasto estimado", valor = "—"),
            CampoApresentacao(id = "lucro_estimado", titulo = "Lucro estimado", valor = "—"),
            CampoApresentacao(id = "status_oferta", titulo = "Status", valor = "Aguardando oferta"),
        )
        return CorridaPresentation(
            plano = plano,
            modo = modo,
            classificacao = ClassificacaoVisual.REGULAR,
            corClassificacao = ClassificacaoConstantes.COR_BORDA_NEUTRA,
            acaoDetalhes = if (modo == ModoApresentacao.DETALHES) "Menos detalhes" else "ⓘ",
            camposCompactos = camposCompactos,
            camposDetalhes = camposDetalhes,
        )
    }

    private fun montarCorridaPresentation(
        analise: AnaliseCorrida,
        plano: PlanoAcesso,
        recursos: RecursosPlano,
        modo: ModoApresentacao,
    ): CorridaPresentation {

        val camposCompactos =
            listOf(

                // ---------------------------------------------------------
                // R$/KM
                // ---------------------------------------------------------

                CampoApresentacao(
                    id = "valor_por_km",
                    titulo = "R$/KM",
                    valor =
                        if (recursos.exibeValorPorKm) {
                            "R$ " + formatDecimal(
                                analise.valorPorKm,
                                2,
                            )
                        } else {
                            "🔒"
                        },
                    disponivel =
                        recursos.exibeValorPorKm,
                    destaque = true,
                ),

                // ---------------------------------------------------------
                // VALOR TOTAL
                // ---------------------------------------------------------

                CampoApresentacao(
                    id = "valor_total",
                    titulo = "VALOR",
                    valor =
                        formatMoney(
                            analise.valorTotal,
                        ),
                ),

                // ---------------------------------------------------------
                // KM TOTAL
                // ---------------------------------------------------------

                CampoApresentacao(
                    id = "km_total",
                    titulo = "DIST.",
                    valor =
                        formatKm(
                            analise.kmTotal,
                        ),
                ),

                // ---------------------------------------------------------
                // TEMPO
                // ---------------------------------------------------------

                CampoApresentacao(
                    id = "tempo_estimado",
                    titulo = "TEMPO",
                    valor =
                        analise.corrida
                            .tempoEstimado
                            ?.let { tempo ->
                                "$tempo MIN"
                            }
                            ?: "—",
                ),

                // ---------------------------------------------------------
                // NOTA
                // ---------------------------------------------------------

                CampoApresentacao(
                    id = "nota_passageiro",
                    titulo = "NOTA",
                    valor =
                        analise.notaPassageiro
                            ?.let { nota ->
                                formatDecimal(nota, 2)
                            }
                            ?: "—",
                ),
            )

        // =================================================================
        // DETALHES
        // =================================================================

        val camposDetalhes =
            listOf(

                CampoApresentacao(
                    id = "km_ate_passageiro",
                    titulo = "Até o passageiro",
                    valor =
                        formatKm(
                            analise.kmAtePassageiro,
                        ),
                ),

                CampoApresentacao(
                    id = "km_viagem",
                    titulo = "Até o destino",
                    valor =
                        formatKm(
                            analise.kmViagem,
                        ),
                ),

                CampoApresentacao(
                    id = "km_total_detalhe",
                    titulo = "Total percorrido",
                    valor = formatKm(analise.kmTotal),
                ),

                CampoApresentacao(
                    id = "endereco_embarque",
                    titulo = "Embarque",
                    valor = analise.corrida.enderecoEmbarque ?: "—",
                ),

                CampoApresentacao(
                    id = "endereco_destino",
                    titulo = "Destino",
                    valor = analise.corrida.enderecoDestino ?: "—",
                ),

                CampoApresentacao(
                    id = "combustivel_estimado",
                    titulo = "Combustível estimado",
                    valor =
                        if (
                            recursos.exibeCombustivelEstimado
                        ) {
                            analise.combustivelEstimado
                                ?.let { combustivel ->
                                    formatLiters(
                                        combustivel,
                                    )
                                }
                                ?: "—"
                        } else {
                            "🔒"
                        },
                    disponivel =
                        recursos.exibeCombustivelEstimado,
                ),

                CampoApresentacao(
                    id = "custo_combustivel",
                    titulo = "Gasto estimado",
                    valor =
                        if (
                            recursos.exibeCustoCombustivel
                        ) {
                            analise.custoCombustivel
                                ?.let { custo ->
                                    formatMoney(custo)
                                }
                                ?: "—"
                        } else {
                            "🔒"
                        },
                    disponivel =
                        recursos.exibeCustoCombustivel,
                ),

                CampoApresentacao(
                    id = "lucro_estimado",
                    titulo = "Lucro estimado",
                    valor =
                        if (recursos.exibeCustoCombustivel) {
                            analise.custoCombustivel
                                ?.let { custo ->
                                    formatMoney(analise.valorTotal - custo)
                                }
                                ?: "—"
                        } else {
                            "🔒"
                        },
                    disponivel = recursos.exibeCustoCombustivel,
                ),
            )

        // =================================================================
        // PRESENTATION
        // =================================================================

        return CorridaPresentation(
            plano = plano,

            modo = modo,

            classificacao =
                ClassificacaoVisual.from(
                    analise.classificacao,
                ),

            corClassificacao =
                analise.corClassificacao,

            acaoDetalhes =
                if (
                    modo ==
                    ModoApresentacao.DETALHES
                ) {
                    "Menos detalhes"
                } else {
                    "ⓘ"
                },

            camposCompactos =
                camposCompactos,

            camposDetalhes =
                camposDetalhes,
        )
    }

    // =====================================================================
    // FORMATAÇÃO
    // =====================================================================

    private fun formatDecimal(
        valor: Double,
        casas: Int,
    ): String =
        "%.${casas}f"
            .format(Locale.US, valor)
            .replace(".", ",")

    private fun formatMoney(
        valor: Double,
    ): String =
        "R$ %.2f"
            .format(Locale.US, valor)
            .replace(".", ",")

    private fun formatKm(
        valor: Double,
    ): String =
        "%.1f".format(Locale.US, valor).replace(".", ",") + " KM"

    private fun formatLiters(
        valor: Double,
    ): String =
        "%.2f L"
            .format(Locale.US, valor)
            .replace(".", ",")
}