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
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import br.com.gestordriver.GestorDriverApp
import br.com.gestordriver.MainActivity
import br.com.gestordriver.R
import br.com.gestordriver.navigation.NavegacaoLauncher
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
    private var seloParams: WindowManager.LayoutParams? = null

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
        return START_STICKY
    }

    override fun onDestroy() {
        removerView(seloView)
        removerView(compactaView)
        seloView = null
        compactaView = null
        scope.cancel()
        super.onDestroy()
    }

    private fun atualizarJanelas(snapshot: OverlaySnapshot) {
        if (!snapshot.monitorando) {
            stopSelf()
            return
        }
        if (snapshot.seloVisivel) {
            garantirSelo(snapshot)
        } else {
            removerView(seloView)
            seloView = null
        }
        if (snapshot.compactaVisivel) {
            garantirCompacta(snapshot)
        } else {
            removerView(compactaView)
            compactaView = null
        }
    }

    private fun garantirSelo(snapshot: OverlaySnapshot) {
        val params = seloParams ?: criarParams(
            snapshot.offsetX.toInt(),
            snapshot.offsetY.toInt(),
        ).also {
            seloParams = it
        }
        params.x = snapshot.offsetX.toInt()
        params.y = snapshot.offsetY.toInt()
        val view = seloView ?: criarSelo().also { nova ->
            seloView = nova
            windowManager.addView(nova, params)
        }
        atualizarSelo(view, snapshot)
        aplicarLayoutSeguro(view, params)
    }

    private fun garantirCompacta(snapshot: OverlaySnapshot) {
        val params = criarParams(24, 0)
        val view = compactaView ?: criarCompacta().also { nova ->
            compactaView = nova
            windowManager.addView(nova, params)
        }
        atualizarCompacta(view, snapshot)
        aplicarLayoutSeguro(view, params)
    }

    private fun criarSelo(): TextView {
        return TextView(this).apply {
            setPadding(28, 22, 28, 22)
            setBackgroundColor(Color.parseColor("#E62B3440"))
            setTextColor(Color.WHITE)
            textSize = 16f
            gravity = Gravity.CENTER
            setOnTouchListener(SeloTouchListener())
        }
    }

    private fun atualizarSelo(view: View, snapshot: OverlaySnapshot) {
        val texto = view as TextView
        texto.text = if (snapshot.monitorando) "◉\nMonitorando" else "◉\nParado"
        texto.setTextColor(
            if (snapshot.monitorando) Color.parseColor("#7CB342") else Color.parseColor("#C62828"),
        )
    }

    private fun criarCompacta(): LinearLayout {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.HORIZONTAL
        layout.setPadding(18, 14, 18, 14)
        layout.setBackgroundColor(Color.parseColor("#F2050809"))
        layout.setOnClickListener { abrirRotaOuReabrir() }
        repeat(5) {
            layout.addView(
                TextView(this).apply {
                    setTextColor(Color.WHITE)
                    textSize = 12f
                    setPadding(10, 0, 10, 0)
                },
            )
        }
        return layout
    }

    private fun atualizarCompacta(view: View, snapshot: OverlaySnapshot) {
        val layout = view as LinearLayout
        val valores = if (snapshot.aguardandoOferta) {
            listOf("Aguardando oferta", "", "", "", "")
        } else {
            listOf(
                "💵 ${snapshot.valorPorKm}",
                "💰 ${snapshot.valorTotal}",
                "🛞 ${snapshot.kmTotal}",
                "🕐 ${snapshot.tempo}",
                "⭐ ${snapshot.nota}",
            )
        }
        valores.forEachIndexed { index, valor ->
            (layout.getChildAt(index) as TextView).text = valor
        }
    }

    private fun criarParams(x: Int, y: Int): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
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
                        reabrirApp()
                    }
                    return true
                }
            }
            return false
        }
    }

    private fun abrirRotaOuReabrir() {
        val snapshot = OverlayBridge.snapshot.value
        val alvo = NavegacaoLauncher.destinoNavegacao(
            embarque = snapshot.enderecoEmbarque,
            destino = snapshot.enderecoDestino,
            corridaAceita = snapshot.corridaAceita,
        )
        if (alvo != null) {
            val app = application as GestorDriverApp
            NavegacaoLauncher.abrir(
                context = this,
                navegacao = app.configuracaoStore.carregar().navegacao,
                embarque = snapshot.enderecoEmbarque,
                destino = snapshot.enderecoDestino,
                corridaAceita = snapshot.corridaAceita,
            )
            return
        }
        reabrirApp()
    }

    private fun reabrirApp() {
        OverlayBridge.emitir(OverlayAcao.Reabrir)
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
        )
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
            Intent(this, MainActivity::class.java),
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
        const val ACAO_PARAR = "br.com.gestordriver.overlay.PARAR"

        fun iniciar(context: Context) {
            context.startForegroundService(Intent(context, OverlayService::class.java))
        }

        fun parar(context: Context) {
            context.stopService(Intent(context, OverlayService::class.java))
        }
    }
}
