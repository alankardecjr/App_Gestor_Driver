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
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.gestordriver.overlay.OverlayService
import br.com.gestordriver.permission.PermissoesMonitoramento
import br.com.gestordriver.ui.AppScreen
import br.com.gestordriver.ui.AppViewModel
import br.com.gestordriver.ui.ConfiguracoesViewModel
import br.com.gestordriver.ui.theme.GestorDriverTheme

class MainActivity : ComponentActivity() {

    private lateinit var appViewModel: AppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        window.setBackgroundDrawableResource(android.R.color.transparent)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        val app = application as GestorDriverApp
        pedirNotificacaoPersistente()
        pedirLocalizacao()

        appViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AppViewModel(app.historicoRepository) as T
                }
            },
        )[AppViewModel::class.java]

        val configuracoesViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ConfiguracoesViewModel(app.configuracaoStore) as T
                }
            },
        )[ConfiguracoesViewModel::class.java]

        setContent {
            GestorDriverTheme {
                LaunchedEffect(Unit) {
                    appViewModel.fecharApp.collect {
                        OverlayService.parar(this@MainActivity)
                        finishAffinity()
                    }
                }

                LaunchedEffect(Unit) {
                    appViewModel.irParaSegundoPlano.collect {
                        if (appViewModel.state.interfaceOculta) {
                            OverlayService.iniciar(this@MainActivity)
                            moveTaskToBack(true)
                        }
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (::appViewModel.isInitialized) {
            tratarIntent(intent, appViewModel)
        }
    }

    override fun onPause() {
        super.onPause()
        OverlayService.iniciar(this)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (::appViewModel.isInitialized) {
            appViewModel.recolherAoSairDoApp()
        }
    }

    override fun onStop() {
        super.onStop()
        if (::appViewModel.isInitialized && !isChangingConfigurations) {
            appViewModel.recolherAoSairDoApp()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::appViewModel.isInitialized) {
            tratarIntent(intent, appViewModel)
        }
        if (PermissoesMonitoramento.todasConcedidas(this)) {
            OverlayService.iniciar(this)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun aplicarJanela(oculta: Boolean, cheia: Boolean) {
        val params = window.attributes
        if (oculta) {
            params.width = 1
            params.height = 1
            params.gravity = android.view.Gravity.TOP or android.view.Gravity.START
            window.attributes = params
            return
        }
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        params.gravity = android.view.Gravity.TOP
        window.attributes = params
    }

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).toInt()

    private fun tratarIntent(intent: Intent?, viewModel: AppViewModel) {
        val abrirExpandida = intent?.getBooleanExtra(EXTRA_ABRIR_EXPANDIDA, false) == true
        if (abrirExpandida) {
            val origemCompacta = intent.getBooleanExtra(EXTRA_ORIGEM_COMPACTA, false)
            intent?.removeExtra(EXTRA_ABRIR_EXPANDIDA)
            intent?.removeExtra(EXTRA_ORIGEM_COMPACTA)
            viewModel.reabrirInterface(origemCompacta)
            OverlayService.iniciar(this)
            moveTaskToBack(true)
            return
        }
        val pedirLocalizacao = intent?.getBooleanExtra(EXTRA_PEDIR_LOCALIZACAO, false) == true
        if (pedirLocalizacao) {
            intent?.removeExtra(EXTRA_PEDIR_LOCALIZACAO)
            pedirLocalizacao()
            OverlayService.iniciar(this)
            moveTaskToBack(true)
            return
        }
        val abrirHistorico = intent?.getBooleanExtra(EXTRA_ABRIR_HISTORICO, false) == true
        if (abrirHistorico) {
            intent?.removeExtra(EXTRA_ABRIR_HISTORICO)
            viewModel.abrirHistoricoPeloOverlay()
            OverlayService.iniciar(this)
            moveTaskToBack(true)
            return
        }
        val abrirConfig = intent?.getBooleanExtra(EXTRA_ABRIR_CONFIG, false) == true
        if (abrirConfig) {
            intent?.removeExtra(EXTRA_ABRIR_CONFIG)
            viewModel.abrirConfiguracoes()
            OverlayService.iniciar(this)
            moveTaskToBack(true)
            return
        }
        val confirmarFechar = intent?.getBooleanExtra(EXTRA_CONFIRMAR_FECHAR, false) == true
        if (confirmarFechar) {
            intent?.removeExtra(EXTRA_CONFIRMAR_FECHAR)
            viewModel.solicitarFecharApp()
            OverlayService.iniciar(this)
            return
        }
        if (!PermissoesMonitoramento.todasConcedidas(this)) {
            val overlayOk = PermissoesMonitoramento.overlayConcedida(this)
            viewModel.abrirConfiguracoes(destaquePermissao = true, usarOverlay = overlayOk)
            if (overlayOk) {
                OverlayService.iniciar(this)
                moveTaskToBack(true)
            }
            return
        }
        viewModel.iniciarMonitoramento()
        OverlayService.iniciar(this)
        if (viewModel.state.interfaceOculta) {
            moveTaskToBack(true)
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

    fun pedirLocalizacao() {
        val fine = android.Manifest.permission.ACCESS_FINE_LOCATION
        val coarse = android.Manifest.permission.ACCESS_COARSE_LOCATION
        val faltando = listOf(fine, coarse).filter {
            checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        if (faltando.isEmpty()) {
            return
        }
        requestPermissions(faltando.toTypedArray(), 7103)
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

    companion object {
        const val EXTRA_ABRIR_EXPANDIDA = "abrir_expandida"
        const val EXTRA_ORIGEM_COMPACTA = "origem_compacta"
        const val EXTRA_ABRIR_HISTORICO = "abrir_historico"
        const val EXTRA_ABRIR_CONFIG = "abrir_config"
        const val EXTRA_CONFIRMAR_FECHAR = "confirmar_fechar"
        const val EXTRA_PEDIR_LOCALIZACAO = "pedir_localizacao"
    }
}
