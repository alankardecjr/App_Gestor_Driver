package br.com.gestordriver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestordriver.R
import br.com.gestordriver.overlay.AtalhosUi
import br.com.gestordriver.ui.theme.LocalPaletaApp

private data class ItemOpcao(
    val titulo: String,
    val subtitulo: String,
    val iconeRes: Int,
    val cores: AtalhosUi.ItemCores,
    val acao: () -> Unit,
)

/** Converte ARGB Android → Compose. */
private fun Int.toComposeColor(): Color =
    Color(
        alpha = (this ushr 24) and 0xFF,
        red = (this ushr 16) and 0xFF,
        green = (this ushr 8) and 0xFF,
        blue = this and 0xFF,
    )

/**
 * Aba principal do Menu — UI oficial idêntica à tela Atalhos (claro/escuro).
 */
@Composable
fun OpcoesTela(
    planoPro: Boolean,
    onFecharParaSelo: () -> Unit,
    onLocalizacao: () -> Unit,
    onHistorico: () -> Unit,
    onCarteira: () -> Unit,
    onDespesas: () -> Unit,
    onSemaforo: () -> Unit,
    onUsuario: () -> Unit,
    onConfigurar: () -> Unit,
    onFecharApp: () -> Unit,
) {
    val paleta = LocalPaletaApp.current
    val ui = AtalhosUi.de(paleta.escuro)
    val formaItem = RoundedCornerShape(16.dp)
    val formaIcone = RoundedCornerShape(12.dp)
    val itens = listOf(
        ItemOpcao("Localização", "Abrir mapa na posição atual", R.mipmap.ic_launcher_round, ui.historico, onLocalizacao),
        ItemOpcao("Histórico", "Ver corridas aceitas", R.drawable.ic_menu_historico, ui.historico, onHistorico),
        ItemOpcao("Carteira", "Saldo e movimentações", R.drawable.ic_menu_carteira, ui.carteira, onCarteira),
        ItemOpcao("Despesas", "Controle de gastos do app", R.drawable.ic_menu_despesas, ui.despesas, onDespesas),
        ItemOpcao("Semáforo", "Regras de classificação", R.drawable.ic_menu_semaforo, ui.semaforo, onSemaforo),
        ItemOpcao("Usuário", "Seus dados e preferências", R.drawable.ic_menu_usuario, ui.usuario, onUsuario),
        ItemOpcao("Configurar", "Ajustes do aplicativo", R.drawable.ic_menu_configurar, ui.configurar, onConfigurar),
        ItemOpcao("Fechar", "Encerrar o aplicativo", R.drawable.ic_menu_fechar, ui.fechar, onFecharApp),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ui.painel.toComposeColor())
            .padding(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clickable(onClick = onFecharParaSelo),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.mipmap.ic_launcher_round),
                contentDescription = "Voltar para o selo",
                tint = Color.Unspecified,
                modifier = Modifier.size(42.dp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Atalhos",
            color = ui.titulo.toComposeColor(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Acesse rapidamente as principais funções",
            color = ui.subtitulo.toComposeColor(),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itens.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ui.item.toComposeColor(), formaItem)
                        .border(1.dp, ui.itemBorda.toComposeColor(), formaItem)
                        .clickable(onClick = item.acao)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(item.cores.fundoIcone.toComposeColor(), formaIcone),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(item.iconeRes),
                            contentDescription = item.titulo,
                            tint = item.cores.icone.toComposeColor(),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp, end = 8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.titulo,
                                color = ui.titulo.toComposeColor(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (item.titulo == "Carteira" && !planoPro) {
                                Text(
                                    text = " PRO ",
                                    color = Color(0xFF212121),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(start = 6.dp)
                                        .background(Color(0xFFFFD54F), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp),
                                )
                            }
                        }
                        Text(
                            text = item.subtitulo,
                            color = ui.subtitulo.toComposeColor(),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Text(
                        text = "›",
                        color = ui.subtitulo.toComposeColor(),
                        fontSize = 22.sp,
                    )
                }
            }
        }
    }
}
