package br.com.gestordriver.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.gestordriver.model.AppNavegacao
import br.com.gestordriver.model.Combustivel

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

        SecaoCard(titulo = "Ajustes do veículo") {
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

        SecaoCard(titulo = "Ajustes do App") {
            SubtituloSecao(texto = "Permissões")
            Text(
                text = "Acesso a notificações e overlay serão configurados aqui.",
                color = TextoSecundario,
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.height(4.dp))

            SubtituloSecao(texto = "Navegação")
            SeletorNavegacao(
                selecionado = configuracao.navegacao,
                onSelecionar = viewModel::selecionarNavegacao,
            )

            Spacer(modifier = Modifier.height(4.dp))

            SubtituloSecao(texto = "Classificação")
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onVoltar) {
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
    val texto = if (valor == 0.0) "" else formatarNumero(valor)

    OutlinedTextField(
        value = texto,
        onValueChange = { entrada ->
            onValorChange(entrada.replace(',', '.').toDoubleOrNull() ?: 0.0)
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
    val texto = if (valor == 0.0) "" else formatarNumero(valor)

    OutlinedTextField(
        value = texto,
        onValueChange = { entrada ->
            onValorChange(entrada.replace(',', '.').toDoubleOrNull() ?: 0.0)
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

private fun formatarNumero(valor: Double): String {
    return if (valor % 1.0 == 0.0) {
        valor.toInt().toString()
    } else {
        valor.toString().replace('.', ',')
    }
}
