package br.com.gestordriver.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestordriver.model.CampoApresentacao
import br.com.gestordriver.model.ModoApresentacao
import br.com.gestordriver.presentation.PresentationBuilder
import br.com.gestordriver.ui.theme.LocalPaletaApp
import br.com.gestordriver.ui.theme.PaletaApp

// =====================================================================
// CORES DA INTERFACE
// =====================================================================

private val TextoAzul = Color(0xFF42A5F5)
private val TextoVerde = Color(0xFF7CB342)
private val TextoLaranja = Color(0xFFFF9800)
private val TextoAmarelo = Color(0xFFFFD54F)
private val AlturaPainelSecundario = 268.dp + 188.dp + 15.dp + 12.6.dp

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
        state.dashboardVisivel ||
        state.recentesConfig ||
        state.confirmacaoFecharVisivel ||
        state.confirmacaoLimparHistoricoVisivel ||
        state.onboardingEtapa != br.com.gestordriver.model.OnboardingEtapa.NENHUMA ||
        (state.monitorando && state.interfaceOculta)
    val activity = LocalContext.current as? br.com.gestordriver.MainActivity
    androidx.compose.runtime.SideEffect {
        activity?.aplicarJanela(
            oculta = state.interfaceOculta,
            cheia = janelaCheia,
        )
    }

    val escuroSistema = isSystemInDarkTheme()
    val escuro = when (configuracoesViewModel.configuracao.tema) {
        br.com.gestordriver.model.TemaApp.ESCURO -> true
        br.com.gestordriver.model.TemaApp.CLARO -> false
        br.com.gestordriver.model.TemaApp.CELULAR -> escuroSistema
    }
    val paleta = PaletaApp.de(escuro)
    CompositionLocalProvider(LocalPaletaApp provides paleta) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        color = if (state.interfaceOculta && state.monitorando) {
            paleta.fundo
        } else {
            Color.Transparent
        },
    ) {
        if (state.interfaceOculta) {
            if (state.monitorando &&
                state.onboardingEtapa == br.com.gestordriver.model.OnboardingEtapa.NENHUMA
            ) {
                when {
                    state.dashboardVisivel -> DashboardTela(
                        state = state,
                        configuracao = configuracoesViewModel.configuracao,
                        onVoltar = viewModel::fecharDashboard,
                        onDia = viewModel::selecionarDiaHistorico,
                        onAvancar = viewModel::avancarPeriodoHistorico,
                        onPeriodo = viewModel::selecionarPeriodoHistorico,
                    )
                    else -> ConfiguracoesScreen(
                        viewModel = configuracoesViewModel,
                        onVoltar = viewModel::voltarPelaBarra,
                        abaInicial = state.abaConfiguracao,
                        destacarPermissoes = state.destacarPermissoes,
                        plano = state.plano,
                    )
                }
            }
            return@Surface
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = LocalPaletaApp.current.fundo,
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
}

@Composable
private fun ConteudoPrincipal(
    viewModel: AppViewModel,
    configuracoesViewModel: ConfiguracoesViewModel,
    state: AppState,
) {
        Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            LocalPaletaApp.current.fundo,
                            LocalPaletaApp.current.fundoPainel,
                            LocalPaletaApp.current.fundo,
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
                    color = LocalPaletaApp.current.texto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {

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
                    containerColor = LocalPaletaApp.current.fundoPainel,
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

            AnimatedVisibility(
                visible = state.confirmacaoFecharVisivel &&
                    state.corrida.modo == ModoApresentacao.DETALHES,
                enter = slideInVertically(animationSpec = tween(220)) { -it },
                exit = slideOutVertically(animationSpec = tween(180)) { -it },
            ) {
                ConfirmacaoFecharSection(
                    titulo = "gestor driver",
                    mensagem = "Deseja encerrar o aplicativo e parar o monitoramento de corridas?",
                    textoConfirmar = "Fechar",
                    onCancelar = viewModel::cancelarFecharApp,
                    onConfirmar = viewModel::confirmarFecharApp,
                )
            }

            AnimatedVisibility(
                visible = state.historicoVisivel &&
                    state.corrida.modo == ModoApresentacao.DETALHES,
                enter = slideInVertically(animationSpec = tween(220)) { -it },
                exit = slideOutVertically(animationSpec = tween(180)) { -it },
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    HistoricoTela(
                        state = state,
                        onVoltar = viewModel::alternarHistorico,
                        onDia = viewModel::selecionarDiaHistorico,
                        onAvancarSemana = viewModel::avancarSemanaHistorico,
                        onAba = viewModel::selecionarAbaHistorico,
                        onSelecionar = viewModel::marcarItemHistorico,
                        onLimpar = viewModel::solicitarLimparHistorico,
                    )
                    if (state.confirmacaoLimparHistoricoVisivel) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black.copy(alpha = 0.45f))
                                .clickable(enabled = false, onClick = {})
                                .padding(horizontal = 16.dp, vertical = 24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            ConfirmacaoFecharSection(
                                titulo = "gestor driver",
                                mensagem = "Limpar histórico",
                                textoConfirmar = "Limpar",
                                onCancelar = viewModel::cancelarLimparHistorico,
                                onConfirmar = viewModel::confirmarLimparHistorico,
                            )
                        }
                    }
                }
            }
            }
        }
        if (state.configuracoesVisivel) {
            ConfiguracoesScreen(
                viewModel = configuracoesViewModel,
                onVoltar = viewModel::fecharConfiguracoes,
                abaInicial = state.abaConfiguracao,
                destacarPermissoes = state.destacarPermissoes,
                plano = state.plano,
            )
        }
        if (state.dashboardVisivel) {
            DashboardTela(
                state = state,
                configuracao = configuracoesViewModel.configuracao,
                onVoltar = viewModel::fecharDashboard,
                onDia = viewModel::selecionarDiaHistorico,
                onAvancar = viewModel::avancarPeriodoHistorico,
                onPeriodo = viewModel::selecionarPeriodoHistorico,
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
    val paleta = LocalPaletaApp.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 2.dp, color = paleta.borda, shape = CardDefaults.shape)
            .background(paleta.fundoPainel, CardDefaults.shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = titulo,
            color = paleta.texto,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = mensagem,
            color = paleta.textoSecundario,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TextButton(onClick = onCancelar) {
                Text(text = "Cancelar", color = paleta.textoSecundario)
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
    corTitulo: Color = LocalPaletaApp.current.textoSecundario,
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
                color = LocalPaletaApp.current.texto,
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
            color = LocalPaletaApp.current.textoDetalhes,
            style = MaterialTheme.typography.bodySmall,
        )

        Text(
            text = campo.valor,
            color = LocalPaletaApp.current.texto,
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