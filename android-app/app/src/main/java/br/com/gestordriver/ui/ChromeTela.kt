package br.com.gestordriver.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestordriver.R
import br.com.gestordriver.ui.theme.LocalPaletaApp

@Composable
fun CabecalhoTela(
    titulo: String,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    icone: String? = null,
    simboloVoltar: String = "←",
    mostrarVoltar: Boolean = true,
    inicio: (@Composable () -> Unit)? = null,
    acao: (@Composable () -> Unit)? = null,
) {
    val paleta = LocalPaletaApp.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (inicio != null) {
            inicio()
        } else if (mostrarVoltar) {
            BotaoCircular(simbolo = simboloVoltar, onClick = onVoltar)
        }
        if (!icone.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(40.dp)
                    .background(paleta.pocoIcone, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = icone, fontSize = 16.sp)
            }
        }
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
fun BotaoSelo(onClick: () -> Unit) {
    Image(
        painter = painterResource(R.mipmap.ic_launcher_round),
        contentDescription = "Selo",
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
    )
}

@Composable
fun BotaoCircular(
    simbolo: String,
    onClick: () -> Unit,
    perigo: Boolean = false,
) {
    val paleta = LocalPaletaApp.current
    val cor = if (perigo) Color(0xFFC62828) else paleta.texto
    val fundo = if (perigo) Color(0x33C62828) else paleta.pocoIcone
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
