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
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import br.com.gestordriver.GestorDriverApp
import br.com.gestordriver.core.CalendarioApp
import br.com.gestordriver.core.CalendarioPeriodo
import br.com.gestordriver.core.FaixasClassificacao
import br.com.gestordriver.MainActivity
import br.com.gestordriver.core.Classificacao
import br.com.gestordriver.core.ClassificacaoConstantes
import br.com.gestordriver.data.ContaVinculo
import br.com.gestordriver.model.AppNavegacao
import br.com.gestordriver.model.Combustivel
import br.com.gestordriver.model.ConfiguracaoUsuario
import br.com.gestordriver.model.SeguroRecorrencia
import br.com.gestordriver.model.TipoContaVinculada
import br.com.gestordriver.model.TipoVeiculo
import br.com.gestordriver.navigation.NavegacaoLauncher
import br.com.gestordriver.notification.Plataforma
import br.com.gestordriver.notification.PlataformasMotorista
import br.com.gestordriver.permission.PermissoesMonitoramento
import br.com.gestordriver.presentation.PresentationBuilder
import br.com.gestordriver.ui.DecimalInput
import kotlin.math.abs

object OverlayPaineis {
    private var rascunho: ConfiguracaoUsuario? = null
    private var abaMontada: Int = -1
    private var alturaMinimaConteudo: Int = 0
    private const val AMARELO = "#FFD54F"

    private enum class AbaMenuFicheiro(val titulo: String) {
        HISTORICO("Histórico"),
        DASHBOARD("Dashboard"),
        DESPESAS("Despesas"),
        SEMAFORO("Semáforo"),
        VEICULO("Veiculo"),
        CONFIGURAR("Configurar"),
    }

    private fun abaMenuAtiva(snapshot: OverlaySnapshot): AbaMenuFicheiro = when {
        snapshot.historicoVisivel -> AbaMenuFicheiro.HISTORICO
        snapshot.dashboardVisivel -> AbaMenuFicheiro.DASHBOARD
        snapshot.configuracoesVisivel -> when (snapshot.abaConfiguracao) {
            1 -> AbaMenuFicheiro.DESPESAS
            0 -> AbaMenuFicheiro.SEMAFORO
            2 -> AbaMenuFicheiro.VEICULO
            else -> AbaMenuFicheiro.CONFIGURAR
        }
        else -> AbaMenuFicheiro.HISTORICO
    }

    private fun emitirAbaMenu(aba: AbaMenuFicheiro) {
        when (aba) {
            AbaMenuFicheiro.HISTORICO -> OverlayBridge.emitir(OverlayAcao.AbrirHistorico)
            AbaMenuFicheiro.DASHBOARD -> OverlayBridge.emitir(OverlayAcao.DashboardPro)
            AbaMenuFicheiro.DESPESAS -> OverlayBridge.emitir(OverlayAcao.AbrirAtalhoConfig(1))
            AbaMenuFicheiro.SEMAFORO -> OverlayBridge.emitir(OverlayAcao.AbrirAtalhoConfig(0))
            AbaMenuFicheiro.VEICULO -> OverlayBridge.emitir(OverlayAcao.AbrirAtalhoConfig(2))
            AbaMenuFicheiro.CONFIGURAR -> OverlayBridge.emitir(OverlayAcao.AbrirAtalhoConfig(3))
        }
    }

    private fun criarFaixaAbasFicheiro(context: Context): LinearLayout {
        val coluna = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            tag = "menu_abas_ficheiro"
            setPadding(dp(context, 4), 0, dp(context, 4), dp(context, 6))
        }
        fun linha(vararg abas: AbaMenuFicheiro): LinearLayout =
            LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.BOTTOM
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
                abas.forEach { aba ->
                    addView(criarAbaFicheiro(context, aba))
                }
            }
        coluna.addView(linha(AbaMenuFicheiro.HISTORICO, AbaMenuFicheiro.DASHBOARD))
        coluna.addView(linha(
            AbaMenuFicheiro.DESPESAS,
            AbaMenuFicheiro.SEMAFORO,
            AbaMenuFicheiro.VEICULO,
            AbaMenuFicheiro.CONFIGURAR,
        ))
        return coluna
    }

    private fun criarAbaFicheiro(context: Context, aba: AbaMenuFicheiro): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            tag = "menu_aba_${aba.name}"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            isClickable = true
            isFocusable = true
            setOnClickListener { emitirAbaMenu(aba) }
            addView(
                TextView(context).apply {
                    tag = "menu_aba_titulo_${aba.name}"
                    text = aba.titulo
                    setTextColor(OverlayTema.de(context).secundario)
                    textSize = 12f
                    gravity = Gravity.CENTER
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    setPadding(dp(context, 2), dp(context, 8), dp(context, 2), dp(context, 6))
                },
            )
            addView(
                View(context).apply {
                    tag = "menu_aba_ind_${aba.name}"
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(context, 3),
                    )
                    setBackgroundColor(Color.TRANSPARENT)
                },
            )
        }
    }

    private fun atualizarFaixaAbasFicheiro(raiz: View, snapshot: OverlaySnapshot) {
        val ativa = abaMenuAtiva(snapshot)
        AbaMenuFicheiro.entries.forEach { aba ->
            val selecionada = aba == ativa
            raiz.findViewWithTag<TextView>("menu_aba_titulo_${aba.name}")?.let { titulo ->
                titulo.setTextColor(
                    if (selecionada) Color.parseColor(VERDE) else OverlayTema.de(raiz.context).secundario,
                )
                titulo.typeface = if (selecionada) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                titulo.elevation = if (selecionada) dp(raiz.context, 2).toFloat() else 0f
            }
            raiz.findViewWithTag<View>("menu_aba_ind_${aba.name}")?.setBackgroundColor(
                Color.parseColor(if (selecionada) VERDE else "#00000000"),
            )
            raiz.findViewWithTag<View>("menu_aba_${aba.name}")?.let { chip ->
                chip.elevation = if (selecionada) dp(raiz.context, 3).toFloat() else 0f
                chip.alpha = if (selecionada) 1f else 0.92f
            }
        }
    }
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
            cabecalhoConfiguracoes(context) {
                OverlayBridge.emitir(OverlayAcao.VoltarAtalho)
            }.also { cabeca ->
                cabeca.findViewWithTag<TextView>("config_titulo")?.text = "Histórico"
            },
        )
        coluna.addView(criarFaixaAbasFicheiro(context))
        val plataformas = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            tag = "historico_plataformas"
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(context, 4), 0, dp(context, 4), dp(context, 4))
        }
        listOf("Todos", "Uber", "99", "inDrive").forEach { nome ->
            plataformas.addView(
                TextView(context).apply {
                    tag = "historico_aba_$nome"
                    text = nome
                    textSize = 13f
                    gravity = Gravity.CENTER
                    minHeight = dp(context, 40)
                    setPadding(dp(context, 6), dp(context, 6), dp(context, 6), dp(context, 6))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    setOnClickListener { OverlayBridge.emitir(OverlayAcao.AbaHistorico(nome)) }
                },
            )
        }
        coluna.addView(plataformas)
        coluna.addView(
            tituloComSetas(
                context,
                CalendarioApp.rotuloMesAno(CalendarioApp.hoje()),
                onEsquerda = { OverlayBridge.emitir(OverlayAcao.HistoricoSemana(-1)) },
                onDireita = { OverlayBridge.emitir(OverlayAcao.HistoricoSemana(1)) },
                tituloTag = "historico_mes_titulo",
            ).apply {
                tag = "historico_mes"
                setPadding(dp(context, 8), 0, dp(context, 8), dp(context, 4))
            },
        )
        val cabecalhoSemana = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            tag = "historico_cabecalho_semana"
            gravity = Gravity.CENTER
            setPadding(dp(context, 4), 0, dp(context, 4), 0)
        }
        CalendarioApp.rotulosCabecalhoSemana().forEach { rotulo ->
            cabecalhoSemana.addView(
                TextView(context).apply {
                    text = rotulo
                    setTextColor(OverlayTema.de(context).secundario)
                    textSize = 11f
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                },
            )
        }
        coluna.addView(cabecalhoSemana)
        val grade = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            tag = "historico_grade"
            setPadding(dp(context, 4), 0, dp(context, 4), dp(context, 4))
        }
        coluna.addView(grade)
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
                text = "🗑️"
                setTextColor(Color.parseColor(AMARELO))
                textSize = 16f
                gravity = Gravity.CENTER
                setPadding(dp(context, 8), dp(context, 8), dp(context, 8), dp(context, 4))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
                setOnClickListener {
                    if (OverlayBridge.snapshot.value.historicoChavesSelecionadas.isEmpty()) {
                        Toast.makeText(context, "Selecionar a(s) corrida(s)", Toast.LENGTH_SHORT).show()
                    } else {
                        OverlayBridge.emitir(OverlayAcao.SolicitarLimparHistorico)
                    }
                }
            },
        )
        // Removido rodapé Cancelar/Salvar — histórico só tem lixeira.
        escutarFlingAbas(
            coluna,
            onProxima = { OverlayBridge.emitir(OverlayAcao.HistoricoSemana(1)) },
            onAnterior = { OverlayBridge.emitir(OverlayAcao.HistoricoSemana(-1)) },
        )
        escutarFlingAbas(
            scroll,
            onProxima = { OverlayBridge.emitir(OverlayAcao.HistoricoSemana(1)) },
            onAnterior = { OverlayBridge.emitir(OverlayAcao.HistoricoSemana(-1)) },
        )
        return coluna
    }

    fun criarDashboard(context: Context): LinearLayout {
        val coluna = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = fundoNeutro(context)
            clipToOutline = true
            setPadding(0, dp(context, 8), 0, dp(context, 8))
            tag = "dashboard_coluna"
        }
        coluna.addView(
            cabecalhoConfiguracoes(context) {
                OverlayBridge.emitir(OverlayAcao.VoltarAtalho)
            }.also { cabeca ->
                cabeca.findViewWithTag<TextView>("config_titulo")?.text = "Dashboard"
            },
        )
        coluna.addView(criarFaixaAbasFicheiro(context))
        val modos = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            tag = "dashboard_modos"
            gravity = Gravity.CENTER
            setPadding(dp(context, 4), 0, dp(context, 4), dp(context, 4))
        }
        listOf(
            CalendarioPeriodo.DIA to "Dia",
            CalendarioPeriodo.SEMANA to "Semana",
            CalendarioPeriodo.MES to "Mês",
            CalendarioPeriodo.ANO to "Ano",
        ).forEach { (periodo, rotulo) ->
            modos.addView(
                TextView(context).apply {
                    tag = "dashboard_modo_${periodo.name}"
                    text = rotulo
                    textSize = 13f
                    gravity = Gravity.CENTER
                    minHeight = dp(context, 40)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    setOnClickListener {
                        OverlayBridge.emitir(OverlayAcao.HistoricoModo(periodo.name))
                    }
                },
            )
        }
        coluna.addView(modos)
        coluna.addView(
            tituloComSetas(
                context,
                "—",
                onEsquerda = { OverlayBridge.emitir(OverlayAcao.HistoricoAvancar(-1)) },
                onDireita = { OverlayBridge.emitir(OverlayAcao.HistoricoAvancar(1)) },
                tituloTag = "dashboard_periodo_titulo",
            ).apply {
                tag = "dashboard_periodo"
                setPadding(dp(context, 8), 0, dp(context, 8), dp(context, 4))
            },
        )
        coluna.addView(
            TextView(context).apply {
                tag = "dashboard_periodo_sub"
                setTextColor(OverlayTema.de(context).secundario)
                textSize = 11f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(context, 4))
            },
        )
        val cabecalhoSemana = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            tag = "dashboard_cabecalho_semana"
            gravity = Gravity.CENTER
            setPadding(dp(context, 4), 0, dp(context, 4), 0)
        }
        CalendarioApp.rotulosCabecalhoSemana().forEach { rotulo ->
            cabecalhoSemana.addView(
                TextView(context).apply {
                    text = rotulo
                    setTextColor(OverlayTema.de(context).secundario)
                    textSize = 11f
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                },
            )
        }
        coluna.addView(cabecalhoSemana)
        coluna.addView(
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                tag = "dashboard_grade"
                setPadding(dp(context, 4), 0, dp(context, 4), dp(context, 4))
            },
        )
        val scroll = ScrollView(context).apply {
            tag = "dashboard_scroll"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f,
            )
            barraJuntoDaBorda(paddingInicioDp = 8)
        }
        val corpo = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            tag = "dashboard_corpo"
            setPadding(dp(context, 8), 0, dp(context, 8), dp(context, 12))
        }
        scroll.addView(corpo)
        coluna.addView(scroll)
        return coluna
    }

    fun atualizarDashboard(view: View, snapshot: OverlaySnapshot) {
        view.alpha = if (snapshot.monitorando) 1f else 0.9f
        val coluna = if (view.tag == "dashboard_coluna") {
            view as LinearLayout
        } else {
            view.findViewWithTag("dashboard_coluna") ?: return
        }
        coluna.background = fundoNeutro(coluna.context)
        val contexto = coluna.context
        atualizarFaixaAbasFicheiro(coluna, snapshot)
        val dia = CalendarioApp.diaDe(snapshot.historicoEpochDay)
        val periodo = CalendarioPeriodo.de(snapshot.historicoPeriodo)
        CalendarioPeriodo.entries.forEach { modo ->
            coluna.findViewWithTag<TextView>("dashboard_modo_${modo.name}")?.let { chip ->
                val ativo = modo == periodo
                chip.setTextColor(
                    if (ativo) Color.parseColor(VERDE) else OverlayTema.de(contexto).secundario,
                )
                chip.typeface = if (ativo) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                chip.background = GradientDrawable().apply {
                    cornerRadius = dp(contexto, 8).toFloat()
                    setColor(
                        if (ativo) Color.parseColor("#1A7CB342") else Color.TRANSPARENT,
                    )
                    if (ativo) {
                        setStroke(dp(contexto, 1), Color.parseColor(VERDE))
                    }
                }
            }
        }
        coluna.findViewWithTag<TextView>("dashboard_periodo_titulo")?.text =
            CalendarioApp.rotuloPeriodoCabecalho(dia, periodo)
        coluna.findViewWithTag<TextView>("dashboard_periodo_sub")?.let { sub ->
            val texto = CalendarioApp.subtituloPeriodo(dia, periodo)
            sub.text = texto
            sub.visibility = if (texto.isBlank()) View.GONE else View.VISIBLE
        }
        atualizarCalendarioDashboard(coluna, snapshot)
        val corpo = coluna.findViewWithTag<LinearLayout>("dashboard_corpo") ?: return
        corpo.removeAllViews()
        if (!snapshot.planoPro) {
            corpo.addView(
                TextView(contexto).apply {
                    text = "🔒 Faturamento, gastos e rateios ficam na versão Pro."
                    setTextColor(OverlayTema.de(contexto).secundario)
                    textSize = 13f
                    gravity = Gravity.CENTER
                    setPadding(dp(contexto, 8), dp(contexto, 16), dp(contexto, 8), dp(contexto, 8))
                },
            )
            return
        }
        val app = contexto.applicationContext as? GestorDriverApp
        val config = app?.configuracaoStore?.carregar()
            ?: br.com.gestordriver.model.ConfiguracaoUsuario.padrao()
        val itensPeriodo = (app?.historicoRepository?.listar().orEmpty()).filter { item ->
            val data = item.dataHoraRegistro?.toLocalDate() ?: return@filter false
            CalendarioApp.noPeriodo(data, dia, periodo)
        }
        val numeros = br.com.gestordriver.core.DashboardNumeros.de(
            itensPeriodo.map {
                br.com.gestordriver.core.CorridaParaResumo(
                    valorTotal = it.valorTotal,
                    kmTotal = it.kmTotal,
                    minutos = it.tempoEstimado ?: 0,
                    gastoCorrida = it.custoCombustivel,
                )
            },
            config,
            diasPeriodo = CalendarioApp.diasDoPeriodo(dia, periodo),
        )
        fun dinheiro(v: Double) = "R$ ${"%.2f".format(v).replace(".", ",")}"
        fun decimal(v: Double) = "%.2f".format(v).replace(".", ",")
        corpo.addView(linhaTresMetricasDash(contexto, listOf(
            Triple("Receitas", "", dinheiro(numeros.receitas) to Color.parseColor(VERDE)),
            Triple("Despesas", "", dinheiro(numeros.despesas) to Color.parseColor("#E53935")),
            Triple(
                "Saldo",
                "",
                dinheiro(numeros.saldo) to Color.parseColor(
                    if (numeros.saldo >= 0) VERDE else "#E53935",
                ),
            ),
        )))
        corpo.addView(rotulo(contexto, "Ganhos/Custos líquido", secao = true))
        corpo.addView(linhaDuasMetricasDash(contexto,
            "Ganhos Km" to ("R$ ${decimal(numeros.ganhoPorKm)}" to Color.parseColor(VERDE)),
            "Custo Km" to ("R$ ${decimal(numeros.custoPorKm)}" to Color.parseColor("#E53935")),
        ))
        corpo.addView(linhaDuasMetricasDash(contexto,
            "Ganhos hora" to ("R$ ${decimal(numeros.ganhoPorHora)}" to Color.parseColor(VERDE)),
            "Custo hora" to ("R$ ${decimal(numeros.custoPorHora)}" to Color.parseColor("#E53935")),
        ))
        corpo.addView(rotulo(contexto, "Estimativa de custos", secao = true))
        listOf(
            "Combustível" to numeros.combustivel,
            "óleo" to numeros.oleo,
            "Pneu dianteiros" to numeros.pneuDianteiro,
            "Pneu traseiros" to numeros.pneuTraseiro,
            "Seguro" to numeros.seguro,
            "IPVA" to numeros.ipva,
        ).forEach { (rotuloItem, valor) ->
            corpo.addView(
                linhaEstimativaDash(
                    contexto,
                    rotuloItem,
                    valor?.let { dinheiro(it) } ?: "—",
                    aviso = valor == null,
                ),
            )
        }
        if (numeros.corridas == 0) {
            corpo.addView(
                TextView(contexto).apply {
                    text = CalendarioApp.textoVazio(periodo)
                    setTextColor(OverlayTema.de(contexto).secundario)
                    textSize = 12f
                    gravity = Gravity.CENTER
                    setPadding(0, dp(contexto, 8), 0, 0)
                },
            )
        }
    }

    private fun atualizarCalendarioDashboard(coluna: LinearLayout, snapshot: OverlaySnapshot) {
        val selecionado = CalendarioApp.diaDe(snapshot.historicoEpochDay)
        val periodo = CalendarioPeriodo.de(snapshot.historicoPeriodo)
        val hoje = CalendarioApp.hoje()
        val marcados = snapshot.historicoDiasComCorrida.toSet()
        val cabecalho = coluna.findViewWithTag<LinearLayout>("dashboard_cabecalho_semana")
        val grade = coluna.findViewWithTag<LinearLayout>("dashboard_grade") ?: return
        grade.removeAllViews()
        when (periodo) {
            CalendarioPeriodo.ANO, CalendarioPeriodo.SEMANA -> {
                // Semana: só setas (faturamento da semana inteira). Ano: sem calendário.
                cabecalho?.visibility = View.GONE
                grade.visibility = View.GONE
            }
            CalendarioPeriodo.MES -> {
                cabecalho?.visibility = View.GONE
                grade.visibility = View.VISIBLE
                CalendarioApp.mesesDoAno(selecionado).chunked(6).forEach { linha ->
                    grade.addView(linhaMesesDashboard(coluna.context, linha, selecionado))
                }
            }
            CalendarioPeriodo.DIA -> {
                cabecalho?.visibility = View.VISIBLE
                grade.visibility = View.VISIBLE
                val dias = CalendarioApp.diasDaSemana(selecionado)
                grade.addView(linhaDiasDashboard(coluna.context, dias, selecionado, hoje, marcados, periodo))
            }
        }
    }

    private fun linhaMesesDashboard(
        context: Context,
        meses: List<java.time.LocalDate>,
        selecionado: java.time.LocalDate,
    ): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(context, 2), 0, dp(context, 2))
            meses.forEach { mes ->
                val ativo = CalendarioApp.noMes(mes, selecionado)
                addView(
                    TextView(context).apply {
                        text = CalendarioApp.rotuloMesChip(mes)
                        val tema = OverlayTema.de(context)
                        setTextColor(if (ativo) Color.parseColor(VERDE) else tema.texto)
                        typeface = if (ativo) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                        textSize = 12f
                        gravity = Gravity.CENTER
                        minHeight = dp(context, 36)
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        background = if (ativo) {
                            GradientDrawable().apply {
                                cornerRadius = dp(context, 8).toFloat()
                                setColor(Color.parseColor("#1A7CB342"))
                                setStroke(dp(context, 1), Color.parseColor(VERDE))
                            }
                        } else {
                            null
                        }
                        setOnClickListener {
                            OverlayBridge.emitir(OverlayAcao.HistoricoDia(mes.toEpochDay()))
                        }
                    },
                )
            }
        }

    private fun linhaDiasDashboard(
        context: Context,
        dias: List<java.time.LocalDate>,
        selecionado: java.time.LocalDate,
        hoje: java.time.LocalDate,
        marcados: Set<Long>,
        periodo: CalendarioPeriodo,
    ): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            dias.forEach { dia ->
                val ativo = when (periodo) {
                    CalendarioPeriodo.DIA -> dia == selecionado
                    CalendarioPeriodo.SEMANA -> CalendarioApp.noPeriodo(dia, selecionado, CalendarioPeriodo.SEMANA) &&
                        dia == selecionado
                    CalendarioPeriodo.MES -> dia == selecionado
                    CalendarioPeriodo.ANO -> false
                }
                val noMes = CalendarioApp.noMes(dia, selecionado)
                val temCorrida = marcados.contains(dia.toEpochDay())
                addView(
                    TextView(context).apply {
                        text = buildString {
                            append(dia.dayOfMonth)
                            if (temCorrida) append(" ·")
                        }
                        val tema = OverlayTema.de(context)
                        setTextColor(
                            when {
                                ativo -> Color.parseColor(VERDE)
                                dia == hoje -> Color.parseColor(AMARELO)
                                periodo == CalendarioPeriodo.MES && !noMes -> tema.secundario
                                else -> tema.texto
                            },
                        )
                        typeface = if (ativo) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                        textSize = 13f
                        gravity = Gravity.CENTER
                        minHeight = dp(context, 36)
                        alpha = if (periodo == CalendarioPeriodo.MES && !noMes) 0.45f else 1f
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        setOnClickListener {
                            OverlayBridge.emitir(OverlayAcao.HistoricoDia(dia.toEpochDay()))
                        }
                    },
                )
            }
        }

    private fun linhaTresMetricasDash(
        context: Context,
        itens: List<Triple<String, String, Pair<String, Int>>>,
    ): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(context, 4), 0, dp(context, 8))
            itens.forEach { (titulo, hint, valorCor) ->
                addView(
                    LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                            marginEnd = dp(context, 4)
                        }
                        background = GradientDrawable().apply {
                            setColor(OverlayTema.de(context).metrica)
                            setStroke(dp(context, 1), OverlayTema.de(context).borda)
                            cornerRadius = dp(context, 8).toFloat()
                        }
                        setPadding(dp(context, 4), dp(context, 8), dp(context, 4), dp(context, 8))
                        addView(TextView(context).apply {
                            text = titulo
                            setTextColor(OverlayTema.de(context).texto)
                            textSize = 11f
                            gravity = Gravity.CENTER
                            typeface = Typeface.DEFAULT_BOLD
                        })
                        addView(TextView(context).apply {
                            text = hint
                            setTextColor(OverlayTema.de(context).secundario)
                            textSize = 9f
                            gravity = Gravity.CENTER
                        })
                        addView(TextView(context).apply {
                            text = valorCor.first
                            setTextColor(valorCor.second)
                            textSize = 13f
                            gravity = Gravity.CENTER
                            typeface = Typeface.DEFAULT_BOLD
                        })
                    },
                )
            }
        }

    private fun linhaDuasMetricasDash(
        context: Context,
        esquerda: Pair<String, Pair<String, Int>>,
        direita: Pair<String, Pair<String, Int>>,
    ): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, dp(context, 6))
            listOf(esquerda, direita).forEach { (titulo, valorCor) ->
                addView(
                    LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                            marginEnd = dp(context, 4)
                        }
                        background = GradientDrawable().apply {
                            setColor(OverlayTema.de(context).fundoPainel)
                            setStroke(dp(context, 1), OverlayTema.de(context).borda)
                            cornerRadius = dp(context, 8).toFloat()
                        }
                        setPadding(dp(context, 8), dp(context, 8), dp(context, 8), dp(context, 8))
                        addView(TextView(context).apply {
                            text = titulo
                            setTextColor(OverlayTema.de(context).texto)
                            textSize = 11f
                            typeface = Typeface.DEFAULT_BOLD
                        })
                        addView(TextView(context).apply {
                            text = valorCor.first
                            setTextColor(valorCor.second)
                            textSize = 13f
                            typeface = Typeface.DEFAULT_BOLD
                        })
                    },
                )
            }
        }

    private fun linhaEstimativaDash(
        context: Context,
        rotulo: String,
        valor: String,
        aviso: Boolean,
    ): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(context, 10), dp(context, 8), dp(context, 10), dp(context, 8))
            background = GradientDrawable().apply {
                setColor(OverlayTema.de(context).fundoPainel)
                setStroke(dp(context, 1), OverlayTema.de(context).borda)
                cornerRadius = dp(context, 8).toFloat()
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = dp(context, 6) }
            addView(TextView(context).apply {
                text = rotulo
                setTextColor(
                    if (aviso) OverlayTema.de(context).secundario else OverlayTema.de(context).texto,
                )
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(TextView(context).apply {
                text = valor
                setTextColor(
                    if (aviso) OverlayTema.de(context).secundario else Color.parseColor(VERDE),
                )
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
            })
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
                text = "gestor driver"
                setTextColor(OverlayTema.de(context).texto)
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(context, 8))
            },
        )
        coluna.addView(
            TextView(context).apply {
                tag = "confirmacao_mensagem"
                text = "Deseja encerrar o aplicativo e parar o monitoramento de corridas?"
                setTextColor(OverlayTema.de(context).secundario)
                textSize = 13f
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
                setTextColor(OverlayTema.de(context).secundario)
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

    fun atualizarConfirmacao(view: View, limparHistorico: Boolean, quantidade: Int = 0) {
        view.findViewWithTag<TextView>("confirmacao_titulo")?.text = "gestor driver"
        view.findViewWithTag<TextView>("confirmacao_mensagem")?.text =
            if (limparHistorico) {
                "Limpar histórico"
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
        atualizarFaixaAbasFicheiro(coluna, snapshot)
        atualizarCalendarioHistorico(coluna, snapshot)
        listOf("Todos", "Uber", "99", "inDrive").forEach { nome ->
            coluna.findViewWithTag<TextView>("historico_aba_$nome")?.let { chip ->
                val ativa = snapshot.historicoAba.equals(nome, ignoreCase = true)
                val tema = OverlayTema.de(chip.context)
                chip.setTextColor(if (ativa) tema.texto else tema.secundario)
                chip.typeface = if (ativa) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            }
        }
        val lista = coluna.findViewWithTag<LinearLayout>("historico_lista") ?: return
        lista.removeAllViews()
        lista.setPadding(dp(view.context, 8), 0, dp(view.context, 8), 0)
        if (snapshot.historicoItens.isEmpty()) {
            lista.addView(
                TextView(view.context).apply {
                    text = CalendarioApp.textoVazio(CalendarioPeriodo.SEMANA)
                    setTextColor(OverlayTema.de(view.context).secundario)
                },
            )
            return
        }
        snapshot.historicoItens.forEach { item ->
            lista.addView(
                criarItemHistorico(
                    view.context,
                    item,
                    selecionado = item.chave in snapshot.historicoChavesSelecionadas,
                ),
            )
        }
    }

    private fun atualizarCalendarioHistorico(coluna: LinearLayout, snapshot: OverlaySnapshot) {
        val selecionado = CalendarioApp.diaDe(snapshot.historicoEpochDay)
        val hoje = CalendarioApp.hoje()
        val marcados = snapshot.historicoDiasComCorrida.toSet()
        coluna.findViewWithTag<TextView>("historico_mes_titulo")?.text =
            CalendarioApp.rotuloPeriodoCabecalho(selecionado, CalendarioPeriodo.SEMANA)
        val grade = coluna.findViewWithTag<LinearLayout>("historico_grade") ?: return
        grade.removeAllViews()
        val dias = CalendarioApp.diasDaSemana(selecionado)
        grade.addView(
            LinearLayout(coluna.context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                dias.forEach { dia ->
                    val ativo = dia == selecionado
                    val temCorrida = marcados.contains(dia.toEpochDay())
                    addView(
                        TextView(coluna.context).apply {
                            text = buildString {
                                append(dia.dayOfMonth)
                                if (temCorrida) append(" ·")
                            }
                            val temaDia = OverlayTema.de(coluna.context)
                            setTextColor(
                                when {
                                    ativo -> Color.parseColor(VERDE)
                                    dia == hoje -> Color.parseColor(AMARELO)
                                    else -> temaDia.texto
                                },
                            )
                            typeface = if (ativo) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                            textSize = 13f
                            gravity = Gravity.CENTER
                            minHeight = dp(coluna.context, 36)
                            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                            setOnClickListener {
                                OverlayBridge.emitir(OverlayAcao.HistoricoDia(dia.toEpochDay()))
                            }
                        },
                    )
                }
            },
        )
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
        raiz.addView(cabecalhoConfiguracoes(ctx) { cancelarConfig(raiz) })
        raiz.addView(criarFaixaAbasFicheiro(ctx))
        raiz.addView(
            View(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(ctx, 1),
                )
                setBackgroundColor(OverlayTema.de(ctx).borda)
            },
        )
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
        raiz.addView(
            View(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(ctx, 1),
                )
                setBackgroundColor(OverlayTema.de(ctx).borda)
            },
        )
        val rodape = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(ctx, 12), dp(ctx, 10), dp(ctx, 12), dp(ctx, 4))
        }
        rodape.addView(
            TextView(ctx).apply {
                text = "Cancelar"
                setTextColor(OverlayTema.de(ctx).secundario)
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                isClickable = true
                setPadding(dp(ctx, 8), dp(ctx, 10), dp(ctx, 8), dp(ctx, 10))
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#334CAF50"))
                    cornerRadius = dp(ctx, 8).toFloat()
                }
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener { cancelarConfig(raiz) }
            },
        )
        rodape.addView(
            TextView(ctx).apply {
                text = "SALVAR"
                setTextColor(Color.parseColor(VERDE))
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                isClickable = true
                setPadding(dp(ctx, 8), dp(ctx, 10), dp(ctx, 8), dp(ctx, 10))
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#334CAF50"))
                    cornerRadius = dp(ctx, 8).toFloat()
                }
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = dp(ctx, 8)
                }
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

    private fun avancarAbaPeriodoHistorico(direcao: Int) {
        val atual = CalendarioPeriodo.de(OverlayBridge.snapshot.value.historicoPeriodo)
        val novo = atual.vizinho(direcao)
        if (novo != atual) {
            OverlayBridge.emitir(OverlayAcao.HistoricoModo(novo.name))
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
        val colhida = colherAba(raiz, aba, base)
        val temAbastecimento =
            br.com.gestordriver.core.CalcularCombustivel.precoPorLitro(
                colhida.abastecimentoValor,
                colhida.abastecimentoLitros,
            ) != null ||
                br.com.gestordriver.core.CalcularCombustivel.consumoKmPorLitro(
                    colhida.abastecimentoKmInicial,
                    colhida.abastecimentoKmFinal,
                    colhida.abastecimentoLitros,
                ) != null
        fun persistir(aplicarAbastecimento: Boolean) {
            val final = FaixasClassificacao.normalizar(
                if (aplicarAbastecimento) colhida.aplicarCalculoAbastecimento() else colhida,
            )
            runCatching { app.configuracaoStore.salvar(final) }
            rascunho = null
            OverlayBridge.emitir(OverlayAcao.SalvarConfig)
        }
        if (!temAbastecimento) {
            persistir(false)
            return
        }
        android.app.AlertDialog.Builder(ContextThemeWrapper(context, android.R.style.Theme_DeviceDefault_Dialog))
            .setTitle("Usar abastecimento?")
            .setMessage("Preencher R$/L e km/L do combustível atual com o cálculo do abastecimento?")
            .setPositiveButton("Sim") { _, _ -> persistir(true) }
            .setNegativeButton("Não") { _, _ -> persistir(false) }
            .setNeutralButton("Cancelar", null)
            .show()
    }

    private fun adicionarAlertaOleo(
        destino: LinearLayout,
        context: Context,
        config: ConfiguracaoUsuario,
    ) {
        val app = context.applicationContext as? GestorDriverApp
        val pontos = app?.historicoRepository?.listar().orEmpty().map {
            it.dataHoraRegistro?.toLocalDate() to it.kmTotal
        }
        val kmDesde = br.com.gestordriver.core.AlertaOleo.kmDesdeTroca(config.oleoData, pontos)
        val nivel = br.com.gestordriver.core.AlertaOleo.nivel(config.oleoKilometragem, kmDesde)
        if (nivel == br.com.gestordriver.core.AlertaOleo.Nivel.OK || config.oleoKilometragem <= 0.0) {
            return
        }
        val restante = config.oleoKilometragem - kmDesde
        val texto = when (nivel) {
            br.com.gestordriver.core.AlertaOleo.Nivel.VENCIDO ->
                "Troca de óleo vencida. Já rodou ${"%.0f".format(kmDesde)} km desde a data informada."
            br.com.gestordriver.core.AlertaOleo.Nivel.AVISO ->
                "Troca de óleo perto do vencimento. Faltam cerca de ${"%.0f".format(restante.coerceAtLeast(0.0))} km."
            else -> return
        }
        destino.addView(
            TextView(context).apply {
                this.text = texto
                setTextColor(Color.parseColor("#E53935"))
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(0, dp(context, 2), 0, dp(context, 4))
            },
        )
    }

    private fun atualizarConfigInterno(view: View, snapshot: OverlaySnapshot) {
        atualizarFaixaAbasFicheiro(view, snapshot)
        view.findViewWithTag<TextView>("config_titulo")?.text = when (snapshot.abaConfiguracao) {
            1 -> "Despesas"
            0 -> "Calibrar a classificação"
            2 -> "Veiculo"
            else -> "Configurar"
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
                0 -> montarClassificacao(conteudo, config)
                1 -> montarCustos(conteudo, config, snapshot.planoPro)
                2 -> montarVeiculo(conteudo, config, snapshot.planoPro)
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

    private fun criarItemHistorico(
        context: Context,
        item: OverlayHistoricoItem,
        selecionado: Boolean,
    ): View {
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            isClickable = true
            setOnClickListener {
                OverlayBridge.emitir(OverlayAcao.SelecionarHistorico(item.chave))
            }
            background = GradientDrawable().apply {
                setColor(
                    if (selecionado) {
                        Color.parseColor("#CFD8DC")
                    } else {
                        OverlayTema.de(context).card
                    },
                )
                setStroke(dp(context, 2), Color.parseColor(item.corMarcador))
                cornerRadius = dp(context, 10).toFloat()
            }
            setPadding(dp(context, 10), dp(context, 10), dp(context, 10), dp(context, 10))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = dp(context, 8)
            }
        }
        val cabecalho = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        cabecalho.addView(
            TextView(context).apply {
                text = item.plataforma.ifBlank { "Uber" }
                setTextColor(OverlayTema.de(context).texto)
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
                maxLines = 1
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
            },
        )
        cabecalho.addView(
            TextView(context).apply {
                text = item.cabecalhoData.ifBlank { "${item.data}  ${item.hora}" }
                setTextColor(OverlayTema.de(context).secundario)
                textSize = 12f
                gravity = Gravity.END
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = dp(context, 12)
                }
            },
        )
        card.addView(cabecalho)
        val rotulos = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(context, 6), 0, 0)
        }
        listOf("Ganhos", "$/Km", "$/Lucro", "$/Gasto", "Nota").forEach { titulo ->
            rotulos.addView(
                TextView(context).apply {
                    text = titulo
                    setTextColor(OverlayTema.de(context).secundario)
                    textSize = 9f
                    gravity = Gravity.CENTER
                    maxLines = 1
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                },
            )
        }
        card.addView(rotulos)
        val metricas = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(context, 2), 0, 0)
        }
        metricas.addView(
            TextView(context).apply {
                text = item.valor
                setTextColor(OverlayTema.de(context).texto)
                textSize = 12.5f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.15f)
            },
        )
        listOf(item.valorPorKm, item.lucro, item.gasto, item.nota).forEach { valor ->
            metricas.addView(caixaMetricaHistorico(context, valor))
        }
        card.addView(metricas)
        card.addView(
            TextView(context).apply {
                text = "🛞 ${item.km}  ·  🕐 ${item.tempoHm}  ·  ⛽ Consumo ${item.consumo}"
                setTextColor(Color.parseColor("#90A4AE"))
                textSize = 11f
                setPadding(0, dp(context, 6), 0, dp(context, 4))
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            },
        )
        if (!item.embarque.isNullOrBlank()) {
            card.addView(linhaRotaHistorico(context, "●", item.embarque))
        }
        if (!item.destino.isNullOrBlank()) {
            card.addView(linhaRotaHistorico(context, "■", item.destino))
        }
        val mapas = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(context, 4), 0, 0)
        }
        if (!item.embarque.isNullOrBlank()) {
            mapas.addView(
                botaoMapaHistorico(context, "Embarque") {
                    abrirMapaHistorico(context, item.embarque, null)
                },
            )
        }
        if (!item.destino.isNullOrBlank()) {
            mapas.addView(
                botaoMapaHistorico(context, "Destino") {
                    abrirMapaHistorico(context, null, item.destino)
                },
            )
        }
        if (mapas.childCount > 0) {
            card.addView(mapas)
        }
        return card
    }

    private fun caixaMetricaHistorico(context: Context, valor: String): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(context, 2)
                marginEnd = dp(context, 2)
            }
            background = GradientDrawable().apply {
                setColor(OverlayTema.de(context).metrica)
                cornerRadius = dp(context, 6).toFloat()
            }
            setPadding(dp(context, 4), dp(context, 4), dp(context, 4), dp(context, 4))
            addView(
                TextView(context).apply {
                    text = valor
                    setTextColor(Color.parseColor(VERDE))
                    textSize = 11f
                    gravity = Gravity.CENTER
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    typeface = Typeface.DEFAULT_BOLD
                },
            )
        }
    }

    private fun linhaRotaHistorico(context: Context, marca: String, texto: String): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(context, 2), 0, 0)
            addView(
                TextView(context).apply {
                    text = marca
                    setTextColor(Color.parseColor(VERDE))
                    textSize = 10f
                    setPadding(0, 0, dp(context, 6), 0)
                },
            )
            addView(
                TextView(context).apply {
                    this.text = texto
                    setTextColor(OverlayTema.de(context).texto)
                    textSize = 11f
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                },
            )
        }
    }

    private fun botaoMapaHistorico(context: Context, rotulo: String, acao: () -> Unit): TextView {
        return TextView(context).apply {
            text = rotulo
            setTextColor(Color.parseColor(AMARELO))
            textSize = 12f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(dp(context, 4), dp(context, 6), dp(context, 4), dp(context, 4))
            setOnClickListener {
                acao()
            }
        }
    }

    private fun abrirMapaPercursoSelecionado(context: Context) {
        val snapshot = OverlayBridge.snapshot.value
        val item = snapshot.historicoItens.firstOrNull { it.chave == snapshot.historicoChaveSelecionada }
            ?: return
        if (item.embarque.isNullOrBlank() && item.destino.isNullOrBlank()) {
            return
        }
        abrirMapaHistorico(context, item.embarque, item.destino)
    }

    private fun abrirMapaHistorico(context: Context, embarque: String?, destino: String?) {
        OverlayBridge.emitir(OverlayAcao.SairParaMapaHistorico)
        val app = context.applicationContext as? GestorDriverApp ?: return
        val nav = app.configuracaoStore.carregar().navegacao
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            NavegacaoLauncher.abrir(
                context = context.applicationContext,
                navegacao = nav,
                embarque = embarque,
                destino = destino,
                corridaAceita = !destino.isNullOrBlank(),
            )
        }
    }

    private fun montarVeiculo(
        destino: LinearLayout,
        config: ConfiguracaoUsuario,
        planoPro: Boolean,
    ) {
        val ctx = destino.context
        destino.addView(
            secaoComAjuda(
                ctx,
                "🚗",
                "#E3F2FD",
                "Descrição do veículo",
                "Carro ou moto",
                "Final da placa (0–9) define o mês do IPVA. O valor R$ entra no custo do Dashboard.",
            ),
        )
        val tipos = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        tipos.addView(
            CheckBox(ctx).apply {
                text = "Carro"
                tag = "cfg_ck_carro"
                isChecked = config.tipoVeiculo == TipoVeiculo.CARRO
                setTextColor(OverlayTema.de(context).texto)
                textSize = 14f
                minHeight = dp(ctx, 48)
                isFocusableInTouchMode = false
                setOnCheckedChangeListener { _, marcado ->
                    if (marcado) {
                        rascunho = (rascunho ?: config).copy(tipoVeiculo = TipoVeiculo.CARRO)
                        destino.findViewWithTag<CheckBox>("cfg_ck_moto")?.isChecked = false
                    }
                }
            },
        )
        tipos.addView(
            CheckBox(ctx).apply {
                text = "Moto"
                tag = "cfg_ck_moto"
                isChecked = config.tipoVeiculo == TipoVeiculo.MOTO
                setTextColor(OverlayTema.de(context).texto)
                textSize = 14f
                minHeight = dp(ctx, 48)
                isFocusableInTouchMode = false
                setOnCheckedChangeListener { _, marcado ->
                    if (marcado) {
                        rascunho = (rascunho ?: config).copy(tipoVeiculo = TipoVeiculo.MOTO)
                        destino.findViewWithTag<CheckBox>("cfg_ck_carro")?.isChecked = false
                    }
                }
            },
        )
        destino.addView(tipos)
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
                campo(
                    ctx,
                    "IPVA R$",
                    DecimalInput.formatar(config.ipvaValor),
                    "cfg_ipva_valor",
                    bloqueado = !planoPro,
                    compacto = true,
                    pro = !planoPro,
                ).first,
            ),
        )
        destino.addView(
            TextView(ctx).apply {
                tag = "cfg_ipva_vencimento_rotulo"
                text = br.com.gestordriver.core.TabelaIpvaPlaca.textoVencimento(config.finalPlaca)
                setTextColor(OverlayTema.de(ctx).secundario)
                textSize = 11f
                setPadding(0, 0, 0, dp(ctx, 4))
            },
        )
        destino.addView(
            secaoComAjuda(
                ctx,
                "⛽",
                "#EDE7F6",
                "Consumo km/L",
                "Do combustível marcado",
                "Quilômetros por litro do combustível atual. Entra no consumo e no gasto estimados da oferta.",
            ),
        )
        destino.addView(
            linha(
                ctx,
                campo(ctx, "Gasolina", DecimalInput.formatar(config.consumoGasolina), "cfg_consumo_g", compacto = true).first,
                campo(ctx, "Etanol", DecimalInput.formatar(config.consumoEtanol), "cfg_consumo_e", compacto = true).first,
                campo(ctx, "Energia", DecimalInput.formatar(config.consumoEnergia), "cfg_consumo_energia", compacto = true).first,
            ),
        )
        if (planoPro) {
            destino.addView(rotulo(ctx, "Calcular abastecimento", compacto = true))
        } else {
            destino.addView(rotuloPro(ctx, "Calcular abastecimento", compacto = true))
        }
        destino.addView(
            linha(
                ctx,
                campo(ctx, "Valor R$", DecimalInput.formatar(config.abastecimentoValor), "cfg_abast_valor", bloqueado = !planoPro, compacto = true).first,
                campo(ctx, "Quant. litros", DecimalInput.formatar(config.abastecimentoLitros), "cfg_abast_litros", bloqueado = !planoPro, compacto = true).first,
            ),
        )
        destino.addView(
            linha(
                ctx,
                campo(ctx, "Km inicial", DecimalInput.formatar(config.abastecimentoKmInicial), "cfg_abast_km_ini", bloqueado = !planoPro, compacto = true).first,
                campo(ctx, "Km final", DecimalInput.formatar(config.abastecimentoKmFinal), "cfg_abast_km_fim", bloqueado = !planoPro, compacto = true).first,
            ),
        )
    }

    private fun montarCustos(
        destino: LinearLayout,
        config: ConfiguracaoUsuario,
        planoPro: Boolean,
    ) {
        val ctx = destino.context
        destino.addView(
            secaoComAjuda(
                ctx,
                "⛽",
                "#EDE7F6",
                "Despesas do veiculo",
                "Preço e tipo de energia",
                "Preço do litro ou do kWh. Com o consumo, o app calcula gasto e lucro da oferta.",
            ),
        )
        destino.addView(
            linha(
                ctx,
                campo(ctx, "R$ / L Gasolina", DecimalInput.formatar(config.precoGasolina), "cfg_preco_g").first,
                campo(ctx, "R$ / L Etanol", DecimalInput.formatar(config.precoEtanol), "cfg_preco_e").first,
                campo(ctx, "R$ / kWh", DecimalInput.formatar(config.precoEnergia), "cfg_preco_energia").first,
            ),
        )
        destino.addView(
            rotulo(ctx, "Combustível atual", secao = true),
        )
        destino.addView(
            rotulo(ctx, "Gasolina, etanol ou energia", compacto = true),
        )
        val ckG = CheckBox(ctx).apply {
            text = "Gasolina"
            tag = "cfg_ck_gasolina"
            setTextColor(OverlayTema.de(context).texto)
            textSize = 12f
            scaleX = 0.82f
            scaleY = 0.82f
            isFocusableInTouchMode = false
            isChecked = config.combustivel == Combustivel.GASOLINA
        }
        val ckE = CheckBox(ctx).apply {
            text = "Etanol"
            tag = "cfg_ck_etanol"
            setTextColor(OverlayTema.de(context).texto)
            textSize = 12f
            scaleX = 0.82f
            scaleY = 0.82f
            isFocusableInTouchMode = false
            isChecked = config.combustivel == Combustivel.ETANOL
        }
        val ckN = CheckBox(ctx).apply {
            text = "Energia"
            tag = "cfg_ck_energia"
            setTextColor(OverlayTema.de(context).texto)
            textSize = 12f
            scaleX = 0.82f
            scaleY = 0.82f
            isFocusableInTouchMode = false
            isChecked = config.combustivel == Combustivel.ENERGIA
        }
        fun marcarCombustivel(alvo: Combustivel) {
            ckG.isChecked = alvo == Combustivel.GASOLINA
            ckE.isChecked = alvo == Combustivel.ETANOL
            ckN.isChecked = alvo == Combustivel.ENERGIA
            rascunho = (rascunho ?: config).copy(combustivel = alvo)
        }
        ckG.setOnCheckedChangeListener { _, marcado -> if (marcado) marcarCombustivel(Combustivel.GASOLINA) }
        ckE.setOnCheckedChangeListener { _, marcado -> if (marcado) marcarCombustivel(Combustivel.ETANOL) }
        ckN.setOnCheckedChangeListener { _, marcado -> if (marcado) marcarCombustivel(Combustivel.ENERGIA) }
        destino.addView(linha(ctx, ckG, ckE, ckN))
        if (planoPro) {
            destino.addView(rotulo(ctx, "Troca de óleo (óleo e filtros)", compacto = true))
        } else {
            destino.addView(rotuloPro(ctx, "Troca de óleo (óleo e filtros)", compacto = true))
        }
        destino.addView(
            linha(
                ctx,
                campo(ctx, "Valor R$", DecimalInput.formatar(config.oleoValor), "cfg_oleo_valor", bloqueado = !planoPro, compacto = true).first,
                campo(ctx, "Km", DecimalInput.formatar(config.oleoKilometragem), "cfg_oleo_km", bloqueado = !planoPro, compacto = true).first,
                campo(ctx, "Data", config.oleoData, "cfg_oleo_data", bloqueado = !planoPro, compacto = true).first,
            ),
        )
        adicionarAlertaOleo(destino, ctx, config)
        if (planoPro) {
            destino.addView(rotulo(ctx, "Custo estimado dos pneus", compacto = true))
        } else {
            destino.addView(rotuloPro(ctx, "Custo estimado dos pneus", compacto = true))
        }
        destino.addView(rotulo(ctx, "Dianteiro", compacto = true))
        destino.addView(
            linha(
                ctx,
                campo(ctx, "Valor R$", DecimalInput.formatar(config.pneuDianteiroValor), "cfg_pneu_d_valor", bloqueado = !planoPro, compacto = true).first,
                campo(ctx, "Rodagem", DecimalInput.formatar(config.pneuDianteiroRodagem), "cfg_pneu_d_km", bloqueado = !planoPro, compacto = true).first,
                campo(ctx, "Data", config.pneuDianteiroData, "cfg_pneu_d_data", bloqueado = !planoPro, compacto = true).first,
            ),
        )
        destino.addView(rotulo(ctx, "Traseiro", compacto = true))
        destino.addView(
            linha(
                ctx,
                campo(ctx, "Valor R$", DecimalInput.formatar(config.pneuTraseiroValor), "cfg_pneu_t_valor", bloqueado = !planoPro, compacto = true).first,
                campo(ctx, "Rodagem", DecimalInput.formatar(config.pneuTraseiroRodagem), "cfg_pneu_t_km", bloqueado = !planoPro, compacto = true).first,
                campo(ctx, "Data", config.pneuTraseiroData, "cfg_pneu_t_data", bloqueado = !planoPro, compacto = true).first,
            ),
        )
        if (planoPro) {
            destino.addView(rotulo(ctx, "Seguro", compacto = true))
        } else {
            destino.addView(rotuloPro(ctx, "Seguro", compacto = true))
        }
        destino.addView(
            linha(
                ctx,
                campo(ctx, "Valor do seguro", DecimalInput.formatar(config.seguroValor), "cfg_seguro_valor", bloqueado = !planoPro, compacto = true).first,
                campo(ctx, "Data de vencimento", config.seguroData, "cfg_seguro_data", bloqueado = !planoPro, compacto = true).first,
            ),
        )
        val ckMensal = CheckBox(ctx).apply {
            text = "Mensal"
            tag = "cfg_ck_seguro_mensal"
            setTextColor(OverlayTema.de(context).texto)
            textSize = 12f
            scaleX = 0.82f
            scaleY = 0.82f
            isFocusableInTouchMode = false
            isEnabled = planoPro
            isChecked = config.seguroRecorrencia == SeguroRecorrencia.MENSAL
        }
        val ckAnual = CheckBox(ctx).apply {
            text = "Anual"
            tag = "cfg_ck_seguro_anual"
            setTextColor(OverlayTema.de(context).texto)
            textSize = 12f
            scaleX = 0.82f
            scaleY = 0.82f
            isFocusableInTouchMode = false
            isEnabled = planoPro
            isChecked = config.seguroRecorrencia == SeguroRecorrencia.ANUAL
        }
        ckMensal.setOnCheckedChangeListener { _, marcado ->
            if (marcado) {
                ckAnual.isChecked = false
                rascunho = (rascunho ?: config).copy(seguroRecorrencia = SeguroRecorrencia.MENSAL)
            }
        }
        ckAnual.setOnCheckedChangeListener { _, marcado ->
            if (marcado) {
                ckMensal.isChecked = false
                rascunho = (rascunho ?: config).copy(seguroRecorrencia = SeguroRecorrencia.ANUAL)
            }
        }
        destino.addView(rotulo(ctx, "Recorrência", compacto = true))
        destino.addView(linha(ctx, ckMensal, ckAnual))
    }

    private fun montarApp(
        destino: LinearLayout,
        config: ConfiguracaoUsuario,
        snapshot: OverlaySnapshot,
        context: Context,
    ) {
        destino.addView(
            secaoComAjuda(
                context,
                "⚙",
                "#E0F2F1",
                "Configurações do aplicativo",
                "Configurar app",
                "",
            ),
        )
        destino.addView(
            secaoComAjuda(
                context,
                "",
                "#E0F2F1",
                "Permissões",
                "Para monitorar ofertas",
                "Notificação, sobrepor, acessibilidade e bateria são obrigatórias. Localização ajuda o mapa. Acessibilidade: Configurações restritas → Serviços instalados.",
            ),
        )
        destino.addView(
            listaPermissoes(
                context,
                snapshot.destacarPermissoes,
                ItemPermissao(
                    "1  Notificações",
                    PermissoesMonitoramento.listenerNotificacoesAtivo(context),
                    dica = "Lê as ofertas da Uber e da 99",
                ) {
                    context.startActivity(PermissoesMonitoramento.intentNotificacoes().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                },
                ItemPermissao(
                    "2  Sobrepor",
                    PermissoesMonitoramento.overlayConcedida(context),
                    dica = "Mostra o card sobre o mapa",
                ) {
                    context.startActivity(PermissoesMonitoramento.intentSobrepor(context).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                },
                ItemPermissao(
                    "3  Acessibilidade",
                    PermissoesMonitoramento.acessibilidadeAtiva(context),
                    dica = "Configurações restritas → Serviços instalados",
                ) {
                    context.startActivity(
                        Intent(context, MainActivity::class.java)
                            .putExtra(MainActivity.EXTRA_PEDIR_ACESSIBILIDADE, true)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    )
                },
                ItemPermissao(
                    "4  Bateria",
                    PermissoesMonitoramento.bateriaLiberada(context),
                    dica = "Evita o overlay sumir no segundo plano",
                ) {
                    context.startActivity(
                        Intent(context, MainActivity::class.java)
                            .putExtra(MainActivity.EXTRA_PEDIR_BATERIA, true)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    )
                },
                ItemPermissao(
                    "5  Localização",
                    PermissoesMonitoramento.localizacaoConcedida(context),
                    obrigatoria = false,
                    dica = "Opcional. Ajuda o mapa",
                ) {
                    context.startActivity(
                        Intent(context, MainActivity::class.java)
                            .putExtra(MainActivity.EXTRA_PEDIR_LOCALIZACAO, true)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    )
                },
            ),
        )
        destino.addView(rotulo(context, "App de corrida", secao = true))
        val apps = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        listOf(
            Plataforma.UBER to "Uber",
            Plataforma.NOVE_NOVE to "99",
            Plataforma.INDRIVE to "InDrive",
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
        destino.addView(
            secaoComAjuda(
                context,
                "",
                "#F3E5F5",
                "Tema",
                "Escuro, claro ou do celular",
                "Define as cores do overlay e das telas. Celular segue o modo do aparelho.",
            ),
        )
        val temaEscuro = CheckBox(context).apply {
            text = "Escuro"
            tag = "cfg_tema_escuro"
            setTextColor(OverlayTema.de(context).texto)
            isFocusableInTouchMode = false
            isChecked = config.tema == br.com.gestordriver.model.TemaApp.ESCURO
        }
        val temaClaro = CheckBox(context).apply {
            text = "Claro"
            tag = "cfg_tema_claro"
            setTextColor(OverlayTema.de(context).texto)
            isFocusableInTouchMode = false
            isChecked = config.tema == br.com.gestordriver.model.TemaApp.CLARO
        }
        val temaCelular = CheckBox(context).apply {
            text = "Celular"
            tag = "cfg_tema_celular"
            setTextColor(OverlayTema.de(context).texto)
            isFocusableInTouchMode = false
            isChecked = config.tema == br.com.gestordriver.model.TemaApp.CELULAR
        }
        fun marcarTema(escolhido: br.com.gestordriver.model.TemaApp) {
            temaEscuro.isChecked = escolhido == br.com.gestordriver.model.TemaApp.ESCURO
            temaClaro.isChecked = escolhido == br.com.gestordriver.model.TemaApp.CLARO
            temaCelular.isChecked = escolhido == br.com.gestordriver.model.TemaApp.CELULAR
            rascunho = (rascunho ?: config).copy(tema = escolhido)
        }
        temaEscuro.setOnClickListener { marcarTema(br.com.gestordriver.model.TemaApp.ESCURO) }
        temaClaro.setOnClickListener { marcarTema(br.com.gestordriver.model.TemaApp.CLARO) }
        temaCelular.setOnClickListener { marcarTema(br.com.gestordriver.model.TemaApp.CELULAR) }
        destino.addView(linha(context, temaEscuro, temaClaro, temaCelular))
        destino.addView(
            secaoComAjuda(
                context,
                "",
                "#E3F2FD",
                "Navegação",
                "Maps ou Waze",
                "App de mapa para abrir embarque e destino quando o endereço for lido.",
            ),
        )
        val maps = CheckBox(context).apply {
            text = "Google Maps"
            tag = "cfg_ck_maps"
            setTextColor(OverlayTema.de(context).texto)
            isFocusableInTouchMode = false
            isChecked = config.navegacao == AppNavegacao.GOOGLE_MAPS
        }
        val waze = CheckBox(context).apply {
            text = "Waze"
            tag = "cfg_ck_waze"
            setTextColor(OverlayTema.de(context).texto)
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
                setTextColor(OverlayTema.de(context).secundario)
                textSize = 12f
                gravity = Gravity.CENTER
                setPadding(0, dp(context, 6), 0, dp(context, 6))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            },
        )
        destino.addView(logVersao)
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
                setTextColor(OverlayTema.de(context).texto)
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(context, 8))
            },
        )
        coluna.addView(
            TextView(context).apply {
                text = "Conecte a conta Google do motorista para identificar o usuário nas versões Free e Pro."
                setTextColor(OverlayTema.de(context).secundario)
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
                setTextColor(OverlayTema.de(context).secundario)
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
                setTextColor(OverlayTema.de(context).texto)
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
                setTextColor(OverlayTema.de(context).secundario)
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
        destino.addView(
            secaoComAjuda(
                ctx,
                "🚦",
                "#FFF8E1",
                "Calibrar a classificação",
                "Cor da borda da compacta",
                "Faixas de R$/km. A cor da borda da compacta segue esta escala. Arraste a barra ou use − e + de 0,01.",
            ),
        )
        destino.addView(barrasSemaforo(ctx, config, destino))
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
                    compacto = true,
                ),
            )
        }
    }

    private fun barrasSemaforo(
        context: Context,
        config: ConfiguracaoUsuario,
        raiz: LinearLayout,
    ): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(barraMarca(context, "Ruim até", "cfg_slide_ruim", config.limiteRuimMax, ClassificacaoConstantes.CORES.getValue(Classificacao.RUIM), raiz))
            addView(barraMarca(context, "Boa até", "cfg_slide_boa", config.limiteBoaMax, ClassificacaoConstantes.CORES.getValue(Classificacao.BOA), raiz))
        }

    private fun barraMarca(
        context: Context,
        titulo: String,
        tag: String,
        valor: Double,
        cor: String,
        raiz: LinearLayout,
    ): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(context, 4), 0, dp(context, 4))
            addView(
                TextView(context).apply {
                    text = "$titulo  ${FaixasClassificacao.formatar(valor)}"
                    this.tag = "${tag}_rotulo"
                    setTextColor(Color.parseColor(cor))
                    textSize = 14f
                },
            )
            addView(
                SeekBar(context).apply {
                    this.tag = tag
                    max = 500
                    progress = (valor * 100).toInt().coerceIn(0, 500)
                    minHeight = dp(context, 48)
                    setOnSeekBarChangeListener(
                        object : SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                                if (!fromUser) {
                                    return
                                }
                                aplicarMarcasDeslizantes(raiz)
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

                            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
                        },
                    )
                },
            )
        }

    private fun aplicarMarcasDeslizantes(raiz: View) {
        val atual = rascunho ?: return
        fun marca(tag: String, fallback: Double): Double {
            val progresso = raiz.findViewWithTag<SeekBar>(tag)?.progress ?: return fallback
            return progresso / 100.0
        }
        val encadeada = FaixasClassificacao.aplicarMarcas(
            atual,
            marca("cfg_slide_ruim", atual.limiteRuimMax),
            marca("cfg_slide_boa", atual.limiteBoaMax),
        )
        rascunho = encadeada
        pintarFaixas(raiz, encadeada)
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
        fun barra(tag: String, valor: Double, titulo: String) {
            raiz.findViewWithTag<SeekBar>(tag)?.progress = (valor * 100).toInt().coerceIn(0, 500)
            raiz.findViewWithTag<TextView>("${tag}_rotulo")?.text =
                "$titulo  ${FaixasClassificacao.formatar(valor)}"
        }
        barra("cfg_slide_ruim", config.limiteRuimMax, "Ruim até")
        barra("cfg_slide_boa", config.limiteBoaMax, "Boa até")
    }

    private fun colherAba(raiz: View, aba: Int, base: ConfiguracaoUsuario): ConfiguracaoUsuario {
        fun txt(tag: String): String? = raiz.findViewWithTag<EditText>(tag)?.text?.toString()
        fun num(tag: String, atual: Double): Double = DecimalInput.parse(txt(tag) ?: "") ?: atual
        return when (aba) {
            0 -> {
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
            1 -> {
                val combustivelMarcado = when {
                    raiz.findViewWithTag<CheckBox>("cfg_ck_energia")?.isChecked == true -> Combustivel.ENERGIA
                    raiz.findViewWithTag<CheckBox>("cfg_ck_etanol")?.isChecked == true -> Combustivel.ETANOL
                    raiz.findViewWithTag<CheckBox>("cfg_ck_gasolina")?.isChecked == true -> Combustivel.GASOLINA
                    else -> base.combustivel
                }
                val seguroRecorrencia = when {
                    raiz.findViewWithTag<CheckBox>("cfg_ck_seguro_mensal")?.isChecked == true -> SeguroRecorrencia.MENSAL
                    raiz.findViewWithTag<CheckBox>("cfg_ck_seguro_anual")?.isChecked == true -> SeguroRecorrencia.ANUAL
                    else -> base.seguroRecorrencia
                }
                base.copy(
                    precoGasolina = num("cfg_preco_g", base.precoGasolina),
                    precoEtanol = num("cfg_preco_e", base.precoEtanol),
                    precoEnergia = num("cfg_preco_energia", base.precoEnergia),
                    combustivel = combustivelMarcado,
                    oleoValor = num("cfg_oleo_valor", base.oleoValor),
                    oleoKilometragem = num("cfg_oleo_km", base.oleoKilometragem),
                    oleoData = txt("cfg_oleo_data") ?: base.oleoData,
                    pneuDianteiroValor = num("cfg_pneu_d_valor", base.pneuDianteiroValor),
                    pneuDianteiroRodagem = num("cfg_pneu_d_km", base.pneuDianteiroRodagem),
                    pneuDianteiroData = txt("cfg_pneu_d_data") ?: base.pneuDianteiroData,
                    pneuTraseiroValor = num("cfg_pneu_t_valor", base.pneuTraseiroValor),
                    pneuTraseiroRodagem = num("cfg_pneu_t_km", base.pneuTraseiroRodagem),
                    pneuTraseiroData = txt("cfg_pneu_t_data") ?: base.pneuTraseiroData,
                    seguroValor = num("cfg_seguro_valor", base.seguroValor),
                    seguroData = txt("cfg_seguro_data") ?: base.seguroData,
                    seguroRecorrencia = seguroRecorrencia,
                )
            }
            2 -> {
                val carro = raiz.findViewWithTag<CheckBox>("cfg_ck_carro")?.isChecked
                base.copy(
                    tipoVeiculo = when (carro) {
                        true -> TipoVeiculo.CARRO
                        false -> TipoVeiculo.MOTO
                        null -> base.tipoVeiculo
                    },
                    marcaVeiculo = txt("cfg_marca") ?: base.marcaVeiculo,
                    modeloVeiculo = txt("cfg_modelo") ?: base.modeloVeiculo,
                    versaoVeiculo = txt("cfg_versao") ?: base.versaoVeiculo,
                    anoVeiculo = txt("cfg_ano") ?: base.anoVeiculo,
                    finalPlaca = txt("cfg_placa")?.filter { it.isDigit() }?.takeLast(1) ?: base.finalPlaca,
                    ipvaValor = num("cfg_ipva_valor", base.ipvaValor),
                    ipvaVencimento = br.com.gestordriver.core.TabelaIpvaPlaca.rotuloMesVencimento(
                        txt("cfg_placa") ?: base.finalPlaca,
                    ).takeIf { it != "—" }.orEmpty(),
                    consumoGasolina = num("cfg_consumo_g", base.consumoGasolina),
                    consumoEtanol = num("cfg_consumo_e", base.consumoEtanol),
                    consumoEnergia = num("cfg_consumo_energia", base.consumoEnergia),
                    abastecimentoValor = num("cfg_abast_valor", base.abastecimentoValor),
                    abastecimentoLitros = num("cfg_abast_litros", base.abastecimentoLitros),
                    abastecimentoKmInicial = num("cfg_abast_km_ini", base.abastecimentoKmInicial),
                    abastecimentoKmFinal = num("cfg_abast_km_fim", base.abastecimentoKmFinal),
                )
            }
            else -> {
                val maps = raiz.findViewWithTag<CheckBox>("cfg_ck_maps")?.isChecked
                val tema = when {
                    raiz.findViewWithTag<CheckBox>("cfg_tema_escuro")?.isChecked == true ->
                        br.com.gestordriver.model.TemaApp.ESCURO
                    raiz.findViewWithTag<CheckBox>("cfg_tema_claro")?.isChecked == true ->
                        br.com.gestordriver.model.TemaApp.CLARO
                    raiz.findViewWithTag<CheckBox>("cfg_tema_celular")?.isChecked == true ->
                        br.com.gestordriver.model.TemaApp.CELULAR
                    else -> base.tema
                }
                base.copy(
                    navegacao = when (maps) {
                        true -> AppNavegacao.GOOGLE_MAPS
                        false -> AppNavegacao.WAZE
                        null -> base.navegacao
                    },
                    tema = tema,
                )
            }
        }
    }

    private data class ItemPermissao(
        val titulo: String,
        val ok: Boolean,
        val obrigatoria: Boolean = true,
        val dica: String = "",
        val onClick: () -> Unit,
    )

    private fun listaPermissoes(
        context: Context,
        destacar: Boolean,
        vararg itens: ItemPermissao,
    ): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            itens.toList().chunked(2).forEach { linhaItens ->
                addView(
                    LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.TOP
                        linhaItens.forEach { item ->
                            addView(
                                LinearLayout(context).apply {
                                    orientation = LinearLayout.VERTICAL
                                    gravity = Gravity.START
                                    isClickable = true
                                    minimumHeight = dp(context, 48)
                                    setPadding(0, dp(context, 8), dp(context, 4), dp(context, 8))
                                    layoutParams = LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1f,
                                    )
                                    setOnClickListener { item.onClick() }
                                    addView(
                                        linhaPermissao(
                                            context,
                                            item.titulo,
                                            item.ok,
                                            destacar && item.obrigatoria && !item.ok,
                                            item.onClick,
                                        ),
                                    )
                                    if (item.dica.isNotBlank()) {
                                        addView(
                                            TextView(context).apply {
                                                text = item.dica
                                                setTextColor(OverlayTema.de(context).secundario)
                                                textSize = 11f
                                                setPadding(dp(context, 2), dp(context, 2), 0, 0)
                                            },
                                        )
                                    }
                                },
                            )
                        }
                        if (linhaItens.size == 1) {
                            addView(View(context), LinearLayout.LayoutParams(0, 1, 1f))
                        }
                    },
                )
            }
        }

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
        pintar("1  Notificações", PermissoesMonitoramento.listenerNotificacoesAtivo(context), true)
        pintar("2  Sobrepor", PermissoesMonitoramento.overlayConcedida(context), true)
        pintar("3  Acessibilidade", PermissoesMonitoramento.acessibilidadeAtiva(context), true)
        pintar("4  Bateria", PermissoesMonitoramento.bateriaLiberada(context), true)
        pintar("5  Localização", PermissoesMonitoramento.localizacaoConcedida(context), false)
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
            textSize = 14f
            setPadding(0, dp(context, 10), 0, dp(context, 10))
            minHeight = dp(context, 48)
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

    private fun caixaCampo(context: Context): GradientDrawable {
        val tema = OverlayTema.de(context)
        return GradientDrawable().apply {
            setColor(tema.caixa)
            setStroke(dp(context, 1), tema.borda)
            cornerRadius = dp(context, 6).toFloat()
        }
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
            setTextColor(OverlayTema.de(context).texto)
            textSize = 13f
            isEnabled = !bloqueado
            isCursorVisible = false
            minHeight = dp(context, 32)
            showSoftInputOnFocus = true
            setOnFocusChangeListener { v, temFoco ->
                (v as EditText).isCursorVisible = temFoco
            }
            setHintTextColor(OverlayTema.de(context).secundario)
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
            setPadding(0, dp(context, 2), 0, 0)
            addView(
                TextView(context).apply {
                    text = texto
                    setTextColor(OverlayTema.de(context).texto)
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

    private fun secaoComAjuda(
        context: Context,
        icone: String,
        fundoIcone: String,
        titulo: String,
        subtitulo: String,
        ajuda: String,
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(context, 8), 0, dp(context, 6))
            if (icone.isNotBlank()) {
                addView(
                    TextView(context).apply {
                        text = icone
                        gravity = Gravity.CENTER
                        textSize = 13f
                        background = GradientDrawable().apply {
                            setColor(Color.parseColor(fundoIcone))
                            cornerRadius = dp(context, 7).toFloat()
                        }
                        layoutParams = LinearLayout.LayoutParams(dp(context, 28), dp(context, 28)).apply {
                            marginEnd = dp(context, 8)
                        }
                    },
                )
            }
            addView(
                LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    addView(
                        TextView(context).apply {
                            text = titulo
                            setTextColor(OverlayTema.de(context).texto)
                            textSize = 13f
                            typeface = Typeface.DEFAULT_BOLD
                        },
                    )
                    addView(
                        TextView(context).apply {
                            text = subtitulo
                            setTextColor(OverlayTema.de(context).secundario)
                            textSize = 10f
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                        },
                    )
                },
            )
            if (ajuda.isNotBlank()) {
                addView(
                    TextView(context).apply {
                        text = "AJUDA"
                        setTextColor(Color.parseColor(AMARELO))
                        textSize = 11f
                        typeface = Typeface.DEFAULT_BOLD
                        setPadding(dp(context, 8), dp(context, 4), 0, dp(context, 4))
                        setOnClickListener {
                            Toast.makeText(context, ajuda, Toast.LENGTH_LONG).show()
                        }
                    },
                )
            }
        }
    }

    private fun rotulo(
        context: Context,
        texto: String,
        compacto: Boolean = false,
        secao: Boolean = false,
    ): TextView =
        TextView(context).apply {
            text = texto
            setTextColor(OverlayTema.de(context).secundario)
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
                    setTextColor(OverlayTema.de(context).secundario)
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
            setPadding(0, 0, 0, dp(context, 2))
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
            setTextColor(OverlayTema.de(context).texto)
            textSize = 13f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        fun botao(texto: String): TextView = TextView(context).apply {
            this.text = texto
            setTextColor(if (editavel) Color.parseColor(AMARELO) else OverlayTema.de(context).secundario)
            textSize = 16f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            minWidth = dp(context, 32)
            minHeight = dp(context, 32)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#22000000"))
                setStroke(dp(context, 1), if (editavel) Color.parseColor(AMARELO) else OverlayTema.de(context).borda)
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

    private fun linha(context: Context, vararg filhos: View, compacto: Boolean = false): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.BOTTOM
            val pad = if (compacto) 0 else 2
            setPadding(0, dp(context, pad), 0, dp(context, pad))
            filhos.forEach { filho ->
                filho.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    gravity = Gravity.BOTTOM
                    marginEnd = dp(context, 4)
                }
                addView(filho)
            }
        }

    private fun cabecalhoConfiguracoes(context: Context, onVoltar: () -> Unit): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            tag = "config_cabecalho"
            setPadding(dp(context, 8), 0, dp(context, 12), dp(context, 8))
            addView(
                TextView(context).apply {
                    text = "←"
                    setTextColor(OverlayTema.de(context).texto)
                    textSize = 22f
                    gravity = Gravity.CENTER
                    setPadding(dp(context, 8), dp(context, 4), dp(context, 12), dp(context, 4))
                    setOnClickListener { onVoltar() }
                },
            )
            addView(
                TextView(context).apply {
                    tag = "config_titulo"
                    text = "Configurações"
                    setTextColor(OverlayTema.de(context).texto)
                    textSize = 16f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER_VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                },
            )
        }
    }

    private fun abaConfiguracao(context: Context, indice: Int, titulo: String): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            isClickable = true
            isFocusable = true
            setOnClickListener { selecionarAbaConfig(indice) }
            addView(
                TextView(context).apply {
                    text = titulo
                    tag = "cfg_aba_$indice"
                    setTextColor(OverlayTema.de(context).secundario)
                    textSize = 11f
                    gravity = Gravity.CENTER
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    setPadding(dp(context, 2), dp(context, 8), dp(context, 2), dp(context, 6))
                },
            )
            addView(
                View(context).apply {
                    tag = "cfg_aba_ind_$indice"
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(context, 2),
                    )
                    setBackgroundColor(Color.TRANSPARENT)
                },
            )
        }
    }

    private fun setaAba(context: Context, texto: String, onClick: () -> Unit): TextView =
        TextView(context).apply {
            text = texto
            textSize = 14f
            gravity = Gravity.CENTER
            minHeight = dp(context, 48)
            setPadding(dp(context, 4), dp(context, 2), dp(context, 4), dp(context, 2))
            setOnClickListener { onClick() }
        }

    private fun tituloComSetas(
        context: Context,
        titulo: String,
        onEsquerda: () -> Unit,
        onDireita: () -> Unit,
        tituloTag: String? = null,
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
                    minHeight = dp(context, 48)
                    setOnClickListener { onEsquerda() }
                },
            )
            addView(
                TextView(context).apply {
                    text = titulo
                    tag = tituloTag
                    setTextColor(OverlayTema.de(context).texto)
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
            setColor(OverlayTema.de(context).secundario)
            cornerRadius = dp(context, 2).toFloat()
            setSize(dp(context, 4), dp(context, 28))
        }
        setVerticalScrollbarThumbDrawable(polegar)
        setVerticalScrollbarTrackDrawable(null)
    }

    private fun fundoNeutro(context: Context): GradientDrawable {
        val tema = OverlayTema.de(context)
        return GradientDrawable().apply {
            setColor(tema.fundoPainel)
            setStroke(dp(context, 2), tema.borda)
            cornerRadius = dp(context, 10).toFloat()
        }
    }

    private fun dp(context: Context, valor: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            valor.toFloat(),
            context.resources.displayMetrics,
        ).toInt()
}
