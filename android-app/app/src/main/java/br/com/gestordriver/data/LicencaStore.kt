package br.com.gestordriver.data

import android.content.Context

interface LicencaStore {
    fun proLiberado(): Boolean
    fun liberarPro()
}

class PreferencesLicencaStore(context: Context) : LicencaStore {
    private val prefs = context.getSharedPreferences("gestor_driver_licenca", Context.MODE_PRIVATE)

    override fun proLiberado(): Boolean = prefs.getBoolean(CHAVE, false)

    override fun liberarPro() {
        prefs.edit().putBoolean(CHAVE, true).commit()
    }

    private companion object {
        const val CHAVE = "pro_liberada"
    }
}

class MemoriaLicencaStore(liberada: Boolean = false) : LicencaStore {
    private var liberada = liberada

    override fun proLiberado(): Boolean = liberada

    override fun liberarPro() {
        liberada = true
    }
}
