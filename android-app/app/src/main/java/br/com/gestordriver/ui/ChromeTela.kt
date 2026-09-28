package br.com.gestordriver.ui

import android.graphics.Bitmap
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
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
            BotaoCircular(simbolo = simboloVoltar, onClick = onVoltar, contraste = true)
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
fun BotaoSelo(onClick: () -> Unit, clicavel: Boolean = true) {
    val contexto = LocalContext.current
    val imagem = remember(contexto) {
        val drawable = ContextCompat.getDrawable(contexto, R.mipmap.ic_launcher_round) ?: return@remember null
        drawable.toBitmap(
            width = drawable.intrinsicWidth.coerceAtLeast(1),
            height = drawable.intrinsicHeight.coerceAtLeast(1),
            config = Bitmap.Config.ARGB_8888,
        ).asImageBitmap()
    }
    if (imagem != null) {
        Image(
            bitmap = imagem,
            contentDescription = "Gestor Driver",
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .then(if (clicavel) Modifier.clickable(onClick = onClick) else Modifier),
        )
    }
}

@Composable
fun BotaoCircular(
    simbolo: String,
    onClick: () -> Unit,
    perigo: Boolean = false,
    contraste: Boolean = false,
) {
    val paleta = LocalPaletaApp.current
    val escuro = paleta.fundo.red < 0.2f
    val cor = if (perigo) Color(0xFFC62828) else paleta.texto
    val fundo = when {
        perigo -> Color(0x33C62828)
        contraste && escuro -> Color(0xFF4A4A52)
        contraste -> Color(0xFFC4C4CA)
        escuro -> paleta.pocoIcone
        else -> Color(0xFFD6D7DC)
    }
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
