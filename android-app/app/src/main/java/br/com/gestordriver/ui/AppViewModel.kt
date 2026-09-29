package br.com.gestordriver.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestordriver.core.AnaliseCorrida
import br.com.gestordriver.core.CalendarioApp
import br.com.gestordriver.core.CalendarioPeriodo
import br.com.gestordriver.core.ClassificacaoConstantes
import br.com.gestordriver.core.SemaforoOferta
import br.com.gestordriver.data.ConfiguracaoStore
import br.com.gestordriver.data.HistoricoRepository
import br.com.gestordriver.data.LicencaStore
import br.com.gestordriver.data.MemoriaConfiguracaoStore
import br.com.gestordriver.data.MemoriaHistoricoRepository
import br.com.gestordriver.data.MemoriaLicencaStore
import br.com.gestordriver.data.MemoriaOnboardingStore
import br.com.gestordriver.data.OnboardingStore
import br.com.gestordriver.data.chaveHistorico
import br.com.gestordriver.model.ChavePro
import br.com.gestordriver.model.HistoricoItemPresentation
import br.com.gestordriver.model.ModoApresentacao
import br.com.gestordriver.model.OnboardingEtapa
import br.com.gestordriver.model.PlanoAcesso
import br.com.gestordriver.model.TutorialConteudo
import br.com.gestordriver.notification.RideNotificationBus
import br.com.gestordriver.notification.SessaoMonitoramento
import br.com.gestordriver.notification.RideNotificationEvent
import br.com.gestordriver.overlay.OverlayAcao
import br.com.gestordriver.overlay.OverlayBridge
import br.com.gestordriver.overlay.OverlayHistoricoItem
import br.com.gestordriver.overlay.OverlaySnapshot
import br.com.gestordriver.presentation.PresentationBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AppViewModel(
    private val historicoRepository: HistoricoRepository = MemoriaHistoricoRepository(),
    private val onboardingStore: OnboardingStore = MemoriaOnboardingStore(inicial = true),
    private val configuracaoStore: ConfiguracaoStore = MemoriaConfiguracaoStore(),
    private val licencaStore: LicencaStore = MemoriaLicencaStore(),
    coroutineScope: CoroutineScope? = null,
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: viewModelScope
    private var compactaTemporariaJob: Job? = null
    private var estadoAntesFechar: AppState? = null
    private var estadoAntesLimparHistorico: AppState? = null
    private var ofertaParaHistorico: AnaliseCorrida? = null
    /** Período/dia do Dashboard — o Histórico não sobrescreve. */
    private var calendarioPeriodoAntesHistorico: CalendarioPeriodo? = null
    private var historicoDiaAntesHistorico: java.time.LocalDate? = null

    // ================================================================
    // ESTADO
    // ================================================================

    var state by mutableStateOf(
        PresentationBuilder.criarEstadoInicial(
            plano = ChavePro.plano(licencaStore.proLiberado()),
        ),
    )
        private set

    // ================================================================
    // EVENTO DE FECHAMENTO
    // ================================================================

    private val _fecharApp =
        MutableSharedFlow<Unit>(
            extraBufferCapacity = 1
        )

    val fecharApp: SharedFlow<Unit> =
        _fecharApp.asSharedFlow()

    private val _irParaSegundoPlano =
        MutableSharedFlow<Unit>(
            extraBufferCapacity = 1,
        )

    val irParaSegundoPlano: SharedFlow<Unit> =
        _irParaSegundoPlano.asSharedFlow()

    private val _irParaFrente =
        MutableSharedFlow<Unit>(
            extraBufferCapacity = 1,
        )

    val irParaFrente: SharedFlow<Unit> =
        _irParaFrente.asSharedFlow()

    private val _mostrarOpcoes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val mostrarOpcoes: SharedFlow<Unit> = _mostrarOpcoes.asSharedFlow()

    private val _trazerJanela = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val trazerJanela: SharedFlow<Unit> = _trazerJanela.asSharedFlow()

    private val _pedirLocalizacao = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val pedirLocalizacao: SharedFlow<Unit> = _pedirLocalizacao.asSharedFlow()

    /** Activity visível. Fechar no menu confirma em Opções, sem a tela expandida antiga. */
    private var menuNaFrente = false

    /** O usuário saiu da tela do menu. Sozinho não desliga o monitoramento. */
    private var menuEncerrado = false

    // ================================================================
    // NOTIFICAÇÕES
    // ================================================================

    init {
        scope.launch {
            RideNotificationBus.events.collect { evento ->

                when (evento) {

                    // ------------------------------------------------
                    // NOVA OFERTA
                    //
                    // A corrida vai somente para a área de corrida
                    // atual.
                    //
                    // NÃO entra no histórico.
                    // ------------------------------------------------

                    is RideNotificationEvent.CorridaRecebida -> {
                        aplicarNovaCorrida(
                            evento.analise
                        )
                        if (evento.aceiteImediato) {
                            registrarAceiteCorrida()
                        }
                    }

                    // ------------------------------------------------
                    // ACEITE
                    //
                    // Este evento somente será publicado quando o
                    // mecanismo de detecção confirmar o aceite.
                    // ------------------------------------------------

                    RideNotificationEvent.CorridaAceita -> {
                        registrarAceiteCorrida()
                    }

                    // ------------------------------------------------
                    // NOTIFICAÇÃO DESCONHECIDA
                    // ------------------------------------------------

                    RideNotificationEvent.CorridaExpirada -> {
                        expirarOfertaAtual()
                    }

                    RideNotificationEvent.NotificacaoNaoReconhecida -> {
                        // Nenhuma alteração.
                    }
                }
            }
        }

        scope.launch {
            OverlayBridge.acoes.collect { acao ->
                when (acao) {
                    is OverlayAcao.Reabrir -> reabrirInterface(acao.origemCompacta)
                    is OverlayAcao.MoverSelo -> atualizarPosicaoSelo(acao.offsetX, acao.offsetY)
                    OverlayAcao.AbrirHistorico -> abrirHistoricoPeloOverlay()
                    OverlayAcao.AbrirConfig -> alternarConfiguracoes()
                    OverlayAcao.SalvarConfig -> fecharConfiguracoes()
                    OverlayAcao.CancelarConfig -> voltarParaOpcoes()
                    OverlayAcao.Ocultar -> ocultarInterface()
                    OverlayAcao.Retratil -> retrairParaCompactaTemporaria()
                    OverlayAcao.ToqueForaDaCompacta -> Unit
                    OverlayAcao.RecolherParaSelo -> {
                        if (state.atalhosAbertos) {
                            state = state.copy(atalhosAbertos = false)
                            publicarOverlay()
                        } else {
                            recolherPorBarraSistema()
                        }
                    }
                    OverlayAcao.VoltarAtalho -> voltarParaOpcoes()
                    OverlayAcao.VoltarBarra -> voltarPelaBarra()
                    OverlayAcao.RecentesBarra -> aoAbrirRecentes()
                    OverlayAcao.EsconderSelo -> esconderSeloManterMonitor()
                    OverlayAcao.AlternarAtalhos -> alternarAtalhosPeloSelo()
                    OverlayAcao.SairParaMapaHistorico -> sairParaMapaHistorico()
                    OverlayAcao.DashboardPro -> abrirDashboard()
                    OverlayAcao.FecharDashboard -> voltarParaOpcoes()
                    is OverlayAcao.AbrirAtalhoConfig -> abrirAtalhoConfig(acao.indice)
                    OverlayAcao.Fechar -> solicitarFecharApp()
                    OverlayAcao.CancelarFechar -> cancelarFecharApp()
                    OverlayAcao.ConfirmarFechar -> confirmarFecharApp()
                    OverlayAcao.DesativarMonitoramento -> desativarMonitoramento()
                    OverlayAcao.AtivarMonitoramento -> ativarMonitoramento()
                    OverlayAcao.SolicitarMonitoramento -> solicitarAlternarMonitoramento()
                    OverlayAcao.ConfirmarMonitoramento -> confirmarAlternarMonitoramento()
                    OverlayAcao.CancelarMonitoramento -> cancelarDesativarMonitoramento()
                    OverlayAcao.PedirLocalizacao -> _pedirLocalizacao.tryEmit(Unit)
                    OverlayAcao.FecharCompacta -> fecharCompactaOferta()
                    OverlayAcao.FecharNotificacao -> fecharNotificacao()
                    OverlayAcao.SolicitarLimparHistorico -> solicitarLimparHistorico()
                    OverlayAcao.CancelarLimparHistorico -> cancelarLimparHistorico()
                    OverlayAcao.ConfirmarLimparHistorico -> confirmarLimparHistorico()
                    is OverlayAcao.SelecionarHistorico -> selecionarHistoricoPorChave(acao.chave)
                    is OverlayAcao.AbaHistorico -> selecionarAbaHistorico(acao.aba)
                    is OverlayAcao.HistoricoDia -> selecionarDiaHistorico(acao.epochDay)
                    is OverlayAcao.HistoricoSemana -> avancarSemanaHistorico(acao.deltaSemanas)
                    is OverlayAcao.HistoricoMes -> avancarMesHistorico(acao.deltaMeses)
                    is OverlayAcao.HistoricoModo -> selecionarPeriodoHistorico(acao.periodo)
                    is OverlayAcao.HistoricoAvancar -> avancarPeriodoHistorico(acao.delta)
                    is OverlayAcao.AbaConfiguracao -> {
                        state = state.copy(
                            abaConfiguracao = acao.indice.coerceIn(0, 3),
                            historicoVisivel = false,
                            dashboardVisivel = false,
                            configuracoesVisivel = true,
                        )
                        publicarOverlay()
                    }
                }
            }
        }

        val historicoPersistido = historicoRepository.listar()
            .sortedByDescending { it.dataHoraRegistro ?: java.time.LocalDateTime.MIN }
        if (historicoPersistido.isNotEmpty()) {
            state = state.copy(
                historico = historicoPersistido,
                ultimaCorridaAceita = historicoPersistido.first().paraAnalise(),
            )
            publicarOverlay()
        }
        if (onboardingStore.concluido() && SessaoMonitoramento.ligada()) {
            val naLixeira = SessaoMonitoramento.seloNaLixeira()
            state = state.copy(
                monitorando = true,
                overlayAtivo = true,
                notificacaoFechada = false,
                seloFlutuante = !naLixeira,
                seloEscondido = naLixeira,
                interfaceOculta = false,
                configuracoesVisivel = true,
                abaConfiguracao = -1,
            )
            publicarOverlay()
        }
    }

    fun iniciarMonitoramento() {
        if (state.onboardingEtapa != OnboardingEtapa.NENHUMA) {
            return
        }
        if (state.monitorando) {
            publicarOverlay()
            if (state.interfaceOculta) {
                _irParaSegundoPlano.tryEmit(Unit)
            }
            return
        }
        SessaoMonitoramento.definir(true)
        SessaoMonitoramento.definirSeloNaLixeira(false)
        state = state.copy(notificacaoFechada = false, seloEscondido = false)
        irParaSelo(enviarParaFundo = false)
    }

    /** Fecha só o aviso da barra. O monitoramento e o selo continuam. */
    fun fecharNotificacao() {
        if (!state.monitorando || state.notificacaoFechada) {
            return
        }
        state = state.copy(notificacaoFechada = true)
        publicarOverlay()
        avaliarEncerramentoTotal()
    }

    fun avaliarInicio(permissoesOk: Boolean, temConta: Boolean) {
        if (onboardingStore.concluido()) {
            if (state.onboardingEtapa != OnboardingEtapa.NENHUMA) {
                concluirOnboarding()
                return
            }
            // Só a primeira abertura cai em Opções. Home, Recentes e qualquer
            // retomada mantêm a aba que já estava na tela.
            val jaTemTela = state.historicoVisivel ||
                state.dashboardVisivel ||
                state.configuracoesVisivel
            if (!jaTemTela) {
                abrirMenuOpcoes()
            }
            return
        }
        val etapa = when {
            !permissoesOk -> OnboardingEtapa.PERMISSOES
            !temConta -> OnboardingEtapa.CONTA
            else -> OnboardingEtapa.TUTORIAL
        }
        if (state.onboardingEtapa == OnboardingEtapa.TUTORIAL && etapa == OnboardingEtapa.TUTORIAL) {
            return
        }
        state = state.copy(
            onboardingEtapa = etapa,
            tutorialPasso = if (etapa == OnboardingEtapa.TUTORIAL) state.tutorialPasso else 0,
            interfaceOculta = false,
            destacarPermissoes = etapa == OnboardingEtapa.PERMISSOES,
            monitorando = false,
            abaConfiguracao = if (etapa == OnboardingEtapa.PERMISSOES) 3 else state.abaConfiguracao,
        )
    }

    fun tutorialSeguir() {
        val proximo = state.tutorialPasso + 1
        if (proximo >= TutorialConteudo.passos.size) {
            concluirOnboarding()
            return
        }
        state = state.copy(tutorialPasso = proximo)
    }

    fun tutorialPular() {
        concluirOnboarding()
    }

    fun onboardingContaPronta() {
        state = state.copy(
            onboardingEtapa = OnboardingEtapa.TUTORIAL,
            tutorialPasso = 0,
            interfaceOculta = false,
        )
    }

    private fun concluirOnboarding() {
        onboardingStore.marcarConcluido()
        SessaoMonitoramento.definir(false)
        // Após o onboarding o app abre em Opções com o monitoramento DESLIGADO;
        // o usuário liga quando quiser (esquema ON/OFF).
        state = state.copy(
            onboardingEtapa = OnboardingEtapa.NENHUMA,
            tutorialPasso = 0,
            destacarPermissoes = false,
            interfaceOculta = false,
            configuracoesVisivel = true,
            abaConfiguracao = -1,
            monitorando = false,
        )
    }

    // ================================================================
    // MONITORAMENTO ON/OFF
    // ================================================================

    /**
     * Seta do cabeçalho: qualquer tela volta para a principal, na aba Opções.
     * Se o painel estava no overlay, traz o app para a frente.
     */
    fun voltarParaOpcoes() {
        val trazerApp = state.interfaceOculta
        abrirMenuOpcoes()
        if (trazerApp) {
            _mostrarOpcoes.tryEmit(Unit)
        }
    }

    /** Ícone do app: menu na aba Opções, sem abrir atalhos nem o selo. */
    fun abrirMenuOpcoes() {
        cancelarCompactaTemporaria()
        state = state.copy(
            interfaceOculta = false,
            configuracoesVisivel = true,
            historicoVisivel = false,
            dashboardVisivel = false,
            seloFlutuante = state.monitorando && !state.seloEscondido,
            atalhosAbertos = false,
            abaConfiguracao = -1,
            confirmacaoFecharVisivel = false,
            confirmacaoDesativarVisivel = false,
        )
        publicarOverlay()
    }

    /** Home e Recentes: sai do app e mantém a aba que estava aberta. */
    fun exibirOpcoesNosRecentes() {
        if (state.onboardingEtapa != OnboardingEtapa.NENHUMA) {
            return
        }
        menuNaFrente = false
        menuEncerrado = true
        if (state.monitorando && state.notificacaoFechada && state.seloEscondido) {
            desativarMonitoramento()
            return
        }
        if (state.atalhosAbertos) {
            state = state.copy(atalhosAbertos = false)
        }
        publicarOverlay()
    }

    fun definirAbaConfiguracao(indice: Int) {
        state = state.copy(
            abaConfiguracao = indice,
            configuracoesVisivel = true,
            historicoVisivel = false,
            dashboardVisivel = false,
            interfaceOculta = false,
        )
        publicarOverlay()
    }

    fun abrirHistoricoNoMenu() {
        state = state.copy(
            interfaceOculta = false,
            historicoVisivel = true,
            configuracoesVisivel = false,
            dashboardVisivel = false,
            atalhosAbertos = false,
            estadoSalvo = null,
            corrida = state.corrida.copy(modo = ModoApresentacao.DETALHES),
        )
        publicarOverlay()
    }

    fun abrirCarteiraNoMenu() {
        state = state.copy(
            interfaceOculta = false,
            dashboardVisivel = true,
            configuracoesVisivel = false,
            historicoVisivel = false,
            estadoSalvo = null,
        )
        publicarOverlay()
    }

    /** Liga o monitoramento (ação explícita do usuário na aba Opções). */
    fun ativarMonitoramento() {
        iniciarMonitoramento()
    }

    /**
     * X no topo da compacta: descarta a oferta atual e volta ao selo.
     * Recusa não grava histórico e mantém o monitoramento ligado.
     */
    fun fecharCompactaOferta() {
        if (!state.monitorando) {
            return
        }
        cancelarCompactaTemporaria()
        state = state.copy(ofertaAtiva = false, analiseAtual = null)
        irParaSelo()
    }

    /** Abre a confirmação antes de ligar ou desligar o monitoramento. */
    fun solicitarAlternarMonitoramento() {
        state = state.copy(confirmacaoDesativarVisivel = true)
    }

    fun confirmarAlternarMonitoramento() {
        if (state.monitorando) {
            confirmarDesativarMonitoramento()
        } else {
            state = state.copy(confirmacaoDesativarVisivel = false)
            ativarMonitoramento()
        }
    }

    /** Abre a confirmação antes de desligar o monitoramento. */
    fun solicitarDesativarMonitoramento() {
        if (!state.monitorando) {
            return
        }
        state = state.copy(confirmacaoDesativarVisivel = true)
    }

    fun cancelarDesativarMonitoramento() {
        state = state.copy(confirmacaoDesativarVisivel = false)
    }

    fun confirmarDesativarMonitoramento() {
        desativarMonitoramento()
    }

    /**
     * Desliga o monitoramento sem encerrar o app: para o serviço/overlay e
     * volta para a interface (Opções). Diferente de "Fechar app".
     */
    fun desativarMonitoramento() {
        SessaoMonitoramento.definir(false)
        cancelarCompactaTemporaria()
        state = state.copy(
            confirmacaoDesativarVisivel = false,
            monitorando = false,
            overlayAtivo = false,
            seloFlutuante = false,
            seloEscondido = false,
            atalhosAbertos = false,
            compactaTemporaria = false,
            interfaceOculta = false,
            historicoVisivel = false,
            dashboardVisivel = false,
            configuracoesVisivel = true,
            abaConfiguracao = -1,
            confirmacaoFecharVisivel = false,
        )
        publicarOverlay()
    }

    // ================================================================
    // PLANO
    // ================================================================

    fun liberarComChave(texto: String): Boolean {
        if (!ChavePro.aceita(texto)) {
            return false
        }
        licencaStore.liberarPro()
        selecionarPlano(PlanoAcesso.PRO)
        return true
    }

    fun selecionarPlano(
        plano: PlanoAcesso
    ) {

        val analise = state.analiseAtual ?: state.ultimaCorridaAceita
        state = PresentationBuilder.criarEstado(
            analise = analise,
            plano = plano,
            historico = state.historico,
            historicoSelecionado = state.historicoSelecionado,
            abaHistorico = state.abaHistorico,
            abaConfiguracao = state.abaConfiguracao,
            destacarPermissoes = state.destacarPermissoes,
            modo = state.corrida.modo,
            historicoVisivel = state.historicoVisivel,
            configuracoesVisivel = state.configuracoesVisivel,
            interfaceOculta = state.interfaceOculta,
            overlayAtivo = state.overlayAtivo,
            notificacaoDisponivel = state.notificacaoDisponivel,
            seloFlutuante = state.seloFlutuante,
            monitorando = state.monitorando,
            seloOffsetX = state.seloOffsetX,
            seloOffsetY = state.seloOffsetY,
            seloEscondido = state.seloEscondido,
            estadoSalvo = state.estadoSalvo,
            corridaAceita = state.corridaAceita,
            ultimaCorridaAceita = state.ultimaCorridaAceita,
            ofertaAtiva = state.ofertaAtiva,
            compactaTemporaria = state.compactaTemporaria,
            corridaAntesDaOferta = state.corridaAntesDaOferta,
            ofertasPendentes = state.ofertasPendentes,
            onboardingEtapa = state.onboardingEtapa,
            tutorialPasso = state.tutorialPasso,
        )
        publicarOverlay()
    }

    // ================================================================
    // EXPANDIR / RETRAIR
    // ================================================================

    fun alternarDetalhes() {
        if (state.corrida.modo == ModoApresentacao.DETALHES) {
            retrairParaCompactaTemporaria()
            return
        }
        state = state.copy(
            corrida = state.corrida.copy(
                modo = ModoApresentacao.DETALHES,
                acaoDetalhes = "Menos detalhes",
            ),
            compactaTemporaria = false,
        )
    }

    // ================================================================
    // HISTÓRICO
    // ================================================================

    fun alternarHistorico() {
        if (state.historicoVisivel) {
            fecharHistoricoELimpar()
            return
        }
        calendarioPeriodoAntesHistorico = state.calendarioPeriodo
        historicoDiaAntesHistorico = state.historicoDia
        state = state.copy(
            historicoVisivel = true,
            configuracoesVisivel = false,
            dashboardVisivel = false,
            interfaceOculta = false,
            seloFlutuante = false,
            abaHistorico = "Todos",
            historicoDia = CalendarioApp.hoje(),
            corrida = state.corrida.copy(
                modo = ModoApresentacao.DETALHES,
                acaoDetalhes = "Menos detalhes",
            ),
        )
        publicarOverlay()
    }

    private fun fecharHistoricoELimpar() {
        val manterOferta = state.ofertaAtiva
        val analise = when {
            manterOferta -> state.analiseAtual
            state.corridaAceita -> state.ultimaCorridaAceita
            else -> null
        }
        val periodoDashboard = calendarioPeriodoAntesHistorico ?: state.calendarioPeriodo
        val diaDashboard = historicoDiaAntesHistorico ?: state.historicoDia
        calendarioPeriodoAntesHistorico = null
        historicoDiaAntesHistorico = null
        state = PresentationBuilder.criarEstado(
            analise = analise,
            plano = state.plano,
            historico = state.historico,
            historicoSelecionado = null,
            abaHistorico = state.abaHistorico,
            abaConfiguracao = state.abaConfiguracao,
            destacarPermissoes = false,
            modo = ModoApresentacao.DETALHES,
            historicoVisivel = false,
            configuracoesVisivel = false,
            interfaceOculta = true,
            overlayAtivo = true,
            notificacaoDisponivel = manterOferta || state.corridaAceita,
            seloFlutuante = false,
            compactaTemporaria = false,
            monitorando = true,
            seloOffsetX = state.seloOffsetX,
            seloOffsetY = state.seloOffsetY,
            seloEscondido = state.seloEscondido,
            estadoSalvo = state.estadoSalvo,
            corridaAceita = if (manterOferta) false else state.corridaAceita,
            ultimaCorridaAceita = state.ultimaCorridaAceita,
            ofertaAtiva = manterOferta,
            corridaAntesDaOferta = null,
            ofertasPendentes = state.ofertasPendentes,
            onboardingEtapa = state.onboardingEtapa,
            tutorialPasso = state.tutorialPasso,
        ).copy(
            calendarioPeriodo = periodoDashboard,
            historicoDia = diaDashboard,
            dashboardVisivel = false,
        )
        publicarOverlay()
        _irParaSegundoPlano.tryEmit(Unit)
    }

    // ================================================================
    // CONFIGURAÇÕES
    // ================================================================

    fun alternarConfiguracoes(destaquePermissao: Boolean = false, usarOverlay: Boolean = true) {
        if (state.configuracoesVisivel && !destaquePermissao) {
            fecharConfiguracoes()
            return
        }
        abrirConfiguracoes(destaquePermissao, usarOverlay)
    }

    fun abrirConfiguracoes(destaquePermissao: Boolean = false, @Suppress("UNUSED_PARAMETER") usarOverlay: Boolean = true) {
        val manterOferta = state.ofertaAtiva
        val analise = if (manterOferta) state.analiseAtual else null
        state = PresentationBuilder.criarEstado(
            analise = analise,
            plano = state.plano,
            historico = state.historico,
            historicoSelecionado = null,
            abaHistorico = state.abaHistorico,
                    abaConfiguracao = if (destaquePermissao) 3 else 0,
            destacarPermissoes = destaquePermissao,
            modo = ModoApresentacao.DETALHES,
            historicoVisivel = false,
            configuracoesVisivel = true,
            interfaceOculta = false,
            overlayAtivo = true,
            notificacaoDisponivel = manterOferta,
            seloFlutuante = false,
            compactaTemporaria = false,
            monitorando = true,
            seloOffsetX = state.seloOffsetX,
            seloOffsetY = state.seloOffsetY,
            seloEscondido = state.seloEscondido,
            estadoSalvo = state.estadoSalvo,
            corridaAceita = false,
            ultimaCorridaAceita = state.ultimaCorridaAceita,
            ofertaAtiva = manterOferta,
            corridaAntesDaOferta = null,
            ofertasPendentes = state.ofertasPendentes,
            onboardingEtapa = state.onboardingEtapa,
            tutorialPasso = state.tutorialPasso,
        ).copy(dashboardVisivel = false)
        publicarOverlay()
        _trazerJanela.tryEmit(Unit)
    }

    fun abrirDashboard() {
        state = state.copy(
            dashboardVisivel = true,
            historicoVisivel = false,
            configuracoesVisivel = false,
            atalhosAbertos = false,
            confirmacaoFecharVisivel = false,
            confirmacaoLimparHistoricoVisivel = false,
            seloFlutuante = false,
            interfaceOculta = false,
            estadoSalvo = null,
            overlayAtivo = true,
            historicoDia = CalendarioApp.hoje(),
            calendarioPeriodo = CalendarioPeriodo.DIA,
            corrida = state.corrida.copy(modo = ModoApresentacao.DETALHES),
        )
        publicarOverlay()
        _trazerJanela.tryEmit(Unit)
    }

    fun fecharDashboard() {
        state = state.copy(
            dashboardVisivel = false,
            seloFlutuante = false,
            interfaceOculta = true,
            corrida = state.corrida.copy(modo = ModoApresentacao.DETALHES),
        )
        publicarOverlay()
        _irParaSegundoPlano.tryEmit(Unit)
    }

    fun abrirHistoricoPeloOverlay() {
        abrirHistoricoNoMenu()
        _trazerJanela.tryEmit(Unit)
    }

    fun fecharConfiguracoes() {
        state = state.copy(
            configuracoesVisivel = false,
            destacarPermissoes = false,
            interfaceOculta = true,
            seloFlutuante = false,
            corrida = state.corrida.copy(modo = ModoApresentacao.DETALHES),
        )
        publicarOverlay()
        _irParaSegundoPlano.tryEmit(Unit)
    }

    /** Volta da tela menu (abas) para o menu atalho ao lado do selo. */
    fun voltarParaAtalho() {
        state = state.copy(
            historicoVisivel = false,
            configuracoesVisivel = false,
            dashboardVisivel = false,
            confirmacaoFecharVisivel = false,
            confirmacaoLimparHistoricoVisivel = false,
            destacarPermissoes = false,
            interfaceOculta = true,
            seloFlutuante = false,
            compactaTemporaria = false,
            overlayAtivo = true,
            corrida = state.corrida.copy(modo = ModoApresentacao.DETALHES),
        )
        publicarOverlay()
        _irParaSegundoPlano.tryEmit(Unit)
    }

    // ================================================================
    // HISTÓRICO — SELEÇÃO
    // ================================================================

    fun selecionarHistorico(
        item: HistoricoItemPresentation
    ) {
        val mostrarAgora = !state.ofertaAtiva
        val analise = if (mostrarAgora) item.paraAnalise() else state.analiseAtual
        state = PresentationBuilder.criarEstado(
            analise = analise,
            plano = state.plano,
            historico = state.historico,
            historicoSelecionado = item,
            historicoChavesSelecionadas = setOf(item.chaveHistorico()),
            abaHistorico = state.abaHistorico,
            abaConfiguracao = state.abaConfiguracao,
            destacarPermissoes = false,
            modo = ModoApresentacao.DETALHES,
            historicoVisivel = true,
            configuracoesVisivel = false,
            interfaceOculta = false,
            overlayAtivo = true,
            notificacaoDisponivel = analise != null,
            seloFlutuante = false,
            compactaTemporaria = false,
            monitorando = true,
            seloOffsetX = state.seloOffsetX,
            seloOffsetY = state.seloOffsetY,
            seloEscondido = state.seloEscondido,
            estadoSalvo = state.estadoSalvo,
            corridaAceita = !state.ofertaAtiva,
            ultimaCorridaAceita = state.ultimaCorridaAceita,
            ofertaAtiva = state.ofertaAtiva,
            corridaAntesDaOferta = state.corridaAntesDaOferta,
            ofertasPendentes = state.ofertasPendentes,
            onboardingEtapa = state.onboardingEtapa,
            tutorialPasso = state.tutorialPasso,
        )
        publicarOverlay()
    }

    fun selecionarHistoricoPorChave(chave: String) {
        val item = state.historico.firstOrNull { it.chaveHistorico() == chave } ?: return
        if (state.historicoVisivel) {
            marcarItemHistorico(item)
        } else {
            selecionarHistorico(item)
        }
    }

    fun marcarItemHistorico(item: HistoricoItemPresentation) {
        val chave = item.chaveHistorico()
        val atuais = state.historicoChavesSelecionadas.toMutableSet()
        if (atuais.contains(chave)) {
            atuais.remove(chave)
            state = state.copy(
                historicoChavesSelecionadas = atuais,
                historicoSelecionado = state.historico.firstOrNull {
                    it.chaveHistorico() in atuais
                },
            )
        } else {
            atuais.add(chave)
            state = state.copy(
                historicoChavesSelecionadas = atuais,
                historicoSelecionado = item,
            )
        }
        publicarOverlay()
    }

    fun limparMarcacaoHistorico() {
        if (state.historicoChavesSelecionadas.isEmpty()) {
            return
        }
        state = state.copy(
            historicoChavesSelecionadas = emptySet(),
            historicoSelecionado = null,
        )
        publicarOverlay()
    }

    fun selecionarAbaHistorico(aba: String) {
        state = state.copy(abaHistorico = aba)
        publicarOverlay()
    }

    fun selecionarDiaHistorico(epochDay: Long) {
        val noDashboard = state.dashboardVisivel && !state.historicoVisivel
        state = state.copy(
            historicoDia = CalendarioApp.diaDe(epochDay),
            calendarioPeriodo = if (noDashboard) CalendarioPeriodo.DIA else state.calendarioPeriodo,
        )
        publicarOverlay()
    }

    fun avancarSemanaHistorico(deltaSemanas: Int) {
        state = state.copy(historicoDia = CalendarioApp.avancarSemana(state.historicoDia, deltaSemanas))
        publicarOverlay()
    }

    fun avancarMesHistorico(deltaMeses: Int) {
        state = state.copy(historicoDia = CalendarioApp.avancarMes(state.historicoDia, deltaMeses))
        publicarOverlay()
    }

    fun selecionarPeriodoHistorico(periodo: String) {
        state = state.copy(calendarioPeriodo = CalendarioPeriodo.de(periodo))
        publicarOverlay()
    }

    fun avancarPeriodoHistorico(delta: Int) {
        state = state.copy(
            historicoDia = CalendarioApp.avancarPeriodo(
                state.historicoDia,
                state.calendarioPeriodo,
                delta,
            ),
        )
        publicarOverlay()
    }

    // ================================================================
    // REGISTRAR ACEITE
    //
    // IMPORTANTE:
    //
    // Este método NÃO é chamado quando a oferta chega.
    //
    // Ele somente deve ser executado quando o mecanismo de detecção
    // confirmar que o usuário aceitou a corrida no aplicativo da
    // plataforma.
    // ================================================================

    fun registrarAceiteCorrida() {
        val analise = state.analiseAtual ?: ofertaParaHistorico ?: return
        val aceiteEm = CalendarioApp.agora()
        val itemHistorico = PresentationBuilder.historicoDe(analise).copy(
            dataHoraRegistro = aceiteEm,
            dataHora = aceiteEm.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm")),
        )
        if (state.historico.any { it.chaveHistorico() == itemHistorico.chaveHistorico() }) {
            return
        }
        historicoRepository.salvar(itemHistorico)
        val chave = chavePlataforma(analise)
        val pendentes = state.ofertasPendentes - chave
        val proxima = pendentes.values.lastOrNull()
        val historico = listOf(itemHistorico) + state.historico
        if (proxima != null) {
            state = PresentationBuilder.criarEstado(
                analise = proxima,
                plano = state.plano,
                historico = historico,
                historicoSelecionado = null,
                abaHistorico = abaDe(analise.plataforma),
                abaConfiguracao = state.abaConfiguracao,
                modo = ModoApresentacao.COMPACTA,
                historicoVisivel = false,
                configuracoesVisivel = false,
                interfaceOculta = true,
                overlayAtivo = true,
                notificacaoDisponivel = true,
                seloFlutuante = !state.seloEscondido,
                compactaTemporaria = false,
                monitorando = true,
                seloOffsetX = state.seloOffsetX,
                seloOffsetY = state.seloOffsetY,
            seloEscondido = state.seloEscondido,
                estadoSalvo = state.estadoSalvo,
                corridaAceita = false,
                ultimaCorridaAceita = analise,
                ofertaAtiva = true,
                ofertasPendentes = pendentes,
                onboardingEtapa = state.onboardingEtapa,
                tutorialPasso = state.tutorialPasso,
            )
        } else {
            state = PresentationBuilder.criarEstado(
                analise = analise,
                plano = state.plano,
                historico = historico,
                historicoSelecionado = null,
                abaHistorico = abaDe(analise.plataforma),
                abaConfiguracao = state.abaConfiguracao,
                modo = ModoApresentacao.COMPACTA,
                historicoVisivel = false,
                configuracoesVisivel = false,
                interfaceOculta = true,
                overlayAtivo = true,
                notificacaoDisponivel = true,
                seloFlutuante = !state.seloEscondido,
                compactaTemporaria = false,
                monitorando = true,
                seloOffsetX = state.seloOffsetX,
                seloOffsetY = state.seloOffsetY,
            seloEscondido = state.seloEscondido,
                estadoSalvo = state.estadoSalvo,
                corridaAceita = true,
                ultimaCorridaAceita = analise,
                ofertaAtiva = false,
                ofertasPendentes = pendentes,
                onboardingEtapa = state.onboardingEtapa,
                tutorialPasso = state.tutorialPasso,
            )
        }
        publicarOverlay()
        _irParaSegundoPlano.tryEmit(Unit)
    }

    // ================================================================
    // NOTIFICAÇÃO / INTERFACE
    // ================================================================

    fun registrarNotificacao() {

        state =
            state.copy(
                overlayAtivo = true,
                notificacaoDisponivel = true,
                seloFlutuante = false,
                interfaceOculta = false,
                monitorando = true
            )
    }

    fun semNotificacao() {
        irParaSelo()
        state = state.copy(notificacaoDisponivel = false)
        publicarOverlay()
    }

    // ================================================================
    // OCULTAR → SELO
    // ================================================================

    fun ocultarInterface() {
        irParaSelo()
    }

    fun recolherAoSairDoApp() {
        if (!state.monitorando || state.seloEscondido) {
            return
        }
        if (state.seloFlutuante && state.interfaceOculta &&
            !state.historicoVisivel &&
            !state.configuracoesVisivel &&
            !state.dashboardVisivel
        ) {
            return
        }
        guardarTelaSeVazio()
        ocultarInterface()
    }

    /**
     * Voltar do celular: um passo de cada vez até Opções.
     * Em Opções devolve true para o app ir ao Home do celular, sem trocar a aba.
     */
    fun voltarPelaBarra(): Boolean {
        if (state.onboardingEtapa != OnboardingEtapa.NENHUMA) {
            return false
        }
        if (state.confirmacaoFecharVisivel) {
            cancelarFecharApp()
            return false
        }
        if (state.confirmacaoLimparHistoricoVisivel) {
            cancelarLimparHistorico()
            return false
        }
        if (state.confirmacaoDesativarVisivel) {
            cancelarDesativarMonitoramento()
            return false
        }
        val foraDeOpcoes = state.dashboardVisivel ||
            state.historicoVisivel ||
            (state.configuracoesVisivel && state.abaConfiguracao >= 0)
        if (foraDeOpcoes) {
            voltarParaOpcoes()
            return false
        }
        if (state.monitorando) {
            menuSaiuDaFrente()
        }
        return true
    }

    fun aoAbrirRecentes() {
        if (!state.monitorando) {
            return
        }
        guardarTelaSeVazio()
        irParaSelo()
    }

    fun restaurarTelaAposRecentes() {
        if (!state.monitorando) {
            return
        }
        val salvo = state.estadoSalvo
        if (state.seloEscondido && salvo == null) {
            publicarOverlay()
            return
        }
        if (salvo == null) {
            publicarOverlay()
            _irParaSegundoPlano.tryEmit(Unit)
            return
        }
        cancelarCompactaTemporaria()
        val escondido = state.seloEscondido || salvo.seloEscondido
        state = state.copy(
            estadoSalvo = null,
            recentesConfig = false,
            historicoVisivel = salvo.historicoVisivel,
            configuracoesVisivel = salvo.configuracoesVisivel,
            dashboardVisivel = salvo.dashboardVisivel,
            seloFlutuante = salvo.seloFlutuante && !escondido,
            seloEscondido = escondido,
            compactaTemporaria = salvo.compactaTemporaria,
            interfaceOculta = !(salvo.historicoVisivel || salvo.configuracoesVisivel || salvo.dashboardVisivel),
            overlayAtivo = true,
            abaConfiguracao = salvo.abaConfiguracao,
            confirmacaoFecharVisivel = salvo.confirmacaoFecharVisivel,
            confirmacaoLimparHistoricoVisivel = salvo.confirmacaoLimparHistoricoVisivel,
            corrida = state.corrida.copy(modo = salvo.modo),
        )
        publicarOverlay()
        if (state.interfaceOculta) {
            _irParaSegundoPlano.tryEmit(Unit)
        } else {
            _trazerJanela.tryEmit(Unit)
        }
    }

    private fun guardarTelaSeVazio() {
        if (state.estadoSalvo != null) {
            return
        }
        state = state.copy(
            estadoSalvo = EstadoInterfaceSalvo(
                modo = state.corrida.modo,
                historicoVisivel = state.historicoVisivel,
                configuracoesVisivel = state.configuracoesVisivel,
                dashboardVisivel = state.dashboardVisivel,
                seloFlutuante = state.seloFlutuante,
                seloEscondido = state.seloEscondido,
                compactaTemporaria = state.compactaTemporaria,
                abaConfiguracao = state.abaConfiguracao,
                confirmacaoFecharVisivel = state.confirmacaoFecharVisivel,
                confirmacaoLimparHistoricoVisivel = state.confirmacaoLimparHistoricoVisivel,
            ),
        )
    }

    private fun abrirMenuAposPainel() {
        state = state.copy(
            historicoVisivel = false,
            configuracoesVisivel = false,
            dashboardVisivel = false,
            recentesConfig = false,
            interfaceOculta = true,
            seloFlutuante = false,
            overlayAtivo = true,
            corrida = state.corrida.copy(modo = ModoApresentacao.DETALHES),
        )
        publicarOverlay()
        _irParaSegundoPlano.tryEmit(Unit)
    }

    fun recolherCompactaPorToqueFora() {
        val emOverlay = state.monitorando && state.overlayAtivo && state.interfaceOculta
        val expandida = emOverlay &&
            state.corrida.modo == ModoApresentacao.DETALHES &&
            !state.seloFlutuante
        val compactaVisivel = emOverlay &&
            !expandida &&
            !state.seloFlutuante &&
            (state.ofertaAtiva || state.compactaTemporaria)
        if (!compactaVisivel) {
            return
        }
        irParaSelo()
    }

    fun recolherPorBarraSistema() {
        if (!state.monitorando) {
            return
        }
        state = state.copy(estadoSalvo = null)
        val jaNoSelo = state.seloFlutuante &&
            !state.historicoVisivel &&
            !state.configuracoesVisivel &&
            !state.dashboardVisivel &&
            !state.confirmacaoFecharVisivel &&
            !state.confirmacaoLimparHistoricoVisivel &&
            state.corrida.modo != ModoApresentacao.DETALHES &&
            !state.seloEscondido
        if (jaNoSelo) {
            return
        }
        irParaSelo()
    }

    fun esconderSeloManterMonitor() {
        if (!state.monitorando) {
            return
        }
        cancelarCompactaTemporaria()
        SessaoMonitoramento.definirSeloNaLixeira(true)
        state = state.copy(
            seloEscondido = true,
            seloFlutuante = false,
            atalhosAbertos = false,
            compactaTemporaria = false,
            overlayAtivo = true,
            confirmacaoFecharVisivel = false,
            confirmacaoLimparHistoricoVisivel = false,
        )
        publicarOverlay()
        avaliarEncerramentoTotal()
    }

    fun abrirAtalhoConfig(indice: Int) {
        state = state.copy(
            seloFlutuante = false,
            interfaceOculta = false,
            overlayAtivo = true,
            historicoVisivel = false,
            dashboardVisivel = false,
            configuracoesVisivel = true,
            abaConfiguracao = indice.coerceIn(0, 3),
            atalhosAbertos = false,
            estadoSalvo = null,
            corrida = state.corrida.copy(modo = ModoApresentacao.DETALHES),
        )
        publicarOverlay()
        _trazerJanela.tryEmit(Unit)
    }

    fun retrairParaCompactaTemporaria() {
        cancelarCompactaTemporaria()
        state = state.copy(
            interfaceOculta = true,
            seloFlutuante = false,
            compactaTemporaria = true,
            historicoVisivel = false,
            configuracoesVisivel = false,
            overlayAtivo = true,
            monitorando = true,
            corrida = state.corrida.copy(
                modo = ModoApresentacao.COMPACTA,
                acaoDetalhes = "ⓘ",
            ),
        )
        publicarOverlay()
        _irParaSegundoPlano.tryEmit(Unit)
        compactaTemporariaJob = scope.launch {
            delay(COMPACTA_EXPIRA_MS)
            if (!state.compactaTemporaria) {
                return@launch
            }
            irParaSelo()
        }
    }

    // ================================================================
    // REABRIR PELO SELO → TELA EXPANDIDA
    // ================================================================

    fun reabrirInterface(@Suppress("UNUSED_PARAMETER") origemCompacta: Boolean = false) {
        if (origemCompacta || !state.monitorando) {
            return
        }
        cancelarCompactaTemporaria()
        // Após mapa (ou recentes): volta para a tela que estava (ex.: Histórico).
        if (state.estadoSalvo != null) {
            restaurarTelaAposRecentes()
            return
        }
        val menuAberto = state.corrida.modo == ModoApresentacao.DETALHES &&
            !state.seloEscondido &&
            !state.ofertaAtiva &&
            !state.historicoVisivel &&
            !state.configuracoesVisivel &&
            !state.dashboardVisivel &&
            !state.confirmacaoFecharVisivel &&
            !state.confirmacaoLimparHistoricoVisivel
        if (menuAberto) {
            irParaSelo()
            return
        }
        state = state.copy(
            interfaceOculta = true,
            seloFlutuante = false,
            compactaTemporaria = false,
            overlayAtivo = true,
            monitorando = true,
            historicoVisivel = false,
            configuracoesVisivel = false,
            dashboardVisivel = false,
            corrida = state.corrida.copy(
                modo = ModoApresentacao.DETALHES,
                acaoDetalhes = "Menos detalhes",
            ),
            estadoSalvo = null,
        )
        publicarOverlay()
        _irParaSegundoPlano.tryEmit(Unit)
    }

    /** Guarda a tela atual (ex. Histórico), vai ao selo e deixa o mapa abrir. */
    fun sairParaMapaHistorico() {
        if (!state.monitorando) {
            return
        }
        guardarTelaSeVazio()
        irParaSelo()
    }

    // ================================================================
    // FECHAMENTO
    // ================================================================

    fun solicitarFecharApp() {
        estadoAntesFechar = state
        if (menuNaFrente || !state.interfaceOculta) {
            state = state.copy(
                confirmacaoFecharVisivel = true,
                confirmacaoLimparHistoricoVisivel = false,
                interfaceOculta = false,
                configuracoesVisivel = true,
                historicoVisivel = false,
                dashboardVisivel = false,
                abaConfiguracao = -1,
                atalhosAbertos = false,
            )
            publicarOverlay()
            return
        }
        state = state.copy(
            confirmacaoFecharVisivel = true,
            confirmacaoLimparHistoricoVisivel = false,
            historicoVisivel = false,
            configuracoesVisivel = false,
            seloFlutuante = false,
            corrida = state.corrida.copy(
                modo = ModoApresentacao.DETALHES,
                acaoDetalhes = "Menos detalhes",
            ),
        )
        publicarOverlay()
        _irParaSegundoPlano.tryEmit(Unit)
    }

    fun cancelarFecharApp() {
        val anterior = estadoAntesFechar
        estadoAntesFechar = null
        if (anterior != null) {
            state = anterior.copy(confirmacaoFecharVisivel = false)
            publicarOverlay()
            if (state.interfaceOculta) {
                _irParaSegundoPlano.tryEmit(Unit)
            }
            return
        }
        state = state.copy(
            confirmacaoFecharVisivel = false,
        )
    }

    fun confirmarFecharApp() {
        SessaoMonitoramento.definir(false)
        cancelarCompactaTemporaria()
        estadoAntesFechar = null
        state =
            state.copy(
                confirmacaoFecharVisivel = false,
                configuracoesVisivel = false,
                historicoVisivel = false,
                overlayAtivo = false,
                monitorando = false,
                seloFlutuante = false,
                compactaTemporaria = false,
                interfaceOculta = false
            )

        _fecharApp.tryEmit(Unit)
        publicarOverlay()
    }

    fun excluirCorridaHistorico(item: HistoricoItemPresentation) {
        state = state.copy(
            historicoChavesSelecionadas = setOf(item.chaveHistorico()),
            historicoSelecionado = item,
        )
        solicitarLimparHistorico()
    }

    fun solicitarLimparHistorico() {
        if (state.historicoChavesSelecionadas.isEmpty()) {
            return
        }
        estadoAntesLimparHistorico = state
        state = state.copy(
            confirmacaoLimparHistoricoVisivel = true,
            confirmacaoFecharVisivel = false,
            historicoVisivel = true,
            configuracoesVisivel = false,
            dashboardVisivel = false,
            seloFlutuante = false,
            corrida = state.corrida.copy(modo = ModoApresentacao.DETALHES),
        )
        publicarOverlay()
        if (state.interfaceOculta) {
            _irParaSegundoPlano.tryEmit(Unit)
        }
    }

    fun cancelarLimparHistorico() {
        estadoAntesLimparHistorico = null
        state = state.copy(
            confirmacaoLimparHistoricoVisivel = false,
            historicoVisivel = true,
            configuracoesVisivel = false,
            dashboardVisivel = false,
            seloFlutuante = false,
            corrida = state.corrida.copy(modo = ModoApresentacao.DETALHES),
        )
        publicarOverlay()
        if (state.interfaceOculta) {
            _irParaSegundoPlano.tryEmit(Unit)
        }
    }

    fun confirmarLimparHistorico() {
        val chaves = state.historicoChavesSelecionadas
        if (chaves.isEmpty()) {
            estadoAntesLimparHistorico = null
            state = state.copy(
                confirmacaoLimparHistoricoVisivel = false,
                historicoVisivel = true,
            )
            publicarOverlay()
            return
        }
        historicoRepository.remover(chaves)
        estadoAntesLimparHistorico = null
        state = state.copy(
            historico = historicoRepository.listar(),
            historicoSelecionado = null,
            historicoChavesSelecionadas = emptySet(),
            confirmacaoLimparHistoricoVisivel = false,
            historicoVisivel = true,
            configuracoesVisivel = false,
            dashboardVisivel = false,
            seloFlutuante = false,
            corrida = state.corrida.copy(
                modo = ModoApresentacao.DETALHES,
                acaoDetalhes = "Menos detalhes",
            ),
        )
        publicarOverlay()
        if (state.interfaceOculta) {
            _irParaSegundoPlano.tryEmit(Unit)
        }
    }

    // ================================================================
    // POSIÇÃO DO SELO
    // ================================================================

    fun atualizarPosicaoSelo(
        offsetX: Float,
        offsetY: Float
    ) {
        state =
            state.copy(
                seloOffsetX = offsetX,
                seloOffsetY = offsetY
            )
        // Mantém o snapshot alinhado para o overlay não recriar o selo no topo.
        if (state.monitorando && (state.seloFlutuante || state.seloEscondido)) {
            publicarOverlay()
        }
    }

    // ================================================================
    // NOVA CORRIDA
    //
    // REGRA:
    //
    // Nova oferta:
    //
    // corrida atual = nova corrida
    // histórico = preservado
    //
    // O aceite será registrado somente pelo evento
    // RideNotificationEvent.CorridaAceita.
    // ================================================================

    internal fun aplicarNovaCorrida(
        analise: AnaliseCorrida
    ) {
        cancelarCompactaTemporaria()
        ofertaParaHistorico = analise
        val chave = chavePlataforma(analise)
        val pendentes = state.ofertasPendentes + (chave to analise)
        state =
            PresentationBuilder.criarEstado(
                analise = analise,
                plano = state.plano,
                historico = state.historico,
                historicoSelecionado = null,
                abaHistorico = state.abaHistorico,
                abaConfiguracao = state.abaConfiguracao,
                destacarPermissoes = false,
                modo = ModoApresentacao.COMPACTA,
                historicoVisivel = false,
                configuracoesVisivel = false,
                interfaceOculta = true,
                overlayAtivo = true,
                notificacaoDisponivel = true,
                seloFlutuante = false,
                compactaTemporaria = false,
                monitorando = true,
                seloOffsetX = state.seloOffsetX,
                seloOffsetY = state.seloOffsetY,
            seloEscondido = state.seloEscondido,
                estadoSalvo = state.estadoSalvo,
                corridaAceita = false,
                ultimaCorridaAceita = state.ultimaCorridaAceita,
                ofertaAtiva = true,
                corridaAntesDaOferta = state.historicoSelecionado?.paraAnalise(),
                ofertasPendentes = pendentes,
                onboardingEtapa = state.onboardingEtapa,
                tutorialPasso = state.tutorialPasso,
            ).copy(
                dashboardVisivel = false,
            )
        publicarOverlay()
        _irParaSegundoPlano.tryEmit(Unit)
    }

    internal fun expirarOfertaAtual() {
        if (!state.ofertaAtiva) {
            if (state.historicoSelecionado != null) {
                return
            }
            if (state.analiseAtual == null &&
                state.seloFlutuante &&
                state.corrida.modo != ModoApresentacao.DETALHES
            ) {
                return
            }
        }
        cancelarCompactaTemporaria()
        val chave = state.analiseAtual?.let { chavePlataforma(it) }.orEmpty()
        val pendentes = if (chave.isBlank()) {
            emptyMap()
        } else {
            state.ofertasPendentes - chave
        }
        val proxima = pendentes.values.lastOrNull()
        if (proxima != null) {
            state = PresentationBuilder.criarEstado(
                analise = proxima,
                plano = state.plano,
                historico = state.historico,
                historicoSelecionado = null,
                abaHistorico = state.abaHistorico,
                abaConfiguracao = state.abaConfiguracao,
                modo = ModoApresentacao.COMPACTA,
                historicoVisivel = false,
                configuracoesVisivel = false,
                interfaceOculta = true,
                overlayAtivo = true,
                notificacaoDisponivel = true,
                seloFlutuante = false,
                compactaTemporaria = false,
                monitorando = true,
                seloOffsetX = state.seloOffsetX,
                seloOffsetY = state.seloOffsetY,
            seloEscondido = state.seloEscondido,
                estadoSalvo = state.estadoSalvo,
                corridaAceita = false,
                ultimaCorridaAceita = state.ultimaCorridaAceita,
                ofertaAtiva = true,
                ofertasPendentes = pendentes,
                onboardingEtapa = state.onboardingEtapa,
                tutorialPasso = state.tutorialPasso,
            )
        } else {
            state = PresentationBuilder.criarEstado(
                analise = null,
                plano = state.plano,
                historico = state.historico,
                historicoSelecionado = null,
                abaHistorico = state.abaHistorico,
                abaConfiguracao = state.abaConfiguracao,
                modo = ModoApresentacao.COMPACTA,
                historicoVisivel = false,
                configuracoesVisivel = false,
                interfaceOculta = true,
                overlayAtivo = true,
                notificacaoDisponivel = false,
                seloFlutuante = !state.seloEscondido,
                compactaTemporaria = false,
                monitorando = true,
                seloOffsetX = state.seloOffsetX,
                seloOffsetY = state.seloOffsetY,
            seloEscondido = state.seloEscondido,
                estadoSalvo = state.estadoSalvo,
                corridaAceita = false,
                ultimaCorridaAceita = state.ultimaCorridaAceita,
                ofertaAtiva = false,
                ofertasPendentes = pendentes,
                onboardingEtapa = state.onboardingEtapa,
                tutorialPasso = state.tutorialPasso,
            )
        }
        publicarOverlay()
        _irParaSegundoPlano.tryEmit(Unit)
    }

    private fun irParaSelo(enviarParaFundo: Boolean = true) {
        cancelarCompactaTemporaria()
        val escondido = state.seloEscondido
        state = state.copy(
            monitorando = true,
            overlayAtivo = true,
            seloFlutuante = !escondido,
            seloEscondido = escondido,
            atalhosAbertos = false,
            compactaTemporaria = false,
            historicoVisivel = false,
            configuracoesVisivel = false,
            dashboardVisivel = false,
            recentesConfig = false,
            confirmacaoFecharVisivel = false,
            confirmacaoDesativarVisivel = false,
            interfaceOculta = true,
            corrida = state.corrida.copy(
                modo = ModoApresentacao.COMPACTA,
                acaoDetalhes = "ⓘ",
            ),
        )
        publicarOverlay()
        if (enviarParaFundo) {
            _irParaSegundoPlano.tryEmit(Unit)
        }
    }

    /** Toque no selo abre ou fecha os atalhos. O menu do app só abre pelo ícone. */
    fun alternarAtalhosPeloSelo() {
        if (!state.monitorando || state.seloEscondido) {
            return
        }
        state = state.copy(atalhosAbertos = !state.atalhosAbertos)
        publicarOverlay()
    }

    fun menuEntrouNaFrente() {
        menuNaFrente = true
        menuEncerrado = false
        if (state.atalhosAbertos) {
            state = state.copy(atalhosAbertos = false)
        }
        publicarOverlay()
    }

    fun menuSaiuDaFrente() {
        menuNaFrente = false
        menuEncerrado = true
        if (state.monitorando && state.notificacaoFechada && state.seloEscondido) {
            desativarMonitoramento()
            return
        }
        publicarOverlay()
    }

    private fun avaliarEncerramentoTotal() {
        if (state.monitorando && state.notificacaoFechada && state.seloEscondido && menuEncerrado) {
            desativarMonitoramento()
        }
    }

    private fun cancelarCompactaTemporaria() {
        compactaTemporariaJob?.cancel()
        compactaTemporariaJob = null
    }

    private fun publicarOverlay() {
        val emOverlay = state.monitorando && state.overlayAtivo && state.interfaceOculta
        val baseFlutuante = state.monitorando && state.overlayAtivo && !state.seloEscondido
        val compactaVisivel = emOverlay &&
            !state.seloEscondido &&
            state.corrida.modo != ModoApresentacao.DETALHES &&
            (state.ofertaAtiva || state.compactaTemporaria)
        val confirmandoFechar = state.confirmacaoFecharVisivel
        val confirmandoLimpar = state.confirmacaoLimparHistoricoVisivel
        val painelAtalhos = baseFlutuante && state.atalhosAbertos && !compactaVisivel && !menuNaFrente
        val expandidaLegado = emOverlay &&
            !menuNaFrente &&
            !state.seloEscondido &&
            !compactaVisivel &&
            (
                confirmandoFechar ||
                    (
                        state.corrida.modo == ModoApresentacao.DETALHES &&
                            !state.historicoVisivel &&
                            !state.configuracoesVisivel &&
                            !state.dashboardVisivel &&
                            !confirmandoLimpar
                        )
                )
        val expandidaVisivel = painelAtalhos || expandidaLegado
        val seloVisivel = baseFlutuante && !compactaVisivel && !menuNaFrente && !state.atalhosAbertos
        val analise = analiseExibida()
        val campos = state.corrida.camposCompactos.associate { it.id to it.valor }
        val detalhes = state.corrida.camposDetalhes.associate { it.id to it.valor }
        val periodoHistorico = if (state.historicoVisivel) {
            CalendarioPeriodo.SEMANA
        } else {
            state.calendarioPeriodo
        }
        val itensDoPeriodo = state.historico
            .sortedByDescending { it.dataHoraRegistro ?: java.time.LocalDateTime.MIN }
            .filter { item ->
                val dia = item.dataHoraRegistro?.toLocalDate() ?: return@filter false
                CalendarioApp.noPeriodo(dia, state.historicoDia, periodoHistorico) &&
                    item.pertenceAba(state.abaHistorico)
            }
        val itensHistorico = itensDoPeriodo
            .map { item ->
                OverlayHistoricoItem(
                    chave = item.chaveHistorico(),
                    data = item.dataLista,
                    hora = item.horaLista,
                    valorPorKm = PresentationBuilder.formatarCelulaHistoricoValorPorKm(item.valorPorKm),
                    valor = PresentationBuilder.formatarCelulaHistoricoValor(item.valorTotal),
                    km = PresentationBuilder.formatarDistanciaHistorico(item.kmTotal),
                    tempo = PresentationBuilder.formatarTempoHistorico(item.tempoEstimado),
                    nota = PresentationBuilder.formatarCelulaHistoricoNota(item.notaPassageiro),
                    marcador = item.classificacao.marcador,
                    corMarcador = item.corClassificacao,
                    plataforma = item.plataforma,
                    lucro = PresentationBuilder.formatarLucroHistorico(
                        item.valorTotal,
                        item.custoCombustivel,
                    ),
                    gasto = PresentationBuilder.formatarGastoHistorico(item.custoCombustivel),
                    consumo = PresentationBuilder.formatarLitrosHistorico(item.combustivelEstimado),
                    tempoHm = PresentationBuilder.formatarTempoHm(item.tempoEstimado),
                    cabecalhoData = PresentationBuilder.formatarCabecalhoHistorico(
                        item.dataHoraRegistro,
                        item.dataLista,
                        item.horaLista,
                    ),
                    embarque = item.enderecoEmbarque,
                    destino = item.enderecoDestino,
                    classificacao = item.classificacao.rotulo,
                    valorTotalNum = item.valorTotal,
                    kmNum = item.kmTotal,
                    minutosNum = item.tempoEstimado ?: 0,
                    gastoNum = item.custoCombustivel,
                )
            }
        val config = configuracaoStore.carregar()
        val corHora = if (config.marcaHoraBoa > 0.0) {
            SemaforoOferta.corPorDuasMarcas(analise?.valorPorHora, config.marcaHoraRuim, config.marcaHoraBoa)
        } else {
            SemaforoOferta.corPorFaixaHora(analise?.valorPorHora, config.metaGanhoHora)
        }
        val corNota = if (config.marcaNotaBoa > 0.0) {
            SemaforoOferta.corPorDuasMarcas(analise?.notaPassageiro, config.marcaNotaRuim, config.marcaNotaBoa)
        } else {
            ClassificacaoConstantes.COR_BORDA_NEUTRA
        }
        OverlayBridge.publicar(
            OverlaySnapshot(
                monitorando = state.monitorando,
                notificacaoFechada = state.notificacaoFechada,
                seloVisivel = seloVisivel,
                compactaVisivel = compactaVisivel,
                expandidaVisivel = expandidaVisivel,
                historicoVisivel = false,
                configuracoesVisivel = false,
                dashboardVisivel = false,
                // Fechar nos atalhos abre o app. Esta confirmação só flutua com o menu fora da frente.
                confirmacaoFecharVisivel = emOverlay && !menuNaFrente && !state.seloEscondido && state.confirmacaoFecharVisivel,
                confirmacaoMonitorVisivel = emOverlay && !menuNaFrente && !state.seloEscondido && state.confirmacaoDesativarVisivel,
                confirmacaoLimparHistoricoVisivel = emOverlay && !state.seloEscondido &&
                    state.confirmacaoLimparHistoricoVisivel &&
                    state.historicoVisivel,
                historicoAba = state.abaHistorico,
                historicoEpochDay = state.historicoDia.toEpochDay(),
                historicoPeriodo = state.calendarioPeriodo.name,
                historicoDiasComCorrida = state.historico.mapNotNull {
                    it.dataHoraRegistro?.toLocalDate()?.toEpochDay()
                }.distinct(),
                historicoItens = itensHistorico,
                historicoFaturamento = PresentationBuilder.formatarResumoFaturamento(itensDoPeriodo),
                historicoDistancia = PresentationBuilder.formatarResumoDistancia(itensDoPeriodo),
                historicoGasto = PresentationBuilder.formatarResumoGasto(itensDoPeriodo),
                historicoLucro = PresentationBuilder.formatarResumoLucro(itensDoPeriodo),
                historicoChaveSelecionada = state.historicoSelecionado?.chaveHistorico().orEmpty(),
                historicoChavesSelecionadas = state.historicoChavesSelecionadas.toList(),
                historicoLimparQuantidade = state.historicoChavesSelecionadas.size,
                planoPro = state.plano.ehPro,
                dashboardGanhosDia = ganhosDoPeriodo(CalendarioPeriodo.DIA),
                dashboardGanhosSemana = ganhosDoPeriodo(CalendarioPeriodo.SEMANA),
                dashboardGanhosMes = ganhosDoPeriodo(CalendarioPeriodo.MES),
                destacarPermissoes = state.destacarPermissoes,
                abaConfiguracao = state.abaConfiguracao,
                offsetX = state.seloOffsetX,
                offsetY = state.seloOffsetY,
                valorPorKm = campos["valor_por_km"] ?: "—",
                valorPorHora = analise?.valorPorHora
                    ?.let { "R$" + PresentationBuilder.formatarDecimalPublico(it) }
                    ?: "—",
                valorTotal = campos["valor_total"] ?: "—",
                kmTotal = campos["km_total"] ?: "—",
                tempo = campos["tempo_estimado"] ?: "—",
                nota = campos["nota_passageiro"] ?: "—",
                detalhes = state.corrida.camposDetalhes.map { "${it.titulo}: ${it.valor}" },
                aguardandoOferta = !state.ofertaAtiva && !state.corridaAceita,
                liquidoPorKm = PresentationBuilder.formatarLiquidoPorKm(analise),
                litrosEstimados = detalhes["combustivel_estimado"] ?: "—",
                gastoEstimado = detalhes["custo_combustivel"] ?: "—",
                lucroEstimado = detalhes["lucro_estimado"] ?: "—",
                lucroPercentual = formatarLucroPercentual(analise),
                kmAtePassageiro = detalhes["km_ate_passageiro"] ?: "—",
                kmViagem = detalhes["km_viagem"] ?: "—",
                quantidadeParadas = analise?.corrida?.quantidadeParadas ?: 0,
                plataformaSigla = siglaPlataforma(analise?.plataforma),
                tempoHm = formatarTempoHm(analise?.tempoEstimado),
                enderecoEmbarque = analise?.corrida?.enderecoEmbarque,
                enderecoDestino = analise?.corrida?.enderecoDestino,
                corridaAceita = state.corridaAceita,
                corClassificacao = if (analise == null) {
                    ClassificacaoConstantes.COR_BORDA_NEUTRA
                } else {
                    state.corrida.corClassificacao
                },
                corValorPorHora = corHora,
                corNota = corNota,
                horaEstimada = analise?.corrida?.horaEstimada == true,
                anunciarVoz = configuracaoStore.carregar().anunciarVoz,
                rotuloClassificacao = state.corrida.classificacao.rotulo,
                corBordaCompacta = SemaforoOferta.pior(
                    if (analise == null) ClassificacaoConstantes.COR_BORDA_NEUTRA else state.corrida.corClassificacao,
                    corHora,
                ),
            ),
        )
    }

    private fun ganhosDoPeriodo(periodo: CalendarioPeriodo): String {
        if (!state.plano.ehPro) {
            return "🔒"
        }
        val itens = state.historico.filter { item ->
            val dia = item.dataHoraRegistro?.toLocalDate() ?: return@filter false
            CalendarioApp.noPeriodo(dia, state.historicoDia, periodo)
        }
        return PresentationBuilder.formatarResumoFaturamento(itens)
    }

    private fun analiseExibida(): AnaliseCorrida? {
        if (state.ofertaAtiva || state.compactaTemporaria) {
            return state.analiseAtual ?: state.ultimaCorridaAceita
        }
        if (state.corridaAceita) {
            return state.ultimaCorridaAceita ?: state.analiseAtual
        }
        if (state.historicoVisivel) {
            return state.historicoSelecionado?.paraAnalise()
        }
        // Mantém resumo na notificação até a próxima oferta.
        return state.ultimaCorridaAceita
    }

    private fun chavePlataforma(analise: AnaliseCorrida): String =
        analise.plataforma?.ifBlank { "Uber" } ?: "Uber"

    private fun abaDe(plataforma: String?): String {
        val nome = plataforma.orEmpty()
        return when {
            nome.contains("99") -> "99"
            nome.contains("inDrive", ignoreCase = true) ||
                nome.contains("indrive", ignoreCase = true) -> "inDrive"
            else -> "Uber"
        }
    }

    private fun agendarCompactaParaSelo(delayMs: Long) {
        cancelarCompactaTemporaria()
        compactaTemporariaJob = scope.launch {
            delay(delayMs)
            if (!state.compactaTemporaria) {
                return@launch
            }
            irParaSelo()
        }
    }

    private fun formatarTempoHm(minutos: Int?): String {
        if (minutos == null) {
            return "—"
        }
        val horas = minutos / 60
        val resto = minutos % 60
        return "${horas}h${resto}m"
    }

    private fun formatarLucroPercentual(analise: AnaliseCorrida?): String {
        val custo = analise?.custoCombustivel ?: return "—"
        val total = analise.valorTotal
        if (total <= 0.0) {
            return "—"
        }
        val percentual = (total - custo) / total * 100.0
        return PresentationBuilder.formatarDecimalPublico(percentual)
    }

    private fun siglaPlataforma(plataforma: String?): String {
        val nome = plataforma.orEmpty()
        return when {
            nome.contains("99") -> "99"
            nome.contains("inDrive", ignoreCase = true) -> "INDRIVE"
            nome.isBlank() -> ""
            else -> "UBER"
        }
    }

        companion object {
        const val COMPACTA_TEMPORARIA_MS = 5_000L
        /** Recolher os detalhes sem oferta: a compacta volta ao selo. */
        const val COMPACTA_EXPIRA_MS = 1_000L
    }
}