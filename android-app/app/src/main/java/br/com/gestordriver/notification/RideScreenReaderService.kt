package br.com.gestordriver.notification

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import br.com.gestordriver.GestorDriverApp
import br.com.gestordriver.overlay.OverlayBridge
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Lê a oferta na tela. Uber costuma expor texto nos nós;
 * 99 (Flutter) quase não expõe — aí entra OCR.
 *
 * Screenshot e OCR usam um executor próprio. O executor de leitura
 * nunca espera o callback do screenshot (isso travava o A14).
 */
class RideScreenReaderService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private val leituraExecutor = Executors.newSingleThreadExecutor()
    private val ocrExecutor = Executors.newSingleThreadExecutor()
    private val ocrOcupado = AtomicBoolean(false)
    private val leituraOcupada = AtomicBoolean(false)
    private val reconhecedor by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }
    private var ultimoTexto: String = ""
    private var haviaOfertaNaTela: Boolean = false
    private var leiturasSemOferta: Int = 0
    private var ultimoLogMs: Long = 0
    private var ultimaCapturaMs: Long = 0
    private var ultimoEventoMs: Long = 0
    private var ultimoPacoteOcr: String = ""

    private val pipeline by lazy {
        val app = application as GestorDriverApp
        RideOfferPipeline(
            processor = RideNotificationProcessor(
                configuracaoProvider = { app.configuracaoStore.carregar() },
            ),
            diagnostico = app.diagnosticLog,
        )
    }

    private val diagnostico by lazy {
        (application as GestorDriverApp).diagnosticLog
    }

    private val soltarOcr = Runnable {
        if (ocrOcupado.get()) {
            ocrOcupado.set(false)
            registrarAmostra(ultimoPacoteOcr, "timeout", "OCR_TIMEOUT")
        }
    }

    private val poll = object : Runnable {
        override fun run() {
            agendarLeitura()
            handler.postDelayed(this, INTERVALO_NOS_MS)
        }
    }

    override fun onServiceConnected() {
        serviceInfo = serviceInfo.apply {
            flags = flags or
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 400
            packageNames = PlataformasMotorista.todosOsPacotes().toTypedArray()
        }
        handler.removeCallbacks(poll)
        handler.post(poll)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pacote = event?.packageName?.toString().orEmpty()
        if (!PlatformDetector.ehSuportada(pacote)) {
            return
        }
        val agora = System.currentTimeMillis()
        if (agora - ultimoEventoMs < DEBOUNCE_EVENTO_MS) {
            return
        }
        ultimoEventoMs = agora
        agendarLeitura()
    }

    override fun onInterrupt() {
        handler.removeCallbacks(poll)
        handler.removeCallbacks(soltarOcr)
    }

    override fun onDestroy() {
        handler.removeCallbacks(poll)
        handler.removeCallbacks(soltarOcr)
        leituraExecutor.shutdownNow()
        ocrExecutor.shutdownNow()
        super.onDestroy()
    }

    private fun agendarLeitura() {
        if (OverlayBridge.leituraPausada()) {
            return
        }
        if (!leituraOcupada.compareAndSet(false, true)) {
            return
        }
        handler.post {
            try {
                if (OverlayBridge.leituraPausada()) {
                    return@post
                }
                val captura = capturarNosNaMain()
                if (captura != null) {
                    leituraExecutor.execute {
                        decidirAposNos(captura.first, captura.second)
                    }
                }
            } finally {
                leituraOcupada.set(false)
            }
        }
    }

    private fun capturarNosNaMain(): Pair<String, String>? {
        val partes = linkedSetOf<String>()
        var pacotePlataforma = ""
        runCatching {
            windows?.forEach { janela ->
                if (janela.type != AccessibilityWindowInfo.TYPE_APPLICATION) {
                    return@forEach
                }
                val raiz = janela.root ?: return@forEach
                try {
                    val pacote = raiz.packageName?.toString().orEmpty()
                    if (!PlatformDetector.ehSuportada(pacote)) {
                        return@forEach
                    }
                    pacotePlataforma = pacote
                    coletar(raiz, partes, 0)
                } finally {
                    raiz.recycle()
                }
            }
        }
        if (pacotePlataforma.isBlank()) {
            return null
        }
        return pacotePlataforma to partes.joinToString("\n")
    }

    private fun decidirAposNos(pacote: String, textoNos: String) {
        runCatching { decidirAposNosInterno(pacote, textoNos) }
    }

    private fun decidirAposNosInterno(pacote: String, textoNos: String) {
        val temDados = OfertaTextoFiltro.temDadosParseaveis(textoNos) &&
            !OfertaTextoFiltro.ehPromocaoOuStatus(textoNos) &&
            !OfertaTextoFiltro.ehInterfaceGestor(textoNos)
        val notificationNos = NotificationData(
            packageName = pacote,
            title = "",
            text = textoNos,
            key = "tela:$pacote",
        )
        val aceiteNos = RideEventClassifier.pareceAceite(notificationNos)
        val uberIncompleto = PlatformDetector.resolver(pacote) == Plataforma.UBER &&
            OfertaTextoFiltro.cardKmIncompleto(textoNos)
        if (temDados || aceiteNos) {
            aplicarTexto(pacote, textoNos, "NOS")
            if (temDados && !uberIncompleto) {
                return
            }
        } else if (OfertaTextoFiltro.ehMapaSemCard(textoNos)) {
            aplicarTexto(pacote, textoNos, "NOS")
            return
        }
        pedirOcr(pacote)
    }

    private fun pedirOcr(pacote: String) {
        if (OverlayBridge.leituraPausada()) {
            return
        }
        val agora = System.currentTimeMillis()
        val intervalo = if (PlatformDetector.resolver(pacote) == Plataforma.UBER) {
            INTERVALO_OCR_UBER_MS
        } else {
            INTERVALO_OCR_MS
        }
        if (agora - ultimaCapturaMs < intervalo) {
            return
        }
        if (ocrOcupado.get() && agora - ultimaCapturaMs > OCR_TIMEOUT_MS) {
            ocrOcupado.set(false)
            registrarAmostra(pacote, "watchdog", "OCR_TIMEOUT")
        }
        if (!ocrOcupado.compareAndSet(false, true)) {
            return
        }
        ultimaCapturaMs = agora
        ultimoPacoteOcr = pacote
        handler.removeCallbacks(soltarOcr)
        handler.postDelayed(soltarOcr, OCR_TIMEOUT_MS)
        handler.post {
            runCatching {
                takeScreenshot(
                    Display.DEFAULT_DISPLAY,
                    ocrExecutor,
                    object : TakeScreenshotCallback {
                        override fun onSuccess(screenshot: ScreenshotResult) {
                            processarBitmap(pacote, screenshot)
                        }

                        override fun onFailure(errorCode: Int) {
                            encerrarOcr()
                            registrarAmostra(pacote, "codigo=$errorCode", "OCR_FALHA")
                        }
                    },
                )
            }.onFailure {
                encerrarOcr()
                registrarAmostra(pacote, it.message.orEmpty(), "OCR_FALHA")
            }
        }
    }

    private fun processarBitmap(pacote: String, screenshot: ScreenshotResult) {
        val bitmap = runCatching {
            val buffer = screenshot.hardwareBuffer
            val hardware = Bitmap.wrapHardwareBuffer(buffer, screenshot.colorSpace)
            val copia = hardware?.copy(Bitmap.Config.ARGB_8888, false)
            hardware?.recycle()
            buffer.close()
            copia
        }.getOrNull()
        if (bitmap == null) {
            encerrarOcr()
            registrarAmostra(pacote, "", "OCR_BITMAP_NULO")
            return
        }
        val recorte = runCatching { recortarOferta(bitmap) }.getOrElse {
            encerrarOcr()
            registrarAmostra(pacote, it.message.orEmpty(), "OCR_RECORTE")
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
            return
        }
        if (recorte != bitmap && !bitmap.isRecycled) {
            bitmap.recycle()
        }
        runCatching {
            reconhecedor.process(InputImage.fromBitmap(recorte, 0))
            .addOnSuccessListener(ocrExecutor) { resultado ->
                aplicarTexto(pacote, resultado.text, "OCR")
                if (!recorte.isRecycled) {
                    recorte.recycle()
                }
                encerrarOcr()
            }
            .addOnFailureListener(ocrExecutor) {
                registrarAmostra(pacote, it.message.orEmpty(), "OCR_FALHA")
                if (!recorte.isRecycled) {
                    recorte.recycle()
                }
                encerrarOcr()
            }
        }.onFailure {
            registrarAmostra(pacote, it.message.orEmpty(), "OCR_FALHA")
            if (!recorte.isRecycled) {
                recorte.recycle()
            }
            encerrarOcr()
        }
    }

    private fun encerrarOcr() {
        handler.removeCallbacks(soltarOcr)
        ocrOcupado.set(false)
    }

    private fun recortarOferta(origem: Bitmap): Bitmap {
        val software = if (origem.config == Bitmap.Config.HARDWARE) {
            origem.copy(Bitmap.Config.ARGB_8888, false) ?: origem
        } else {
            origem
        }
        val maxLargura = 540
        val trabalho = if (software.width > maxLargura) {
            val escala = maxLargura.toFloat() / software.width
            val reduzido = Bitmap.createScaledBitmap(
                software,
                maxLargura,
                (software.height * escala).toInt().coerceAtLeast(80),
                true,
            )
            if (software != origem && software != reduzido && !software.isRecycled) {
                software.recycle()
            }
            reduzido
        } else {
            software
        }
        if (trabalho.width < 40 || trabalho.height < 80) {
            return trabalho
        }
        val top = (trabalho.height * 0.38f).toInt().coerceIn(0, trabalho.height - 80)
        val recorte = Bitmap.createBitmap(trabalho, 0, top, trabalho.width, trabalho.height - top)
        if (trabalho != origem && trabalho != recorte && !trabalho.isRecycled) {
            trabalho.recycle()
        }
        return recorte
    }

    private fun aplicarTexto(pacote: String, texto: String, origem: String) {
        if (texto.isBlank()) {
            registrarAmostra(pacote, "", "TELA_VAZIA_$origem")
            if (haviaOfertaNaTela) {
                tratarPerdaDeOferta(pacote, texto)
            }
            return
        }
        val notification = NotificationData(
            packageName = pacote,
            title = "",
            text = texto,
            key = "tela:$pacote",
        )
        if (OfertaTextoFiltro.ehInterfaceGestor(texto)) {
            registrarAmostra(pacote, texto, "TELA_UI_GESTOR_$origem")
            return
        }
        val temOferta = OfertaTextoFiltro.temDadosParseaveis(texto) &&
            !OfertaTextoFiltro.ehPromocaoOuStatus(texto)
        val aceite = RideEventClassifier.pareceAceite(notification)
        registrarAmostra(
            pacote,
            texto,
            when {
                temOferta -> "TELA_OFERTA_$origem"
                aceite -> "TELA_ACEITE_$origem"
                else -> "TELA_LIDA_$origem"
            },
        )
        if (temOferta) {
            leiturasSemOferta = 0
            if (texto == ultimoTexto) {
                return
            }
            ultimoTexto = texto
            haviaOfertaNaTela = true
            enviarAoPipeline(pacote, texto)
            return
        }
        if (aceite) {
            enviarAoPipeline(pacote, texto)
            haviaOfertaNaTela = false
            leiturasSemOferta = 0
            ultimoTexto = ""
            return
        }
        tratarPerdaDeOferta(pacote, texto)
    }

    private fun enviarAoPipeline(pacote: String, texto: String) {
        val linhas = texto.lines().map { it.trim() }.filter { it.isNotBlank() }
        pipeline.processar(
            NotificationData(
                packageName = pacote,
                title = linhas.firstOrNull().orEmpty(),
                text = linhas.drop(1).joinToString("\n"),
                key = "tela:$pacote",
            ),
        )
    }

    private fun tratarPerdaDeOferta(pacote: String, texto: String) {
        if (!OfertaSessao.chaveAtiva(pacote)) {
            haviaOfertaNaTela = false
            leiturasSemOferta = 0
            return
        }
        leiturasSemOferta += 1
        when (
            OfertaTelaTransicao.decidir(
                textoAtual = texto,
                leiturasSemOferta = leiturasSemOferta,
                pacote = pacote,
            )
        ) {
            TransicaoTelaOferta.ACEITE -> {
                registrarAmostra(pacote, texto, "TELA_ACEITE")
                OfertaSessao.registrarAceite(pacote)
                RideNotificationBus.publish(RideNotificationEvent.CorridaAceita)
                haviaOfertaNaTela = false
                leiturasSemOferta = 0
                ultimoTexto = ""
            }
            TransicaoTelaOferta.EXPIRAR -> {
                registrarAmostra(pacote, texto, "TELA_EXPIRADA")
                OfertaSessao.limpar(pacote)
                RideNotificationBus.publish(RideNotificationEvent.CorridaExpirada)
                haviaOfertaNaTela = false
                leiturasSemOferta = 0
                ultimoTexto = ""
            }
            TransicaoTelaOferta.AGUARDAR -> Unit
        }
    }

    private fun registrarAmostra(pacote: String, texto: String, evento: String) {
        val agora = System.currentTimeMillis()
        val critico = evento.contains("OFERTA") ||
            evento.contains("ACEITE") ||
            evento.contains("TIMEOUT") ||
            evento.contains("FALHA")
        if (agora - ultimoLogMs < LOG_MS && !critico) {
            return
        }
        ultimoLogMs = agora
        diagnostico.registrar(
            NotificationData(
                packageName = pacote,
                title = evento,
                text = texto.take(900),
                key = "tela:$pacote",
            ),
            evento,
        )
    }

    private fun coletar(no: AccessibilityNodeInfo, destino: MutableSet<String>, visitados: Int): Int {
        if (visitados >= MAX_NOS || destino.size >= MAX_TEXTOS) {
            return visitados
        }
        val classe = no.className?.toString().orEmpty()
        if (classe.contains("SurfaceView") ||
            classe.contains("TextureView") ||
            classe.contains("MapView")
        ) {
            return visitados + 1
        }
        var contagem = visitados + 1
        listOf(no.text, no.contentDescription, no.hintText).forEach { valor ->
            val texto = valor?.toString()?.trim().orEmpty()
            if (texto.isNotBlank() && texto.length < 400) {
                destino.add(texto)
            }
        }
        val filhos = no.childCount.coerceAtMost(40)
        for (i in 0 until filhos) {
            if (contagem >= MAX_NOS) {
                break
            }
            val filho = no.getChild(i) ?: continue
            try {
                contagem = coletar(filho, destino, contagem)
            } finally {
                filho.recycle()
            }
        }
        return contagem
    }

    companion object {
        private const val INTERVALO_NOS_MS = 1100L
        private const val INTERVALO_OCR_MS = 2800L
        private const val INTERVALO_OCR_UBER_MS = 1600L
        private const val OCR_TIMEOUT_MS = 2800L
        private const val DEBOUNCE_EVENTO_MS = 500L
        private const val LOG_MS = 8000L
        private const val MAX_NOS = 180
        private const val MAX_TEXTOS = 100
    }
}
