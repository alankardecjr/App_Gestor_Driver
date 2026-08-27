package br.com.gestordriver.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.gestordriver.model.CampoApresentacao
import br.com.gestordriver.model.HistoricoItemPresentation
import br.com.gestordriver.model.ModoApresentacao
import br.com.gestordriver.presentation.PresentationBuilder

// =====================================================================
// CORES DA INTERFACE
// =====================================================================

private val FundoPrincipal = Color(0xFF10161D)
private val FundoCard = Color(0xFF050809)
private val FundoHistorico = Color(0xFF111821)

private val TextoPrincipal = Color.White
private val TextoSecundario = Color(0xFFB8C5D1)
private val TextoDetalhes = Color(0xFFD0D9E2)
private val TextoHistorico = Color(0xFFDDE6F2)
private val TextoAzul = Color(0xFF42A5F5)
private val TextoVerde = Color(0xFF7CB342)
private val TextoLaranja = Color(0xFFFF9800)
private val TextoAmarelo = Color(0xFFFFD54F)

// =====================================================================
// TELA PRINCIPAL
// =====================================================================

@Composable
fun AppScreen(
    viewModel: AppViewModel,
    configuracoesViewModel: ConfiguracoesViewModel,
) {

    val state = viewModel.state
    val janelaCheia = state.historicoVisivel || state.configuracoesVisivel
    val activity = LocalContext.current as? br.com.gestordriver.MainActivity
    androidx.compose.runtime.SideEffect {
        activity?.aplicarJanela(
            oculta = state.interfaceOculta,
            cheia = janelaCheia,
        )
    }

    if (state.confirmacaoFecharVisivel) {
        AlertDialog(
            onDismissRequest = viewModel::cancelarFecharApp,
            title = { Text("Fechar Gestor Driver") },
            text = {
                Text(
                    "Deseja encerrar o aplicativo e parar o monitoramento de corridas?",
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmarFecharApp) {
                    Text("Fechar")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelarFecharApp) {
                    Text("Cancelar")
                }
            },
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
                if (state.configuracoesVisivel) {
                    ConfiguracoesScreen(
                        viewModel = configuracoesViewModel,
                        onVoltar = viewModel::fecharConfiguracoes,
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
    @Suppress("UNUSED_PARAMETER") configuracoesViewModel: ConfiguracoesViewModel,
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
                .padding(
                    horizontal = 10.dp,
                    vertical = 8.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
            // BORDA GROSSA = CORRIDA EM ANDAMENTO
            // =========================================================

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 3.dp,
                        color = parseColor(
                            state.corrida.corClassificacao,
                        ),
                        shape = CardDefaults.shape,
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = FundoCard,
                ),
            ) {

                Column(
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 8.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {

                    // =================================================
                    // CABEÇALHO
                    // =================================================

                    CabecalhoCorrida(
                        campos = state.corrida.camposCompactos,
                        modo = state.corrida.modo,
                        liquidoPorKm = PresentationBuilder.formatarLiquidoPorKm(
                            state.analiseAtual ?: state.ultimaCorridaAceita,
                        ),
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

                        Spacer(
                            modifier = Modifier.height(2.dp),
                        )

                        // =================================================
                        // CONTROLES
                        //
                        // SOMENTE NA TELA EXPANDIDA
                        // =================================================

                        ControlesInterface(
                            historicoVisivel = state.historicoVisivel,
                            onConfig = viewModel::abrirConfiguracoes,
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
                state.historicoVisivel &&
                state.corrida.modo ==
                ModoApresentacao.DETALHES
            ) {

                HistoricoSection(
                    state = state,
                    onSelecionarHistorico = viewModel::selecionarHistorico,
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
    liquidoPorKm: String,
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
                        modo = modo,
                        liquidoPorKm = liquidoPorKm,
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
                    color = TextoPrincipal,
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
                    color = TextoPrincipal,
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
    modo: ModoApresentacao,
    liquidoPorKm: String,
) {

    when (campo.id) {

        "valor_por_km" -> {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CabecalhoSimples(
                    icone = "💵",
                    titulo = "R$/KM",
                    valor = campo.valor,
                    destaque = campo.destaque,
                    corTitulo = TextoVerde,
                )
                if (modo == ModoApresentacao.DETALHES) {
                    Text(
                        text = "LÍQUIDO $liquidoPorKm",
                        color = TextoVerde,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
            }
        }

        "valor_total" -> {

            CabecalhoSimples(
                icone = "💰",
                titulo = if (modo == ModoApresentacao.DETALHES) "VALOR" else "R$",
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
                titulo = if (modo == ModoApresentacao.DETALHES) "KM TOTAL" else "KM",
                valor = campo.valor,
                destaque = campo.destaque,
                corTitulo = TextoAzul,
            )
        }

        "tempo_estimado" -> {

            CabecalhoSimples(
                icone = "🕐",
                titulo = if (modo == ModoApresentacao.DETALHES) "TEMPO" else "MIN",
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
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {

            if (icone.isNotEmpty()) {

                Text(
                    text = icone,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                )
            }

            Text(
                text = valor,
                color = TextoPrincipal,
                style = if (destaque) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.bodyMedium
                },
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
            .padding(top = 2.dp),
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
                titulo = "CUSTOS (COMBUSTÍVEL)",
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
            style = MaterialTheme.typography.labelSmall,
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

        TextButton(onClick = onConfig) {
            Text(
                text = "⚙️Config",
                color = TextoPrincipal,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        TextButton(onClick = onOcultar) {
            Text(
                text = "❎Ocultar",
                color = TextoPrincipal,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        TextButton(onClick = onFechar) {
            Text(
                text = "📴Fechar",
                color = TextoPrincipal,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        TextButton(onClick = onAlternarHistorico) {
            Text(
                text = if (historicoVisivel) {
                    "⤴️Histórico"
                } else {
                    "📜Histórico"
                },
                color = TextoPrincipal,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

// =====================================================================
// SEÇÃO DE HISTÓRICO
// =====================================================================
//
// O histórico é horizontal.
// Cada corrida possui seu próprio cartão.
// =====================================================================

@Composable
private fun HistoricoSection(
    state: AppState,
    onSelecionarHistorico: (HistoricoItemPresentation) -> Unit,
) {
    val abas = listOf("Uber", "99", "inDrive")
    var abaSelecionada by remember { mutableStateOf(0) }
    val itens = state.historico.filter { item ->
        when (abas[abaSelecionada]) {
            "Uber" -> item.plataforma.contains("Uber", ignoreCase = true)
            "99" -> item.plataforma.contains("99")
            else -> item.plataforma.contains("inDrive", ignoreCase = true) ||
                item.plataforma.contains("indrive", ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Histórico",
                color = TextoPrincipal,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            abas.forEachIndexed { index, titulo ->
                TextButton(onClick = { abaSelecionada = index }) {
                    Text(
                        text = titulo,
                        color = if (abaSelecionada == index) Color(0xFF7CB342) else TextoSecundario,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (abaSelecionada == index) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }

        if (itens.isEmpty()) {
            Text(
                text = "Nenhuma corrida aceita nesta plataforma.",
                color = TextoSecundario,
                style = MaterialTheme.typography.bodySmall,
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itens.forEach { item ->
                    HistoricoCard(
                        item = item,
                        seta = "➡️",
                        onClick = { onSelecionarHistorico(item) },
                    )
                }
            }
        }
    }
}

// =====================================================================
// CARD DE HISTÓRICO
// =====================================================================
//
// BORDA FINA = CLASSIFICAÇÃO DA CORRIDA
//
// A cor vem de corClassificacao.
// =====================================================================

@Composable
private fun HistoricoCard(
    item: HistoricoItemPresentation,
    seta: String,
    onClick: () -> Unit,
) {

    Card(
        modifier = Modifier
            .width(300.dp)
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = parseColor(
                    item.corClassificacao,
                ),
                shape = CardDefaults.shape,
            ),
        colors = CardDefaults.cardColors(
            containerColor = FundoHistorico,
        ),
    ) {

        Column(
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 8.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {

            // =========================================================
            // PLATAFORMA
            // =========================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {

                    Text(
                        text = item.plataforma,
                        color = TextoPrincipal,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Text(
                    text = seta,
                    color = TextoPrincipal,
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            // =========================================================
            // DATA / HORA
            // =========================================================

            Text(
                text = "📅Data/Hora: ${item.data}",
                color = TextoHistorico,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
            )

            // =========================================================
            // INDICADORES
            // =========================================================


            Text(
                text = "R$/KM R$/TOTAL KM/TOTAL TEMPO NOTA",
                color = TextoHistorico,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
            )

            // =========================================================
            // VALORES
            // =========================================================

            Text(
                text = item.linhaHorizontal,
                color = TextoPrincipal,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
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