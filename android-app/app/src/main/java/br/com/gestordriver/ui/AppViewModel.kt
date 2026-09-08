package br.com.gestordriver.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestordriver.core.AnaliseCorrida
import br.com.gestordriver.core.CalendarioApp
import br.com.gestordriver.core.CalendarioPeriodo
import br.com.gestordriver.data.HistoricoRepository
import br.com.gestordriver.data.MemoriaHistoricoRepository
import br.com.gestordriver.data.MemoriaOnboardingStore
import br.com.gestordriver.data.OnboardingStore
import br.com.gestordriver.data.chaveHistorico
import br.com.gestordriver.model.HistoricoItemPresentation
import br.com.gestordriver.model.ModoApresentacao
import br.com.gestordriver.model.OnboardingEtapa
import br.com.gestordriver.model.PlanoAcesso
import br.com.gestordriver.model.TutorialConteudo
import br.com.gestordriver.notification.RideNotificationBus
import br.com.gestordriver.notification.RideNotificationEvent
import br.com.gestordriver.overlay.OverlayAcao
import br.com.gestordriver.overlay.OverlayBridge
import br.com.gestordriver.overlay.OverlayHistoricoItem
import br.com.gestordriver.overlay.OverlaySnapshot
import br.com.gestordriver.presentation.PresentationBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AppViewModel(
    private val historicoRepository: HistoricoRepository = MemoriaHistoricoRepository(),
    private val onboardingStore: OnboardingStore = MemoriaOnboardingStore(inicial = true),
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
        PresentationBuilder.criarEstadoInicial()
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
                    OverlayAcao.AbrirSemaforo -> abrirSemaforo()
                    OverlayAcao.AbrirConfig -> alternarConfiguracoes()
                    OverlayAcao.SalvarConfig -> fecharConfiguracoes()
                    OverlayAcao.CancelarConfig -> voltarParaAtalho()
                    OverlayAcao.Ocultar -> ocultarInterface()
                    OverlayAcao.Retratil -> retrairParaCompactaTemporaria()
                    OverlayAcao.ToqueForaDaCompacta -> Unit
                    OverlayAcao.RecolherParaSelo -> recolherPorBarraSistema()
                    OverlayAcao.FecharAtalhos -> fecharAtalhosParaSelo()
                    OverlayAcao.VoltarAtalho -> voltarParaAtalho()
                    OverlayAcao.VoltarBarra -> voltarPelaBarra()
                    OverlayAcao.RecentesBarra -> aoAbrirRecentes()
                    OverlayAcao.EsconderSelo -> esconderSeloManterMonitor()
                    OverlayAcao.SairParaMapaHistorico -> sairParaMapaHistorico()
                    OverlayAcao.DashboardPro -> abrirDashboard()
                    OverlayAcao.FecharDashboard -> voltarParaAtalho()
                    is OverlayAcao.AbrirAtalhoConfig -> abrirAtalhoConfig(acao.indice)
                    OverlayAcao.Fechar -> solicitarFecharApp()
                    OverlayAcao.CancelarFechar -> cancelarFecharApp()
                    OverlayAcao.ConfirmarFechar -> confirmarFecharApp()
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
                            abaConfiguracao = acao.indice.coerceIn(0, 2),
                            historicoVisivel = false,
                            dashboardVisivel = false,
                            semaforoVisivel = false,
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
    }

    fun iniciarMonitoramento() {
        if (state.onboardingEtapa != OnboardingEtapa.NENHUMA) {
            return
        }
        // Só liga monitoramento quando permissões já estão ok (caso 2 permanece sem monitorar).
        if (state.avisoSemMonitoramento) {
            return
        }
        ativarMonitoramento()
        if (!state.opcoesVisivel &&
            !state.configuracoesVisivel &&
            !state.historicoVisivel &&
            !state.dashboardVisivel &&
            !state.semaforoVisivel
        ) {
            abrirOpcoes()
        }
    }

    /** Liga monitoramento (barra de notificação / overlay) sem forçar o selo. */
    fun ativarMonitoramento() {
        if (state.onboardingEtapa != OnboardingEtapa.NENHUMA) {
            return
        }
        state = state.copy(
            monitorando = true,
            overlayAtivo = true,
            avisoSemMonitoramento = false,
            destacarPermissoes = false,
        )
        publicarOverlay()
    }

    /**
     * Abertura pelo ícone do app.
     *
     * Caso 1: perms faltando → Menu Configurar; se liberar → monitora e fica em Configurar.
     * Caso 2: perms faltando e não liberadas → aviso, sem monitoramento, fica em Configurar.
     * Caso 3: perms ok → monitora e abre Menu Opções.
     */
    fun avaliarInicio(permissoesOk: Boolean, temConta: Boolean) {
        if (!onboardingStore.concluido()) {
            // Conta / tutorial legado: só se ainda não concluiu e perms ok.
            if (!permissoesOk) {
                abrirMenuConfigurarSemMonitoramento()
                return
            }
            if (!temConta) {
                state = state.copy(
                    onboardingEtapa = OnboardingEtapa.CONTA,
                    interfaceOculta = false,
                    monitorando = false,
                    avisoSemMonitoramento = false,
                )
                return
            }
            if (state.onboardingEtapa == OnboardingEtapa.TUTORIAL) {
                return
            }
            if (state.onboardingEtapa == OnboardingEtapa.CONTA) {
                return
            }
            // 1º uso com perms ok → conclui e vai para Opções (caso 3 + tema claro padrão).
            onboardingStore.marcarConcluido()
            state = state.copy(onboardingEtapa = OnboardingEtapa.NENHUMA)
            abrirMenuOpcoesComMonitoramento()
            return
        }

        if (!permissoesOk) {
            // Já em Configurar aguardando liberação (caso 1 em andamento).
            if (state.configuracoesVisivel && state.abaConfiguracao == 2) {
                state = state.copy(
                    monitorando = false,
                    avisoSemMonitoramento = true,
                    destacarPermissoes = true,
                    interfaceOculta = false,
                )
                publicarOverlay()
                return
            }
            abrirMenuConfigurarSemMonitoramento()
            return
        }

        // Permissões ok
        if (state.configuracoesVisivel && (state.destacarPermissoes || state.avisoSemMonitoramento)) {
            // Caso 1: liberou permissões → inicia monitoramento e permanece em Configurar.
            state = state.copy(
                monitorando = true,
                overlayAtivo = true,
                avisoSemMonitoramento = false,
                destacarPermissoes = false,
                onboardingEtapa = OnboardingEtapa.NENHUMA,
                interfaceOculta = false,
            )
            publicarOverlay()
            return
        }

        if (state.onboardingEtapa != OnboardingEtapa.NENHUMA) {
            concluirOnboardingParaOpcoes()
            return
        }

        // Caso 3: perms ok → monitoramento + aba Opções.
        abrirMenuOpcoesComMonitoramento()
    }

    private fun abrirMenuConfigurarSemMonitoramento() {
        state = state.copy(
            onboardingEtapa = OnboardingEtapa.NENHUMA,
            tutorialPasso = 0,
            monitorando = false,
            avisoSemMonitoramento = true,
            destacarPermissoes = true,
            configuracoesVisivel = true,
            opcoesVisivel = false,
            historicoVisivel = false,
            dashboardVisivel = false,
            semaforoVisivel = false,
            abaConfiguracao = 2,
            interfaceOculta = false,
            seloFlutuante = false,
            seloEscondido = false,
            overlayAtivo = false,
            corrida = state.corrida.copy(modo = ModoApresentacao.DETALHES),
        )
        publicarOverlay()
        _irParaFrente.tryEmit(Unit)
    }

    private fun abrirMenuOpcoesComMonitoramento() {
        state = state.copy(
            onboardingEtapa = OnboardingEtapa.NENHUMA,
            monitorando = true,
            overlayAtivo = true,
            avisoSemMonitoramento = false,
            destacarPermissoes = false,
            opcoesVisivel = true,
            configuracoesVisivel = false,
            historicoVisivel = false,
            dashboardVisivel = false,
            semaforoVisivel = false,
            interfaceOculta = false,
            seloFlutuante = false,
            seloEscondido = false,
            corrida = state.corrida.copy(modo = ModoApresentacao.DETALHES),
        )
        publicarOverlay()
        _irParaFrente.tryEmit(Unit)
    }

    fun abrirOpcoes() {
        state = state.copy(
            opcoesVisivel = true,
            historicoVisivel = false,
            configuracoesVisivel = false,
            dashboardVisivel = false,
            semaforoVisivel = false,
            confirmacaoFecharVisivel = false,
            confirmacaoLimparHistoricoVisivel = false,
            interfaceOculta = false,
            seloFlutuante = false,
            seloEscondido = false,
            overlayAtivo = state.monitorando,
            corrida = state.corrida.copy(modo = ModoApresentacao.DETALHES),
        )
        publicarOverlay()
        _irParaFrente.tryEmit(Unit)
    }

    fun tutorialSeguir() {
        val proximo = state.tutorialPasso + 1
        if (proximo >= TutorialConteudo.passos.size) {
            concluirOnboardingParaOpcoes()
            return
        }
        state = state.copy(tutorialPasso = proximo)
    }

    fun tutorialPular() {
        concluirOnboardingParaOpcoes()
    }

    fun onboardingContaPronta() {
        state = state.copy(
            onboardingEtapa = OnboardingEtapa.TUTORIAL,
            tutorialPasso = 0,
            interfaceOculta = false,
        )
    }

    private fun concluirOnboardingParaOpcoes() {
        onboardingStore.marcarConcluido()
        abrirMenuOpcoesComMonitoramento()
    }

    private fun concluirOnboarding() {
        concluirOnboardingParaOpcoes()
    }

    // ================================================================
    // PLANO
    // ================================================================

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
            if (state.ofertaAtiva) {
                state = state.copy(
                    interfaceOculta = true,
                    seloFlutuante = false,
                    compactaTemporaria = false,
                    historicoVisivel = false,
                    configuracoesVisivel = false,
                    dashboardVisivel = false,
                    semaforoVisivel = false,
                    corrida = state.corrida.copy(
                        modo = ModoApresentacao.COMPACTA,
                        acaoDetalhes = "ⓘ",
                    ),
                )
                publicarOverlay()
                _irParaSegundoPlano.tryEmit(Unit)
            } else {
                irParaSelo()
            }
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
            fecharTelaNativaParaAtalho()
            return
        }
        calendarioPeriodoAntesHistorico = state.calendarioPeriodo
        historicoDiaAntesHistorico = state.historicoDia
        state = state.copy(
            historicoVisivel = true,
            configuracoesVisivel = false,
            dashboardVisivel = false,
            semaforoVisivel = false,
            opcoesVisivel = false,
            confirmacaoFecharVisivel = false,
            confirmacaoLimparHistoricoVisivel = false,
            interfaceOculta = false,
            seloFlutuante = false,
            seloEscondido = false,
            abaHistorico = "Todos",
            historicoDia = CalendarioApp.hoje(),
            corrida = state.corrida.copy(
                modo = ModoApresentacao.DETALHES,
                acaoDetalhes = "Menos detalhes",
            ),
        )
        publicarOverlay()
        _irParaFrente.tryEmit(Unit)
    }

    private fun fecharHistoricoELimpar() {
        calendarioPeriodoAntesHistorico = null
        historicoDiaAntesHistorico = null
        fecharTelaNativaParaAtalho()
    }

    // ================================================================
    // CONFIGURAÇÕES
    // ================================================================

    fun alternarConfiguracoes(destaquePermissao: Boolean = false, @Suppress("UNUSED_PARAMETER") usarOverlay: Boolean = true) {
        if (state.configuracoesVisivel && !destaquePermissao) {
            fecharConfiguracoes()
            return
        }
        abrirConfiguracoes(destaquePermissao)
    }

    fun abrirConfiguracoes(destaquePermissao: Boolean = false, @Suppress("UNUSED_PARAMETER") usarOverlay: Boolean = true) {
        state = state.copy(
            historicoVisivel = false,
            dashboardVisivel = false,
            semaforoVisivel = false,
            opcoesVisivel = false,
            configuracoesVisivel = true,
            confirmacaoFecharVisivel = false,
            confirmacaoLimparHistoricoVisivel = false,
            abaConfiguracao = 2,
            destacarPermissoes = destaquePermissao || state.avisoSemMonitoramento,
            interfaceOculta = false,
            seloFlutuante = false,
            seloEscondido = false,
            overlayAtivo = state.monitorando,
            corrida = state.corrida.copy(modo = ModoApresentacao.DETALHES),
        )
        publicarOverlay()
        _irParaFrente.tryEmit(Unit)
    }

    fun abrirSemaforo() {
        state = state.copy(
            semaforoVisivel = true,
            historicoVisivel = false,
            dashboardVisivel = false,
            configuracoesVisivel = false,
            opcoesVisivel = false,
            confirmacaoFecharVisivel = false,
            confirmacaoLimparHistoricoVisivel = false,
            destacarPermissoes = false,
            seloFlutuante = false,
            seloEscondido = false,
            interfaceOculta = false,
            overlayAtivo = state.monitorando,
            corrida = state.corrida.copy(modo = ModoApresentacao.DETALHES),
        )
        publicarOverlay()
        _irParaFrente.tryEmit(Unit)
    }

    fun abrirDashboard() {
        state = state.copy(
            dashboardVisivel = true,
            historicoVisivel = false,
            configuracoesVisivel = false,
            semaforoVisivel = false,
            opcoesVisivel = false,
            confirmacaoFecharVisivel = false,
            confirmacaoLimparHistoricoVisivel = false,
            seloFlutuante = false,
            seloEscondido = false,
            interfaceOculta = false,
            overlayAtivo = state.monitorando,
            historicoDia = CalendarioApp.hoje(),
            calendarioPeriodo = CalendarioPeriodo.DIA,
            corrida = state.corrida.copy(modo = ModoApresentacao.DETALHES),
        )
        publicarOverlay()
        _irParaFrente.tryEmit(Unit)
    }

    fun fecharDashboard() {
        fecharTelaNativaParaAtalho()
    }

    fun abrirHistoricoPeloOverlay() {
        if (!state.historicoVisivel) {
            alternarHistorico()
        } else {
            publicarOverlay()
            _irParaFrente.tryEmit(Unit)
        }
    }

    fun fecharConfiguracoes() {
        fecharTelaNativaParaAtalho()
    }

    /** Fecha tela nativa e volta à aba Opções do Menu (não ao overlay Atalhos). */
    fun voltarParaAtalho() {
        fecharTelaNativaParaAtalho()
    }

    private fun fecharTelaNativaParaAtalho() {
        val periodoDashboard = calendarioPeriodoAntesHistorico ?: state.calendarioPeriodo
        val diaDashboard = historicoDiaAntesHistorico ?: state.historicoDia
        calendarioPeriodoAntesHistorico = null
        historicoDiaAntesHistorico = null
        state = state.copy(
            historicoVisivel = false,
            configuracoesVisivel = false,
            dashboardVisivel = false,
            semaforoVisivel = false,
            opcoesVisivel = true,
            detalhesCorridaVisivel = false,
            confirmacaoFecharVisivel = false,
            confirmacaoLimparHistoricoVisivel = false,
            destacarPermissoes = state.avisoSemMonitoramento,
            recentesConfig = false,
            interfaceOculta = false,
            seloFlutuante = false,
            seloEscondido = false,
            compactaTemporaria = false,
            overlayAtivo = state.monitorando,
            calendarioPeriodo = periodoDashboard,
            historicoDia = diaDashboard,
            corrida = state.corrida.copy(modo = ModoApresentacao.DETALHES),
        )
        publicarOverlay()
        _irParaFrente.tryEmit(Unit)
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
            interfaceOculta = true,
            overlayAtivo = true,
            notificacaoDisponivel = analise != null,
            seloFlutuante = false,
            compactaTemporaria = false,
            monitorando = true,
            seloOffsetX = state.seloOffsetX,
            seloOffsetY = state.seloOffsetY,
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
        _irParaSegundoPlano.tryEmit(Unit)
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
                detalhesCorridaVisivel = false,
            )
        } else {
            atuais.add(chave)
            state = state.copy(
                historicoChavesSelecionadas = atuais,
                historicoSelecionado = item,
                detalhesCorridaVisivel = false,
            )
        }
        publicarOverlay()
    }

    fun abrirDetalhesCorrida(item: HistoricoItemPresentation) {
        state = state.copy(
            historicoSelecionado = item,
            detalhesCorridaVisivel = true,
            confirmacaoLimparHistoricoVisivel = false,
        )
        publicarOverlay()
    }

    fun fecharDetalhesCorrida() {
        state = state.copy(detalhesCorridaVisivel = false)
        publicarOverlay()
    }

    fun excluirCorridaDosDetalhes() {
        val item = state.historicoSelecionado ?: return
        state = state.copy(
            historicoChavesSelecionadas = setOf(item.chaveHistorico()),
            detalhesCorridaVisivel = false,
            confirmacaoLimparHistoricoVisivel = true,
        )
        publicarOverlay()
    }

    fun selecionarAbaHistorico(aba: String) {
        state = state.copy(abaHistorico = aba)
        publicarOverlay()
    }

    fun selecionarDiaHistorico(epochDay: Long) {
        state = state.copy(historicoDia = CalendarioApp.diaDe(epochDay))
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

    /** Carteira: Dia/Semana saltam ±7 dias; Mês/Ano saltam ±1 ano. */
    fun avancarPeriodoCarteira(delta: Int) {
        val dia = state.historicoDia
        val novo = when (state.calendarioPeriodo) {
            CalendarioPeriodo.DIA, CalendarioPeriodo.SEMANA ->
                CalendarioApp.avancarSemana(dia, delta)
            CalendarioPeriodo.MES, CalendarioPeriodo.ANO ->
                CalendarioApp.avancarAno(dia, delta)
        }
        state = state.copy(historicoDia = novo)
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
        val itemHistorico = PresentationBuilder.historicoDe(analise)
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
                seloFlutuante = true,
                compactaTemporaria = false,
                monitorando = true,
                seloOffsetX = state.seloOffsetX,
                seloOffsetY = state.seloOffsetY,
                estadoSalvo = state.estadoSalvo,
                corridaAceita = false,
                ultimaCorridaAceita = analise,
                ofertaAtiva = true,
                ofertasPendentes = pendentes,
                onboardingEtapa = state.onboardingEtapa,
                tutorialPasso = state.tutorialPasso,
            )
        } else {
            // Aceite: compacta some junto; limpa oferta e aguarda a próxima.
            cancelarCompactaTemporaria()
            ofertaParaHistorico = null
            state = PresentationBuilder.criarEstado(
                analise = null,
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
                notificacaoDisponivel = false,
                seloFlutuante = true,
                compactaTemporaria = false,
                monitorando = true,
                seloOffsetX = state.seloOffsetX,
                seloOffsetY = state.seloOffsetY,
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
        if (!state.monitorando) {
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

    fun voltarPelaBarra() {
        if (!state.monitorando) {
            return
        }
        if (state.confirmacaoFecharVisivel) {
            cancelarFecharApp()
            return
        }
        if (state.confirmacaoLimparHistoricoVisivel) {
            cancelarLimparHistorico()
            return
        }
        if (state.detalhesCorridaVisivel) {
            fecharDetalhesCorrida()
            return
        }
        if (state.dashboardVisivel || state.historicoVisivel || state.configuracoesVisivel || state.semaforoVisivel) {
            fecharTelaNativaParaAtalho()
            return
        }
        val noMenu = state.corrida.modo == ModoApresentacao.DETALHES &&
            !state.seloFlutuante &&
            !state.seloEscondido
        if (noMenu || state.ofertaAtiva || !state.seloFlutuante) {
            irParaSelo()
        }
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
        val telaNativaSalva = salvo != null && (
            salvo.historicoVisivel ||
                salvo.configuracoesVisivel ||
                salvo.dashboardVisivel ||
                salvo.semaforoVisivel ||
                salvo.confirmacaoFecharVisivel ||
                salvo.confirmacaoLimparHistoricoVisivel
            )
        // §44 caso 2: selo no X + tela Menu guardada (Recentes) → Abrir App reabre o Menu.
        // Selo volta a ficar ativo; aparece de novo ao sair da tela (fluxo normal).
        if (salvo != null && telaNativaSalva) {
            cancelarCompactaTemporaria()
            state = state.copy(
                estadoSalvo = null,
                recentesConfig = false,
                historicoVisivel = salvo.historicoVisivel,
                configuracoesVisivel = salvo.configuracoesVisivel,
                dashboardVisivel = salvo.dashboardVisivel,
                semaforoVisivel = salvo.semaforoVisivel,
                seloFlutuante = false,
                seloEscondido = false,
                compactaTemporaria = false,
                interfaceOculta = false,
                overlayAtivo = true,
                abaConfiguracao = salvo.abaConfiguracao,
                confirmacaoFecharVisivel = salvo.confirmacaoFecharVisivel,
                confirmacaoLimparHistoricoVisivel = salvo.confirmacaoLimparHistoricoVisivel,
                corrida = state.corrida.copy(modo = salvo.modo),
            )
            publicarOverlay()
            _irParaFrente.tryEmit(Unit)
            return
        }
        // §44 caso 1: só selo no X → Abrir App mostra o selo de novo.
        if (state.seloEscondido) {
            irParaSelo()
            return
        }
        if (salvo == null) {
            publicarOverlay()
            _irParaSegundoPlano.tryEmit(Unit)
            return
        }
        cancelarCompactaTemporaria()
        state = state.copy(
            estadoSalvo = null,
            recentesConfig = false,
            historicoVisivel = false,
            configuracoesVisivel = false,
            dashboardVisivel = false,
            semaforoVisivel = false,
            seloFlutuante = salvo.seloFlutuante,
            seloEscondido = false,
            compactaTemporaria = salvo.compactaTemporaria,
            interfaceOculta = true,
            overlayAtivo = true,
            abaConfiguracao = salvo.abaConfiguracao,
            confirmacaoFecharVisivel = false,
            confirmacaoLimparHistoricoVisivel = false,
            corrida = state.corrida.copy(modo = salvo.modo),
        )
        publicarOverlay()
        _irParaSegundoPlano.tryEmit(Unit)
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
                semaforoVisivel = state.semaforoVisivel,
                opcoesVisivel = state.opcoesVisivel,
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
            semaforoVisivel = false,
            recentesConfig = false,
            interfaceOculta = true,
            seloFlutuante = false,
            seloEscondido = false,
            overlayAtivo = true,
            corrida = state.corrida.copy(modo = ModoApresentacao.DETALHES),
        )
        publicarOverlay()
        _irParaSegundoPlano.tryEmit(Unit)
    }

    fun recolherCompactaPorToqueFora() {
        // Compacta só acompanha oferta; toque fora não a mantém artificialmente.
        if (!state.ofertaAtiva) {
            irParaSelo()
        }
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
        // Se havia tela do Menu aberta, guarda para Abrir App (§44 caso 2).
        guardarTelaSeVazio()
        state = state.copy(
            seloEscondido = true,
            seloFlutuante = false,
            compactaTemporaria = false,
            historicoVisivel = false,
            configuracoesVisivel = false,
            dashboardVisivel = false,
            semaforoVisivel = false,
            confirmacaoFecharVisivel = false,
            confirmacaoLimparHistoricoVisivel = false,
            overlayAtivo = true,
            interfaceOculta = true,
            corrida = state.corrida.copy(modo = ModoApresentacao.COMPACTA),
        )
        publicarOverlay()
    }

    fun abrirAtalhoConfig(indice: Int) {
        state = state.copy(
            seloEscondido = false,
            seloFlutuante = false,
            interfaceOculta = false,
            overlayAtivo = state.monitorando,
            historicoVisivel = false,
            dashboardVisivel = false,
            semaforoVisivel = false,
            opcoesVisivel = false,
            configuracoesVisivel = true,
            confirmacaoFecharVisivel = false,
            confirmacaoLimparHistoricoVisivel = false,
            abaConfiguracao = indice.coerceIn(0, 2),
            corrida = state.corrida.copy(modo = ModoApresentacao.DETALHES),
        )
        publicarOverlay()
        _irParaFrente.tryEmit(Unit)
    }

    fun retrairParaCompactaTemporaria() {
        // Compacta só existe com oferta ativa; sem oferta volta ao selo.
        if (state.ofertaAtiva) {
            cancelarCompactaTemporaria()
            state = state.copy(
                interfaceOculta = true,
                seloFlutuante = false,
                seloEscondido = false,
                compactaTemporaria = false,
                historicoVisivel = false,
                configuracoesVisivel = false,
                dashboardVisivel = false,
                overlayAtivo = true,
                monitorando = true,
                corrida = state.corrida.copy(
                    modo = ModoApresentacao.COMPACTA,
                    acaoDetalhes = "ⓘ",
                ),
            )
            publicarOverlay()
            _irParaSegundoPlano.tryEmit(Unit)
            return
        }
        irParaSelo()
    }

    // ================================================================
    // REABRIR PELO SELO → TELA EXPANDIDA
    // ================================================================

    fun reabrirInterface(@Suppress("UNUSED_PARAMETER") origemCompacta: Boolean = false) {
        if (origemCompacta) {
            return
        }
        cancelarCompactaTemporaria()
        // §44 Recentes: toque no selo reabre a última tela nativa (ex.: Histórico).
        if (state.estadoSalvo != null) {
            restaurarTelaAposRecentes()
            return
        }
        // Selo → esconde selo e abre Atalhos overlay. Fechar Atalhos = X ou toque fora.
        state = state.copy(
            interfaceOculta = true,
            seloFlutuante = false,
            seloEscondido = false,
            compactaTemporaria = false,
            overlayAtivo = true,
            monitorando = true,
            historicoVisivel = false,
            configuracoesVisivel = false,
            dashboardVisivel = false,
            semaforoVisivel = false,
            opcoesVisivel = false,
            corrida = state.corrida.copy(
                modo = ModoApresentacao.DETALHES,
                acaoDetalhes = "Menos detalhes",
            ),
            estadoSalvo = null,
        )
        publicarOverlay()
        _irParaSegundoPlano.tryEmit(Unit)
    }

    /** X ou toque fora dos Atalhos / Opções → fecha e devolve o selo (se monitorando). */
    fun fecharAtalhosParaSelo() {
        if (!state.monitorando) {
            return
        }
        if (state.historicoVisivel ||
            state.configuracoesVisivel ||
            state.dashboardVisivel ||
            state.semaforoVisivel ||
            state.confirmacaoFecharVisivel ||
            state.confirmacaoLimparHistoricoVisivel
        ) {
            return
        }
        if (state.opcoesVisivel || state.corrida.modo == ModoApresentacao.DETALHES) {
            irParaSelo()
        }
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
        state = state.copy(
            confirmacaoFecharVisivel = true,
            confirmacaoLimparHistoricoVisivel = false,
            historicoVisivel = false,
            configuracoesVisivel = false,
            dashboardVisivel = false,
            seloFlutuante = false,
            seloEscondido = false,
            interfaceOculta = false,
            corrida = state.corrida.copy(
                modo = ModoApresentacao.DETALHES,
                acaoDetalhes = "Menos detalhes",
            ),
        )
        publicarOverlay()
        _irParaFrente.tryEmit(Unit)
    }

    fun cancelarFecharApp() {
        val anterior = estadoAntesFechar
        estadoAntesFechar = null
        if (anterior != null) {
            state = anterior.copy(confirmacaoFecharVisivel = false)
            publicarOverlay()
            if (state.interfaceOculta) {
                _irParaSegundoPlano.tryEmit(Unit)
            } else if (state.historicoVisivel || state.configuracoesVisivel || state.dashboardVisivel) {
                _irParaFrente.tryEmit(Unit)
            } else {
                fecharTelaNativaParaAtalho()
            }
            return
        }
        fecharTelaNativaParaAtalho()
    }

    fun confirmarFecharApp() {
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
            interfaceOculta = false,
            corrida = state.corrida.copy(modo = ModoApresentacao.DETALHES),
        )
        publicarOverlay()
        _irParaFrente.tryEmit(Unit)
    }

    fun cancelarLimparHistorico() {
        estadoAntesLimparHistorico = null
        state = state.copy(
            confirmacaoLimparHistoricoVisivel = false,
            historicoVisivel = true,
            configuracoesVisivel = false,
            dashboardVisivel = false,
            seloFlutuante = false,
            interfaceOculta = false,
            corrida = state.corrida.copy(modo = ModoApresentacao.DETALHES),
        )
        publicarOverlay()
        _irParaFrente.tryEmit(Unit)
    }

    fun confirmarLimparHistorico() {
        val chaves = state.historicoChavesSelecionadas
        if (chaves.isEmpty()) {
            estadoAntesLimparHistorico = null
            state = state.copy(
                confirmacaoLimparHistoricoVisivel = false,
                historicoVisivel = true,
                interfaceOculta = false,
            )
            publicarOverlay()
            _irParaFrente.tryEmit(Unit)
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
            interfaceOculta = false,
            corrida = state.corrida.copy(
                modo = ModoApresentacao.DETALHES,
                acaoDetalhes = "Menos detalhes",
            ),
        )
        publicarOverlay()
        _irParaFrente.tryEmit(Unit)
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
        // Sem monitoramento ativo: não calcula nem exibe compacta.
        if (!state.monitorando) {
            return
        }
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
                estadoSalvo = state.estadoSalvo,
                corridaAceita = false,
                ultimaCorridaAceita = state.ultimaCorridaAceita,
                ofertaAtiva = true,
                corridaAntesDaOferta = state.historicoSelecionado?.paraAnalise(),
                ofertasPendentes = pendentes,
                onboardingEtapa = state.onboardingEtapa,
                tutorialPasso = state.tutorialPasso,
            ).copy(
                seloEscondido = false,
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
                estadoSalvo = state.estadoSalvo,
                corridaAceita = false,
                ultimaCorridaAceita = state.ultimaCorridaAceita,
                ofertaAtiva = true,
                ofertasPendentes = pendentes,
                onboardingEtapa = state.onboardingEtapa,
                tutorialPasso = state.tutorialPasso,
            )
        } else {
            // Expirou/recusou: compacta some junto; limpa oferta e aguarda a próxima.
            cancelarCompactaTemporaria()
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
                seloFlutuante = true,
                compactaTemporaria = false,
                monitorando = true,
                seloOffsetX = state.seloOffsetX,
                seloOffsetY = state.seloOffsetY,
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

    private fun irParaSelo() {
        cancelarCompactaTemporaria()
        state = state.copy(
            monitorando = true,
            overlayAtivo = true,
            interfaceOculta = true,
            seloFlutuante = true,
            seloEscondido = false,
            compactaTemporaria = false,
            historicoVisivel = false,
            configuracoesVisivel = false,
            dashboardVisivel = false,
            semaforoVisivel = false,
            opcoesVisivel = false,
            recentesConfig = false,
            confirmacaoFecharVisivel = false,
            corrida = state.corrida.copy(
                modo = ModoApresentacao.COMPACTA,
                acaoDetalhes = "ⓘ",
            ),
        )
        publicarOverlay()
        _irParaSegundoPlano.tryEmit(Unit)
    }

    private fun cancelarCompactaTemporaria() {
        compactaTemporariaJob?.cancel()
        compactaTemporariaJob = null
    }

    private fun publicarOverlay() {
        val emOverlay = state.monitorando && state.overlayAtivo && state.interfaceOculta
        val telaNativa = state.historicoVisivel ||
            state.configuracoesVisivel ||
            state.dashboardVisivel ||
            state.semaforoVisivel ||
            state.opcoesVisivel ||
            state.confirmacaoFecharVisivel ||
            state.confirmacaoLimparHistoricoVisivel
        // Compacta só com oferta ativa, monitoramento ligado e fora do Menu.
        val compactaVisivel = emOverlay &&
            state.monitorando &&
            !state.seloEscondido &&
            !state.seloFlutuante &&
            state.ofertaAtiva &&
            state.corrida.modo != ModoApresentacao.DETALHES
        // §44: overlay = só selo, atalhos (expandidaVisivel) e compacta.
        // Fluxo: selo → Atalhos (selo some); X/fora → Atalhos fecham e selo volta.
        val expandidaVisivel = emOverlay &&
            state.monitorando &&
            !state.seloEscondido &&
            !compactaVisivel &&
            !telaNativa &&
            state.corrida.modo == ModoApresentacao.DETALHES
        val seloVisivel = emOverlay &&
            state.monitorando &&
            !state.seloEscondido &&
            !compactaVisivel &&
            !telaNativa &&
            !expandidaVisivel
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
        OverlayBridge.publicar(
            OverlaySnapshot(
                monitorando = state.monitorando,
                seloVisivel = seloVisivel,
                compactaVisivel = compactaVisivel,
                expandidaVisivel = expandidaVisivel,
                // §44: nunca publicar painéis de menu no overlay — só Activity.
                historicoVisivel = false,
                configuracoesVisivel = false,
                dashboardVisivel = false,
                confirmacaoFecharVisivel = false,
                confirmacaoLimparHistoricoVisivel = false,
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
                kmAtePassageiro = detalhes["km_ate_passageiro"] ?: "—",
                kmViagem = detalhes["km_viagem"] ?: "—",
                quantidadeParadas = analise?.corrida?.quantidadeParadas ?: 0,
                plataformaSigla = siglaPlataforma(analise?.plataforma),
                tempoHm = formatarTempoHm(analise?.tempoEstimado),
                enderecoEmbarque = analise?.corrida?.enderecoEmbarque,
                enderecoDestino = analise?.corrida?.enderecoDestino,
                corridaAceita = state.corridaAceita,
                corClassificacao = if (analise == null) {
                    br.com.gestordriver.core.ClassificacaoConstantes.COR_BORDA_NEUTRA
                } else {
                    state.corrida.corClassificacao
                },
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
        if (state.ofertaAtiva) {
            return state.analiseAtual
        }
        if (state.historicoVisivel) {
            return state.historicoSelecionado?.paraAnalise()
        }
        // Notificação / última aceita — sem reabrir compacta.
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

    private fun formatarTempoHm(minutos: Int?): String {
        if (minutos == null) {
            return "—"
        }
        val horas = minutos / 60
        val resto = minutos % 60
        return "${horas}h${resto}m"
    }

    private fun siglaPlataforma(plataforma: String?): String {
        val nome = plataforma.orEmpty()
        return when {
            nome.contains("99") -> "99"
            nome.contains("inDrive", ignoreCase = true) -> "in"
            nome.isBlank() -> ""
            else -> "U"
        }
    }

}