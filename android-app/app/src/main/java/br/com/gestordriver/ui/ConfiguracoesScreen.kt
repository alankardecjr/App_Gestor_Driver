package br.com.gestordriver.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestordriver.R
import br.com.gestordriver.core.AlertaOleo
import br.com.gestordriver.core.Classificacao
import br.com.gestordriver.core.ClassificacaoConstantes
import br.com.gestordriver.core.FaixasClassificacao
import br.com.gestordriver.core.CalcularCombustivel
import br.com.gestordriver.core.TabelaIpvaPlaca
import br.com.gestordriver.data.ContaVinculo
import br.com.gestordriver.model.AppNavegacao
import br.com.gestordriver.model.Combustivel
import br.com.gestordriver.model.ConfiguracaoUsuario
import br.com.gestordriver.model.PlanoAcesso
import br.com.gestordriver.model.TemaApp
import br.com.gestordriver.model.TipoContaVinculada
import br.com.gestordriver.model.TipoVeiculo
import br.com.gestordriver.permission.PermissoesMonitoramento
import br.com.gestordriver.ui.theme.LocalPaletaApp
import kotlin.math.abs
import kotlin.math.round
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private val TextoAmareloConfig = Color(0xFFFFD54F)
private val DestaqueSelecionado = Color(0xFF7CB342)
private val FormaPainel = RoundedCornerShape(16.dp)
private val FormaCaixa = RoundedCornerShape(12.dp)

private val FonteCampo = 14.sp
private val FonteValor = 15.sp
private val FonteTitulo = 16.sp
private val FonteAjuda = 12.sp
private val AlturaToque = 48.dp

@Composable
fun ConfiguracoesScreen(
    viewModel: ConfiguracoesViewModel,
    onVoltar: () -> Unit,
    abaInicial: Int = 0,
    destacarPermissoes: Boolean = false,
    plano: PlanoAcesso = PlanoAcesso.PRO,
    monitorando: Boolean = false,
    onAbaPersistida: (Int) -> Unit = {},
    onHistorico: () -> Unit = {},
    onCarteira: () -> Unit = {},
    onAtivarMonitoramento: () -> Unit = {},
    onDesativarMonitoramento: () -> Unit = {},
    onLocalizacao: () -> Unit = {},
    onFechar: () -> Unit = {},
    confirmacaoMonitorVisivel: Boolean = false,
    onCancelarMonitor: () -> Unit = {},
    onConfirmarMonitor: () -> Unit = {},
    onLiberarChave: (String) -> Boolean = { false },
) {
    val configuracao = viewModel.configuracao
    var aba by remember { mutableIntStateOf(if (abaInicial < 0) 0 else abaInicial + 1) }
    val rolagem = rememberScrollState()
    val foco = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current
    var dialogoGoogle by remember { mutableStateOf(false) }
    var dialogoEmail by remember { mutableStateOf(false) }
    var dialogoAbastecimento by remember { mutableStateOf(false) }
    var perguntarSalvar by remember { mutableStateOf(false) }
    var voltarDepois by remember { mutableStateOf(false) }
    LaunchedEffect(abaInicial) {
        aba = if (abaInicial < 0) 0 else abaInicial + 1
    }
    LaunchedEffect(aba) {
        rolagem.scrollTo(0)
        foco.clearFocus(force = true)
        teclado?.hide()
    }
    val paleta = LocalPaletaApp.current
    val contexto = LocalContext.current
    fun sairDoCampo() {
        foco.clearFocus(force = true)
        teclado?.hide()
    }
    fun avisar(texto: String) {
        android.widget.Toast.makeText(contexto, texto, android.widget.Toast.LENGTH_SHORT).show()
    }
    fun salvarEdicao(aplicarAbastecimento: Boolean, limparCalculadora: Boolean = false) {
        viewModel.salvar(aplicarAbastecimento = aplicarAbastecimento, limparCalculadora = limparCalculadora)
        sairDoCampo()
        avisar("Alteração salva.")
        if (voltarDepois) {
            voltarDepois = false
            onVoltar()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(paleta.fundo),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            val titulosPagina = listOf(
                "Gestor Driver",
                "Semáforo",
                "Despesas",
                "Usuário",
                "Sistema",
            )
            CabecalhoTela(
                titulo = titulosPagina.getOrElse(aba) { "Menu" },
                mostrarVoltar = aba != 0,
                inicio = if (aba == 0) {
                    { BotaoSelo(onClick = {}, clicavel = false) }
                } else {
                    null
                },
                acao = when (aba) {
                    1 -> {
                        {
                            BotaoCircular(
                                simbolo = "?",
                                onClick = {
                                    android.widget.Toast.makeText(
                                        contexto,
                                        "Arraste as marcas para calibrar. Toque numa marca para destacá-la e use − e + para o ajuste fino. Segure o botão para a marca continuar. A barra mostra ruim, boa e ótima do mesmo tamanho. Até a primeira é ruim, vermelho. Da primeira mais 0,01 até a segunda é boa, amarelo. Da segunda mais 0,01 é ótima, verde. R$/km vai de 0 a 4. R$/hora vai de 0 a 99. A borda da oferta fica com a pior cor. A nota vai de 3,00 a 5,00 e pinta só a nota. Salvar grava e permanece nesta tela.",
                                        android.widget.Toast.LENGTH_LONG,
                                    ).show()
                                },
                            )
                        }
                    }
                    2 -> {
                        {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BotaoCircular(
                                    simbolo = "🔢",
                                    onClick = { abrirCalculadora(contexto) },
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                BotaoCircular(
                                    simbolo = "?",
                                    onClick = {
                                        android.widget.Toast.makeText(
                                            contexto,
                                            "Combustível atual: ajuste o preço e o consumo do combustível marcado. Ele entra na oferta. Óleo e pneus são estimativa por km. IPVA é o valor anual, no mês do final da placa. Seguro é o valor mensal. A calculadora abre a do celular. Salvar grava e permanece nesta tela.",
                                            android.widget.Toast.LENGTH_LONG,
                                        ).show()
                                    },
                                )
                            }
                        }
                    }
                    3 -> {
                        {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BotaoCircular(
                                    simbolo = "🔢",
                                    onClick = { abrirCalculadora(contexto) },
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                BotaoCircular(
                                    simbolo = "?",
                                    onClick = {
                                        android.widget.Toast.makeText(
                                            contexto,
                                            "Seu veículo: carro ou moto, marca, modelo, versão, ano e o final da placa. O final define o mês do IPVA. Abastecimento calcula o preço e o consumo do combustível marcado em Despesas. A calculadora abre a do celular. Salvar grava e permanece nesta tela.",
                                            android.widget.Toast.LENGTH_LONG,
                                        ).show()
                                    },
                                )
                            }
                        }
                    }
                    4 -> {
                        {
                            BotaoCircular(
                                simbolo = "?",
                                onClick = {
                                    android.widget.Toast.makeText(
                                        contexto,
                                        "Permissões: toque para abrir o ajuste do celular. Apps de corrida mostra o que está instalado. Tema, mapa e conta entram ao voltar. A chave Pro libera esta instalação. Sem chave, o app fica Free. O voltar grava e retorna para Opções. Sobre envia o log.",
                                        android.widget.Toast.LENGTH_LONG,
                                    ).show()
                                },
                            )
                        }
                    }
                    else -> null
                },
                onVoltar = {
                    sairDoCampo()
                    when (aba) {
                        4 -> {
                            if (viewModel.temAlteracao()) {
                                viewModel.salvar(aplicarAbastecimento = false)
                                avisar("Alteração salva.")
                            }
                            onVoltar()
                        }
                        1, 2, 3 -> {
                            if (viewModel.temAlteracao()) {
                                perguntarSalvar = true
                            } else {
                                onVoltar()
                            }
                        }
                        else -> onVoltar()
                    }
                },
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(paleta.borda),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
                    .then(
                        if (aba == 0) {
                            Modifier
                        } else {
                            Modifier.barraRolagemAoToque(rolagem).verticalScroll(rolagem)
                        },
                    )
                    .padding(start = 12.dp, end = 16.dp, top = 10.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(if (aba == 0) 4.dp else 6.dp),
            ) {
                when (aba) {
                    0 -> AbaOpcoes(
                        monitorando = monitorando,
                        onHistorico = onHistorico,
                        onCarteira = onCarteira,
                        onDespesas = { aba = 2; onAbaPersistida(1) },
                        onSemaforo = { aba = 1; onAbaPersistida(0) },
                        onUsuario = { aba = 3; onAbaPersistida(2) },
                        onConfigurar = { aba = 4; onAbaPersistida(3) },
                        onMonitoramento = if (monitorando) onDesativarMonitoramento else onAtivarMonitoramento,
                        onLocalizacao = onLocalizacao,
                        onFechar = onFechar,
                    )
                    1 -> AbaClassificacao(viewModel)
                    2 -> AbaCustos(viewModel, plano)
                    3 -> AbaVeiculo(viewModel, plano)
                    else -> AbaApp(
                        viewModel = viewModel,
                        destacarPermissoes = destacarPermissoes,
                        plano = plano,
                        onGoogle = { dialogoGoogle = true },
                        onEmail = { dialogoEmail = true },
                        onLiberarChave = { texto ->
                            val ok = onLiberarChave(texto)
                            avisar(if (ok) "Versão Pro liberada." else "Chave inválida.")
                            ok
                        },
                    )
                }
            }

            if (aba in 1..3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(paleta.borda),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = {
                        sairDoCampo()
                        if (aba == 3) {
                            viewModel.descartarMantendoCalculadora()
                        } else {
                            viewModel.cancelar()
                        }
                        onVoltar()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .background(DestaqueSelecionado.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                ) {
                    Text(
                        text = "Cancelar",
                        color = paleta.textoSecundario,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                TextButton(
                    onClick = {
                        if (aba == 3 && viewModel.temCalculoAbastecimento()) {
                            dialogoAbastecimento = true
                        } else {
                            salvarEdicao(aplicarAbastecimento = false)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .background(DestaqueSelecionado.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                ) {
                    Text(
                        text = "Salvar",
                        color = DestaqueSelecionado,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            }
        }

        if (confirmacaoMonitorVisivel) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(enabled = false, onClick = {}),
                contentAlignment = Alignment.Center,
            ) {
                CaixaDialogo(
                    titulo = if (monitorando) "Desligar monitoramento" else "Ligar monitoramento",
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Text(
                        text = if (monitorando) {
                            "Desligar o monitoramento? O selo e o aviso somem. O app continua aberto."
                        } else {
                            "Ligar o monitoramento? O selo e o aviso aparecem."
                        },
                        color = LocalPaletaApp.current.textoSecundario,
                        fontSize = 13.sp,
                    )
                    BotoesMensagem(
                        textoEsquerda = "Cancelar",
                        onEsquerda = onCancelarMonitor,
                        textoDireita = if (monitorando) "Desligar" else "Ligar",
                        onDireita = onConfirmarMonitor,
                        direitaPerigo = monitorando,
                    )
                }
            }
        }
        if (dialogoGoogle) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(enabled = false, onClick = {}),
                contentAlignment = Alignment.Center,
            ) {
                DialogoContaGoogle(
                    emailAtual = if (configuracao.contaTipo == TipoContaVinculada.GOOGLE) {
                        configuracao.contaEmail
                    } else {
                        ""
                    },
                    onFechar = { dialogoGoogle = false },
                    onConectar = { email ->
                        viewModel.conectarContaGoogle(email)
                        dialogoGoogle = false
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
        if (dialogoEmail) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(enabled = false, onClick = {}),
                contentAlignment = Alignment.Center,
            ) {
                DialogoContaEmail(
                    emailAtual = if (configuracao.contaTipo == TipoContaVinculada.EMAIL) {
                        configuracao.contaEmail
                    } else {
                        ""
                    },
                    onFechar = { dialogoEmail = false },
                    onConectar = { email ->
                        if (viewModel.conectarContaEmail(email)) {
                            dialogoEmail = false
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
        if (perguntarSalvar) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(enabled = false, onClick = {}),
                contentAlignment = Alignment.Center,
            ) {
                CaixaDialogo(
                    titulo = "Salvar alterações?",
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Text(
                        text = "Deseja salvar as alterações antes de voltar?",
                        color = LocalPaletaApp.current.textoSecundario,
                        fontSize = 13.sp,
                    )
                    BotoesMensagem(
                        textoEsquerda = "Não",
                        onEsquerda = {
                            perguntarSalvar = false
                            viewModel.cancelar()
                            onVoltar()
                        },
                        textoDireita = "Sim",
                        onDireita = {
                            perguntarSalvar = false
                            voltarDepois = true
                            if (aba == 3 && viewModel.temCalculoAbastecimento()) {
                                dialogoAbastecimento = true
                            } else {
                                salvarEdicao(aplicarAbastecimento = false)
                            }
                        },
                    )
                }
            }
        }
        if (dialogoAbastecimento) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(enabled = false, onClick = {}),
                contentAlignment = Alignment.Center,
            ) {
                CaixaDialogo("Usar abastecimento?", Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Preencher R$/L e km/L do combustível atual com o cálculo do abastecimento?",
                        color = LocalPaletaApp.current.textoSecundario,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                    )
                    BotoesMensagem(
                        textoEsquerda = "Não",
                        onEsquerda = {
                            dialogoAbastecimento = false
                            salvarEdicao(aplicarAbastecimento = false, limparCalculadora = true)
                        },
                        textoDireita = "Sim",
                        onDireita = {
                            dialogoAbastecimento = false
                            salvarEdicao(aplicarAbastecimento = true, limparCalculadora = true)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AbaVeiculo(viewModel: ConfiguracoesViewModel, plano: PlanoAcesso) {
    val configuracao = viewModel.configuracao
    val paleta = LocalPaletaApp.current
    val travar = plano.travaCalculadora
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CartaoDespesa(
            titulo = "Veículo",
            subtitulo = "Marca, modelo e placa",
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OpcaoMarca(
                    texto = "Carro",
                    marcado = configuracao.tipoVeiculo == TipoVeiculo.CARRO,
                    onMarcar = { viewModel.atualizarTipoVeiculo(TipoVeiculo.CARRO) },
                )
                OpcaoMarca(
                    texto = "Moto",
                    marcado = configuracao.tipoVeiculo == TipoVeiculo.MOTO,
                    onMarcar = { viewModel.atualizarTipoVeiculo(TipoVeiculo.MOTO) },
                )
            }
            LinhaCampos {
                CampoCaixa("Marca", configuracao.marcaVeiculo, viewModel::atualizarMarca, Modifier.weight(1f))
                CampoCaixa("Modelo", configuracao.modeloVeiculo, viewModel::atualizarModelo, Modifier.weight(1f))
            }
            LinhaCampos {
                CampoCaixa("Versão", configuracao.versaoVeiculo, viewModel::atualizarVersao, Modifier.weight(1f))
                CampoCaixa("Ano", configuracao.anoVeiculo, viewModel::atualizarAno, Modifier.weight(1f))
            }
            CampoCaixa(
                "Final da placa",
                configuracao.finalPlaca,
                viewModel::atualizarFinalPlaca,
                Modifier.fillMaxWidth(),
            )
            Text(
                text = TabelaIpvaPlaca.textoVencimento(configuracao.finalPlaca),
                color = paleta.texto,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        CartaoDespesa(
            titulo = "Abastecimento",
            subtitulo = if (travar) "Disponível no Pro" else "Calcular preço e consumo",
        ) {
            val energia = configuracao.combustivel == Combustivel.ENERGIA
            LinhaCampos {
                CampoNumericoCaixa("Valor R$", configuracao.abastecimentoValor, viewModel::atualizarAbastecimentoValor, Modifier.weight(1f), bloqueado = travar)
                CampoNumericoCaixa(
                    if (energia) "kWh" else "Litros",
                    configuracao.abastecimentoLitros,
                    viewModel::atualizarAbastecimentoLitros,
                    Modifier.weight(1f),
                    bloqueado = travar,
                )
            }
            LinhaCampos {
                CampoNumericoCaixa("Km inicial", configuracao.abastecimentoKmInicial, viewModel::atualizarAbastecimentoKmInicial, Modifier.weight(1f), bloqueado = travar)
                CampoNumericoCaixa("Km final", configuracao.abastecimentoKmFinal, viewModel::atualizarAbastecimentoKmFinal, Modifier.weight(1f), bloqueado = travar)
            }
            Text(
                text = textoCalculo(
                    if (energia) "R$/kWh" else "R$/L",
                    CalcularCombustivel.precoPorLitro(configuracao.abastecimentoValor, configuracao.abastecimentoLitros),
                ),
                color = paleta.texto,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = textoCalculo(
                    if (energia) "km/kWh" else "km/L",
                    CalcularCombustivel.consumoKmPorLitro(
                        configuracao.abastecimentoKmInicial,
                        configuracao.abastecimentoKmFinal,
                        configuracao.abastecimentoLitros,
                    ),
                ),
                color = paleta.texto,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun abrirCalculadora(contexto: android.content.Context) {
    val pm = contexto.packageManager
    val categoria = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
        addCategory(android.content.Intent.CATEGORY_APP_CALCULATOR)
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val encontrada = pm.queryIntentActivities(categoria, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
        .firstOrNull()
    if (encontrada != null) {
        categoria.component = android.content.ComponentName(
            encontrada.activityInfo.packageName,
            encontrada.activityInfo.name,
        )
        if (runCatching { contexto.startActivity(categoria) }.isSuccess) {
            return
        }
    }
    val alternativas = listOf(
        "com.sec.android.app.popupcalculator" to "com.sec.android.app.popupcalculator.Calculator",
        "com.samsung.android.calculator" to "com.samsung.android.calculator.Calculator",
        "com.google.android.calculator" to "com.android.calculator2.Calculator",
        "com.android.calculator2" to "com.android.calculator2.Calculator",
    )
    for ((pacote, classe) in alternativas) {
        val especifico = android.content.Intent().apply {
            component = android.content.ComponentName(pacote, classe)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (runCatching { contexto.startActivity(especifico) }.isSuccess) {
            return
        }
        val launcher = pm.getLaunchIntentForPackage(pacote) ?: continue
        if (runCatching {
                contexto.startActivity(launcher.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
            }.isSuccess
        ) {
            return
        }
    }
    android.widget.Toast.makeText(
        contexto,
        "Calculadora não encontrada neste celular.",
        android.widget.Toast.LENGTH_SHORT,
    ).show()
}

@Composable
private fun AbaCustos(viewModel: ConfiguracoesViewModel, plano: PlanoAcesso) {
    val configuracao = viewModel.configuracao
    val paleta = LocalPaletaApp.current
    val travar = plano.travaCalculadora
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    CartaoDespesa(
        titulo = "Combustível atual",
        subtitulo = "Ajustar preço e consumo",
        ajuda = "Do combustível marcado. Gasolina e etanol em km/L. Energia em km/kWh, com 12% de perda na recarga. Os dois entram no gasto da oferta.",
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OpcaoMarca(
                texto = "Gasolina",
                marcado = configuracao.combustivel == Combustivel.GASOLINA,
                onMarcar = { viewModel.selecionarCombustivel(Combustivel.GASOLINA) },
            )
            OpcaoMarca(
                texto = "Etanol",
                marcado = configuracao.combustivel == Combustivel.ETANOL,
                onMarcar = { viewModel.selecionarCombustivel(Combustivel.ETANOL) },
            )
            OpcaoMarca(
                texto = "Energia",
                marcado = configuracao.combustivel == Combustivel.ENERGIA,
                onMarcar = { viewModel.selecionarCombustivel(Combustivel.ENERGIA) },
            )
        }
        when (configuracao.combustivel) {
            Combustivel.GASOLINA -> LinhaCampos {
                CampoNumericoCaixa("R$ / L", configuracao.precoGasolina, viewModel::atualizarPrecoGasolina, Modifier.weight(1f))
                CampoNumericoCaixa("km / L", configuracao.consumoGasolina, viewModel::atualizarConsumoGasolina, Modifier.weight(1f))
            }
            Combustivel.ETANOL -> LinhaCampos {
                CampoNumericoCaixa("R$ / L", configuracao.precoEtanol, viewModel::atualizarPrecoEtanol, Modifier.weight(1f))
                CampoNumericoCaixa("km / L", configuracao.consumoEtanol, viewModel::atualizarConsumoEtanol, Modifier.weight(1f))
            }
            Combustivel.ENERGIA -> LinhaCampos {
                CampoNumericoCaixa("R$ / kWh", configuracao.precoEnergia, viewModel::atualizarPrecoEnergia, Modifier.weight(1f))
                CampoNumericoCaixa("km / kWh", configuracao.consumoEnergia, viewModel::atualizarConsumoEnergia, Modifier.weight(1f))
            }
        }
        if (!configuracao.calculadoraCombustivelPronta()) {
            Text(
                text = "Consumo ou preço 0: litros, gasto e lucro da oferta ficam sem valor até preencher.",
                color = paleta.textoSecundario,
                fontSize = 11.sp,
            )
        }
    }
    CartaoDespesa(
        titulo = "Óleo",
        subtitulo = if (travar) "Disponível no Pro" else "Estimativa por km",
        ajuda = "Valor da troca e a cada quantos km. O dashboard mostra esse rateio. Não soma de novo na despesa do período.",
    ) {
        LinhaCampos {
            CampoNumericoCaixa("Valor R$", configuracao.oleoValor, viewModel::atualizarOleoValor, Modifier.weight(1f), bloqueado = travar)
            CampoNumericoCaixa("Km", configuracao.oleoKilometragem, viewModel::atualizarOleoKm, Modifier.weight(1f), bloqueado = travar)
            CampoCaixa("Data", configuracao.oleoData, viewModel::atualizarOleoData, Modifier.weight(1f), bloqueado = travar)
        }
        AlertaOleoUi(configuracao)
        ResultadoReaisPorKm(configuracao.oleoValor, configuracao.oleoKilometragem)
    }
    CartaoDespesa(
        titulo = "Pneus",
        subtitulo = if (travar) "Disponível no Pro" else "Estimativa por km",
        ajuda = "Valor e km do dianteiro e do traseiro. O dashboard mostra a estimativa. Não entra de novo na despesa do período.",
    ) {
        Text("Dianteiro", color = paleta.texto, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        LinhaCampos {
            CampoNumericoCaixa("Valor R$", configuracao.pneuDianteiroValor, viewModel::atualizarPneuDianteiroValor, Modifier.weight(1f), bloqueado = travar)
            CampoNumericoCaixa("Km", configuracao.pneuDianteiroRodagem, viewModel::atualizarPneuDianteiroRodagem, Modifier.weight(1f), bloqueado = travar)
            CampoCaixa("Data", configuracao.pneuDianteiroData, viewModel::atualizarPneuDianteiroData, Modifier.weight(1f), bloqueado = travar)
        }
        ResultadoReaisPorKm(configuracao.pneuDianteiroValor, configuracao.pneuDianteiroRodagem)
        Text("Traseiro", color = paleta.texto, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        LinhaCampos {
            CampoNumericoCaixa("Valor R$", configuracao.pneuTraseiroValor, viewModel::atualizarPneuTraseiroValor, Modifier.weight(1f), bloqueado = travar)
            CampoNumericoCaixa("Km", configuracao.pneuTraseiroRodagem, viewModel::atualizarPneuTraseiroRodagem, Modifier.weight(1f), bloqueado = travar)
            CampoCaixa("Data", configuracao.pneuTraseiroData, viewModel::atualizarPneuTraseiroData, Modifier.weight(1f), bloqueado = travar)
        }
        ResultadoReaisPorKm(configuracao.pneuTraseiroValor, configuracao.pneuTraseiroRodagem)
    }
    CartaoDespesa(
        titulo = "IPVA",
        subtitulo = if (travar) "Disponível no Pro" else "Valor anual",
        ajuda = "Valor do ano. O vencimento já vem do final da placa, na aba Usuário. No dashboard, o mês leva 1/12 e o ano leva o valor inteiro.",
    ) {
        CampoNumericoCaixa(
            label = "Valor R$",
            valor = configuracao.ipvaValor,
            onValorChange = viewModel::atualizarIpvaValor,
            modifier = Modifier.fillMaxWidth(),
            bloqueado = travar,
        )
        Text(
            text = TabelaIpvaPlaca.textoVencimento(configuracao.finalPlaca),
            color = paleta.textoSecundario,
            fontSize = 11.sp,
        )
    }
    CartaoDespesa(
        titulo = "Seguro",
        subtitulo = if (travar) "Disponível no Pro" else "Valor mensal",
        ajuda = "Valor de cada mês. A data de vencimento serve para controlar o pagamento. No dashboard, o mês leva o valor inteiro e o ano leva doze vezes.",
    ) {
        LinhaCampos {
            CampoNumericoCaixa(
                "Valor R$",
                configuracao.seguroValor,
                viewModel::atualizarSeguroValor,
                Modifier.weight(1f),
                bloqueado = travar,
            )
            CampoCaixa(
                "Vencimento",
                configuracao.seguroData,
                viewModel::atualizarSeguroData,
                Modifier.weight(1f),
                bloqueado = travar,
            )
        }
    }
    }
}

@Composable
private fun ResultadoReaisPorKm(valor: Double, km: Double) {
    val paleta = LocalPaletaApp.current
    Text(
        text = textoReaisPorKm(valor, km),
        color = paleta.texto,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

private fun textoCalculo(rotulo: String, valor: Double?): String {
    if (valor == null || !valor.isFinite()) {
        return "$rotulo  —"
    }
    return "$rotulo  ${DecimalInput.formatarFixo(valor)}"
}

private fun textoReaisPorKm(valor: Double, km: Double): String {
    if (valor <= 0.0 || km <= 0.0) {
        return "R$/km  —"
    }
    val porKm = valor / km
    if (!porKm.isFinite()) {
        return "R$/km  —"
    }
    return "R$/km  ${DecimalInput.formatarFixo(porKm)}"
}

@Composable
private fun CartaoDespesa(
    titulo: String,
    subtitulo: String,
    ajuda: String? = null,
    conteudo: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val paleta = LocalPaletaApp.current
    val contexto = LocalContext.current
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(paleta.fundoPainel, forma)
            .border(1.dp, paleta.borda, forma)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    color = paleta.texto,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitulo,
                    color = paleta.textoSecundario,
                    fontSize = 11.sp,
                )
            }
            if (ajuda != null) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(paleta.pocoIcone, CircleShape)
                        .clickable {
                            android.widget.Toast.makeText(contexto, ajuda, android.widget.Toast.LENGTH_LONG).show()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "?", color = paleta.texto, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        conteudo()
    }
}

@Composable
private fun AbaOpcoes(
    monitorando: Boolean,
    onHistorico: () -> Unit,
    onCarteira: () -> Unit,
    onDespesas: () -> Unit,
    onSemaforo: () -> Unit,
    onUsuario: () -> Unit,
    onConfigurar: () -> Unit,
    onMonitoramento: () -> Unit,
    onLocalizacao: () -> Unit,
    onFechar: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val linha = Modifier.weight(1f)
        LinhaOpcao(
            R.drawable.ic_menu_monitorar,
            if (monitorando) "Monitorar (ON)" else "Monitorar (Off)",
            "Calculadora de ganhos",
            onMonitoramento,
            ligado = monitorando,
            modifier = linha,
        )
        LinhaOpcao(
            R.drawable.ic_menu_localizacao,
            "Localização",
            "Localização no mapa",
            onLocalizacao,
            modifier = linha,
        )
        LinhaOpcao(
            R.drawable.ic_menu_historico,
            "Histórico",
            "Ver corridas aceitas",
            onHistorico,
            modifier = linha,
        )
        LinhaOpcao(
            R.drawable.ic_menu_carteira,
            "Dashboard",
            "Saldo e movimentações",
            onCarteira,
            modifier = linha,
        )
        LinhaOpcao(
            R.drawable.ic_menu_despesas,
            "Despesas",
            "Controle de gastos",
            onDespesas,
            modifier = linha,
        )
        LinhaOpcao(
            R.drawable.ic_menu_semaforo,
            "Semáforo",
            "Calibrar calculadora",
            onSemaforo,
            modifier = linha,
        )
        LinhaOpcao(
            R.drawable.ic_menu_usuario,
            "Usuário",
            "Informações do veiculo",
            onUsuario,
            modifier = linha,
        )
        LinhaOpcao(
            R.drawable.ic_menu_sistema,
            "Sistema",
            "Configurar o sistema",
            onConfigurar,
            modifier = linha,
        )
        LinhaOpcao(
            R.drawable.ic_menu_fechar,
            "Fechar",
            "Encerrar aplicativo",
            onFechar,
            perigo = true,
            modifier = linha,
        )
    }
}

@Composable
private fun LinhaOpcao(
    icone: Int,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit,
    perigo: Boolean = false,
    ligado: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val paleta = LocalPaletaApp.current
    val verde = Color(0xFF2E7D32)
    val cor = if (perigo) Color(0xFFE53935) else paleta.texto
    val fundoLinha = if (ligado) verde.copy(alpha = 0.20f) else paleta.fundoPainel
    val borda = if (ligado) verde.copy(alpha = 0.45f) else paleta.borda
    val fundoIcone = when {
        perigo -> Color(0x33E53935)
        ligado -> verde.copy(alpha = 0.28f)
        else -> paleta.pocoIcone
    }
    val tintaIcone = when {
        perigo -> cor
        ligado -> verde
        else -> paleta.textoSecundario
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .border(1.dp, borda, RoundedCornerShape(16.dp))
            .background(fundoLinha, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(fundoIcone, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icone),
                contentDescription = null,
                tint = tintaIcone,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
        ) {
            Text(
                text = titulo,
                color = cor,
                fontSize = if (ligado) 12.sp else 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(text = subtitulo, color = paleta.textoSecundario, fontSize = 10.sp, maxLines = 1)
        }
        Text(text = "›", color = paleta.textoSecundario, fontSize = 18.sp)
    }
}

@Composable
private fun AbaClassificacao(viewModel: ConfiguracoesViewModel) {
    val configuracao = viewModel.configuracao
    val paleta = LocalPaletaApp.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = "Calibrar classificações",
                color = paleta.texto,
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Arraste as marcações para ajustar",
                color = paleta.textoSecundario,
                fontSize = 12.sp,
                lineHeight = 15.sp,
            )
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                LegendaFaixa(Color(0xFFC62828), "Ruim = abaixo da média")
                LegendaFaixa(Color(0xFFF9A825), "Boa = na média")
                LegendaFaixa(Color(0xFF2E7D32), "Ótima = acima da média")
            }
        }
        ReguaDuasMarcas(
            titulo = "Ganhos por Km",
            ajuda = "A barra divide ruim, boa e ótima em três partes iguais. Até a marca de baixo é ruim. Da marca de baixo mais R$ 0,01 até a de cima é boa. Acima da de cima é ótima. A marca vai de 0 a 4. Toque na marca e use − e +. Segure o botão para a marca continuar. Entra na borda da oferta.",
            ruim = configuracao.limiteRuimMax,
            boa = configuracao.limiteBoaMax,
            ate = 4f,
            escalaInicio = "Ruim",
            escalaFim = "Bom",
            rotulo = { "R$ ${FaixasClassificacao.formatar(it)}/km" },
            onMarcas = viewModel::atualizarMarcasDeslizantes,
        )
        ReguaDuasMarcas(
            titulo = "Ganhos por Hora",
            ajuda = "A barra divide ruim, boa e ótima em três partes iguais. Até a marca de baixo é ruim. Da marca de baixo mais R$ 0,01 até a de cima é boa. Acima da de cima é ótima. A marca vai de 0 a 99. Toque na marca e use − e +. Segure o botão para a marca continuar. Entra na borda da oferta, com a pior cor entre km e hora.",
            ruim = configuracao.marcaHoraRuim,
            boa = configuracao.marcaHoraBoa,
            ate = 99f,
            escalaInicio = "Ruim",
            escalaFim = "Bom",
            rotulo = { "R$ ${FaixasClassificacao.formatar(it)}/h" },
            onMarcas = viewModel::atualizarMarcasHora,
        )
        ReguaDuasMarcas(
            titulo = "Nota do passageiro",
            ajuda = "A barra divide ruim, boa e ótima em três partes iguais. Até a marca de baixo é ruim. Da marca de baixo mais 0,01 até a de cima é boa. Acima da de cima é ótima. A marca vai de 3,00 a 5,00. Toque na marca e use − e +. Segure o botão para a marca continuar. Pinta só a nota.",
            ruim = configuracao.marcaNotaRuim,
            boa = configuracao.marcaNotaBoa,
            desde = 3f,
            ate = 5f,
            escalaInicio = "Ruim",
            escalaFim = "Bom",
            rotulo = { FaixasClassificacao.formatar(it) },
            onMarcas = viewModel::atualizarMarcasNota,
        )
    }
}

@Composable
private fun LegendaFaixa(cor: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(cor, CircleShape),
        )
        Text(
            text = texto,
            color = LocalPaletaApp.current.texto,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

@Composable
private fun ReguaDuasMarcas(
    titulo: String,
    ajuda: String,
    ruim: Double,
    boa: Double,
    ate: Float,
    rotulo: (Double) -> String,
    onMarcas: (Double, Double) -> Unit,
    desde: Float = 0f,
    escalaInicio: String = "Ruim",
    escalaFim: String = "Bom",
    passo: Float = 0.01f,
) {
    val paleta = LocalPaletaApp.current
    val contexto = LocalContext.current
    val forma = RoundedCornerShape(16.dp)
    val vermelho = Color(0xFFC62828)
    val amarelo = Color(0xFFF9A825)
    val verde = Color(0xFF2E7D32)
    val ruimAtual by rememberUpdatedState(ruim.toFloat().coerceIn(desde, ate))
    val boaAtual by rememberUpdatedState(boa.toFloat().coerceIn(desde, ate))
    val aoMudar by rememberUpdatedState(onMarcas)
    var marcaAtiva by remember { mutableIntStateOf(1) }
    val marcaAtivaAtual by rememberUpdatedState(marcaAtiva)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(paleta.fundoPainel, forma)
            .border(1.dp, paleta.borda, forma)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = titulo,
                color = paleta.texto,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(paleta.pocoIcone, CircleShape)
                    .clickable {
                        android.widget.Toast.makeText(contexto, ajuda, android.widget.Toast.LENGTH_LONG).show()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "?", color = paleta.texto, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
        ) {
            val margem = 36.dp
            val botao = 28.dp
            val raio = 9.dp
            val faixa = (ate - desde).coerceAtLeast(passo)
            val meio = maxWidth - margem * 2 - botao * 2
            val util = (meio - raio * 2).coerceAtLeast(1.dp)
            // Ruim, boa e ótima ocupam um terço cada. O número da marca
            // muda com −/+, arraste ou toque; a largura da cor não muda.
            val fracRuim = 1f / 3f
            val fracBoa = 2f / 3f
            val posRuim = util * fracRuim
            val posBoa = util * fracBoa
            fun centroDe(posicao: Dp): Dp = margem + botao + raio + posicao
            fun centavos(valor: Double): Double = round(valor * 100.0) / 100.0
            fun moverMarca(ruimCursor: Double, boaCursor: Double, repeticoes: Int, sinal: Double): Pair<Double, Double> {
                val base = passo.toDouble().coerceAtLeast(0.01)
                val amplo = ate - desde > 10f
                val mult = when {
                    repeticoes < 12 -> 1.0
                    repeticoes < 28 -> 5.0
                    amplo && repeticoes >= 40 -> 50.0
                    amplo -> 10.0
                    else -> 5.0
                }
                val delta = sinal * base * mult
                val folga = base
                val novo = if (marcaAtivaAtual == 0) {
                    val limite = (boaCursor - folga).coerceAtLeast(desde.toDouble())
                    centavos((ruimCursor + delta).coerceIn(desde.toDouble(), limite)) to boaCursor
                } else {
                    val limite = (ruimCursor + folga).coerceAtMost(ate.toDouble())
                    ruimCursor to centavos((boaCursor + delta).coerceIn(limite, ate.toDouble()))
                }
                if (abs(novo.first - ruimCursor) > 0.0001 || abs(novo.second - boaCursor) > 0.0001) {
                    aoMudar(novo.first, novo.second)
                }
                return novo
            }
            Column {
                Box(modifier = Modifier.fillMaxWidth().height(30.dp)) {
                    MarcaAlinhada(centroDe(posBoa)) {
                        BolhaMarca(rotulo(boaAtual.toDouble()), verde)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = escalaInicio,
                        color = paleta.textoSecundario,
                        fontSize = 12.sp,
                        modifier = Modifier.width(margem),
                    )
                    BotaoAjusteContinuo(
                        simbolo = "−",
                        tamanho = botao,
                        fundo = paleta.pocoIcone,
                        texto = paleta.texto,
                        aoIniciar = { ruimAtual.toDouble() to boaAtual.toDouble() },
                        aoTick = { ruimCursor, boaCursor, repeticoes ->
                            moverMarca(ruimCursor, boaCursor, repeticoes, -1.0)
                        },
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .pointerInput(ate, desde) {
                                fun aplicar(inicio: Boolean, valor: Float) {
                                    val piso = ruimAtual
                                    val teto = boaAtual
                                    val folga = passo.coerceAtLeast(0.01f)
                                    if (inicio) {
                                        val limite = (teto - folga).coerceAtLeast(desde)
                                        aoMudar(valor.coerceIn(desde, limite).toDouble(), teto.toDouble())
                                    } else {
                                        val limite = (piso + folga).coerceAtMost(ate)
                                        aoMudar(piso.toDouble(), valor.coerceIn(limite, ate).toDouble())
                                    }
                                }
                                awaitEachGesture {
                                    val down = awaitFirstDown()
                                    val raioPx = raio.toPx()
                                    val utilPx = (size.width - raioPx * 2f).coerceAtLeast(1f)
                                    val fracao = ((down.position.x - raioPx) / utilPx).coerceIn(0f, 1f)
                                    val inicio = abs(fracao - (1f / 3f)) <= abs(fracao - (2f / 3f))
                                    marcaAtiva = if (inicio) 0 else 1
                                    val base = if (inicio) ruimAtual else boaAtual
                                    val x0 = down.position.x
                                    drag(down.id) { change ->
                                        val delta = (change.position.x - x0) / utilPx
                                        val novo = round((base + delta * faixa) * 100f) / 100f
                                        aplicar(inicio, novo)
                                    }
                                }
                            },
                    ) {
                        Canvas(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth()
                                .height(12.dp),
                        ) {
                            val raioPx = 9.dp.toPx()
                            val esquerda = raioPx
                            val direita = size.width - raioPx
                            val largura = (direita - esquerda).coerceAtLeast(1f)
                            val altura = size.height
                            val xRuim = esquerda + largura * fracRuim
                            val xBoa = esquerda + largura * fracBoa
                            val trilha = Path().apply {
                                addRoundRect(
                                    RoundRect(
                                        left = esquerda,
                                        top = 0f,
                                        right = direita,
                                        bottom = altura,
                                        cornerRadius = CornerRadius(altura / 2f, altura / 2f),
                                    ),
                                )
                            }
                            drawContext.canvas.save()
                            drawContext.canvas.clipPath(trilha)
                            if (boaAtual <= 0f) {
                                drawRect(paleta.borda, Offset(esquerda, 0f), Size(direita - esquerda, altura))
                            } else {
                                if (xRuim - esquerda > 0.5f) {
                                    drawRect(vermelho, Offset(esquerda, 0f), Size(xRuim - esquerda, altura))
                                }
                                if (xBoa - xRuim > 0.5f) {
                                    drawRect(amarelo, Offset(xRuim, 0f), Size(xBoa - xRuim, altura))
                                }
                                if (direita - xBoa > 0.5f) {
                                    drawRect(verde, Offset(xBoa, 0f), Size(direita - xBoa, altura))
                                }
                            }
                            drawContext.canvas.restore()
                        }
                        if (marcaAtiva == 0) {
                            PoloMarca(posBoa, paleta.borda, false)
                            PoloMarca(posRuim, vermelho, true)
                        } else {
                            PoloMarca(posRuim, paleta.borda, false)
                            PoloMarca(posBoa, verde, true)
                        }
                    }
                    BotaoAjusteContinuo(
                        simbolo = "+",
                        tamanho = botao,
                        fundo = paleta.pocoIcone,
                        texto = paleta.texto,
                        aoIniciar = { ruimAtual.toDouble() to boaAtual.toDouble() },
                        aoTick = { ruimCursor, boaCursor, repeticoes ->
                            moverMarca(ruimCursor, boaCursor, repeticoes, 1.0)
                        },
                    )
                    Text(
                        text = escalaFim,
                        color = paleta.textoSecundario,
                        fontSize = 12.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(margem),
                    )
                }
                Box(modifier = Modifier.fillMaxWidth().height(30.dp)) {
                    MarcaAlinhada(centroDe(posRuim)) {
                        BolhaMarca(rotulo(ruimAtual.toDouble()), vermelho)
                    }
                }
            }
        }
    }
}

@Composable
private fun BotaoAjusteContinuo(
    simbolo: String,
    tamanho: Dp,
    fundo: Color,
    texto: Color,
    aoIniciar: () -> Pair<Double, Double>,
    aoTick: (Double, Double, Int) -> Pair<Double, Double>,
) {
    val iniciar by rememberUpdatedState(aoIniciar)
    val tick by rememberUpdatedState(aoTick)
    Box(
        modifier = Modifier
            .size(tamanho)
            .background(fundo, CircleShape)
            .pointerInput(Unit) {
                coroutineScope {
                    val escopo = this
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        var cursor = iniciar()
                        val job = escopo.launch {
                            var repeticoes = 0
                            while (isActive) {
                                val novo = tick(cursor.first, cursor.second, repeticoes)
                                val mudou = abs(novo.first - cursor.first) > 0.0001 ||
                                    abs(novo.second - cursor.second) > 0.0001
                                cursor = novo
                                if (!mudou) {
                                    break
                                }
                                repeticoes++
                                delay(
                                    when {
                                        repeticoes == 1 -> 420L
                                        repeticoes < 10 -> 85L
                                        repeticoes < 24 -> 50L
                                        else -> 32L
                                    },
                                )
                            }
                        }
                        try {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) {
                                    change.consume()
                                    break
                                }
                                change.consume()
                            }
                        } finally {
                            job.cancel()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = simbolo, color = texto, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BoxScope.PoloMarca(deslocamento: Dp, borda: Color, destaque: Boolean) {
    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .offset(x = deslocamento - 5.dp)
            .size(28.dp)
            .border(if (destaque) 3.dp else 1.dp, borda, CircleShape)
            .background(Color.White, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "↔", color = Color(0xFF607D8B), fontSize = 12.sp)
    }
}

@Composable
private fun BolhaMarca(texto: String, cor: Color) {
    Text(
        text = texto,
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(cor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
private fun MarcaAlinhada(centro: Dp, conteudo: @Composable () -> Unit) {
    Layout(content = conteudo, modifier = Modifier.fillMaxWidth()) { measurables, constraints ->
        val placeable = measurables.first().measure(
            constraints.copy(minWidth = 0, minHeight = 0),
        )
        val x = (centro.roundToPx() - placeable.width / 2)
            .coerceIn(0, (constraints.maxWidth - placeable.width).coerceAtLeast(0))
        layout(constraints.maxWidth, placeable.height) {
            placeable.place(x, 0)
        }
    }
}

@Composable
private fun AbaApp(
    viewModel: ConfiguracoesViewModel,
    destacarPermissoes: Boolean,
    plano: PlanoAcesso,
    onGoogle: () -> Unit,
    onEmail: () -> Unit,
    onLiberarChave: (String) -> Boolean,
) {
    val contexto = LocalContext.current
    val configuracao = viewModel.configuracao
    val overlayOk = PermissoesMonitoramento.overlayConcedida(contexto)
    val listenerOk = PermissoesMonitoramento.listenerNotificacoesAtivo(contexto)
    val leituraOk = PermissoesMonitoramento.acessibilidadeAtiva(contexto)
    val bateriaOk = PermissoesMonitoramento.bateriaLiberada(contexto)
    val localizacaoOk = PermissoesMonitoramento.localizacaoConcedida(contexto)
    val paleta = LocalPaletaApp.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CartaoDespesa(
            titulo = "Permissões",
            subtitulo = "Toque para abrir o ajuste",
        ) {
            StatusToque(
                titulo = "Notificações",
                dica = "Lê as ofertas da Uber e da 99",
                ok = listenerOk,
                destacar = destacarPermissoes && !listenerOk,
                onClick = { PermissoesMonitoramento.abrirNotificacoes(contexto) },
            )
            StatusToque(
                titulo = "Sobrepor",
                dica = "Mostra o card sobre o mapa",
                ok = overlayOk,
                destacar = destacarPermissoes && !overlayOk,
                onClick = { contexto.startActivity(PermissoesMonitoramento.intentSobrepor(contexto)) },
            )
            StatusToque(
                titulo = "Acessibilidade",
                dica = "Liga só com o Monitorar. Desligada, o banco abre",
                ok = leituraOk,
                destacar = destacarPermissoes && !leituraOk,
                onClick = {
                    if (!br.com.gestordriver.notification.SessaoMonitoramento.ligada(contexto)) {
                        return@StatusToque
                    }
                    br.com.gestordriver.overlay.OverlayBridge.segurarAcessibilidade()
                    PermissoesMonitoramento.abrirAcessibilidade(contexto)
                },
            )
            StatusToque(
                titulo = "Bateria",
                dica = "Evita o aviso sumir no segundo plano",
                ok = bateriaOk,
                destacar = destacarPermissoes && !bateriaOk,
                onClick = { contexto.startActivity(PermissoesMonitoramento.intentBateria(contexto)) },
            )
            StatusToque(
                titulo = "Localização",
                dica = "Opcional. Abre o mapa na posição atual",
                ok = localizacaoOk,
                destacar = false,
                onClick = { (contexto as? br.com.gestordriver.MainActivity)?.pedirLocalizacao() },
            )
        }
        CartaoDespesa(
            titulo = "Apps de corrida",
            subtitulo = "Instalados neste celular",
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    br.com.gestordriver.notification.Plataforma.UBER to "Uber",
                    br.com.gestordriver.notification.Plataforma.NOVE_NOVE to "99",
                    br.com.gestordriver.notification.Plataforma.INDRIVE to "inDrive",
                ).forEach { (plataforma, titulo) ->
                    val ok = br.com.gestordriver.notification.PlataformasMotorista.instalada(contexto, plataforma)
                    StatusToque(
                        titulo = titulo,
                        dica = if (ok) "Instalado" else "Não instalado",
                        ok = ok,
                        destacar = false,
                        onClick = {},
                        clicavel = false,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        CartaoDespesa(
            titulo = "Tema",
            subtitulo = "Escuro, claro ou do celular",
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OpcaoMarca("Escuro", configuracao.tema == TemaApp.ESCURO) {
                    viewModel.selecionarTema(TemaApp.ESCURO)
                }
                OpcaoMarca("Claro", configuracao.tema == TemaApp.CLARO) {
                    viewModel.selecionarTema(TemaApp.CLARO)
                }
                OpcaoMarca("Celular", configuracao.tema == TemaApp.CELULAR) {
                    viewModel.selecionarTema(TemaApp.CELULAR)
                }
            }
        }
        CartaoDespesa(
            titulo = "Navegação",
            subtitulo = "Mapa do embarque e do destino",
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OpcaoMarca("Google Maps", configuracao.navegacao == AppNavegacao.GOOGLE_MAPS) {
                    viewModel.selecionarNavegacao(AppNavegacao.GOOGLE_MAPS)
                }
                OpcaoMarca("Waze", configuracao.navegacao == AppNavegacao.WAZE) {
                    viewModel.selecionarNavegacao(AppNavegacao.WAZE)
                }
            }
        }
        CartaoDespesa(
            titulo = "Versão Pro",
            subtitulo = if (plano.ehPro) "Liberada neste celular" else "Digite a chave de liberação",
        ) {
            if (plano.ehPro) {
                Text(
                    text = "Pro ativo",
                    color = paleta.texto,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            } else {
                var chave by remember { mutableStateOf("") }
                CampoCaixa("Chave", chave, { chave = it }, Modifier.fillMaxWidth())
                Text(
                    text = "Liberar",
                    color = DestaqueSelecionado,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onLiberarChave(chave) },
                )
            }
        }
        CartaoDespesa(
            titulo = "Conta",
            subtitulo = "Identifica o motorista",
        ) {
            val googleOk = configuracao.contaTipo == TipoContaVinculada.GOOGLE
            val emailOk = configuracao.contaTipo == TipoContaVinculada.EMAIL
            StatusToque(
                titulo = "Google",
                dica = if (googleOk) configuracao.contaEmail.ifBlank { "Conectado" } else "Toque para conectar",
                ok = googleOk,
                destacar = false,
                onClick = onGoogle,
            )
            StatusToque(
                titulo = "E-mail",
                dica = if (emailOk) configuracao.contaEmail.ifBlank { "Conectado" } else "Toque para conectar",
                ok = emailOk,
                destacar = false,
                onClick = onEmail,
            )
        }
        CartaoDespesa(
            titulo = "Sobre",
            subtitulo = "v${PermissoesMonitoramento.versaoApp(contexto)}",
        ) {
            Text(
                text = "Enviar log",
                color = paleta.texto,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    (contexto.applicationContext as? br.com.gestordriver.GestorDriverApp)
                        ?.diagnosticLog
                        ?.compartilhar(contexto)
                },
            )
        }
    }
}

@Composable
private fun DialogoContaGoogle(
    emailAtual: String,
    onFechar: () -> Unit,
    onConectar: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val seletor = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            ContaVinculo.emailDaResposta(resultado.data)?.let(onConectar)
        }
    }
    CaixaDialogo("Conta google", modifier) {
        Text(
            text = if (emailAtual.isBlank()) {
                "Conecte a conta Google do motorista. Isso identifica o usuário nas versões Free e Pro, sem sincronizar dados agora."
            } else {
                "Conectado: $emailAtual"
            },
            color = LocalPaletaApp.current.textoSecundario,
            fontSize = FonteCampo,
        )
        BotoesMensagem(
            textoEsquerda = "Cancelar",
            onEsquerda = onFechar,
            textoDireita = "Conectar",
            onDireita = { seletor.launch(ContaVinculo.intentEscolherContaGoogle()) },
        )
    }
}

@Composable
private fun DialogoContaEmail(
    emailAtual: String,
    onFechar: () -> Unit,
    onConectar: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by remember(emailAtual) { mutableStateOf(emailAtual) }
    var erro by remember { mutableStateOf(false) }
    CaixaDialogo("Conta email", modifier) {
        CampoCaixa("E-mail", email, {
            email = it
            erro = false
        })
        if (erro) {
            Text("Informe um e-mail válido.", color = TextoAmareloConfig, fontSize = 11.sp)
        }
        BotoesMensagem(
            textoEsquerda = "Cancelar",
            onEsquerda = onFechar,
            textoDireita = "Conectar",
            onDireita = {
                if (ContaVinculo.emailValido(email)) {
                    onConectar(email)
                } else {
                    erro = true
                }
            },
        )
    }
}

@Composable
private fun CaixaDialogo(
    titulo: String,
    modifier: Modifier = Modifier,
    conteudo: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, LocalPaletaApp.current.borda, FormaPainel)
            .background(LocalPaletaApp.current.fundoPainel, FormaPainel)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                titulo,
                color = LocalPaletaApp.current.texto,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            conteudo()
        }
    }
}

@Composable
private fun LinhaCampos(conteudo: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
        content = conteudo,
    )
}

@Composable
private fun CampoCaixa(
    label: String,
    valor: String,
    onValorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    bloqueado: Boolean = false,
    pro: Boolean = false,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        if (pro) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "🔒 $label",
                    color = LocalPaletaApp.current.textoSecundario,
                    fontSize = FonteCampo,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "versão pro",
                    color = TextoAmareloConfig,
                    fontSize = 9.sp,
                    maxLines = 1,
                )
            }
        } else {
            Text(
                text = label,
                color = LocalPaletaApp.current.textoSecundario,
                fontSize = FonteCampo,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(18.dp),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 32.dp)
                .border(1.dp, LocalPaletaApp.current.borda, FormaCaixa)
                .background(LocalPaletaApp.current.fundoCaixa, FormaCaixa)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = valor,
                onValueChange = { if (!bloqueado) onValorChange(it) },
                enabled = !bloqueado,
                singleLine = true,
                textStyle = TextStyle(color = LocalPaletaApp.current.texto, fontSize = FonteValor),
                cursorBrush = SolidColor(DestaqueSelecionado),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CampoNumericoCaixa(
    label: String,
    valor: Double,
    onValorChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    bloqueado: Boolean = false,
) {
    var texto by remember(valor) { mutableStateOf(DecimalInput.formatar(valor)) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(text = label, color = LocalPaletaApp.current.textoSecundario, fontSize = FonteCampo)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 32.dp)
                .border(1.dp, LocalPaletaApp.current.borda, FormaCaixa)
                .background(LocalPaletaApp.current.fundoCaixa, FormaCaixa)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = texto,
                onValueChange = { entrada ->
                    if (bloqueado) {
                        return@BasicTextField
                    }
                    if (entrada.isEmpty() || entrada.matches(Regex("^[0-9]*[.,]?[0-9]*$"))) {
                        texto = entrada
                        DecimalInput.parse(entrada)?.let(onValorChange)
                    }
                },
                enabled = !bloqueado,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = TextStyle(color = LocalPaletaApp.current.texto, fontSize = FonteValor),
                cursorBrush = SolidColor(DestaqueSelecionado),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FaixaClassificacao(
    titulo: String,
    corHex: String,
    min: Double,
    max: Double,
    minFixo: String?,
    maxFixo: String?,
    onMinChange: (Double) -> Unit,
    onMaxChange: (Double) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = titulo, color = LocalPaletaApp.current.texto, fontSize = FonteCampo, fontWeight = FontWeight.SemiBold)
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(android.graphics.Color.parseColor(corHex)), CircleShape),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CampoStepper(
                label = "Min",
                valorTexto = minFixo ?: FaixasClassificacao.formatar(min),
                editavel = minFixo == null,
                onMenos = { onMinChange(min - FaixasClassificacao.PASSO) },
                onMais = { onMinChange(min + FaixasClassificacao.PASSO) },
                modifier = Modifier.weight(1f),
            )
            CampoStepper(
                label = "Max",
                valorTexto = maxFixo ?: FaixasClassificacao.formatar(max),
                editavel = maxFixo == null,
                onMenos = { onMaxChange(max - FaixasClassificacao.PASSO) },
                onMais = { onMaxChange(max + FaixasClassificacao.PASSO) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CampoStepper(
    label: String,
    valorTexto: String,
    editavel: Boolean,
    onMenos: () -> Unit,
    onMais: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(text = label, color = LocalPaletaApp.current.textoSecundario, fontSize = FonteCampo)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LocalPaletaApp.current.borda, FormaCaixa)
                .background(LocalPaletaApp.current.fundoCaixa, FormaCaixa)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BotaoPasso("−", editavel, onMenos)
            Text(text = valorTexto, color = LocalPaletaApp.current.texto, fontSize = FonteValor, fontWeight = FontWeight.SemiBold)
            BotaoPasso("+", editavel, onMais)
        }
    }
}

@Composable
private fun BotaoPasso(texto: String, ativo: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .border(1.dp, if (ativo) TextoAmareloConfig else LocalPaletaApp.current.borda, FormaCaixa)
            .background(Color(0x22000000), FormaCaixa)
            .then(if (ativo) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = texto,
            color = if (ativo) TextoAmareloConfig else LocalPaletaApp.current.textoSecundario,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun AlertaOleoUi(configuracao: ConfiguracaoUsuario) {
    val contexto = LocalContext.current
    val app = contexto.applicationContext as? br.com.gestordriver.GestorDriverApp
    val pontos = app?.historicoRepository?.listar().orEmpty().map {
        it.dataHoraRegistro?.toLocalDate() to it.kmTotal
    }
    val kmDesde = AlertaOleo.kmDesdeTroca(configuracao.oleoData, pontos)
    val nivel = AlertaOleo.nivel(configuracao.oleoKilometragem, kmDesde)
    if (nivel == AlertaOleo.Nivel.OK || configuracao.oleoKilometragem <= 0.0) {
        return
    }
    val restante = configuracao.oleoKilometragem - kmDesde
    val texto = when (nivel) {
        AlertaOleo.Nivel.VENCIDO ->
            "Troca de óleo vencida. Já rodou ${"%.0f".format(kmDesde)} km desde a data informada."
        AlertaOleo.Nivel.AVISO ->
            "Troca de óleo perto do vencimento. Faltam cerca de ${"%.0f".format(restante.coerceAtLeast(0.0))} km."
        AlertaOleo.Nivel.OK -> return
    }
    Text(
        text = texto,
        color = Color(0xFFE53935),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun OpcaoMarca(texto: String, marcado: Boolean, onMarcar: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .heightIn(min = AlturaToque)
            .clickable(onClick = onMarcar)
            .padding(vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .border(1.dp, if (marcado) DestaqueSelecionado else LocalPaletaApp.current.borda, RoundedCornerShape(3.dp))
                .background(if (marcado) DestaqueSelecionado.copy(alpha = 0.25f) else Color.Transparent),
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(text = texto, color = LocalPaletaApp.current.texto, fontSize = FonteCampo)
    }
}

@Composable
private fun BarrasSemaforo(viewModel: ConfiguracoesViewModel) {
    val config = viewModel.configuracao
    BarraMarca("Ruim até", config.limiteRuimMax, Color(android.graphics.Color.parseColor(ClassificacaoConstantes.CORES.getValue(Classificacao.RUIM)))) { valor ->
        viewModel.atualizarMarcasDeslizantes(valor, config.limiteBoaMax)
    }
    BarraMarca("Boa até", config.limiteBoaMax, Color(android.graphics.Color.parseColor(ClassificacaoConstantes.CORES.getValue(Classificacao.BOA)))) { valor ->
        viewModel.atualizarMarcasDeslizantes(config.limiteRuimMax, valor)
    }
}

@Composable
private fun BarraMarca(titulo: String, valor: Double, cor: Color, onValor: (Double) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = "$titulo  ${FaixasClassificacao.formatar(valor)}",
            color = cor,
            fontSize = FonteCampo,
            fontWeight = FontWeight.SemiBold,
        )
        Slider(
            value = valor.toFloat().coerceIn(0f, 5f),
            onValueChange = { onValor(it.toDouble()) },
            valueRange = 0f..5f,
            modifier = Modifier.fillMaxWidth().heightIn(min = AlturaToque),
        )
    }
}

@Composable
private fun StatusToque(
    titulo: String,
    dica: String,
    ok: Boolean,
    destacar: Boolean,
    onClick: () -> Unit,
    clicavel: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val paleta = LocalPaletaApp.current
    val corMarca = when {
        ok -> Color(0xFF2E7D32)
        destacar -> Color(0xFFC62828)
        else -> paleta.borda
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AlturaToque)
            .then(if (clicavel) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(corMarca, CircleShape),
        )
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(
                text = titulo,
                color = paleta.texto,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = dica,
                color = paleta.textoSecundario,
                fontSize = 11.sp,
                maxLines = 2,
            )
        }
    }
}
