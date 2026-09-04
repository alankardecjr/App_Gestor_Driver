package br.com.gestordriver.overlay

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import br.com.gestordriver.GestorDriverApp
import br.com.gestordriver.model.TemaApp

data class OverlayCores(
    val fundo: Int,
    val fundoPainel: Int,
    val texto: Int,
    val secundario: Int,
    val detalhes: Int,
    val borda: Int,
    val card: Int,
    val menu: Int,
    val menuTexto: Int,
    val metrica: Int,
    val caixa: Int,
)

/** Mesmas cores da paleta Compose (claro/escuro do telefone). */
object OverlayTema {
    fun de(context: Context): OverlayCores {
        val preferencia = (context.applicationContext as? GestorDriverApp)
            ?.configuracaoStore
            ?.carregar()
            ?.tema
            ?: TemaApp.CELULAR
        val noiteSistema = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val escuro = when (preferencia) {
            TemaApp.ESCURO -> true
            TemaApp.CLARO -> false
            TemaApp.CELULAR -> noiteSistema
        }
        return if (escuro) {
            OverlayCores(
                fundo = Color.parseColor("#10161D"),
                fundoPainel = Color.parseColor("#F2050809"),
                texto = Color.WHITE,
                secundario = Color.parseColor("#C5D3DE"),
                detalhes = Color.parseColor("#E0E7ED"),
                borda = Color.parseColor("#607D8B"),
                card = Color.parseColor("#1A2228"),
                menu = Color.parseColor("#1A2228"),
                menuTexto = Color.WHITE,
                metrica = Color.parseColor("#14261C"),
                caixa = Color.parseColor("#33000000"),
            )
        } else {
            OverlayCores(
                fundo = Color.parseColor("#F4F6F8"),
                fundoPainel = Color.WHITE,
                texto = Color.parseColor("#0D1B22"),
                secundario = Color.parseColor("#37474F"),
                detalhes = Color.parseColor("#263238"),
                borda = Color.parseColor("#90A4AE"),
                card = Color.WHITE,
                menu = Color.WHITE,
                menuTexto = Color.parseColor("#0D1B22"),
                metrica = Color.parseColor("#E8F5E9"),
                caixa = Color.parseColor("#14000000"),
            )
        }
    }
}
