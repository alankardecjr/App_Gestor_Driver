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
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestordriver.core.CalendarioApp
import br.com.gestordriver.core.CalendarioPeriodo
import br.com.gestordriver.core.CorridaParaResumo
import br.com.gestordriver.core.DashboardNumeros
import br.com.gestordriver.model.ConfiguracaoUsuario
import br.com.gestordriver.ui.theme.LocalPaletaApp
import java.time.LocalDate
import java.time.YearMonth

private val AbasModoDash = listOf("Dia", "Semana", "Mês", "Ano")

// ─────────────────────────────────────────────────────────────────────────────
// TELA DASHBOARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DashboardTela(
    state: AppState,
    configuracao: ConfiguracaoUsuario,
    onVoltar: () -> Unit,
    onDia: (Long) -> Unit,
    onAvancar: (Int) -> Unit,
    onPeriodo: (String) -> Unit,
) {
    val paleta  = LocalPaletaApp.current
    val forma   = RoundedCornerShape(16.dp)
    val periodo = state.calendarioPeriodo
    val selecionado = state.historicoDia
    val hoje    = CalendarioApp.hoje()

    // ── corridas do período ────────────────────────────────────────
    val itens = state.historico.filter { item ->
        val dia = item.dataHoraRegistro?.toLocalDate() ?: return@filter false
        CalendarioApp.noPeriodo(dia, selecionado, periodo)
    }
    val numeros = DashboardNumeros.de(
        itens.map {
            CorridaParaResumo(
                valorTotal = it.valorTotal,
                kmTotal = it.kmTotal,
                minutos = it.tempoEstimado ?: 0,
                gastoCorrida = it.custoCombustivel,
                litros = it.combustivelEstimado,
            )
        },
        configuracao,
        diasPeriodo = CalendarioApp.diasDoPeriodo(selecionado, periodo),
    )
    val receitas = numeros.receitas
    val despesas = numeros.despesas
    val saldo = numeros.saldo
    val ganhoPorKm = numeros.ganhoPorKm
    val custoPorKm = numeros.custoPorKm
    val ganhoPorHora = numeros.ganhoPorHora
    val custoPorHora = numeros.custoPorHora
    val estCombustivel = numeros.combustivel
    val estOleo = numeros.oleo
    val estPneuD = numeros.pneuDianteiro
    val estPneuT = numeros.pneuTraseiro
    val estSeguro = numeros.seguro
    val estIpva = numeros.ipva

    val marcados = state.historico
        .mapNotNull { it.dataHoraRegistro?.toLocalDate() }
        .toSet()
    var calendarioAberto by remember { mutableStateOf(false) }
    var mesCalendario by remember { mutableStateOf(CalendarioApp.mesDe(selecionado)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(paleta.fundo, forma)
            .border(1.dp, paleta.borda, forma)
            .padding(vertical = 8.dp),
    ) {
        CabecalhoTela(
            titulo = "Dashboard",
            subtitulo = "Resultado do período",
            onVoltar = onVoltar,
            acao = {
                BotaoCircular(
                    simbolo = "📅",
                    onClick = {
                        if (!calendarioAberto) {
                            mesCalendario = CalendarioApp.mesDe(selecionado)
                        }
                        calendarioAberto = !calendarioAberto
                    },
                )
            },
        )

        // ── Abas Diário / Semanal / Mensal ─────────────────────────
        FaixaAbasComSetas(
            titulos = AbasModoDash,
            selecionada = periodo.ordinal.coerceIn(0, AbasModoDash.lastIndex),
            corAtiva = paleta.texto,
            corInativa = paleta.textoSecundario,
            onSelecionar = { onPeriodo(CalendarioPeriodo.entries[it].name) },
        )

        // ── Título do período + setas ───────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TituloComSetas(
                titulo = CalendarioApp.rotuloPeriodoCabecalho(selecionado, periodo),
                onEsquerda = { onAvancar(-1) },
                onDireita  = { onAvancar(1) },
                corTitulo  = paleta.texto,
            )
            val sub = CalendarioApp.subtituloPeriodo(selecionado, periodo)
            if (sub.isNotBlank()) {
                Text(text = sub, color = paleta.textoSecundario, fontSize = 12.sp)
            }
        }

        if (calendarioAberto) {
            CalendarioMesDash(
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

        // ── Conteúdo rolável ────────────────────────────────────────
        val scroll = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .barraRolagemAoToque(scroll)
                .verticalScroll(scroll)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CartaoTopo("⏱", "Tempo em corridas", fmtTempo(numeros.minutos))
                CartaoTopo("↔", "Km em corridas", fmtKm(numeros.kmTotal))
                CartaoTopo(
                    "⛽",
                    "Consumo estimado",
                    numeros.litros?.let { "${fmtDecimal(it)} L" } ?: "—",
                )
            }

            SecaoTitulo("Financeiro")
            CartaoGrupo {
                Row(Modifier.fillMaxWidth()) {
                    Celula("Receitas", fmtDinheiro(receitas))
                    Celula("Despesas", fmtDinheiro(despesas))
                    Celula("Lucro", fmtDinheiro(saldo))
                    Celula(
                        "Margem",
                        if (receitas > 0) "${fmtDecimal(saldo / receitas * 100)}%" else "—",
                    )
                }
            }

            CartaoGrupo {
                LinhaPar(
                    "Ganhos / km" to "R$ ${fmtDecimal(ganhoPorKm)}",
                    "Custo / km" to "R$ ${fmtDecimal(custoPorKm)}",
                )
                Box(Modifier.fillMaxWidth().height(1.dp).background(paleta.borda))
                LinhaPar(
                    "Ganhos / hora" to "R$ ${fmtDecimal(ganhoPorHora)}",
                    "Custo / hora" to "R$ ${fmtDecimal(custoPorHora)}",
                )
            }

            SecaoTitulo("Estimativa de custos")
            CartaoGrupo {
                LinhaCusto("⛽", "Combustível", estCombustivel)
                Box(Modifier.fillMaxWidth().height(1.dp).background(paleta.borda))
                LinhaCusto("🛢", "Óleo", estOleo)
                Box(Modifier.fillMaxWidth().height(1.dp).background(paleta.borda))
                LinhaCusto("◉", "Pneu dianteiro", estPneuD)
                Box(Modifier.fillMaxWidth().height(1.dp).background(paleta.borda))
                LinhaCusto("◉", "Pneu traseiro", estPneuT)
                Box(Modifier.fillMaxWidth().height(1.dp).background(paleta.borda))
                LinhaCusto("▣", "Seguro", estSeguro)
                Box(Modifier.fillMaxWidth().height(1.dp).background(paleta.borda))
                LinhaCusto("▤", "IPVA", estIpva)
            }

            if (itens.isEmpty()) {
                Text(
                    text = CalendarioApp.textoVazio(periodo),
                    color = paleta.textoSecundario,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPONENTES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RowScope.CartaoTopo(icone: String, titulo: String, valor: String) {
    val paleta = LocalPaletaApp.current
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .weight(1f)
            .background(paleta.fundoPainel, forma)
            .border(1.dp, paleta.borda, forma)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(paleta.pocoIcone, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(icone, fontSize = 14.sp)
        }
        Text(
            titulo,
            color = paleta.textoSecundario,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 12.sp,
        )
        Text(
            valor,
            color = paleta.texto,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun CartaoGrupo(conteudo: @Composable () -> Unit) {
    val paleta = LocalPaletaApp.current
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(paleta.fundoPainel, forma)
            .border(1.dp, paleta.borda, forma),
    ) {
        conteudo()
    }
}

@Composable
private fun RowScope.Celula(titulo: String, valor: String) {
    val paleta = LocalPaletaApp.current
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 2.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            titulo,
            color = paleta.textoSecundario,
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Text(
            valor,
            color = paleta.texto,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun LinhaPar(esquerda: Pair<String, String>, direita: Pair<String, String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CelulaPar(esquerda.first, esquerda.second)
        CelulaPar(direita.first, direita.second)
    }
}

@Composable
private fun RowScope.CelulaPar(titulo: String, valor: String) {
    val paleta = LocalPaletaApp.current
    Column(modifier = Modifier.weight(1f)) {
        Text(titulo, color = paleta.textoSecundario, fontSize = 11.sp, maxLines = 1)
        Text(
            valor,
            color = paleta.texto,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun LinhaCusto(icone: String, rotulo: String, valor: Double?) {
    val paleta = LocalPaletaApp.current
    val ausente = valor == null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(paleta.pocoIcone, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(icone, fontSize = 13.sp)
        }
        Text(
            rotulo,
            color = if (ausente) paleta.textoSecundario else paleta.texto,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            maxLines = 1,
        )
        Text(
            valor?.let { fmtDinheiro(it) } ?: "—",
            color = if (ausente) paleta.textoSecundario else paleta.texto,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun SecaoTitulo(texto: String) {
    Text(
        text = texto,
        color = LocalPaletaApp.current.textoSecundario,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 2.dp),
    )
}

@Composable
private fun CalendarioMesDash(
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
                    val temCorrida = marcados.contains(dia)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 36.dp)
                            .clickable { onDia(dia.toEpochDay()) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                                    color = when {
                                        ativo -> paleta.fundoPainel
                                        !noMes -> paleta.textoSecundario.copy(alpha = 0.45f)
                                        dia == hoje -> paleta.texto
                                        else -> paleta.texto
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (ativo || temCorrida) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                            if (temCorrida && noMes) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 1.dp)
                                        .size(4.dp)
                                        .background(
                                            if (ativo) paleta.texto else paleta.textoSecundario,
                                            CircleShape,
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS DE FORMATAÇÃO
// ─────────────────────────────────────────────────────────────────────────────

private fun fmtDinheiro(valor: Double): String =
    "R$ ${"%.2f".format(valor).replace(".", ",")}"

private fun fmtDecimal(valor: Double): String =
    "%.2f".format(valor).replace(".", ",")

private fun fmtKm(valor: Double): String =
    "${"%.1f".format(valor).replace(".", ",")} km"

private fun fmtTempo(minutos: Int): String {
    if (minutos <= 0) return "0 min"
    val horas = minutos / 60
    val resto = minutos % 60
    return if (horas == 0) "$resto min" else "${horas}h ${resto}min"
}
