package br.com.gestordriver.permission

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import br.com.gestordriver.notification.NotificationDiagnosticLog
import br.com.gestordriver.notification.RideNotificationListenerService
import br.com.gestordriver.notification.RideScreenReaderService

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

    fun avisoDoAppConcedido(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 33) {
            return true
        }
        return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
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
        overlayConcedida(context) &&
            listenerNotificacoesAtivo(context) &&
            bateriaLiberada(context)

    fun todasConcedidas(context: Context): Boolean = monitoramentoPronto(context)

    fun abrirNotificacoes(context: Context) {
        val reserva = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        val preferida = if (Build.VERSION.SDK_INT >= 33) {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).apply {
                putExtra(
                    Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                    ComponentName(context, RideNotificationListenerService::class.java).flattenToString(),
                )
            }
        } else {
            null
        }
        abrirAjuste(context, preferida, reserva)
    }

    fun intentNotificacoes(context: Context): Intent {
        val componente = ComponentName(context, RideNotificationListenerService::class.java).flattenToString()
        return if (Build.VERSION.SDK_INT >= 33) {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).apply {
                putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, componente)
            }
        } else {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        }
    }

    fun intentSobrepor(context: Context): Intent =
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}"),
        )

    fun abrirAcessibilidade(context: Context) {
        val reserva = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        val preferida = Intent("android.settings.ACCESSIBILITY_DETAILS_SETTINGS").apply {
            putExtra(
                Intent.EXTRA_COMPONENT_NAME,
                ComponentName(context, RideScreenReaderService::class.java).flattenToString(),
            )
        }
        abrirAjuste(context, preferida, reserva)
    }

    fun intentAcessibilidade(context: Context): Intent =
        Intent("android.settings.ACCESSIBILITY_DETAILS_SETTINGS").apply {
            putExtra(
                Intent.EXTRA_COMPONENT_NAME,
                ComponentName(context, RideScreenReaderService::class.java).flattenToString(),
            )
        }

    private fun abrirAjuste(context: Context, preferida: Intent?, reserva: Intent) {
        val alvo = preparar(context, preferida ?: reserva)
        try {
            context.startActivity(alvo)
        } catch (_: ActivityNotFoundException) {
            abrirReserva(context, preferida, reserva)
        } catch (_: SecurityException) {
            abrirReserva(context, preferida, reserva)
        }
    }

    private fun abrirReserva(context: Context, preferida: Intent?, reserva: Intent) {
        if (preferida == null) {
            return
        }
        runCatching { context.startActivity(preparar(context, reserva)) }
    }

    private fun preparar(context: Context, intent: Intent): Intent {
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return intent
    }

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
