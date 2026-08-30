package br.com.gestordriver.overlay

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.provider.Settings
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
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import br.com.gestordriver.GestorDriverApp
import br.com.gestordriver.MainActivity
import br.com.gestordriver.core.ClassificacaoConstantes
import br.com.gestordriver.model.AppNavegacao
import br.com.gestordriver.model.Combustivel
import br.com.gestordriver.model.ConfiguracaoUsuario
import br.com.gestordriver.navigation.NavegacaoLauncher
import br.com.gestordriver.notification.Plataforma
import br.com.gestordriver.notification.PlataformasMotorista
import br.com.gestordriver.permission.PermissoesMonitoramento
import br.com.gestordriver.ui.DecimalInput
import kotlin.math.abs

object OverlayPaineis {
    private var rascunho: ConfiguracaoUsuario? = null
    private var abaMontada: Int = -1
    private const val FUNDO = "#F2050809"
    private const val TEXTO = "#FFFFFF"
    private const val SECUNDARIO = "#B8C5D1"
    private const val AMARELO = "#FFD54F"
    private const val VERDE = "#7CB342"

    fun criarHistorico(context: Context): ScrollView {
        val scroll = ScrollView(context)
        scroll.background = fundoNeutro(context)
        scroll.clipToOutline = true
        val coluna = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(context, 10), dp(context, 8), dp(context, 10), dp(context, 8))
            tag = "historico_coluna"
        }
        coluna.addView(
            TextView(context).apply {
                text = "HISTÓRICO"
                setTextColor(Color.WHITE)
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(context, 6))
            },
        )
        val abas = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            tag = "historico_abas"
            gravity = Gravity.CENTER
        }
        listOf("Uber", "99", "inDrive").forEach { aba ->
            abas.addView(
                TextView(context).apply {
                    text = aba
                    tag = "aba_$aba"
                    setTextColor(Color.parseColor(SECUNDARIO))
                    textSize = 12f
                    gravity = Gravity.CENTER
                    setPadding(dp(context, 10), dp(context, 4), dp(context, 10), dp(context, 4))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    setOnClickListener { OverlayBridge.emitir(OverlayAcao.AbaHistorico(aba)) }
                },
            )
        }
        coluna.addView(abas)
        coluna.addView(
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                tag = "historico_lista"
            },
        )
        scroll.addView(coluna)
        escutarFlingAbas(
            scroll,
            onProxima = { avancarAbaHistorico(1) },
            onAnterior = { avancarAbaHistorico(-1) },
        )
        return scroll
    }

    fun criarConfirmacaoFechar(context: Context): LinearLayout {
        val coluna = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = fundoNeutro(context)
            setPadding(dp(context, 12), dp(context, 10), dp(context, 12), dp(context, 10))
        }
        coluna.addView(
            TextView(context).apply {
                text = "FECHAR GESTOR DRIVER"
                setTextColor(Color.WHITE)
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(context, 8))
            },
        )
        coluna.addView(
            TextView(context).apply {
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
                text = "Cancelar"
                setTextColor(Color.parseColor(SECUNDARIO))
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(dp(context, 16), dp(context, 8), dp(context, 16), dp(context, 8))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener { OverlayBridge.emitir(OverlayAcao.CancelarFechar) }
            },
        )
        acoes.addView(
            TextView(context).apply {
                text = "Fechar"
                setTextColor(Color.parseColor(AMARELO))
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(dp(context, 16), dp(context, 8), dp(context, 16), dp(context, 8))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener { OverlayBridge.emitir(OverlayAcao.ConfirmarFechar) }
            },
        )
        coluna.addView(acoes)
        return coluna
    }

    fun atualizarHistorico(view: View, snapshot: OverlaySnapshot) {
        val coluna = (view as ScrollView).getChildAt(0) as LinearLayout
        val abas = coluna.findViewWithTag<LinearLayout>("historico_abas")
        listOf("Uber", "99", "inDrive").forEach { aba ->
            val rotulo = abas.findViewWithTag<TextView>("aba_$aba")
            val selecionada = snapshot.historicoAba.equals(aba, ignoreCase = true)
            rotulo.setTextColor(Color.parseColor(if (selecionada) VERDE else SECUNDARIO))
            rotulo.typeface = if (selecionada) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        val lista = coluna.findViewWithTag<LinearLayout>("historico_lista")
        lista.removeAllViews()
        if (snapshot.historicoItens.isEmpty()) {
            lista.addView(
                TextView(view.context).apply {
                    text = "Nenhuma corrida aceita."
                    setTextColor(Color.parseColor(SECUNDARIO))
                    textSize = 11f
                    setPadding(0, dp(view.context, 8), 0, 0)
                },
            )
            return
        }
        snapshot.historicoItens.forEach { item ->
            lista.addView(criarItemHistorico(view.context, item))
        }
    }

    fun criarConfig(context: Context): LinearLayout {
        val ctx = ContextThemeWrapper(context, android.R.style.Theme_DeviceDefault)
        val raiz = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background = fundoNeutro(ctx)
            clipToOutline = true
            descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
            isFocusable = true
            isFocusableInTouchMode = true
            setPadding(dp(ctx, 10), dp(ctx, 8), dp(ctx, 10), dp(ctx, 8))
            tag = "config_coluna"
        }
        raiz.addView(
            TextView(ctx).apply {
                text = "CONFIGURAÇÃO"
                setTextColor(Color.WHITE)
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(ctx, 6))
            },
        )
        val abas = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            tag = "config_abas"
            gravity = Gravity.CENTER
        }
        listOf("VEÍCULO", "CUSTOS", "APP").forEachIndexed { indice, titulo ->
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
                    setPadding(dp(ctx, 6), dp(ctx, 4), dp(ctx, 6), dp(ctx, 4))
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
            setPadding(0, dp(ctx, 6), 0, 0)
        }
        rodape.addView(
            TextView(ctx).apply {
                text = "CANCELAR"
                setTextColor(Color.parseColor(SECUNDARIO))
                textSize = 13f
                gravity = Gravity.CENTER
                isClickable = true
                setPadding(dp(ctx, 8), dp(ctx, 8), dp(ctx, 8), dp(ctx, 8))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener { cancelarConfig() }
            },
        )
        rodape.addView(
            TextView(ctx).apply {
                text = "SALVAR"
                setTextColor(Color.parseColor(VERDE))
                textSize = 13f
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
        return raiz
    }

    fun atualizarConfig(view: View, snapshot: OverlaySnapshot) {
        if (!snapshot.configuracoesVisivel) {
            rascunho = null
            abaMontada = -1
            return
        }
        atualizarConfigInterno(view, snapshot)
    }

    private fun selecionarAbaConfig(indice: Int) {
        OverlayBridge.emitir(OverlayAcao.AbaConfiguracao(indice))
        val atual = OverlayBridge.snapshot.value
        OverlayBridge.publicar(atual.copy(abaConfiguracao = indice, configuracoesVisivel = true))
    }

    private fun avancarAbaConfig(direcao: Int) {
        val atual = OverlayBridge.snapshot.value.abaConfiguracao
        val novo = (atual + direcao).coerceIn(0, 2)
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

    private fun cancelarConfig() {
        rascunho = null
        OverlayBridge.emitir(OverlayAcao.CancelarConfig)
    }

    private fun salvarConfig(raiz: View, context: Context) {
        val app = context.applicationContext as? GestorDriverApp ?: return
        val aba = OverlayBridge.snapshot.value.abaConfiguracao
        val base = rascunho ?: app.configuracaoStore.carregar()
        val final = colherAba(raiz, aba, base)
        runCatching { app.configuracaoStore.salvar(final) }
        rascunho = null
        OverlayBridge.emitir(OverlayAcao.SalvarConfig)
    }

    private fun atualizarConfigInterno(view: View, snapshot: OverlaySnapshot) {
        val abas = view.findViewWithTag<LinearLayout>("config_abas") ?: return
        repeat(3) { indice ->
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
            if (snapshot.abaConfiguracao == 2) {
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
                else -> montarApp(conteudo, config, snapshot, view.context)
            }
        }
        if (montou.isSuccess) {
            abaMontada = snapshot.abaConfiguracao
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

    private fun criarItemHistorico(context: Context, item: OverlayHistoricoItem): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.TRANSPARENT)
                setStroke(dp(context, 1), Color.parseColor(ClassificacaoConstantes.COR_BORDA_NEUTRA))
                cornerRadius = dp(context, 8).toFloat()
            }
            setPadding(dp(context, 6), dp(context, 5), dp(context, 6), dp(context, 5))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            params.topMargin = dp(context, 5)
            layoutParams = params
            addView(
                TextView(context).apply {
                    text = "DATA | HORA | R$/KM | VALOR | KM | TEMPO | NOTA | ⭐"
                    setTextColor(Color.parseColor(SECUNDARIO))
                    textSize = 8f
                    maxLines = 1
                },
            )
            addView(
                LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    addView(
                        TextView(context).apply {
                            text = "${item.data} │ ${item.hora} │ ${item.valorPorKm} │ ${item.valor} │ ${item.km} │ ${item.tempo} │ ${item.nota}"
                            setTextColor(Color.WHITE)
                            textSize = 9f
                            maxLines = 2
                            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        },
                    )
                    addView(
                        TextView(context).apply {
                            text = item.marcador
                            setTextColor(Color.parseColor(item.corMarcador))
                            textSize = 12f
                        },
                    )
                },
            )
            setOnClickListener { OverlayBridge.emitir(OverlayAcao.SelecionarHistorico(item.chave)) }
        }
    }

    private fun montarVeiculo(
        destino: LinearLayout,
        config: ConfiguracaoUsuario,
    ) {
        val ctx = destino.context
        destino.addView(rotulo(ctx, "DESCRIÇÃO"))
        destino.addView(
            linha(
                ctx,
                campo(ctx, "MARCA", config.marcaVeiculo, "cfg_marca").first,
                campo(ctx, "MODELO", config.modeloVeiculo, "cfg_modelo").first,
            ),
        )
        destino.addView(
            linha(
                ctx,
                campo(ctx, "VERSÃO", config.versaoVeiculo, "cfg_versao").first,
                campo(ctx, "ANO", config.anoVeiculo, "cfg_ano").first,
            ),
        )
        destino.addView(rotulo(ctx, "CONSUMO KM"))
        destino.addView(
            linha(
                ctx,
                campo(ctx, "GASOLINA", DecimalInput.formatar(config.consumoGasolina), "cfg_consumo_g").first,
                campo(ctx, "ETANOL", DecimalInput.formatar(config.consumoEtanol), "cfg_consumo_e").first,
            ),
        )
        destino.addView(rotulo(ctx, "COMBUSTÍVEL ATUAL"))
        val ckG = CheckBox(ctx).apply {
            text = "GASOLINA"
            tag = "cfg_ck_gasolina"
            setTextColor(Color.WHITE)
            textSize = 11f
            scaleX = 0.82f
            scaleY = 0.82f
            isFocusableInTouchMode = false
            isChecked = config.combustivel == Combustivel.GASOLINA
        }
        val ckE = CheckBox(ctx).apply {
            text = "ETANOL"
            tag = "cfg_ck_etanol"
            setTextColor(Color.WHITE)
            textSize = 11f
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
    }

    private fun montarCustos(
        destino: LinearLayout,
        config: ConfiguracaoUsuario,
    ) {
        val ctx = destino.context
        destino.addView(rotulo(ctx, "VALOR DO COMBUSTÍVEL"))
        destino.addView(
            linha(
                ctx,
                campo(ctx, "LITRO GASOLINA", DecimalInput.formatar(config.precoGasolina), "cfg_preco_g").first,
                campo(ctx, "LITRO ETANOL", DecimalInput.formatar(config.precoEtanol), "cfg_preco_e").first,
            ),
        )
        destino.addView(rotulo(ctx, "🔒 TROCA DE ÓLEO (ÓLEO E FILTROS)", compacto = true))
        destino.addView(
            linha(
                ctx,
                campo(ctx, "VALOR", DecimalInput.formatar(config.oleoValor), "cfg_oleo_valor", bloqueado = true, compacto = true).first,
                campo(ctx, "KILOMETRAGEM", DecimalInput.formatar(config.oleoKilometragem), "cfg_oleo_km", bloqueado = true, compacto = true).first,
            ),
        )
        destino.addView(rotulo(ctx, "🔒 CUSTO ESTIMADO DOS PNEUS", compacto = true))
        destino.addView(rotulo(ctx, "DIANTEIRO", compacto = true))
        destino.addView(
            linha(
                ctx,
                campo(ctx, "VALOR", DecimalInput.formatar(config.pneuDianteiroValor), "cfg_pneu_d_valor", bloqueado = true, compacto = true).first,
                campo(ctx, "RODAGEM", DecimalInput.formatar(config.pneuDianteiroRodagem), "cfg_pneu_d_km", bloqueado = true, compacto = true).first,
            ),
        )
        destino.addView(rotulo(ctx, "TRASEIRO", compacto = true))
        destino.addView(
            linha(
                ctx,
                campo(ctx, "VALOR", DecimalInput.formatar(config.pneuTraseiroValor), "cfg_pneu_t_valor", bloqueado = true, compacto = true).first,
                campo(ctx, "RODAGEM", DecimalInput.formatar(config.pneuTraseiroRodagem), "cfg_pneu_t_km", bloqueado = true, compacto = true).first,
            ),
        )
    }

    private fun montarApp(
        destino: LinearLayout,
        config: ConfiguracaoUsuario,
        snapshot: OverlaySnapshot,
        context: Context,
    ) {
        destino.addView(rotulo(context, "PERMISSÕES"))
        val perms = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        perms.addView(
            linhaPermissao(context, "LOCALIZAÇÃO", PermissoesMonitoramento.localizacaoConcedida(context), snapshot.destacarPermissoes) {
                context.startActivity(
                    Intent(context, MainActivity::class.java)
                        .putExtra(MainActivity.EXTRA_PEDIR_LOCALIZACAO, true)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                )
            }.apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            },
        )
        perms.addView(
            linhaPermissao(context, "NOTIFICAÇÕES", PermissoesMonitoramento.listenerNotificacoesAtivo(context), snapshot.destacarPermissoes) {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }.apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            },
        )
        perms.addView(
            linhaPermissao(context, "SOBREPOR", PermissoesMonitoramento.overlayConcedida(context), snapshot.destacarPermissoes) {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        android.net.Uri.parse("package:${context.packageName}"),
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }.apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            },
        )
        destino.addView(perms)
        destino.addView(rotulo(context, "APPS DE CORRIDA"))
        val apps = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        listOf(
            Plataforma.UBER to "UBER",
            Plataforma.NOVE_NOVE to "99",
            Plataforma.INDRIVE to "INDRIVE",
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
        destino.addView(rotulo(context, "NAVEGAÇÃO"))
        val maps = CheckBox(context).apply {
            text = "GOOGLE MAPS"
            tag = "cfg_ck_maps"
            setTextColor(Color.WHITE)
            isFocusableInTouchMode = false
            isChecked = config.navegacao == AppNavegacao.GOOGLE_MAPS
        }
        val waze = CheckBox(context).apply {
            text = "WAZE"
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
        destino.addView(
            TextView(context).apply {
                text = "Nesta versão abre o app. No Pro o trajeto da corrida aceita poderá ser visto no histórico."
                setTextColor(Color.parseColor(SECUNDARIO))
                textSize = 9f
                setPadding(0, dp(context, 4), 0, dp(context, 6))
            },
        )
        destino.addView(rotulo(context, "CLASSIFICAÇÃO"))
        montarFaixas(destino, config)
    }

    private fun montarFaixas(destino: LinearLayout, config: ConfiguracaoUsuario) {
        val ctx = destino.context
        data class Faixa(
            val titulo: String,
            val minTexto: String,
            val maxTexto: String,
            val tagMin: String?,
            val tagMax: String?,
        )
        val faixas = listOf(
            Faixa("RUIM", "MIN", br.com.gestordriver.core.FaixasClassificacao.formatar(config.limiteRuimMax), null, "cfg_ruim_max"),
            Faixa(
                "REGULAR",
                br.com.gestordriver.core.FaixasClassificacao.formatar(config.limiteRegularMin),
                br.com.gestordriver.core.FaixasClassificacao.formatar(config.limiteRegularMax),
                "cfg_reg_min",
                "cfg_reg_max",
            ),
            Faixa(
                "BOA",
                br.com.gestordriver.core.FaixasClassificacao.formatar(config.limiteBoaMin),
                br.com.gestordriver.core.FaixasClassificacao.formatar(config.limiteBoaMax),
                "cfg_boa_min",
                "cfg_boa_max",
            ),
            Faixa(
                "ÓTIMA",
                br.com.gestordriver.core.FaixasClassificacao.formatar(config.limiteOtimaMin),
                "MAX",
                "cfg_otima_min",
                null,
            ),
        )
        faixas.forEach { faixa ->
            destino.addView(rotulo(ctx, faixa.titulo))
            destino.addView(
                linha(
                    ctx,
                    campo(ctx, "MIN", faixa.minTexto, faixa.tagMin ?: "cfg_${faixa.titulo}_min_fixo", bloqueado = faixa.tagMin == null).first,
                    campo(ctx, "MAX", faixa.maxTexto, faixa.tagMax ?: "cfg_${faixa.titulo}_max_fixo", bloqueado = faixa.tagMax == null).first,
                ),
            )
        }
    }

    private fun colherAba(raiz: View, aba: Int, base: ConfiguracaoUsuario): ConfiguracaoUsuario {
        fun txt(tag: String): String? = raiz.findViewWithTag<EditText>(tag)?.text?.toString()
        fun num(tag: String, atual: Double): Double = DecimalInput.parse(txt(tag) ?: "") ?: atual
        return when (aba) {
            0 -> {
                val gasolinaMarcada = raiz.findViewWithTag<CheckBox>("cfg_ck_gasolina")?.isChecked
                base.copy(
                    marcaVeiculo = txt("cfg_marca") ?: base.marcaVeiculo,
                    modeloVeiculo = txt("cfg_modelo") ?: base.modeloVeiculo,
                    versaoVeiculo = txt("cfg_versao") ?: base.versaoVeiculo,
                    anoVeiculo = txt("cfg_ano") ?: base.anoVeiculo,
                    consumoGasolina = num("cfg_consumo_g", base.consumoGasolina),
                    consumoEtanol = num("cfg_consumo_e", base.consumoEtanol),
                    combustivel = when (gasolinaMarcada) {
                        true -> Combustivel.GASOLINA
                        false -> Combustivel.ETANOL
                        null -> base.combustivel
                    },
                )
            }
            1 -> base.copy(
                precoGasolina = num("cfg_preco_g", base.precoGasolina),
                precoEtanol = num("cfg_preco_e", base.precoEtanol),
            )
            else -> {
                val maps = raiz.findViewWithTag<CheckBox>("cfg_ck_maps")?.isChecked
                var atual = base.copy(
                    navegacao = when (maps) {
                        true -> AppNavegacao.GOOGLE_MAPS
                        false -> AppNavegacao.WAZE
                        null -> base.navegacao
                    },
                )
                txt("cfg_ruim_max")?.let {
                    DecimalInput.parse(it)?.let { n ->
                        atual = br.com.gestordriver.core.FaixasClassificacao.aplicar(
                            atual,
                            br.com.gestordriver.core.FaixasClassificacao.Campo.RUIM_MAX,
                            n,
                        )
                    }
                }
                txt("cfg_reg_min")?.let {
                    DecimalInput.parse(it)?.let { n ->
                        atual = br.com.gestordriver.core.FaixasClassificacao.aplicar(
                            atual,
                            br.com.gestordriver.core.FaixasClassificacao.Campo.REGULAR_MIN,
                            n,
                        )
                    }
                }
                txt("cfg_reg_max")?.let {
                    DecimalInput.parse(it)?.let { n ->
                        atual = br.com.gestordriver.core.FaixasClassificacao.aplicar(
                            atual,
                            br.com.gestordriver.core.FaixasClassificacao.Campo.REGULAR_MAX,
                            n,
                        )
                    }
                }
                txt("cfg_boa_min")?.let {
                    DecimalInput.parse(it)?.let { n ->
                        atual = br.com.gestordriver.core.FaixasClassificacao.aplicar(
                            atual,
                            br.com.gestordriver.core.FaixasClassificacao.Campo.BOA_MIN,
                            n,
                        )
                    }
                }
                txt("cfg_boa_max")?.let {
                    DecimalInput.parse(it)?.let { n ->
                        atual = br.com.gestordriver.core.FaixasClassificacao.aplicar(
                            atual,
                            br.com.gestordriver.core.FaixasClassificacao.Campo.BOA_MAX,
                            n,
                        )
                    }
                }
                txt("cfg_otima_min")?.let {
                    DecimalInput.parse(it)?.let { n ->
                        atual = br.com.gestordriver.core.FaixasClassificacao.aplicar(
                            atual,
                            br.com.gestordriver.core.FaixasClassificacao.Campo.OTIMA_MIN,
                            n,
                        )
                    }
                }
                atual
            }
        }
    }

    private fun atualizarPermissoes(conteudo: LinearLayout, snapshot: OverlaySnapshot, context: Context) {
        val localizacao = PermissoesMonitoramento.localizacaoConcedida(context)
        val notificacoes = PermissoesMonitoramento.listenerNotificacoesAtivo(context)
        val overlay = PermissoesMonitoramento.overlayConcedida(context)
        conteudo.findViewWithTag<TextView>("perm_LOCALIZAÇÃO")?.let {
            colorirPermissao(it, "LOCALIZAÇÃO", localizacao, snapshot.destacarPermissoes)
        }
        conteudo.findViewWithTag<TextView>("perm_NOTIFICAÇÕES")?.let {
            colorirPermissao(it, "NOTIFICAÇÕES", notificacoes, snapshot.destacarPermissoes)
        }
        conteudo.findViewWithTag<TextView>("perm_SOBREPOR")?.let {
            colorirPermissao(it, "SOBREPOR", overlay, snapshot.destacarPermissoes)
        }
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

    private fun campo(
        context: Context,
        label: String,
        valor: String,
        tag: String,
        bloqueado: Boolean = false,
        compacto: Boolean = false,
    ): Pair<LinearLayout, EditText> {
        val bloco = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = if (compacto) 0 else 4
            setPadding(0, dp(context, pad), 0, dp(context, pad))
        }
        bloco.addView(
            rotulo(context, label, compacto = compacto).apply {
                minHeight = dp(context, 14)
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            },
        )
        val campo = EditText(context).apply {
            this.tag = tag
            setText(valor)
            setTextColor(Color.WHITE)
            textSize = 13f
            isEnabled = !bloqueado
            isCursorVisible = false
            minHeight = dp(context, if (compacto) 32 else 36)
            showSoftInputOnFocus = true
            setOnFocusChangeListener { v, temFoco ->
                (v as EditText).isCursorVisible = temFoco
            }
            setHintTextColor(Color.parseColor(SECUNDARIO))
            setBackgroundColor(Color.parseColor("#33000000"))
            val padV = if (compacto) 4 else 6
            setPadding(dp(context, 8), dp(context, padV), dp(context, 8), dp(context, padV))
        }
        bloco.addView(campo)
        return bloco to campo
    }

    private fun rotulo(context: Context, texto: String, compacto: Boolean = false): TextView =
        TextView(context).apply {
            text = texto
            setTextColor(Color.parseColor(SECUNDARIO))
            textSize = 10f
            val padTopo = if (compacto) 1 else 4
            val padBase = if (compacto) 0 else 2
            setPadding(0, dp(context, padTopo), 0, dp(context, padBase))
        }

    private fun linha(context: Context, esquerda: View, direita: View): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.TOP
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            params.gravity = Gravity.TOP
            esquerda.layoutParams = LinearLayout.LayoutParams(params)
            direita.layoutParams = LinearLayout.LayoutParams(params)
            addView(esquerda)
            addView(direita)
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
