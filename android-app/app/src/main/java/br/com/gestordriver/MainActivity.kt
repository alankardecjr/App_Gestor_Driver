package br.com.gestordriver

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.gestordriver.data.ContaVinculo
import br.com.gestordriver.model.OnboardingEtapa
import br.com.gestordriver.model.TipoContaVinculada
import br.com.gestordriver.overlay.OverlayBridge
import br.com.gestordriver.overlay.OverlayPaineis
import br.com.gestordriver.overlay.OverlayService
import br.com.gestordriver.permission.PermissoesMonitoramento
import br.com.gestordriver.ui.AppScreen
import br.com.gestordriver.ui.AppViewModel
import br.com.gestordriver.ui.ConfiguracoesViewModel
import br.com.gestordriver.ui.theme.GestorDriverTheme

class MainActivity : ComponentActivity() {

    private lateinit var appViewModel: AppViewModel
    private var retomadaInicial = true

    private val seletorGoogle = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { resultado ->
        if (resultado.resultCode == RESULT_OK) {
            val email = ContaVinculo.emailDaResposta(resultado.data)
            if (!email.isNullOrBlank()) {
                val app = application as GestorDriverApp
                val atual = app.configuracaoStore.carregar()
                app.configuracaoStore.salvar(
                    ContaVinculo.aplicar(atual, TipoContaVinculada.GOOGLE, email),
                )
                OverlayPaineis.atualizarContaVinculada(TipoContaVinculada.GOOGLE, email)
                OverlayPaineis.invalidarMontagem()
                OverlayBridge.publicar(OverlayBridge.snapshot.value)
            }
        }
        OverlayService.iniciar(this)
        moveTaskToBack(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val noite = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        enableEdgeToEdge(
            statusBarStyle = if (noite) {
                SystemBarStyle.dark(Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            },
            navigationBarStyle = if (noite) {
                SystemBarStyle.dark(Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            },
        )
        window.setBackgroundDrawableResource(R.color.fundo_app)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        val app = application as GestorDriverApp
        pedirNotificacaoPersistente()

        appViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AppViewModel(
                        historicoRepository = app.historicoRepository,
                        onboardingStore = app.onboardingStore,
                    ) as T
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

                LaunchedEffect(Unit) {
                    appViewModel.irParaFrente.collect {
                        startActivity(
                            Intent(this@MainActivity, MainActivity::class.java)
                                .addFlags(
                                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                        Intent.FLAG_ACTIVITY_NEW_TASK,
                                )
                                .putExtra(EXTRA_RECENTES_CONFIG, true),
                        )
                    }
                }

                LaunchedEffect(appViewModel.state.monitorando) {
                    if (appViewModel.state.monitorando &&
                        PermissoesMonitoramento.overlayConcedida(this@MainActivity)
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
        onBackPressedDispatcher.addCallback(this) {
            if (!::appViewModel.isInitialized) {
                finish()
                return@addCallback
            }
            // Sem monitoramento: Voltar = padrão do celular (sair da activity).
            if (!appViewModel.state.monitorando) {
                finish()
                return@addCallback
            }
            val jaNoSelo = appViewModel.state.seloFlutuante &&
                !appViewModel.state.historicoVisivel &&
                !appViewModel.state.configuracoesVisivel &&
                !appViewModel.state.dashboardVisivel &&
                !appViewModel.state.confirmacaoFecharVisivel &&
                !appViewModel.state.confirmacaoLimparHistoricoVisivel
            appViewModel.voltarPelaBarra()
            if (jaNoSelo || appViewModel.state.seloFlutuante || appViewModel.state.interfaceOculta) {
                OverlayService.iniciar(this@MainActivity)
                moveTaskToBack(true)
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
            val atalho = tratarIntent(intent, appViewModel)
            if (retomadaInicial) {
                retomadaInicial = false
            } else if (
                !atalho &&
                appViewModel.state.monitorando &&
                appViewModel.state.onboardingEtapa == OnboardingEtapa.NENHUMA &&
                !isChangingConfigurations
            ) {
                appViewModel.restaurarTelaAposRecentes()
            }
        }
        if (PermissoesMonitoramento.overlayConcedida(this)) {
            OverlayService.iniciar(this)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun aplicarJanela(oculta: Boolean, cheia: Boolean) {
        val params = window.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        params.gravity = android.view.Gravity.TOP
        window.attributes = params
    }

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).toInt()

    private fun tratarIntent(intent: Intent?, viewModel: AppViewModel): Boolean {
        val abrirExpandida = intent?.getBooleanExtra(EXTRA_ABRIR_EXPANDIDA, false) == true
        if (abrirExpandida) {
            val origemCompacta = intent.getBooleanExtra(EXTRA_ORIGEM_COMPACTA, false)
            intent?.removeExtra(EXTRA_ABRIR_EXPANDIDA)
            intent?.removeExtra(EXTRA_ORIGEM_COMPACTA)
            viewModel.reabrirInterface(origemCompacta)
            OverlayService.iniciar(this)
            moveTaskToBack(true)
            return true
        }
        val conectarGoogle = intent?.getBooleanExtra(EXTRA_CONECTAR_GOOGLE, false) == true
        if (conectarGoogle) {
            intent?.removeExtra(EXTRA_CONECTAR_GOOGLE)
            seletorGoogle.launch(ContaVinculo.intentEscolherContaGoogle())
            return true
        }
        val pedirLocalizacao = intent?.getBooleanExtra(EXTRA_PEDIR_LOCALIZACAO, false) == true
        if (pedirLocalizacao) {
            intent?.removeExtra(EXTRA_PEDIR_LOCALIZACAO)
            pedirLocalizacao()
            OverlayService.iniciar(this)
            moveTaskToBack(true)
            return true
        }
        val pedirAcessibilidade = intent?.getBooleanExtra(EXTRA_PEDIR_ACESSIBILIDADE, false) == true
        if (pedirAcessibilidade) {
            intent?.removeExtra(EXTRA_PEDIR_ACESSIBILIDADE)
            startActivity(PermissoesMonitoramento.intentAcessibilidade())
            OverlayService.iniciar(this)
            return true
        }
        val pedirBateria = intent?.getBooleanExtra(EXTRA_PEDIR_BATERIA, false) == true
        if (pedirBateria) {
            intent?.removeExtra(EXTRA_PEDIR_BATERIA)
            startActivity(PermissoesMonitoramento.intentBateria(this))
            OverlayService.iniciar(this)
            return true
        }
        val compartilharLog = intent?.getBooleanExtra(EXTRA_COMPARTILHAR_LOG, false) == true
        if (compartilharLog) {
            intent?.removeExtra(EXTRA_COMPARTILHAR_LOG)
            (application as GestorDriverApp).diagnosticLog.compartilhar(this)
            OverlayService.iniciar(this)
            return true
        }
        val abrirHistorico = intent?.getBooleanExtra(EXTRA_ABRIR_HISTORICO, false) == true
        if (abrirHistorico) {
            intent?.removeExtra(EXTRA_ABRIR_HISTORICO)
            viewModel.abrirHistoricoPeloOverlay()
            OverlayService.iniciar(this)
            moveTaskToBack(true)
            return true
        }
        val recentesConfig = intent?.getBooleanExtra(EXTRA_RECENTES_CONFIG, false) == true
        if (recentesConfig) {
            intent?.removeExtra(EXTRA_RECENTES_CONFIG)
            viewModel.restaurarTelaAposRecentes()
            OverlayService.iniciar(this)
            return true
        }
        val abrirConfig = intent?.getBooleanExtra(EXTRA_ABRIR_CONFIG, false) == true
        if (abrirConfig) {
            intent?.removeExtra(EXTRA_ABRIR_CONFIG)
            viewModel.abrirConfiguracoes()
            OverlayService.iniciar(this)
            moveTaskToBack(true)
            return true
        }
        val confirmarFechar = intent?.getBooleanExtra(EXTRA_CONFIRMAR_FECHAR, false) == true
        if (confirmarFechar) {
            intent?.removeExtra(EXTRA_CONFIRMAR_FECHAR)
            viewModel.solicitarFecharApp()
            OverlayService.iniciar(this)
            return true
        }
        val app = application as GestorDriverApp
        val temConta = app.configuracaoStore.carregar().contaTipo != TipoContaVinculada.NENHUMA
        viewModel.avaliarInicio(
            permissoesOk = PermissoesMonitoramento.permissoesIniciaisOk(this),
            temConta = temConta,
        )
        if (viewModel.state.onboardingEtapa != OnboardingEtapa.NENHUMA) {
            return true
        }
        OverlayService.iniciar(this)
        return false
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
        const val EXTRA_RECENTES_CONFIG = "recentes_config"
        const val EXTRA_CONFIRMAR_FECHAR = "confirmar_fechar"
        const val EXTRA_PEDIR_LOCALIZACAO = "pedir_localizacao"
        const val EXTRA_PEDIR_ACESSIBILIDADE = "pedir_acessibilidade"
        const val EXTRA_PEDIR_BATERIA = "pedir_bateria"
        const val EXTRA_COMPARTILHAR_LOG = "compartilhar_log"
        const val EXTRA_CONECTAR_GOOGLE = "conectar_google"
    }
}
