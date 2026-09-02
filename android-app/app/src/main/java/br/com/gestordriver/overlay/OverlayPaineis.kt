package br.com.gestordriver.overlay

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import br.com.gestordriver.GestorDriverApp
import br.com.gestordriver.core.FaixasClassificacao
import br.com.gestordriver.MainActivity
import br.com.gestordriver.core.Classificacao
import br.com.gestordriver.core.ClassificacaoConstantes
import br.com.gestordriver.data.ContaVinculo
import br.com.gestordriver.model.AppNavegacao
import br.com.gestordriver.model.Combustivel
import br.com.gestordriver.model.ConfiguracaoUsuario
import br.com.gestordriver.model.TipoContaVinculada
import br.com.gestordriver.navigation.NavegacaoLauncher
import br.com.gestordriver.notification.Plataforma
import br.com.gestordriver.notification.PlataformasMotorista
import br.com.gestordriver.permission.PermissoesMonitoramento
import br.com.gestordriver.ui.DecimalInput
import kotlin.math.abs

object OverlayPaineis {
    private var rascunho: ConfiguracaoUsuario? = null
    private var abaMontada: Int = -1
    private var alturaMinimaConteudo: Int = 0
    private const val FUNDO = "#F2050809"
    private const val TEXTO = "#FFFFFF"
    private const val SECUNDARIO = "#B8C5D1"
    private const val AMARELO = "#FFD54F"
    private const val VERDE = "#7CB342"

    fun criarHistorico(context: Context): LinearLayout {
        val coluna = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = fundoNeutro(context)
            clipToOutline = true
            setPadding(0, dp(context, 8), 0, dp(context, 8))
            tag = "historico_coluna"
        }
        coluna.addView(
            tituloComSetas(
                context,
                "HISTÓRICO",
                onEsquerda = { avancarAbaHistorico(-1) },
                onDireita = { avancarAbaHistorico(1) },
            ).apply {
                setPadding(dp(context, 8), 0, dp(context, 8), dp(context, 6))
            },
        )
        val abas = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            tag = "historico_abas"
            gravity = Gravity.CENTER
            setPadding(dp(context, 8), 0, dp(context, 8), 0)
        }
        listOf("Uber", "99", "inDrive").forEach { aba ->
            abas.addView(
                TextView(context).apply {
                    text = aba.uppercase()
                    tag = "aba_$aba"
                    setTextColor(Color.parseColor(SECUNDARIO))
                    textSize = 12f
                    gravity = Gravity.CENTER
                    maxLines = 1
                    setPadding(dp(context, 2), dp(context, 5), dp(context, 2), dp(context, 5))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    setOnClickListener { OverlayBridge.emitir(OverlayAcao.AbaHistorico(aba)) }
                },
            )
        }
        coluna.addView(abas)
        val lista = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            tag = "historico_lista"
        }
        val scroll = ScrollView(context).apply {
            tag = "historico_scroll"
            isFillViewport = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f,
            )
            barraJuntoDaBorda(paddingInicioDp = 8)
            addView(lista)
        }
        coluna.addView(scroll)
        coluna.addView(
            TextView(context).apply {
                text = "🗑️ Limpar histórico"
                setTextColor(Color.parseColor(AMARELO))
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(dp(context, 8), dp(context, 10), dp(context, 8), dp(context, 4))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
                setOnClickListener { OverlayBridge.emitir(OverlayAcao.SolicitarLimparHistorico) }
            },
        )
        escutarFlingAbas(
            coluna,
            onProxima = { avancarAbaHistorico(1) },
            onAnterior = { avancarAbaHistorico(-1) },
        )
        escutarFlingAbas(
            scroll,
            onProxima = { avancarAbaHistorico(1) },
            onAnterior = { avancarAbaHistorico(-1) },
        )
        return coluna
    }

    fun criarConfirmacaoFechar(context: Context): LinearLayout {
        val coluna = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = fundoNeutro(context)
            setPadding(dp(context, 12), dp(context, 10), dp(context, 12), dp(context, 10))
        }
        coluna.addView(
            TextView(context).apply {
                tag = "confirmacao_titulo"
                text = "Fechar gestor driver"
                setTextColor(Color.WHITE)
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(context, 8))
            },
        )
        coluna.addView(
            TextView(context).apply {
                tag = "confirmacao_mensagem"
                text = "Deseja encerrar o aplicativo e parar o monitoramento de corridas?"
                setTextColor(Color.parseColor(SECUNDARIO))
                textSize = 12f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(context, 10))
            },
        )
        val acoes = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        acoes.addView(
            TextView(context).apply {
                tag = "confirmacao_cancelar"
                text = "Cancelar"
                setTextColor(Color.parseColor(SECUNDARIO))
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(dp(context, 16), dp(context, 8), dp(context, 16), dp(context, 8))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            },
        )
        acoes.addView(
            TextView(context).apply {
                tag = "confirmacao_confirmar"
                text = "Fechar"
                setTextColor(Color.parseColor(AMARELO))
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(dp(context, 16), dp(context, 8), dp(context, 16), dp(context, 8))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            },
        )
        coluna.addView(acoes)
        atualizarConfirmacao(coluna, limparHistorico = false)
        return coluna
    }

    fun atualizarConfirmacao(view: View, limparHistorico: Boolean) {
        view.findViewWithTag<TextView>("confirmacao_titulo")?.text =
            if (limparHistorico) "Limpar histórico" else "Fechar gestor driver"
        view.findViewWithTag<TextView>("confirmacao_mensagem")?.text =
            if (limparHistorico) {
                "Deseja apagar todas as corridas aceitas do histórico?"
            } else {
                "Deseja encerrar o aplicativo e parar o monitoramento de corridas?"
            }
        view.findViewWithTag<TextView>("confirmacao_confirmar")?.apply {
            text = if (limparHistorico) "Limpar" else "Fechar"
            setOnClickListener {
                OverlayBridge.emitir(
                    if (limparHistorico) OverlayAcao.ConfirmarLimparHistorico else OverlayAcao.ConfirmarFechar,
                )
            }
        }
        view.findViewWithTag<TextView>("confirmacao_cancelar")?.setOnClickListener {
            OverlayBridge.emitir(
                if (limparHistorico) OverlayAcao.CancelarLimparHistorico else OverlayAcao.CancelarFechar,
            )
        }
    }

    fun atualizarHistorico(view: View, snapshot: OverlaySnapshot) {
        val coluna = if (view.tag == "historico_coluna") {
            view as LinearLayout
        } else {
            view.findViewWithTag("historico_coluna") ?: return
        }
        coluna.background = fundoNeutro(coluna.context)
        val abas = coluna.findViewWithTag<LinearLayout>("historico_abas")
        listOf("Uber", "99", "inDrive").forEach { aba ->
            val rotulo = abas.findViewWithTag<TextView>("aba_$aba")
            val selecionada = snapshot.historicoAba.equals(aba, ignoreCase = true)
            rotulo.setTextColor(Color.parseColor(if (selecionada) VERDE else SECUNDARIO))
            rotulo.typeface = if (selecionada) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        val lista = coluna.findViewWithTag<LinearLayout>("historico_lista") ?: return
        lista.removeAllViews()
        lista.addView(
            encapsularLinhaHistorico(
                view.context,
                cabecalho = true,
                linhaHistorico(view.context, titulosHistorico, cabecalho = true),
            ),
        )
        if (snapshot.historicoItens.isEmpty()) {
            lista.addView(
                TextView(view.context).apply {
                    text = "Nenhuma corrida aceita."
                    setTextColor(Color.parseColor(SECUNDARIO))
                    textSize = 12f
                    setPadding(0, dp(view.context, 8), 0, 0)
                },
            )
            return
        }
        snapshot.historicoItens.forEach { item ->
            lista.addView(criarItemHistorico(view.context, item))
        }
    }

    fun criarConfig(context: Context): View {
        val ctx = ContextThemeWrapper(context, android.R.style.Theme_DeviceDefault)
        val moldura = FrameLayout(ctx).apply {
            tag = "config_frame"
        }
        val raiz = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background = fundoNeutro(ctx)
            clipToOutline = true
            descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
            isFocusable = true
            isFocusableInTouchMode = true
            setPadding(0, dp(ctx, 12), 0, dp(ctx, 12))
            tag = "config_coluna"
        }
        raiz.addView(
            tituloComSetas(
                ctx,
                "CONFIGURAÇÃO",
                onEsquerda = { avancarAbaConfig(-1) },
                onDireita = { avancarAbaConfig(1) },
            ).apply {
                setPadding(dp(ctx, 12), 0, dp(ctx, 12), dp(ctx, 6))
            },
        )
        val abas = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            tag = "config_abas"
            gravity = Gravity.CENTER
            setPadding(dp(ctx, 12), 0, dp(ctx, 12), 0)
        }
        listOf("VEÍCULO", "CUSTOS", "CALIBRAR", "APP").forEachIndexed { indice, titulo ->
            abas.addView(
                TextView(ctx).apply {
                    text = titulo
                    tag = "cfg_aba_$indice"
                    isClickable = true
                    isFocusable = true
                    isFocusableInTouchMode = false
                    setTextColor(Color.parseColor(SECUNDARIO))
                    textSize = 12f
                    gravity = Gravity.CENTER
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    setPadding(dp(ctx, 2), dp(ctx, 5), dp(ctx, 2), dp(ctx, 5))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    setOnClickListener { selecionarAbaConfig(indice) }
                },
            )
        }
        raiz.addView(abas)
        val scroll = ScrollView(ctx).apply {
            tag = "config_scroll"
            descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
            isFillViewport = true
            isFocusable = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f,
            )
            barraJuntoDaBorda(paddingInicioDp = 12)
        }
        scroll.addView(
            LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
                tag = "config_conteudo"
            },
        )
        raiz.addView(scroll)
        val rodape = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(ctx, 12), dp(ctx, 6), dp(ctx, 12), 0)
        }
        rodape.addView(
            TextView(ctx).apply {
                text = "Cancelar"
                setTextColor(Color.parseColor(SECUNDARIO))
                textSize = 12f
                gravity = Gravity.CENTER
                isClickable = true
                setPadding(dp(ctx, 8), dp(ctx, 8), dp(ctx, 8), dp(ctx, 8))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener { cancelarConfig(raiz) }
            },
        )
        rodape.addView(
            TextView(ctx).apply {
                text = "Salvar"
                setTextColor(Color.parseColor(VERDE))
                textSize = 12f
                gravity = Gravity.CENTER
                isClickable = true
                setPadding(dp(ctx, 8), dp(ctx, 8), dp(ctx, 8), dp(ctx, 8))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener { salvarConfig(raiz, ctx) }
            },
        )
        raiz.addView(rodape)
        escutarFlingAbas(
            raiz,
            onProxima = { avancarAbaConfig(1) },
            onAnterior = { avancarAbaConfig(-1) },
        )
        escutarFlingAbas(
            scroll,
            onProxima = { avancarAbaConfig(1) },
            onAnterior = { avancarAbaConfig(-1) },
        )
        moldura.addView(
            raiz,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )
        moldura.addView(criarDialogoContaGoogle(ctx))
        moldura.addView(criarDialogoContaEmail(ctx))
        return moldura
    }

    fun invalidarMontagem() {
        abaMontada = -1
    }

    fun atualizarContaVinculada(tipo: TipoContaVinculada, email: String) {
        rascunho = (rascunho ?: return).copy(contaTipo = tipo, contaEmail = email)
        abaMontada = -1
    }

    fun atualizarConfig(view: View, snapshot: OverlaySnapshot) {
        if (!snapshot.configuracoesVisivel) {
            descartarPainel(view)
            return
        }
        atualizarConfigInterno(view, snapshot)
        view.findViewWithTag<View>("config_coluna")?.background = fundoNeutro(view.context)
    }

    private fun selecionarAbaConfig(indice: Int) {
        OverlayBridge.emitir(OverlayAcao.AbaConfiguracao(indice))
        val atual = OverlayBridge.snapshot.value
        OverlayBridge.publicar(
            atual.copy(
                abaConfiguracao = indice,
                configuracoesVisivel = true,
                historicoVisivel = false,
            ),
        )
    }

    private fun avancarAbaConfig(direcao: Int) {
        val atual = OverlayBridge.snapshot.value.abaConfiguracao
        val novo = (atual + direcao).coerceIn(0, 3)
        if (novo != atual) {
            selecionarAbaConfig(novo)
        }
    }

    private fun avancarAbaHistorico(direcao: Int) {
        val abas = listOf("Uber", "99", "inDrive")
        val atualNome = OverlayBridge.snapshot.value.historicoAba
        val atual = abas.indexOfFirst { it.equals(atualNome, ignoreCase = true) }.coerceAtLeast(0)
        val novo = (atual + direcao).coerceIn(0, abas.lastIndex)
        if (novo != atual) {
            OverlayBridge.emitir(OverlayAcao.AbaHistorico(abas[novo]))
        }
    }

    private fun cancelarConfig(raiz: View) {
        descartarPainel(raiz)
        OverlayBridge.emitir(OverlayAcao.CancelarConfig)
    }

    private fun descartarPainel(view: View) {
        rascunho = null
        abaMontada = -1
        view.findViewWithTag<ScrollView>("config_scroll")?.let { scroll ->
            (scroll.getChildAt(0) as? LinearLayout)?.removeAllViews()
        }
        view.findViewWithTag<View>("dialogo_conta_google")?.visibility = View.GONE
        view.findViewWithTag<View>("dialogo_conta_email")?.visibility = View.GONE
    }

    private fun salvarConfig(raiz: View, context: Context) {
        val app = context.applicationContext as? GestorDriverApp ?: return
        val aba = OverlayBridge.snapshot.value.abaConfiguracao
        val base = rascunho ?: app.configuracaoStore.carregar()
        val final = FaixasClassificacao.normalizar(colherAba(raiz, aba, base))
        runCatching { app.configuracaoStore.salvar(final) }
        rascunho = null
        OverlayBridge.emitir(OverlayAcao.SalvarConfig)
    }

    private fun atualizarConfigInterno(view: View, snapshot: OverlaySnapshot) {
        val abas = view.findViewWithTag<LinearLayout>("config_abas") ?: return
        repeat(4) { indice ->
            val rotulo = abas.findViewWithTag<TextView>("cfg_aba_$indice") ?: return@repeat
            val selecionada = snapshot.abaConfiguracao == indice
            rotulo.setTextColor(Color.parseColor(if (selecionada) VERDE else SECUNDARIO))
            rotulo.typeface = if (selecionada) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        val scroll = view.findViewWithTag<ScrollView>("config_scroll") ?: return
        val conteudo = scroll.getChildAt(0) as? LinearLayout ?: return
        conteudo.tag = "config_conteudo"
        val app = view.context.applicationContext as? GestorDriverApp
            ?: view.context as? GestorDriverApp
            ?: return
        val store = app.configuracaoStore
        if (rascunho == null) {
            rascunho = store.carregar()
        }
        val abaAtual = abaMontada
        if (conteudo.childCount > 0 && abaAtual >= 0 && abaAtual != snapshot.abaConfiguracao) {
            rascunho = colherAba(view, abaAtual, rascunho ?: store.carregar())
        }
        if (conteudo.childCount > 0 && abaAtual == snapshot.abaConfiguracao) {
            if (snapshot.abaConfiguracao == 3) {
                atualizarPermissoes(conteudo, snapshot, view.context)
            }
            return
        }
        conteudo.removeAllViews()
        val config = rascunho ?: store.carregar()
        val montou = runCatching {
            when (snapshot.abaConfiguracao) {
                0 -> montarVeiculo(conteudo, config)
                1 -> montarCustos(conteudo, config)
                2 -> montarClassificacao(conteudo, config)
                else -> montarApp(conteudo, config, snapshot, view.context)
            }
        }
        if (montou.isSuccess) {
            abaMontada = snapshot.abaConfiguracao
            aplicarAlturaMinimaAba(conteudo)
        } else {
            abaMontada = -1
            conteudo.addView(
                TextView(view.context).apply {
                    text = "Não foi possível abrir esta aba."
                    setTextColor(Color.parseColor(AMARELO))
                    textSize = 12f
                    setPadding(0, dp(view.context, 8), 0, 0)
                },
            )
        }
        focarTopoSemCampo(view, scroll)
    }

    private fun focarTopoSemCampo(raiz: View, scroll: ScrollView) {
        val grupo = raiz as? ViewGroup
        grupo?.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
        raiz.isFocusableInTouchMode = true
        scroll.scrollTo(0, 0)
        raiz.clearFocus()
        raiz.requestFocus()
        ocultarTeclado(raiz)
        raiz.post {
            scroll.scrollTo(0, 0)
            raiz.clearFocus()
            raiz.requestFocus()
            ocultarTeclado(raiz)
        }
    }

    private fun ocultarTeclado(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private val titulosHistorico = listOf(
        "Data",
        "Hora",
        "R$/Km",
        "Valor",
        "Dist.",
        "Tempo",
        "Nota",
    )
    private val pesosColunaHistorico = floatArrayOf(0.85f, 0.75f, 1f, 1.2f, 1.15f, 1.2f, 0.85f)

    private fun linhaHistorico(
        context: Context,
        valores: List<String>,
        cabecalho: Boolean,
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            valores.forEachIndexed { indice, valor ->
                addView(
                    TextView(context).apply {
                        text = valor
                        setTextColor(Color.parseColor(if (cabecalho) SECUNDARIO else TEXTO))
                        textSize = if (cabecalho) 11f else 9f
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                        gravity = Gravity.CENTER
                        includeFontPadding = false
                        setPadding(0, 0, 0, 0)
                        typeface = if (cabecalho) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            pesosColunaHistorico.getOrElse(indice) { 1f },
                        )
                    },
                )
            }
        }
    }

    private fun encapsularLinhaHistorico(
        context: Context,
        cabecalho: Boolean,
        linha: View,
        corBorda: String = ClassificacaoConstantes.COR_BORDA_NEUTRA,
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.TRANSPARENT)
                setStroke(
                    dp(context, 2),
                    Color.parseColor(corBorda),
                )
                cornerRadius = dp(context, 8).toFloat()
            }
            setPadding(
                dp(context, if (cabecalho) 3 else 4),
                dp(context, 3),
                dp(context, if (cabecalho) 3 else 4),
                dp(context, 3),
            )
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            if (!cabecalho) {
                params.topMargin = dp(context, 5)
            }
            layoutParams = params
            addView(linha)
        }
    }

    private fun criarItemHistorico(context: Context, item: OverlayHistoricoItem): View {
        return encapsularLinhaHistorico(
            context,
            cabecalho = false,
            linhaHistorico(
                context,
                listOf(
                    item.data,
                    item.hora,
                    item.valorPorKm,
                    item.valor,
                    item.km,
                    item.tempo,
                    item.nota,
                ),
                cabecalho = false,
            ),
            corBorda = item.corMarcador,
        ).apply {
            setOnClickListener { OverlayBridge.emitir(OverlayAcao.SelecionarHistorico(item.chave)) }
        }
    }

    private fun montarVeiculo(
        destino: LinearLayout,
        config: ConfiguracaoUsuario,
    ) {
        val ctx = destino.context
        destino.addView(rotulo(ctx, "Descrição veículo", secao = true))
        destino.addView(
            linha(
                ctx,
                campo(ctx, "Marca", config.marcaVeiculo, "cfg_marca", compacto = true).first,
                campo(ctx, "Modelo", config.modeloVeiculo, "cfg_modelo", compacto = true).first,
            ),
        )
        destino.addView(
            linha(
                ctx,
                campo(ctx, "Versão", config.versaoVeiculo, "cfg_versao", compacto = true).first,
                campo(ctx, "Ano", config.anoVeiculo, "cfg_ano", compacto = true).first,
            ),
        )
        destino.addView(
            linha(
                ctx,
                campo(ctx, "Final da placa", config.finalPlaca, "cfg_placa", compacto = true).first,
                campo(ctx, "Ipva", config.ipvaVencimento, "cfg_ipva", bloqueado = true, compacto = true, pro = true).first,
            ),
        )
        destino.addView(rotulo(ctx, "Consumo km", secao = true))
        destino.addView(
            linha(
                ctx,
                campo(ctx, "Gasolina", DecimalInput.formatar(config.consumoGasolina), "cfg_consumo_g", compacto = true).first,
                campo(ctx, "Etanol", DecimalInput.formatar(config.consumoEtanol), "cfg_consumo_e", compacto = true).first,
            ),
        )
        destino.addView(rotuloPro(ctx, "Calcular abastecimento", compacto = true))
        destino.addView(
            linha(
                ctx,
                campo(ctx, "Valor total", "", "cfg_abast_valor", bloqueado = true, compacto = true).first,
                campo(ctx, "Litros total", "", "cfg_abast_litros", bloqueado = true, compacto = true).first,
            ),
        )
        destino.addView(
            linha(
                ctx,
                campo(ctx, "Km inicial", "", "cfg_abast_km_ini", bloqueado = true, compacto = true).first,
                campo(ctx, "Km final", "", "cfg_abast_km_fim", bloqueado = true, compacto = true).first,
            ),
        )
    }

    private fun montarCustos(
        destino: LinearLayout,
        config: ConfiguracaoUsuario,
    ) {
        val ctx = destino.context
        destino.addView(rotulo(ctx, "Valor do combustível", secao = true))
        destino.addView(
            linha(
                ctx,
                campo(ctx, "Litro gasolina", DecimalInput.formatar(config.precoGasolina), "cfg_preco_g").first,
                campo(ctx, "Litro etanol", DecimalInput.formatar(config.precoEtanol), "cfg_preco_e").first,
            ),
        )
        destino.addView(rotulo(ctx, "Combustível atual", secao = true))
        val ckG = CheckBox(ctx).apply {
            text = "Gasolina"
            tag = "cfg_ck_gasolina"
            setTextColor(Color.WHITE)
            textSize = 12f
            scaleX = 0.82f
            scaleY = 0.82f
            isFocusableInTouchMode = false
            isChecked = config.combustivel == Combustivel.GASOLINA
        }
        val ckE = CheckBox(ctx).apply {
            text = "Etanol"
            tag = "cfg_ck_etanol"
            setTextColor(Color.WHITE)
            textSize = 12f
            scaleX = 0.82f
            scaleY = 0.82f
            isFocusableInTouchMode = false
            isChecked = config.combustivel == Combustivel.ETANOL
        }
        ckG.setOnCheckedChangeListener { _, marcado ->
            if (marcado) {
                ckE.isChecked = false
                rascunho = (rascunho ?: config).copy(combustivel = Combustivel.GASOLINA)
            }
        }
        ckE.setOnCheckedChangeListener { _, marcado ->
            if (marcado) {
                ckG.isChecked = false
                rascunho = (rascunho ?: config).copy(combustivel = Combustivel.ETANOL)
            }
        }
        destino.addView(linha(ctx, ckG, ckE))
        destino.addView(rotuloPro(ctx, "Troca de óleo (óleo e filtros)", compacto = true))
        destino.addView(
            linha(
                ctx,
                campo(ctx, "Valor", "", "cfg_oleo_valor", bloqueado = true, compacto = true).first,
                campo(ctx, "Km", "", "cfg_oleo_km", bloqueado = true, compacto = true).first,
                campo(ctx, "Data", "", "cfg_oleo_data", bloqueado = true, compacto = true).first,
            ),
        )
        destino.addView(rotuloPro(ctx, "Custo estimado dos pneus", compacto = true))
        destino.addView(rotulo(ctx, "Dianteiro", compacto = true))
        destino.addView(
            linha(
                ctx,
                campo(ctx, "Valor", "", "cfg_pneu_d_valor", bloqueado = true, compacto = true).first,
                campo(ctx, "Rodagem", "", "cfg_pneu_d_km", bloqueado = true, compacto = true).first,
                campo(ctx, "Data", "", "cfg_pneu_d_data", bloqueado = true, compacto = true).first,
            ),
        )
        destino.addView(rotulo(ctx, "Traseiro", compacto = true))
        destino.addView(
            linha(
                ctx,
                campo(ctx, "Valor", "", "cfg_pneu_t_valor", bloqueado = true, compacto = true).first,
                campo(ctx, "Rodagem", "", "cfg_pneu_t_km", bloqueado = true, compacto = true).first,
                campo(ctx, "Data", "", "cfg_pneu_t_data", bloqueado = true, compacto = true).first,
            ),
        )
    }

    private fun montarApp(
        destino: LinearLayout,
        config: ConfiguracaoUsuario,
        snapshot: OverlaySnapshot,
        context: Context,
    ) {
        destino.addView(rotulo(context, "Configurar aplicativo", secao = true))
        destino.addView(rotulo(context, "Permissões", secao = true))
        destino.addView(
            linhaPermissoes(
                context,
                snapshot.destacarPermissoes,
                ItemPermissao("Notificações", PermissoesMonitoramento.listenerNotificacoesAtivo(context)) {
                    context.startActivity(PermissoesMonitoramento.intentNotificacoes().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                },
                ItemPermissao("Sobrepor", PermissoesMonitoramento.overlayConcedida(context)) {
                    context.startActivity(PermissoesMonitoramento.intentSobrepor(context).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                },
                ItemPermissao("Acessib.", PermissoesMonitoramento.acessibilidadeAtiva(context)) {
                    context.startActivity(
                        Intent(context, MainActivity::class.java)
                            .putExtra(MainActivity.EXTRA_PEDIR_ACESSIBILIDADE, true)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    )
                },
            ),
        )
        destino.addView(
            linhaPermissoes(
                context,
                snapshot.destacarPermissoes,
                ItemPermissao("Bateria", PermissoesMonitoramento.bateriaLiberada(context)) {
                    context.startActivity(
                        Intent(context, MainActivity::class.java)
                            .putExtra(MainActivity.EXTRA_PEDIR_BATERIA, true)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    )
                },
                ItemPermissao(
                    "Localização",
                    PermissoesMonitoramento.localizacaoConcedida(context),
                    obrigatoria = false,
                ) {
                    context.startActivity(
                        Intent(context, MainActivity::class.java)
                            .putExtra(MainActivity.EXTRA_PEDIR_LOCALIZACAO, true)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    )
                },
            ),
        )
        destino.addView(
            TextView(context).apply {
                text = "Acessib. lê o card. Bateria evita o overlay sumir. Localização opcional."
                setTextColor(Color.parseColor(SECUNDARIO))
                textSize = 10f
                setPadding(0, dp(context, 2), 0, dp(context, 4))
            },
        )
        val logVersao = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        logVersao.addView(
            TextView(context).apply {
                text = "Enviar log"
                setTextColor(Color.parseColor(AMARELO))
                textSize = 12f
                gravity = Gravity.CENTER
                setPadding(0, dp(context, 6), 0, dp(context, 6))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener {
                    context.startActivity(
                        Intent(context, MainActivity::class.java)
                            .putExtra(MainActivity.EXTRA_COMPARTILHAR_LOG, true)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    )
                }
            },
        )
        logVersao.addView(
            TextView(context).apply {
                text = "v${PermissoesMonitoramento.versaoApp(context)}"
                setTextColor(Color.parseColor(SECUNDARIO))
                textSize = 12f
                gravity = Gravity.CENTER
                setPadding(0, dp(context, 6), 0, dp(context, 6))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            },
        )
        destino.addView(logVersao)
        destino.addView(rotulo(context, "App de corrida", secao = true))
        val apps = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        listOf(
            Plataforma.UBER to "Uber",
            Plataforma.NOVE_NOVE to "99",
            Plataforma.INDRIVE to "Indrive",
        ).forEach { (plataforma, titulo) ->
            val ok = PlataformasMotorista.instalada(context, plataforma)
            apps.addView(
                TextView(context).apply {
                    text = if (ok) "$titulo  🆗" else "$titulo  ❎"
                    setTextColor(Color.parseColor(if (ok) VERDE else AMARELO))
                    textSize = 11f
                    gravity = Gravity.CENTER
                    setPadding(0, dp(context, 4), 0, dp(context, 4))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                },
            )
        }
        destino.addView(apps)
        destino.addView(rotulo(context, "Navegação", secao = true))
        val maps = CheckBox(context).apply {
            text = "Google maps"
            tag = "cfg_ck_maps"
            setTextColor(Color.WHITE)
            isFocusableInTouchMode = false
            isChecked = config.navegacao == AppNavegacao.GOOGLE_MAPS
        }
        val waze = CheckBox(context).apply {
            text = "Waze"
            tag = "cfg_ck_waze"
            setTextColor(Color.WHITE)
            isFocusableInTouchMode = false
            isChecked = config.navegacao == AppNavegacao.WAZE
        }
        maps.setOnCheckedChangeListener { _, marcado ->
            if (marcado) {
                waze.isChecked = false
                rascunho = (rascunho ?: config).copy(navegacao = AppNavegacao.GOOGLE_MAPS)
                NavegacaoLauncher.abrirAplicativo(context, AppNavegacao.GOOGLE_MAPS)
            }
        }
        waze.setOnCheckedChangeListener { _, marcado ->
            if (marcado) {
                maps.isChecked = false
                rascunho = (rascunho ?: config).copy(navegacao = AppNavegacao.WAZE)
                NavegacaoLauncher.abrirAplicativo(context, AppNavegacao.WAZE)
            }
        }
        destino.addView(linha(context, maps, waze))
        destino.addView(rotulo(context, "Conectar conta email", secao = true))
        val contas = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        contas.addView(
            botaoConta(
                context,
                if (config.contaTipo == TipoContaVinculada.GOOGLE) "Conta google 🆗" else "Conta google",
                "dialogo_conta_google",
            ),
        )
        contas.addView(
            botaoConta(
                context,
                if (config.contaTipo == TipoContaVinculada.EMAIL) "Conta email 🆗" else "Conta email",
                "dialogo_conta_email",
            ),
        )
        destino.addView(contas)
    }

    private fun botaoConta(context: Context, titulo: String, dialogo: String): TextView =
        TextView(context).apply {
            text = titulo
            setTextColor(Color.parseColor(AMARELO))
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(dp(context, 6), dp(context, 8), dp(context, 6), dp(context, 8))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { mostrarDialogo(this, dialogo) }
        }

    private fun mostrarDialogo(origem: View, tag: String) {
        painelConfig(origem)?.findViewWithTag<View>(tag)?.visibility = View.VISIBLE
    }

    private fun ocultarDialogo(origem: View, tag: String) {
        painelConfig(origem)?.findViewWithTag<View>(tag)?.visibility = View.GONE
    }

    private fun painelConfig(origem: View): View? {
        var atual: View? = origem
        while (atual != null) {
            if (atual.tag == "config_frame") {
                return atual
            }
            atual = atual.parent as? View
        }
        return origem.rootView
    }

    private fun molduraDialogo(context: Context, tag: String): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            this.tag = tag
            visibility = View.GONE
            background = fundoNeutro(context)
            setPadding(dp(context, 12), dp(context, 10), dp(context, 12), dp(context, 10))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                gravity = Gravity.CENTER
                marginStart = dp(context, 8)
                marginEnd = dp(context, 8)
            }
        }

    private fun criarDialogoContaGoogle(context: Context): LinearLayout {
        val coluna = molduraDialogo(context, "dialogo_conta_google")
        coluna.addView(
            TextView(context).apply {
                text = "Conta google"
                setTextColor(Color.WHITE)
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(context, 8))
            },
        )
        coluna.addView(
            TextView(context).apply {
                text = "Conecte a conta Google do motorista para identificar o usuário nas versões Free e Beta."
                setTextColor(Color.parseColor(SECUNDARIO))
                textSize = 12f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(context, 10))
            },
        )
        val acoes = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        acoes.addView(
            TextView(context).apply {
                text = "Cancelar"
                setTextColor(Color.parseColor(SECUNDARIO))
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(dp(context, 12), dp(context, 8), dp(context, 12), dp(context, 8))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener { ocultarDialogo(this, "dialogo_conta_google") }
            },
        )
        acoes.addView(
            TextView(context).apply {
                text = "Conectar"
                setTextColor(Color.parseColor(AMARELO))
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(dp(context, 12), dp(context, 8), dp(context, 12), dp(context, 8))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener {
                    ocultarDialogo(this, "dialogo_conta_google")
                    context.startActivity(
                        Intent(context, MainActivity::class.java)
                            .putExtra(MainActivity.EXTRA_CONECTAR_GOOGLE, true)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    )
                }
            },
        )
        coluna.addView(acoes)
        return coluna
    }

    private fun criarDialogoContaEmail(context: Context): LinearLayout {
        val coluna = molduraDialogo(context, "dialogo_conta_email")
        coluna.addView(
            TextView(context).apply {
                text = "Conta email"
                setTextColor(Color.WHITE)
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(context, 8))
            },
        )
        val campoEmail = campo(context, "E-mail", "", "cfg_conta_email", compacto = true).first
        coluna.addView(campoEmail)
        val aviso = TextView(context).apply {
            tag = "cfg_conta_email_erro"
            visibility = View.GONE
            text = "Informe um e-mail válido."
            setTextColor(Color.parseColor(AMARELO))
            textSize = 11f
            gravity = Gravity.CENTER
            setPadding(0, dp(context, 6), 0, 0)
        }
        coluna.addView(aviso)
        val acoes = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        acoes.addView(
            TextView(context).apply {
                text = "Cancelar"
                setTextColor(Color.parseColor(SECUNDARIO))
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(dp(context, 12), dp(context, 8), dp(context, 12), dp(context, 8))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener { ocultarDialogo(this, "dialogo_conta_email") }
            },
        )
        acoes.addView(
            TextView(context).apply {
                text = "Conectar"
                setTextColor(Color.parseColor(AMARELO))
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(dp(context, 12), dp(context, 8), dp(context, 12), dp(context, 8))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener {
                    val email = coluna.findViewWithTag<EditText>("cfg_conta_email")?.text?.toString().orEmpty()
                    if (!ContaVinculo.emailValido(email)) {
                        aviso.visibility = View.VISIBLE
                        return@setOnClickListener
                    }
                    persistirConta(coluna.context, TipoContaVinculada.EMAIL, email)
                    ocultarDialogo(this, "dialogo_conta_email")
                }
            },
        )
        coluna.addView(acoes)
        return coluna
    }

    private fun persistirConta(context: Context, tipo: TipoContaVinculada, email: String) {
        val app = context.applicationContext as? GestorDriverApp ?: return
        val base = app.configuracaoStore.carregar()
        val nova = ContaVinculo.aplicar(base, tipo, email)
        runCatching { app.configuracaoStore.salvar(nova) }
        rascunho = (rascunho ?: base).copy(contaTipo = tipo, contaEmail = email.trim())
        abaMontada = -1
        OverlayBridge.publicar(OverlayBridge.snapshot.value)
    }

    private fun montarClassificacao(destino: LinearLayout, config: ConfiguracaoUsuario) {
        val ctx = destino.context
        destino.addView(rotulo(ctx, "Calibrar classificações", secao = true))
        data class Faixa(
            val titulo: String,
            val cor: String,
            val minTexto: String,
            val maxTexto: String,
            val tagMin: String?,
            val tagMax: String?,
        )
        val faixas = listOf(
            Faixa("Ruim", ClassificacaoConstantes.CORES.getValue(Classificacao.RUIM), "Min", FaixasClassificacao.formatar(config.limiteRuimMax), null, "cfg_ruim_max"),
            Faixa(
                "Regular",
                ClassificacaoConstantes.CORES.getValue(Classificacao.REGULAR),
                FaixasClassificacao.formatar(config.limiteRegularMin),
                FaixasClassificacao.formatar(config.limiteRegularMax),
                "cfg_reg_min",
                "cfg_reg_max",
            ),
            Faixa(
                "Boa",
                ClassificacaoConstantes.CORES.getValue(Classificacao.BOA),
                FaixasClassificacao.formatar(config.limiteBoaMin),
                FaixasClassificacao.formatar(config.limiteBoaMax),
                "cfg_boa_min",
                "cfg_boa_max",
            ),
            Faixa(
                "Ótima",
                ClassificacaoConstantes.CORES.getValue(Classificacao.EXCELENTE),
                FaixasClassificacao.formatar(config.limiteOtimaMin),
                "Max",
                "cfg_otima_min",
                null,
            ),
        )
        faixas.forEach { faixa ->
            destino.addView(rotuloFaixa(ctx, faixa.titulo, faixa.cor))
            destino.addView(
                linha(
                    ctx,
                    stepper(
                        ctx,
                        "Min",
                        faixa.minTexto,
                        faixa.tagMin ?: "cfg_${faixa.titulo}_min_fixo",
                        faixa.tagMin != null,
                        onPasso = { tag, delta -> aplicarPassoFaixa(destino, tag, delta) },
                    ),
                    stepper(
                        ctx,
                        "Max",
                        faixa.maxTexto,
                        faixa.tagMax ?: "cfg_${faixa.titulo}_max_fixo",
                        faixa.tagMax != null,
                        onPasso = { tag, delta -> aplicarPassoFaixa(destino, tag, delta) },
                    ),
                ),
            )
        }
    }

    private fun aplicarPassoFaixa(raiz: View, tag: String, delta: Double) {
        val campo = campoFaixa(tag) ?: return
        val atual = rascunho ?: return
        val alvo = FaixasClassificacao.valorDe(atual, campo) + delta
        val encadeada = FaixasClassificacao.aplicar(atual, campo, alvo)
        rascunho = encadeada
        pintarFaixas(raiz, encadeada)
    }

    private fun campoFaixa(tag: String): FaixasClassificacao.Campo? =
        when (tag) {
            "cfg_ruim_max" -> FaixasClassificacao.Campo.RUIM_MAX
            "cfg_reg_min" -> FaixasClassificacao.Campo.REGULAR_MIN
            "cfg_reg_max" -> FaixasClassificacao.Campo.REGULAR_MAX
            "cfg_boa_min" -> FaixasClassificacao.Campo.BOA_MIN
            "cfg_boa_max" -> FaixasClassificacao.Campo.BOA_MAX
            "cfg_otima_min" -> FaixasClassificacao.Campo.OTIMA_MIN
            else -> null
        }

    private fun pintarFaixas(raiz: View, config: ConfiguracaoUsuario) {
        fun texto(tag: String, valor: Double) {
            raiz.findViewWithTag<TextView>(tag)?.text = FaixasClassificacao.formatar(valor)
        }
        texto("cfg_ruim_max", config.limiteRuimMax)
        texto("cfg_reg_min", config.limiteRegularMin)
        texto("cfg_reg_max", config.limiteRegularMax)
        texto("cfg_boa_min", config.limiteBoaMin)
        texto("cfg_boa_max", config.limiteBoaMax)
        texto("cfg_otima_min", config.limiteOtimaMin)
    }

    private fun colherAba(raiz: View, aba: Int, base: ConfiguracaoUsuario): ConfiguracaoUsuario {
        fun txt(tag: String): String? = raiz.findViewWithTag<EditText>(tag)?.text?.toString()
        fun num(tag: String, atual: Double): Double = DecimalInput.parse(txt(tag) ?: "") ?: atual
        return when (aba) {
            0 -> base.copy(
                    marcaVeiculo = txt("cfg_marca") ?: base.marcaVeiculo,
                    modeloVeiculo = txt("cfg_modelo") ?: base.modeloVeiculo,
                    versaoVeiculo = txt("cfg_versao") ?: base.versaoVeiculo,
                    anoVeiculo = txt("cfg_ano") ?: base.anoVeiculo,
                    finalPlaca = txt("cfg_placa") ?: base.finalPlaca,
                    consumoGasolina = num("cfg_consumo_g", base.consumoGasolina),
                    consumoEtanol = num("cfg_consumo_e", base.consumoEtanol),
                )
            1 -> {
                val gasolinaMarcada = raiz.findViewWithTag<CheckBox>("cfg_ck_gasolina")?.isChecked
                base.copy(
                    precoGasolina = num("cfg_preco_g", base.precoGasolina),
                    precoEtanol = num("cfg_preco_e", base.precoEtanol),
                    combustivel = when (gasolinaMarcada) {
                        true -> Combustivel.GASOLINA
                        false -> Combustivel.ETANOL
                        null -> base.combustivel
                    },
                )
            }
            2 -> {
                fun faixa(tag: String, atual: Double): Double {
                    val texto = raiz.findViewWithTag<TextView>(tag)?.text?.toString()
                    return DecimalInput.parse(texto ?: "") ?: atual
                }
                base.copy(
                    limiteRuimMax = faixa("cfg_ruim_max", base.limiteRuimMax),
                    limiteRegularMin = faixa("cfg_reg_min", base.limiteRegularMin),
                    limiteRegularMax = faixa("cfg_reg_max", base.limiteRegularMax),
                    limiteBoaMin = faixa("cfg_boa_min", base.limiteBoaMin),
                    limiteBoaMax = faixa("cfg_boa_max", base.limiteBoaMax),
                    limiteOtimaMin = faixa("cfg_otima_min", base.limiteOtimaMin),
                )
            }
            else -> {
                val maps = raiz.findViewWithTag<CheckBox>("cfg_ck_maps")?.isChecked
                base.copy(
                    navegacao = when (maps) {
                        true -> AppNavegacao.GOOGLE_MAPS
                        false -> AppNavegacao.WAZE
                        null -> base.navegacao
                    },
                )
            }
        }
    }

    private data class ItemPermissao(
        val titulo: String,
        val ok: Boolean,
        val obrigatoria: Boolean = true,
        val onClick: () -> Unit,
    )

    private fun linhaPermissoes(
        context: Context,
        destacar: Boolean,
        vararg itens: ItemPermissao,
    ): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            val laterais = if (itens.size <= 2) 0.45f else 0f
            if (laterais > 0f) {
                addView(View(context), LinearLayout.LayoutParams(0, 1, laterais))
            }
            itens.forEach { item ->
                addView(
                    linhaPermissao(
                        context,
                        item.titulo,
                        item.ok,
                        destacar && item.obrigatoria && !item.ok,
                        item.onClick,
                    ).apply {
                        gravity = Gravity.CENTER
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    },
                )
            }
            if (laterais > 0f) {
                addView(View(context), LinearLayout.LayoutParams(0, 1, laterais))
            }
        }

    private fun atualizarPermissoes(conteudo: LinearLayout, snapshot: OverlaySnapshot, context: Context) {
        fun pintar(titulo: String, ok: Boolean, obrigatoria: Boolean) {
            conteudo.findViewWithTag<TextView>("perm_$titulo")?.let {
                colorirPermissao(it, titulo, ok, snapshot.destacarPermissoes && obrigatoria && !ok)
            }
        }
        pintar("Notificações", PermissoesMonitoramento.listenerNotificacoesAtivo(context), true)
        pintar("Sobrepor", PermissoesMonitoramento.overlayConcedida(context), true)
        pintar("Acessib.", PermissoesMonitoramento.acessibilidadeAtiva(context), true)
        pintar("Bateria", PermissoesMonitoramento.bateriaLiberada(context), true)
        pintar("Localização", PermissoesMonitoramento.localizacaoConcedida(context), false)
    }

    private fun linhaPermissao(
        context: Context,
        titulo: String,
        ok: Boolean,
        destacar: Boolean,
        onClick: () -> Unit,
    ): TextView {
        return TextView(context).apply {
            tag = "perm_$titulo"
            textSize = 12f
            setPadding(0, dp(context, 6), 0, dp(context, 6))
            colorirPermissao(this, titulo, ok, destacar)
            setOnClickListener { onClick() }
        }
    }

    private fun colorirPermissao(view: TextView, titulo: String, ok: Boolean, destacar: Boolean) {
        view.text = if (ok) "$titulo  🆗" else "$titulo  ❎"
        view.setTextColor(
            Color.parseColor(
                when {
                    ok -> VERDE
                    destacar -> "#FFCDD2"
                    else -> AMARELO
                },
            ),
        )
    }

    private fun aplicarAlturaMinimaAba(conteudo: LinearLayout) {
        conteudo.post {
            val medida = conteudo.height
            if (medida > alturaMinimaConteudo) {
                alturaMinimaConteudo = medida
            }
            if (alturaMinimaConteudo > 0) {
                conteudo.minimumHeight = alturaMinimaConteudo
            }
        }
    }

    private fun caixaCampo(context: Context): GradientDrawable =
        GradientDrawable().apply {
            setColor(Color.parseColor("#33000000"))
            setStroke(dp(context, 1), Color.parseColor("#3D4A57"))
            cornerRadius = dp(context, 6).toFloat()
        }

    private fun campo(
        context: Context,
        label: String,
        valor: String,
        tag: String,
        bloqueado: Boolean = false,
        compacto: Boolean = false,
        pro: Boolean = false,
    ): Pair<LinearLayout, EditText> {
        val bloco = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(context, if (compacto) 2 else 4), 0, dp(context, if (compacto) 2 else 4))
        }
        if (pro) {
            bloco.addView(rotuloPro(context, label, compacto = compacto))
        } else {
            bloco.addView(
                rotulo(context, label, compacto = compacto).apply {
                    minHeight = dp(context, 16)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                },
            )
        }
        val campo = EditText(context).apply {
            this.tag = tag
            setText(valor)
            setTextColor(Color.WHITE)
            textSize = 13f
            isEnabled = !bloqueado
            isCursorVisible = false
            minHeight = dp(context, 32)
            showSoftInputOnFocus = true
            setOnFocusChangeListener { v, temFoco ->
                (v as EditText).isCursorVisible = temFoco
            }
            setHintTextColor(Color.parseColor(SECUNDARIO))
            background = caixaCampo(context)
            setPadding(dp(context, 8), dp(context, 5), dp(context, 8), dp(context, 5))
        }
        bloco.addView(campo)
        return bloco to campo
    }

    private fun rotuloFaixa(context: Context, texto: String, corHex: String): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(context, 4), 0, dp(context, 2))
            addView(
                TextView(context).apply {
                    text = texto
                    setTextColor(Color.WHITE)
                    textSize = 12f
                    typeface = Typeface.DEFAULT_BOLD
                },
            )
            addView(
                View(context).apply {
                    val tamanho = dp(context, 8)
                    layoutParams = LinearLayout.LayoutParams(tamanho, tamanho).apply {
                        marginStart = dp(context, 8)
                    }
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(Color.parseColor(corHex))
                    }
                },
            )
        }

    private fun rotulo(
        context: Context,
        texto: String,
        compacto: Boolean = false,
        secao: Boolean = false,
    ): TextView =
        TextView(context).apply {
            text = texto
            setTextColor(Color.parseColor(if (secao) SECUNDARIO else SECUNDARIO))
            textSize = 12f
            typeface = if (secao) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            val padTopo = if (secao) 4 else if (compacto) 2 else 4
            val padBase = if (secao) 2 else if (compacto) 1 else 2
            setPadding(0, dp(context, padTopo), 0, dp(context, padBase))
        }

    private fun rotuloPro(context: Context, texto: String, compacto: Boolean = false): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val padTopo = if (compacto) 2 else 4
            val padBase = if (compacto) 1 else 2
            setPadding(0, dp(context, padTopo), 0, dp(context, padBase))
            minimumHeight = dp(context, 16)
            addView(
                TextView(context).apply {
                    text = "🔒 $texto"
                    setTextColor(Color.parseColor(SECUNDARIO))
                    textSize = 11.5f
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                },
            )
            addView(
                TextView(context).apply {
                    text = "versão pro"
                    setTextColor(Color.parseColor(AMARELO))
                    textSize = 9f
                    maxLines = 1
                },
            )
        }

    private fun stepper(
        context: Context,
        label: String,
        valor: String,
        tag: String,
        editavel: Boolean,
        onPasso: ((String, Double) -> Unit)? = null,
    ): LinearLayout {
        val bloco = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(context, 4), 0, dp(context, 6))
        }
        bloco.addView(rotulo(context, label, compacto = true))
        val linha = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = caixaCampo(context)
            setPadding(dp(context, 6), dp(context, 4), dp(context, 6), dp(context, 4))
        }
        val valorView = TextView(context).apply {
            this.tag = tag
            text = valor
            setTextColor(Color.WHITE)
            textSize = 13f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        fun botao(texto: String): TextView = TextView(context).apply {
            this.text = texto
            setTextColor(Color.parseColor(if (editavel) AMARELO else SECUNDARIO))
            textSize = 16f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            minWidth = dp(context, 32)
            minHeight = dp(context, 32)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#22000000"))
                setStroke(dp(context, 1), Color.parseColor(if (editavel) AMARELO else "#3D4A57"))
                cornerRadius = dp(context, 6).toFloat()
            }
            setPadding(dp(context, 8), dp(context, 4), dp(context, 8), dp(context, 4))
            if (editavel) {
                isClickable = true
                setOnClickListener {
                    val delta = if (texto == "+") FaixasClassificacao.PASSO else -FaixasClassificacao.PASSO
                    if (onPasso != null) {
                        onPasso(tag, delta)
                    } else {
                        val atual = DecimalInput.parse(valorView.text.toString()) ?: return@setOnClickListener
                        val novo = (atual + delta).coerceIn(
                            FaixasClassificacao.MIN_ABSOLUTO,
                            FaixasClassificacao.MAX_ABSOLUTO,
                        )
                        valorView.text = FaixasClassificacao.formatar(novo)
                    }
                }
            }
        }
        linha.addView(botao("−"))
        linha.addView(valorView)
        linha.addView(botao("+"))
        bloco.addView(linha)
        return bloco
    }

    private fun linha(context: Context, vararg filhos: View): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.BOTTOM
            setPadding(0, dp(context, 2), 0, dp(context, 2))
            filhos.forEach { filho ->
                filho.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    gravity = Gravity.BOTTOM
                    marginEnd = dp(context, 4)
                }
                addView(filho)
            }
        }

    private fun tituloComSetas(
        context: Context,
        titulo: String,
        onEsquerda: () -> Unit,
        onDireita: () -> Unit,
    ): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(context, 6))
            addView(
                TextView(context).apply {
                    text = "⬅️"
                    textSize = 14f
                    gravity = Gravity.CENTER
                    setPadding(dp(context, 4), dp(context, 2), dp(context, 4), dp(context, 2))
                    background = null
                    setOnClickListener { onEsquerda() }
                },
            )
            addView(
                TextView(context).apply {
                    text = titulo
                    setTextColor(Color.WHITE)
                    textSize = 14f
                    gravity = Gravity.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                },
            )
            addView(
                TextView(context).apply {
                    text = "➡️"
                    textSize = 14f
                    gravity = Gravity.CENTER
                    setPadding(dp(context, 4), dp(context, 2), dp(context, 4), dp(context, 2))
                    background = null
                    setOnClickListener { onDireita() }
                },
            )
        }

    private fun escutarFlingAbas(
        view: View,
        onProxima: () -> Unit,
        onAnterior: () -> Unit,
    ) {
        val detector = GestureDetector(
            view.context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean = true

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocidadeX: Float,
                    velocidadeY: Float,
                ): Boolean {
                    if (abs(velocidadeX) < 700f || abs(velocidadeX) < abs(velocidadeY) * 1.1f) {
                        return false
                    }
                    if (velocidadeX < 0) onProxima() else onAnterior()
                    return true
                }
            },
        )
        view.setOnTouchListener { _, evento ->
            detector.onTouchEvent(evento)
            false
        }
    }

    fun aplicarBordaNeutra(view: View) {
        view.background = fundoNeutro(view.context)
    }

    private fun ScrollView.barraJuntoDaBorda(paddingInicioDp: Int) {
        isVerticalScrollBarEnabled = true
        isHorizontalScrollBarEnabled = false
        isScrollbarFadingEnabled = true
        scrollBarStyle = View.SCROLLBARS_INSIDE_INSET
        clipToPadding = true
        setPadding(dp(context, paddingInicioDp), 0, dp(context, 12), 0)
        val polegar = GradientDrawable().apply {
            setColor(Color.parseColor("#B8C5D1"))
            cornerRadius = dp(context, 2).toFloat()
            setSize(dp(context, 4), dp(context, 28))
        }
        setVerticalScrollbarThumbDrawable(polegar)
        setVerticalScrollbarTrackDrawable(null)
    }

    private fun fundoNeutro(context: Context): GradientDrawable =
        GradientDrawable().apply {
            setColor(Color.parseColor(FUNDO))
            setStroke(dp(context, 2), Color.parseColor(ClassificacaoConstantes.COR_BORDA_NEUTRA))
            cornerRadius = dp(context, 10).toFloat()
        }

    private fun dp(context: Context, valor: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            valor.toFloat(),
            context.resources.displayMetrics,
        ).toInt()
}
