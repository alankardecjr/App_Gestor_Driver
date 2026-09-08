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
import androidx.compose.material3.Slider
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
            onSelo = { fecharDescartando() },
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
                text = "Classifique as corridas somente pelo R$/km:",
                color = paleta.textoSecundario,
                fontSize = 13.sp,
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LegendaSemaforoLinha(corRuim, "Ruim: vermelho")
                LegendaSemaforoLinha(corBoa, "Boa: amarelo")
                LegendaSemaforoLinha(corOtima, "Ótima: verde")
            }

            FaixaClassificacaoSlider(
                titulo = "Ruim",
                descricao = "De R$ 0,00 até o limite escolhido",
                minimo = configuracao.limiteRuimMin,
                maximo = configuracao.limiteRuimMax,
                cor = corRuim,
                onMudarMaximo = { viewModel.atualizarLimiteRuimMax(it.toDouble()) },
            )
            FaixaClassificacaoSlider(
                titulo = "Boa",
                descricao = "Começa automaticamente em R$ ${FaixasClassificacao.formatar(configuracao.limiteBoaMin)}",
                minimo = configuracao.limiteBoaMin,
                maximo = configuracao.limiteBoaMax,
                cor = corBoa,
                onMudarMaximo = { viewModel.atualizarLimiteBoaMax(it.toDouble()) },
            )
            FaixaClassificacaoSlider(
                titulo = "Ótima",
                descricao = "Começa automaticamente em R$ ${FaixasClassificacao.formatar(configuracao.limiteOtimaMin)}",
                minimo = configuracao.limiteOtimaMin,
                maximo = configuracao.limiteOtimaMax,
                cor = corOtima,
                onMudarMaximo = { viewModel.atualizarLimiteOtimaMax(it.toDouble()) },
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
private fun FaixaClassificacaoSlider(
    titulo: String,
    descricao: String,
    minimo: Double,
    maximo: Double,
    cor: Color,
    onMudarMaximo: (Float) -> Unit,
) {
    val paleta = LocalPaletaApp.current
    val forma = RoundedCornerShape(12.dp)
    val limiteMaximo = FaixasClassificacao.MAX_ABSOLUTO.toFloat()
    val minimoSeguro = minimo.toFloat().coerceIn(0f, limiteMaximo)
    val maximoSeguro = maximo.toFloat().coerceIn(minimoSeguro, limiteMaximo)
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
            Box(modifier = Modifier.size(12.dp).background(cor, CircleShape))
            Text(
                text = titulo,
                color = paleta.texto,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            )
        }
        Text(text = descricao, color = paleta.textoSecundario, fontSize = FonteAjuda)
        Text(
            text = "R$ ${FaixasClassificacao.formatar(maximoSeguro.toDouble())} /km",
            color = cor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
        Slider(
            value = maximoSeguro,
            onValueChange = onMudarMaximo,
            valueRange = minimoSeguro..limiteMaximo,
            steps = 9899,
            colors = SliderDefaults.colors(
                thumbColor = cor,
                activeTrackColor = cor,
                inactiveTrackColor = cor.copy(alpha = 0.22f),
            ),
            modifier = Modifier.fillMaxWidth().heightIn(min = AlturaToque),
        )
        Text(
            text = "Máximo ajustável pelo seletor",
            color = paleta.textoSecundario,
            fontSize = 11.sp,
        )
    }
}
