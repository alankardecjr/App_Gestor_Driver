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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import br.com.gestordriver.core.ClassificacaoConstantes
import br.com.gestordriver.core.SemaforoOferta
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
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

private val VerdePaleta = Color(0xFF2E7D32)
private val AmareloPaleta = Color(0xFFF9A825)
private val VermelhoPaleta = Color(0xFFC62828)
private val IconesAba = listOf("◻", "⬛", "🟡", "🟢")
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
    val faixa = CalendarioApp.diasDaSemana(selecionado)
    val paleta = LocalPaletaApp.current
    val forma = RoundedCornerShape(16.dp)
    val contexto = LocalContext.current
    val abaSelecionada = NomesAba.indexOfFirst {
        it.equals(state.abaHistorico, ignoreCase = true)
    }.coerceAtLeast(0)
    val marcando = state.historicoChavesSelecionadas.isNotEmpty()
    var calendarioAberto by remember { mutableStateOf(false) }
    var mesCalendario by remember { mutableStateOf(CalendarioApp.mesDe(selecionado)) }

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
            subtitulo = "Corridas aceitas",
            icone = "📅",
            onVoltar = onVoltar,
            acao = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BotaoCircular(
                        simbolo = "?",
                        onClick = {
                            android.widget.Toast.makeText(
                                contexto,
                                "A semana serve para escolher o dia. A lista e os quatro campos mostram só as corridas aceitas nesse dia: faturamento, distância, tempo em corridas e a quantidade. Segure um card para marcar e a lixeira apaga o que estiver marcado.",
                                android.widget.Toast.LENGTH_LONG,
                            ).show()
                        },
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    BotaoCircular(
                        simbolo = if (marcando) "←" else "📆",
                        onClick = {
                            if (marcando) {
                                onCancelarMarcacao()
                            } else {
                                if (!calendarioAberto) {
                                    mesCalendario = CalendarioApp.mesDe(selecionado)
                                }
                                calendarioAberto = !calendarioAberto
                            }
                        },
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    BotaoCircular(
                        simbolo = "🗑",
                        perigo = true,
                        onClick = {
                            if (state.historicoChavesSelecionadas.isEmpty()) {
                                android.widget.Toast.makeText(
                                    contexto,
                                    "Selecionar a(s) corrida(s)",
                                    android.widget.Toast.LENGTH_SHORT,
                                ).show()
                            } else {
                                onLimpar()
                            }
                        },
                    )
                }
            },
        )

        FaixaAbasComSetas(
            titulos = NomesAba,
            icones = IconesAba,
            selecionada = abaSelecionada,
            corAtiva = paleta.texto,
            corInativa = paleta.textoSecundario,
            onSelecionar = { onAba(NomesAba[it]) },
            tamanhoFonte = 11.sp,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TituloComSetas(
                titulo = CalendarioApp.rotuloPeriodoCabecalho(selecionado, CalendarioPeriodo.SEMANA),
                onEsquerda = { onAvancarSemana(-1) },
                onDireita = { onAvancarSemana(1) },
                corTitulo = paleta.texto,
            )
        }

        if (calendarioAberto && !marcando) {
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
        } else {
            GradeDiasHistorico(
                faixa = faixa,
                selecionado = selecionado,
                hoje = hoje,
                marcados = marcados,
                onDia = onDia,
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
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MetricaResumo("R$", PresentationBuilder.formatarDinheiroHistorico(itens.sumOf { it.valorTotal }), "Faturamento")
        MetricaResumo("km", PresentationBuilder.formatarDistanciaHistorico(itens.sumOf { it.kmTotal }), "Distância")
        MetricaResumo("h", formatarTempoResumo(minutos), "Tempo em corridas")
        MetricaResumo("n", itens.size.toString(), "Corridas aceitas")
    }
}

@Composable
private fun RowScope.MetricaResumo(icone: String, valor: String, titulo: String) {
    val paleta = LocalPaletaApp.current
    Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(paleta.pocoIcone, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = icone,
                color = paleta.texto,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(modifier = Modifier.padding(start = 4.dp)) {
            Text(
                text = valor,
                color = paleta.texto,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = titulo,
                color = paleta.textoSecundario,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun formatarTempoResumo(minutos: Int): String {
    val horas = minutos / 60
    val resto = minutos % 60
    return if (horas == 0) "${resto}min" else "${horas}h ${resto}m"
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
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            faixa.forEach { dia ->
                val ativo = dia == selecionado
                val temCorrida = marcados.contains(dia)
                val cor = when {
                    ativo -> paleta.fundoPainel
                    dia == hoje -> AmareloPaleta
                    else -> paleta.texto
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onDia(dia.toEpochDay()) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
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
                            fontSize = 12.sp,
                            fontWeight = if (ativo || temCorrida) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                    if (temCorrida && !ativo) {
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .background(VerdePaleta, CircleShape),
                        )
                    }
                }
            }
        }
    }
}

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
                        Box(
                            modifier = Modifier
                                .size(24.dp)
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
                                color = when {
                                    ativo -> paleta.fundoPainel
                                    !noMes -> paleta.textoSecundario.copy(alpha = 0.4f)
                                    dia == hoje -> AmareloPaleta
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
    onMarcar: () -> Unit,
    onSegurar: () -> Unit,
) {
    val contexto = LocalContext.current
    val paleta = LocalPaletaApp.current
    val forma = RoundedCornerShape(16.dp)
    val verde = VerdePaleta
    val lucroKm = if (item.kmTotal > 0 && item.custoCombustivel != null) {
        (item.valorTotal - item.custoCombustivel) / item.kmTotal
    } else {
        null
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(paleta.fundoPainel, forma)
            .border(1.dp, paleta.borda, forma)
            .combinedClickable(
                onClick = { if (mostrarMarca) onMarcar() },
                onLongClick = onSegurar,
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (mostrarMarca) {
                Text(
                    text = if (selecionado) "☑" else "☐",
                    color = if (selecionado) verde else paleta.textoSecundario,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(paleta.pocoIcone, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = seloPlataforma(item.plataforma), fontSize = 13.sp)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
            ) {
                Text(
                    text = formatarCabecalhoData(item.dataHoraRegistro, item.dataLista, item.horaLista),
                    color = paleta.texto,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.plataforma,
                    color = paleta.textoSecundario,
                    fontSize = 11.sp,
                    maxLines = 1,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = PresentationBuilder.formatarCelulaHistoricoNota(item.notaPassageiro),
                    color = corDaNota(item),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = item.classificacao.rotulo,
                    color = parseCor(item.corClassificacao),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(28.dp)
                    .background(paleta.pocoIcone, CircleShape)
                    .clickable {
                        android.widget.Toast.makeText(
                            contexto,
                            "Valor, R$/km, distância e tempo são da corrida aceita. Combustível e custo são o cálculo gravado. Resultado é o valor menos esse custo. A nota segue o semáforo da nota. Rota abre o mapa.",
                            android.widget.Toast.LENGTH_LONG,
                        ).show()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "?",
                    color = paleta.texto,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        val temEmbarque = !item.enderecoEmbarque.isNullOrBlank()
        val temDestino = !item.enderecoDestino.isNullOrBlank()
        if (temEmbarque || temDestino) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (temEmbarque) {
                        LinhaRota("●", item.enderecoEmbarque.orEmpty(), VerdePaleta)
                    }
                    if (temDestino) {
                        LinhaRota("●", item.enderecoDestino.orEmpty(), VermelhoPaleta)
                    }
                }
                Text(
                    text = "Rota",
                    color = paleta.texto,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .background(paleta.pocoIcone, RoundedCornerShape(12.dp))
                        .clickable {
                            abrirMapaDoHistorico(
                                contexto,
                                item.enderecoEmbarque,
                                item.enderecoDestino,
                            )
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            CelulaHistorico("VALOR", PresentationBuilder.formatarCelulaHistoricoValor(item.valorTotal))
            CelulaHistorico("R$/KM", PresentationBuilder.formatarCelulaHistoricoValorPorKm(item.valorPorKm))
            CelulaHistorico("DISTÂNCIA", PresentationBuilder.formatarDistanciaHistorico(item.kmTotal))
            CelulaHistorico("TEMPO", PresentationBuilder.formatarTempoHistorico(item.tempoEstimado))
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            CelulaHistorico("COMBUSTÍVEL", PresentationBuilder.formatarLitrosHistorico(item.combustivelEstimado))
            CelulaHistorico("CUSTO", PresentationBuilder.formatarGastoHistorico(item.custoCombustivel))
            CelulaHistorico(
                "RESULTADO",
                PresentationBuilder.formatarLucroHistorico(item.valorTotal, item.custoCombustivel),
                verde,
            )
            CelulaHistorico(
                "RESULTADO/KM",
                lucroKm?.let { PresentationBuilder.formatarCelulaHistoricoValorPorKm(it) } ?: "—",
                verde,
            )
        }
        if (item.kmAtePassageiro > 0 || item.kmViagem > 0) {
            Row(modifier = Modifier.fillMaxWidth()) {
                if (item.kmAtePassageiro > 0) {
                    CelulaHistorico(
                        "ATÉ O PASSAGEIRO",
                        PresentationBuilder.formatarDistanciaHistorico(item.kmAtePassageiro),
                    )
                }
                if (item.kmViagem > 0) {
                    CelulaHistorico(
                        "VIAGEM",
                        PresentationBuilder.formatarDistanciaHistorico(item.kmViagem),
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.CelulaHistorico(titulo: String, valor: String, corValor: Color? = null) {
    val paleta = LocalPaletaApp.current
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = titulo,
            color = paleta.textoSecundario,
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = valor,
            color = corValor ?: paleta.texto,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RowScope.RotuloMetrica(texto: String) {
    Text(
        text = texto,
        color = LocalPaletaApp.current.textoSecundario,
        fontSize = 9.sp,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = Modifier.weight(1f),
    )
}

@Composable
private fun RowScope.ValorGanhos(valor: String) {
    Text(
        text = valor,
        color = LocalPaletaApp.current.texto,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1.15f),
    )
}

@Composable
private fun RowScope.CaixaMetrica(valor: String) {
    val paleta = LocalPaletaApp.current
    Box(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 2.dp)
            .background(paleta.fundoMetrica, RoundedCornerShape(6.dp))
            .padding(vertical = 4.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = valor,
            color = VerdePaleta,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LinhaRota(marca: String, texto: String, cor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = marca, color = cor, fontSize = 12.sp, modifier = Modifier.padding(end = 6.dp))
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

@Composable
private fun corDaNota(item: HistoricoItemPresentation): Color {
    val contexto = LocalContext.current
    val config = (contexto.applicationContext as? GestorDriverApp)?.configuracaoStore?.carregar()
    val cor = if (config != null && config.marcaNotaBoa > 0.0) {
        SemaforoOferta.corPorDuasMarcas(item.notaPassageiro, config.marcaNotaRuim, config.marcaNotaBoa)
    } else {
        item.corClassificacao
    }
    if (cor == ClassificacaoConstantes.COR_BORDA_NEUTRA) {
        return LocalPaletaApp.current.texto
    }
    return parseCor(cor)
}

private fun parseCor(valor: String): Color =
    try {
        Color(android.graphics.Color.parseColor(valor))
    } catch (_: IllegalArgumentException) {
        VerdePaleta
    }
