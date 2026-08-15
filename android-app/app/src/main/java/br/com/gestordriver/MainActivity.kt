package br.com.gestordriver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.gestordriver.ui.AppScreen
import br.com.gestordriver.ui.AppViewModel
import br.com.gestordriver.ui.ConfiguracoesViewModel
import br.com.gestordriver.ui.theme.GestorDriverTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: AppViewModel = viewModel()
            val configuracoesViewModel: ConfiguracoesViewModel = viewModel()

            LaunchedEffect(viewModel) {
                viewModel.fecharApp.collect {
                    finish()
                }
            }

            GestorDriverTheme {
                AppScreen(
                    viewModel = viewModel,
                    configuracoesViewModel = configuracoesViewModel,
                )
            }
        }
    }
}
