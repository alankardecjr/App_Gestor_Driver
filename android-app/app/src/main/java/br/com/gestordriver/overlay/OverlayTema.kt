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
    val menuItem: Int,
    val menuItemBorda: Int,
    val menuIconeFundo: Int,
    val menuIcone: Int,
    val menuFecharFundo: Int,
    val menuFecharIcone: Int,
    val menuBotaoFecharFundo: Int,
    val metrica: Int,
    val caixa: Int,
    val escuro: Boolean,
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
                secundario = Color.parseColor("#9CA3AF"),
                detalhes = Color.parseColor("#E0E7ED"),
                borda = Color.parseColor("#2C323A"),
                card = Color.parseColor("#1A2228"),
                menu = Color.parseColor("#0F1115"),
                menuTexto = Color.WHITE,
                menuItem = Color.parseColor("#1A1D23"),
                menuItemBorda = Color.parseColor("#2A2E36"),
                menuIconeFundo = Color.parseColor("#2A2E36"),
                menuIcone = Color.parseColor("#F3F4F6"),
                menuFecharFundo = Color.parseColor("#3A2226"),
                menuFecharIcone = Color.parseColor("#EF5350"),
                menuBotaoFecharFundo = Color.parseColor("#2A2E36"),
                metrica = Color.parseColor("#14261C"),
                caixa = Color.parseColor("#33000000"),
                escuro = true,
            )
        } else {
            OverlayCores(
                fundo = Color.parseColor("#F4F6F8"),
                fundoPainel = Color.WHITE,
                texto = Color.parseColor("#0D1B22"),
                secundario = Color.parseColor("#6B7280"),
                detalhes = Color.parseColor("#263238"),
                borda = Color.parseColor("#E5E7EB"),
                card = Color.WHITE,
                menu = Color.parseColor("#F3F4F6"),
                menuTexto = Color.parseColor("#111827"),
                menuItem = Color.WHITE,
                menuItemBorda = Color.parseColor("#E5E7EB"),
                menuIconeFundo = Color.parseColor("#EEF0F2"),
                menuIcone = Color.parseColor("#1F2937"),
                menuFecharFundo = Color.parseColor("#FDECEC"),
                menuFecharIcone = Color.parseColor("#E53935"),
                menuBotaoFecharFundo = Color.parseColor("#E8EAED"),
                metrica = Color.parseColor("#E8F5E9"),
                caixa = Color.parseColor("#14000000"),
                escuro = false,
            )
        }
    }
}
