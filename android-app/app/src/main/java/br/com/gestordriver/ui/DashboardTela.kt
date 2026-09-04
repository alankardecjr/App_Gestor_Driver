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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestordriver.core.CalendarioApp
import br.com.gestordriver.core.CalendarioPeriodo
import br.com.gestordriver.core.CorridaParaResumo
import br.com.gestordriver.core.DashboardNumeros
import br.com.gestordriver.model.ConfiguracaoUsuario
import br.com.gestordriver.model.HistoricoItemPresentation
import br.com.gestordriver.ui.theme.LocalPaletaApp
import java.time.LocalDate

private val VerdeMetrica   = Color(0xFF7CB342)
private val AmareloMetrica = Color(0xFFFFD54F)
private val VermelhoMetrica= Color(0xFFE53935)
private val AbasModoDash   = listOf("Dia", "Semana", "Mês", "Ano")

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
    val forma   = RoundedCornerShape(10.dp)
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
    val faixa = CalendarioApp.faixaDiasVisivel(selecionado, periodo)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(paleta.fundoPainel, forma)
            .border(2.dp, paleta.borda, forma)
            .padding(vertical = 8.dp),
    ) {
        // ── Cabeçalho ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "←",
                color = paleta.texto,
                fontSize = 22.sp,
                modifier = Modifier
                    .clickable(onClick = onVoltar)
                    .padding(8.dp),
            )
            Text(
                text = "Dashboard",
                color = paleta.texto,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
        }

        // ── Abas Diário / Semanal / Mensal ─────────────────────────
        FaixaAbasComSetas(
            titulos = AbasModoDash,
            selecionada = periodo.ordinal.coerceIn(0, AbasModoDash.lastIndex),
            corAtiva = VerdeMetrica,
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

        // ── Seleção visual: dia = semana de dias; mês = chips; semana/ano = só setas
        when (periodo) {
            CalendarioPeriodo.DIA -> GradeDiasDash(
                faixa = faixa,
                selecionado = selecionado,
                hoje = hoje,
                marcados = marcados,
                onDia = onDia,
            )
            CalendarioPeriodo.MES -> FaixaMesesDash(
                meses = CalendarioApp.mesesDoAno(selecionado),
                selecionado = selecionado,
                onMes = onDia,
            )
            CalendarioPeriodo.SEMANA, CalendarioPeriodo.ANO -> Unit
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {

            // ── Receitas / Despesas / Saldo ───────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CartaoDash(
                    titulo = "Receitas",
                    hint = "",
                    valor = fmtDinheiro(receitas),
                    cor = VerdeMetrica,
                )
                CartaoDash(
                    titulo = "Despesas",
                    hint = "",
                    valor = fmtDinheiro(despesas),
                    cor = VermelhoMetrica,
                )
                CartaoDash(
                    titulo = "Saldo",
                    hint = "",
                    valor = fmtDinheiro(saldo),
                    cor = if (saldo >= 0) VerdeMetrica else VermelhoMetrica,
                )
            }

            SecaoTitulo("Ganhos/Custos líquido")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CartaoDashDuplo(
                    titulo = "Ganhos Km",
                    hint = "",
                    valor = "R$ ${fmtDecimal(ganhoPorKm)}",
                    cor = VerdeMetrica,
                )
                CartaoDashDuplo(
                    titulo = "Custo Km",
                    hint = "",
                    valor = "R$ ${fmtDecimal(custoPorKm)}",
                    cor = VermelhoMetrica,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CartaoDashDuplo(
                    titulo = "Ganhos hora",
                    hint = "",
                    valor = "R$ ${fmtDecimal(ganhoPorHora)}",
                    cor = VerdeMetrica,
                )
                CartaoDashDuplo(
                    titulo = "Custo hora",
                    hint = "",
                    valor = "R$ ${fmtDecimal(custoPorHora)}",
                    cor = VermelhoMetrica,
                )
            }

            SecaoTitulo("Estimativa de custos")

            LinhaEstimativa(
                rotulo = "Combustível",
                valor = estCombustivel?.let { fmtDinheiro(it) } ?: "—",
                aviso = estCombustivel == null,
            )
            LinhaEstimativa(
                rotulo = "óleo",
                valor = estOleo?.let { fmtDinheiro(it) } ?: "—",
                aviso = estOleo == null,
            )
            LinhaEstimativa(
                rotulo = "Pneu dianteiros",
                valor = estPneuD?.let { fmtDinheiro(it) } ?: "—",
                aviso = estPneuD == null,
            )
            LinhaEstimativa(
                rotulo = "Pneu traseiros",
                valor = estPneuT?.let { fmtDinheiro(it) } ?: "—",
                aviso = estPneuT == null,
            )
            LinhaEstimativa(
                rotulo = "Seguro",
                valor = estSeguro?.let { fmtDinheiro(it) } ?: "—",
                aviso = estSeguro == null,
            )
            LinhaEstimativa(
                rotulo = "IPVA",
                valor = estIpva?.let { fmtDinheiro(it) } ?: "—",
                aviso = estIpva == null,
            )

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
private fun RowScope.CartaoDash(
    titulo: String,
    hint: String,
    valor: String,
    cor: Color,
) {
    val paleta = LocalPaletaApp.current
    Column(
        modifier = Modifier
            .weight(1f)
            .background(paleta.fundoMetrica, RoundedCornerShape(10.dp))
            .border(1.dp, cor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(
            titulo,
            color = paleta.texto,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        Text(hint, color = paleta.textoSecundario, fontSize = 9.sp, textAlign = TextAlign.Center, maxLines = 1)
        Text(valor, color = cor, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
private fun RowScope.CartaoDashDuplo(
    titulo: String,
    hint: String,
    valor: String,
    cor: Color,
) {
    val paleta = LocalPaletaApp.current
    Column(
        modifier = Modifier
            .weight(1f)
            .background(paleta.fundoPainel, RoundedCornerShape(8.dp))
            .border(1.dp, paleta.borda, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(titulo, color = paleta.texto, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
        Text(hint, color = paleta.textoSecundario, fontSize = 10.sp, maxLines = 2)
        Text(valor, color = cor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
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
private fun LinhaEstimativa(
    rotulo: String,
    valor: String,
    aviso: Boolean = false,
    detalhe: String? = null,
) {
    val paleta = LocalPaletaApp.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(paleta.fundoPainel, RoundedCornerShape(8.dp))
            .border(1.dp, paleta.borda, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = rotulo,
                color = if (aviso) paleta.textoSecundario else paleta.texto,
                fontSize = 13.sp,
            )
            if (detalhe != null) {
                Text(detalhe, color = paleta.textoSecundario, fontSize = 11.sp)
            }
        }
        Text(
            text = valor,
            color = if (aviso) paleta.textoSecundario else VerdeMetrica,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun FaixaMesesDash(
    meses: List<LocalDate>,
    selecionado: LocalDate,
    onMes: (Long) -> Unit,
) {
    val paleta = LocalPaletaApp.current
    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
        meses.chunked(6).forEach { linha ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                linha.forEach { mes ->
                    val ativo = CalendarioApp.noMes(mes, selecionado)
                    Text(
                        text = CalendarioApp.rotuloMesChip(mes),
                        color = if (ativo) paleta.fundoPainel else paleta.texto,
                        fontSize = 12.sp,
                        fontWeight = if (ativo) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .then(
                                if (ativo) {
                                    Modifier.background(paleta.texto, RoundedCornerShape(8.dp))
                                } else {
                                    Modifier
                                },
                            )
                            .clickable { onMes(mes.toEpochDay()) }
                            .padding(vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun GradeDiasDash(
    faixa: List<LocalDate>,
    selecionado: LocalDate,
    hoje: LocalDate,
    marcados: Set<LocalDate>,
    onDia: (Long) -> Unit,
) {
    val paleta = LocalPaletaApp.current
    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CalendarioApp.rotulosCabecalhoSemana().forEach { r ->
                Text(r, color = paleta.textoSecundario, fontSize = 10.sp,
                    textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            faixa.forEach { dia ->
                val ativo = dia == selecionado
                val temCorrida = marcados.contains(dia)
                val cor = when {
                    ativo -> paleta.fundoPainel
                    dia == hoje -> AmareloMetrica
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
                            .then(if (ativo) Modifier.background(paleta.texto, CircleShape) else Modifier),
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
                                .background(VerdeMetrica, CircleShape),
                        )
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
    "${"%,.0f".format(valor).replace(",", ".")} km"
