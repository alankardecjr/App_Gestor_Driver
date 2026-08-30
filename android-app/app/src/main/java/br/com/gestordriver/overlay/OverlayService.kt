package br.com.gestordriver.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.service.notification.NotificationListenerService
import android.util.TypedValue
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
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
    private var confirmacaoView: View? = null
    private var seloParams: WindowManager.LayoutParams? = null
    private var compactaParams: WindowManager.LayoutParams? = null
    private var expandidaParams: WindowManager.LayoutParams? = null
    private var historicoParams: WindowManager.LayoutParams? = null
    private var configParams: WindowManager.LayoutParams? = null
    private var confirmacaoParams: WindowManager.LayoutParams? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        criarCanal()
        startForeground(NOTIFICACAO_ID, criarNotificacao())
        scope.launch {
            OverlayBridge.snapshot.collect { snapshot ->
                atualizarJanelas(snapshot)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACAO_PARAR) {
            stopSelf()
            return START_NOT_STICKY
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
        removerView(confirmacaoView)
        seloView = null
        compactaView = null
        expandidaView = null
        historicoView = null
        configView = null
        confirmacaoView = null
        seloParams = null
        compactaParams = null
        expandidaParams = null
        historicoParams = null
        configParams = null
        confirmacaoParams = null
        scope.cancel()
        super.onDestroy()
    }

    private fun atualizarJanelas(snapshot: OverlaySnapshot) {
        if (!snapshot.monitorando) {
            seloView?.visibility = View.INVISIBLE
            compactaView?.visibility = View.INVISIBLE
            expandidaView?.visibility = View.INVISIBLE
            historicoView?.visibility = View.INVISIBLE
            configView?.visibility = View.INVISIBLE
            confirmacaoView?.visibility = View.INVISIBLE
            return
        }
        garantirSelo(snapshot)
        seloView?.visibility = if (snapshot.seloVisivel) View.VISIBLE else View.INVISIBLE
        if (snapshot.compactaVisivel) {
            garantirCompacta(snapshot)
            compactaView?.visibility = View.VISIBLE
        } else {
            compactaView?.visibility = View.INVISIBLE
        }
        if (snapshot.expandidaVisivel) {
            garantirExpandida(snapshot)
            expandidaView?.visibility = View.VISIBLE
        } else {
            expandidaView?.visibility = View.INVISIBLE
        }
        if (snapshot.historicoVisivel) {
            garantirHistorico(snapshot)
            historicoView?.visibility = View.VISIBLE
        } else {
            historicoView?.visibility = View.INVISIBLE
        }
        if (snapshot.configuracoesVisivel) {
            garantirConfig(snapshot)
            configView?.visibility = View.VISIBLE
        } else {
            configView?.visibility = View.INVISIBLE
        }
        if (snapshot.confirmacaoFecharVisivel) {
            garantirConfirmacao(snapshot)
            confirmacaoView?.visibility = View.VISIBLE
        } else {
            confirmacaoView?.visibility = View.INVISIBLE
        }
    }

    private fun garantirSelo(snapshot: OverlaySnapshot) {
        val tamanho = dp(64)
        val params = seloParams ?: criarParams(
            snapshot.offsetX.toInt(),
            snapshot.offsetY.toInt(),
        ).also {
            seloParams = it
        }
        params.width = tamanho
        params.height = tamanho
        params.x = snapshot.offsetX.toInt()
        params.y = snapshot.offsetY.toInt()
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
        val params = compactaParams ?: criarParams(
            0,
            insets.top + dp(8),
            Gravity.TOP or Gravity.CENTER_HORIZONTAL,
        ).also { compactaParams = it }
        params.width = larguraPaineis()
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.x = 0
        params.y = insets.top + dp(8)
        val view = compactaView ?: criarCompacta().also { nova ->
            val adicionou = runCatching { windowManager.addView(nova, params) }.isSuccess
            if (!adicionou) {
                return
            }
            compactaView = nova
        }
        atualizarCompacta(view, snapshot)
        atualizarPainel(view, params)
    }

    private fun garantirExpandida(snapshot: OverlaySnapshot) {
        val insets = insetsSeguros()
        val params = expandidaParams ?: criarParams(
            0,
            insets.top + dp(6),
            Gravity.TOP or Gravity.CENTER_HORIZONTAL,
        ).also { expandidaParams = it }
        params.width = larguraPaineis()
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.x = 0
        params.y = insets.top + dp(6)
        val view = expandidaView ?: criarExpandida().also { nova ->
            val adicionou = runCatching { windowManager.addView(nova, params) }.isSuccess
            if (!adicionou) {
                return
            }
            expandidaView = nova
        }
        atualizarExpandida(view, snapshot)
        aplicarTamanhoDoConteudo(view, params)
        view.post {
            aplicarTamanhoDoConteudo(view, params)
            reposicionarPaineisAbaixo(snapshot)
        }
    }

    private fun garantirHistorico(snapshot: OverlaySnapshot) {
        val params = historicoParams ?: criarParams(
            0,
            yAbaixoExpandida(),
            Gravity.TOP or Gravity.CENTER_HORIZONTAL,
        ).also { historicoParams = it }
        params.width = larguraPaineis()
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.x = 0
        params.y = yAbaixoExpandida()
        val view = historicoView ?: OverlayPaineis.criarHistorico(this).also { nova ->
            val adicionou = runCatching { windowManager.addView(nova, params) }.isSuccess
            if (!adicionou) {
                return
            }
            historicoView = nova
        }
        OverlayPaineis.atualizarHistorico(view, snapshot)
        atualizarPainel(view, params)
    }

    private fun garantirConfig(snapshot: OverlaySnapshot) {
        val params = configParams ?: criarParams(
            0,
            yAbaixoExpandida(),
            Gravity.TOP or Gravity.CENTER_HORIZONTAL,
            focavel = true,
        ).also { configParams = it }
        params.width = larguraPaineis()
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.x = 0
        params.y = yAbaixoExpandida()
        val view = configView ?: OverlayPaineis.criarConfig(this).also { nova ->
            val adicionou = runCatching { windowManager.addView(nova, params) }.isSuccess
            if (!adicionou) {
                return
            }
            configView = nova
        }
        OverlayPaineis.atualizarConfig(view, snapshot)
        params.width = larguraPaineis()
        params.height = alturaPainelConfig()
        params.x = 0
        params.y = yAbaixoExpandida()
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun garantirConfirmacao(@Suppress("UNUSED_PARAMETER") snapshot: OverlaySnapshot) {
        val params = confirmacaoParams ?: criarParams(
            0,
            yAbaixoExpandida(),
            Gravity.TOP or Gravity.CENTER_HORIZONTAL,
        ).also { confirmacaoParams = it }
        params.width = larguraPaineis()
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.x = 0
        params.y = yAbaixoExpandida()
        val view = confirmacaoView ?: OverlayPaineis.criarConfirmacaoFechar(this).also { nova ->
            val adicionou = runCatching { windowManager.addView(nova, params) }.isSuccess
            if (!adicionou) {
                return
            }
            confirmacaoView = nova
        }
        atualizarPainel(view, params)
    }

    private fun corBorda(snapshot: OverlaySnapshot): String = snapshot.corClassificacao

    private fun criarExpandida(): LinearLayout {
        val coluna = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(4), dp(8), dp(2))
            background = fundoPainel(ClassificacaoConstantes.COR_BORDA_NEUTRA, BORDA_COMPACTA_DP)
        }
        val metricas = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            tag = "metricas"
        }
        metricas.addView(criarColunaMetrica("💵", "R$/KM"))
        metricas.addView(criarColunaMetrica("💰", "VALOR"))
        metricas.addView(criarColunaMetrica("🛞", "DIST."))
        metricas.addView(criarColunaMetrica("🕐", "TEMPO"))
        metricas.addView(criarColunaMetrica("⭐", "NOTA"))
        val controle = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        controle.addView(
            TextView(this).apply {
                text = "ℹ️"
                setTextColor(Color.WHITE)
                textSize = 14f
                gravity = Gravity.CENTER
                setOnClickListener { OverlayBridge.emitir(OverlayAcao.Retratil) }
            },
        )
        controle.addView(
            TextView(this).apply {
                tag = "seta_expandida"
                text = "⬆️"
                setTextColor(Color.WHITE)
                textSize = 14f
                gravity = Gravity.CENTER
                setOnClickListener { OverlayBridge.emitir(OverlayAcao.Retratil) }
            },
        )
        metricas.addView(controle)
        coluna.addView(metricas)
        val corpo = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(2), 0, dp(0))
        }
        corpo.addView(
            criarBlocoDetalhes("distancias").apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            },
        )
        corpo.addView(
            criarBlocoDetalhes("custos").apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            },
        )
        coluna.addView(corpo)
        val acoes = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            tag = "acoes"
        }
        listOf(
            "📴 Fechar" to { OverlayBridge.emitir(OverlayAcao.Fechar) },
            "⚙️ Config" to { OverlayBridge.emitir(OverlayAcao.AbrirConfig) },
            "❎ Ocultar" to { OverlayBridge.emitir(OverlayAcao.Ocultar) },
            "📜 Histórico" to { OverlayBridge.emitir(OverlayAcao.AbrirHistorico) },
        ).forEach { (rotulo, acao) ->
            acoes.addView(
                TextView(this).apply {
                    tag = when {
                        rotulo.contains("Histórico") -> "botao_historico"
                        rotulo.contains("Config") -> "botao_config"
                        else -> null
                    }
                    text = rotulo
                    setTextColor(Color.parseColor("#FFD54F"))
                    textSize = 11f
                    setPadding(dp(2), dp(2), dp(2), dp(2))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    gravity = Gravity.CENTER
                    setOnClickListener { acao() }
                },
            )
        }
        coluna.addView(acoes)
        return coluna
    }

    private fun atualizarExpandida(view: View, snapshot: OverlaySnapshot) {
        val coluna = view as LinearLayout
        val metricas = coluna.findViewWithTag<LinearLayout>("metricas")
        val valores = listOf(
            snapshot.valorPorKm,
            snapshot.valorTotal,
            snapshot.kmTotal,
            snapshot.tempo,
            snapshot.nota,
        )
        listOf("💵", "💰", "🛞", "🕐", "⭐").forEachIndexed { index, icone ->
            val bloco = metricas.getChildAt(index) as LinearLayout
            (bloco.getChildAt(0) as TextView).text = "$icone ${
                listOf("R$/KM", "VALOR", "DIST.", "TEMPO", "NOTA")[index]
            }"
            (bloco.getChildAt(1) as TextView).text = valores[index]
        }
        val acoes = coluna.findViewWithTag<LinearLayout>("acoes")
        val historicoBotao = acoes.findViewWithTag<TextView>("botao_historico")
        historicoBotao?.text = if (snapshot.historicoVisivel) "⤴️ Histórico" else "📜 Histórico"
        val configBotao = acoes.findViewWithTag<TextView>("botao_config")
        configBotao?.text = if (snapshot.configuracoesVisivel) "⤴️ Config" else "⚙️ Config"
        val distancias = coluna.findViewWithTag<LinearLayout>("distancias")
        distancias.removeAllViews()
        adicionarTituloBloco(distancias, "🛞 DISTÂNCIAS", "#42A5F5")
        adicionarLinhaDetalhe(distancias, "Até o passageiro", snapshot.kmAtePassageiro)
        adicionarLinhaDetalhe(distancias, "Até o destino", snapshot.kmViagem)
        adicionarLinhaDetalhe(distancias, "Total percorrido", snapshot.kmTotal)
        val custos = coluna.findViewWithTag<LinearLayout>("custos")
        custos.removeAllViews()
        adicionarTituloBloco(custos, "💰 CUSTOS (COMBUSTÍVEL)", "#7CB342")
        adicionarLinhaDetalhe(custos, "Consumo estimado", snapshot.litrosEstimados)
        adicionarLinhaDetalhe(custos, "Gasto estimado", snapshot.gastoEstimado)
        adicionarLinhaDetalhe(custos, "Lucro estimado", snapshot.lucroEstimado)
        view.background = fundoPainel(corBorda(snapshot), BORDA_COMPACTA_DP)
    }

    private fun criarSelo(): ImageView {
        val tamanho = dp(64)
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
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.HORIZONTAL
        layout.setPadding(dp(10), dp(6), dp(10), dp(6))
        layout.background = fundoPainel(ClassificacaoConstantes.COR_BORDA_NEUTRA, BORDA_COMPACTA_DP)
        layout.gravity = Gravity.CENTER_VERTICAL
        layout.setOnClickListener { reabrirApp(origemCompacta = true) }
        listOf("💵 R$/KM", "💰 VALOR", "🛞 DIST.", "🕐 TEMPO", "⭐ NOTA").forEach { titulo ->
            layout.addView(
                criarColunaMetrica(
                    icone = titulo.substringBefore(" "),
                    titulo = titulo.substringAfter(" "),
                    comPeso = true,
                ),
            )
        }
        val controle = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        controle.addView(
            TextView(this).apply {
                text = "ℹ️"
                setTextColor(Color.WHITE)
                textSize = 14f
                gravity = Gravity.CENTER
            },
        )
        controle.addView(
            TextView(this).apply {
                text = "⬇️"
                setTextColor(Color.WHITE)
                textSize = 14f
                gravity = Gravity.CENTER
            },
        )
        layout.addView(controle)
        return layout
    }

    private fun atualizarCompacta(view: View, snapshot: OverlaySnapshot) {
        val layout = view as LinearLayout
        val valores = if (snapshot.aguardandoOferta) {
            listOf("—", "—", "—", "—", "—")
        } else {
            listOf(
                snapshot.valorPorKm,
                snapshot.valorTotal,
                snapshot.kmTotal,
                snapshot.tempo,
                snapshot.nota,
            )
        }
        valores.forEachIndexed { index, valor ->
            val bloco = layout.getChildAt(index) as LinearLayout
            (bloco.getChildAt(1) as TextView).text = valor
        }
        layout.background = fundoPainel(corBorda(snapshot), BORDA_COMPACTA_DP)
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
                    textSize = 11.5f
                    gravity = Gravity.CENTER
                },
            )
            addView(
                TextView(this@OverlayService).apply {
                    setTextColor(Color.WHITE)
                    textSize = 14.5f
                    gravity = Gravity.CENTER
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
            this.tag = tag
            setPadding(dp(4), 0, dp(4), 0)
        }
    }

    private fun adicionarTituloBloco(destino: LinearLayout, texto: String, cor: String) {
        destino.addView(
            TextView(this).apply {
                text = texto
                setTextColor(Color.parseColor(cor))
                textSize = 11f
                setPadding(0, 0, 0, dp(2))
            },
        )
    }

    private fun adicionarLinhaDetalhe(destino: LinearLayout, titulo: String, valor: String) {
        destino.addView(
            TextView(this).apply {
                text = "$titulo  $valor"
                setTextColor(Color.parseColor("#D0D9E2"))
                textSize = 11f
                setPadding(0, dp(1), 0, dp(1))
            },
        )
    }

    private fun fundoPainel(corBorda: String, espessuraDp: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.parseColor("#F2050809"))
            setStroke(dp(espessuraDp), Color.parseColor(corBorda))
            cornerRadius = dp(10).toFloat()
        }
    }

    private fun criarParams(
        x: Int,
        y: Int,
        gravidade: Int = Gravity.TOP or Gravity.START,
        focavel: Boolean = false,
    ): WindowManager.LayoutParams {
        val flags = if (focavel) {
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        } else {
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        }
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = gravidade
            this.x = x
            this.y = y
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
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
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    inicialX = params.x
                    inicialY = params.y
                    toqueX = event.rawX
                    toqueY = event.rawY
                    arrastou = false
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - toqueX).toInt()
                    val dy = (event.rawY - toqueY).toInt()
                    if (abs(dx) + abs(dy) > 12) {
                        arrastou = true
                    }
                    params.x = inicialX + dx
                    params.y = inicialY + dy
                    limitarPosicao(params, v)
                    windowManager.updateViewLayout(v, params)
                    OverlayBridge.emitir(OverlayAcao.MoverSelo(params.x.toFloat(), params.y.toFloat()))
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (!arrastou) {
                        reabrirApp(origemCompacta = false)
                    }
                    return true
                }
            }
            return false
        }
    }

    private fun reabrirApp(origemCompacta: Boolean) {
        OverlayBridge.emitir(OverlayAcao.Reabrir(origemCompacta))
    }

    private fun dp(valor: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
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

    private fun alturaPainelConfig(): Int =
        alturaMaximaAbaixoExpandida().coerceAtMost(dp(268)).coerceAtLeast(dp(220))

    private fun yAbaixoExpandida(): Int {
        val insets = insetsSeguros()
        val altura = expandidaView?.height?.takeIf { it > 0 }
            ?: expandidaView?.measuredHeight?.takeIf { it > 0 }
            ?: dp(148)
        return insets.top + dp(6) + altura + dp(4)
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
        val y = yAbaixoExpandida()
        fun aplicar(view: View?, params: WindowManager.LayoutParams?, alturaMaxima: Int? = null) {
            if (view == null || params == null) {
                return
            }
            params.y = y
            aplicarTamanhoDoConteudo(view, params, alturaMaxima)
        }
        if (snapshot.historicoVisivel) {
            aplicar(historicoView, historicoParams)
        }
        if (snapshot.configuracoesVisivel) {
            configParams?.let { params ->
                params.y = y
                params.width = larguraPaineis()
                params.height = alturaPainelConfig()
                configView?.let { view ->
                    runCatching { windowManager.updateViewLayout(view, params) }
                }
            }
        }
        if (snapshot.confirmacaoFecharVisivel) {
            aplicar(confirmacaoView, confirmacaoParams)
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

    private fun criarNotificacao(): Notification {
        val pending = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ABRIR_EXPANDIDA, true),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CANAL_ID)
            .setContentTitle("Gestor Driver")
            .setContentText("Monitoramento de ofertas ativo")
            .setSmallIcon(R.drawable.ic_stat_monitor)
            .setContentIntent(pending)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CANAL_ID = "gestor_driver_monitoramento"
        private const val NOTIFICACAO_ID = 7101
        private const val BORDA_COMPACTA_DP = 5
        const val ACAO_PARAR = "br.com.gestordriver.overlay.PARAR"

        fun iniciar(context: Context) {
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
