package br.com.gestordriver.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.gestordriver.core.FaixasClassificacao
import br.com.gestordriver.model.AppNavegacao
import br.com.gestordriver.model.Combustivel
import br.com.gestordriver.navigation.NavegacaoLauncher
import br.com.gestordriver.permission.PermissoesMonitoramento

private val FundoCard = Color(0xFF050809)
private val TextoPrincipal = Color.White
private val TextoSecundario = Color(0xFFB8C5D1)
private val BordaCampo = Color(0xFF2B3440)
private val DestaqueSelecionado = Color(0xFF7CB342)
private val TextoAmareloConfig = Color(0xFFFFD54F)

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
    LaunchedEffect(abaInicial) {
        aba = abaInicial
    }
    LaunchedEffect(aba) {
        rolagem.scrollTo(0)
        foco.clearFocus(force = true)
        teclado?.hide()
    }
    val abas = listOf("VEÍCULO", "CUSTOS", "APP")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF101418),
                        Color(0xFF171D25),
                        Color(0xFF0D1117),
                    ),
                ),
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .deslizeHorizontalAbas(aba, abas.size) { aba = it }
            .verticalScroll(rolagem),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "CONFIGURAÇÃO",
                color = TextoPrincipal,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            abas.forEachIndexed { index, titulo ->
                TextButton(onClick = { aba = index }) {
                    Text(
                        text = titulo,
                        color = if (aba == index) DestaqueSelecionado else TextoSecundario,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (aba == index) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }

        when (aba) {
            0 -> SecaoCard(titulo = "VEÍCULO") {
                SubtituloSecao(texto = "DESCRIÇÃO")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CampoTextoCompacto(
                        label = "MARCA",
                        valor = configuracao.marcaVeiculo,
                        onValorChange = viewModel::atualizarMarca,
                        modifier = Modifier.weight(1f),
                    )
                    CampoTextoCompacto(
                        label = "MODELO",
                        valor = configuracao.modeloVeiculo,
                        onValorChange = viewModel::atualizarModelo,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CampoTextoCompacto(
                        label = "VERSÃO",
                        valor = configuracao.versaoVeiculo,
                        onValorChange = viewModel::atualizarVersao,
                        modifier = Modifier.weight(1f),
                    )
                    CampoTextoCompacto(
                        label = "ANO",
                        valor = configuracao.anoVeiculo,
                        onValorChange = viewModel::atualizarAno,
                        modifier = Modifier.weight(1f),
                    )
                }
                SubtituloSecao(texto = "CONSUMO KM")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CampoNumericoCompacto(
                        label = "GASOLINA",
                        valor = configuracao.consumoGasolina,
                        onValorChange = viewModel::atualizarConsumoGasolina,
                        modifier = Modifier.weight(1f),
                    )
                    CampoNumericoCompacto(
                        label = "ETANOL",
                        valor = configuracao.consumoEtanol,
                        onValorChange = viewModel::atualizarConsumoEtanol,
                        modifier = Modifier.weight(1f),
                    )
                }
                SubtituloSecao(texto = "COMBUSTÍVEL ATUAL")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = configuracao.combustivel == Combustivel.GASOLINA,
                        onCheckedChange = { marcado ->
                            if (marcado) viewModel.selecionarCombustivel(Combustivel.GASOLINA)
                        },
                        modifier = Modifier.scale(0.82f),
                    )
                    Text(
                        "GASOLINA",
                        color = TextoPrincipal,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                    androidx.compose.material3.Checkbox(
                        checked = configuracao.combustivel == Combustivel.ETANOL,
                        onCheckedChange = { marcado ->
                            if (marcado) viewModel.selecionarCombustivel(Combustivel.ETANOL)
                        },
                        modifier = Modifier.scale(0.82f),
                    )
                    Text(
                        "ETANOL",
                        color = TextoPrincipal,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                    Text("ETANOL", color = TextoPrincipal, modifier = Modifier.align(Alignment.CenterVertically))
                }
            }

            1 -> SecaoCard(titulo = "CUSTOS", compacto = true) {
                SubtituloSecao(texto = "VALOR DO COMBUSTÍVEL")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CampoNumericoCompacto(
                        label = "LITRO GASOLINA",
                        valor = configuracao.precoGasolina,
                        onValorChange = viewModel::atualizarPrecoGasolina,
                        modifier = Modifier.weight(1f),
                    )
                    CampoNumericoCompacto(
                        label = "LITRO ETANOL",
                        valor = configuracao.precoEtanol,
                        onValorChange = viewModel::atualizarPrecoEtanol,
                        modifier = Modifier.weight(1f),
                    )
                }
                SubtituloSecao(texto = "🔒 TROCA DE ÓLEO (ÓLEO E FILTROS)")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CampoFaixaFixa(label = "VALOR", valor = "🔒", modifier = Modifier.weight(1f))
                    CampoFaixaFixa(label = "KILOMETRAGEM", valor = "🔒", modifier = Modifier.weight(1f))
                }
                SubtituloSecao(texto = "🔒 CUSTO ESTIMADO DOS PNEUS")
                Text("DIANTEIRO", color = TextoPrincipal, style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CampoFaixaFixa(label = "VALOR", valor = "🔒", modifier = Modifier.weight(1f))
                    CampoFaixaFixa(label = "RODAGEM", valor = "🔒", modifier = Modifier.weight(1f))
                }
                Text("TRASEIRO", color = TextoPrincipal, style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CampoFaixaFixa(label = "VALOR", valor = "🔒", modifier = Modifier.weight(1f))
                    CampoFaixaFixa(label = "RODAGEM", valor = "🔒", modifier = Modifier.weight(1f))
                }
            }

            else -> {
                val contexto = LocalContext.current
                val localizacaoOk = PermissoesMonitoramento.localizacaoConcedida(contexto)
                val overlayOk = PermissoesMonitoramento.overlayConcedida(contexto)
                val listenerOk = PermissoesMonitoramento.listenerNotificacoesAtivo(contexto)
                SecaoCard(titulo = "APP") {
                    SubtituloSecao(texto = "PERMISSÕES")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        TextButton(
                            onClick = {
                                (contexto as? br.com.gestordriver.MainActivity)?.pedirLocalizacao()
                            },
                        ) {
                            Text(
                                text = if (localizacaoOk) "LOCALIZAÇÃO 🆗" else "LOCALIZAÇÃO ❎",
                                color = when {
                                    localizacaoOk -> DestaqueSelecionado
                                    destacarPermissoes -> Color(0xFFFFCDD2)
                                    else -> TextoAmareloConfig
                                },
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                        TextButton(
                            onClick = {
                                contexto.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            },
                        ) {
                            Text(
                                text = if (listenerOk) "NOTIFICAÇÕES 🆗" else "NOTIFICAÇÕES ❎",
                                color = when {
                                    listenerOk -> DestaqueSelecionado
                                    destacarPermissoes -> Color(0xFFFFCDD2)
                                    else -> TextoAmareloConfig
                                },
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                        TextButton(
                            onClick = {
                                contexto.startActivity(
                                    Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        android.net.Uri.parse("package:${contexto.packageName}"),
                                    ),
                                )
                            },
                        ) {
                            Text(
                                text = if (overlayOk) "SOBREPOR 🆗" else "SOBREPOR ❎",
                                color = when {
                                    overlayOk -> DestaqueSelecionado
                                    destacarPermissoes -> Color(0xFFFFCDD2)
                                    else -> TextoAmareloConfig
                                },
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                    SubtituloSecao(texto = "APPS DE CORRIDA")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        listOf(
                            br.com.gestordriver.notification.Plataforma.UBER to "UBER",
                            br.com.gestordriver.notification.Plataforma.NOVE_NOVE to "99",
                            br.com.gestordriver.notification.Plataforma.INDRIVE to "INDRIVE",
                        ).forEach { (plataforma, titulo) ->
                            val ok = br.com.gestordriver.notification.PlataformasMotorista.instalada(
                                contexto,
                                plataforma,
                            )
                            Text(
                                text = if (ok) "$titulo 🆗" else "$titulo ❎",
                                color = if (ok) DestaqueSelecionado else TextoAmareloConfig,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                    SubtituloSecao(texto = "NAVEGAÇÃO")
                    SeletorNavegacao(
                        selecionado = configuracao.navegacao,
                        onSelecionar = { app ->
                            viewModel.selecionarNavegacao(app)
                            NavegacaoLauncher.abrirAplicativo(contexto, app)
                        },
                    )
                    SubtituloSecao(texto = "CLASSIFICAÇÃO")
                    FaixaClassificacao(
                        titulo = "RUIM",
                        min = configuracao.limiteRuimMin,
                        max = configuracao.limiteRuimMax,
                        minFixo = "MIN",
                        onMinChange = viewModel::atualizarLimiteRuimMin,
                        onMaxChange = viewModel::atualizarLimiteRuimMax,
                    )
                    FaixaClassificacao(
                        titulo = "REGULAR",
                        min = configuracao.limiteRegularMin,
                        max = configuracao.limiteRegularMax,
                        onMinChange = viewModel::atualizarLimiteRegularMin,
                        onMaxChange = viewModel::atualizarLimiteRegularMax,
                    )
                    FaixaClassificacao(
                        titulo = "BOA",
                        min = configuracao.limiteBoaMin,
                        max = configuracao.limiteBoaMax,
                        onMinChange = viewModel::atualizarLimiteBoaMin,
                        onMaxChange = viewModel::atualizarLimiteBoaMax,
                    )
                    FaixaClassificacao(
                        titulo = "ÓTIMA",
                        min = configuracao.limiteOtimaMin,
                        max = configuracao.limiteOtimaMax,
                        maxFixo = "MAX",
                        onMinChange = viewModel::atualizarLimiteOtimaMin,
                        onMaxChange = viewModel::atualizarLimiteOtimaMax,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = {
                    viewModel.cancelar()
                    onVoltar()
                },
            ) {
                Text(
                    text = "CANCELAR",
                    color = TextoSecundario,
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            TextButton(
                onClick = {
                    viewModel.salvar()
                    onVoltar()
                },
            ) {
                Text(
                    text = "SALVAR",
                    color = DestaqueSelecionado,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SecaoCard(
    titulo: String,
    compacto: Boolean = false,
    conteudo: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = titulo,
                color = TextoPrincipal,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = BordaCampo,
                    shape = CardDefaults.shape,
                ),
            colors = CardDefaults.cardColors(
                containerColor = FundoCard,
            ),
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = if (compacto) 4.dp else 8.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(if (compacto) 2.dp else 8.dp),
                content = conteudo,
            )
        }
    }
}

@Composable
private fun SubtituloSecao(texto: String) {
    Text(
        text = texto,
        color = TextoSecundario,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun CampoTexto(
    label: String,
    valor: String,
    onValorChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                text = label,
                color = TextoSecundario,
            )
        },
        singleLine = true,
        colors = coresCampo(),
    )
}

@Composable
private fun CampoNumerico(
    label: String,
    valor: Double,
    onValorChange: (Double) -> Unit,
) {
    var texto by remember(valor) { mutableStateOf(DecimalInput.formatar(valor)) }

    OutlinedTextField(
        value = texto,
        onValueChange = { entrada ->
            if (entrada.isEmpty() || entrada.matches(Regex("^[0-9]*[.,]?[0-9]*$"))) {
                texto = entrada
                DecimalInput.parse(entrada)?.let(onValorChange)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                text = label,
                color = TextoSecundario,
            )
        },
        singleLine = true,
        colors = coresCampo(),
    )
}

@Composable
private fun FaixaClassificacao(
    titulo: String,
    min: Double,
    max: Double,
    onMinChange: (Double) -> Unit,
    onMaxChange: (Double) -> Unit,
    minFixo: String? = null,
    maxFixo: String? = null,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = titulo,
            color = TextoPrincipal,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (minFixo != null) {
                CampoFaixaFixa(
                    label = "MIN",
                    valor = minFixo,
                    modifier = Modifier.weight(1f),
                )
            } else {
                CampoNumericoCompacto(
                    label = "MIN",
                    valor = min,
                    onValorChange = onMinChange,
                    modifier = Modifier.weight(1f),
                    duasCasas = true,
                )
            }
            if (maxFixo != null) {
                CampoFaixaFixa(
                    label = "MAX",
                    valor = maxFixo,
                    modifier = Modifier.weight(1f),
                )
            } else {
                CampoNumericoCompacto(
                    label = "MAX",
                    valor = max,
                    onValorChange = onMaxChange,
                    modifier = Modifier.weight(1f),
                    duasCasas = true,
                )
            }
        }
    }
}

@Composable
private fun CampoTextoCompacto(
    label: String,
    valor: String,
    onValorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        modifier = modifier,
        label = {
            Text(
                text = label,
                color = TextoSecundario,
            )
        },
        singleLine = true,
        colors = coresCampo(),
    )
}

@Composable
private fun CampoFaixaFixa(
    label: String,
    valor: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = {},
        modifier = modifier,
        label = {
            Text(
                text = label,
                color = TextoSecundario,
            )
        },
        enabled = false,
        singleLine = true,
        colors = coresCampo(),
    )
}

@Composable
private fun CampoNumericoCompacto(
    label: String,
    valor: Double,
    onValorChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    duasCasas: Boolean = false,
) {
    var texto by remember(valor) {
        mutableStateOf(
            if (duasCasas) FaixasClassificacao.formatar(valor) else DecimalInput.formatar(valor),
        )
    }

    OutlinedTextField(
        value = texto,
        onValueChange = { entrada ->
            if (entrada.isEmpty() || entrada.matches(Regex("^[0-9]*[.,]?[0-9]*$"))) {
                texto = entrada
                DecimalInput.parse(entrada)?.let(onValorChange)
            }
        },
        modifier = modifier,
        label = {
            Text(
                text = label,
                color = TextoSecundario,
            )
        },
        singleLine = true,
        colors = coresCampo(),
    )
}

@Composable
private fun SeletorCombustivel(
    selecionado: Combustivel,
    onSelecionar: (Combustivel) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OpcaoSelecionavel(
            texto = "Gasolina",
            selecionado = selecionado == Combustivel.GASOLINA,
            onClick = { onSelecionar(Combustivel.GASOLINA) },
            modifier = Modifier.weight(1f),
        )
        OpcaoSelecionavel(
            texto = "Etanol",
            selecionado = selecionado == Combustivel.ETANOL,
            onClick = { onSelecionar(Combustivel.ETANOL) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SeletorNavegacao(
    selecionado: AppNavegacao,
    onSelecionar: (AppNavegacao) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OpcaoSelecionavel(
            texto = "GOOGLE MAPS",
            selecionado = selecionado == AppNavegacao.GOOGLE_MAPS,
            onClick = { onSelecionar(AppNavegacao.GOOGLE_MAPS) },
            modifier = Modifier.weight(1f),
        )
        OpcaoSelecionavel(
            texto = "WAZE",
            selecionado = selecionado == AppNavegacao.WAZE,
            onClick = { onSelecionar(AppNavegacao.WAZE) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun OpcaoSelecionavel(
    texto: String,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .border(
                width = 1.dp,
                color = if (selecionado) DestaqueSelecionado else BordaCampo,
                shape = CardDefaults.shape,
            ),
    ) {
        Text(
            text = texto,
            color = if (selecionado) DestaqueSelecionado else TextoPrincipal,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun coresCampo() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextoPrincipal,
    unfocusedTextColor = TextoPrincipal,
    focusedBorderColor = DestaqueSelecionado,
    unfocusedBorderColor = BordaCampo,
    cursorColor = DestaqueSelecionado,
    focusedLabelColor = TextoSecundario,
    unfocusedLabelColor = TextoSecundario,
)
