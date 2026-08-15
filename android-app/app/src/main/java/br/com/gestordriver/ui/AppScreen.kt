package br.com.gestordriver.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import br.com.gestordriver.model.CampoApresentacao
import br.com.gestordriver.model.HistoricoItemPresentation
import br.com.gestordriver.model.ModoApresentacao
import kotlin.math.roundToInt

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

// =====================================================================
// TELA PRINCIPAL
// =====================================================================

@Composable
fun AppScreen(
    viewModel: AppViewModel,
    configuracoesViewModel: ConfiguracoesViewModel,
) {

    val state = viewModel.state

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
        modifier = Modifier.fillMaxSize(),
        color = FundoPrincipal,
    ) {
        if (state.interfaceOculta && state.seloFlutuante) {
            Box(modifier = Modifier.fillMaxSize()) {
                SeloFlutuante(
                    offsetX = state.seloOffsetX,
                    offsetY = state.seloOffsetY,
                    monitorando = state.monitorando,
                    onReabrir = viewModel::reabrirInterface,
                    onPosicaoAlterada = viewModel::atualizarPosicaoSelo,
                )
            }
            return@Surface
        }

        if (state.configuracoesVisivel) {
            ConfiguracoesScreen(
                viewModel = configuracoesViewModel,
                onVoltar = viewModel::fecharConfiguracoes,
            )
            return@Surface
        }

        ConteudoPrincipal(viewModel = viewModel, state = state)
    }
}

@Composable
private fun ConteudoPrincipal(
    viewModel: AppViewModel,
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

            // =========================================================
            // PREVIEW DO OVERLAY
            // =========================================================

            if (state.overlayAtivo && state.monitorando) {
                OverlayPreview()
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

                    CampoCabecalho(campo)
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
) {

    when (campo.id) {

        // =============================================================
        // R$/KM
        // =============================================================

        "valor_por_km" -> {

            CabecalhoSimples(
                icone = "💵",
                titulo = "R$/KM",
                valor = campo.valor,
                destaque = campo.destaque,
            )
        }

        // =============================================================
        // VALOR
        // =============================================================

        "valor_total" -> {

            CabecalhoSimples(
                icone = "💰",
                titulo = "R$/TOTAL",
                valor = removerPrefixoReal(
                    campo.valor,
                ),
                destaque = campo.destaque,
            )
        }

        // =============================================================
        // DISTÂNCIA
        // =============================================================

        "km_total" -> {

            CabecalhoSimples(
                icone = "🛞",
                titulo = "KM/TOTAL",
                valor = campo.valor,
                destaque = campo.destaque,
            )
        }

        // =============================================================
        // TEMPO
        // =============================================================

        "tempo_estimado" -> {

            CabecalhoSimples(
                icone = "🕐",
                titulo = "TEMPO",
                valor = campo.valor,
                destaque = campo.destaque,
            )
        }

        // =============================================================
        // NOTA        
        // =============================================================

        "nota_passageiro" -> {

            CabecalhoSimples(
                icone = "⭐",
                titulo = "NOTA",
                valor = campo.valor,
                destaque = campo.destaque,
            )
        }

        // =============================================================
        // OUTROS
        // =============================================================

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
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = titulo,
            color = TextoSecundario,
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {

        // =============================================================
        // TÍTULO CENTRALIZADO
        // =============================================================

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {

            Text(
                text = "Detalhes",
                color = TextoPrincipal,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

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

        // =============================================================
        // CONFIG
        // =============================================================

        TextButton(
            onClick = onConfig,
        ) {

            Text(
                text = "⚙️Config",
                color = TextoPrincipal,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        // =============================================================
        // OCULTAR
        // =============================================================

        TextButton(
            onClick = onOcultar,
        ) {

            Text(
                text = "❎Ocultar",
                color = TextoPrincipal,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        // =============================================================
        // FECHAR
        // =============================================================

        TextButton(
            onClick = onFechar,
        ) {

            Text(
                text = "📴Fechar",
                color = TextoPrincipal,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        // =============================================================
        // HISTÓRICO
        // =============================================================

        TextButton(
            onClick = onAlternarHistorico,
        ) {

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

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {

        // =============================================================
        // CABEÇALHO DO HISTÓRICO
        // =============================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {

                Text(
                    text = "Histórico",
                    color = TextoPrincipal,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Text(
                text = "ℹ️",
                color = TextoPrincipal,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        // =============================================================
        // LISTA HORIZONTAL
        // =============================================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    rememberScrollState(),
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            state.historico.forEachIndexed { index, item ->

                HistoricoCard(
                    item = item,
                    seta = when (index) {
                        0 -> "⬅️"
                        1 -> "➡️"
                        else -> "⏹️"
                    },
                    onClick = { onSelecionarHistorico(item) },
                )
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
// OVERLAY
// =====================================================================

@Composable
private fun OverlayPreview() {

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF213040),
        ),
    ) {

        Text(
            modifier = Modifier.padding(8.dp),
            text = "Overlay ativo sobre Uber / 99 / inDrive",
            color = TextoPrincipal,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

// =====================================================================
// SELO FLUTUANTE
// =====================================================================

@Composable
private fun SeloFlutuante(
    offsetX: Float,
    offsetY: Float,
    monitorando: Boolean,
    onReabrir: () -> Unit,
    onPosicaoAlterada: (Float, Float) -> Unit,
) {
    var dragAcumulado by remember { mutableStateOf(0f) }

    Card(
        modifier = Modifier
            .offset {
                IntOffset(
                    offsetX.roundToInt(),
                    offsetY.roundToInt(),
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { dragAcumulado = 0f },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAcumulado += kotlin.math.abs(dragAmount.x) + kotlin.math.abs(dragAmount.y)
                        onPosicaoAlterada(
                            offsetX + dragAmount.x,
                            offsetY + dragAmount.y,
                        )
                    },
                    onDragEnd = {
                        if (dragAcumulado < 24f) {
                            onReabrir()
                        }
                    },
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2B3440),
        ),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 10.dp,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "◉",
                color = if (monitorando) Color(0xFF7CB342) else Color(0xFFC62828),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (monitorando) "Monitorando" else "Parado",
                color = TextoPrincipal,
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