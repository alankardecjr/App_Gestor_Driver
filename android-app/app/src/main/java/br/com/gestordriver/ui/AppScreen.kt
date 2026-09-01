package br.com.gestordriver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestordriver.model.CampoApresentacao
import br.com.gestordriver.model.HistoricoItemPresentation
import br.com.gestordriver.model.ModoApresentacao
import br.com.gestordriver.presentation.PresentationBuilder

// =====================================================================
// CORES DA INTERFACE
// =====================================================================

private val FundoPrincipal = Color(0xFF10161D)
private val FundoPainel = Color(0xF2050809)
private val BordaNeutra = Color(0xFF607D8B)

private val TextoPrincipal = Color.White
private val TextoSecundario = Color(0xFFB8C5D1)
private val TextoDetalhes = Color(0xFFD0D9E2)
private val TextoHistorico = Color(0xFFDDE6F2)
private val TextoAzul = Color(0xFF42A5F5)
private val TextoVerde = Color(0xFF7CB342)
private val TextoLaranja = Color(0xFFFF9800)
private val TextoAmarelo = Color(0xFFFFD54F)
private val AlturaPainelSecundario = 268.dp + 188.dp
private val titulosColunaHistorico = listOf(
    "🗓️Data",
    "🕓Hora",
    "💵R$/Km",
    "💰VALOR",
    "🛞DIST.",
    "🕐TEMPO",
    "⭐NOTA",
    "Class.",
)
private val estiloCelulaHistorico = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both,
    ),
)

// =====================================================================
// TELA PRINCIPAL
// =====================================================================

@Composable
fun AppScreen(
    viewModel: AppViewModel,
    configuracoesViewModel: ConfiguracoesViewModel,
) {

    val state = viewModel.state
    val janelaCheia = state.historicoVisivel ||
        state.configuracoesVisivel ||
        state.confirmacaoFecharVisivel ||
        state.confirmacaoLimparHistoricoVisivel ||
        state.onboardingEtapa != br.com.gestordriver.model.OnboardingEtapa.NENHUMA
    val activity = LocalContext.current as? br.com.gestordriver.MainActivity
    androidx.compose.runtime.SideEffect {
        activity?.aplicarJanela(
            oculta = state.interfaceOculta,
            cheia = janelaCheia,
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        color = Color.Transparent,
    ) {
        if (state.interfaceOculta) {
            return@Surface
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = FundoPrincipal,
            ) {
                if (state.onboardingEtapa != br.com.gestordriver.model.OnboardingEtapa.NENHUMA) {
                    OnboardingHost(
                        state = state,
                        configuracoesViewModel = configuracoesViewModel,
                        onAvancarPermissoes = viewModel::avaliarInicio,
                        onContaPronta = viewModel::onboardingContaPronta,
                        onSeguirTutorial = viewModel::tutorialSeguir,
                        onPularTutorial = viewModel::tutorialPular,
                    )
                } else {
                    ConteudoPrincipal(
                        viewModel = viewModel,
                        configuracoesViewModel = configuracoesViewModel,
                        state = state,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConteudoPrincipal(
    viewModel: AppViewModel,
    configuracoesViewModel: ConfiguracoesViewModel,
    state: AppState,
) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF101418),
                            Color(0xFF171D25),
                            Color(0xFF0D1117),
                        ),
                    ),
                )
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 12.dp,
                    vertical = 8.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {

            // =========================================================
            // TÍTULO
            // =========================================================

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Gestor Driver 🚗",
                    color = TextoPrincipal,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            // =========================================================
            // CORRIDA ATUAL
            //
            // BORDA GROSSA = CLASSIFICAÇÃO DA CORRIDA ATUAL
            // =========================================================

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 5.dp,
                        color = parseColor(
                            state.corrida.corClassificacao,
                        ),
                        shape = CardDefaults.shape,
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = FundoPainel,
                ),
            ) {

                Column(
                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 6.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {

                    // =================================================
                    // CABEÇALHO
                    // =================================================

                    CabecalhoCorrida(
                        campos = state.corrida.camposCompactos,
                        modo = state.corrida.modo,
                        onInformacao = viewModel::alternarDetalhes,
                        onAlternarDetalhes = viewModel::alternarDetalhes,
                    )

                    // =================================================
                    // DETALHES
                    //
                    // SOMENTE NA TELA EXPANDIDA
                    // =================================================

                    if (
                        state.corrida.modo ==
                        ModoApresentacao.DETALHES
                    ) {

                        DetalhesCorrida(
                            campos = state.corrida.camposDetalhes,
                        )

                        ControlesInterface(
                            historicoVisivel = state.historicoVisivel,
                            configuracoesVisivel = state.configuracoesVisivel,
                            onConfig = viewModel::alternarConfiguracoes,
                            onOcultar = viewModel::ocultarInterface,
                            onFechar = viewModel::solicitarFecharApp,
                            onAlternarHistorico = viewModel::alternarHistorico,
                        )
                    }
                }
            }

            // =========================================================
            // HISTÓRICO
            //
            // SOMENTE NA TELA EXPANDIDA
            // =========================================================

            if (
                (state.confirmacaoFecharVisivel || state.confirmacaoLimparHistoricoVisivel) &&
                state.corrida.modo ==
                ModoApresentacao.DETALHES
            ) {
                ConfirmacaoFecharSection(
                    titulo = if (state.confirmacaoLimparHistoricoVisivel) {
                        "Limpar histórico"
                    } else {
                        "Fechar gestor driver"
                    },
                    mensagem = if (state.confirmacaoLimparHistoricoVisivel) {
                        "Deseja apagar todas as corridas aceitas do histórico?"
                    } else {
                        "Deseja encerrar o aplicativo e parar o monitoramento de corridas?"
                    },
                    textoConfirmar = if (state.confirmacaoLimparHistoricoVisivel) "Limpar" else "Fechar",
                    onCancelar = if (state.confirmacaoLimparHistoricoVisivel) {
                        viewModel::cancelarLimparHistorico
                    } else {
                        viewModel::cancelarFecharApp
                    },
                    onConfirmar = if (state.confirmacaoLimparHistoricoVisivel) {
                        viewModel::confirmarLimparHistorico
                    } else {
                        viewModel::confirmarFecharApp
                    },
                )
            }

            if (
                state.configuracoesVisivel &&
                state.corrida.modo ==
                ModoApresentacao.DETALHES
            ) {
                ConfiguracoesScreen(
                    viewModel = configuracoesViewModel,
                    onVoltar = viewModel::fecharConfiguracoes,
                    abaInicial = state.abaConfiguracao,
                    destacarPermissoes = state.destacarPermissoes,
                )
            }

            if (
                state.historicoVisivel &&
                state.corrida.modo ==
                ModoApresentacao.DETALHES
            ) {

                HistoricoSection(
                    state = state,
                    onSelecionarHistorico = viewModel::selecionarHistorico,
                    onAba = viewModel::selecionarAbaHistorico,
                    onLimpar = viewModel::solicitarLimparHistorico,
                )
            }
        }
}

// =====================================================================
// CABEÇALHO DA CORRIDA
// =====================================================================
//
// Estrutura:
//
// R$/KM  R$/TOTAL  KM/TOTAL  TEMPO  NOTA   ℹ️
// 💵2,38 💰38,00 🛞16,02  🕐24,6 ⭐4,98  ⬇️ / ⬆️
//
// O controle ℹ️ + seta ocupa uma única coluna estreita.
// =====================================================================

@Composable
private fun CabecalhoCorrida(
    campos: List<CampoApresentacao>,
    modo: ModoApresentacao,
    onInformacao: () -> Unit,
    onAlternarDetalhes: () -> Unit,
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        // =============================================================
        // INDICADORES DA CORRIDA
        // =============================================================

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {

            campos.forEach { campo ->

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 1.dp),
                    contentAlignment = Alignment.Center,
                ) {

                    CampoCabecalho(
                        campo = campo,
                        compacta = modo == ModoApresentacao.COMPACTA,
                    )
                }
            }
        }

        // =============================================================
        // CONTROLE ℹ️ + SETA
        //
        // UMA ÚNICA COLUNA
        // ℹ️
        // ⬇️ / ⬆️
        // =============================================================

        Column(
            modifier = Modifier.width(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            // ---------------------------------------------------------
            // INFORMAÇÃO
            // ---------------------------------------------------------

            TextButton(
                onClick = onInformacao,
                modifier = Modifier
                    .width(32.dp)
                    .height(26.dp),
                contentPadding = androidx.compose.foundation.layout
                    .PaddingValues(0.dp),
            ) {

                Text(
                    text = "ℹ️",
                    color = Color.Unspecified,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }

            // ---------------------------------------------------------
            // SETA
            // ---------------------------------------------------------

            TextButton(
                onClick = onAlternarDetalhes,
                modifier = Modifier
                    .width(32.dp)
                    .height(26.dp),
                contentPadding = androidx.compose.foundation.layout
                    .PaddingValues(0.dp),
            ) {

                Text(
                    text = if (
                        modo == ModoApresentacao.COMPACTA
                    ) {
                        "⬇️"
                    } else {
                        "⬆️"
                    },
                    color = Color.Unspecified,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}

// =====================================================================
// CAMPO DO CABEÇALHO
// =====================================================================

@Composable
private fun CampoCabecalho(
    campo: CampoApresentacao,
    compacta: Boolean,
) {
    when (campo.id) {

        "valor_por_km" -> {

                CabecalhoSimples(
                    icone = "💵",
                    titulo = "R$/KM",
                    valor = campo.valor,
                    destaque = campo.destaque,
                    corTitulo = TextoVerde,
                )
        }

        "valor_total" -> {

            CabecalhoSimples(
                icone = "💰",
                titulo = "VALOR",
                valor = removerPrefixoReal(
                    campo.valor,
                ),
                destaque = campo.destaque,
                corTitulo = TextoVerde,
            )
        }

        "km_total" -> {

            CabecalhoSimples(
                icone = "🛞",
                titulo = "DIST.",
                valor = campo.valor,
                destaque = campo.destaque,
                corTitulo = TextoAzul,
            )
        }

        "tempo_estimado" -> {

            CabecalhoSimples(
                icone = "🕐",
                titulo = "TEMPO",
                valor = campo.valor,
                destaque = campo.destaque,
                corTitulo = TextoLaranja,
            )
        }

        "nota_passageiro" -> {

            CabecalhoSimples(
                icone = "⭐",
                titulo = "NOTA",
                valor = campo.valor,
                destaque = campo.destaque,
                corTitulo = TextoAmarelo,
            )
        }

        else -> {

            CabecalhoSimples(
                icone = "",
                titulo = campo.titulo,
                valor = campo.valor,
                destaque = campo.destaque,
            )
        }
    }
}

// =====================================================================
// CABEÇALHO PADRÃO
// =====================================================================

@Composable
private fun CabecalhoSimples(
    icone: String,
    titulo: String,
    valor: String,
    destaque: Boolean,
    corTitulo: Color = TextoSecundario,
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = titulo,
            color = corTitulo,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {

            if (icone.isNotEmpty()) {

                Text(
                    text = icone,
                    fontSize = 14.sp,
                    maxLines = 1,
                )
            }

            Text(
                text = valor,
                color = TextoPrincipal,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}

// =====================================================================
// DETALHES DA CORRIDA
// =====================================================================

@Composable
private fun DetalhesCorrida(
    campos: List<CampoApresentacao>,
) {
    val mapa = campos.associateBy { it.id }
    val distancias = listOfNotNull(
        mapa["km_ate_passageiro"],
        mapa["km_viagem"],
        mapa["km_total_detalhe"],
        mapa["endereco_embarque"],
        mapa["endereco_destino"],
    )
    val custos = listOfNotNull(
        mapa["combustivel_estimado"],
        mapa["custo_combustivel"],
        mapa["lucro_estimado"],
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ColunaDetalhes(
                modifier = Modifier.weight(1f),
                titulo = "DISTÂNCIAS",
                icone = "🛞",
                corTitulo = TextoAzul,
                campos = distancias,
            )
            ColunaDetalhes(
                modifier = Modifier.weight(1f),
                titulo = "CUSTOS (ESTIMADO)",
                icone = "💰",
                corTitulo = TextoVerde,
                campos = custos,
            )
        }
        campos.filter { campo ->
            campo.id !in setOf(
                "km_ate_passageiro",
                "km_viagem",
                "km_total_detalhe",
                "endereco_embarque",
                "endereco_destino",
                "combustivel_estimado",
                "custo_combustivel",
                "lucro_estimado",
            )
        }.forEach { campo ->
            LinhaDetalhe(campo)
        }
    }
}

@Composable
private fun ColunaDetalhes(
    modifier: Modifier,
    titulo: String,
    icone: String,
    corTitulo: Color,
    campos: List<CampoApresentacao>,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "$icone $titulo",
            color = corTitulo,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        campos.forEach { campo ->
            LinhaDetalhe(campo)
        }
    }
}

// =====================================================================
// LINHA DOS DETALHES
// =====================================================================

@Composable
private fun LinhaDetalhe(
    campo: CampoApresentacao,
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Text(
            text = campo.titulo,
            color = TextoDetalhes,
            style = MaterialTheme.typography.bodySmall,
        )

        Text(
            text = campo.valor,
            color = TextoPrincipal,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// =====================================================================
// CONTROLES DA INTERFACE
//
// ESTES BOTÕES EXISTEM SOMENTE NA TELA EXPANDIDA.
// =====================================================================

@Composable
private fun ControlesInterface(
    historicoVisivel: Boolean,
    configuracoesVisivel: Boolean,
    onConfig: () -> Unit,
    onOcultar: () -> Unit,
    onFechar: () -> Unit,
    onAlternarHistorico: () -> Unit,
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        TextButton(onClick = onFechar) {
            Text(
                text = "📴 Fechar",
                color = TextoAmarelo,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        TextButton(onClick = onConfig) {
            Text(
                text = if (configuracoesVisivel) {
                    "⤴️ Config"
                } else {
                    "⚙️ Config"
                },
                color = TextoAmarelo,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        TextButton(onClick = onOcultar) {
            Text(
                text = "❎ Ocultar",
                color = TextoAmarelo,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        TextButton(onClick = onAlternarHistorico) {
            Text(
                text = if (historicoVisivel) {
                    "⤴️ Histórico"
                } else {
                    "📜 Histórico"
                },
                color = TextoAmarelo,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

// =====================================================================
// SEÇÃO DE HISTÓRICO
//
// Listas por plataforma (esboço: Uber | 99, com setas).
// Borda neutra em cada item; classificação = marcador.
// =====================================================================

@Composable
private fun HistoricoSection(
    state: AppState,
    onSelecionarHistorico: (HistoricoItemPresentation) -> Unit,
    onAba: (String) -> Unit,
    onLimpar: () -> Unit,
) {
    val plataformas = listOf("Uber", "99", "inDrive")
    val aba = state.abaHistorico
    val indice = plataformas.indexOfFirst { it.equals(aba, ignoreCase = true) }.coerceAtLeast(0)
    val itens = state.historico
        .sortedByDescending { it.dataHoraRegistro ?: java.time.LocalDateTime.MIN }
        .filter { it.pertenceAba(aba) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(AlturaPainelSecundario)
            .deslizeHorizontalAbas(indice, plataformas.size) { novo ->
                onAba(plataformas[novo])
            }
            .border(
                width = 2.dp,
                color = BordaNeutra,
                shape = CardDefaults.shape,
            )
            .background(FundoPainel, CardDefaults.shape)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
            TituloComSetas(
                titulo = "HISTÓRICO",
                onEsquerda = {
                    onAba(plataformas[(indice - 1).coerceAtLeast(0)])
                },
                onDireita = {
                    onAba(plataformas[(indice + 1).coerceAtMost(plataformas.lastIndex)])
                },
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            plataformas.forEach { plataforma ->
                Text(
                    text = plataforma.uppercase(),
                    color = if (aba.equals(plataforma, ignoreCase = true)) TextoVerde else TextoSecundario,
                    fontSize = 12.sp,
                    fontWeight = if (aba.equals(plataforma, ignoreCase = true)) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.clickable { onAba(plataforma) },
                )
            }
        }

        val rolagemHistorico = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
                .barraRolagemAoToque(rolagemHistorico)
                .verticalScroll(rolagemHistorico)
                .padding(start = 8.dp, end = 12.dp),
        ) {
            GradeColunasHistorico {
                titulosColunaHistorico.forEach { titulo ->
                    CelulaHistorico(
                        texto = titulo,
                        destaque = true,
                    )
                }
            }

            if (itens.isEmpty()) {
                Text(
                    text = "Nenhuma corrida aceita.",
                    color = TextoSecundario,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                )
            } else {
                itens.forEach { item ->
                    HistoricoItemLista(
                        item = item,
                        onClick = { onSelecionarHistorico(item) },
                    )
                }
            }
        }

        TextButton(
            onClick = onLimpar,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "🗑️ Limpar histórico",
                color = TextoAmarelo,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ConfirmacaoFecharSection(
    titulo: String,
    mensagem: String,
    textoConfirmar: String,
    onCancelar: () -> Unit,
    onConfirmar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = BordaNeutra,
                shape = CardDefaults.shape,
            )
            .background(FundoPainel, CardDefaults.shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = titulo,
            color = TextoPrincipal,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = mensagem,
            color = TextoSecundario,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TextButton(onClick = onCancelar) {
                Text(
                    text = "Cancelar",
                    color = TextoSecundario,
                )
            }
            TextButton(onClick = onConfirmar) {
                Text(
                    text = textoConfirmar,
                    color = TextoAmarelo,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// =====================================================================
// ITEM DE LISTA DO HISTÓRICO
//
// BORDA NEUTRA; CLASSIFICAÇÃO = MARCADOR
// =====================================================================

@Composable
private fun GradeColunasHistorico(
    modifier: Modifier = Modifier,
    comBorda: Boolean = false,
    conteudo: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (comBorda) Color(0xFF3D4A57) else Color.Transparent,
                shape = CardDefaults.shape,
            )
            .padding(horizontal = 1.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = conteudo,
    )
}

@Composable
private fun RowScope.CelulaHistorico(
    texto: String,
    destaque: Boolean = false,
) {
    Text(
        text = texto,
        color = if (destaque) TextoHistorico else TextoPrincipal,
        fontSize = 8.5.sp,
        lineHeight = 10.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontWeight = if (destaque) FontWeight.SemiBold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        style = estiloCelulaHistorico,
        modifier = Modifier.weight(1f, fill = true),
    )
}

@Composable
private fun HistoricoItemLista(
    item: HistoricoItemPresentation,
    onClick: () -> Unit,
) {
    val nota = item.notaPassageiro?.let { PresentationBuilder.formatarDecimalPublico(it) } ?: "—"
    val valores = listOf(
        item.dataLista,
        item.horaLista,
        PresentationBuilder.formatarDinheiroHistorico(item.valorPorKm),
        PresentationBuilder.formatarDinheiroHistorico(item.valorTotal),
        PresentationBuilder.formatarDistanciaHistorico(item.kmTotal),
        PresentationBuilder.formatarTempoHistorico(item.tempoEstimado),
        nota,
    )
    GradeColunasHistorico(
        modifier = Modifier.clickable(onClick = onClick),
        comBorda = true,
    ) {
        valores.forEach { valor ->
            CelulaHistorico(texto = valor)
        }
        Box(
            modifier = Modifier.weight(1f, fill = true),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(parseColor(item.corClassificacao), CircleShape),
            )
        }
    }
}

// =====================================================================
// UTILITÁRIO — REMOVE R$ DUPLICADO
// =====================================================================

private fun removerPrefixoReal(
    valor: String,
): String {

    return valor
        .removePrefix("R$")
        .trim()
}

// =====================================================================
// UTILITÁRIO — COR DA CLASSIFICAÇÃO
// =====================================================================

private fun parseColor(
    valor: String,
): Color {

    return try {

        Color(
            android.graphics.Color.parseColor(valor),
        )

    } catch (_: IllegalArgumentException) {

        Color.Green
    }
}