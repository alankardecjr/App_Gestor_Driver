package br.com.gestordriver.ui

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestordriver.GestorDriverApp
import br.com.gestordriver.core.CalendarioApp
import br.com.gestordriver.core.CalendarioPeriodo
import br.com.gestordriver.data.chaveHistorico
import br.com.gestordriver.model.HistoricoItemPresentation
import br.com.gestordriver.navigation.NavegacaoLauncher
import br.com.gestordriver.overlay.OverlayAcao
import br.com.gestordriver.overlay.OverlayBridge
import br.com.gestordriver.presentation.PresentationBuilder
import br.com.gestordriver.ui.theme.LocalPaletaApp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

private val TextoVerde = Color(0xFF7CB342)
private val TextoAmarelo = Color(0xFFFFD54F)
private val TextoCinzaEmoji = Color(0xFF90A4AE)
private val AbasPlataforma = listOf("Todos", "Uber", "99", "inDrive")

@Composable
fun HistoricoTela(
    state: AppState,
    onVoltar: () -> Unit,
    onDia: (Long) -> Unit,
    onAvancarSemana: (Int) -> Unit,
    onAba: (String) -> Unit,
    onAbrirDetalhes: (HistoricoItemPresentation) -> Unit,
    onMarcar: (HistoricoItemPresentation) -> Unit,
    onLimpar: () -> Unit,
) {
    val periodo = CalendarioPeriodo.SEMANA
    val itensSemana = state.historico
        .sortedByDescending { it.dataHoraRegistro ?: LocalDateTime.MIN }
        .filter { item ->
            val dia = item.dataHoraRegistro?.toLocalDate() ?: return@filter false
            CalendarioApp.noPeriodo(dia, state.historicoDia, periodo) &&
                item.pertenceAba(state.abaHistorico)
        }
    val selecionado = state.historicoDia
    val itensDia = itensSemana.filter {
        it.dataHoraRegistro?.toLocalDate() == selecionado
    }
    val hoje = CalendarioApp.hoje()
    val marcados = state.historico.mapNotNull { it.dataHoraRegistro?.toLocalDate() }.toSet()
    val faixa = CalendarioApp.diasDaSemana(selecionado)
    val paleta = LocalPaletaApp.current
    val forma = RoundedCornerShape(10.dp)
    val contexto = LocalContext.current
    val abaSelecionada = AbasPlataforma.indexOfFirst {
        it.equals(state.abaHistorico, ignoreCase = true)
    }.coerceAtLeast(0)
    val modoSelecao = state.historicoChavesSelecionadas.isNotEmpty()

    val faturamento = itensDia.sumOf { it.valorTotal }
    val distancia = itensDia.sumOf { it.kmTotal }
    val minutos = itensDia.sumOf { it.tempoEstimado ?: 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .deslizeHorizontalAbas(abaSelecionada, AbasPlataforma.size) { novo ->
                onAba(AbasPlataforma[novo])
            }
            .background(paleta.fundoPainel, forma)
            .border(2.dp, paleta.borda, forma),
    ) {
        CabecalhoTelaNativa(
            titulo = "Histórico",
            onVoltar = onVoltar,
            trailing = {
                Text(
                    text = "🔍",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                Text(
                    text = "🗑️",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .clickable {
                            if (state.historicoChavesSelecionadas.isEmpty()) {
                                android.widget.Toast.makeText(
                                    contexto,
                                    "Selecionar a(s) corrida(s)",
                                    android.widget.Toast.LENGTH_SHORT,
                                ).show()
                            } else {
                                onLimpar()
                            }
                        }
                        .padding(horizontal = 8.dp),
                )
            },
        )

        FaixaAbasComSetas(
            titulos = AbasPlataforma,
            selecionada = abaSelecionada,
            corAtiva = paleta.texto,
            corInativa = paleta.textoSecundario,
            onSelecionar = { onAba(AbasPlataforma[it]) },
            tamanhoFonte = 13.sp,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .border(1.dp, paleta.borda, forma)
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TituloComSetas(
                titulo = CalendarioApp.rotuloPeriodoCabecalho(selecionado, CalendarioPeriodo.SEMANA),
                onEsquerda = { onAvancarSemana(-1) },
                onDireita = { onAvancarSemana(1) },
                corTitulo = paleta.texto,
            )
            GradeDiasHistorico(
                faixa = faixa,
                selecionado = selecionado,
                hoje = hoje,
                marcados = marcados,
                onDia = onDia,
            )
        }

        ResumoDiaHistorico(
            faturamento = PresentationBuilder.formatarCelulaHistoricoValor(faturamento)
                .takeIf { itensDia.isNotEmpty() } ?: "—",
            distancia = if (itensDia.isNotEmpty()) {
                PresentationBuilder.formatarDistanciaHistorico(distancia)
            } else {
                "—"
            },
            tempo = if (itensDia.isNotEmpty()) PresentationBuilder.formatarTempoHm(minutos) else "—",
            corridas = if (itensDia.isNotEmpty()) "${itensDia.size}" else "—",
        )

        val rolagem = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
                .barraRolagemAoToque(rolagem)
                .verticalScroll(rolagem)
                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (itensDia.isEmpty()) {
                Text(
                    text = "Nenhuma corrida neste dia",
                    color = paleta.textoSecundario,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                )
            } else {
                itensDia.forEach { item ->
                    CartaoCorridaHistorico(
                        item = item,
                        selecionado = item.chaveHistorico() in state.historicoChavesSelecionadas,
                        modoSelecao = modoSelecao,
                        onAbrir = { onAbrirDetalhes(item) },
                        onMarcar = { onMarcar(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ResumoDiaHistorico(
    faturamento: String,
    distancia: String,
    tempo: String,
    corridas: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MetricaResumo("💵", faturamento, "Faturamento", TextoVerde)
        MetricaResumo("🛣️", distancia, "Distância", Color(0xFF1976D2))
        MetricaResumo("⏱", tempo, "Tempo online", Color(0xFFEF6C00))
        MetricaResumo("✅", corridas, "Corridas aceitas", Color(0xFF00897B))
    }
}

@Composable
private fun RowScope.MetricaResumo(icone: String, valor: String, rotulo: String, cor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = icone, fontSize = 16.sp)
        Text(
            text = valor,
            color = LocalPaletaApp.current.texto,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(text = rotulo, color = cor, fontSize = 10.sp, maxLines = 1)
    }
}

@Composable
private fun GradeDiasHistorico(
    faixa: List<LocalDate>,
    selecionado: LocalDate,
    hoje: LocalDate,
    marcados: Set<LocalDate>,
    onDia: (Long) -> Unit,
) {
    val paleta = LocalPaletaApp.current
    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            CalendarioApp.rotulosCabecalhoSemana().forEach { rotulo ->
                Text(
                    text = rotulo,
                    color = paleta.textoSecundario,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            faixa.forEach { dia ->
                val ativo = dia == selecionado
                val temCorrida = marcados.contains(dia)
                val cor = when {
                    ativo -> paleta.fundoPainel
                    dia == hoje -> TextoAmarelo
                    else -> paleta.texto
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clickable { onDia(dia.toEpochDay()) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .then(
                                if (ativo) {
                                    Modifier.background(paleta.texto, CircleShape)
                                } else {
                                    Modifier
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${dia.dayOfMonth}",
                            color = cor,
                            fontSize = 13.sp,
                            fontWeight = if (ativo || temCorrida) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                    if (temCorrida && !ativo) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(5.dp)
                                .background(TextoVerde, CircleShape),
                        )
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
    selecionado: Boolean,
    modoSelecao: Boolean,
    onAbrir: () -> Unit,
    onMarcar: () -> Unit,
) {
    val paleta = LocalPaletaApp.current
    val forma = RoundedCornerShape(10.dp)
    val corClasse = parseCor(item.corClassificacao)
    val lucro = PresentationBuilder.formatarLucroHistorico(item.valorTotal, item.custoCombustivel)
    val gasto = PresentationBuilder.formatarGastoHistorico(item.custoCombustivel)
    val consumo = PresentationBuilder.formatarLitrosHistorico(item.combustivelEstimado)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(paleta.fundoCardHistorico, forma)
            .border(2.dp, corClasse, forma)
            .combinedClickable(
                onClick = {
                    if (modoSelecao) onMarcar() else onAbrir()
                },
                onLongClick = onMarcar,
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        if (modoSelecao || selecionado) {
            Checkbox(
                checked = selecionado,
                onCheckedChange = { onMarcar() },
                colors = CheckboxDefaults.colors(
                    checkedColor = TextoVerde,
                    uncheckedColor = paleta.textoSecundario,
                ),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "${seloPlataforma(item.plataforma)} ${nomePlataforma(item.plataforma)}",
                    color = paleta.texto,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatarCabecalhoData(item.dataHoraRegistro, item.dataLista, item.horaLista),
                    color = paleta.textoSecundario,
                    fontSize = 11.sp,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = PresentationBuilder.formatarCelulaHistoricoValor(item.valorTotal),
                color = paleta.texto,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                RotuloMetrica("R$/Km")
                RotuloMetrica("Lucro")
                RotuloMetrica("Consumo")
                RotuloMetrica("Nota")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CaixaMetrica(PresentationBuilder.formatarCelulaHistoricoValorPorKm(item.valorPorKm))
                CaixaMetrica(lucro)
                CaixaMetrica(consumo)
                CaixaMetrica(PresentationBuilder.formatarCelulaHistoricoNota(item.notaPassageiro))
            }
            Text(
                text = "🛣️ ${PresentationBuilder.formatarDistanciaHistorico(item.kmTotal)}  ·  " +
                    "⏱️ ${PresentationBuilder.formatarTempoHm(item.tempoEstimado)}  ·  " +
                    "⛽ Gasto $gasto",
                color = TextoCinzaEmoji,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!item.enderecoEmbarque.isNullOrBlank()) {
                LinhaRota("●", item.enderecoEmbarque.orEmpty())
            }
            if (!item.enderecoDestino.isNullOrBlank()) {
                LinhaRota("■", item.enderecoDestino.orEmpty())
            }
        }
        Text(text = "›", color = paleta.textoSecundario, fontSize = 18.sp)
    }
}

@Composable
private fun RowScope.RotuloMetrica(texto: String) {
    Text(
        text = texto,
        color = LocalPaletaApp.current.textoSecundario,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = Modifier.weight(1f),
    )
}

@Composable
private fun RowScope.CaixaMetrica(valor: String) {
    val paleta = LocalPaletaApp.current
    Box(
        modifier = Modifier
            .weight(1f)
            .background(paleta.fundoMetrica, RoundedCornerShape(6.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = valor,
            color = TextoVerde,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LinhaRota(marca: String, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(vertical = 2.dp),
    ) {
        Text(text = marca, color = TextoVerde, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(
            text = texto,
            color = LocalPaletaApp.current.texto,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun seloPlataforma(plataforma: String): String =
    when {
        plataforma.contains("Uber", ignoreCase = true) -> "⬛"
        plataforma.contains("99") -> "🟡"
        plataforma.contains("inDrive", ignoreCase = true) ||
            plataforma.contains("indrive", ignoreCase = true) -> "🟢"
        else -> "⬜"
    }

private fun nomePlataforma(plataforma: String): String =
    when {
        plataforma.contains("Uber", ignoreCase = true) -> "Uber"
        plataforma.contains("99") -> "99"
        plataforma.contains("inDrive", ignoreCase = true) ||
            plataforma.contains("indrive", ignoreCase = true) -> "inDrive"
        plataforma.isBlank() -> "Corrida"
        else -> plataforma
    }

private fun formatarCabecalhoData(
    registro: LocalDateTime?,
    dataLista: String,
    horaLista: String,
): String {
    if (registro == null) {
        return "$dataLista  $horaLista"
    }
    val diaSemana = registro.dayOfWeek
        .getDisplayName(TextStyle.SHORT, CalendarioApp.localePtBr)
        .replaceFirstChar { it.titlecase(CalendarioApp.localePtBr) }
    val data = registro.format(DateTimeFormatter.ofPattern("dd/MM/yy"))
    val hora = registro.format(DateTimeFormatter.ofPattern("HH:mm"))
    return "$diaSemana $data  $hora"
}

private fun parseCor(valor: String): Color =
    try {
        Color(android.graphics.Color.parseColor(valor))
    } catch (_: IllegalArgumentException) {
        TextoVerde
    }
