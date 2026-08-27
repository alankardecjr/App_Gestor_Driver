package br.com.gestordriver.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import br.com.gestordriver.model.AppNavegacao

object NavegacaoLauncher {
    private const val PACOTE_MAPS = "com.google.android.apps.maps"
    private const val PACOTE_WAZE = "com.waze"

    fun abrir(
        context: Context,
        navegacao: AppNavegacao,
        embarque: String?,
        destino: String?,
        corridaAceita: Boolean,
    ) {
        val alvo = destinoNavegacao(embarque, destino, corridaAceita)
        if (alvo == null) {
            Toast.makeText(
                context,
                "Endereço não veio na notificação. O log vai ajudar a calibrar.",
                Toast.LENGTH_LONG,
            ).show()
            return
        }
        val uri = when (navegacao) {
            AppNavegacao.GOOGLE_MAPS -> uriMaps(embarque, destino, corridaAceita, alvo)
            AppNavegacao.WAZE -> Uri.parse(
                "https://waze.com/ul?q=${Uri.encode(alvo)}&navigate=yes",
            )
        }
        val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching {
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(
                context,
                "Não foi possível abrir o app de navegação.",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    fun abrirAplicativo(
        context: Context,
        navegacao: AppNavegacao,
    ) {
        val pacote = when (navegacao) {
            AppNavegacao.GOOGLE_MAPS -> PACOTE_MAPS
            AppNavegacao.WAZE -> PACOTE_WAZE
        }
        val nome = if (navegacao == AppNavegacao.WAZE) "Waze" else "Google Maps"
        val launch = context.packageManager.getLaunchIntentForPackage(pacote)
        if (launch == null) {
            Toast.makeText(
                context,
                "$nome não está instalado neste celular.",
                Toast.LENGTH_LONG,
            ).show()
            return
        }
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching {
            context.startActivity(launch)
        }.onFailure {
            Toast.makeText(
                context,
                "Não foi possível abrir o $nome.",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    fun destinoNavegacao(
        embarque: String?,
        destino: String?,
        corridaAceita: Boolean,
    ): String? {
        if (corridaAceita) {
            return destino?.takeIf { it.isNotBlank() } ?: embarque?.takeIf { it.isNotBlank() }
        }
        return embarque?.takeIf { it.isNotBlank() } ?: destino?.takeIf { it.isNotBlank() }
    }

    private fun uriMaps(
        embarque: String?,
        destino: String?,
        corridaAceita: Boolean,
        alvo: String,
    ): Uri {
        val origem = embarque?.takeIf { it.isNotBlank() }
        val chegada = destino?.takeIf { it.isNotBlank() }
        if (corridaAceita && origem != null && chegada != null) {
            return Uri.parse(
                "https://www.google.com/maps/dir/?api=1" +
                    "&origin=${Uri.encode(origem)}" +
                    "&destination=${Uri.encode(chegada)}" +
                    "&travelmode=driving",
            )
        }
        return Uri.parse(
            "https://www.google.com/maps/dir/?api=1" +
                "&destination=${Uri.encode(alvo)}" +
                "&travelmode=driving",
        )
    }
}
