package br.com.gestordriver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestordriver.core.Classificacao
import br.com.gestordriver.core.ClassificacaoConstantes
import br.com.gestordriver.core.FaixasClassificacao
import br.com.gestordriver.ui.theme.LocalPaletaApp

private val AlturaToque = 48.dp
private val FonteAjuda = 12.sp

@Composable
fun SemaforoTela(
    viewModel: ConfiguracoesViewModel,
    onVoltar: () -> Unit,
) {
    val configuracao = viewModel.configuracao
    val paleta = LocalPaletaApp.current
    val forma = RoundedCornerShape(10.dp)
    val rolagem = rememberScrollState()
    val corRuim = Color(android.graphics.Color.parseColor(ClassificacaoConstantes.CORES.getValue(Classificacao.RUIM)))
    val corBoa = Color(android.graphics.Color.parseColor(ClassificacaoConstantes.CORES.getValue(Classificacao.BOA)))
    val corOtima = Color(android.graphics.Color.parseColor(ClassificacaoConstantes.CORES.getValue(Classificacao.EXCELENTE)))
    var ajudaKm by remember { mutableStateOf(false) }
    var ajudaHora by remember { mutableStateOf(false) }
    var ajudaNota by remember { mutableStateOf(false) }

    fun fecharDescartando() {
        viewModel.cancelar()
        onVoltar()
    }

    fun fecharSalvando() {
        viewModel.salvar(aplicarAbastecimento = false)
        onVoltar()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(paleta.fundoPainel, forma)
            .border(2.dp, paleta.borda, forma),
    ) {
        CabecalhoTelaNativa(
            titulo = "Semáforo",
            onVoltar = { fecharDescartando() },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
                .barraRolagemAoToque(rolagem)
                .verticalScroll(rolagem)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Calibrar faixas",
                color = paleta.textoSecundario,
                fontSize = 13.sp,
            )
            Text(
                text = "Defina suas metas para classificar as corridas por cor:",
                color = paleta.textoSecundario,
                fontSize = 13.sp,
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LegendaSemaforoLinha(corRuim, "Vermelho para corridas ruins")
                LegendaSemaforoLinha(corBoa, "Amarelo para corridas intermediárias")
                LegendaSemaforoLinha(corOtima, "Verde para corridas boas")
            }

            CartaoSemaforo(
                icone = "$",
                titulo = "Ganhos por Km",
                ajudaAberta = ajudaKm,
                onAjuda = { ajudaKm = !ajudaKm },
                textoAjuda = "Arraste as marcas. Abaixo de R$ ${FaixasClassificacao.formatar(configuracao.limiteRuimMax)}/km = ruim; " +
                    "de R$ ${FaixasClassificacao.formatar(configuracao.limiteBoaMin)} a ${FaixasClassificacao.formatar(configuracao.limiteBoaMax)}/km = intermediária; " +
                    "a partir de R$ ${FaixasClassificacao.formatar(configuracao.limiteOtimaMin)}/km = ótima. " +
                    "Max de uma faixa define o min da próxima em +R$ 0,01 (como −/+).",
                baixo = configuracao.limiteRuimMax.toFloat(),
                alto = configuracao.limiteBoaMax.toFloat(),
                faixa = 0f..5f,
                formatar = { "R$ ${FaixasClassificacao.formatar(it.toDouble())} /km" },
                corBaixo = corRuim,
                corAlto = corOtima,
                onMudar = { a, b -> viewModel.atualizarMarcasDeslizantes(a.toDouble(), b.toDouble()) },
            )
            CartaoSemaforo(
                icone = "$",
                titulo = "Ganhos por Hora",
                ajudaAberta = ajudaHora,
                onAjuda = { ajudaHora = !ajudaHora },
                textoAjuda = "Meta de faturamento por hora. Vermelho até R$ ${"%.0f".format(configuracao.limiteHoraRuimMax)}/Hr; " +
                    "verde a partir de R$ ${"%.0f".format(configuracao.limiteHoraBoaMax)}/Hr.",
                baixo = configuracao.limiteHoraRuimMax.toFloat(),
                alto = configuracao.limiteHoraBoaMax.toFloat(),
                faixa = 0f..120f,
                formatar = { "R$ ${"%.0f".format(it)} /Hr" },
                corBaixo = corRuim,
                corAlto = corOtima,
                onMudar = { a, b -> viewModel.atualizarMarcasHora(a.toDouble(), b.toDouble()) },
            )
            CartaoSemaforo(
                icone = "★",
                titulo = "Nota do passageiro",
                ajudaAberta = ajudaNota,
                onAjuda = { ajudaNota = !ajudaNota },
                textoAjuda = "Nota mínima desejada. Vermelho até ${FaixasClassificacao.formatar(configuracao.limiteNotaRuimMax)}; " +
                    "verde a partir de ${FaixasClassificacao.formatar(configuracao.limiteNotaBoaMax)}.",
                baixo = configuracao.limiteNotaRuimMax.toFloat(),
                alto = configuracao.limiteNotaBoaMax.toFloat(),
                faixa = 0f..5f,
                formatar = { FaixasClassificacao.formatar(it.toDouble()) },
                corBaixo = corRuim,
                corAlto = corOtima,
                onMudar = { a, b -> viewModel.atualizarMarcasNota(a.toDouble(), b.toDouble()) },
            )
        }

        BarraCancelarSalvar(
            onCancelar = { fecharDescartando() },
            onSalvar = { fecharSalvando() },
        )
    }
}

@Composable
private fun LegendaSemaforoLinha(cor: Color, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.size(10.dp).background(cor, CircleShape))
        Text(text = texto, color = LocalPaletaApp.current.texto, fontSize = 13.sp)
    }
}

@Composable
private fun CartaoSemaforo(
    icone: String,
    titulo: String,
    ajudaAberta: Boolean,
    onAjuda: () -> Unit,
    textoAjuda: String,
    baixo: Float,
    alto: Float,
    faixa: ClosedFloatingPointRange<Float>,
    formatar: (Float) -> String,
    corBaixo: Color,
    corAlto: Color,
    onMudar: (Float, Float) -> Unit,
) {
    val paleta = LocalPaletaApp.current
    val forma = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(paleta.fundoCardHistorico, forma)
            .border(1.dp, paleta.borda, forma)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(0xFFFFE0B2), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = icone, color = Color(0xFFE65100), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Text(
                text = titulo,
                color = paleta.texto,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            )
            Text(
                text = "? AJUDA",
                color = paleta.fundoPainel,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .background(paleta.texto, RoundedCornerShape(12.dp))
                    .clickable(onClick = onAjuda)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
        if (ajudaAberta) {
            Text(text = textoAjuda, color = paleta.textoSecundario, fontSize = FonteAjuda)
        }
        SemaforoDualSlider(
            baixo = baixo,
            alto = alto,
            faixa = faixa,
            formatar = formatar,
            corBaixo = corBaixo,
            corAlto = corAlto,
            onMudar = onMudar,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SemaforoDualSlider(
    baixo: Float,
    alto: Float,
    faixa: ClosedFloatingPointRange<Float>,
    formatar: (Float) -> String,
    corBaixo: Color,
    corAlto: Color,
    onMudar: (Float, Float) -> Unit,
) {
    val corMeio = Color(android.graphics.Color.parseColor(ClassificacaoConstantes.CORES.getValue(Classificacao.BOA)))
    // Mesmo vão mínimo dos campos −/+: 0,01 entre marcas (max → min da próxima).
    val minGap = FaixasClassificacao.PASSO.toFloat()
    fun snap(valor: Float): Float =
        (kotlin.math.round(valor * 100f) / 100f).coerceIn(faixa.start, faixa.endInclusive)
    val baixoSeguro = snap(baixo).coerceIn(faixa.start, faixa.endInclusive - minGap)
    val altoSeguro = snap(alto).coerceIn(baixoSeguro + minGap, faixa.endInclusive)
    val span = (faixa.endInclusive - faixa.start).coerceAtLeast(0.01f)
    val pesoRuim = ((baixoSeguro - faixa.start) / span).coerceIn(0.02f, 0.96f)
    val pesoMeio = ((altoSeguro - baixoSeguro) / span).coerceIn(0.02f, 0.96f)
    val pesoBom = ((faixa.endInclusive - altoSeguro) / span).coerceIn(0.02f, 0.96f)
    val fracaoBaixo = ((baixoSeguro - faixa.start) / span).coerceIn(0f, 1f)
    val fracaoAlto = ((altoSeguro - faixa.start) / span).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.fillMaxWidth().heightIn(min = 28.dp)) {
            Text(
                text = formatar(altoSeguro),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(fracaoAlto.coerceAtLeast(0.08f))
                    .wrapContentWidth(Alignment.End)
                    .background(corAlto, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .height(10.dp)
                    .padding(horizontal = 10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(pesoRuim)
                        .fillMaxSize()
                        .background(corBaixo, RoundedCornerShape(topStart = 5.dp, bottomStart = 5.dp)),
                )
                Box(modifier = Modifier.weight(pesoMeio).fillMaxSize().background(corMeio))
                Box(
                    modifier = Modifier
                        .weight(pesoBom)
                        .fillMaxSize()
                        .background(corAlto, RoundedCornerShape(topEnd = 5.dp, bottomEnd = 5.dp)),
                )
            }
            RangeSlider(
                value = baixoSeguro..altoSeguro,
                onValueChange = { range ->
                    val novoBaixo = snap(range.start).coerceIn(faixa.start, faixa.endInclusive - minGap)
                    val novoAlto = snap(range.endInclusive).coerceIn(novoBaixo + minGap, faixa.endInclusive)
                    onMudar(novoBaixo, novoAlto)
                },
                valueRange = faixa,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth().heightIn(min = AlturaToque),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Ruim", color = corBaixo, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("Boa", color = corMeio, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("Ótima", color = corAlto, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(modifier = Modifier.fillMaxWidth().heightIn(min = 28.dp)) {
            Text(
                text = formatar(baixoSeguro),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(fracaoBaixo.coerceAtLeast(0.08f))
                    .wrapContentWidth(Alignment.End)
                    .background(corBaixo, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}
