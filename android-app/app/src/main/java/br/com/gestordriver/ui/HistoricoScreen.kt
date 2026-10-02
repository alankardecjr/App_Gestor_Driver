package br.com.gestordriver.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestordriver.GestorDriverApp
import br.com.gestordriver.core.CalendarioApp
import br.com.gestordriver.core.CalendarioPeriodo
import br.com.gestordriver.data.chaveHistorico
import br.com.gestordriver.model.ClassificacaoVisual
import br.com.gestordriver.model.HistoricoItemPresentation
import br.com.gestordriver.navigation.NavegacaoLauncher
import br.com.gestordriver.overlay.OverlayAcao
import br.com.gestordriver.overlay.OverlayBridge
import br.com.gestordriver.ui.theme.LocalPaletaApp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val VerdePaleta = Color(0xFF2E7D32)
private val VermelhoPaleta = Color(0xFFC62828)
private val DestaqueSelecionado = Color(0xFF7CB342)
private val NomesAba = listOf("Todos", "Uber", "99", "inDrive")

@Composable
fun HistoricoTela(
    state: AppState,
    onVoltar: () -> Unit,
    onDia: (Long) -> Unit,
    onAvancarSemana: (Int) -> Unit,
    onAba: (String) -> Unit,
    onSelecionar: (HistoricoItemPresentation) -> Unit,
    onCancelarMarcacao: () -> Unit,
    onLimpar: () -> Unit,
    onExcluir: (HistoricoItemPresentation) -> Unit,
) {
    val itens = state.historico
        .sortedByDescending { it.dataHoraRegistro ?: LocalDateTime.MIN }
        .filter { item ->
            val dia = item.dataHoraRegistro?.toLocalDate() ?: return@filter false
            dia == state.historicoDia && item.pertenceAba(state.abaHistorico)
        }
    val selecionado = state.historicoDia
    val hoje = CalendarioApp.hoje()
    val marcados = state.historico.mapNotNull { it.dataHoraRegistro?.toLocalDate() }.toSet()
    val paleta = LocalPaletaApp.current
    val forma = RoundedCornerShape(16.dp)
    val contexto = LocalContext.current
    val abaSelecionada = NomesAba.indexOfFirst {
        it.equals(state.abaHistorico, ignoreCase = true)
    }.coerceAtLeast(0)
    val marcando = state.historicoChavesSelecionadas.isNotEmpty()
    var calendarioAberto by remember { mutableStateOf(false) }
    var mesCalendario by remember { mutableStateOf(CalendarioApp.mesDe(selecionado)) }
    var detalhe by remember { mutableStateOf<HistoricoItemPresentation?>(null) }
    BackHandler(enabled = detalhe != null) { detalhe = null }
    BackHandler(enabled = detalhe == null && calendarioAberto) { calendarioAberto = false }
    BackHandler(enabled = detalhe == null && !calendarioAberto && marcando) { onCancelarMarcacao() }
    LaunchedEffect(itens) {
        val aberto = detalhe
        if (aberto != null && itens.none { it.chaveHistorico() == aberto.chaveHistorico() }) {
            detalhe = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .deslizeHorizontalAbas(abaSelecionada, NomesAba.size) { novo ->
                onAba(NomesAba[novo])
            }
            .background(paleta.fundo, forma)
            .border(1.dp, paleta.borda, forma)
            .padding(vertical = 8.dp),
    ) {
        CabecalhoTela(
            titulo = "Histórico",
            onVoltar = onVoltar,
            acao = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BotaoCircular(
                        simbolo = "📆",
                        onClick = {
                            if (!calendarioAberto) {
                                mesCalendario = CalendarioApp.mesDe(selecionado)
                            }
                            calendarioAberto = !calendarioAberto
                        },
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    BotaoCircular(
                        simbolo = "?",
                        onClick = {
                            android.widget.Toast.makeText(
                                contexto,
                                "O histórico mostra só as corridas aceitas do dia. Todos, Uber, 99 e inDrive filtram o app. As setas trocam um dia. Hoje aparece quando o dia escolhido é o atual. O calendário abre o mês e pinta o dia de hoje de verde. O card soma a quantidade, os km e o tempo. Os valores ficam no Dashboard. Segure um card para marcar: Cancelar desmarca e a Lixeira pede confirmação para apagar.",
                                android.widget.Toast.LENGTH_LONG,
                            ).show()
                        },
                    )
                }
            },
        )

        FaixaAppsHistorico(
            selecionada = abaSelecionada,
            onSelecionar = { onAba(NomesAba[it]) },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TituloComSetas(
                titulo = CalendarioApp.rotuloPeriodoCabecalho(selecionado, CalendarioPeriodo.DIA),
                onEsquerda = { onDia(selecionado.minusDays(1).toEpochDay()) },
                onDireita = { onDia(selecionado.plusDays(1).toEpochDay()) },
                corTitulo = paleta.texto,
            )
            val sub = CalendarioApp.subtituloPeriodo(selecionado, CalendarioPeriodo.DIA)
            if (sub.isNotBlank()) {
                Text(text = sub, color = paleta.textoSecundario, fontSize = 12.sp)
            }
        }

        if (calendarioAberto) {
            CalendarioMesHistorico(
                mes = mesCalendario,
                selecionado = selecionado,
                hoje = hoje,
                marcados = marcados,
                onMes = { mesCalendario = mesCalendario.plusMonths(it.toLong()) },
                onDia = { epoch ->
                    onDia(epoch)
                    calendarioAberto = false
                },
            )
        }

        ResumoPeriodoHistorico(itens)

        val rolagem = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
                .barraRolagemAoToque(rolagem)
                .verticalScroll(rolagem)
                .padding(start = 8.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (itens.isEmpty()) {
                Text(
                    text = CalendarioApp.textoVazio(CalendarioPeriodo.DIA),
                    color = paleta.textoSecundario,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                )
            } else {
                itens.forEach { item ->
                    CartaoCorridaHistorico(
                        item = item,
                        mostrarMarca = marcando,
                        selecionado = item.chaveHistorico() in state.historicoChavesSelecionadas,
                        onAbrir = { detalhe = item },
                        onMarcar = { onSelecionar(item) },
                        onSegurar = {
                            if (item.chaveHistorico() !in state.historicoChavesSelecionadas) {
                                onSelecionar(item)
                            }
                        },
                    )
                }
            }
        }

        Text(
            text = "Apenas corridas aceitas entram no histórico",
            color = paleta.textoSecundario,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
        )
        if (marcando) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(paleta.borda),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onCancelarMarcacao,
                    modifier = Modifier
                        .weight(1f)
                        .background(DestaqueSelecionado.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                ) {
                    Text(
                        text = "Cancelar",
                        color = paleta.textoSecundario,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                TextButton(
                    onClick = onLimpar,
                    modifier = Modifier
                        .weight(1f)
                        .background(DestaqueSelecionado.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                ) {
                    Text(
                        text = "Lixeira",
                        color = DestaqueSelecionado,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
        detalhe?.let { item ->
            DetalheCorridaHistorico(
                item = item,
                onFechar = { detalhe = null },
                onExcluir = { onExcluir(item) },
                onRota = {
                    abrirMapaDoHistorico(contexto, item.enderecoEmbarque, item.enderecoDestino)
                },
            )
        }
    }
}

@Composable
private fun FaixaAppsHistorico(
    selecionada: Int,
    onSelecionar: (Int) -> Unit,
) {
    val paleta = LocalPaletaApp.current
    val forma = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        NomesAba.forEachIndexed { indice, nome ->
            val ativa = indice == selecionada
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (ativa) paleta.texto else paleta.fundoPainel, forma)
                    .border(1.dp, if (ativa) paleta.texto else paleta.borda, forma)
                    .clickable { onSelecionar(indice) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = nome,
                    color = if (ativa) paleta.fundo else paleta.texto,
                    fontSize = 13.sp,
                    fontWeight = if (ativa) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ResumoPeriodoHistorico(itens: List<HistoricoItemPresentation>) {
    val paleta = LocalPaletaApp.current
    val forma = RoundedCornerShape(16.dp)
    val minutos = itens.sumOf { it.tempoEstimado ?: 0 }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(paleta.fundoPainel, forma)
            .border(1.dp, paleta.borda, forma)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MetricaResumo("R$", formatarDinheiroResumo(itens.sumOf { it.valorTotal }), "Ganhos")
        MetricaResumo("Qt", formatarQuantidadeResumo(itens.size), "Viagens")
        MetricaResumo("Km", formatarDecimalResumo(itens.sumOf { it.kmTotal }), "Distância")
        MetricaResumo("h", formatarHorasResumo(minutos), "Tempo")
    }
}

@Composable
private fun RowScope.MetricaResumo(icone: String, valor: String, titulo: String) {
    val paleta = LocalPaletaApp.current
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = titulo,
            color = paleta.texto,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(paleta.pocoIcone, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = icone,
                    color = paleta.texto,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = valor,
                color = paleta.texto,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(start = 3.dp),
            )
        }
    }
}

private fun formatarDinheiroResumo(valor: Double): String =
    "%.2f".format(Locale.US, valor).replace('.', ',')

private fun formatarQuantidadeResumo(quantidade: Int): String =
    if (quantidade < 100) "%02d".format(quantidade) else quantidade.toString()

private fun formatarDecimalResumo(valor: Double): String =
    "%.2f".format(Locale.US, valor).replace('.', ',')

private fun formatarHorasResumo(minutos: Int): String =
    formatarDecimalResumo(minutos / 60.0)

@Composable
private fun CalendarioMesHistorico(
    mes: YearMonth,
    selecionado: LocalDate,
    hoje: LocalDate,
    marcados: Set<LocalDate>,
    onMes: (Int) -> Unit,
    onDia: (Long) -> Unit,
) {
    val paleta = LocalPaletaApp.current
    val ancora = mes.atDay(1)
    val dias = CalendarioApp.gradeMes(ancora)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "‹",
                color = paleta.texto,
                fontSize = 18.sp,
                modifier = Modifier
                    .clickable { onMes(-1) }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )
            Text(
                text = CalendarioApp.rotuloMesAno(ancora),
                color = paleta.texto,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "›",
                color = paleta.texto,
                fontSize = 18.sp,
                modifier = Modifier
                    .clickable { onMes(1) }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            CalendarioApp.rotulosCabecalhoSemana().forEach { rotulo ->
                Text(
                    text = rotulo,
                    color = paleta.textoSecundario,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        dias.chunked(7).forEach { semana ->
            Row(modifier = Modifier.fillMaxWidth()) {
                semana.forEach { dia ->
                    val noMes = CalendarioApp.noMes(dia, ancora)
                    val ativo = dia == selecionado
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDia(dia.toEpochDay()) },
                        contentAlignment = Alignment.Center,
                    ) {
                        val ehHoje = dia == hoje
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    when {
                                        ehHoje -> VerdePaleta
                                        ativo -> paleta.texto
                                        else -> Color.Transparent
                                    },
                                    CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${dia.dayOfMonth}",
                                color = when {
                                    ehHoje -> Color.White
                                    ativo -> paleta.fundoPainel
                                    !noMes -> paleta.textoSecundario.copy(alpha = 0.4f)
                                    else -> paleta.texto
                                },
                                fontSize = 12.sp,
                                fontWeight = if (ativo || marcados.contains(dia)) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Normal
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CartaoCorridaHistorico(
    item: HistoricoItemPresentation,
    mostrarMarca: Boolean,
    selecionado: Boolean,
    onAbrir: () -> Unit,
    onMarcar: () -> Unit,
    onSegurar: () -> Unit,
) {
    val paleta = LocalPaletaApp.current
    val forma = RoundedCornerShape(16.dp)
    val corFaixa = parseCor(item.corClassificacao)
    val embarque = partesEndereco(item.enderecoEmbarque)
    val destino = partesEndereco(item.enderecoDestino)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(paleta.fundoPainel, forma)
            .border(if (selecionado) 2.dp else 1.dp, if (selecionado) corFaixa else paleta.borda, forma)
            .combinedClickable(
                onClick = { if (mostrarMarca) onMarcar() else onAbrir() },
                onLongClick = onSegurar,
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LogoPlataforma(item.plataforma, 40.dp)
        Column(
            modifier = Modifier.padding(start = 8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = horaHistorico(item),
                color = paleta.texto,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                text = diaMesHistorico(item),
                color = paleta.textoSecundario,
                fontSize = 10.sp,
                maxLines = 1,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        ) {
            LinhaEndereco("●", embarque.first to null, VerdePaleta)
            LinhaEndereco("●", destino.first to null, VermelhoPaleta)
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ColunaValor("R$/km", numero2(item.valorPorKm), VerdePaleta)
                ColunaValor("R$", numero2(item.valorTotal), paleta.texto)
            }
            Text(
                text = "${numero1(item.kmTotal)} km  |  ${minutosHistorico(item.tempoEstimado)}",
                color = paleta.textoSecundario,
                fontSize = 10.sp,
                maxLines = 1,
            )
        }
        Column(
            modifier = Modifier.padding(start = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(corFaixa, CircleShape),
            )
            Text(
                text = rotuloFaixa(item),
                color = corFaixa,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
        Text(
            text = "›",
            color = paleta.textoSecundario,
            fontSize = 18.sp,
            modifier = Modifier.padding(start = 2.dp),
        )
    }
}

@Composable
private fun DetalheCorridaHistorico(
    item: HistoricoItemPresentation,
    onFechar: () -> Unit,
    onExcluir: () -> Unit,
    onRota: () -> Unit,
) {
    val paleta = LocalPaletaApp.current
    val corFaixa = parseCor(item.corClassificacao)
    val custo = item.custoCombustivel
    val lucro = custo?.let { item.valorTotal - it }
    val lucroKm = if (lucro != null && item.kmTotal > 0.0) lucro / item.kmTotal else null
    val rolagem = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(paleta.fundo)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            )
            .barraRolagemAoToque(rolagem)
            .verticalScroll(rolagem)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.size(36.dp))
            Text(
                text = "DETALHES DA CORRIDA",
                color = paleta.texto,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clickable(onClick = onFechar),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "✕", color = paleta.texto, fontSize = 18.sp)
            }
        }
        CartaoDetalhe {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LogoPlataforma(item.plataforma, 48.dp)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp),
                ) {
                    Text(
                        text = dataHoraDetalhe(item),
                        color = paleta.texto,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.plataforma,
                            color = paleta.textoSecundario,
                            fontSize = 12.sp,
                        )
                        Text(
                            text = "ACEITA",
                            color = VerdePaleta,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .background(VerdePaleta.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(corFaixa.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = item.notaPassageiro?.let { numero2(it) } ?: "—",
                            color = corFaixa,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = rotuloFaixa(item),
                        color = corFaixa,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        CartaoDetalhe {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    LinhaEndereco("●", partesEndereco(item.enderecoEmbarque), VerdePaleta, 13.sp)
                    LinhaEndereco("●", partesEndereco(item.enderecoDestino), VermelhoPaleta, 13.sp)
                }
                MiniMapaHistorico(onClick = onRota)
            }
        }
        CartaoDetalhe {
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricaDetalhe("VALOR", reais(item.valorTotal))
                MetricaDetalhe("R$/KM", reais(item.valorPorKm))
                MetricaDetalhe("DISTÂNCIA", "${numero1(item.kmTotal)} km")
                MetricaDetalhe("TEMPO", minutosHistorico(item.tempoEstimado))
            }
        }
        CartaoDetalhe {
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricaDetalhe(
                    "COMBUSTÍVEL (EST.)",
                    item.combustivelEstimado?.let { "${numero2(it)} L" } ?: "—",
                )
                MetricaDetalhe("CUSTO COMB. (EST.)", custo?.let { reais(it) } ?: "—")
                MetricaDetalhe("LUCRO", lucro?.let { reais(it) } ?: "—", VerdePaleta)
                MetricaDetalhe("LUCRO/KM", lucroKm?.let { reais(it) } ?: "—", VerdePaleta)
            }
        }
        CartaoDetalhe {
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoDetalhe("◎", "Km até o passageiro", "${numero1(item.kmAtePassageiro)} km")
                InfoDetalhe("↔", "Km do passageiro ao destino", "${numero1(item.kmViagem)} km")
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.End)
                .border(1.dp, VermelhoPaleta, RoundedCornerShape(12.dp))
                .clickable(onClick = onExcluir)
                .padding(horizontal = 14.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "EXCLUIR CORRIDA",
                color = VermelhoPaleta,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun CartaoDetalhe(conteudo: @Composable () -> Unit) {
    val paleta = LocalPaletaApp.current
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(paleta.fundoPainel, forma)
            .border(1.dp, paleta.borda, forma)
            .padding(12.dp),
    ) {
        conteudo()
    }
}

@Composable
private fun LogoPlataforma(plataforma: String, tamanho: Dp) {
    val uber = plataforma.contains("Uber", ignoreCase = true)
    val noventa = plataforma.contains("99")
    val indrive = plataforma.contains("inDrive", ignoreCase = true) ||
        plataforma.contains("indrive", ignoreCase = true)
    val fundo = when {
        uber -> Color.Black
        noventa -> Color(0xFFFFD000)
        indrive -> Color(0xFFC6F135)
        else -> LocalPaletaApp.current.pocoIcone
    }
    val tinta = when {
        uber -> Color.White
        noventa || indrive -> Color.Black
        else -> LocalPaletaApp.current.texto
    }
    val sigla = when {
        uber -> "Uber"
        noventa -> "99"
        indrive -> "in"
        else -> "?"
    }
    Box(
        modifier = Modifier
            .size(tamanho)
            .background(fundo, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = sigla,
            color = tinta,
            fontSize = if (uber) 9.sp else 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun LinhaEndereco(
    marca: String,
    partes: Pair<String, String?>,
    cor: Color,
    tamanho: androidx.compose.ui.unit.TextUnit = 11.sp,
) {
    val paleta = LocalPaletaApp.current
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = marca,
            color = cor,
            fontSize = tamanho,
            modifier = Modifier.padding(end = 6.dp, top = 1.dp),
        )
        Column {
            Text(
                text = partes.first,
                color = paleta.texto,
                fontSize = tamanho,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            partes.second?.let { complemento ->
                Text(
                    text = complemento,
                    color = paleta.textoSecundario,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ColunaValor(titulo: String, valor: String, corValor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = titulo,
            color = LocalPaletaApp.current.textoSecundario,
            fontSize = 9.sp,
            maxLines = 1,
        )
        Text(
            text = valor,
            color = corValor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun RowScope.MetricaDetalhe(titulo: String, valor: String, corValor: Color? = null) {
    val paleta = LocalPaletaApp.current
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = titulo,
            color = paleta.textoSecundario,
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = valor,
            color = corValor ?: paleta.texto,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RowScope.InfoDetalhe(simbolo: String, titulo: String, valor: String) {
    val paleta = LocalPaletaApp.current
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = simbolo, color = paleta.textoSecundario, fontSize = 12.sp)
        Text(
            text = titulo,
            color = paleta.textoSecundario,
            fontSize = 8.sp,
            textAlign = TextAlign.Center,
            lineHeight = 10.sp,
            maxLines = 2,
        )
        Text(
            text = valor,
            color = paleta.texto,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MiniMapaHistorico(onClick: () -> Unit) {
    val paleta = LocalPaletaApp.current
    val forma = RoundedCornerShape(12.dp)
    Canvas(
        modifier = Modifier
            .size(108.dp, 84.dp)
            .clip(forma)
            .background(paleta.pocoIcone)
            .clickable(onClick = onClick),
    ) {
        val traco = paleta.borda
        drawLine(traco, Offset(size.width * 0.15f, size.height * 0.35f), Offset(size.width * 0.85f, size.height * 0.35f), strokeWidth = 2f)
        drawLine(traco, Offset(size.width * 0.28f, size.height * 0.15f), Offset(size.width * 0.28f, size.height * 0.85f), strokeWidth = 2f)
        drawLine(traco, Offset(size.width * 0.62f, size.height * 0.2f), Offset(size.width * 0.62f, size.height * 0.8f), strokeWidth = 2f)
        val rota = Path().apply {
            moveTo(size.width * 0.22f, size.height * 0.72f)
            quadraticBezierTo(size.width * 0.4f, size.height * 0.55f, size.width * 0.55f, size.height * 0.48f)
            quadraticBezierTo(size.width * 0.72f, size.height * 0.38f, size.width * 0.78f, size.height * 0.22f)
        }
        drawPath(rota, VerdePaleta, style = Stroke(width = 6f, cap = StrokeCap.Round))
        drawCircle(VerdePaleta, radius = 7f, center = Offset(size.width * 0.22f, size.height * 0.72f))
        drawCircle(VermelhoPaleta, radius = 8f, center = Offset(size.width * 0.78f, size.height * 0.22f))
    }
}

private val MesesHistorico = listOf(
    "JAN", "FEV", "MAR", "ABR", "MAI", "JUN",
    "JUL", "AGO", "SET", "OUT", "NOV", "DEZ",
)

private fun horaHistorico(item: HistoricoItemPresentation): String {
    val registro = item.dataHoraRegistro ?: return item.horaLista
    return registro.format(DateTimeFormatter.ofPattern("HH:mm"))
}

private fun diaMesHistorico(item: HistoricoItemPresentation): String {
    val registro = item.dataHoraRegistro ?: return item.dataLista
    return "%02d %s".format(registro.dayOfMonth, MesesHistorico[registro.monthValue - 1])
}

private fun dataHoraDetalhe(item: HistoricoItemPresentation): String {
    val registro = item.dataHoraRegistro ?: return "${item.dataLista} • ${item.horaLista}"
    val hora = registro.format(DateTimeFormatter.ofPattern("HH:mm"))
    return "%02d %s %d • %s".format(
        registro.dayOfMonth,
        MesesHistorico[registro.monthValue - 1],
        registro.year,
        hora,
    )
}

private fun minutosHistorico(minutos: Int?): String = minutos?.let { "$it min" } ?: "—"

private fun numero2(valor: Double): String =
    "%.2f".format(Locale.US, valor).replace('.', ',')

private fun numero1(valor: Double): String =
    "%.1f".format(Locale.US, valor).replace('.', ',')

private fun reais(valor: Double): String = "R$ ${numero2(valor)}"

private fun rotuloFaixa(item: HistoricoItemPresentation): String = when (item.classificacao) {
    ClassificacaoVisual.EXCELENTE -> "ÓTIMA"
    ClassificacaoVisual.BOA -> "BOA"
    ClassificacaoVisual.REGULAR -> "REGULAR"
    ClassificacaoVisual.BAIXA -> "BAIXA"
    ClassificacaoVisual.RUIM -> "RUIM"
}

private fun partesEndereco(texto: String?): Pair<String, String?> {
    val limpo = texto?.trim().orEmpty()
    if (limpo.isEmpty()) return "—" to null
    val indice = limpo.indexOf(',')
    if (indice <= 0 || indice >= limpo.lastIndex) return limpo to null
    val resto = limpo.substring(indice + 1).trim()
    return limpo.substring(0, indice).trim() to resto.ifBlank { null }
}


private fun abrirMapaDoHistorico(
    contexto: android.content.Context,
    embarque: String?,
    destino: String?,
) {
    OverlayBridge.emitir(OverlayAcao.SairParaMapaHistorico)
    val app = contexto.applicationContext as? GestorDriverApp ?: return
    android.os.Handler(android.os.Looper.getMainLooper()).post {
        NavegacaoLauncher.abrir(
            context = contexto.applicationContext,
            navegacao = app.configuracaoStore.carregar().navegacao,
            embarque = embarque,
            destino = destino,
            corridaAceita = !destino.isNullOrBlank(),
        )
    }
}

private fun parseCor(valor: String): Color =
    try {
        Color(android.graphics.Color.parseColor(valor))
    } catch (_: IllegalArgumentException) {
        VerdePaleta
    }
