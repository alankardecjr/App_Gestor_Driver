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
) {
    companion object {
        val escura = PaletaApp(
            fundo = Color(0xFF10161D),
            fundoPainel = Color(0xF2050809),
            borda = Color(0xFF607D8B),
            texto = Color.White,
            textoSecundario = Color(0xFFC5D3DE),
            textoDetalhes = Color(0xFFE0E7ED),
            fundoCardHistorico = Color(0xFF1A2228),
            fundoMetrica = Color(0xFF14261C),
            fundoCaixa = Color(0x33000000),
        )
        val clara = PaletaApp(
            fundo = Color(0xFFF4F6F8),
            fundoPainel = Color(0xFFFFFFFF),
            borda = Color(0xFF90A4AE),
            texto = Color(0xFF0D1B22),
            textoSecundario = Color(0xFF37474F),
            textoDetalhes = Color(0xFF263238),
            fundoCardHistorico = Color(0xFFFFFFFF),
            fundoMetrica = Color(0xFFE8F5E9),
            fundoCaixa = Color(0x14000000),
        )

        fun de(escuro: Boolean): PaletaApp = if (escuro) escura else clara
    }
}

val LocalPaletaApp = staticCompositionLocalOf { PaletaApp.escura }
