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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Slider
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
import br.com.gestordriver.core.AlertaOleo
import br.com.gestordriver.core.Classificacao
import br.com.gestordriver.core.ClassificacaoConstantes
import br.com.gestordriver.core.FaixasClassificacao
import br.com.gestordriver.core.TabelaIpvaPlaca
import br.com.gestordriver.data.ContaVinculo
import br.com.gestordriver.model.AppNavegacao
import br.com.gestordriver.model.Combustivel
import br.com.gestordriver.model.ConfiguracaoUsuario
import br.com.gestordriver.model.PlanoAcesso
import br.com.gestordriver.model.SeguroRecorrencia
import br.com.gestordriver.model.TemaApp
import br.com.gestordriver.model.TipoContaVinculada
import br.com.gestordriver.model.TipoVeiculo
import br.com.gestordriver.navigation.NavegacaoLauncher
import br.com.gestordriver.permission.PermissoesMonitoramento
import br.com.gestordriver.ui.theme.LocalPaletaApp

private val TextoAmareloConfig = Color(0xFFFFD54F)
private val DestaqueSelecionado = Color(0xFF7CB342)
private val FormaPainel = RoundedCornerShape(10.dp)
private val FormaCaixa = RoundedCornerShape(6.dp)

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
) {
    val configuracao = viewModel.configuracao
    var aba by remember { mutableIntStateOf(abaInicial) }
    val rolagem = rememberScrollState()
    val foco = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current
    var dialogoGoogle by remember { mutableStateOf(false) }
    var dialogoEmail by remember { mutableStateOf(false) }
    var dialogoAbastecimento by remember { mutableStateOf(false) }
    LaunchedEffect(abaInicial) {
        aba = abaInicial
    }
    LaunchedEffect(aba) {
        rolagem.scrollTo(0)
        foco.clearFocus(force = true)
        teclado?.hide()
    }
    val abas = listOf("Semáforo", "Custos", "Veículo", "App")
    val paleta = LocalPaletaApp.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .deslizeHorizontalAbas(aba, abas.size) { aba = it }
            .background(paleta.fundo),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "←",
                    color = paleta.texto,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .clickable {
                            viewModel.cancelar()
                            onVoltar()
                        }
                        .padding(8.dp),
                )
                Text(
                    text = "Configurações",
                    color = paleta.texto,
                    fontSize = FonteTitulo,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }

            FaixaAbasComSetas(
                titulos = abas,
                selecionada = aba,
                corAtiva = DestaqueSelecionado,
                corInativa = paleta.textoSecundario,
                onSelecionar = { aba = it },
                mostrarIndicador = true,
                tamanhoFonte = 11.sp,
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
                    .barraRolagemAoToque(rolagem)
                    .verticalScroll(rolagem)
                    .padding(start = 12.dp, end = 16.dp, top = 10.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                when (aba) {
                    0 -> AbaClassificacao(viewModel)
                    1 -> AbaCustos(viewModel, plano)
                    2 -> AbaVeiculo(viewModel, plano)
                    else -> AbaApp(
                        viewModel = viewModel,
                        destacarPermissoes = destacarPermissoes,
                        onGoogle = { dialogoGoogle = true },
                        onEmail = { dialogoEmail = true },
                    )
                }
            }

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
                        viewModel.cancelar()
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
                        if (viewModel.temCalculoAbastecimento()) {
                            dialogoAbastecimento = true
                        } else {
                            viewModel.salvar(aplicarAbastecimento = false)
                            onVoltar()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .background(DestaqueSelecionado.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                ) {
                    Text(
                        text = "SALVAR",
                        color = DestaqueSelecionado,
                        fontSize = 14.sp,
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
        if (dialogoAbastecimento) {
            CaixaDialogo("Usar abastecimento?", Modifier.align(Alignment.Center)) {
                Text(
                    text = "Preencher R$/L e km/L do combustível atual com o cálculo do abastecimento?",
                    color = LocalPaletaApp.current.textoSecundario,
                    fontSize = FonteCampo,
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    TextButton(onClick = {
                        viewModel.salvar(aplicarAbastecimento = false)
                        dialogoAbastecimento = false
                        onVoltar()
                    }) {
                        Text("Não", color = LocalPaletaApp.current.textoSecundario)
                    }
                    TextButton(onClick = {
                        viewModel.salvar(aplicarAbastecimento = true)
                        dialogoAbastecimento = false
                        onVoltar()
                    }) {
                        Text("Sim", color = DestaqueSelecionado, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AbaVeiculo(viewModel: ConfiguracoesViewModel, plano: PlanoAcesso) {
    val configuracao = viewModel.configuracao
    val travar = plano.travaCalculadora
    SubtituloSecao(
        texto = "Descrição do veículo",
        subtitulo = "Marca, modelo e placa",
        icone = "🚗",
        fundoIcone = Color(0xFFE3F2FD),
        ajuda = "Final da placa (0–9) define o mês de vencimento do IPVA. O valor do IPVA entra no custo do Dashboard.",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
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
    LinhaCampos {
        CampoCaixa("Final da placa", configuracao.finalPlaca, viewModel::atualizarFinalPlaca, Modifier.weight(1f))
        CampoNumericoCaixa(
            label = if (travar) "🔒 IPVA R$" else "IPVA R$",
            valor = configuracao.ipvaValor,
            onValorChange = viewModel::atualizarIpvaValor,
            modifier = Modifier.weight(1f),
            bloqueado = travar,
        )
    }
    Text(
        text = TabelaIpvaPlaca.textoVencimento(configuracao.finalPlaca),
        color = LocalPaletaApp.current.textoSecundario,
        fontSize = 11.sp,
    )
    SubtituloSecao(
        texto = "Consumo",
        subtitulo = "Km/L ou km/kWh",
        icone = "⛽",
        fundoIcone = Color(0xFFEDE7F6),
        ajuda = "Gasolina/etanol em km/L. Energia em km/kWh. Entra no gasto estimado da oferta.",
    )
    LinhaCampos {
        CampoNumericoCaixa("Gasolina", configuracao.consumoGasolina, viewModel::atualizarConsumoGasolina, Modifier.weight(1f))
        CampoNumericoCaixa("Etanol", configuracao.consumoEtanol, viewModel::atualizarConsumoEtanol, Modifier.weight(1f))
        CampoNumericoCaixa("Energia", configuracao.consumoEnergia, viewModel::atualizarConsumoEnergia, Modifier.weight(1f))
    }
    if (travar) {
        TituloPro("Calcular abastecimento")
    } else {
        Text(
            text = "Calcular abastecimento",
            color = LocalPaletaApp.current.textoSecundario,
            fontSize = FonteCampo,
            fontWeight = FontWeight.Medium,
        )
    }
    LinhaCampos {
        CampoNumericoCaixa("Valor R$", configuracao.abastecimentoValor, viewModel::atualizarAbastecimentoValor, Modifier.weight(1f), bloqueado = travar)
        CampoNumericoCaixa("Quant. litros", configuracao.abastecimentoLitros, viewModel::atualizarAbastecimentoLitros, Modifier.weight(1f), bloqueado = travar)
    }
    LinhaCampos {
        CampoNumericoCaixa("Km inicial", configuracao.abastecimentoKmInicial, viewModel::atualizarAbastecimentoKmInicial, Modifier.weight(1f), bloqueado = travar)
        CampoNumericoCaixa("Km final", configuracao.abastecimentoKmFinal, viewModel::atualizarAbastecimentoKmFinal, Modifier.weight(1f), bloqueado = travar)
    }
}

@Composable
private fun AbaCustos(viewModel: ConfiguracoesViewModel, plano: PlanoAcesso) {
    val configuracao = viewModel.configuracao
    val travar = plano.travaCalculadora
    SubtituloSecao(
        texto = "Despesas do veiculo",
        subtitulo = "Preço e tipo de energia",
        icone = "⛽",
        fundoIcone = Color(0xFFEDE7F6),
        ajuda = "Preço do litro ou do kWh. Com o consumo, o app calcula gasto e lucro da oferta.",
    )
    LinhaCampos {
        CampoNumericoCaixa("R$ / L Gasolina", configuracao.precoGasolina, viewModel::atualizarPrecoGasolina, Modifier.weight(1f))
        CampoNumericoCaixa("R$ / L Etanol", configuracao.precoEtanol, viewModel::atualizarPrecoEtanol, Modifier.weight(1f))
        CampoNumericoCaixa("R$ / kWh", configuracao.precoEnergia, viewModel::atualizarPrecoEnergia, Modifier.weight(1f))
    }
    SubtituloSecao(
        texto = "Combustível atual",
        subtitulo = "Gasolina, etanol ou energia",
        ajuda = "Marque só um. Energia usa km/kWh × R$/kWh × 1,12 (perdas de recarga).",
    )
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
    if (!configuracao.calculadoraCombustivelPronta()) {
        Text(
            text = "Consumo ou preço 0: litros/kWh, gasto e lucro da oferta ficam — até preencher.",
            color = TextoAmareloConfig,
            fontSize = 11.sp,
        )
    }
    if (travar) {
        TituloPro("Troca de óleo (óleo e filtros)")
    } else {
        Text(
            text = "Troca de óleo (óleo e filtros)",
            color = LocalPaletaApp.current.textoSecundario,
            fontSize = FonteCampo,
            fontWeight = FontWeight.Medium,
        )
    }
    LinhaCampos {
        CampoNumericoCaixa("Valor R$", configuracao.oleoValor, viewModel::atualizarOleoValor, Modifier.weight(1f), bloqueado = travar)
        CampoNumericoCaixa("Km", configuracao.oleoKilometragem, viewModel::atualizarOleoKm, Modifier.weight(1f), bloqueado = travar)
        CampoCaixa("Data", configuracao.oleoData, viewModel::atualizarOleoData, Modifier.weight(1f), bloqueado = travar)
    }
    AlertaOleoUi(configuracao)
    if (travar) {
        TituloPro("Custo estimado dos pneus")
    } else {
        Text(
            text = "Custo estimado dos pneus",
            color = LocalPaletaApp.current.textoSecundario,
            fontSize = FonteCampo,
            fontWeight = FontWeight.Medium,
        )
    }
    Text("Dianteiro", color = LocalPaletaApp.current.texto, fontSize = FonteCampo, fontWeight = FontWeight.Medium)
    LinhaCampos {
        CampoNumericoCaixa("Valor R$", configuracao.pneuDianteiroValor, viewModel::atualizarPneuDianteiroValor, Modifier.weight(1f), bloqueado = travar)
        CampoNumericoCaixa("Rodagem", configuracao.pneuDianteiroRodagem, viewModel::atualizarPneuDianteiroRodagem, Modifier.weight(1f), bloqueado = travar)
        CampoCaixa("Data", configuracao.pneuDianteiroData, viewModel::atualizarPneuDianteiroData, Modifier.weight(1f), bloqueado = travar)
    }
    Text("Traseiro", color = LocalPaletaApp.current.texto, fontSize = FonteCampo, fontWeight = FontWeight.Medium)
    LinhaCampos {
        CampoNumericoCaixa("Valor R$", configuracao.pneuTraseiroValor, viewModel::atualizarPneuTraseiroValor, Modifier.weight(1f), bloqueado = travar)
        CampoNumericoCaixa("Rodagem", configuracao.pneuTraseiroRodagem, viewModel::atualizarPneuTraseiroRodagem, Modifier.weight(1f), bloqueado = travar)
        CampoCaixa("Data", configuracao.pneuTraseiroData, viewModel::atualizarPneuTraseiroData, Modifier.weight(1f), bloqueado = travar)
    }
    if (travar) {
        TituloPro("Seguro")
    } else {
        Text(
            text = "Seguro",
            color = LocalPaletaApp.current.textoSecundario,
            fontSize = FonteCampo,
            fontWeight = FontWeight.Medium,
        )
    }
    LinhaCampos {
        CampoNumericoCaixa(
            "Valor do seguro",
            configuracao.seguroValor,
            viewModel::atualizarSeguroValor,
            Modifier.weight(1f),
            bloqueado = travar,
        )
        CampoCaixa(
            "Data de vencimento",
            configuracao.seguroData,
            viewModel::atualizarSeguroData,
            Modifier.weight(1f),
            bloqueado = travar,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Recorrência",
                color = LocalPaletaApp.current.textoSecundario,
                fontSize = 11.sp,
            )
            OpcaoMarca(
                texto = "Mensal",
                marcado = configuracao.seguroRecorrencia == SeguroRecorrencia.MENSAL,
                onMarcar = { if (!travar) viewModel.atualizarSeguroRecorrencia(SeguroRecorrencia.MENSAL) },
            )
            OpcaoMarca(
                texto = "Anual",
                marcado = configuracao.seguroRecorrencia == SeguroRecorrencia.ANUAL,
                onMarcar = { if (!travar) viewModel.atualizarSeguroRecorrencia(SeguroRecorrencia.ANUAL) },
            )
        }
    }
}

@Composable
private fun AbaClassificacao(viewModel: ConfiguracoesViewModel) {
    val configuracao = viewModel.configuracao
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
    SubtituloSecao(
        texto = "Calibrar a classificação",
        subtitulo = "Cor da borda da compacta",
        icone = "🚦",
        fundoIcone = Color(0xFFFFF8E1),
        ajuda = "Faixas de R$/km. A cor da borda da compacta segue esta escala. Arraste a barra ou use − e + de 0,01.",
    )
    BarrasSemaforo(viewModel)
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
    SubtituloSecao(
        texto = "Configurações do aplicativo",
        subtitulo = "Configurar app",
        icone = "⚙",
        fundoIcone = Color(0xFFE0F2F1),
    )
    SubtituloSecao(
        texto = "Permissões",
        subtitulo = "Para monitorar ofertas",
        ajuda = "Notificação, sobrepor, acessibilidade e bateria são obrigatórias. Localização ajuda o mapa. Acessibilidade: Configurações restritas → Serviços instalados.",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatusToque(
            titulo = "1  Notificações",
            dica = "Lê as ofertas da Uber e da 99",
            ok = listenerOk,
            destacar = destacarPermissoes && !listenerOk,
            onClick = { contexto.startActivity(PermissoesMonitoramento.intentNotificacoes()) },
            modifier = Modifier.weight(1f),
        )
        StatusToque(
            titulo = "2  Sobrepor",
            dica = "Mostra o card sobre o mapa",
            ok = overlayOk,
            destacar = destacarPermissoes && !overlayOk,
            onClick = { contexto.startActivity(PermissoesMonitoramento.intentSobrepor(contexto)) },
            modifier = Modifier.weight(1f),
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatusToque(
            titulo = "3  Acessibilidade",
            dica = "Configurações restritas → Serviços instalados",
            ok = leituraOk,
            destacar = destacarPermissoes && !leituraOk,
            onClick = { contexto.startActivity(PermissoesMonitoramento.intentAcessibilidade()) },
            modifier = Modifier.weight(1f),
        )
        StatusToque(
            titulo = "4  Bateria",
            dica = "Evita o overlay sumir no segundo plano",
            ok = bateriaOk,
            destacar = destacarPermissoes && !bateriaOk,
            onClick = { contexto.startActivity(PermissoesMonitoramento.intentBateria(contexto)) },
            modifier = Modifier.weight(1f),
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatusToque(
            titulo = "5  Localização",
            dica = "Opcional. Ajuda o mapa",
            ok = localizacaoOk,
            destacar = false,
            onClick = { (contexto as? br.com.gestordriver.MainActivity)?.pedirLocalizacao() },
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.weight(1f))
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
    SubtituloSecao(
        texto = "Tema",
        subtitulo = "Escuro, claro ou do celular",
        ajuda = "Define as cores do overlay e das telas. Celular segue o modo do aparelho.",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OpcaoMarca(
            texto = "Escuro",
            marcado = configuracao.tema == TemaApp.ESCURO,
            onMarcar = { viewModel.selecionarTema(TemaApp.ESCURO) },
        )
        OpcaoMarca(
            texto = "Claro",
            marcado = configuracao.tema == TemaApp.CLARO,
            onMarcar = { viewModel.selecionarTema(TemaApp.CLARO) },
        )
        OpcaoMarca(
            texto = "Celular",
            marcado = configuracao.tema == TemaApp.CELULAR,
            onMarcar = { viewModel.selecionarTema(TemaApp.CELULAR) },
        )
    }
    SubtituloSecao(
        texto = "Navegação",
        subtitulo = "Maps ou Waze",
        ajuda = "App de mapa para abrir embarque e destino quando o endereço for lido.",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OpcaoMarca(
            texto = "Google Maps",
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
            color = LocalPaletaApp.current.textoSecundario,
            fontSize = FonteCampo,
            modifier = Modifier.padding(6.dp),
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
                "Conecte a conta Google do motorista. Isso identifica o usuário nas versões Free e Pro, sem sincronizar dados agora."
            } else {
                "Conectado: $emailAtual"
            },
            color = LocalPaletaApp.current.textoSecundario,
            fontSize = FonteCampo,
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = onFechar) {
                Text("Cancelar", color = LocalPaletaApp.current.textoSecundario)
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
                Text("Cancelar", color = LocalPaletaApp.current.textoSecundario)
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
            .border(2.dp, LocalPaletaApp.current.borda, FormaPainel)
            .background(LocalPaletaApp.current.fundoPainel, FormaPainel)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(titulo, color = LocalPaletaApp.current.texto, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            conteudo()
        }
    }
}

@Composable
private fun SubtituloSecao(
    texto: String,
    ajuda: String? = null,
    icone: String? = null,
    fundoIcone: Color = Color(0xFFE8EEF2),
    subtitulo: String? = null,
) {
    val contexto = LocalContext.current
    val paleta = LocalPaletaApp.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icone != null) {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(28.dp)
                    .background(fundoIcone, RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = icone, fontSize = 13.sp)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = texto,
                color = paleta.texto,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            if (subtitulo != null) {
                Text(
                    text = subtitulo,
                    color = paleta.textoSecundario,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (ajuda != null) {
            Text(
                text = "AJUDA",
                color = TextoAmareloConfig,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable {
                        android.widget.Toast.makeText(contexto, ajuda, android.widget.Toast.LENGTH_LONG).show()
                    }
                    .padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
            )
        }
    }
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
            color = LocalPaletaApp.current.textoSecundario,
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .heightIn(min = AlturaToque)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
    ) {
        Text(
            text = if (ok) "$titulo  🆗" else "$titulo  ❎",
            color = when {
                ok -> DestaqueSelecionado
                destacar -> Color(0xFFFFCDD2)
                else -> TextoAmareloConfig
            },
            fontSize = FonteCampo,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = dica,
            color = LocalPaletaApp.current.textoSecundario,
            fontSize = FonteAjuda,
        )
    }
}
