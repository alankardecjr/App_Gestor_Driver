package br.com.gestordriver.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import br.com.gestordriver.notification.NotificationDiagnosticLog

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
        return habilitados.split(":").any { it.contains(context.packageName) }
    }

    fun acessibilidadeAtiva(context: Context): Boolean {
        val habilitados = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ).orEmpty()
        if (TextUtils.isEmpty(habilitados)) {
            return false
        }
        val pacote = context.packageName
        return habilitados.split(":").any { entrada ->
            entrada.contains(pacote, ignoreCase = true)
        }
    }

    fun bateriaLiberada(context: Context): Boolean {
        val gerente = context.getSystemService(PowerManager::class.java) ?: return true
        return gerente.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun monitoramentoPronto(context: Context): Boolean =
        overlayConcedida(context) &&
            listenerNotificacoesAtivo(context) &&
            acessibilidadeAtiva(context)

    fun permissoesIniciaisOk(context: Context): Boolean =
        monitoramentoPronto(context) && bateriaLiberada(context)

    fun todasConcedidas(context: Context): Boolean = monitoramentoPronto(context)

    fun intentNotificacoes(): Intent =
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)

    fun intentSobrepor(context: Context): Intent =
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}"),
        )

    fun intentAcessibilidade(): Intent =
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

    fun intentBateria(context: Context): Intent =
        Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:${context.packageName}"),
        )

    fun versaoApp(context: Context): String =
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty().ifBlank { "—" }

    fun compartilharDiagnostico(context: Context, diagnostico: NotificationDiagnosticLog) {
        diagnostico.compartilhar(context)
    }
}
