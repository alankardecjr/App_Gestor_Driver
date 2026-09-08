package br.com.gestordriver.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun GestorDriverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val paleta = PaletaApp.de(darkTheme)
    val colorScheme = if (paleta.escuro) {
        darkColorScheme(
            primary = Color(0xFF4D9183),
            onPrimary = Color.White,
            primaryContainer = paleta.fundoMetrica,
            onPrimaryContainer = paleta.texto,
            secondary = Color(0xFF4D8796),
            onSecondary = Color.White,
            tertiary = Color(0xFFA98452),
            onTertiary = Color.White,
            background = paleta.fundo,
            onBackground = paleta.texto,
            surface = paleta.fundoPainel,
            onSurface = paleta.texto,
            surfaceVariant = paleta.fundoCardHistorico,
            onSurfaceVariant = paleta.textoDetalhes,
            outline = paleta.borda,
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF3B776B),
            onPrimary = Color.White,
            primaryContainer = paleta.fundoMetrica,
            onPrimaryContainer = paleta.texto,
            secondary = Color(0xFF3C7180),
            onSecondary = Color.White,
            tertiary = Color(0xFF8A6A42),
            onTertiary = Color.White,
            background = paleta.fundo,
            onBackground = paleta.texto,
            surface = paleta.fundoPainel,
            onSurface = paleta.texto,
            surfaceVariant = paleta.fundoCardHistorico,
            onSurfaceVariant = paleta.textoDetalhes,
            outline = paleta.borda,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
