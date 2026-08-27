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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.gestordriver.model.AppNavegacao
import br.com.gestordriver.model.Combustivel
import br.com.gestordriver.navigation.NavegacaoLauncher
import br.com.gestordriver.permission.PermissoesMonitoramento

private val FundoCard = Color(0xFF050809)
private val TextoPrincipal = Color.White
private val TextoSecundario = Color(0xFFB8C5D1)
private val BordaCampo = Color(0xFF2B3440)
private val DestaqueSelecionado = Color(0xFF7CB342)

@Composable
fun ConfiguracoesScreen(
    viewModel: ConfiguracoesViewModel,
    onVoltar: () -> Unit,
) {
    val configuracao = viewModel.configuracao
    var aba by remember { mutableIntStateOf(0) }
    val abas = listOf("Veículo", "Classificação", "App")

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
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Configurações",
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
            0 -> SecaoCard(titulo = "Ajustes do veículo") {
                CampoTexto(
                    label = "Marca",
                    valor = configuracao.marcaVeiculo,
                    onValorChange = viewModel::atualizarMarca,
                )
                CampoTexto(
                    label = "Modelo",
                    valor = configuracao.modeloVeiculo,
                    onValorChange = viewModel::atualizarModelo,
                )
                CampoTexto(
                    label = "Versão",
                    valor = configuracao.versaoVeiculo,
                    onValorChange = viewModel::atualizarVersao,
                )
                CampoTexto(
                    label = "Ano",
                    valor = configuracao.anoVeiculo,
                    onValorChange = viewModel::atualizarAno,
                )
                CampoNumerico(
                    label = "Consumo gasolina (km/L)",
                    valor = configuracao.consumoGasolina,
                    onValorChange = viewModel::atualizarConsumoGasolina,
                )
                CampoNumerico(
                    label = "Consumo etanol (km/L)",
                    valor = configuracao.consumoEtanol,
                    onValorChange = viewModel::atualizarConsumoEtanol,
                )
                SubtituloSecao(texto = "Combustível")
                SeletorCombustivel(
                    selecionado = configuracao.combustivel,
                    onSelecionar = viewModel::selecionarCombustivel,
                )
                CampoNumerico(
                    label = "Preço gasolina (R$)",
                    valor = configuracao.precoGasolina,
                    onValorChange = viewModel::atualizarPrecoGasolina,
                )
                CampoNumerico(
                    label = "Preço etanol (R$)",
                    valor = configuracao.precoEtanol,
                    onValorChange = viewModel::atualizarPrecoEtanol,
                )
            }

            1 -> SecaoCard(titulo = "Classificação (R$/km)") {
                FaixaClassificacao(
                    titulo = "Ruim",
                    min = configuracao.limiteRuimMin,
                    max = configuracao.limiteRuimMax,
                    onMinChange = viewModel::atualizarLimiteRuimMin,
                    onMaxChange = viewModel::atualizarLimiteRuimMax,
                )
                FaixaClassificacao(
                    titulo = "Regular",
                    min = configuracao.limiteRegularMin,
                    max = configuracao.limiteRegularMax,
                    onMinChange = viewModel::atualizarLimiteRegularMin,
                    onMaxChange = viewModel::atualizarLimiteRegularMax,
                )
                FaixaClassificacao(
                    titulo = "Boa",
                    min = configuracao.limiteBoaMin,
                    max = configuracao.limiteBoaMax,
                    onMinChange = viewModel::atualizarLimiteBoaMin,
                    onMaxChange = viewModel::atualizarLimiteBoaMax,
                )
                FaixaClassificacao(
                    titulo = "Ótima",
                    min = configuracao.limiteOtimaMin,
                    max = configuracao.limiteOtimaMax,
                    onMinChange = viewModel::atualizarLimiteOtimaMin,
                    onMaxChange = viewModel::atualizarLimiteOtimaMax,
                )
            }

            else -> {
                val contexto = LocalContext.current
                val localizacaoOk = PermissoesMonitoramento.localizacaoConcedida(contexto)
                val overlayOk = PermissoesMonitoramento.overlayConcedida(contexto)
                val listenerOk = PermissoesMonitoramento.listenerNotificacoesAtivo(contexto)
                SecaoCard(titulo = "Ajustes do App") {
                    SubtituloSecao(texto = "Permissões")
                    Text(
                        text = if (overlayOk && listenerOk) {
                            "Permissões de overlay e notificações concedidas."
                        } else {
                            "Conceda overlay e acesso a notificações para monitorar ofertas."
                        },
                        color = TextoSecundario,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    TextButton(
                        onClick = {
                            (contexto as? br.com.gestordriver.MainActivity)?.pedirLocalizacao()
                        },
                    ) {
                        Text(
                            text = if (localizacaoOk) "Localização ✓" else "Permitir localização",
                            color = if (localizacaoOk) DestaqueSelecionado else TextoPrincipal,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TextButton(
                            onClick = {
                                contexto.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            },
                        ) {
                            Text(
                                text = if (listenerOk) "Notificações ✓" else "Notificações",
                                color = if (listenerOk) DestaqueSelecionado else TextoPrincipal,
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
                                text = if (overlayOk) "Overlay ✓" else "Overlay",
                                color = if (overlayOk) DestaqueSelecionado else TextoPrincipal,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    SubtituloSecao(texto = "Navegação")
                    SeletorNavegacao(
                        selecionado = configuracao.navegacao,
                        onSelecionar = viewModel::selecionarNavegacao,
                    )
                    TextButton(
                        onClick = {
                            NavegacaoLauncher.abrirAplicativo(contexto, configuracao.navegacao)
                        },
                    ) {
                        Text(
                            text = if (configuracao.navegacao == AppNavegacao.WAZE) {
                                "Abrir Waze"
                            } else {
                                "Abrir Google Maps"
                            },
                            color = DestaqueSelecionado,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
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
                    viewModel.salvar()
                    onVoltar()
                },
            ) {
                Text(
                    text = "⬅️ Voltar",
                    color = TextoPrincipal,
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
                    text = "💾 Salvar",
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
                    vertical = 8.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
            CampoNumericoCompacto(
                label = "Mín",
                valor = min,
                onValorChange = onMinChange,
                modifier = Modifier.weight(1f),
            )
            CampoNumericoCompacto(
                label = "Máx",
                valor = max,
                onValorChange = onMaxChange,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CampoNumericoCompacto(
    label: String,
    valor: Double,
    onValorChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
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
            texto = "Google Maps",
            selecionado = selecionado == AppNavegacao.GOOGLE_MAPS,
            onClick = { onSelecionar(AppNavegacao.GOOGLE_MAPS) },
            modifier = Modifier.weight(1f),
        )
        OpcaoSelecionavel(
            texto = "Waze",
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
