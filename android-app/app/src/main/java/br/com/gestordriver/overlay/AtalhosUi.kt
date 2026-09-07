package br.com.gestordriver.overlay

import android.graphics.Color

/**
 * UI oficial Atalhos / aba Opções (claro + escuro) — congelada 06/09/2026.
 * Ícones: símbolo branco sobre quadrado colorido; X neutro (não vermelho).
 */
object AtalhosUi {
    data class ItemCores(
        /** Cor do símbolo (sempre branco no mock oficial). */
        val icone: Int,
        /** Fundo do quadrado arredondado. */
        val fundoIcone: Int,
    )

    data class Tema(
        val painel: Int,
        val item: Int,
        val itemBorda: Int,
        val titulo: Int,
        val subtitulo: Int,
        val botaoXFundo: Int,
        val botaoX: Int,
        val historico: ItemCores,
        val carteira: ItemCores,
        val despesas: ItemCores,
        val semaforo: ItemCores,
        val usuario: ItemCores,
        val configurar: ItemCores,
        val fechar: ItemCores,
    )

    private val iconeBranco = Color.WHITE

    val claro = Tema(
        painel = Color.parseColor("#F3F4F6"),
        item = Color.WHITE,
        itemBorda = Color.parseColor("#E5E7EB"),
        titulo = Color.parseColor("#111827"),
        subtitulo = Color.parseColor("#6B7280"),
        botaoXFundo = Color.parseColor("#E8EAED"),
        botaoX = Color.parseColor("#1F2937"),
        historico = ItemCores(iconeBranco, Color.parseColor("#3B776B")),
        carteira = ItemCores(iconeBranco, Color.parseColor("#3C7180")),
        despesas = ItemCores(iconeBranco, Color.parseColor("#8A6A42")),
        semaforo = ItemCores(iconeBranco, Color.parseColor("#53656A")),
        usuario = ItemCores(iconeBranco, Color.parseColor("#5B6075")),
        configurar = ItemCores(iconeBranco, Color.parseColor("#65747A")),
        fechar = ItemCores(iconeBranco, Color.parseColor("#8B4A45")),
    )

    val escuro = Tema(
        painel = Color.parseColor("#0F1115"),
        item = Color.parseColor("#1A1D23"),
        itemBorda = Color.parseColor("#2A2E36"),
        titulo = Color.WHITE,
        subtitulo = Color.parseColor("#9CA3AF"),
        botaoXFundo = Color.parseColor("#2A2E36"),
        botaoX = Color.WHITE,
        historico = ItemCores(iconeBranco, Color.parseColor("#4D9183")),
        carteira = ItemCores(iconeBranco, Color.parseColor("#4D8796")),
        despesas = ItemCores(iconeBranco, Color.parseColor("#A98452")),
        semaforo = ItemCores(iconeBranco, Color.parseColor("#6D8187")),
        usuario = ItemCores(iconeBranco, Color.parseColor("#777D99")),
        configurar = ItemCores(iconeBranco, Color.parseColor("#819399")),
        fechar = ItemCores(iconeBranco, Color.parseColor("#B56C64")),
    )

    fun de(escuro: Boolean): Tema = if (escuro) this.escuro else claro
}
