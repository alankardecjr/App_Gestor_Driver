package br.com.gestordriver.notification

import android.app.Application
import android.content.Context

/** Liga a leitura da tela só enquanto o monitoramento está ativo. */
object SessaoMonitoramento {
    private const val ARQUIVO = "gestor_driver_monitor"
    private const val CHAVE = "ligado"
    private const val CHAVE_LIXEIRA = "selo_lixeira"

    private var aplicativo: Application? = null

    fun instalar(aplicativo: Application) {
        this.aplicativo = aplicativo
    }

    fun ligada(): Boolean = aplicativo?.let { ligada(it) } ?: false

    fun ligada(contexto: Context): Boolean =
        contexto.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE).getBoolean(CHAVE, false)

    fun definir(ligado: Boolean) {
        val app = aplicativo ?: return
        val editor = app.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE).edit()
        editor.putBoolean(CHAVE, ligado)
        if (!ligado) {
            editor.putBoolean(CHAVE_LIXEIRA, false)
        }
        editor.commit()
        if (!ligado) {
            RideScreenReaderService.desligar()
        }
    }

    fun seloNaLixeira(): Boolean = aplicativo?.let { seloNaLixeira(it) } ?: false

    fun seloNaLixeira(contexto: Context): Boolean =
        contexto.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE).getBoolean(CHAVE_LIXEIRA, false)

    fun definirSeloNaLixeira(naLixeira: Boolean) {
        val app = aplicativo ?: return
        app.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(CHAVE_LIXEIRA, naLixeira)
            .commit()
    }
}
