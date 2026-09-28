package br.com.gestordriver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestordriver.ui.theme.LocalPaletaApp

private val VerdeAcao = Color(0xFF7CB342)
private val VermelhoAcao = Color(0xFFE53935)
private val FormaBotao = RoundedCornerShape(12.dp)

@Composable
fun BotoesMensagem(
    textoDireita: String,
    onDireita: () -> Unit,
    modifier: Modifier = Modifier,
    textoEsquerda: String = "Cancelar",
    onEsquerda: () -> Unit = {},
    mostrarEsquerda: Boolean = true,
    direitaHabilitada: Boolean = true,
    direitaPerigo: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (mostrarEsquerda) {
            BotaoMensagem(
                texto = textoEsquerda,
                onClick = onEsquerda,
                destaque = false,
                perigo = false,
                habilitado = true,
                modifier = Modifier.weight(1f),
            )
        }
        BotaoMensagem(
            texto = textoDireita,
            onClick = onDireita,
            destaque = true,
            perigo = direitaPerigo,
            habilitado = direitaHabilitada,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun BotaoMensagem(
    texto: String,
    onClick: () -> Unit,
    destaque: Boolean,
    perigo: Boolean,
    habilitado: Boolean,
    modifier: Modifier = Modifier,
) {
    val paleta = LocalPaletaApp.current
    val cor = when {
        !habilitado -> paleta.textoSecundario
        perigo -> VermelhoAcao
        destaque -> VerdeAcao
        else -> paleta.texto
    }
    val fundo = when {
        !habilitado -> paleta.pocoIcone
        perigo -> VermelhoAcao.copy(alpha = 0.16f)
        destaque -> VerdeAcao.copy(alpha = 0.20f)
        else -> paleta.pocoIcone
    }
    val borda = when {
        perigo && habilitado -> VermelhoAcao.copy(alpha = 0.45f)
        destaque && habilitado -> VerdeAcao.copy(alpha = 0.45f)
        else -> paleta.borda
    }
    Text(
        text = texto,
        color = cor,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = modifier
            .alpha(if (habilitado) 1f else 0.55f)
            .heightIn(min = 44.dp)
            .background(fundo, FormaBotao)
            .border(1.dp, borda, FormaBotao)
            .clickable(enabled = habilitado, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    )
}
