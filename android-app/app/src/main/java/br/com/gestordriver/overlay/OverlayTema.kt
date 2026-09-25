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
    val pocoIcone: Int,
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
                fundo = Color.parseColor("#0E0E10"),
                fundoPainel = Color.parseColor("#1C1C1E"),
                texto = Color.WHITE,
                secundario = Color.parseColor("#B0B0B6"),
                detalhes = Color.parseColor("#E0E7ED"),
                borda = Color.parseColor("#2E2E32"),
                card = Color.parseColor("#1C1C1E"),
                menu = Color.parseColor("#161618"),
                menuTexto = Color.WHITE,
                metrica = Color.parseColor("#14261C"),
                caixa = Color.parseColor("#2A2A2E"),
                pocoIcone = Color.parseColor("#2A2A2E"),
            )
        } else {
            OverlayCores(
                fundo = Color.parseColor("#F6F7F8"),
                fundoPainel = Color.WHITE,
                texto = Color.parseColor("#111111"),
                secundario = Color.parseColor("#8A8A8E"),
                detalhes = Color.parseColor("#263238"),
                borda = Color.parseColor("#E6E8EC"),
                card = Color.WHITE,
                menu = Color.parseColor("#F6F7F8"),
                menuTexto = Color.parseColor("#111111"),
                metrica = Color.parseColor("#F6F7F8"),
                caixa = Color.parseColor("#F2F3F5"),
                pocoIcone = Color.parseColor("#F2F3F5"),
            )
        }
    }
}
