package br.com.gestordriver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestordriver.ui.theme.LocalPaletaApp

/** Verde ativo compartilhado das telas nativas (rodapé / ações). */
val VerdeAcaoNativa = Color(0xFF2E7D32)
val VerdeAcaoEscuro = Color(0xFF1B5E20)

@Composable
fun CabecalhoTelaNativa(
    titulo: String,
    onVoltar: () -> Unit,
    onSelo: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val paleta = LocalPaletaApp.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "←",
            color = paleta.texto,
            fontSize = 20.sp,
            modifier = Modifier
                .clickable(onClick = onVoltar)
                .padding(horizontal = 8.dp, vertical = 5.dp),
        )
        Text(
            text = titulo,
            color = paleta.texto,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            trailing()
        }
        if (onSelo != null) {
            Icon(
                painter = painterResource(br.com.gestordriver.R.mipmap.ic_launcher_round),
                contentDescription = "Voltar para o selo",
                tint = Color.Unspecified,
                modifier = Modifier
                    .clickable(onClick = onSelo)
                    .padding(horizontal = 8.dp, vertical = 5.dp),
            )
        }
    }
}

@Composable
fun BarraCancelarSalvar(
    onCancelar: () -> Unit,
    onSalvar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val paleta = LocalPaletaApp.current
    val forma = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Cancelar",
            color = paleta.texto,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 44.dp)
                .background(paleta.fundoMetrica, forma)
                .border(1.dp, paleta.borda, forma)
                .clickable(onClick = onCancelar)
                .padding(vertical = 12.dp),
        )
        Text(
            text = "Salvar",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 44.dp)
                .background(VerdeAcaoEscuro, forma)
                .border(1.dp, VerdeAcaoNativa, forma)
                .clickable(onClick = onSalvar)
                .padding(vertical = 12.dp),
        )
    }
}
