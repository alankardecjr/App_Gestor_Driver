package br.com.gestordriver.ui

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestordriver.data.ContaVinculo
import br.com.gestordriver.model.OnboardingEtapa
import br.com.gestordriver.model.PlanoAcesso
import br.com.gestordriver.model.TipoContaVinculada
import br.com.gestordriver.model.TutorialConteudo
import br.com.gestordriver.permission.PermissoesMonitoramento
import br.com.gestordriver.ui.theme.LocalPaletaApp

private val Forma = RoundedCornerShape(16.dp)
private val Verde = androidx.compose.ui.graphics.Color(0xFF7CB342)

@Composable
fun OnboardingHost(
    state: AppState,
    configuracoesViewModel: ConfiguracoesViewModel,
    onAvancarPermissoes: (Boolean, Boolean) -> Unit,
    onContaPronta: () -> Unit,
    onLiberarChave: (String) -> Boolean,
    onSeguirTutorial: () -> Unit,
    onPularTutorial: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (state.onboardingEtapa) {
            OnboardingEtapa.NENHUMA -> Unit
            OnboardingEtapa.PERMISSOES -> PainelPermissoes(
                temConta = configuracoesViewModel.configuracao.contaTipo != TipoContaVinculada.NENHUMA,
                onAvancar = onAvancarPermissoes,
            )
            OnboardingEtapa.CONTA -> PainelConta(
                viewModel = configuracoesViewModel,
                plano = state.plano,
                onPronto = onContaPronta,
                onLiberarChave = onLiberarChave,
            )
            OnboardingEtapa.TUTORIAL -> PainelTutorial(
                passo = state.tutorialPasso,
                onSeguir = onSeguirTutorial,
                onPular = onPularTutorial,
            )
        }
    }
}

private enum class PedidoPermissao(val titulo: String, val texto: String) {
    LEITURA(
        "Permitir leitura das notificações?",
        "O Android abre o interruptor só do Gestor Driver. Ative e volte. É assim que o app vê a oferta da Uber, da 99 e da inDrive.",
    ),
    SOBREPOR(
        "Permitir exibir sobre outros apps?",
        "O Android abre o interruptor só do Gestor Driver. Ative e volte. O selo e o cartão passam a ficar sobre o mapa.",
    ),
}

@Composable
private fun PainelPermissoes(
    temConta: Boolean,
    onAvancar: (Boolean, Boolean) -> Unit,
) {
    val contexto = LocalContext.current
    var retomada by remember { mutableIntStateOf(0) }
    val dono = remember(contexto) {
        generateSequence(contexto) { (it as? android.content.ContextWrapper)?.baseContext }
            .filterIsInstance<androidx.lifecycle.LifecycleOwner>()
            .firstOrNull()
    }
    if (dono != null) {
        DisposableEffect(dono) {
            val observador = androidx.lifecycle.LifecycleEventObserver { _, evento ->
                if (evento == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                    retomada++
                }
            }
            dono.lifecycle.addObserver(observador)
            onDispose { dono.lifecycle.removeObserver(observador) }
        }
    }
    val avisoOk = remember(retomada) { PermissoesMonitoramento.avisoDoAppConcedido(contexto) }
    val overlayOk = remember(retomada) { PermissoesMonitoramento.overlayConcedida(contexto) }
    val listenerOk = remember(retomada) { PermissoesMonitoramento.listenerNotificacoesAtivo(contexto) }
    val bateriaOk = remember(retomada) { PermissoesMonitoramento.bateriaLiberada(contexto) }
    val prontas = remember(retomada) { PermissoesMonitoramento.permissoesIniciaisOk(contexto) }
    var pedido by remember { mutableStateOf<PedidoPermissao?>(null) }
    val pedirAviso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (!PermissoesMonitoramento.listenerNotificacoesAtivo(contexto)) {
            pedido = PedidoPermissao.LEITURA
        }
    }
    CaixaOnboarding(
        titulo = "Permissões",
        texto = "Toque no item. O Android abre a caixa de confirmação quando ela existe. Leitura e sobreposição usam um interruptor só deste app.",
    ) {
        LinhaPermissao(
            titulo = "Notificações",
            porque = "A caixa do sistema libera o aviso do Gestor. Em seguida, o interruptor deixa o app ler a oferta.",
            ok = avisoOk && listenerOk,
        ) {
            if (Build.VERSION.SDK_INT >= 33 && !PermissoesMonitoramento.avisoDoAppConcedido(contexto)) {
                pedirAviso.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else if (!PermissoesMonitoramento.listenerNotificacoesAtivo(contexto)) {
                pedido = PedidoPermissao.LEITURA
            }
        }
        LinhaPermissao(
            titulo = "Sobrepor",
            porque = "Confirme para o selo e o cartão aparecerem sobre o mapa.",
            ok = overlayOk,
        ) {
            pedido = PedidoPermissao.SOBREPOR
        }
        LinhaPermissao(
            titulo = "Acessibilidade",
            porque = "Liga só quando você toca em Monitorar e desliga quando para. Com ela desligada, o banco abre.",
            ok = false,
            legenda = "Pedida ao ligar o Monitorar",
            clicavel = false,
            onClick = {},
        )
        LinhaPermissao(
            titulo = "Bateria",
            porque = "O Android mostra a caixa para o app continuar enquanto você dirige.",
            ok = bateriaOk,
        ) {
            contexto.startActivity(PermissoesMonitoramento.intentBateria(contexto))
        }
        val atual = pedido
        if (atual != null) {
            Text(atual.titulo, color = LocalPaletaApp.current.texto, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(atual.texto, color = LocalPaletaApp.current.textoSecundario, fontSize = 14.sp)
            BotoesMensagem(
                textoEsquerda = "Não permitir",
                onEsquerda = { pedido = null },
                textoDireita = "Permitir",
                onDireita = {
                    pedido = null
                    when (atual) {
                        PedidoPermissao.LEITURA ->
                            PermissoesMonitoramento.abrirNotificacoes(contexto)
                        PedidoPermissao.SOBREPOR ->
                            contexto.startActivity(PermissoesMonitoramento.intentSobrepor(contexto))
                    }
                },
            )
        }
        BotoesMensagem(
            textoDireita = if (prontas) "Seguir" else "Autorize para seguir",
            onDireita = { onAvancar(prontas, temConta) },
            mostrarEsquerda = false,
            direitaHabilitada = prontas,
        )
    }
}

@Composable
private fun PainelConta(
    viewModel: ConfiguracoesViewModel,
    plano: PlanoAcesso,
    onPronto: () -> Unit,
    onLiberarChave: (String) -> Boolean,
) {
    val contexto = LocalContext.current
    var email by remember { mutableStateOf(viewModel.configuracao.contaEmail) }
    var chave by remember { mutableStateOf("") }
    var erroEmail by remember { mutableStateOf(false) }
    var erroChave by remember { mutableStateOf(false) }
    val seletorGoogle = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
        ContaVinculo.emailDaResposta(resultado.data)?.let {
            viewModel.conectarContaGoogle(it)
            email = it
            erroEmail = false
        }
    }
    val conectado = viewModel.configuracao.contaTipo != TipoContaVinculada.NENHUMA
    CaixaOnboarding(
        titulo = "Conta e versão Pro",
        texto = "O e-mail identifica o motorista neste celular. A chave GestorDrivePro libera óleo, pneus, seguro, IPVA e a calculadora de abastecimento. Sem a chave, o app fica na versão Free.",
    ) {
        CampoOnboarding("E-mail", email, "nome@email.com") {
            email = it
            erroEmail = false
        }
        if (erroEmail) {
            Text("Informe um e-mail válido.", color = androidx.compose.ui.graphics.Color(0xFFE53935), fontSize = 12.sp)
        }
        if (conectado) {
            Text(
                text = "Conta salva: ${viewModel.configuracao.contaEmail}",
                color = LocalPaletaApp.current.textoSecundario,
                fontSize = 12.sp,
            )
        }
        Text(
            text = "Usar conta Google",
            color = LocalPaletaApp.current.texto,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clickable { seletorGoogle.launch(ContaVinculo.intentEscolherContaGoogle()) }
                .padding(vertical = 4.dp),
        )
        if (plano.ehPro) {
            Text(
                text = "Pro ativo neste celular.",
                color = Verde,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        } else {
            CampoOnboarding("Chave Pro", chave, "GestorDrivePro") {
                chave = it
                erroChave = false
            }
            if (erroChave) {
                Text("Chave inválida.", color = androidx.compose.ui.graphics.Color(0xFFE53935), fontSize = 12.sp)
            }
        }
        BotoesMensagem(
            textoDireita = "Seguir",
            onDireita = {
                val contaOk = if (conectado) {
                    true
                } else if (viewModel.conectarContaEmail(email)) {
                    true
                } else {
                    erroEmail = true
                    false
                }
                if (!contaOk) {
                    return@BotoesMensagem
                }
                val textoChave = chave.trim()
                if (textoChave.isNotEmpty() && !plano.ehPro) {
                    if (!onLiberarChave(textoChave)) {
                        erroChave = true
                        Toast.makeText(contexto, "Chave inválida.", Toast.LENGTH_SHORT).show()
                        return@BotoesMensagem
                    }
                    Toast.makeText(contexto, "Versão Pro liberada.", Toast.LENGTH_SHORT).show()
                }
                onPronto()
            },
            mostrarEsquerda = false,
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
    val ultimo = passo >= TutorialConteudo.passos.lastIndex
    CaixaOnboarding(
        titulo = atual.titulo,
        texto = atual.texto,
        etapa = "${passo + 1} de ${TutorialConteudo.passos.size}",
    ) {
        BotoesMensagem(
            textoEsquerda = "Pular",
            onEsquerda = onPular,
            textoDireita = if (ultimo) "Começar" else "Seguir",
            onDireita = onSeguir,
        )
    }
}

@Composable
private fun LinhaPermissao(
    titulo: String,
    porque: String,
    ok: Boolean,
    legenda: String = if (ok) "Liberada" else "Toque para autorizar",
    clicavel: Boolean = true,
    onClick: () -> Unit,
) {
    val paleta = LocalPaletaApp.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (ok) Verde.copy(alpha = 0.45f) else paleta.borda, RoundedCornerShape(12.dp))
            .background(if (ok) Verde.copy(alpha = 0.12f) else paleta.pocoIcone, RoundedCornerShape(12.dp))
            .then(if (clicavel) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(titulo, color = paleta.texto, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(porque, color = paleta.textoSecundario, fontSize = 12.sp)
        Text(
            text = legenda,
            color = if (ok) Verde else paleta.texto,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun CampoOnboarding(
    rotulo: String,
    valor: String,
    dica: String,
    onChange: (String) -> Unit,
) {
    val paleta = LocalPaletaApp.current
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(rotulo, color = paleta.textoSecundario, fontSize = 12.sp)
        BasicTextField(
            value = valor,
            onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(color = paleta.texto, fontSize = 14.sp),
            cursorBrush = SolidColor(Verde),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, paleta.borda, RoundedCornerShape(12.dp))
                .background(paleta.fundo, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 12.dp),
            decorationBox = { inner ->
                if (valor.isBlank()) {
                    Text(dica, color = paleta.textoSecundario, fontSize = 14.sp)
                }
                inner()
            },
        )
    }
}

@Composable
private fun CaixaOnboarding(
    titulo: String,
    texto: String,
    etapa: String? = null,
    conteudo: @Composable ColumnScope.() -> Unit,
) {
    val paleta = LocalPaletaApp.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, paleta.borda, Forma)
            .background(paleta.fundoPainel, Forma)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(titulo, color = paleta.texto, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        if (etapa != null) {
            Text(etapa, color = paleta.textoSecundario, fontSize = 12.sp)
        }
        Text(
            texto,
            color = paleta.textoSecundario,
            fontSize = 14.sp,
            textAlign = TextAlign.Start,
        )
        conteudo()
    }
}
