package br.com.gestordriver

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.gestordriver.overlay.OverlayService
import br.com.gestordriver.permission.PermissoesMonitoramento
import br.com.gestordriver.ui.AppScreen
import br.com.gestordriver.ui.AppViewModel
import br.com.gestordriver.ui.ConfiguracoesViewModel
import br.com.gestordriver.ui.theme.GestorDriverTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        val app = application as GestorDriverApp
        pedirNotificacaoPersistente()

        setContent {
            GestorDriverTheme {
                val appViewModel: AppViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return AppViewModel(app.historicoRepository) as T
                        }
                    },
                )
                val configuracoesViewModel: ConfiguracoesViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return ConfiguracoesViewModel(app.configuracaoStore) as T
                        }
                    },
                )

                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            if (PermissoesMonitoramento.todasConcedidas(this@MainActivity)) {
                                appViewModel.iniciarMonitoramento()
                                OverlayService.iniciar(this@MainActivity)
                            } else {
                                appViewModel.abrirConfiguracoes()
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                LaunchedEffect(Unit) {
                    appViewModel.fecharApp.collect {
                        OverlayService.parar(this@MainActivity)
                        finishAffinity()
                    }
                }

                LaunchedEffect(appViewModel.state.monitorando) {
                    if (appViewModel.state.monitorando &&
                        PermissoesMonitoramento.todasConcedidas(this@MainActivity)
                    ) {
                        OverlayService.iniciar(this@MainActivity)
                    }
                    if (!appViewModel.state.monitorando) {
                        OverlayService.parar(this@MainActivity)
                    }
                }

                AppScreen(
                    viewModel = appViewModel,
                    configuracoesViewModel = configuracoesViewModel,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (PermissoesMonitoramento.todasConcedidas(this)) {
            OverlayService.iniciar(this)
        }
    }

    private fun pedirNotificacaoPersistente() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 7102)
    }

    fun abrirConfiguracaoOverlay() {
        startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"),
            ),
        )
    }

    fun abrirConfiguracaoNotificacoes() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }
}
