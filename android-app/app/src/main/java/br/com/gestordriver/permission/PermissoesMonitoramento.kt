package br.com.gestordriver.permission

import android.content.Context
import android.provider.Settings
import android.text.TextUtils

object PermissoesMonitoramento {
    fun overlayConcedida(context: Context): Boolean =
        Settings.canDrawOverlays(context)

    fun listenerNotificacoesAtivo(context: Context): Boolean {
        val habilitados = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ).orEmpty()
        if (TextUtils.isEmpty(habilitados)) {
            return false
        }
        val componente = context.packageName
        return habilitados.split(":").any { it.contains(componente) }
    }

    fun todasConcedidas(context: Context): Boolean =
        overlayConcedida(context) && listenerNotificacoesAtivo(context)
}
