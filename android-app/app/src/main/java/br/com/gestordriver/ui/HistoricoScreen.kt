package br.com.gestordriver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
    onSelecionar: (HistoricoItemPresentation) -> Unit,
    onLimpar: () -> Unit,
) {
    val periodo = CalendarioPeriodo.SEMANA
    val itens = state.historico
        .sortedByDescending { it.dataHoraRegistro ?: LocalDateTime.MIN }
        .filter { item ->
            val dia = item.dataHoraRegistro?.toLocalDate() ?: return@filter false
            CalendarioApp.noPeriodo(dia, state.historicoDia, periodo) &&
                item.pertenceAba(state.abaHistorico)
        }
    val selecionado = state.historicoDia
    val hoje = CalendarioApp.hoje()
    val marcados = state.historico.mapNotNull { it.dataHoraRegistro?.toLocalDate() }.toSet()
    val faixa = CalendarioApp.diasDaSemana(selecionado)
    val paleta = LocalPaletaApp.current
    val forma = RoundedCornerShape(10.dp)
    val contexto = LocalContext.current
    val abaSelecionada = AbasPlataforma.indexOfFirst {
        it.equals(state.abaHistorico, ignoreCase = true)
    }.coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .deslizeHorizontalAbas(abaSelecionada, AbasPlataforma.size) { novo ->
                onAba(AbasPlataforma[novo])
            }
            .background(paleta.fundoPainel, forma)
            .border(2.dp, paleta.borda, forma)
            .padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "⬅️",
                fontSize = 18.sp,
                modifier = Modifier
                    .clickable(onClick = onVoltar)
                    .padding(8.dp),
            )
            Text(
                text = "HISTÓRICO",
                color = paleta.texto,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
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
                    .padding(8.dp),
            )
        }

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
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TituloComSetas(
                titulo = CalendarioApp.rotuloPeriodoCabecalho(selecionado, CalendarioPeriodo.SEMANA),
                onEsquerda = { onAvancarSemana(-1) },
                onDireita = { onAvancarSemana(1) },
                corTitulo = paleta.texto,
            )
        }

        GradeDiasHistorico(
            faixa = faixa,
            selecionado = selecionado,
            hoje = hoje,
            marcados = marcados,
            onDia = onDia,
        )

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
                    text = CalendarioApp.textoVazio(periodo),
                    color = paleta.textoSecundario,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                )
            } else {
                itens.forEach { item ->
                    CartaoCorridaHistorico(
                        item = item,
                        selecionado = item.chaveHistorico() in state.historicoChavesSelecionadas,
                        onSelecionar = { onSelecionar(item) },
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
private fun GradeDiasHistorico(
    faixa: List<LocalDate>,
    selecionado: LocalDate,
    hoje: LocalDate,
    marcados: Set<LocalDate>,
    onDia: (Long) -> Unit,
) {
    val paleta = LocalPaletaApp.current
    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CalendarioApp.rotulosCabecalhoSemana().forEach { rotulo ->
                Text(
                    text = rotulo,
                    color = paleta.textoSecundario,
                    fontSize = 10.sp,
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
                        .heightIn(min = 36.dp)
                        .clickable { onDia(dia.toEpochDay()) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
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
                                .padding(top = 1.dp)
                                .size(4.dp)
                                .background(TextoVerde, CircleShape),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartaoCorridaHistorico(
    item: HistoricoItemPresentation,
    selecionado: Boolean,
    onSelecionar: () -> Unit,
) {
    val contexto = LocalContext.current
    val paleta = LocalPaletaApp.current
    val forma = RoundedCornerShape(10.dp)
    val corClasse = parseCor(item.corClassificacao)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(paleta.fundoCardHistorico, forma)
            .border(2.dp, corClasse, forma)
            .clickable(onClick = onSelecionar)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = seloPlataforma(item.plataforma),
                fontSize = 14.sp,
            )
            Text(
                text = formatarCabecalhoData(item.dataHoraRegistro, item.dataLista, item.horaLista),
                color = paleta.textoSecundario,
                fontSize = 12.sp,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            RotuloMetrica("Ganhos")
            RotuloMetrica("R$/Km")
            RotuloMetrica("R$/Lucro")
            RotuloMetrica("R$/gasto")
            RotuloMetrica("Nota")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ValorGanhos(PresentationBuilder.formatarCelulaHistoricoValor(item.valorTotal))
            CaixaMetrica(
                PresentationBuilder.formatarCelulaHistoricoValorPorKm(item.valorPorKm),
            )
            CaixaMetrica(
                PresentationBuilder.formatarLucroHistorico(item.valorTotal, item.custoCombustivel),
            )
            CaixaMetrica(
                PresentationBuilder.formatarGastoHistorico(item.custoCombustivel),
            )
            CaixaMetrica(
                PresentationBuilder.formatarCelulaHistoricoNota(item.notaPassageiro),
            )
        }

        Text(
            text = "🛞 ${PresentationBuilder.formatarDistanciaHistorico(item.kmTotal)}  ·  " +
                "🕐 ${PresentationBuilder.formatarTempoHm(item.tempoEstimado)}  ·  " +
                "⛽ Consumo ${PresentationBuilder.formatarLitrosHistorico(item.combustivelEstimado)}",
            color = TextoCinzaEmoji,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        val temEmbarque = !item.enderecoEmbarque.isNullOrBlank()
        val temDestino = !item.enderecoDestino.isNullOrBlank()
        if (temEmbarque) {
            LinhaRota("●", item.enderecoEmbarque.orEmpty())
        }
        if (temDestino) {
            LinhaRota("■", item.enderecoDestino.orEmpty())
        }
        if (temEmbarque || temDestino) {
            Row(modifier = Modifier.fillMaxWidth()) {
                if (temEmbarque) {
                    Text(
                        text = "Embarque",
                        color = TextoAmarelo,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                abrirMapaDoHistorico(contexto, item.enderecoEmbarque, null)
                            }
                            .padding(vertical = 6.dp),
                    )
                }
                if (temDestino) {
                    Text(
                        text = "Destino",
                        color = TextoAmarelo,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                abrirMapaDoHistorico(contexto, null, item.enderecoDestino)
                            }
                            .padding(vertical = 6.dp),
                    )
                }
            }
        }
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
            color = TextoVerde,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LinhaRota(marca: String, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = marca, color = TextoVerde, fontSize = 10.sp, modifier = Modifier.padding(end = 6.dp))
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

private fun parseCor(valor: String): Color =
    try {
        Color(android.graphics.Color.parseColor(valor))
    } catch (_: IllegalArgumentException) {
        TextoVerde
    }
