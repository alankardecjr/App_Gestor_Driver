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
    val escuro: Boolean,
) {
    companion object {
        val escura = PaletaApp(
            fundo = Color(0xFF111719),
            fundoPainel = Color(0xFF1A2224),
            borda = Color(0xFF344246),
            texto = Color(0xFFF3F6F5),
            textoSecundario = Color(0xFFB4C0C2),
            textoDetalhes = Color(0xFFD7E0E0),
            fundoCardHistorico = Color(0xFF202A2C),
            fundoMetrica = Color(0xFF20312F),
            fundoCaixa = Color(0x33111719),
            escuro = true,
        )
        val clara = PaletaApp(
            fundo = Color(0xFFF6F7F8),
            fundoPainel = Color(0xFFFFFFFF),
            borda = Color(0xFFD7DEE2),
            texto = Color(0xFF172126),
            textoSecundario = Color(0xFF5D6B73),
            textoDetalhes = Color(0xFF34434A),
            fundoCardHistorico = Color(0xFFFFFFFF),
            fundoMetrica = Color(0xFFE8F0EF),
            fundoCaixa = Color(0x140D1B22),
            escuro = false,
        )

        fun de(escuro: Boolean): PaletaApp = if (escuro) escura else clara
    }
}

val LocalPaletaApp = staticCompositionLocalOf { PaletaApp.escura }
