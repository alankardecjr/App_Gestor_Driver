package br.com.gestordriver.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.service.notification.NotificationListenerService
import android.util.Log
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
import br.com.gestordriver.MainActivity
import br.com.gestordriver.R
import br.com.gestordriver.core.ClassificacaoConstantes
import br.com.gestordriver.ui.DecimalInput
import br.com.gestordriver.notification.RideNotificationListenerService
import br.com.gestordriver.notification.SessaoMonitoramento
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
    private var toqueNoSelo = false
    private var arrastandoCompacta = false
    private val fecharAtalhoFora = Runnable {
        if (!toqueNoSelo) {
            OverlayBridge.emitir(OverlayAcao.RecolherParaSelo)
        }
    }
    private var compactaLembradaX: Int? = null
    private var compactaLembradaY: Int? = null
    private var historicoAberto = false
    private var configAberto = false
    private var dashboardAberto = false
    private var confirmacaoAberto = false
    private val camadaHandler = Handler(Looper.getMainLooper())
    private val fonteValorCompacta: Typeface by lazy {
        Typeface.create(Typeface.SANS_SERIF, 900, false)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        criarCanal()
        runCatching {
            startForeground(NOTIFICACAO_ID, criarNotificacaoMonitoramento())
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
                if (!snapshot.monitorando) {
                    encerrarSemMonitoramento()
                    return@collect
                }
                atualizarNotificacao(snapshot)
                if (snapshot.compactaVisivel && compactaNova) {
                    agendarCompactaNaFrente()
                } else if (!snapshot.compactaVisivel) {
                    cancelarCompactaNaFrente()
                }
            }
        }
        scope.launch {
            OverlayBridge.reafirmarCamada.collect {
                if (OverlayBridge.snapshot.value.compactaVisivel) {
                    agendarCompactaNaFrente()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACAO_PARAR) {
            OverlayBridge.emitir(OverlayAcao.ConfirmarFechar)
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent?.action == ACAO_DESATIVAR) {
            // Desativar = pausa o monitoramento; o app continua aberto.
            OverlayBridge.emitir(OverlayAcao.DesativarMonitoramento)
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent?.action == ACAO_FECHAR_NOTIFICACAO) {
            OverlayBridge.marcarNotificacaoFechada()
            OverlayBridge.emitir(OverlayAcao.FecharNotificacao)
            return START_STICKY
        }
        if (intent?.action == ACAO_ABRIR) {
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            )
        }
        if (!OverlayBridge.snapshot.value.monitorando) {
            encerrarSemMonitoramento()
            return START_NOT_STICKY
        }
        atualizarJanelas(OverlayBridge.snapshot.value)
        if (OverlayBridge.snapshot.value.compactaVisivel) {
            agendarCompactaNaFrente()
        }
        runCatching {
            NotificationListenerService.requestRebind(
                android.content.ComponentName(this, RideNotificationListenerService::class.java),
            )
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val snap = OverlayBridge.snapshot.value
        val seloNaLixeira = SessaoMonitoramento.seloNaLixeira(this)
        if (snap.monitorando && snap.notificacaoFechada && seloNaLixeira) {
            SessaoMonitoramento.definir(false)
            OverlayBridge.desligarMonitoramentoNoSnapshot()
            OverlayBridge.emitir(OverlayAcao.DesativarMonitoramento)
            encerrarSemMonitoramento()
        }
        super.onTaskRemoved(rootIntent)
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

    private fun encerrarSemMonitoramento() {
        runCatching { stopForeground(STOP_FOREGROUND_REMOVE) }
        val avisos = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        avisos.cancel(NOTIFICACAO_ID)
        avisos.cancel(NOTIFICACAO_OFERTA_ID)
        stopSelf()
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
            if (compactaView?.visibility != View.VISIBLE) {
                compactaView?.visibility = View.VISIBLE
            }
            compactaView?.elevation = 48f
        } else if (compactaView?.visibility != View.INVISIBLE) {
            compactaView?.visibility = View.INVISIBLE
            desligarToqueForaCompacta()
        }
        if (snapshot.expandidaVisivel) {
            val jaVisivel = expandidaView?.visibility == View.VISIBLE
            garantirExpandida(snapshot)
            expandidaView?.visibility = View.VISIBLE
            if (!jaVisivel) {
                animarSaidaDoSelo(expandidaView)
            }
        } else {
            expandidaView?.animate()?.cancel()
            expandidaView?.visibility = View.INVISIBLE
        }
        if (snapshot.historicoVisivel && !snapshot.configuracoesVisivel && !snapshot.dashboardVisivel) {
            garantirHistorico(snapshot)
            animarPainelSecundario(historicoView, abrir = true, jaAberto = historicoAberto)
            historicoAberto = true
        } else {
            animarPainelSecundario(historicoView, abrir = false, jaAberto = historicoAberto)
            historicoAberto = false
        }
        if (snapshot.configuracoesVisivel && !snapshot.dashboardVisivel && !snapshot.confirmacaoFecharVisivel) {
            garantirConfig(snapshot)
            animarPainelSecundario(configView, abrir = true, jaAberto = configAberto)
            configAberto = true
        } else {
            animarPainelSecundario(configView, abrir = false, jaAberto = configAberto)
            configAberto = false
        }
        if (snapshot.dashboardVisivel && !snapshot.confirmacaoFecharVisivel) {
            garantirDashboard(snapshot)
            animarPainelSecundario(dashboardView, abrir = true, jaAberto = dashboardAberto)
            dashboardAberto = true
        } else {
            animarPainelSecundario(dashboardView, abrir = false, jaAberto = dashboardAberto)
            dashboardAberto = false
        }
        if (snapshot.confirmacaoLimparHistoricoVisivel && snapshot.historicoVisivel) {
            garantirConfirmacaoSobreHistorico(snapshot)
            confirmacaoView?.visibility = View.VISIBLE
            confirmacaoAberto = true
        } else {
            confirmacaoView?.visibility = View.INVISIBLE
            confirmacaoAberto = false
        }
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
            val adicionou = abrirJanela(nova, params)
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
        val params = compactaParams ?: criarParams(
            insets.left + dp(8),
            insets.top + dp(8),
            Gravity.TOP or Gravity.START,
            focavel = false,
        ).also { compactaParams = it }
        params.gravity = Gravity.TOP or Gravity.START
        if (!arrastandoCompacta) {
            aplicarPosicaoLembrada(params)
        }
        aplicarFlagsJanela(params, focavel = false)
        val view = compactaView ?: criarCompacta().also { nova ->
            val adicionou = abrirJanela(nova, params)
            if (!adicionou) {
                return
            }
            compactaView = nova
            escutarBarraInferior(nova)
        }
        atualizarCompacta(view, snapshot)
        aplicarFlagsToqueFora(params, ativo = false)
        aplicarTamanhoCompacta(view, params)
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
            val adicionou = abrirJanela(nova, params)
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
            val adicionou = abrirJanela(nova, params)
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
            val adicionou = abrirJanela(nova, params)
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
            val adicionou = abrirJanela(nova, params)
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
            val adicionou = abrirJanela(nova, params)
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
                camadaHandler.removeCallbacks(fecharAtalhoFora)
                camadaHandler.postDelayed(fecharAtalhoFora, 80)
                return@setOnTouchListener true
            }
            false
        }
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            tag = "menu_atalho_card"
            background = fundoMenuCard()
            elevation = dp(8).toFloat()
            outlineProvider = ViewOutlineProvider.BACKGROUND
            clipToOutline = true
            setPadding(dp(4), dp(4), dp(4), dp(4))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            )
        }
        montarListaAtalhos(card, snapshot)
        raiz.addView(card)
        return raiz
    }

    private fun atualizarExpandida(view: View, snapshot: OverlaySnapshot) {
        view.alpha = if (snapshot.monitorando) 1f else 0.9f
        val card = view.findViewWithTag<LinearLayout>("menu_atalho_card") ?: return
        card.background = fundoMenuCard()
        card.removeAllViews()
        if (snapshot.confirmacaoFecharVisivel) {
            montarConfirmacaoNoAtalho(card, snapshot)
            return
        }
        if (snapshot.confirmacaoMonitorVisivel) {
            montarConfirmacaoMonitor(card, snapshot)
            return
        }
        montarListaAtalhos(card, snapshot)
    }

    private fun montarConfirmacaoNoAtalho(card: LinearLayout, snapshot: OverlaySnapshot) {
        card.setPadding(dp(12), dp(14), dp(12), dp(12))
        card.addView(
            TextView(this).apply {
                text = "Gestor Driver"
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

    private fun montarConfirmacaoMonitor(card: LinearLayout, snapshot: OverlaySnapshot) {
        val ligado = snapshot.monitorando
        card.setPadding(dp(12), dp(14), dp(12), dp(12))
        card.addView(
            TextView(this).apply {
                text = if (ligado) "Desligar monitoramento" else "Ligar monitoramento"
                setTextColor(OverlayTema.de(this@OverlayService).texto)
                textSize = 15f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(8))
            },
        )
        card.addView(
            TextView(this).apply {
                text = if (ligado) {
                    "Desligar o monitoramento? O selo e o aviso somem. O app continua aberto."
                } else {
                    "Ligar o monitoramento? O selo e o aviso aparecem."
                }
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
                setOnClickListener { OverlayBridge.emitir(OverlayAcao.CancelarMonitoramento) }
            },
        )
        acoes.addView(
            TextView(this).apply {
                text = if (ligado) "Desligar" else "Ligar"
                setTextColor(Color.parseColor("#2E7D32"))
                textSize = 14f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setPadding(dp(12), dp(10), dp(12), dp(10))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener { OverlayBridge.emitir(OverlayAcao.ConfirmarMonitoramento) }
            },
        )
        card.addView(acoes)
    }

    private data class ItemMenuAtalho(
        val icone: Int,
        val titulo: String,
        val subtitulo: String,
        val perigo: Boolean = false,
        val ligado: Boolean = false,
        val acao: () -> Unit,
    )

    private fun itensAtalho(snapshot: OverlaySnapshot): List<ItemMenuAtalho> {
        val ligado = snapshot.monitorando
        return listOf(
            ItemMenuAtalho(
                R.drawable.ic_menu_monitorar,
                if (ligado) "Monitorar (ON)" else "Monitorar (Off)",
                "Calculadora de ganhos",
                ligado = ligado,
            ) {
                OverlayBridge.emitir(OverlayAcao.SolicitarMonitoramento)
            },
            ItemMenuAtalho(
                R.drawable.ic_menu_localizacao,
                "Localização",
                "Localização no mapa",
            ) {
                val fina = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                val grossa = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                val concedida = fina == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                    grossa == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (concedida) {
                    br.com.gestordriver.navigation.NavegacaoLauncher.abrirPosicaoAtual(this)
                } else {
                    OverlayBridge.emitir(OverlayAcao.PedirLocalizacao)
                }
            },
            ItemMenuAtalho(
                R.drawable.ic_menu_historico,
                "Histórico",
                "Ver corridas aceitas",
            ) {
                OverlayBridge.emitir(OverlayAcao.AbrirHistorico)
            },
            ItemMenuAtalho(
                R.drawable.ic_menu_carteira,
                "Dashboard",
                "Saldo e movimentações",
            ) {
                OverlayBridge.emitir(OverlayAcao.DashboardPro)
            },
            ItemMenuAtalho(
                R.drawable.ic_menu_despesas,
                "Despesas",
                "Controle de gastos",
            ) {
                OverlayBridge.emitir(OverlayAcao.AbrirAtalhoConfig(1))
            },
            ItemMenuAtalho(
                R.drawable.ic_menu_semaforo,
                "Semáforo",
                "Calibrar calculadora",
            ) {
                OverlayBridge.emitir(OverlayAcao.AbrirAtalhoConfig(0))
            },
            ItemMenuAtalho(
                R.drawable.ic_menu_usuario,
                "Usuário",
                "Informações do veiculo",
            ) {
                OverlayBridge.emitir(OverlayAcao.AbrirAtalhoConfig(2))
            },
            ItemMenuAtalho(
                R.drawable.ic_menu_sistema,
                "Sistema",
                "Configurar o sistema",
            ) {
                OverlayBridge.emitir(OverlayAcao.AbrirAtalhoConfig(3))
            },
            ItemMenuAtalho(R.drawable.ic_menu_fechar, "Fechar", "Encerrar aplicativo", perigo = true) {
                startActivity(
                    Intent(this@OverlayService, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra(MainActivity.EXTRA_CONFIRMAR_FECHAR, true),
                )
            },
        )
    }

    private fun montarListaAtalhos(card: LinearLayout, snapshot: OverlaySnapshot) {
        card.setPadding(dp(6), dp(6), dp(6), dp(4))
        val temaCabecalho = OverlayTema.de(this)
        card.addView(
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { bottomMargin = dp(4) }
                addView(
                    ImageView(this@OverlayService).apply {
                        setImageResource(R.mipmap.ic_launcher_round)
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        clipToOutline = true
                        outlineProvider = object : ViewOutlineProvider() {
                            override fun getOutline(view: View, outline: android.graphics.Outline) {
                                outline.setOval(0, 0, view.width, view.height)
                            }
                        }
                        layoutParams = LinearLayout.LayoutParams(dp(36), dp(36))
                    },
                )
                addView(
                    TextView(this@OverlayService).apply {
                        text = "Gestor Driver"
                        setTextColor(temaCabecalho.menuTexto)
                        textSize = 22f
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        maxLines = 1
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                            marginStart = dp(12)
                        }
                    },
                )
                addView(botaoCircular("✕", dp(28)) { OverlayBridge.emitir(OverlayAcao.RecolherParaSelo) })
            },
        )
        itensAtalho(snapshot).forEach { item ->
            card.addView(linhaMenuAtalho(item))
        }
    }

    private fun fundoMenuCard(): GradientDrawable {
        val tema = OverlayTema.de(this)
        return GradientDrawable().apply {
            setColor(tema.menu)
            setStroke(dp(1), tema.borda)
            cornerRadius = dp(16).toFloat()
        }
    }

    private fun divisorMenu(): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1),
            ).apply {
                marginStart = dp(8)
                marginEnd = dp(6)
            }
            setBackgroundColor(OverlayTema.de(this@OverlayService).borda)
        }
    }

    private fun linhaMenuAtalho(item: ItemMenuAtalho): LinearLayout {
        val tema = OverlayTema.de(this)
        val verde = Color.parseColor("#2E7D32")
        val corIcone = when {
            item.perigo -> Color.parseColor("#E53935")
            item.ligado -> verde
            else -> tema.secundario
        }
        val fundoIcone = when {
            item.perigo -> Color.parseColor("#33E53935")
            item.ligado -> Color.parseColor("#472E7D32")
            else -> tema.pocoIcone
        }
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(2), dp(6), dp(2))
            isClickable = true
            isFocusable = true
            background = GradientDrawable().apply {
                setColor(if (item.ligado) Color.parseColor("#332E7D32") else tema.card)
                setStroke(dp(1), if (item.ligado) Color.parseColor("#732E7D32") else tema.borda)
                cornerRadius = dp(12).toFloat()
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = dp(3) }
            setOnClickListener { item.acao() }
            addView(
                ImageView(this@OverlayService).apply {
                    setImageResource(item.icone)
                    setColorFilter(corIcone, android.graphics.PorterDuff.Mode.SRC_IN)
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                    setPadding(dp(5), dp(5), dp(5), dp(5))
                    background = GradientDrawable().apply {
                        setColor(fundoIcone)
                        cornerRadius = dp(8).toFloat()
                    }
                    layoutParams = LinearLayout.LayoutParams(dp(26), dp(26))
                },
            )
            addView(
                LinearLayout(this@OverlayService).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                        marginStart = dp(8)
                        marginEnd = dp(4)
                    }
                    addView(
                        TextView(this@OverlayService).apply {
                            text = item.titulo
                            setTextColor(if (item.perigo) corIcone else tema.menuTexto)
                            textSize = if (item.ligado) 11f else 12f
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                        },
                    )
                    addView(
                        TextView(this@OverlayService).apply {
                            text = item.subtitulo
                            setTextColor(tema.secundario)
                            textSize = 10f
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                        },
                    )
                },
            )
            addView(
                TextView(this@OverlayService).apply {
                    text = "›"
                    setTextColor(tema.secundario)
                    textSize = 16f
                    gravity = Gravity.CENTER
                },
            )
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
        val seloTam = dp(SELO_DP)
        val gap = dp(6)
        val margem = dp(4)
        val largura = dp(200).coerceAtMost(areaW - seloTam - gap - margem * 2).coerceAtLeast(dp(180))
        view.measure(
            View.MeasureSpec.makeMeasureSpec(largura, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        )
        val altura = view.measuredHeight.coerceAtLeast(1).coerceAtMost(areaH - margem * 2)
        val minX = insets.left + margem
        val maxX = (insets.left + areaW - largura - margem).coerceAtLeast(minX)
        val minY = insets.top + margem
        val maxY = (insets.top + areaH - altura - margem).coerceAtLeast(minY)
        val seloX = snapshot.offsetX.toInt()
        val seloY = snapshot.offsetY.toInt()
        val seloCentroX = seloX + seloTam / 2
        val seloCentroY = seloY + seloTam / 2
        val meioX = insets.left + areaW / 2
        val meioY = insets.top + areaH / 2
        val abreDireita = seloCentroX < meioX
        val abreBaixo = seloCentroY < meioY
        var x = if (abreDireita) seloX + seloTam + gap else seloX - gap - largura
        var y = if (abreBaixo) seloY else seloY + seloTam - altura
        if (x < minX || x > maxX) {
            x = if (abreDireita) seloX - gap - largura else seloX + seloTam + gap
        }
        if (y < minY || y > maxY) {
            y = if (abreBaixo) seloY + seloTam - altura else seloY
        }
        x = x.coerceIn(minX, maxX)
        y = y.coerceIn(minY, maxY)
        params.width = largura
        params.height = altura
        params.x = x
        params.y = y
        val paraDireita = x >= seloX
        val paraBaixo = y >= seloY
        view.pivotX = if (paraDireita) 0f else largura.toFloat()
        view.pivotY = if (paraBaixo) 0f else altura.toFloat()
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    /** O painel nasce no selo e as linhas descem em cascata. */
    private fun animarSaidaDoSelo(view: View?) {
        val alvo = view ?: return
        alvo.animate().cancel()
        alvo.scaleX = 0.08f
        alvo.scaleY = 0.08f
        alvo.alpha = 0f
        alvo.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(280)
            .setInterpolator(DecelerateInterpolator())
            .start()
        val card = alvo.findViewWithTag<LinearLayout>("menu_atalho_card") ?: return
        for (indice in 0 until card.childCount) {
            val filho = card.getChildAt(indice)
            filho.animate().cancel()
            filho.alpha = 0f
            filho.translationY = -dp(12).toFloat()
            filho.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(36L * indice)
                .setDuration(220)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
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
        val tema = OverlayTema.de(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(10), dp(12), dp(10))
            background = fundoCompacta(ClassificacaoConstantes.COR_BORDA_NEUTRA)
        }
        layout.setOnTouchListener(CompactaTouchListener())
        val cabecalho = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(6))
        }
        cabecalho.addView(
            TextView(this).apply {
                tag = "cmp_app"
                setTextColor(tema.texto)
                textSize = 14f
                maxLines = 1
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER_VERTICAL
                text = ""
            },
        )
        cabecalho.addView(
            TextView(this).apply {
                tag = "cmp_linha"
                setTextColor(tema.texto)
                textSize = 13f
                maxLines = 1
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                includeFontPadding = false
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = dp(16)
                }
                text = ""
            },
        )
        cabecalho.addView(
            TextView(this).apply {
                tag = "cmp_fechar"
                text = "✕"
                setTextColor(tema.texto)
                textSize = 12f
                includeFontPadding = false
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 0)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(tema.pocoIcone)
                }
                layoutParams = LinearLayout.LayoutParams(dp(22), dp(22)).apply {
                    marginStart = dp(6)
                    gravity = Gravity.CENTER_VERTICAL
                }
                isClickable = true
                isFocusable = true
                setOnClickListener { OverlayBridge.emitir(OverlayAcao.FecharCompacta) }
            },
        )
        layout.addView(cabecalho)
        val metricas = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            tag = "metricas"
            gravity = Gravity.CENTER
        }
        listOf(
            "R$/Km" to true,
            "R$/Hora" to true,
            "Nota" to false,
            "Lucro" to false,
        ).forEach { (titulo, barraGrossa) ->
            metricas.addView(criarColunaCompacta(titulo, barraGrossa))
        }
        val espacoTotal = dp(8) + dp(10)
        val espaco = espacoTotal / 3
        val sobra = espacoTotal % 3
        for (indice in 1..3) {
            val extra = if (indice <= sobra) 1 else 0
            (metricas.getChildAt(indice).layoutParams as LinearLayout.LayoutParams).marginStart =
                espaco + extra
        }
        layout.addView(metricas)
        val paradasLinha = LinearLayout(this).apply {
            tag = "linha_paradas"
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            visibility = View.GONE
            setPadding(0, dp(4), 0, 0)
        }
        paradasLinha.addView(
            TextView(this).apply {
                text = "⚑"
                setTextColor(Color.parseColor("#FFD600"))
                textSize = 13f
                gravity = Gravity.CENTER
            },
        )
        paradasLinha.addView(
            TextView(this).apply {
                tag = "ctx_paradas"
                setTextColor(tema.texto)
                textSize = 13f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                maxLines = 1
                setPadding(dp(6), 0, 0, 0)
                text = ""
            },
        )
        layout.addView(paradasLinha)
        return layout
    }

    private fun criarColunaCompacta(titulo: String, barraGrossa: Boolean): LinearLayout {
        val peso = when {
            titulo == "Lucro" -> 1.35f
            titulo == "Nota" -> 1.05f
            barraGrossa -> 1.55f
            else -> 0.7f
        }
        val folgaTitulo = dp(if (barraGrossa) 8 else 3) + dp(4)
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, peso)
            addView(
                LinearLayout(this@OverlayService).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    )
                    addView(
                        View(this@OverlayService).apply {
                            layoutParams = LinearLayout.LayoutParams(folgaTitulo, 1)
                        },
                    )
                    addView(
                        TextView(this@OverlayService).apply {
                            tag = "titulo"
                            text = titulo
                            setTextColor(OverlayTema.de(this@OverlayService).secundario)
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                            gravity = Gravity.CENTER
                            maxLines = 1
                            includeFontPadding = false
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f,
                            )
                            setAutoSizeTextTypeUniformWithConfiguration(9, 12, 1, TypedValue.COMPLEX_UNIT_SP)
                        },
                    )
                },
            )
            addView(
                LinearLayout(this@OverlayService).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    )
                    addView(
                        View(this@OverlayService).apply {
                            tag = "barra"
                            background = GradientDrawable().apply {
                                setColor(OverlayTema.de(this@OverlayService).borda)
                                cornerRadius = dp(2).toFloat()
                            }
                            layoutParams = LinearLayout.LayoutParams(
                                dp(if (barraGrossa) 8 else 3),
                                dp(if (barraGrossa) 24 else 18),
                            ).apply {
                                marginEnd = dp(4)
                            }
                        },
                    )
                    addView(
                        TextView(this@OverlayService).apply {
                            tag = "valor"
                            setTextColor(OverlayTema.de(this@OverlayService).texto)
                            typeface = fonteValorCompacta
                            gravity = Gravity.CENTER
                            maxLines = 1
                            includeFontPadding = false
                            ellipsize = null
                            layoutParams = if (barraGrossa) {
                                LinearLayout.LayoutParams(0, dp(28), 1f)
                            } else {
                                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                            }
                            setAutoSizeTextTypeUniformWithConfiguration(
                                if (barraGrossa) 20 else 11,
                                if (barraGrossa) 26 else 14,
                                1,
                                TypedValue.COMPLEX_UNIT_SP,
                            )
                            text = "—"
                        },
                    )
                },
            )
        }
    }

    private fun atualizarCompacta(view: View, snapshot: OverlaySnapshot) {
        val layout = view as LinearLayout
        val metricas = layout.findViewWithTag<LinearLayout>("metricas")
        val aguardando = snapshot.aguardandoOferta && !snapshot.corridaAceita
        val valores = if (aguardando) {
            listOf("—", "—", "—", "—")
        } else {
            listOf(
                DecimalInput.formatarDuasCasasExibicao(snapshot.valorPorKm),
                DecimalInput.formatarDuasCasasExibicao(snapshot.valorPorHora),
                DecimalInput.formatarDuasCasasExibicao(snapshot.nota),
                DecimalInput.formatarUmaCasaExibicao(snapshot.lucroPercentual),
            )
        }
        val tema = OverlayTema.de(this)
        val escuro = tema.texto == Color.WHITE
        val corLinha1 = if (escuro) Color.WHITE else Color.parseColor("#111111")
        val corTitulo = if (escuro) Color.parseColor("#D0D0D0") else Color.parseColor("#4E4E4E")
        val corValor = if (escuro) Color.WHITE else Color.parseColor("#111111")
        val corBotao = if (escuro) Color.parseColor("#D0D0D0") else Color.parseColor("#9E9E9E")
        val corMarcaBotao = if (escuro) Color.parseColor("#111111") else Color.WHITE
        val corBarraLucro = if (escuro) Color.parseColor("#D0D0D0") else Color.parseColor("#4E4E4E")
        val lucro = valores[3].let { if (it == "—") it else "$it%" }
        valores.forEachIndexed { index, valor ->
            val texto = if (index == 3) lucro else valor
            valorCompacta(metricas, index).apply {
                text = texto
                setTextColor(corValor)
                typeface = fonteValorCompacta
            }
            metricas.getChildAt(index).findViewWithTag<TextView>("titulo")?.apply {
                setTextColor(corTitulo)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
        }
        val corKm = if (aguardando) tema.borda else Color.parseColor(corBorda(snapshot))
        val corHora = if (aguardando) tema.borda else Color.parseColor(snapshot.corValorPorHora)
        val corNota = if (aguardando) tema.borda else Color.parseColor(snapshot.corNota)
        pintarMetrica(metricas, 0, corKm, corValor)
        pintarMetrica(metricas, 1, corHora, corValor)
        pintarMetrica(metricas, 2, corNota, corValor)
        pintarMetrica(metricas, 3, corBarraLucro, corValor)
        igualarPar(
            metricas,
            primeiro = 0,
            segundo = 1,
            maxSp = 26f,
            minSp = 20f,
        )
        igualarNotaELucro(metricas)

        layout.findViewWithTag<TextView>("cmp_app")?.apply {
            text = snapshot.plataformaSigla.ifBlank { "" }
            setTextColor(corLinha1)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val tempo = snapshot.tempo.takeUnless { aguardando || it.isBlank() || it == "—" }.orEmpty()
        val km = snapshot.kmTotal.takeUnless { aguardando || it.isBlank() || it == "—" }
            ?.let { if (it.contains("km", ignoreCase = true)) it else "$it km" }
            .orEmpty()
        val resumo = listOf(tempo, km).filter { it.isNotBlank() }.joinToString(" · ")
        layout.findViewWithTag<TextView>("cmp_linha")?.apply {
            text = resumo
            setTextColor(corLinha1)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        layout.findViewWithTag<TextView>("cmp_fechar")?.apply {
            setTextColor(corMarcaBotao)
            (background as? GradientDrawable)?.setColor(corBotao)
        }

        val paradas = snapshot.quantidadeParadas
        val linhaParadas = layout.findViewWithTag<LinearLayout>("linha_paradas")
        val paradasView = layout.findViewWithTag<TextView>("ctx_paradas")
        if (paradas > 0 && !aguardando) {
            linhaParadas.visibility = View.VISIBLE
            paradasView.text = if (paradas == 1) "1 PARADA" else "$paradas PARADAS"
        } else {
            linhaParadas.visibility = View.GONE
            paradasView.text = ""
        }
        layout.contentDescription = "R\$ por km ${valores[0]}, R\$ por hora ${valores[1]}, " +
            "tempo ${valores[2]}, nota ${valores[3]}"
        val borda = if (aguardando) ClassificacaoConstantes.COR_BORDA_NEUTRA else snapshot.corBordaCompacta
        layout.background = fundoCompacta(borda)
    }

    private fun botaoCircular(simbolo: String, tamanho: Int = dp(36), onClick: () -> Unit): TextView {
        val tema = OverlayTema.de(this)
        return TextView(this).apply {
            text = simbolo
            setTextColor(tema.menuTexto)
            textSize = if (tamanho < dp(32)) 14f else 16f
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(tema.pocoIcone)
            }
            layoutParams = LinearLayout.LayoutParams(tamanho, tamanho)
            setOnClickListener { onClick() }
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
                        text = "RESULTADO —"
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
            cornerRadius = dp(16).toFloat()
        }
    }

    private fun fundoCompacta(corBorda: String): GradientDrawable {
        val escuro = OverlayTema.de(this).texto == Color.WHITE
        val fundo = if (escuro) Color.argb(0xD4, 0x00, 0x00, 0x00) else Color.argb(0xD4, 0xFF, 0xFF, 0xFF)
        return GradientDrawable().apply {
            setColor(fundo)
            setStroke(dp(BORDA_COMPACTA_DP), Color.parseColor(corBorda))
            cornerRadius = dp(16).toFloat()
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

    private fun agendarCompactaNaFrente() = Unit

    private fun cancelarCompactaNaFrente() = Unit

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
                    toqueNoSelo = true
                    camadaHandler.removeCallbacks(fecharAtalhoFora)
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - toqueX).toInt()
                    val dy = (event.rawY - toqueY).toInt()
                    if (abs(dx) + abs(dy) > 12) {
                        arrastou = true
                    }
                    // Só move a view localmente; a posição salva só confirma no UP
                    // (assim o X não grava a base da tela / o topo padrão).
                    params.x = inicialX + dx
                    params.y = inicialY + dy
                    limitarPosicao(params, v)
                    if (arrastou && seloNaBase(params)) {
                        mostrarLixeira()
                    } else {
                        ocultarLixeira()
                    }
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
                        toqueNoSelo = false
                        return true
                    }
                    if (arrastou) {
                        OverlayBridge.emitir(OverlayAcao.MoverSelo(params.x.toFloat(), params.y.toFloat()))
                    } else if (event.actionMasked == MotionEvent.ACTION_UP) {
                        OverlayBridge.emitir(OverlayAcao.AlternarAtalhos)
                    }
                    arrastandoSelo = false
                    toqueNoSelo = false
                    return true
                }
            }
            return false
        }
    }

    private fun reabrirApp(origemCompacta: Boolean) {
        OverlayBridge.emitir(OverlayAcao.Reabrir(origemCompacta))
    }

    private fun igualarNotaELucro(metricas: LinearLayout) {
        igualarPar(metricas, primeiro = 2, segundo = 3, maxSp = 14f, minSp = 9f)
    }

    private fun igualarPar(
        metricas: LinearLayout,
        primeiro: Int,
        segundo: Int,
        maxSp: Float,
        minSp: Float,
    ) {
        val esquerda = valorCompacta(metricas, primeiro)
        val direita = valorCompacta(metricas, segundo)
        metricas.post {
            val paint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = fonteValorCompacta
            }
            fun cabe(view: TextView, sp: Float): Boolean {
                val largura = view.width - view.compoundPaddingLeft - view.compoundPaddingRight
                if (largura <= 0) {
                    return false
                }
                paint.textSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    sp,
                    resources.displayMetrics,
                )
                return paint.measureText(view.text.toString()) <= largura
            }
            var sp = maxSp
            while (sp > minSp && (!cabe(esquerda, sp) || !cabe(direita, sp))) {
                sp -= 0.5f
            }
            esquerda.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE)
            direita.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE)
            esquerda.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
            direita.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
        }
    }

    private fun valorCompacta(metricas: LinearLayout, indice: Int): TextView {
        val coluna = metricas.getChildAt(indice) as LinearLayout
        val linha = coluna.getChildAt(1) as LinearLayout
        return linha.findViewWithTag("valor")
    }

    private fun pintarMetrica(metricas: LinearLayout, indice: Int, corBarra: Int, corValor: Int) {
        val coluna = metricas.getChildAt(indice) as LinearLayout
        val linha = coluna.getChildAt(1) as LinearLayout
        (linha.findViewWithTag<View>("barra").background as GradientDrawable).setColor(corBarra)
        linha.findViewWithTag<TextView>("valor").setTextColor(corValor)
    }

    private fun soNumero(valor: String): String =
        valor.replace("R$", "", ignoreCase = true).trim()

    private fun aplicarTamanhoCompacta(view: View, params: WindowManager.LayoutParams) {
        val insets = insetsSeguros()
        val larguraTela = windowManager.currentWindowMetrics.bounds.width() - insets.left - insets.right
        val max = mm(46).coerceAtMost(larguraTela - dp(8))
        view.measure(
            View.MeasureSpec.makeMeasureSpec(max, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        )
        params.width = max
        params.height = view.measuredHeight.coerceAtLeast(1)
        params.gravity = Gravity.TOP or Gravity.START
        if (!arrastandoCompacta) {
            aplicarPosicaoLembrada(params)
            limitarPosicao(params, view)
        }
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun aplicarPosicaoLembrada(params: WindowManager.LayoutParams) {
        if (compactaLembradaX == null) {
            val prefs = getSharedPreferences(PREFS_COMPACTA, MODE_PRIVATE)
            if (prefs.contains(PREF_COMPACTA_X)) {
                compactaLembradaX = prefs.getInt(PREF_COMPACTA_X, 0)
                compactaLembradaY = prefs.getInt(PREF_COMPACTA_Y, 0)
            }
        }
        val insets = insetsSeguros()
        params.x = compactaLembradaX ?: (insets.left + dp(8))
        params.y = compactaLembradaY ?: (insets.top + dp(8))
    }

    private fun guardarPosicaoCompacta(x: Int, y: Int) {
        compactaLembradaX = x
        compactaLembradaY = y
        getSharedPreferences(PREFS_COMPACTA, MODE_PRIVATE)
            .edit()
            .putInt(PREF_COMPACTA_X, x)
            .putInt(PREF_COMPACTA_Y, y)
            .apply()
    }

    private inner class CompactaTouchListener : View.OnTouchListener {
        private var inicialX = 0
        private var inicialY = 0
        private var toqueX = 0f
        private var toqueY = 0f
        private var arrastou = false

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val params = compactaParams ?: return false
            when (event.actionMasked) {
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
                    }
                    params.x = inicialX + dx
                    params.y = inicialY + dy
                    limitarPosicao(params, v)
                    runCatching { windowManager.updateViewLayout(v, params) }
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (arrastou) {
                        limitarPosicao(params, v)
                        guardarPosicaoCompacta(params.x, params.y)
                        runCatching { windowManager.updateViewLayout(v, params) }
                    } else {
                        params.x = inicialX
                        params.y = inicialY
                    }
                    arrastandoCompacta = false
                    return true
                }
            }
            return false
        }
    }

    private fun mostrarLixeira() {
        val tamanho = dp(LIXEIRA_DP)
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
        params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        val view = lixeiraView ?: TextView(this).apply {
            text = "X"
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 28f
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#CC000000"))
                setStroke(dp(3), Color.WHITE)
            }
        }.also { nova ->
            val adicionou = abrirJanela(nova, params)
            if (!adicionou) {
                return
            }
            lixeiraView = nova
        }
        view.visibility = View.VISIBLE
        view.elevation = 64f
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun ocultarLixeira() {
        lixeiraView?.visibility = View.INVISIBLE
    }

    private fun seloNaBase(selo: WindowManager.LayoutParams): Boolean {
        val bounds = windowManager.currentWindowMetrics.bounds
        val insets = insetsSeguros()
        val faixa = (bounds.height() * 22 / 100).coerceAtLeast(dp(96))
        val centro = selo.y + dp(SELO_DP) / 2
        return centro >= bounds.height() - insets.bottom - faixa
    }

    private fun seloSobreLixeira(selo: WindowManager.LayoutParams): Boolean {
        if (lixeiraParams == null) return false
        val seloTam = dp(SELO_DP)
        val lixeiraTam = dp(LIXEIRA_DP)
        val bounds = windowManager.currentWindowMetrics.bounds
        val insets = insetsSeguros()
        val seloCx = selo.x + seloTam / 2
        val seloCy = selo.y + seloTam / 2
        val lixeiraCx = bounds.width() / 2
        val lixeiraCy = bounds.height() - insets.bottom - dp(24) - lixeiraTam / 2
        val dx = seloCx - lixeiraCx
        val dy = seloCy - lixeiraCy
        val alcance = (lixeiraTam * 0.72f).toInt()
        return dx * dx + dy * dy < alcance * alcance
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

    private fun abrirJanela(view: View, params: WindowManager.LayoutParams): Boolean {
        return runCatching { windowManager.addView(view, params) }
            .onFailure { Log.w("GestorOverlay", "Janela não abriu", it) }
            .isSuccess
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
        manager.notify(NOTIFICACAO_ID, criarNotificacaoMonitoramento())
        if (ofertaNaBarra(snapshot)) {
            manager.notify(NOTIFICACAO_OFERTA_ID, criarNotificacaoOferta(snapshot))
        } else {
            manager.cancel(NOTIFICACAO_OFERTA_ID)
        }
    }

    private fun ofertaNaBarra(snapshot: OverlaySnapshot): Boolean =
        snapshot.monitorando && !snapshot.aguardandoOferta && snapshot.valorTotal != "—"

    private fun criarNotificacaoMonitoramento(): Notification {
        val abrir = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(MainActivity.EXTRA_ABRIR_OPCOES, true),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, CANAL_ID)
            .setContentTitle("Gestor Driver")
            .setContentText("está monitorando o celular!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Gestor Driver está monitorando o celular!"),
            )
            .setSmallIcon(R.drawable.ic_stat_monitor)
            .setContentIntent(abrir)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun criarNotificacaoOferta(snapshot: OverlaySnapshot): Notification {
        val titulo = "${snapshot.valorTotal} · ${snapshot.tempoHm} · ${snapshot.kmTotal}"
        val numeros = "${soNumero(snapshot.valorPorKm)} · " +
            "${soNumero(snapshot.lucroEstimado)} · ${snapshot.litrosEstimados} · ${snapshot.nota}"
        val detalhe = "$titulo\nR$/km · Resultado · Consumo · Nota\n$numeros"
        return NotificationCompat.Builder(this, CANAL_ID)
            .setContentTitle(titulo)
            .setContentText(numeros)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detalhe))
            .setSmallIcon(R.drawable.ic_stat_monitor)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    companion object {
        private const val CANAL_ID = "gestor_driver_monitoramento"
        private const val NOTIFICACAO_ID = 7101
        private const val NOTIFICACAO_OFERTA_ID = 7102
        private const val BORDA_COMPACTA_DP = 6
        private const val SELO_DP = 52
        private const val LIXEIRA_DP = 96
        private const val PREFS_COMPACTA = "compacta_posicao"
        private const val PREF_COMPACTA_X = "x"
        private const val PREF_COMPACTA_Y = "y"
        const val ACAO_PARAR = "br.com.gestordriver.overlay.PARAR"
        const val ACAO_DESATIVAR = "br.com.gestordriver.overlay.DESATIVAR"
        const val ACAO_FECHAR_NOTIFICACAO = "br.com.gestordriver.overlay.FECHAR_NOTIFICACAO"
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
