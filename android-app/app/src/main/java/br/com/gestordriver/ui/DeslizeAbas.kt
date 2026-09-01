package br.com.gestordriver.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun TituloComSetas(
    titulo: String,
    onEsquerda: () -> Unit,
    onDireita: () -> Unit,
    corTitulo: Color = Color.White,
) {
    val toqueEsquerda = remember { MutableInteractionSource() }
    val toqueDireita = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "⬅️",
            color = Color.Unspecified,
            fontSize = 14.sp,
            modifier = Modifier
                .clickable(
                    interactionSource = toqueEsquerda,
                    indication = null,
                    onClick = onEsquerda,
                )
                .padding(horizontal = 4.dp, vertical = 2.dp),
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = titulo,
                color = corTitulo,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = "➡️",
            color = Color.Unspecified,
            fontSize = 14.sp,
            modifier = Modifier
                .clickable(
                    interactionSource = toqueDireita,
                    indication = null,
                    onClick = onDireita,
                )
                .padding(horizontal = 4.dp, vertical = 2.dp),
        )
    }
}

fun Modifier.barraRolagemAoToque(estado: ScrollState): Modifier = composed {
    var alvo by remember { mutableFloatStateOf(0f) }
    val alpha by animateFloatAsState(
        targetValue = alvo,
        animationSpec = tween(durationMillis = if (alvo > 0f) 80 else 400),
        label = "barraRolagem",
    )
    LaunchedEffect(estado.isScrollInProgress) {
        if (estado.isScrollInProgress) {
            alvo = 1f
        } else {
            delay(800)
            alvo = 0f
        }
    }
    drawWithContent {
        drawContent()
        if (alpha <= 0.02f) {
            return@drawWithContent
        }
        val viewport = size.height
        val conteudo = estado.maxValue.toFloat() + viewport
        if (conteudo <= viewport + 1f) {
            return@drawWithContent
        }
        val largura = 4.dp.toPx()
        val margem = 2.dp.toPx()
        val minimo = 24.dp.toPx()
        val polegar = (viewport * viewport / conteudo).coerceIn(minimo, viewport)
        val percurso = (viewport - polegar).coerceAtLeast(0f)
        val y = if (estado.maxValue == 0) 0f else percurso * (estado.value / estado.maxValue.toFloat())
        drawRoundRect(
            color = Color(0xFFB8C5D1).copy(alpha = alpha),
            topLeft = Offset(size.width - largura - margem, y),
            size = Size(largura, polegar),
            cornerRadius = CornerRadius(largura / 2f, largura / 2f),
        )
    }
}

fun Modifier.deslizeHorizontalAbas(
    aba: Int,
    total: Int,
    onMudar: (Int) -> Unit,
): Modifier = pointerInput(aba, total) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val inicioY = down.position.y
        val inicioX = down.position.x
        val inicioMs = System.currentTimeMillis()
        var ultimoY = inicioY
        var ultimoX = inicioX
        while (true) {
            val evento = awaitPointerEvent(PointerEventPass.Final)
            val mudanca = evento.changes.firstOrNull() ?: break
            ultimoY = mudanca.position.y
            ultimoX = mudanca.position.x
            if (!mudanca.pressed) {
                val dy = ultimoY - inicioY
                val dx = ultimoX - inicioX
                val dt = (System.currentTimeMillis() - inicioMs).coerceAtLeast(1L)
                val velocidade = dx / dt * 1000f
                if (abs(velocidade) > 750f && abs(dx) > abs(dy)) {
                    if (velocidade < 0f) {
                        onMudar((aba + 1).coerceAtMost(total - 1))
                    } else {
                        onMudar((aba - 1).coerceAtLeast(0))
                    }
                }
                break
            }
        }
    }
}
