package br.com.gestordriver.notification

import android.content.Context

object PlataformasMotorista {
    val pacotesUber = listOf("com.ubercab.driver")
    val pacotes99 = listOf(
        "com.app99.driver",
        "com.taxis99.driver",
        "com.taxis99",
    )
    val pacotesInDrive = listOf(
        "sinet.startup.inDriver",
        "com.sis.android.indriver",
        "com.indrive.android",
    )

    fun todosOsPacotes(): List<String> =
        pacotesUber + pacotes99 + pacotesInDrive

    fun instalada(context: Context, plataforma: Plataforma): Boolean {
        val pacotes = when (plataforma) {
            Plataforma.UBER -> pacotesUber
            Plataforma.NOVE_NOVE -> pacotes99
            Plataforma.INDRIVE -> pacotesInDrive
            Plataforma.DESCONHECIDA -> emptyList()
        }
        return pacotes.any { instalado(context, it) }
    }

    fun instalado(context: Context, packageName: String): Boolean =
        runCatching {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        }.getOrDefault(false)
}
