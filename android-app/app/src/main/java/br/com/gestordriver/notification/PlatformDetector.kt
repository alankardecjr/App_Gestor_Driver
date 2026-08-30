package br.com.gestordriver.notification

enum class Plataforma(val label: String) {
    UBER("Uber"),
    NOVE_NOVE("99"),
    INDRIVE("inDrive"),
    DESCONHECIDA("Desconhecida"),
}

object PlatformDetector {
    internal val packages = mapOf(
        "com.ubercab.driver" to Plataforma.UBER,
        "com.app99.driver" to Plataforma.NOVE_NOVE,
        "com.taxis99.driver" to Plataforma.NOVE_NOVE,
        "com.taxis99" to Plataforma.NOVE_NOVE,
        "sinet.startup.inDriver" to Plataforma.INDRIVE,
        "com.sis.android.indriver" to Plataforma.INDRIVE,
        "com.indrive.android" to Plataforma.INDRIVE,
    )

    fun resolver(packageName: String): Plataforma {
        if (packageName.isBlank()) {
            return Plataforma.DESCONHECIDA
        }
        packages[packageName]?.let { return it }
        val nome = packageName.lowercase()
        return when {
            nome.contains("ubercab.driver") -> Plataforma.UBER
            nome.contains("app99") && nome.contains("driver") -> Plataforma.NOVE_NOVE
            nome.contains("taxis99") && nome.contains("driver") -> Plataforma.NOVE_NOVE
            nome.contains("indrive") -> Plataforma.INDRIVE
            nome.contains("didi") && nome.contains("driver") -> Plataforma.NOVE_NOVE
            else -> Plataforma.DESCONHECIDA
        }
    }

    fun ehSuportada(packageName: String): Boolean =
        resolver(packageName) != Plataforma.DESCONHECIDA

    fun detectar(notification: NotificationData): Plataforma {
        if (notification.packageName.isBlank()) {
            throw InvalidNotification("package_name nao informado.")
        }
        return resolver(notification.packageName)
    }

    fun detectarOuErro(notification: NotificationData): Plataforma {
        val plataforma = detectar(notification)
        if (plataforma == Plataforma.DESCONHECIDA) {
            throw UnsupportedPlatform(
                "Plataforma nao suportada: ${notification.packageName}.",
            )
        }
        return plataforma
    }
}
