package br.com.gestordriver.permission

import android.content.Context
import android.provider.Settings
import android.text.TextUtils

object PermissoesMonitoramento {
    fun overlayConcedida(context: Context): Boolean =
        Settings.canDrawOverlays(context)

    fun localizacaoConcedida(context: Context): Boolean {
        val fine = android.Manifest.permission.ACCESS_FINE_LOCATION
        val coarse = android.Manifest.permission.ACCESS_COARSE_LOCATION
        return context.checkSelfPermission(fine) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(coarse) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    }

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
