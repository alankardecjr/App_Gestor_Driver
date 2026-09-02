package br.com.gestordriver.overlay

/**
 * Home, Recents e launcher — o overlay não recebe esses botões
 * se a janela for FLAG_NOT_FOCUSABLE.
 */
object BarraSistema {
    private val pacotesHome = listOf(
        "com.sec.android.app.launcher",
        "com.android.launcher3",
        "com.google.android.apps.nexuslauncher",
        "com.miui.home",
        "com.huawei.android.launcher",
        "com.motorola.launcher3",
    )

    fun deveRecolherParaSelo(
        pacote: String,
        classe: String = "",
        tipoJanelaAlterada: Boolean,
    ): Boolean {
        if (!tipoJanelaAlterada || pacote.isBlank()) {
            return false
        }
        if (pacotesHome.any { pacote.equals(it, ignoreCase = true) }) {
            return true
        }
        val nome = classe.lowercase()
        if (pacote == "com.android.systemui" &&
            (nome.contains("recents") || nome.contains("overview") || nome.contains("launcher"))
        ) {
            return true
        }
        return nome.contains("recentsactivity") || nome.contains("overviewactivity")
    }
}
