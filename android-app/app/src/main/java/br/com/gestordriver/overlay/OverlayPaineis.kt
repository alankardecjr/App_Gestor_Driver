package br.com.gestordriver.overlay

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
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
import br.com.gestordriver.permission.PermissoesMonitoramento
import br.com.gestordriver.ui.DecimalInput

object OverlayPaineis {
    private const val FUNDO = "#F2050809"
    private const val TEXTO = "#FFFFFF"
    private const val SECUNDARIO = "#B8C5D1"
    private const val AMARELO = "#FFD54F"
    private const val VERDE = "#7CB342"

    fun criarHistorico(context: Context): ScrollView {
        val scroll = ScrollView(context)
        scroll.background = fundoNeutro(context)
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
        return scroll
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

    fun criarConfig(context: Context): ScrollView {
        val scroll = ScrollView(context)
        scroll.background = fundoNeutro(context)
        val coluna = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(context, 10), dp(context, 8), dp(context, 10), dp(context, 8))
            tag = "config_coluna"
        }
        coluna.addView(
            TextView(context).apply {
                text = "CONFIGURAÇÃO"
                setTextColor(Color.WHITE)
                textSize = 13f
                gravity = Gravity.CENTER
            },
        )
        val abas = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            tag = "config_abas"
            gravity = Gravity.CENTER
        }
        listOf("VEÍCULO", "CLASSIFICAÇÃO", "APP").forEachIndexed { indice, titulo ->
            abas.addView(
                TextView(context).apply {
                    text = titulo
                    tag = "cfg_aba_$indice"
                    setTextColor(Color.parseColor(SECUNDARIO))
                    textSize = 11f
                    gravity = Gravity.CENTER
                    setPadding(dp(context, 6), dp(context, 6), dp(context, 6), dp(context, 6))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    setOnClickListener { OverlayBridge.emitir(OverlayAcao.AbaConfiguracao(indice)) }
                },
            )
        }
        coluna.addView(abas)
        coluna.addView(
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                tag = "config_conteudo"
            },
        )
        scroll.addView(coluna)
        return scroll
    }

    fun atualizarConfig(view: View, snapshot: OverlaySnapshot) {
        val coluna = (view as ScrollView).getChildAt(0) as LinearLayout
        val abas = coluna.findViewWithTag<LinearLayout>("config_abas")
        repeat(3) { indice ->
            val rotulo = abas.findViewWithTag<TextView>("cfg_aba_$indice")
            val selecionada = snapshot.abaConfiguracao == indice
            rotulo.setTextColor(Color.parseColor(if (selecionada) VERDE else SECUNDARIO))
            rotulo.typeface = if (selecionada) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        val conteudo = coluna.findViewWithTag<LinearLayout>("config_conteudo")
        if (conteudo.childCount > 0 && conteudo.tag == snapshot.abaConfiguracao) {
            if (snapshot.abaConfiguracao == 2) {
                atualizarPermissoes(conteudo, snapshot, view.context)
            }
            return
        }
        conteudo.removeAllViews()
        conteudo.tag = snapshot.abaConfiguracao
        val store = (view.context.applicationContext as GestorDriverApp).configuracaoStore
        val config = store.carregar()
        when (snapshot.abaConfiguracao) {
            0 -> montarVeiculo(conteudo, config, store)
            1 -> montarClassificacao(conteudo, config, store)
            else -> montarApp(conteudo, config, store, snapshot, view.context)
        }
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
        store: br.com.gestordriver.data.ConfiguracaoStore,
    ) {
        val ctx = destino.context
        val marca = campo(ctx, "MARCA", config.marcaVeiculo)
        val modelo = campo(ctx, "MODELO", config.modeloVeiculo)
        val versao = campo(ctx, "VERSÃO", config.versaoVeiculo)
        val ano = campo(ctx, "ANO", config.anoVeiculo)
        val placa = campo(ctx, "FINAL DA PLACA", config.finalPlaca)
        val gasolina = campo(ctx, "GASOLINA (km/L)", DecimalInput.formatar(config.consumoGasolina))
        val etanol = campo(ctx, "ETANOL (km/L)", DecimalInput.formatar(config.consumoEtanol))
        listOf(marca, modelo, versao, ano, placa).forEach { destino.addView(it.first) }
        destino.addView(rotulo(ctx, "CONSUMO"))
        destino.addView(linha(ctx, gasolina.first, etanol.first))
        destino.addView(rotulo(ctx, "COMBUSTÍVEL ATUAL"))
        val ckG = CheckBox(ctx).apply {
            text = "GASOLINA"
            setTextColor(Color.WHITE)
            isChecked = config.combustivel == Combustivel.GASOLINA
        }
        val ckE = CheckBox(ctx).apply {
            text = "ETANOL"
            setTextColor(Color.WHITE)
            isChecked = config.combustivel == Combustivel.ETANOL
        }
        ckG.setOnCheckedChangeListener { _, marcado ->
            if (marcado) {
                ckE.isChecked = false
                salvar(store, store.carregar().copy(combustivel = Combustivel.GASOLINA))
            }
        }
        ckE.setOnCheckedChangeListener { _, marcado ->
            if (marcado) {
                ckG.isChecked = false
                salvar(store, store.carregar().copy(combustivel = Combustivel.ETANOL))
            }
        }
        destino.addView(linha(ctx, ckG, ckE))
        fun persistir() {
            val atual = store.carregar()
            salvar(
                store,
                atual.copy(
                    marcaVeiculo = marca.second.text.toString(),
                    modeloVeiculo = modelo.second.text.toString(),
                    versaoVeiculo = versao.second.text.toString(),
                    anoVeiculo = ano.second.text.toString(),
                    finalPlaca = placa.second.text.toString(),
                    consumoGasolina = DecimalInput.parse(gasolina.second.text.toString()) ?: atual.consumoGasolina,
                    consumoEtanol = DecimalInput.parse(etanol.second.text.toString()) ?: atual.consumoEtanol,
                ),
            )
        }
        listOf(marca, modelo, versao, ano, placa, gasolina, etanol).forEach { par ->
            par.second.setOnFocusChangeListener { _, temFoco -> if (!temFoco) persistir() }
        }
    }

    private fun montarClassificacao(
        destino: LinearLayout,
        config: ConfiguracaoUsuario,
        store: br.com.gestordriver.data.ConfiguracaoStore,
    ) {
        val ctx = destino.context
        data class Faixa(
            val titulo: String,
            val min: Double,
            val max: Double,
            val minFixo: String?,
            val maxFixo: String?,
            val campoMin: br.com.gestordriver.core.FaixasClassificacao.Campo,
            val campoMax: br.com.gestordriver.core.FaixasClassificacao.Campo,
        )
        val faixas = listOf(
            Faixa(
                "RUIM",
                config.limiteRuimMin,
                config.limiteRuimMax,
                "MIN",
                null,
                br.com.gestordriver.core.FaixasClassificacao.Campo.RUIM_MIN,
                br.com.gestordriver.core.FaixasClassificacao.Campo.RUIM_MAX,
            ),
            Faixa(
                "REGULAR",
                config.limiteRegularMin,
                config.limiteRegularMax,
                null,
                null,
                br.com.gestordriver.core.FaixasClassificacao.Campo.REGULAR_MIN,
                br.com.gestordriver.core.FaixasClassificacao.Campo.REGULAR_MAX,
            ),
            Faixa(
                "BOA",
                config.limiteBoaMin,
                config.limiteBoaMax,
                null,
                null,
                br.com.gestordriver.core.FaixasClassificacao.Campo.BOA_MIN,
                br.com.gestordriver.core.FaixasClassificacao.Campo.BOA_MAX,
            ),
            Faixa(
                "ÓTIMA",
                config.limiteOtimaMin,
                config.limiteOtimaMax,
                null,
                "MAX",
                br.com.gestordriver.core.FaixasClassificacao.Campo.OTIMA_MIN,
                br.com.gestordriver.core.FaixasClassificacao.Campo.OTIMA_MAX,
            ),
        )
        faixas.forEach { faixa ->
            destino.addView(rotulo(ctx, faixa.titulo))
            val min = campo(
                ctx,
                "MIN",
                faixa.minFixo ?: br.com.gestordriver.core.FaixasClassificacao.formatar(faixa.min),
            )
            val max = campo(
                ctx,
                "MAX",
                faixa.maxFixo ?: br.com.gestordriver.core.FaixasClassificacao.formatar(faixa.max),
            )
            min.second.isEnabled = faixa.minFixo == null
            max.second.isEnabled = faixa.maxFixo == null
            destino.addView(linha(ctx, min.first, max.first))
            val remount = {
                destino.removeAllViews()
                montarClassificacao(destino, store.carregar(), store)
            }
            min.second.setOnFocusChangeListener { _, temFoco ->
                if (!temFoco && faixa.minFixo == null) {
                    val nMin = DecimalInput.parse(min.second.text.toString()) ?: faixa.min
                    salvar(store, br.com.gestordriver.core.FaixasClassificacao.aplicar(store.carregar(), faixa.campoMin, nMin))
                    remount()
                }
            }
            max.second.setOnFocusChangeListener { _, temFoco ->
                if (!temFoco && faixa.maxFixo == null) {
                    val nMax = DecimalInput.parse(max.second.text.toString()) ?: faixa.max
                    salvar(store, br.com.gestordriver.core.FaixasClassificacao.aplicar(store.carregar(), faixa.campoMax, nMax))
                    remount()
                }
            }
        }
    }

    private fun montarApp(
        destino: LinearLayout,
        config: ConfiguracaoUsuario,
        store: br.com.gestordriver.data.ConfiguracaoStore,
        snapshot: OverlaySnapshot,
        context: Context,
    ) {
        destino.addView(rotulo(context, "PERMISSÕES"))
        destino.addView(linhaPermissao(context, "LOCALIZAÇÃO", PermissoesMonitoramento.localizacaoConcedida(context), snapshot.destacarPermissoes) {
            context.startActivity(
                Intent(context, MainActivity::class.java)
                    .putExtra(MainActivity.EXTRA_PEDIR_LOCALIZACAO, true)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            )
        })
        destino.addView(linhaPermissao(context, "NOTIFICAÇÕES", PermissoesMonitoramento.listenerNotificacoesAtivo(context), snapshot.destacarPermissoes) {
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        })
        destino.addView(linhaPermissao(context, "SOBRESSAIR", PermissoesMonitoramento.overlayConcedida(context), snapshot.destacarPermissoes) {
            context.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:${context.packageName}"),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        })
        destino.addView(rotulo(context, "VALORES COMBUSTÍVEL"))
        val precoG = campo(context, "GASOLINA", DecimalInput.formatar(config.precoGasolina))
        val precoE = campo(context, "ETANOL", DecimalInput.formatar(config.precoEtanol))
        destino.addView(linha(context, precoG.first, precoE.first))
        fun persistirPrecos() {
            val atual = store.carregar()
            salvar(
                store,
                atual.copy(
                    precoGasolina = DecimalInput.parse(precoG.second.text.toString()) ?: atual.precoGasolina,
                    precoEtanol = DecimalInput.parse(precoE.second.text.toString()) ?: atual.precoEtanol,
                ),
            )
        }
        precoG.second.setOnFocusChangeListener { _, temFoco -> if (!temFoco) persistirPrecos() }
        precoE.second.setOnFocusChangeListener { _, temFoco -> if (!temFoco) persistirPrecos() }
        destino.addView(rotulo(context, "APP DE NAVEGAÇÃO"))
        val maps = CheckBox(context).apply {
            text = "MAPS"
            setTextColor(Color.WHITE)
            isChecked = config.navegacao == AppNavegacao.GOOGLE_MAPS
        }
        val waze = CheckBox(context).apply {
            text = "WAZE"
            setTextColor(Color.WHITE)
            isChecked = config.navegacao == AppNavegacao.WAZE
        }
        maps.setOnCheckedChangeListener { _, marcado ->
            if (marcado) {
                waze.isChecked = false
                salvar(store, store.carregar().copy(navegacao = AppNavegacao.GOOGLE_MAPS))
            }
        }
        waze.setOnCheckedChangeListener { _, marcado ->
            if (marcado) {
                maps.isChecked = false
                salvar(store, store.carregar().copy(navegacao = AppNavegacao.WAZE))
            }
        }
        destino.addView(linha(context, maps, waze))
        destino.addView(
            TextView(context).apply {
                text = "Abrir app selecionado"
                setTextColor(Color.parseColor(AMARELO))
                textSize = 12f
                setPadding(0, dp(context, 8), 0, 0)
                setOnClickListener {
                    NavegacaoLauncher.abrirAplicativo(context, store.carregar().navegacao)
                }
            },
        )
        destino.addView(
            TextView(context).apply {
                text = "Nesta versão apenas abre o app. No Pro o trajeto da corrida aceita poderá ser visto no histórico."
                setTextColor(Color.parseColor(SECUNDARIO))
                textSize = 9f
                setPadding(0, dp(context, 6), 0, 0)
            },
        )
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
        conteudo.findViewWithTag<TextView>("perm_SOBRESSAIR")?.let {
            colorirPermissao(it, "SOBRESSAIR", overlay, snapshot.destacarPermissoes)
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
        view.text = if (ok) "$titulo  ✓" else "$titulo  — faltando"
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

    private fun campo(context: Context, label: String, valor: String): Pair<LinearLayout, EditText> {
        val bloco = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(context, 4), 0, dp(context, 4))
        }
        bloco.addView(rotulo(context, label))
        val campo = EditText(context).apply {
            setText(valor)
            setTextColor(Color.WHITE)
            textSize = 13f
            setHintTextColor(Color.parseColor(SECUNDARIO))
            setBackgroundColor(Color.parseColor("#33000000"))
            setPadding(dp(context, 8), dp(context, 6), dp(context, 8), dp(context, 6))
        }
        bloco.addView(campo)
        return bloco to campo
    }

    private fun rotulo(context: Context, texto: String): TextView =
        TextView(context).apply {
            text = texto
            setTextColor(Color.parseColor(SECUNDARIO))
            textSize = 10f
            setPadding(0, dp(context, 4), 0, dp(context, 2))
        }

    private fun linha(context: Context, esquerda: View, direita: View): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            esquerda.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            direita.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            addView(esquerda)
            addView(direita)
        }

    private fun salvar(store: br.com.gestordriver.data.ConfiguracaoStore, config: ConfiguracaoUsuario) {
        store.salvar(config)
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
