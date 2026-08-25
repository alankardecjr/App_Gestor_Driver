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
        "com.taxis99.driver" to Plataforma.NOVE_NOVE,
        "sinet.startup.inDriver" to Plataforma.INDRIVE,
    )

    fun detectar(notification: NotificationData): Plataforma {
        if (notification.packageName.isBlank()) {
            throw InvalidNotification("package_name nao informado.")
        }
        return packages[notification.packageName] ?: Plataforma.DESCONHECIDA
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
