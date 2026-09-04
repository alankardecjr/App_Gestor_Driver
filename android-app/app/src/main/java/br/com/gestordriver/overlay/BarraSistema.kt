package br.com.gestordriver.overlay

/**
 * Home, Recentes e launcher — detectados pela acessibilidade.
 * O overlay não deve consumir esses botões (padrão do celular).
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

    fun ehHome(
        pacote: String,
        classe: String = "",
        tipoJanelaAlterada: Boolean,
    ): Boolean {
        if (!tipoJanelaAlterada || pacote.isBlank() || ehRecentes(pacote, classe, true)) {
            return false
        }
        if (pacotesHome.any { pacote.equals(it, ignoreCase = true) }) {
            return true
        }
        val nome = classe.lowercase()
        return pacote == "com.android.systemui" && nome.contains("launcher")
    }

    fun ehRecentes(
        pacote: String,
        classe: String = "",
        tipoJanelaAlterada: Boolean,
    ): Boolean {
        if (!tipoJanelaAlterada || pacote.isBlank()) {
            return false
        }
        val nome = classe.lowercase()
        if (
            nome.contains("recents") ||
            nome.contains("overview") ||
            nome.contains("quickstep") ||
            nome.contains("taskview")
        ) {
            return true
        }
        return pacote == "com.android.systemui" &&
            (nome.contains("recents") || nome.contains("overview") || nome.contains("quickstep"))
    }

    fun deveRecolherParaSelo(
        pacote: String,
        classe: String = "",
        tipoJanelaAlterada: Boolean,
    ): Boolean = ehHome(pacote, classe, tipoJanelaAlterada)
}
