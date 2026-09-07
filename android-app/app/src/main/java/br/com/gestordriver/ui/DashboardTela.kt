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
import androidx.compose.ui.platform.LocalContext
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
import br.com.gestordriver.presentation.PresentationBuilder
import br.com.gestordriver.ui.theme.LocalPaletaApp
import java.time.LocalDate

private val VerdeMetrica = Color(0xFF276A63)
private val AzulMetrica = Color(0xFF39708C)
private val LaranjaMetrica = Color(0xFF8A6A42)
private val AmareloMetrica = Color(0xFFB7832F)
private val VermelhoMetrica = Color(0xFFB85C4A)
private val AbasModoDash = listOf("Semana", "Mês", "Ano")

@Composable
fun DashboardTela(
    state: AppState,
    configuracao: ConfiguracaoUsuario,
    onVoltar: () -> Unit,
    onDia: (Long) -> Unit,
    onAvancar: (Int) -> Unit,
    onPeriodo: (String) -> Unit,
) {
    val paleta = LocalPaletaApp.current
    val forma = RoundedCornerShape(10.dp)
    val periodo = state.calendarioPeriodo
    val selecionado = state.historicoDia
    val hoje = CalendarioApp.hoje()
    val contexto = LocalContext.current
    val indicePeriodo = when (periodo) {
        CalendarioPeriodo.MES -> 1
        CalendarioPeriodo.ANO -> 2
        else -> 0
    }

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
    val lucro = numeros.saldo
    val margem = if (receitas > 0.0) (lucro / receitas) * 100.0 else 0.0
    val kmTotalPeriodo = itens.sumOf { it.kmTotal }
    val minutosPeriodo = itens.sumOf { it.tempoEstimado ?: 0 }
    val litros = itens.mapNotNull { it.combustivelEstimado }
    val consumoLitros = litros.takeIf { it.isNotEmpty() }?.sum()
    val marcados = state.historico
        .mapNotNull { it.dataHoraRegistro?.toLocalDate() }
        .toSet()

    val tituloPeriodo = when (periodo) {
        CalendarioPeriodo.DIA, CalendarioPeriodo.SEMANA ->
            CalendarioApp.rotuloPeriodoCabecalho(selecionado, CalendarioPeriodo.SEMANA)
        CalendarioPeriodo.MES -> CalendarioApp.rotuloMesAno(selecionado)
        CalendarioPeriodo.ANO -> CalendarioApp.rotuloAno(selecionado)
    }
    val subtitulo = when (periodo) {
        CalendarioPeriodo.DIA, CalendarioPeriodo.SEMANA ->
            CalendarioApp.subtituloPeriodo(selecionado, CalendarioPeriodo.SEMANA)
        else -> CalendarioApp.subtituloPeriodo(selecionado, periodo)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(paleta.fundoPainel, forma)
            .border(2.dp, paleta.borda, forma),
    ) {
        CabecalhoTelaNativa(
            titulo = "Carteira",
            onVoltar = onVoltar,
        )

        FaixaAbasComSetas(
            titulos = AbasModoDash,
            selecionada = indicePeriodo,
            corAtiva = VerdeMetrica,
            corInativa = paleta.textoSecundario,
            onSelecionar = {
                onPeriodo(
                    when (it) {
                        1 -> CalendarioPeriodo.MES.name
                        2 -> CalendarioPeriodo.ANO.name
                        else -> CalendarioPeriodo.SEMANA.name
                    },
                )
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 2.dp)
                .border(1.dp, paleta.borda, forma)
                .padding(vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TituloComSetas(
                titulo = tituloPeriodo,
                onEsquerda = { onAvancar(-1) },
                onDireita = { onAvancar(1) },
                corTitulo = VerdeMetrica,
            )
            Text(
                text = "📅 Escolher dia",
                color = paleta.textoSecundario,
                fontSize = 12.sp,
                modifier = Modifier
                    .clickable {
                        android.app.DatePickerDialog(
                            contexto,
                            { _, ano, mes, dia ->
                                onPeriodo(CalendarioPeriodo.DIA.name)
                                onDia(LocalDate.of(ano, mes + 1, dia).toEpochDay())
                            },
                            selecionado.year,
                            selecionado.monthValue - 1,
                            selecionado.dayOfMonth,
                        ).show()
                    }
                    .padding(vertical = 4.dp),
            )
            if (subtitulo.isNotBlank() && periodo != CalendarioPeriodo.DIA) {
                Text(text = subtitulo, color = paleta.textoSecundario, fontSize = 12.sp)
            }
            when (periodo) {
                CalendarioPeriodo.DIA -> GradeDiasDash(
                    faixa = CalendarioApp.diasDaSemana(selecionado),
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
                CalendarioPeriodo.ANO -> FaixaAnosDash(
                    anos = CalendarioApp.anosVisiveis(selecionado),
                    selecionado = selecionado.year,
                    onAno = { ano ->
                        onDia(
                            LocalDate.of(ano, selecionado.month, 1)
                                .withDayOfMonth(
                                    minOf(selecionado.dayOfMonth, LocalDate.of(ano, selecionado.month, 1).lengthOfMonth()),
                                )
                                .toEpochDay(),
                        )
                    },
                )
                CalendarioPeriodo.SEMANA -> Unit
            }
        }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CartaoAtividade(
                    icone = "⏱",
                    titulo = "Tempo em corridas",
                    valor = if (itens.isNotEmpty()) fmtTempoCorridas(minutosPeriodo) else "—",
                    cor = VerdeMetrica,
                )
                CartaoAtividade(
                    icone = "🛣️",
                    titulo = "Km em corridas",
                    valor = if (itens.isNotEmpty()) {
                        PresentationBuilder.formatarDistanciaHistorico(kmTotalPeriodo)
                    } else {
                        "—"
                    },
                    cor = AzulMetrica,
                )
                CartaoAtividade(
                    icone = "⛽",
                    titulo = "Consumo estimado",
                    valor = PresentationBuilder.formatarLitrosHistorico(consumoLitros),
                    cor = LaranjaMetrica,
                )
            }

            SecaoTitulo("Financeiro")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                MetricaFinanceira("Receitas", fmtDinheiro(receitas), VerdeMetrica)
                MetricaFinanceira("Despesas", fmtDinheiro(despesas), VermelhoMetrica)
                MetricaFinanceira("Lucro", fmtDinheiro(lucro), if (lucro >= 0) VerdeMetrica else VermelhoMetrica)
                MetricaFinanceira("Margem", fmtPercentual(margem), VerdeMetrica)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CartaoDashDuplo("Ganhos / Km", "R$ ${fmtDecimal(numeros.ganhoPorKm)}", VerdeMetrica)
                CartaoDashDuplo("Custo / Km", "R$ ${fmtDecimal(numeros.custoPorKm)}", VermelhoMetrica)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CartaoDashDuplo("Ganhos / Hora", "R$ ${fmtDecimal(numeros.ganhoPorHora)}", VerdeMetrica)
                CartaoDashDuplo("Custo / Hora", "R$ ${fmtDecimal(numeros.custoPorHora)}", VermelhoMetrica)
            }

            SecaoTitulo("Estimativa de gastos")
            
            CardEstimativaCusto(
                icone = "⛽",
                rotulo = "Combustível",
                valor = numeros.combustivel?.let { fmtDinheiro(it) } ?: "—",
                aviso = numeros.combustivel == null,
            )
            
            CardEstimativaCusto(
                icone = "🔧",
                rotulo = "Óleo",
                valor = numeros.oleo?.let { fmtDinheiro(it) } ?: "—",
                aviso = numeros.oleo == null,
            )
            
            CardPneusEstimativa(
                dianteiro = numeros.pneuDianteiro,
                traseiro = numeros.pneuTraseiro,
            )
            
            CardEstimativaCusto(
                icone = "🛡️",
                rotulo = "Seguro",
                valor = numeros.seguro?.let { fmtDinheiro(it) } ?: "—",
                aviso = numeros.seguro == null,
                detalhe = "Rateio mensal no período",
            )
            
            CardEstimativaCusto(
                icone = "📋",
                rotulo = "IPVA",
                valor = numeros.ipva?.let { fmtDinheiro(it) } ?: "—",
                aviso = numeros.ipva == null,
                detalhe = "Rateio anual no período",
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

@Composable
private fun RowScope.CartaoAtividade(
    icone: String,
    titulo: String,
    valor: String,
    cor: Color,
) {
    val paleta = LocalPaletaApp.current
    Column(
        modifier = Modifier
            .weight(1f)
            .background(paleta.fundoMetrica, RoundedCornerShape(10.dp))
            .border(1.dp, paleta.borda, RoundedCornerShape(10.dp))
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(icone, fontSize = 16.sp)
        Text(
            titulo.uppercase(),
            color = paleta.textoSecundario,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        Text(
            valor,
            color = cor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun MetricaFinanceira(titulo: String, valor: String, cor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            titulo.uppercase(),
            color = LocalPaletaApp.current.textoSecundario,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(valor, color = cor, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun RowScope.CartaoDashDuplo(
    titulo: String,
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
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(titulo, color = paleta.texto, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(valor, color = cor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun SecaoTitulo(texto: String) {
    Text(
        text = texto.uppercase(),
        color = LocalPaletaApp.current.textoSecundario,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 2.dp),
    )
}

@Composable
private fun CardEstimativaCusto(
    icone: String,
    rotulo: String,
    valor: String,
    aviso: Boolean = false,
    detalhe: String? = null,
) {
    val paleta = LocalPaletaApp.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(paleta.fundoPainel, RoundedCornerShape(10.dp))
            .border(1.dp, paleta.borda, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = icone,
            fontSize = 18.sp,
            modifier = Modifier.size(32.dp),
            textAlign = TextAlign.Center,
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = rotulo,
                color = if (aviso) paleta.textoSecundario else paleta.texto,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            if (detalhe != null) {
                Text(
                    text = detalhe,
                    color = paleta.textoSecundario,
                    fontSize = 10.sp,
                )
            }
        }
        
        Text(
            text = valor,
            color = if (aviso) paleta.textoSecundario else VerdeMetrica,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun CardPneusEstimativa(dianteiro: Double?, traseiro: Double?) {
    val paleta = LocalPaletaApp.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(paleta.fundoPainel, RoundedCornerShape(10.dp))
            .border(1.dp, paleta.borda, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "🛞",
                fontSize = 18.sp,
                modifier = Modifier.size(32.dp),
                textAlign = TextAlign.Center,
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Pneu dianteiros",
                    color = if (dianteiro == null) paleta.textoSecundario else paleta.texto,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            
            Text(
                dianteiro?.let { fmtDinheiro(it) } ?: "—",
                color = if (dianteiro == null) paleta.textoSecundario else VerdeMetrica,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "🛞",
                fontSize = 18.sp,
                modifier = Modifier.size(32.dp),
                textAlign = TextAlign.Center,
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Pneu traseiros",
                    color = if (traseiro == null) paleta.textoSecundario else paleta.texto,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            
            Text(
                traseiro?.let { fmtDinheiro(it) } ?: "—",
                color = if (traseiro == null) paleta.textoSecundario else VerdeMetrica,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
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
private fun CartaoPneusEstimativa(dianteiro: Double?, traseiro: Double?) {
    val paleta = LocalPaletaApp.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(paleta.fundoPainel, RoundedCornerShape(8.dp))
            .border(1.dp, paleta.borda, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Pneu dianteiros", color = if (dianteiro == null) paleta.textoSecundario else paleta.texto, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text(
                dianteiro?.let { fmtDinheiro(it) } ?: "—",
                color = if (dianteiro == null) paleta.textoSecundario else VerdeMetrica,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Pneu traseiros", color = if (traseiro == null) paleta.textoSecundario else paleta.texto, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text(
                traseiro?.let { fmtDinheiro(it) } ?: "—",
                color = if (traseiro == null) paleta.textoSecundario else VerdeMetrica,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
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
private fun FaixaAnosDash(
    anos: List<Int>,
    selecionado: Int,
    onAno: (Int) -> Unit,
) {
    val paleta = LocalPaletaApp.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        anos.forEach { ano ->
            val ativo = ano == selecionado
            Text(
                text = "$ano",
                color = if (ativo) paleta.fundoPainel else paleta.texto,
                fontSize = 13.sp,
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
                    .clickable { onAno(ano) }
                    .padding(vertical = 8.dp),
            )
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
                Text(
                    r,
                    color = paleta.textoSecundario,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
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
                        .heightIn(min = 32.dp)
                        .clickable { onDia(dia.toEpochDay()) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
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

private fun fmtDinheiro(valor: Double): String =
    "R$ ${"%.2f".format(valor).replace(".", ",")}"

private fun fmtDecimal(valor: Double): String =
    "%.2f".format(valor).replace(".", ",")

private fun fmtPercentual(valor: Double): String =
    "${"%.1f".format(valor).replace(".", ",")}%"

private fun fmtTempoCorridas(minutos: Int): String {
    if (minutos <= 0) return "—"
    val h = minutos / 60
    val m = minutos % 60
    return "${h}h${"%02d".format(m)}min"
}
