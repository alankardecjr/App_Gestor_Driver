package br.com.gestordriver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestordriver.ui.theme.LocalPaletaApp

@Composable
fun CabecalhoTela(
    titulo: String,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    simboloVoltar: String = "←",
    acao: (@Composable () -> Unit)? = null,
) {
    val paleta = LocalPaletaApp.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BotaoCircular(simbolo = simboloVoltar, onClick = onVoltar)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 8.dp),
        ) {
            Text(
                text = titulo,
                color = paleta.texto,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            if (!subtitulo.isNullOrBlank()) {
                Text(
                    text = subtitulo,
                    color = paleta.textoSecundario,
                    fontSize = 12.sp,
                    maxLines = 1,
                )
            }
        }
        acao?.invoke()
    }
}

@Composable
fun BotaoCircular(
    simbolo: String,
    onClick: () -> Unit,
    perigo: Boolean = false,
) {
    val paleta = LocalPaletaApp.current
    val cor = if (perigo) Color(0xFFE53935) else paleta.texto
    val fundo = if (perigo) Color(0x33E53935) else paleta.pocoIcone
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(fundo, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = simbolo, color = cor, fontSize = 16.sp)
    }
}
