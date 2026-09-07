package br.com.gestordriver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestordriver.model.ConfiguracaoUsuario
import br.com.gestordriver.model.HistoricoItemPresentation
import br.com.gestordriver.presentation.PresentationBuilder
import br.com.gestordriver.ui.theme.LocalPaletaApp
import java.time.format.DateTimeFormatter

private val VerdeAcaoDetalhe = Color(0xFF276A63)
private val VermelhoAcaoDetalhe = Color(0xFFB85C4A)
private val CinzaDetalhe = Color(0xFF5D6B73)

/** Layout oficial do card DETALHES DA CORRIDA (altura compacta). */
@Composable
fun DetalhesCorridaSheet(
    item: HistoricoItemPresentation,
    configuracao: ConfiguracaoUsuario,
    onFechar: () -> Unit,
    onExcluir: () -> Unit,
) {
    val paleta = LocalPaletaApp.current
    val forma = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    val lucro = PresentationBuilder.formatarLucroHistorico(item.valorTotal, item.custoCombustivel)
    val consumoAtivo = configuracao.consumoAtivo()
    val precoAtivo = configuracao.precoAtivo()
    val custoCombustivelEstimado = if (item.kmTotal > 0.0 && consumoAtivo > 0.0 && precoAtivo > 0.0) {
        (item.kmTotal / consumoAtivo) * precoAtivo
    } else {
        null
    }
    val gastoCombustivel = custoCombustivelEstimado?.let(PresentationBuilder::formatarGastoHistorico) ?: "—"
    val gastoTotal = PresentationBuilder.formatarGastoHistorico(item.custoCombustivel)
    val custoOperacional = item.custoCombustivel?.let { total ->
        (total - (custoCombustivelEstimado ?: 0.0)).coerceAtLeast(0.0)
    }
    val consumo = PresentationBuilder.formatarLitrosHistorico(item.combustivelEstimado)
    val lucroKm = if (item.kmTotal > 0 && item.custoCombustivel != null) {
        PresentationBuilder.formatarCelulaHistoricoValorPorKm(
            (item.valorTotal - (item.custoCombustivel ?: 0.0)) / item.kmTotal,
        )
    } else {
        "—"
    }
    val dataHora = item.dataHoraRegistro
        ?.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
        ?.uppercase()
        ?: "${item.dataLista} ${item.horaLista}"
    val nota = item.notaPassageiro?.let { String.format("%.2f", it).replace('.', ',') } ?: "—"
    val classe = item.classificacao.name.replace('_', ' ')
    val corClassificacao = runCatching {
        Color(android.graphics.Color.parseColor(item.corClassificacao))
    }.getOrDefault(VerdeAcaoDetalhe)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(paleta.fundoPainel, forma)
            .border(1.dp, paleta.borda, forma)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(36.dp)
                .height(3.dp)
                .background(CinzaDetalhe, RoundedCornerShape(2.dp)),
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "DETALHES DA CORRIDA",
                color = paleta.texto,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center),
            )
            Text(
                text = "X",
                fontSize = 20.sp,
                color = paleta.texto,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable(onClick = onFechar)
                    .padding(4.dp),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(paleta.texto, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = seloDetalhe(item.plataforma),
                    color = paleta.fundoPainel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(text = dataHora, color = paleta.texto, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = tipoCorrida(item.plataforma), color = CinzaDetalhe, fontSize = 11.sp)
                    Text(
                        text = "ACEITA",
                        color = VerdeAcaoDetalhe,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .background(VerdeAcaoDetalhe.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(corClassificacao.copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = nota, color = corClassificacao, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Text(text = classe, color = corClassificacao, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, paleta.borda, RoundedCornerShape(8.dp))
                .background(paleta.fundoCardHistorico, RoundedCornerShape(10.dp))
                .padding(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinhaEnderecoDetalhe("●", VerdeAcaoDetalhe, item.enderecoEmbarque ?: "-")
                LinhaEnderecoDetalhe("●", VermelhoAcaoDetalhe, item.enderecoDestino ?: "-")
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(paleta.fundoMetrica, RoundedCornerShape(8.dp))
                    .border(1.dp, paleta.borda, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "MAPA", color = CinzaDetalhe, fontSize = 10.sp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, paleta.borda, RoundedCornerShape(8.dp)),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                CelulaDetalhe("VALOR", PresentationBuilder.formatarCelulaHistoricoValor(item.valorTotal), paleta.texto, Modifier.weight(1f))
                CelulaDetalhe("R$/KM", PresentationBuilder.formatarCelulaHistoricoValorPorKm(item.valorPorKm), paleta.texto, Modifier.weight(1f))
                CelulaDetalhe("DISTANCIA", PresentationBuilder.formatarDistanciaHistorico(item.kmTotal), paleta.texto, Modifier.weight(1f))
                CelulaDetalhe("TEMPO", PresentationBuilder.formatarTempoHm(item.tempoEstimado), paleta.texto, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                CelulaDetalhe("COMBUSTÍVEL", consumo, paleta.texto, Modifier.weight(1f))
                CelulaDetalhe("CUSTO COMB. EST.", gastoCombustivel, paleta.texto, Modifier.weight(1f))
                CelulaDetalhe("LUCRO", lucro, VerdeAcaoDetalhe, Modifier.weight(1f))
                CelulaDetalhe("LUCRO/KM", lucroKm, VerdeAcaoDetalhe, Modifier.weight(1f))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CelulaInformacaoReal(
                rotulo = "CUSTO TOTAL",
                valor = gastoTotal,
                modifier = Modifier.weight(1f),
            )
            CelulaInformacaoReal(
                rotulo = "CUSTO OPERACIONAL",
                valor = custoOperacional?.let(PresentationBuilder::formatarGastoHistorico) ?: "—",
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            InfoExtraDetalhe("Tipo", tipoCorrida(item.plataforma))
            InfoExtraDetalhe("Pagamento", "Não informado")
            InfoExtraDetalhe("Km passageiro", PresentationBuilder.formatarDistanciaHistorico(item.kmAtePassageiro))
            InfoExtraDetalhe("Km destino", PresentationBuilder.formatarDistanciaHistorico(item.kmViagem))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, paleta.borda, RoundedCornerShape(8.dp))
                    .padding(8.dp),
            ) {
                Text(text = "OBSERVAÇÕES", color = CinzaDetalhe, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = "Nenhuma observação registrada", color = paleta.texto, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text(
                text = "EXCLUIR CORRIDA",
                color = VermelhoAcaoDetalhe,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .border(1.dp, VermelhoAcaoDetalhe, RoundedCornerShape(10.dp))
                    .clickable(onClick = onExcluir)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun CelulaDetalhe(rotulo: String, valor: String, corValor: Color, modifier: Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = rotulo, color = CinzaDetalhe, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = valor, color = corValor, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun CelulaInformacaoReal(rotulo: String, valor: String, modifier: Modifier) {
    val paleta = LocalPaletaApp.current
    Column(
        modifier = modifier
            .background(paleta.fundoMetrica, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(rotulo, color = CinzaDetalhe, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Text(valor, color = paleta.texto, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LinhaEnderecoDetalhe(marca: String, cor: Color, titulo: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(text = marca, color = cor, fontSize = 10.sp, modifier = Modifier.padding(end = 6.dp, top = 2.dp))
        Text(
            text = titulo,
            color = LocalPaletaApp.current.texto,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun InfoExtraDetalhe(rotulo: String, valor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 2.dp)) {
        Text(text = rotulo, color = CinzaDetalhe, fontSize = 8.sp, textAlign = TextAlign.Center, maxLines = 2)
        Text(
            text = valor,
            color = LocalPaletaApp.current.texto,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun seloDetalhe(plataforma: String): String = when {
    plataforma.contains("Uber", ignoreCase = true) -> "U"
    plataforma.contains("99") -> "99"
    else -> "iD"
}

private fun tipoCorrida(plataforma: String): String = when {
    plataforma.contains("Uber", ignoreCase = true) -> "UberX"
    plataforma.contains("99") -> "99"
    plataforma.contains("inDrive", ignoreCase = true) || plataforma.contains("indrive", ignoreCase = true) -> "inDrive"
    else -> plataforma.ifBlank { "Corrida" }
}
