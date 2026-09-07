package br.com.gestordriver.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.service.notification.NotificationListenerService
import android.util.TypedValue
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.widget.ImageViewCompat
import br.com.gestordriver.MainActivity
import br.com.gestordriver.R
import br.com.gestordriver.core.ClassificacaoConstantes
import br.com.gestordriver.notification.RideNotificationListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.abs

class OverlayService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }

    private var seloView: View? = null
    private var compactaView: View? = null
    private var expandidaView: View? = null
    private var historicoView: View? = null
    private var configView: View? = null
    private var dashboardView: View? = null
    private var confirmacaoView: View? = null
    private var lixeiraView: View? = null
    private var seloParams: WindowManager.LayoutParams? = null
    private var compactaParams: WindowManager.LayoutParams? = null
    private var expandidaParams: WindowManager.LayoutParams? = null
    private var historicoParams: WindowManager.LayoutParams? = null
    private var configParams: WindowManager.LayoutParams? = null
    private var dashboardParams: WindowManager.LayoutParams? = null
    private var confirmacaoParams: WindowManager.LayoutParams? = null
    private var lixeiraParams: WindowManager.LayoutParams? = null
    private var ultimoSnapshot: OverlaySnapshot? = null
    private var arrastandoSelo = false
    private var arrastandoCompacta = false
    private var historicoAberto = false
    private var configAberto = false
    private var dashboardAberto = false
    private var confirmacaoAberto = false
    private val camadaHandler = Handler(Looper.getMainLooper())
    private val prefsCompacta by lazy { getSharedPreferences(PREFS_COMPACTA, MODE_PRIVATE) }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        criarCanal()
        runCatching {
            startForeground(NOTIFICACAO_ID, criarNotificacao())
        }.onFailure {
            stopSelf()
            return
        }
        scope.launch {
            OverlayBridge.snapshot.collect { snapshot ->
                val compactaNova = snapshot.compactaVisivel && ultimoSnapshot?.compactaVisivel != true
                if (snapshot == ultimoSnapshot) {
                    return@collect
                }
                ultimoSnapshot = snapshot
                atualizarJanelas(snapshot)
                atualizarNotificacao(snapshot)
                if (snapshot.compactaVisivel && compactaNova) {
                    // Uma vez só, junto com a oferta — sem reafirmar em loop sobre o app.
                    trazerCompactaParaFrente()
                } else if (!snapshot.compactaVisivel) {
                    cancelarCompactaNaFrente()
                }
            }
        }
        // reafirmarCamada ignorado: não forçar compacta a reaparecer sobre a plataforma.
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACAO_PARAR) {
            // Encerramento já confirmado pela UI; só para o serviço.
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent?.action == ACAO_ABRIR) {
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            )
        }
        atualizarJanelas(OverlayBridge.snapshot.value)
        runCatching {
            NotificationListenerService.requestRebind(
                android.content.ComponentName(this, RideNotificationListenerService::class.java),
            )
        }
        return START_STICKY
    }

    override fun onDestroy() {
        removerView(seloView)
        removerView(compactaView)
        removerView(expandidaView)
        removerView(historicoView)
        removerView(configView)
        removerView(dashboardView)
        removerView(confirmacaoView)
        removerView(lixeiraView)
        seloView = null
        compactaView = null
        expandidaView = null
        historicoView = null
        configView = null
        dashboardView = null
        confirmacaoView = null
        lixeiraView = null
        seloParams = null
        compactaParams = null
        expandidaParams = null
        historicoParams = null
        configParams = null
        dashboardParams = null
        confirmacaoParams = null
        cancelarCompactaNaFrente()
        scope.cancel()
        super.onDestroy()
    }

    private fun atualizarJanelas(snapshot: OverlaySnapshot) {
        runCatching { atualizarJanelasInterno(snapshot) }
    }

    private fun atualizarJanelasInterno(snapshot: OverlaySnapshot) {
        if (!snapshot.monitorando) {
            seloView?.visibility = View.INVISIBLE
            compactaView?.visibility = View.INVISIBLE
            expandidaView?.visibility = View.INVISIBLE
            historicoView?.visibility = View.INVISIBLE
            configView?.visibility = View.INVISIBLE
            dashboardView?.visibility = View.INVISIBLE
            confirmacaoView?.visibility = View.INVISIBLE
            return
        }
        if (snapshot.seloVisivel && !snapshot.compactaVisivel && !snapshot.expandidaVisivel) {
            mostrarSeloImediato()
        }
        garantirSelo(snapshot)
        seloView?.visibility = if (snapshot.seloVisivel) View.VISIBLE else View.INVISIBLE
        if (snapshot.compactaVisivel) {
            garantirCompacta(snapshot)
            compactaView?.visibility = View.VISIBLE
            compactaView?.elevation = 48f
        } else {
            compactaView?.visibility = View.INVISIBLE
            desligarToqueForaCompacta()
        }
        if (snapshot.expandidaVisivel) {
            garantirExpandida(snapshot)
            expandidaView?.visibility = View.VISIBLE
        } else {
            expandidaView?.visibility = View.INVISIBLE
        }
        // §44: com atalhos abertos o selo some; X/fora devolve o selo.
        if (snapshot.seloVisivel && snapshot.expandidaVisivel) {
            trazerSeloParaFrente()
        }
        // Overlay sobre outros apps (§44): só selo, atalhos (expandidaVisivel) e compacta.
        // Histórico / Dashboard / Config / Semáforo / confirmações: só na Activity (Compose).
        historicoView?.let {
            animarPainelSecundario(it, abrir = false, jaAberto = historicoAberto)
            historicoAberto = false
        }
        configView?.let {
            animarPainelSecundario(it, abrir = false, jaAberto = configAberto)
            configAberto = false
        }
        dashboardView?.let {
            animarPainelSecundario(it, abrir = false, jaAberto = dashboardAberto)
            dashboardAberto = false
        }
        confirmacaoView?.visibility = View.INVISIBLE
        confirmacaoAberto = false
    }

    private fun mostrarSeloImediato() {
        historicoView?.animate()?.cancel()
        configView?.animate()?.cancel()
        dashboardView?.animate()?.cancel()
        confirmacaoView?.animate()?.cancel()
        historicoView?.translationY = 0f
        configView?.translationY = 0f
        dashboardView?.translationY = 0f
        confirmacaoView?.translationY = 0f
        historicoAberto = false
        configAberto = false
        dashboardAberto = false
        confirmacaoAberto = false
        compactaView?.visibility = View.INVISIBLE
        expandidaView?.visibility = View.INVISIBLE
        historicoView?.visibility = View.INVISIBLE
        configView?.visibility = View.INVISIBLE
        dashboardView?.visibility = View.INVISIBLE
        confirmacaoView?.visibility = View.INVISIBLE
        desligarToqueForaCompacta()
        seloView?.visibility = View.VISIBLE
    }

    private fun animarPainelSecundario(view: View?, abrir: Boolean, jaAberto: Boolean) {
        val alvo = view ?: return
        alvo.animate().cancel()
        if (abrir) {
            alvo.visibility = View.VISIBLE
            if (jaAberto) {
                alvo.translationY = 0f
                return
            }
            val descer = Runnable {
                val altura = alvo.height.coerceAtLeast(dp(120)).toFloat()
                alvo.translationY = -altura
                alvo.animate()
                    .translationY(0f)
                    .setDuration(220)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
            if (alvo.height > 0) {
                descer.run()
            } else {
                alvo.post(descer)
            }
        } else if (jaAberto && alvo.visibility == View.VISIBLE) {
            val altura = alvo.height.coerceAtLeast(dp(120)).toFloat()
            alvo.animate()
                .translationY(-altura)
                .setDuration(180)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    alvo.visibility = View.INVISIBLE
                    alvo.translationY = 0f
                }
                .start()
        } else {
            alvo.visibility = View.INVISIBLE
            alvo.translationY = 0f
        }
    }

    private fun garantirSelo(snapshot: OverlaySnapshot) {
        val tamanho = dp(SELO_DP)
        val params = seloParams ?: criarParams(
            snapshot.offsetX.toInt(),
            snapshot.offsetY.toInt(),
        ).also {
            seloParams = it
        }
        params.width = tamanho
        params.height = tamanho
        if (!arrastandoSelo) {
            params.x = snapshot.offsetX.toInt()
            params.y = snapshot.offsetY.toInt()
        }
        val view = seloView ?: criarSelo().also { nova ->
            val adicionou = runCatching { windowManager.addView(nova, params) }.isSuccess
            if (!adicionou) {
                return
            }
            seloView = nova
        }
        atualizarSelo(view, snapshot)
        aplicarLayoutSeguro(view, params)
    }

    private fun garantirCompacta(snapshot: OverlaySnapshot) {
        val insets = insetsSeguros()
        val padraoX = insets.left + dp(8)
        val padraoY = insets.top + dp(8)
        val primeira = compactaParams == null
        val params = compactaParams ?: criarParams(
            prefsCompacta.getInt(KEY_COMPACTA_X, padraoX),
            prefsCompacta.getInt(KEY_COMPACTA_Y, padraoY),
            Gravity.TOP or Gravity.START,
            focavel = false,
        ).also { compactaParams = it }
        params.gravity = Gravity.TOP or Gravity.START
        if (primeira && !prefsCompacta.contains(KEY_COMPACTA_X)) {
            params.x = padraoX
            params.y = padraoY
        }
        aplicarFlagsJanela(params, focavel = false)
        val view = compactaView ?: criarCompacta().also { nova ->
            val adicionou = runCatching { windowManager.addView(nova, params) }.isSuccess
            if (!adicionou) {
                return
            }
            compactaView = nova
            escutarBarraInferior(nova)
        }
        atualizarCompacta(view, snapshot)
        aplicarFlagsToqueFora(params, ativo = false)
        aplicarTamanhoCompacta(view, params, reposicionar = primeira && !prefsCompacta.contains(KEY_COMPACTA_X))
    }

    private fun desligarToqueForaCompacta() {
        val view = compactaView ?: return
        val params = compactaParams ?: return
        aplicarFlagsToqueFora(params, ativo = false)
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun garantirExpandida(snapshot: OverlaySnapshot) {
        val params = expandidaParams ?: criarParams(
            snapshot.offsetX.toInt(),
            snapshot.offsetY.toInt(),
            Gravity.TOP or Gravity.START,
            focavel = false,
        ).also { expandidaParams = it }
        params.gravity = Gravity.TOP or Gravity.START
        aplicarFlagsJanela(params, focavel = false)
        val view = expandidaView ?: criarExpandida(snapshot).also { nova ->
            val adicionou = runCatching { windowManager.addView(nova, params) }.isSuccess
            if (!adicionou) {
                return
            }
            expandidaView = nova
            escutarBarraInferior(nova)
        }
        atualizarExpandida(view, snapshot)
        aplicarFlagsToqueFora(params, ativo = true)
        posicionarMenuAtalho(view, snapshot, params)
    }

    private fun garantirHistorico(snapshot: OverlaySnapshot) {
        val insets = insetsSeguros()
        val bounds = windowManager.currentWindowMetrics.bounds
        val params = historicoParams ?: criarParams(
            insets.left + dp(8),
            insets.top + dp(8),
            Gravity.TOP or Gravity.START,
            focavel = false,
        ).also { historicoParams = it }
        params.width = bounds.width() - insets.left - insets.right - dp(16)
        params.height = bounds.height() - insets.top - insets.bottom - dp(16)
        params.x = insets.left + dp(8)
        params.y = insets.top + dp(8)
        aplicarFlagsJanela(params, focavel = false)
        val view = historicoView ?: OverlayPaineis.criarHistorico(this).also { nova ->
            val adicionou = runCatching { windowManager.addView(nova, params) }.isSuccess
            if (!adicionou) {
                return
            }
            historicoView = nova
            escutarBarraInferior(nova)
        }
        if (view.findViewWithTag<View>("menu_abas_ficheiro") == null) {
            removerView(historicoView)
            historicoView = null
            garantirHistorico(snapshot)
            return
        }
        OverlayPaineis.atualizarHistorico(view, snapshot)
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun garantirConfig(snapshot: OverlaySnapshot) {
        val insets = insetsSeguros()
        val bounds = windowManager.currentWindowMetrics.bounds
        val params = configParams ?: criarParams(
            insets.left + dp(8),
            insets.top + dp(8),
            Gravity.TOP or Gravity.START,
            focavel = true,
        ).also { configParams = it }
        params.width = bounds.width() - insets.left - insets.right - dp(16)
        params.height = bounds.height() - insets.top - insets.bottom - dp(16)
        params.x = insets.left + dp(8)
        params.y = insets.top + dp(8)
        aplicarFlagsJanela(params, focavel = true)
        if (configView?.findViewWithTag<View>("config_cabecalho") == null ||
            configView?.findViewWithTag<View>("menu_abas_ficheiro") == null
        ) {
            removerView(configView)
            configView = null
        }
        val view = configView ?: OverlayPaineis.criarConfig(this).also { nova ->
            val adicionou = runCatching { windowManager.addView(nova, params) }.isSuccess
            if (!adicionou) {
                return
            }
            configView = nova
            escutarBarraInferior(nova)
        }
        OverlayPaineis.atualizarConfig(view, snapshot)
        params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN or
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun garantirDashboard(snapshot: OverlaySnapshot) {
        val insets = insetsSeguros()
        val bounds = windowManager.currentWindowMetrics.bounds
        val params = dashboardParams ?: criarParams(
            insets.left + dp(8),
            insets.top + dp(8),
            Gravity.TOP or Gravity.START,
            focavel = false,
        ).also { dashboardParams = it }
        params.width = bounds.width() - insets.left - insets.right - dp(16)
        params.height = bounds.height() - insets.top - insets.bottom - dp(16)
        params.x = insets.left + dp(8)
        params.y = insets.top + dp(8)
        aplicarFlagsJanela(params, focavel = false)
        val view = dashboardView ?: OverlayPaineis.criarDashboard(this).also { nova ->
            val adicionou = runCatching { windowManager.addView(nova, params) }.isSuccess
            if (!adicionou) {
                return
            }
            dashboardView = nova
            escutarBarraInferior(nova)
        }
        if (view.findViewWithTag<View>("menu_abas_ficheiro") == null ||
            view.findViewWithTag<View>("dashboard_grade") == null
        ) {
            removerView(dashboardView)
            dashboardView = null
            garantirDashboard(snapshot)
            return
        }
        OverlayPaineis.atualizarDashboard(view, snapshot)
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun garantirConfirmacao(snapshot: OverlaySnapshot) {
        garantirConfirmacaoSobreHistorico(snapshot)
    }

    private fun garantirConfirmacaoSobreHistorico(snapshot: OverlaySnapshot) {
        val insets = insetsSeguros()
        val bounds = windowManager.currentWindowMetrics.bounds
        val params = confirmacaoParams ?: criarParams(
            insets.left + dp(8),
            insets.top + dp(8),
            Gravity.TOP or Gravity.START,
            focavel = false,
        ).also { confirmacaoParams = it }
        val largura = bounds.width() - insets.left - insets.right - dp(16)
        params.width = largura
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.x = insets.left + dp(8)
        // Centraliza verticalmente sobre a área do histórico.
        val alturaPainel = bounds.height() - insets.top - insets.bottom - dp(16)
        params.y = insets.top + dp(8) + (alturaPainel / 3)
        aplicarFlagsJanela(params, focavel = false)
        val view = confirmacaoView ?: OverlayPaineis.criarConfirmacaoFechar(this).also { nova ->
            val adicionou = runCatching { windowManager.addView(nova, params) }.isSuccess
            if (!adicionou) {
                return
            }
            confirmacaoView = nova
            escutarBarraInferior(nova)
        }
        OverlayPaineis.atualizarConfirmacao(
            view,
            limparHistorico = true,
            quantidade = snapshot.historicoLimparQuantidade,
        )
        OverlayPaineis.aplicarBordaNeutra(view)
        view.elevation = 64f
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun corBorda(snapshot: OverlaySnapshot): String = snapshot.corClassificacao

    private fun criarExpandida(snapshot: OverlaySnapshot): View {
        val raiz = FrameLayout(this)
        raiz.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE) {
                OverlayBridge.emitir(OverlayAcao.FecharAtalhos)
                return@setOnTouchListener true
            }
            false
        }
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            tag = "menu_atalho_card"
            background = fundoMenuCard()
            elevation = dp(12).toFloat()
            outlineProvider = ViewOutlineProvider.BACKGROUND
            clipToOutline = true
            setPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            )
        }
        montarMenuAtalho(card, snapshot)
        raiz.addView(card)
        return raiz
    }

    private fun atualizarExpandida(view: View, snapshot: OverlaySnapshot) {
        view.alpha = if (snapshot.monitorando) 1f else 0.9f
        val card = view.findViewWithTag<LinearLayout>("menu_atalho_card") ?: return
        card.background = fundoMenuCard()
        card.removeAllViews()
        if (snapshot.confirmacaoFecharVisivel) {
            return
        }
        montarMenuAtalho(card, snapshot)
    }

    private fun montarMenuAtalho(card: LinearLayout, snapshot: OverlaySnapshot) {
        // UI Atalhos oficial — tema claro/escuro; ícones coloridos; X vermelho.
        val ui = AtalhosUi.de(OverlayTema.de(this).escuro)
        card.addView(cabecalhoAtalhos(ui))
        val itens = listOf(
            ItemMenuAtalho(
                titulo = "Histórico",
                subtitulo = "Ver corridas aceitas",
                iconeRes = R.drawable.ic_menu_historico,
                cores = ui.historico,
            ) {
                OverlayBridge.emitir(OverlayAcao.AbrirHistorico)
            },
            ItemMenuAtalho(
                titulo = "Carteira",
                subtitulo = "Saldo e movimentações",
                iconeRes = R.drawable.ic_menu_carteira,
                cores = ui.carteira,
                pro = !snapshot.planoPro,
            ) {
                OverlayBridge.emitir(OverlayAcao.DashboardPro)
            },
            ItemMenuAtalho(
                titulo = "Despesas",
                subtitulo = "Controle de gastos do app",
                iconeRes = R.drawable.ic_menu_despesas,
                cores = ui.despesas,
            ) {
                OverlayBridge.emitir(OverlayAcao.AbrirAtalhoConfig(0))
            },
            ItemMenuAtalho(
                titulo = "Semáforo",
                subtitulo = "Regras de classificação",
                iconeRes = R.drawable.ic_menu_semaforo,
                cores = ui.semaforo,
            ) {
                OverlayBridge.emitir(OverlayAcao.AbrirSemaforo)
            },
            ItemMenuAtalho(
                titulo = "Usuário",
                subtitulo = "Seus dados e preferências",
                iconeRes = R.drawable.ic_menu_usuario,
                cores = ui.usuario,
            ) {
                OverlayBridge.emitir(OverlayAcao.AbrirAtalhoConfig(1))
            },
            ItemMenuAtalho(
                titulo = "Configurar",
                subtitulo = "Ajustes do aplicativo",
                iconeRes = R.drawable.ic_menu_configurar,
                cores = ui.configurar,
            ) {
                OverlayBridge.emitir(OverlayAcao.AbrirAtalhoConfig(2))
            },
            ItemMenuAtalho(
                titulo = "Fechar",
                subtitulo = "Encerrar o aplicativo",
                iconeRes = R.drawable.ic_menu_fechar,
                cores = ui.fechar,
            ) {
                OverlayBridge.emitir(OverlayAcao.Fechar)
            },
        )
        itens.forEachIndexed { indice, item ->
            card.addView(
                linhaMenuAtalho(item, ui).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    ).apply {
                        topMargin = if (indice == 0) dp(14) else dp(8)
                    }
                },
            )
        }
    }

    private fun cabecalhoAtalhos(ui: AtalhosUi.Tema): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(botaoFecharAtalhos(ui))
            addView(
                TextView(this@OverlayService).apply {
                    text = "Atalhos"
                    setTextColor(ui.titulo)
                    textSize = 24f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    setPadding(0, dp(12), 0, dp(4))
                },
            )
            addView(
                TextView(this@OverlayService).apply {
                    text = "Acesse rapidamente as principais funções"
                    setTextColor(ui.subtitulo)
                    textSize = 13f
                    setPadding(0, 0, 0, 0)
                },
            )
        }
    }

    private fun botaoFecharAtalhos(ui: AtalhosUi.Tema): FrameLayout {
        val tamanho = dp(34)
        return FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(tamanho, tamanho)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(ui.botaoXFundo)
            }
            isClickable = true
            isFocusable = true
            contentDescription = "Fechar atalhos"
            setOnClickListener {
                OverlayBridge.emitir(OverlayAcao.FecharAtalhos)
            }
            addView(
                ImageView(this@OverlayService).apply {
                    setImageResource(R.drawable.ic_fechar_x)
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                    setPadding(dp(9), dp(9), dp(9), dp(9))
                    ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(ui.botaoX))
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                    )
                },
            )
        }
    }

    private fun montarConfirmacaoNoAtalho(card: LinearLayout, snapshot: OverlaySnapshot) {
        card.setPadding(dp(12), dp(14), dp(12), dp(12))
        card.addView(
            TextView(this).apply {
                text = "gestor driver"
                setTextColor(OverlayTema.de(this@OverlayService).texto)
                textSize = 15f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(8))
            },
        )
        card.addView(
            TextView(this).apply {
                text = "Deseja encerrar o aplicativo e parar o monitoramento de corridas?"
                setTextColor(OverlayTema.de(this@OverlayService).secundario)
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(14))
            },
        )
        val acoes = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        acoes.addView(
            TextView(this).apply {
                text = "Cancelar"
                setTextColor(OverlayTema.de(this@OverlayService).secundario)
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(dp(12), dp(10), dp(12), dp(10))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener {
                    OverlayBridge.emitir(OverlayAcao.CancelarFechar)
                }
            },
        )
        acoes.addView(
            TextView(this).apply {
                text = "Fechar"
                setTextColor(Color.parseColor("#F9A825"))
                textSize = 14f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setPadding(dp(12), dp(10), dp(12), dp(10))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener {
                    OverlayBridge.emitir(OverlayAcao.ConfirmarFechar)
                }
            },
        )
        card.addView(acoes)
    }

    private data class ItemMenuAtalho(
        val titulo: String,
        val subtitulo: String,
        val iconeRes: Int,
        val cores: AtalhosUi.ItemCores,
        val pro: Boolean = false,
        val acao: () -> Unit,
    )

    private fun fundoMenuCard(): GradientDrawable {
        val ui = AtalhosUi.de(OverlayTema.de(this).escuro)
        return GradientDrawable().apply {
            setColor(ui.painel)
            cornerRadius = dp(24).toFloat()
        }
    }

    private fun fundoItemMenu(ui: AtalhosUi.Tema): GradientDrawable {
        return GradientDrawable().apply {
            setColor(ui.item)
            setStroke(dp(1), ui.itemBorda)
            cornerRadius = dp(16).toFloat()
        }
    }

    private fun linhaMenuAtalho(item: ItemMenuAtalho, ui: AtalhosUi.Tema): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            minimumHeight = dp(60)
            background = fundoItemMenu(ui)
            isClickable = true
            isFocusable = true
            setOnClickListener { item.acao() }
            addView(iconeMenuAtalho(item))
            addView(
                LinearLayout(this@OverlayService).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                        marginStart = dp(12)
                        marginEnd = dp(8)
                    }
                    addView(
                        LinearLayout(this@OverlayService).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = Gravity.CENTER_VERTICAL
                            addView(
                                TextView(this@OverlayService).apply {
                                    text = item.titulo
                                    setTextColor(ui.titulo)
                                    textSize = 16f
                                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                                    maxLines = 1
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                    )
                                },
                            )
                            if (item.pro) {
                                addView(seloPro())
                            }
                        },
                    )
                    addView(
                        TextView(this@OverlayService).apply {
                            text = item.subtitulo
                            setTextColor(ui.subtitulo)
                            textSize = 12.5f
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                            setPadding(0, dp(2), 0, 0)
                        },
                    )
                },
            )
            addView(
                TextView(this@OverlayService).apply {
                    text = "›"
                    setTextColor(ui.subtitulo)
                    textSize = 22f
                    gravity = Gravity.CENTER
                },
            )
        }
    }

    private fun iconeMenuAtalho(item: ItemMenuAtalho): FrameLayout {
        val tamanho = dp(40)
        return FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(tamanho, tamanho)
            background = GradientDrawable().apply {
                setColor(item.cores.fundoIcone)
                cornerRadius = dp(12).toFloat()
            }
            addView(
                ImageView(this@OverlayService).apply {
                    setImageResource(item.iconeRes)
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                    setPadding(dp(8), dp(8), dp(8), dp(8))
                    ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(item.cores.icone))
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                    )
                },
            )
            if (item.pro) {
                addView(
                    ImageView(this@OverlayService).apply {
                        setImageResource(R.drawable.ic_menu_cadeado)
                        layoutParams = FrameLayout.LayoutParams(dp(12), dp(12)).apply {
                            gravity = Gravity.TOP or Gravity.END
                            topMargin = dp(2)
                            marginEnd = dp(2)
                        }
                    },
                )
            }
        }
    }

    private fun seloPro(): TextView {
        return TextView(this).apply {
            text = "PRO"
            setTextColor(Color.parseColor("#212121"))
            textSize = 8f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(dp(5), dp(1), dp(5), dp(1))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#FFD54F"))
                cornerRadius = dp(4).toFloat()
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { marginStart = dp(6) }
        }
    }

    private fun posicionarMenuAtalho(
        view: View,
        snapshot: OverlaySnapshot,
        params: WindowManager.LayoutParams,
    ) {
        val insets = insetsSeguros()
        val bounds = windowManager.currentWindowMetrics.bounds
        val areaW = bounds.width() - insets.left - insets.right
        val areaH = bounds.height() - insets.top - insets.bottom
        val largura = (areaW * 78 / 100).coerceIn(dp(260), dp(320))
        view.measure(
            View.MeasureSpec.makeMeasureSpec(largura, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        )
        val altura = view.measuredHeight.coerceAtLeast(1)
        val faixaRecusar = (areaH * 22 / 100).coerceAtLeast(dp(96))
        val areaMinX = insets.left + dp(8)
        val areaMinY = insets.top + dp(8)
        val areaMaxX = insets.left + areaW - dp(8)
        val areaMaxY = insets.top + areaH - faixaRecusar
        val pos = AtalhosPosicao.calcular(
            seloX = snapshot.offsetX.toInt(),
            seloY = snapshot.offsetY.toInt(),
            seloTam = dp(SELO_DP),
            menuW = largura,
            menuH = altura,
            areaMinX = areaMinX,
            areaMinY = areaMinY,
            areaMaxX = areaMaxX,
            areaMaxY = areaMaxY,
            gap = dp(8),
        )
        android.util.Log.d(
            "GestorAtalhos",
            "pos=${pos.direcao} xy=${pos.x},${pos.y} selo=${snapshot.offsetX.toInt()},${snapshot.offsetY.toInt()} menu=${largura}x$altura",
        )
        params.width = largura
        params.height = altura
        params.x = pos.x
        params.y = pos.y
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun criarSelo(): ImageView {
        val tamanho = dp(SELO_DP)
        return ImageView(this).apply {
            setPadding(0, 0, 0, 0)
            minimumWidth = tamanho
            minimumHeight = tamanho
            maxWidth = tamanho
            maxHeight = tamanho
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(R.mipmap.ic_launcher_round)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.TRANSPARENT)
            }
            clipToOutline = true
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: android.graphics.Outline) {
                    outline.setOval(0, 0, view.width.coerceAtLeast(tamanho), view.height.coerceAtLeast(tamanho))
                }
            }
            setOnTouchListener(SeloTouchListener())
        }
    }

    private fun atualizarSelo(view: View, snapshot: OverlaySnapshot) {
        view.alpha = if (snapshot.monitorando) 1f else 0.55f
    }

    private fun criarCompacta(): LinearLayout {
        val cinzaEscuro = Color.parseColor(COR_CINZA_ESCURO)
        val cinzaMedio = Color.parseColor(COR_CINZA_MEDIO)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(6), dp(10), dp(6))
            background = fundoPainelCompacta(ClassificacaoConstantes.COR_BORDA_NEUTRA)
            gravity = Gravity.CENTER
        }
        layout.setOnTouchListener(CompactaTouchListener())

        val metricas = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            tag = "metricas"
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
        }
        metricas.addView(
            criarColunaCompacta(
                titulo = "R$/Km",
                tagValor = "v_rkm",
                destaque = true,
                corTitulo = cinzaEscuro,
                corValor = cinzaEscuro,
                peso = 1.15f,
            ),
        )
        metricas.addView(
            criarColunaCompacta(
                titulo = "Dist.",
                tagValor = "v_dist",
                destaque = false,
                corTitulo = cinzaMedio,
                corValor = cinzaMedio,
            ),
        )
        metricas.addView(
            criarColunaCompacta(
                titulo = "Tempo",
                tagValor = "v_tempo",
                destaque = false,
                corTitulo = cinzaMedio,
                corValor = cinzaMedio,
            ),
        )
        metricas.addView(
            criarColunaCompacta(
                titulo = "Nota",
                tagValor = "v_nota",
                destaque = false,
                corTitulo = cinzaMedio,
                corValor = cinzaMedio,
            ),
        )
        layout.addView(metricas)

        val contexto = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            tag = "contexto"
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
        }
        contexto.addView(
            TextView(this).apply {
                tag = "ctx_icone"
                text = "—"
                textSize = 13f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setPadding(dp(6), dp(2), dp(6), dp(2))
                setTextColor(Color.WHITE)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dp(6).toFloat()
                    setColor(Color.parseColor("#424242"))
                }
                minWidth = dp(28)
            },
        )
        contexto.addView(
            TextView(this).apply {
                tag = "ctx_paradas"
                text = "Parada(s)"
                textSize = 11f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setTextColor(cinzaMedio)
                setPadding(dp(10), 0, 0, 0)
                gravity = Gravity.CENTER_VERTICAL
            },
        )
        layout.addView(contexto)
        return layout
    }

    private fun criarColunaCompacta(
        titulo: String,
        tagValor: String,
        destaque: Boolean,
        corTitulo: Int,
        corValor: Int,
        peso: Float = 1f,
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, peso)
            addView(
                TextView(this@OverlayService).apply {
                    text = titulo
                    setTextColor(corTitulo)
                    textSize = if (destaque) 12f else 10f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    maxLines = 1
                },
            )
            addView(
                TextView(this@OverlayService).apply {
                    tag = tagValor
                    setTextColor(corValor)
                    textSize = if (destaque) 18f else 14f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    text = "—"
                },
            )
        }
    }

    private fun atualizarCompacta(view: View, snapshot: OverlaySnapshot) {
        val layout = view as LinearLayout
        val vazio = snapshot.aguardandoOferta && !snapshot.corridaAceita
        val rKm = if (vazio) "—" else soNumero(snapshot.valorPorKm).ifBlank { "—" }
        val dist = if (vazio) "—" else formatarDistCompacta(snapshot.kmTotal)
        val tempo = if (vazio) "—" else formatarTempoCompacta(snapshot.tempoHm, snapshot.tempo)
        val nota = if (vazio) "—" else snapshot.nota.ifBlank { "—" }

        layout.findViewWithTag<TextView>("v_rkm").text = rKm
        layout.findViewWithTag<TextView>("v_dist").text = dist
        layout.findViewWithTag<TextView>("v_tempo").text = tempo
        layout.findViewWithTag<TextView>("v_nota").text = nota

        val icone = layout.findViewWithTag<TextView>("ctx_icone")
        val (sigla, corIcone) = iconePlataforma(snapshot.plataformaSigla)
        icone.text = sigla
        icone.setTextColor(if (sigla == "99") Color.BLACK else Color.WHITE)
        (icone.background as? GradientDrawable)?.setColor(corIcone)

        val paradas = snapshot.quantidadeParadas
        val paradasView = layout.findViewWithTag<TextView>("ctx_paradas")
        paradasView.text = if (paradas > 0) "$paradas Parada(s)" else "Parada(s)"
        paradasView.setTextColor(
            Color.parseColor(if (paradas > 0) COR_CINZA_ESCURO else COR_CINZA_MEDIO),
        )

        layout.contentDescription =
            "R\$ por km $rKm, distância $dist, tempo $tempo, nota $nota"
        layout.background = fundoPainelCompacta(corBorda(snapshot))
    }

    private fun formatarDistCompacta(kmTexto: String): String {
        val limpo = kmTexto
            .replace("Km", "", ignoreCase = true)
            .replace("km", "", ignoreCase = true)
            .trim()
            .replace(",", ".")
        val n = limpo.toDoubleOrNull() ?: return if (kmTexto.isBlank() || kmTexto == "—") "—" else kmTexto
        return "%.1f Km".format(n).replace('.', ',')
    }

    private fun formatarTempoCompacta(tempoHm: String, tempo: String): String {
        val m = Regex("""(\d+)\s*h\s*(\d+)\s*m""", RegexOption.IGNORE_CASE).find(tempoHm)
        if (m != null) {
            val total = m.groupValues[1].toInt() * 60 + m.groupValues[2].toInt()
            return "$total Min"
        }
        val soMin = Regex("""(\d+(?:[.,]\d+)?)\s*min""", RegexOption.IGNORE_CASE).find(tempo)
            ?: Regex("""(\d+(?:[.,]\d+)?)""").find(tempoHm)
        if (soMin != null) {
            val raw = soMin.groupValues[1].replace(",", ".")
            val n = raw.toDoubleOrNull() ?: return tempoHm.ifBlank { "—" }
            return if (n % 1.0 == 0.0) {
                "${n.toInt()} Min"
            } else {
                "%.1f Min".format(n).replace('.', ',')
            }
        }
        return if (tempoHm.isBlank() || tempoHm == "—") "—" else tempoHm
    }

    private fun iconePlataforma(sigla: String): Pair<String, Int> {
        val s = sigla.trim().lowercase()
        return when {
            s.contains("99") -> "99" to Color.parseColor("#FFCC00")
            s.contains("in") -> "in" to Color.parseColor("#1DBF73")
            s.isBlank() || s == "—" -> "—" to Color.parseColor("#616161")
            else -> "U" to Color.parseColor("#000000")
        }
    }

    private fun criarColunaMetrica(
        icone: String,
        titulo: String,
        incluirLiquido: Boolean = false,
        comPeso: Boolean = true,
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = if (comPeso) {
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            } else {
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { setMargins(dp(4), 0, dp(4), 0) }
            }
            addView(
                TextView(this@OverlayService).apply {
                    text = "$icone $titulo"
                    setTextColor(Color.parseColor("#7CB342"))
                    textSize = 12f
                    gravity = Gravity.CENTER
                    maxLines = 1
                },
            )
            addView(
                TextView(this@OverlayService).apply {
                    setTextColor(OverlayTema.de(this@OverlayService).texto)
                    textSize = 13f
                    gravity = Gravity.CENTER
                    maxLines = 1
                    text = "—"
                },
            )
            if (incluirLiquido) {
                addView(
                    TextView(this@OverlayService).apply {
                        setTextColor(Color.parseColor("#7CB342"))
                        textSize = 9f
                        gravity = Gravity.CENTER
                        text = "LÍQUIDO —"
                    },
                )
            }
        }
    }

    private fun criarBlocoDetalhes(tag: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP
            this.tag = tag
            setPadding(dp(8), 0, dp(6), 0)
        }
    }

    private fun adicionarTituloBloco(destino: LinearLayout, texto: String, cor: String) {
        destino.addView(
            TextView(this).apply {
                text = texto
                setTextColor(Color.parseColor(cor))
                textSize = 12f
                gravity = Gravity.START
                setPadding(0, 0, 0, dp(2))
            },
        )
    }

    private fun adicionarLinhaDetalhe(destino: LinearLayout, titulo: String, valor: String) {
        val linha = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            setPadding(0, dp(1), 0, dp(1))
        }
        linha.addView(
            TextView(this).apply {
                text = titulo
                setTextColor(OverlayTema.de(this@OverlayService).detalhes)
                textSize = 12f
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            },
        )
        linha.addView(
            TextView(this).apply {
                text = valor
                setTextColor(OverlayTema.de(this@OverlayService).detalhes)
                textSize = 12f
                gravity = Gravity.END
                maxLines = 1
            },
        )
        destino.addView(linha)
    }

    private fun fundoPainel(corBorda: String, espessuraDp: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(OverlayTema.de(this@OverlayService).fundoPainel)
            setStroke(dp(espessuraDp), Color.parseColor(corBorda))
            cornerRadius = dp(10).toFloat()
        }
    }

    private fun fundoPainelCompacta(corBorda: String): GradientDrawable {
        return GradientDrawable().apply {
            setColor(OverlayTema.de(this@OverlayService).fundoPainel)
            setStroke(dp(BORDA_COMPACTA_DP), Color.parseColor(corBorda))
            cornerRadius = dp(8).toFloat()
        }
    }

    private fun criarParams(
        x: Int,
        y: Int,
        gravidade: Int = Gravity.TOP or Gravity.START,
        focavel: Boolean = false,
    ): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flagsJanela(focavel),
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = gravidade
            this.x = x
            this.y = y
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            // Não cobrir Home / Voltar / Recentes: respeita a barra do sistema.
            fitInsetsTypes = WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
        }
    }

    private fun flagsJanela(focavel: Boolean): Int {
        val base = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        return if (focavel) {
            base
        } else {
            base or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }
    }

    private fun aplicarFlagsJanela(params: WindowManager.LayoutParams, focavel: Boolean) {
        val fora = params.flags and WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        params.flags = flagsJanela(focavel) or fora
    }

    private fun cancelarCompactaNaFrente() {
        camadaHandler.removeCallbacksAndMessages(null)
    }

    private fun trazerCompactaParaFrente() {
        if (!OverlayBridge.snapshot.value.compactaVisivel) {
            return
        }
        val view = compactaView ?: return
        val params = compactaParams ?: return
        view.visibility = View.VISIBLE
        view.elevation = 48f
        // Só sobe se ainda não estiver na janela — evita remove/add em loop.
        if (view.isAttachedToWindow) {
            return
        }
        runCatching { windowManager.addView(view, params) }
    }

    private fun trazerSeloParaFrente() {
        val view = seloView ?: return
        val params = seloParams ?: return
        if (!view.isAttachedToWindow) {
            return
        }
        view.visibility = View.VISIBLE
        view.elevation = 56f
        runCatching { windowManager.removeViewImmediate(view) }
        runCatching { windowManager.addView(view, params) }
    }

    private fun aplicarFlagsToqueFora(params: WindowManager.LayoutParams, ativo: Boolean) {
        params.flags = if (ativo) {
            params.flags or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        } else {
            params.flags and WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH.inv()
        }
    }

    /**
     * Observa a barra inferior sem consumir o evento — Home, Voltar e Recentes
     * seguem o padrão do celular; o Gestor só reage em paralelo (selo / degraus).
     */
    private fun escutarBarraInferior(view: View) {
        view.setOnKeyListener { _, keyCode, evento ->
            if (evento.action != KeyEvent.ACTION_UP) {
                return@setOnKeyListener false
            }
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> OverlayBridge.emitir(OverlayAcao.VoltarBarra)
                KeyEvent.KEYCODE_HOME -> OverlayBridge.emitir(OverlayAcao.RecolherParaSelo)
                KeyEvent.KEYCODE_APP_SWITCH -> OverlayBridge.emitir(OverlayAcao.RecentesBarra)
            }
            // Sempre false: não bloquear a navegação do sistema.
            false
        }
    }

    private inner class SeloTouchListener : View.OnTouchListener {
        private var inicialX = 0
        private var inicialY = 0
        private var toqueX = 0f
        private var toqueY = 0f
        private var arrastou = false

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val params = seloParams ?: return false
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    inicialX = params.x
                    inicialY = params.y
                    toqueX = event.rawX
                    toqueY = event.rawY
                    arrastou = false
                    arrastandoSelo = true
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - toqueX).toInt()
                    val dy = (event.rawY - toqueY).toInt()
                    if (abs(dx) + abs(dy) > 12) {
                        arrastou = true
                        mostrarLixeira()
                    }
                    // Só move a view localmente; a posição salva só confirma no UP
                    // (assim o X não grava a base da tela / o topo padrão).
                    params.x = inicialX + dx
                    params.y = inicialY + dy
                    limitarPosicao(params, v)
                    windowManager.updateViewLayout(v, params)
                    return true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val sobreLixeira = event.actionMasked == MotionEvent.ACTION_UP &&
                        arrastou &&
                        seloSobreLixeira(params)
                    ocultarLixeira()
                    if (sobreLixeira) {
                        // Volta à posição de antes do arraste e esconde.
                        params.x = inicialX
                        params.y = inicialY
                        limitarPosicao(params, v)
                        runCatching { windowManager.updateViewLayout(v, params) }
                        OverlayBridge.emitir(OverlayAcao.MoverSelo(params.x.toFloat(), params.y.toFloat()))
                        OverlayBridge.emitir(OverlayAcao.EsconderSelo)
                        arrastandoSelo = false
                        return true
                    }
                    if (arrastou) {
                        OverlayBridge.emitir(OverlayAcao.MoverSelo(params.x.toFloat(), params.y.toFloat()))
                    } else if (event.actionMasked == MotionEvent.ACTION_UP) {
                        reabrirApp(origemCompacta = false)
                    }
                    arrastandoSelo = false
                    return true
                }
            }
            return false
        }
    }

    private fun reabrirApp(origemCompacta: Boolean) {
        OverlayBridge.emitir(OverlayAcao.Reabrir(origemCompacta))
    }

    private fun aplicarTamanhoCompacta(
        view: View,
        params: WindowManager.LayoutParams,
        reposicionar: Boolean = false,
    ) {
        val insets = insetsSeguros()
        val bounds = windowManager.currentWindowMetrics.bounds
        val larguraAlvo = mm(COMPACTA_LARGURA_MM)
        val alturaAlvo = mm(COMPACTA_ALTURA_MM)
        val maxLargura = (bounds.width() - insets.left - insets.right - dp(16))
            .coerceAtLeast(dp(180))
        params.width = larguraAlvo.coerceAtMost(maxLargura).coerceAtLeast(dp(180))
        params.height = alturaAlvo.coerceAtLeast(dp(52))
        params.gravity = Gravity.TOP or Gravity.START
        if (reposicionar) {
            params.x = insets.left + dp(8)
            // Topo, mas um pouco abaixo da status — evita cobrir Recusar da 99 (canto superior).
            params.y = insets.top + dp(8)
        }
        limitarPosicaoCompacta(params)
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun limitarPosicaoCompacta(params: WindowManager.LayoutParams) {
        val insets = insetsSeguros()
        val bounds = windowManager.currentWindowMetrics.bounds
        val maxX = (bounds.width() - insets.right - params.width).coerceAtLeast(insets.left)
        val maxY = (bounds.height() - insets.bottom - params.height).coerceAtLeast(insets.top)
        params.x = params.x.coerceIn(insets.left, maxX)
        params.y = params.y.coerceIn(insets.top, maxY)
    }

    private fun salvarPosicaoCompacta(params: WindowManager.LayoutParams) {
        prefsCompacta.edit()
            .putInt(KEY_COMPACTA_X, params.x)
            .putInt(KEY_COMPACTA_Y, params.y)
            .apply()
    }

    private inner class CompactaTouchListener : View.OnTouchListener {
        private var inicialX = 0
        private var inicialY = 0
        private var toqueX = 0f
        private var toqueY = 0f
        private var arrastou = false

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val params = compactaParams ?: return true
            when (event.actionMasked) {
                MotionEvent.ACTION_OUTSIDE -> return true
                MotionEvent.ACTION_DOWN -> {
                    inicialX = params.x
                    inicialY = params.y
                    toqueX = event.rawX
                    toqueY = event.rawY
                    arrastou = false
                    arrastandoCompacta = true
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - toqueX).toInt()
                    val dy = (event.rawY - toqueY).toInt()
                    if (abs(dx) + abs(dy) > 8) {
                        arrastou = true
                        params.x = inicialX + dx
                        params.y = inicialY + dy
                        limitarPosicaoCompacta(params)
                        runCatching { windowManager.updateViewLayout(v, params) }
                    }
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (arrastou) {
                        salvarPosicaoCompacta(params)
                    }
                    // Toque curto: não abre menu, não some.
                    arrastandoCompacta = false
                    return true
                }
            }
            return true
        }
    }

    private fun soNumero(valor: String): String =
        valor.replace("R$", "", ignoreCase = true).trim()

    private fun mostrarLixeira() {
        val tamanho = dp(SELO_DP)
        val insets = insetsSeguros()
        val params = lixeiraParams ?: criarParams(
            0,
            insets.bottom + dp(24),
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
        ).also { lixeiraParams = it }
        params.width = tamanho
        params.height = tamanho
        params.x = 0
        params.y = insets.bottom + dp(24)
        val view = lixeiraView ?: TextView(this).apply {
            text = "X"
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 22f
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#CC000000"))
                setStroke(dp(2), Color.WHITE)
            }
        }.also { nova ->
            val adicionou = runCatching { windowManager.addView(nova, params) }.isSuccess
            if (!adicionou) {
                return
            }
            lixeiraView = nova
        }
        view.visibility = View.VISIBLE
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun ocultarLixeira() {
        lixeiraView?.visibility = View.INVISIBLE
    }

    private fun seloSobreLixeira(selo: WindowManager.LayoutParams): Boolean {
        val lixeira = lixeiraParams ?: return false
        val tamanho = dp(SELO_DP)
        val bounds = windowManager.currentWindowMetrics.bounds
        val insets = insetsSeguros()
        val seloCx = selo.x + tamanho / 2
        val seloCy = selo.y + tamanho / 2
        val lixeiraCx = bounds.width() / 2
        val lixeiraCy = bounds.height() - insets.bottom - dp(24) - tamanho / 2
        val dx = seloCx - lixeiraCx
        val dy = seloCy - lixeiraCy
        return dx * dx + dy * dy < (tamanho * tamanho)
    }

    private fun dp(valor: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            valor.toFloat(),
            resources.displayMetrics,
        ).toInt()

    private fun mm(valor: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_MM,
            valor.toFloat(),
            resources.displayMetrics,
        ).toInt()

    private fun larguraPaineis(): Int {
        val insets = insetsSeguros()
        val bounds = windowManager.currentWindowMetrics.bounds
        return (bounds.width() - insets.left - insets.right - dp(24)).coerceAtLeast(dp(240))
    }

    private fun alturaMaximaAbaixoExpandida(): Int {
        val insets = insetsSeguros()
        val bounds = windowManager.currentWindowMetrics.bounds
        return (bounds.height() - insets.bottom - yAbaixoExpandida() - dp(8)).coerceAtLeast(dp(96))
    }

    private fun alturaPainelSecundario(): Int =
        alturaMaximaAbaixoExpandida()
            .coerceAtMost(dp(268) + mm(30) + mm(4))
            .coerceAtLeast(dp(220).coerceAtMost(alturaMaximaAbaixoExpandida()))

    private fun aplicarAlturaPainelSecundario(
        view: View,
        params: WindowManager.LayoutParams,
    ) {
        params.width = larguraPaineis()
        params.height = alturaPainelSecundario()
        params.x = 0
        params.y = yAbaixoExpandida()
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun yAbaixoExpandida(): Int {
        val insets = insetsSeguros()
        val altura = expandidaView?.height?.takeIf { it > 0 }
            ?: expandidaView?.measuredHeight?.takeIf { it > 0 }
            ?: dp(148)
        return insets.top + dp(6) + altura
    }

    private fun aplicarTamanhoDoConteudo(
        view: View,
        params: WindowManager.LayoutParams,
        alturaMaxima: Int? = null,
    ) {
        val largura = larguraPaineis()
        view.measure(
            View.MeasureSpec.makeMeasureSpec(largura, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        )
        val altura = view.measuredHeight.coerceAtLeast(1).let { medida ->
            if (alturaMaxima != null) medida.coerceAtMost(alturaMaxima) else medida
        }
        params.width = largura
        params.height = altura
        params.x = 0
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun atualizarPainel(view: View, params: WindowManager.LayoutParams) {
        aplicarTamanhoDoConteudo(view, params)
    }

    private fun reposicionarPaineisAbaixo(snapshot: OverlaySnapshot) {
        if (snapshot.historicoVisivel && !snapshot.configuracoesVisivel && !snapshot.dashboardVisivel) {
            historicoParams?.let { params ->
                historicoView?.let { view ->
                    val insets = insetsSeguros()
                    val bounds = windowManager.currentWindowMetrics.bounds
                    params.width = bounds.width() - insets.left - insets.right - dp(16)
                    params.height = bounds.height() - insets.top - insets.bottom - dp(16)
                    params.x = insets.left + dp(8)
                    params.y = insets.top + dp(8)
                    runCatching { windowManager.updateViewLayout(view, params) }
                }
            }
        }
        if (snapshot.configuracoesVisivel && !snapshot.dashboardVisivel && !snapshot.confirmacaoFecharVisivel) {
            configParams?.let { params ->
                configView?.let { view ->
                    val insets = insetsSeguros()
                    val bounds = windowManager.currentWindowMetrics.bounds
                    params.width = bounds.width() - insets.left - insets.right - dp(16)
                    params.height = bounds.height() - insets.top - insets.bottom - dp(16)
                    params.x = insets.left + dp(8)
                    params.y = insets.top + dp(8)
                    runCatching { windowManager.updateViewLayout(view, params) }
                }
            }
        }
        if (snapshot.dashboardVisivel && !snapshot.confirmacaoFecharVisivel) {
            dashboardParams?.let { params ->
                dashboardView?.let { view ->
                    val insets = insetsSeguros()
                    val bounds = windowManager.currentWindowMetrics.bounds
                    params.width = bounds.width() - insets.left - insets.right - dp(16)
                    params.height = bounds.height() - insets.top - insets.bottom - dp(16)
                    params.x = insets.left + dp(8)
                    params.y = insets.top + dp(8)
                    runCatching { windowManager.updateViewLayout(view, params) }
                }
            }
        }
        if (snapshot.confirmacaoLimparHistoricoVisivel && snapshot.historicoVisivel) {
            garantirConfirmacaoSobreHistorico(snapshot)
        }
    }

    private fun aplicarLayoutSeguro(view: View, params: WindowManager.LayoutParams) {
        val aplicar = {
            limitarPosicao(params, view)
            runCatching { windowManager.updateViewLayout(view, params) }
        }
        if (view.width == 0 || view.height == 0) {
            view.post { aplicar() }
        } else {
            aplicar()
        }
    }

    private fun limitarPosicao(params: WindowManager.LayoutParams, view: View) {
        val insets = insetsSeguros()
        val bounds = windowManager.currentWindowMetrics.bounds
        val margem = 8
        val largura = view.width.takeIf { it > 0 } ?: view.measuredWidth
        val altura = view.height.takeIf { it > 0 } ?: view.measuredHeight
        val minX = insets.left + margem
        val minY = insets.top + margem
        val maxX = (bounds.width() - insets.right - largura - margem).coerceAtLeast(minX)
        val maxY = (bounds.height() - insets.bottom - altura - margem).coerceAtLeast(minY)
        params.x = params.x.coerceIn(minX, maxX)
        params.y = params.y.coerceIn(minY, maxY)
    }

    private fun insetsSeguros(): android.graphics.Insets {
        return windowManager.currentWindowMetrics.windowInsets.getInsets(
            WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout(),
        )
    }

    private fun removerView(view: View?) {
        if (view != null) {
            runCatching { windowManager.removeView(view) }
        }
    }

    private fun criarCanal() {
        val canal = NotificationChannel(
            CANAL_ID,
            "Monitoramento Gestor Driver",
            NotificationManager.IMPORTANCE_LOW,
        )
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(canal)
    }

    private fun atualizarNotificacao(snapshot: OverlaySnapshot) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICACAO_ID, criarNotificacao(snapshot))
    }

    private fun criarNotificacao(snapshot: OverlaySnapshot = OverlaySnapshot()): Notification {
        val abrir = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        // Desligar App → confirmação na Activity (§44), não encerra direto.
        val desligar = PendingIntent.getActivity(
            this,
            1,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(MainActivity.EXTRA_CONFIRMAR_FECHAR, true),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val titulo: String
        val texto: String
        val detalhe: String
        if (!snapshot.aguardandoOferta && snapshot.valorTotal != "—") {
            titulo = "${snapshot.valorTotal} · ${snapshot.tempoHm} · ${snapshot.kmTotal}"
            texto = "$/km · $/Lucro · Consumo · Nota"
            detalhe = "$titulo\n$texto\n${soNumero(snapshot.valorPorKm)} · " +
                "${soNumero(snapshot.lucroEstimado)} · ${snapshot.litrosEstimados} · ${snapshot.nota}"
        } else {
            titulo = "Gestor Driver"
            texto = "Monitorando ofertas"
            detalhe = texto
        }
        return NotificationCompat.Builder(this, CANAL_ID)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detalhe))
            .setSmallIcon(R.drawable.ic_stat_monitor)
            .setContentIntent(abrir)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(0, "Abrir App", abrir)
            .addAction(0, "Desligar App", desligar)
            .build()
    }

    companion object {
        private const val CANAL_ID = "gestor_driver_monitoramento"
        private const val NOTIFICACAO_ID = 7101
        private const val BORDA_COMPACTA_DP = 6
        private const val COMPACTA_LARGURA_MM = 45
        private const val COMPACTA_ALTURA_MM = 17
        private const val COR_CINZA_ESCURO = "#424242"
        private const val COR_CINZA_MEDIO = "#757575"
        private const val PREFS_COMPACTA = "gestor_compacta_pos"
        private const val KEY_COMPACTA_X = "x"
        private const val KEY_COMPACTA_Y = "y"
        private const val SELO_DP = 60
        const val ACAO_PARAR = "br.com.gestordriver.overlay.PARAR"
        const val ACAO_ABRIR = "br.com.gestordriver.overlay.ABRIR"

        fun iniciar(context: Context) {
            if (!android.provider.Settings.canDrawOverlays(context)) {
                return
            }
            runCatching {
                context.startForegroundService(Intent(context, OverlayService::class.java))
            }
        }

        fun parar(context: Context) {
            runCatching {
                context.stopService(Intent(context, OverlayService::class.java))
            }
        }
    }
}
