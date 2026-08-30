package br.com.gestordriver.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

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
