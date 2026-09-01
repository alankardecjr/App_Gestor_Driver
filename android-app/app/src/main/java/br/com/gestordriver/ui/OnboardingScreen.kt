package br.com.gestordriver.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestordriver.data.ContaVinculo
import br.com.gestordriver.model.OnboardingEtapa
import br.com.gestordriver.model.TipoContaVinculada
import br.com.gestordriver.model.TutorialConteudo
import br.com.gestordriver.permission.PermissoesMonitoramento

private val TextoPrincipal = Color.White
private val TextoSecundario = Color(0xFFB8C5D1)
private val TextoAmarelo = Color(0xFFFFD54F)
private val TextoVerde = Color(0xFF7CB342)
private val Borda = Color(0xFF607D8B)
private val Fundo = Color(0xF2050809)
private val Forma = RoundedCornerShape(10.dp)

@Composable
fun OnboardingHost(
    state: AppState,
    configuracoesViewModel: ConfiguracoesViewModel,
    onAvancarPermissoes: (Boolean, Boolean) -> Unit,
    onContaPronta: () -> Unit,
    onSeguirTutorial: () -> Unit,
    onPularTutorial: () -> Unit,
) {
    when (state.onboardingEtapa) {
        OnboardingEtapa.NENHUMA -> Unit
        OnboardingEtapa.PERMISSOES -> PainelPermissoes(
            temConta = configuracoesViewModel.configuracao.contaTipo != TipoContaVinculada.NENHUMA,
            onAvancar = onAvancarPermissoes,
        )
        OnboardingEtapa.CONTA -> PainelConta(
            viewModel = configuracoesViewModel,
            onPronto = onContaPronta,
        )
        OnboardingEtapa.TUTORIAL -> PainelTutorial(
            passo = state.tutorialPasso,
            onSeguir = onSeguirTutorial,
            onPular = onPularTutorial,
        )
    }
}

@Composable
private fun PainelPermissoes(
    temConta: Boolean,
    onAvancar: (Boolean, Boolean) -> Unit,
) {
    val contexto = LocalContext.current
    val overlayOk = PermissoesMonitoramento.overlayConcedida(contexto)
    val listenerOk = PermissoesMonitoramento.listenerNotificacoesAtivo(contexto)
    val leituraOk = PermissoesMonitoramento.acessibilidadeAtiva(contexto)
    val bateriaOk = PermissoesMonitoramento.bateriaLiberada(contexto)
    val prontas = PermissoesMonitoramento.permissoesIniciaisOk(contexto)
    CaixaOnboarding("Permissões") {
        Text(
            "Toque em cada item e autorize. Sem isso o Gestor não lê a oferta nem fica sobre o mapa.",
            color = TextoSecundario,
            fontSize = 12.sp,
        )
        LinhaStatus("Notificações", listenerOk) {
            contexto.startActivity(PermissoesMonitoramento.intentNotificacoes())
        }
        LinhaStatus("Sobrepor", overlayOk) {
            contexto.startActivity(PermissoesMonitoramento.intentSobrepor(contexto))
        }
        LinhaStatus("Acessib.", leituraOk) {
            contexto.startActivity(PermissoesMonitoramento.intentAcessibilidade())
        }
        LinhaStatus("Bateria", bateriaOk) {
            contexto.startActivity(PermissoesMonitoramento.intentBateria(contexto))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(
                onClick = { onAvancar(prontas, temConta) },
                enabled = prontas,
            ) {
                Text(
                    if (prontas) "Seguir" else "Autorize para seguir",
                    color = if (prontas) TextoVerde else TextoSecundario,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun PainelConta(
    viewModel: ConfiguracoesViewModel,
    onPronto: () -> Unit,
) {
    var dialogoGoogle by remember { mutableStateOf(false) }
    var dialogoEmail by remember { mutableStateOf(false) }
    val seletorGoogle = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
        ContaVinculo.emailDaResposta(resultado.data)?.let {
            viewModel.conectarContaGoogle(it)
            dialogoGoogle = false
        }
    }
    val conectado = viewModel.configuracao.contaTipo != TipoContaVinculada.NENHUMA
    CaixaOnboarding("Conta") {
        Text(
            "Primeiro uso: conecte Google ou e-mail para identificar o motorista nas versões Free e Beta. Nada é enviado para a nuvem agora.",
            color = TextoSecundario,
            fontSize = 12.sp,
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Text(
                text = if (viewModel.configuracao.contaTipo == TipoContaVinculada.GOOGLE) {
                    "Conta google 🆗"
                } else {
                    "Conta google"
                },
                color = TextoAmarelo,
                fontSize = 12.sp,
                modifier = Modifier.clickable { dialogoGoogle = true }.padding(8.dp),
            )
            Text(
                text = if (viewModel.configuracao.contaTipo == TipoContaVinculada.EMAIL) {
                    "Conta email 🆗"
                } else {
                    "Conta email"
                },
                color = TextoAmarelo,
                fontSize = 12.sp,
                modifier = Modifier.clickable { dialogoEmail = true }.padding(8.dp),
            )
        }
        if (dialogoGoogle) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TextButton(onClick = { dialogoGoogle = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
                TextButton(onClick = { seletorGoogle.launch(ContaVinculo.intentEscolherContaGoogle()) }) {
                    Text("Conectar Google", color = TextoAmarelo, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        if (dialogoEmail) {
            var email by remember { mutableStateOf("") }
            CampoEmailOnboarding(email) { email = it }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TextButton(onClick = { dialogoEmail = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
                TextButton(
                    onClick = {
                        if (viewModel.conectarContaEmail(email)) {
                            dialogoEmail = false
                        }
                    },
                ) {
                    Text("Conectar e-mail", color = TextoAmarelo, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        TextButton(
            onClick = onPronto,
            enabled = conectado,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(
                if (conectado) "Seguir" else "Conecte para seguir",
                color = if (conectado) TextoVerde else TextoSecundario,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun CampoEmailOnboarding(valor: String, onChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("E-mail", color = TextoSecundario, fontSize = 12.sp)
        androidx.compose.foundation.text.BasicTextField(
            value = valor,
            onValueChange = onChange,
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(color = TextoPrincipal, fontSize = 13.sp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Borda, Forma)
                .padding(8.dp),
            decorationBox = { inner ->
                if (valor.isBlank()) {
                    Text("e-mail", color = TextoSecundario, fontSize = 13.sp)
                }
                inner()
            },
        )
    }
}

@Composable
private fun PainelTutorial(
    passo: Int,
    onSeguir: () -> Unit,
    onPular: () -> Unit,
) {
    val atual = TutorialConteudo.passos.getOrElse(passo) { TutorialConteudo.passos.last() }
    CaixaOnboarding("${atual.titulo}  ${passo + 1}/${TutorialConteudo.passos.size}") {
        Text(atual.texto, color = TextoSecundario, fontSize = 13.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TextButton(onClick = onPular) {
                Text("Pular", color = TextoSecundario)
            }
            TextButton(onClick = onSeguir) {
                Text(
                    if (passo >= TutorialConteudo.passos.lastIndex) "Iniciar" else "Seguir",
                    color = TextoVerde,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun LinhaStatus(titulo: String, ok: Boolean, onClick: () -> Unit) {
    Text(
        text = if (ok) "$titulo 🆗" else "$titulo ❎",
        color = if (ok) TextoVerde else TextoAmarelo,
        fontSize = 13.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
    )
}

@Composable
private fun CaixaOnboarding(
    titulo: String,
    conteudo: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .border(2.dp, Borda, Forma)
            .background(Fundo, Forma)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(titulo, color = TextoPrincipal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        conteudo()
    }
}
