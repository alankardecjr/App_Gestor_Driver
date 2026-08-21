package br.com.gestordriver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.gestordriver.ui.AppScreen
import br.com.gestordriver.ui.AppViewModel
import br.com.gestordriver.ui.ConfiguracoesViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)

        setContent {

            val appViewModel: AppViewModel =
                viewModel()

            val configuracoesViewModel: ConfiguracoesViewModel =
                viewModel()

            AppScreen(
                viewModel = appViewModel,
                configuracoesViewModel = configuracoesViewModel,
            )
        }
    }
}