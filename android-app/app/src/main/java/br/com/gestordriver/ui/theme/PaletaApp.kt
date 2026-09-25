package br.com.gestordriver.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class PaletaApp(
    val fundo: Color,
    val fundoPainel: Color,
    val borda: Color,
    val texto: Color,
    val textoSecundario: Color,
    val textoDetalhes: Color,
    val fundoCardHistorico: Color,
    val fundoMetrica: Color,
    val fundoCaixa: Color,
    val pocoIcone: Color,
) {
    companion object {
        val escura = PaletaApp(
            fundo = Color(0xFF0E0E10),
            fundoPainel = Color(0xFF1C1C1E),
            borda = Color(0xFF2E2E32),
            texto = Color.White,
            textoSecundario = Color(0xFFB0B0B6),
            textoDetalhes = Color(0xFFE0E7ED),
            fundoCardHistorico = Color(0xFF1C1C1E),
            fundoMetrica = Color(0xFF14261C),
            fundoCaixa = Color(0xFF2A2A2E),
            pocoIcone = Color(0xFF2A2A2E),
        )
        val clara = PaletaApp(
            fundo = Color(0xFFF6F7F8),
            fundoPainel = Color(0xFFFFFFFF),
            borda = Color(0xFFE6E8EC),
            texto = Color(0xFF111111),
            textoSecundario = Color(0xFF8A8A8E),
            textoDetalhes = Color(0xFF263238),
            fundoCardHistorico = Color(0xFFFFFFFF),
            fundoMetrica = Color(0xFFF6F7F8),
            fundoCaixa = Color(0xFFF2F3F5),
            pocoIcone = Color(0xFFF2F3F5),
        )

        fun de(escuro: Boolean): PaletaApp = if (escuro) escura else clara
    }
}

val LocalPaletaApp = staticCompositionLocalOf { PaletaApp.escura }
