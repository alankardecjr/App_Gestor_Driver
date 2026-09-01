package br.com.gestordriver.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestordriver.core.Classificacao
import br.com.gestordriver.core.ClassificacaoConstantes
import br.com.gestordriver.core.FaixasClassificacao
import br.com.gestordriver.data.ContaVinculo
import br.com.gestordriver.model.AppNavegacao
import br.com.gestordriver.model.Combustivel
import br.com.gestordriver.model.TipoContaVinculada
import br.com.gestordriver.navigation.NavegacaoLauncher
import br.com.gestordriver.permission.PermissoesMonitoramento

private val TextoPrincipal = Color.White
private val TextoSecundario = Color(0xFFB8C5D1)
private val BordaCampo = Color(0xFF3D4A57)
private val BordaPainel = Color(0xFF607D8B)
private val DestaqueSelecionado = Color(0xFF7CB342)
private val TextoAmareloConfig = Color(0xFFFFD54F)
private val FundoCaixa = Color(0x33000000)
private val FormaPainel = RoundedCornerShape(10.dp)
private val FormaCaixa = RoundedCornerShape(6.dp)
private val AlturaAba = 268.dp + 188.dp + 76.dp

private val FonteAba = 13.sp
private val FonteCampo = 12.sp
private val FonteValor = 13.sp

@Composable
fun ConfiguracoesScreen(
    viewModel: ConfiguracoesViewModel,
    onVoltar: () -> Unit,
    abaInicial: Int = 0,
    destacarPermissoes: Boolean = false,
) {
    val configuracao = viewModel.configuracao
    var aba by remember { mutableIntStateOf(abaInicial) }
    val rolagem = rememberScrollState()
    val foco = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current
    var dialogoGoogle by remember { mutableStateOf(false) }
    var dialogoEmail by remember { mutableStateOf(false) }
    LaunchedEffect(abaInicial) {
        aba = abaInicial
    }
    LaunchedEffect(aba) {
        rolagem.scrollTo(0)
        foco.clearFocus(force = true)
        teclado?.hide()
    }
    val abas = listOf("VEÍCULO", "CUSTOS", "CALIBRAR", "APP")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .deslizeHorizontalAbas(aba, abas.size) { aba = it }
            .border(width = 2.dp, color = BordaPainel, shape = FormaPainel)
            .background(Color(0xF2050809), FormaPainel)
            .padding(vertical = 8.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                TituloComSetas(
                    titulo = "CONFIGURAÇÃO",
                    onEsquerda = { aba = (aba - 1).coerceAtLeast(0) },
                    onDireita = { aba = (aba + 1).coerceAtMost(abas.lastIndex) },
                    corTitulo = TextoPrincipal,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                abas.forEachIndexed { index, titulo ->
                    Text(
                        text = titulo,
                        color = if (aba == index) DestaqueSelecionado else TextoSecundario,
                        fontSize = FonteAba,
                        fontWeight = if (aba == index) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier
                            .clickable { aba = index }
                            .padding(horizontal = 2.dp, vertical = 2.dp),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = AlturaAba, max = AlturaAba)
                    .barraRolagemAoToque(rolagem)
                    .verticalScroll(rolagem)
                    .padding(start = 8.dp, end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                when (aba) {
                    0 -> AbaVeiculo(viewModel)
                    1 -> AbaCustos(viewModel)
                    2 -> AbaClassificacao(viewModel)
                    else -> AbaApp(
                        viewModel = viewModel,
                        destacarPermissoes = destacarPermissoes,
                        onGoogle = { dialogoGoogle = true },
                        onEmail = { dialogoEmail = true },
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = {
                        viewModel.cancelar()
                        onVoltar()
                    },
                ) {
                    Text(text = "Cancelar", color = TextoSecundario, fontSize = FonteCampo)
                }
                TextButton(
                    onClick = {
                        viewModel.salvar()
                        onVoltar()
                    },
                ) {
                    Text(
                        text = "Salvar",
                        color = DestaqueSelecionado,
                        fontSize = FonteCampo,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        if (dialogoGoogle) {
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
                modifier = Modifier.align(Alignment.Center),
            )
        }
        if (dialogoEmail) {
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
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun AbaVeiculo(viewModel: ConfiguracoesViewModel) {
    val configuracao = viewModel.configuracao
    SubtituloSecao("Descrição veículo")
    LinhaCampos {
        CampoCaixa("Marca", configuracao.marcaVeiculo, viewModel::atualizarMarca, Modifier.weight(1f))
        CampoCaixa("Modelo", configuracao.modeloVeiculo, viewModel::atualizarModelo, Modifier.weight(1f))
    }
    LinhaCampos {
        CampoCaixa("Versão", configuracao.versaoVeiculo, viewModel::atualizarVersao, Modifier.weight(1f))
        CampoCaixa("Ano", configuracao.anoVeiculo, viewModel::atualizarAno, Modifier.weight(1f))
    }
    LinhaCampos {
        CampoCaixa("Final da placa", configuracao.finalPlaca, viewModel::atualizarFinalPlaca, Modifier.weight(1f))
        CampoCaixa(
            label = "Ipva",
            valor = configuracao.ipvaVencimento,
            onValorChange = {},
            modifier = Modifier.weight(1f),
            bloqueado = true,
            pro = true,
        )
    }
    SubtituloSecao("Consumo km")
    LinhaCampos {
        CampoNumericoCaixa("Gasolina", configuracao.consumoGasolina, viewModel::atualizarConsumoGasolina, Modifier.weight(1f))
        CampoNumericoCaixa("Etanol", configuracao.consumoEtanol, viewModel::atualizarConsumoEtanol, Modifier.weight(1f))
    }
    SubtituloSecao("Combustível atual")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
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
    }
    TituloPro("Calcular abastecimento")
    LinhaCampos {
        CampoCaixa("Valor total", "", {}, Modifier.weight(1f), bloqueado = true)
        CampoCaixa("Litros total", "", {}, Modifier.weight(1f), bloqueado = true)
    }
    LinhaCampos {
        CampoCaixa("Km inicial", "", {}, Modifier.weight(1f), bloqueado = true)
        CampoCaixa("Km final", "", {}, Modifier.weight(1f), bloqueado = true)
    }
}

@Composable
private fun AbaCustos(viewModel: ConfiguracoesViewModel) {
    val configuracao = viewModel.configuracao
    SubtituloSecao("Valor do combustível")
    LinhaCampos {
        CampoNumericoCaixa("Litro gasolina", configuracao.precoGasolina, viewModel::atualizarPrecoGasolina, Modifier.weight(1f))
        CampoNumericoCaixa("Litro etanol", configuracao.precoEtanol, viewModel::atualizarPrecoEtanol, Modifier.weight(1f))
    }
    TituloPro("Troca de óleo (óleo e filtros)")
    LinhaCampos {
        CampoCaixa("Valor", "", {}, Modifier.weight(1f), bloqueado = true)
        CampoCaixa("Km", "", {}, Modifier.weight(1f), bloqueado = true)
        CampoCaixa("Data", "", {}, Modifier.weight(1f), bloqueado = true)
    }
    TituloPro("Custo estimado dos pneus")
    Text("Dianteiro", color = TextoPrincipal, fontSize = FonteCampo, fontWeight = FontWeight.Medium)
    LinhaCampos {
        CampoCaixa("Valor", "", {}, Modifier.weight(1f), bloqueado = true)
        CampoCaixa("Rodagem", "", {}, Modifier.weight(1f), bloqueado = true)
        CampoCaixa("Data", "", {}, Modifier.weight(1f), bloqueado = true)
    }
    Text("Traseiro", color = TextoPrincipal, fontSize = FonteCampo, fontWeight = FontWeight.Medium)
    LinhaCampos {
        CampoCaixa("Valor", "", {}, Modifier.weight(1f), bloqueado = true)
        CampoCaixa("Rodagem", "", {}, Modifier.weight(1f), bloqueado = true)
        CampoCaixa("Data", "", {}, Modifier.weight(1f), bloqueado = true)
    }
}

@Composable
private fun AbaClassificacao(viewModel: ConfiguracoesViewModel) {
    val configuracao = viewModel.configuracao
    SubtituloSecao("Calibrar classificações")
    FaixaClassificacao(
        "Ruim",
        ClassificacaoConstantes.CORES.getValue(Classificacao.RUIM),
        configuracao.limiteRuimMin,
        configuracao.limiteRuimMax,
        "Min",
        null,
        viewModel::atualizarLimiteRuimMin,
        viewModel::atualizarLimiteRuimMax,
    )
    FaixaClassificacao(
        "Regular",
        ClassificacaoConstantes.CORES.getValue(Classificacao.REGULAR),
        configuracao.limiteRegularMin,
        configuracao.limiteRegularMax,
        null,
        null,
        viewModel::atualizarLimiteRegularMin,
        viewModel::atualizarLimiteRegularMax,
    )
    FaixaClassificacao(
        "Boa",
        ClassificacaoConstantes.CORES.getValue(Classificacao.BOA),
        configuracao.limiteBoaMin,
        configuracao.limiteBoaMax,
        null,
        null,
        viewModel::atualizarLimiteBoaMin,
        viewModel::atualizarLimiteBoaMax,
    )
    FaixaClassificacao(
        "Ótima",
        ClassificacaoConstantes.CORES.getValue(Classificacao.EXCELENTE),
        configuracao.limiteOtimaMin,
        configuracao.limiteOtimaMax,
        null,
        "Max",
        viewModel::atualizarLimiteOtimaMin,
        viewModel::atualizarLimiteOtimaMax,
    )
}

@Composable
private fun AbaApp(
    viewModel: ConfiguracoesViewModel,
    destacarPermissoes: Boolean,
    onGoogle: () -> Unit,
    onEmail: () -> Unit,
) {
    val contexto = LocalContext.current
    val configuracao = viewModel.configuracao
    val overlayOk = PermissoesMonitoramento.overlayConcedida(contexto)
    val listenerOk = PermissoesMonitoramento.listenerNotificacoesAtivo(contexto)
    val leituraOk = PermissoesMonitoramento.acessibilidadeAtiva(contexto)
    val bateriaOk = PermissoesMonitoramento.bateriaLiberada(contexto)
    val localizacaoOk = PermissoesMonitoramento.localizacaoConcedida(contexto)
    SubtituloSecao("Permissões")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        StatusToque(
            titulo = "Notificações",
            ok = listenerOk,
            destacar = destacarPermissoes && !listenerOk,
            onClick = { contexto.startActivity(PermissoesMonitoramento.intentNotificacoes()) },
        )
        StatusToque(
            titulo = "Sobrepor",
            ok = overlayOk,
            destacar = destacarPermissoes && !overlayOk,
            onClick = { contexto.startActivity(PermissoesMonitoramento.intentSobrepor(contexto)) },
        )
        StatusToque(
            titulo = "Acessib.",
            ok = leituraOk,
            destacar = destacarPermissoes && !leituraOk,
            onClick = { contexto.startActivity(PermissoesMonitoramento.intentAcessibilidade()) },
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        StatusToque(
            titulo = "Bateria",
            ok = bateriaOk,
            destacar = destacarPermissoes && !bateriaOk,
            onClick = { contexto.startActivity(PermissoesMonitoramento.intentBateria(contexto)) },
        )
        StatusToque(
            titulo = "Localização",
            ok = localizacaoOk,
            destacar = false,
            onClick = { (contexto as? br.com.gestordriver.MainActivity)?.pedirLocalizacao() },
        )
    }
    Text(
        text = "Acessib. lê o card na tela (Uber/99). Bateria evita o overlay sumir. Localização é opcional.",
        color = TextoSecundario,
        fontSize = 10.sp,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Enviar log",
            color = TextoAmareloConfig,
            fontSize = FonteCampo,
            modifier = Modifier.clickable {
                (contexto.applicationContext as? br.com.gestordriver.GestorDriverApp)
                    ?.diagnosticLog
                    ?.compartilhar(contexto)
            }.padding(6.dp),
        )
        Text(
            text = "v${PermissoesMonitoramento.versaoApp(contexto)}",
            color = TextoSecundario,
            fontSize = FonteCampo,
            modifier = Modifier.padding(6.dp),
        )
    }
    SubtituloSecao("App de corrida")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        listOf(
            br.com.gestordriver.notification.Plataforma.UBER to "Uber",
            br.com.gestordriver.notification.Plataforma.NOVE_NOVE to "99",
            br.com.gestordriver.notification.Plataforma.INDRIVE to "Indrive",
        ).forEach { (plataforma, titulo) ->
            val ok = br.com.gestordriver.notification.PlataformasMotorista.instalada(contexto, plataforma)
            Text(
                text = if (ok) "$titulo 🆗" else "$titulo ❎",
                color = if (ok) DestaqueSelecionado else TextoAmareloConfig,
                fontSize = FonteCampo,
            )
        }
    }
    SubtituloSecao("Navegação")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OpcaoMarca(
            texto = "Google maps",
            marcado = configuracao.navegacao == AppNavegacao.GOOGLE_MAPS,
            onMarcar = {
                viewModel.selecionarNavegacao(AppNavegacao.GOOGLE_MAPS)
                NavegacaoLauncher.abrirAplicativo(contexto, AppNavegacao.GOOGLE_MAPS)
            },
        )
        OpcaoMarca(
            texto = "Waze",
            marcado = configuracao.navegacao == AppNavegacao.WAZE,
            onMarcar = {
                viewModel.selecionarNavegacao(AppNavegacao.WAZE)
                NavegacaoLauncher.abrirAplicativo(contexto, AppNavegacao.WAZE)
            },
        )
    }
    SubtituloSecao("Conectar conta email")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        val googleOk = configuracao.contaTipo == TipoContaVinculada.GOOGLE
        val emailOk = configuracao.contaTipo == TipoContaVinculada.EMAIL
        Text(
            text = if (googleOk) "Conta google 🆗" else "Conta google",
            color = TextoAmareloConfig,
            fontSize = FonteCampo,
            modifier = Modifier.clickable(onClick = onGoogle).padding(8.dp),
        )
        Text(
            text = if (emailOk) "Conta email 🆗" else "Conta email",
            color = TextoAmareloConfig,
            fontSize = FonteCampo,
            modifier = Modifier.clickable(onClick = onEmail).padding(8.dp),
        )
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
                "Conecte a conta Google do motorista. Isso identifica o usuário nas versões Free e Beta, sem sincronizar dados agora."
            } else {
                "Conectado: $emailAtual"
            },
            color = TextoSecundario,
            fontSize = FonteCampo,
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = onFechar) {
                Text("Cancelar", color = TextoSecundario)
            }
            TextButton(
                onClick = { seletor.launch(ContaVinculo.intentEscolherContaGoogle()) },
            ) {
                Text("Conectar", color = TextoAmareloConfig, fontWeight = FontWeight.SemiBold)
            }
        }
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = onFechar) {
                Text("Cancelar", color = TextoSecundario)
            }
            TextButton(
                onClick = {
                    if (ContaVinculo.emailValido(email)) {
                        onConectar(email)
                    } else {
                        erro = true
                    }
                },
            ) {
                Text("Conectar", color = TextoAmareloConfig, fontWeight = FontWeight.SemiBold)
            }
        }
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
            .border(2.dp, BordaPainel, FormaPainel)
            .background(Color(0xF2050809), FormaPainel)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(titulo, color = TextoPrincipal, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            conteudo()
        }
    }
}

@Composable
private fun SubtituloSecao(texto: String) {
    Text(
        text = texto,
        color = TextoSecundario,
        fontSize = FonteCampo,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun TituloPro(texto: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "🔒 $texto",
            color = TextoSecundario,
            fontSize = FonteCampo,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        Text(text = "versão pro", color = TextoAmareloConfig, fontSize = 10.sp)
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
                    color = TextoSecundario,
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
                color = TextoSecundario,
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
                .border(1.dp, BordaCampo, FormaCaixa)
                .background(FundoCaixa, FormaCaixa)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = valor,
                onValueChange = { if (!bloqueado) onValorChange(it) },
                enabled = !bloqueado,
                singleLine = true,
                textStyle = TextStyle(color = TextoPrincipal, fontSize = FonteValor),
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
) {
    var texto by remember(valor) { mutableStateOf(DecimalInput.formatar(valor)) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(text = label, color = TextoSecundario, fontSize = FonteCampo)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 32.dp)
                .border(1.dp, BordaCampo, FormaCaixa)
                .background(FundoCaixa, FormaCaixa)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = texto,
                onValueChange = { entrada ->
                    if (entrada.isEmpty() || entrada.matches(Regex("^[0-9]*[.,]?[0-9]*$"))) {
                        texto = entrada
                        DecimalInput.parse(entrada)?.let(onValorChange)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = TextStyle(color = TextoPrincipal, fontSize = FonteValor),
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
        modifier = Modifier.padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = titulo, color = TextoPrincipal, fontSize = FonteCampo, fontWeight = FontWeight.SemiBold)
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
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(text = label, color = TextoSecundario, fontSize = FonteCampo)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BordaCampo, FormaCaixa)
                .background(FundoCaixa, FormaCaixa)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BotaoPasso("−", editavel, onMenos)
            Text(text = valorTexto, color = TextoPrincipal, fontSize = FonteValor, fontWeight = FontWeight.SemiBold)
            BotaoPasso("+", editavel, onMais)
        }
    }
}

@Composable
private fun BotaoPasso(texto: String, ativo: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .border(1.dp, if (ativo) TextoAmareloConfig else BordaCampo, FormaCaixa)
            .background(Color(0x22000000), FormaCaixa)
            .then(if (ativo) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = texto,
            color = if (ativo) TextoAmareloConfig else TextoSecundario,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun OpcaoMarca(texto: String, marcado: Boolean, onMarcar: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onMarcar)
            .padding(vertical = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .border(1.dp, if (marcado) DestaqueSelecionado else BordaCampo, RoundedCornerShape(3.dp))
                .background(if (marcado) DestaqueSelecionado.copy(alpha = 0.25f) else Color.Transparent),
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(text = texto, color = TextoPrincipal, fontSize = FonteCampo)
    }
}

@Composable
private fun StatusToque(titulo: String, ok: Boolean, destacar: Boolean, onClick: () -> Unit) {
    Text(
        text = if (ok) "$titulo 🆗" else "$titulo ❎",
        color = when {
            ok -> DestaqueSelecionado
            destacar -> Color(0xFFFFCDD2)
            else -> TextoAmareloConfig
        },
        fontSize = FonteCampo,
        modifier = Modifier.clickable(onClick = onClick).padding(4.dp),
    )
}
