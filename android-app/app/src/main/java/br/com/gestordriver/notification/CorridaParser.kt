package br.com.gestordriver.notification

import br.com.gestordriver.core.Corrida
import br.com.gestordriver.core.ValidadorCorrida

interface NotificationParser {
    fun parse(notification: NotificationData): Corrida
}

class ParserPadrao : NotificationParser {
    override fun parse(notification: NotificationData): Corrida {
        val campos = NotificationExtractor.extrairCamposPadrao(notification.fullText)
        val enderecos = EnderecoExtractor.extrair(notification.fullText)
        return Corrida(
            valorTotal = campos.valorTotal,
            kmAtePassageiro = campos.kmAtePassageiro,
            kmViagem = campos.kmViagem,
            tempoEstimado = campos.tempoEstimado,
            enderecoEmbarque = enderecos.embarque,
            enderecoDestino = enderecos.destino,
            quantidadeParadas = campos.quantidadeParadas,
        )
    }
}

class CorridaParser {
    private val parsers: Map<Plataforma, NotificationParser> = mapOf(
        Plataforma.UBER to ParserPadrao(),
        Plataforma.NOVE_NOVE to ParserPadrao(),
        Plataforma.INDRIVE to ParserPadrao(),
    )

    fun parse(notification: NotificationData): Corrida {
        validarNotification(notification)

        val plataforma = PlatformDetector.detectarOuErro(notification)
        val parser = parsers[plataforma]
            ?: throw UnsupportedPlatform("Parser nao implementado para: ${plataforma.label}.")

        val corrida = parser.parse(notification)
        ValidadorCorrida.validar(corrida)
        return corrida
    }

    fun parseComPlataforma(notification: NotificationData): Pair<Corrida, Plataforma> {
        validarNotification(notification)
        val plataforma = PlatformDetector.detectarOuErro(notification)
        val parser = parsers[plataforma]
            ?: throw UnsupportedPlatform("Parser nao implementado para: ${plataforma.label}.")
        val corrida = parser.parse(notification)
        ValidadorCorrida.validar(corrida)
        return corrida to plataforma
    }

    private fun validarNotification(notification: NotificationData) {
        if (notification.packageName.isBlank()) {
            throw InvalidNotification("package_name nao informado.")
        }
        if (notification.title.isBlank() && notification.text.isBlank()) {
            throw InvalidNotification("Notificacao sem titulo e sem texto.")
        }
    }
}
