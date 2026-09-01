package br.com.gestordriver.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestordriver.core.AnaliseCorrida
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
import kotlinx.coroutines.delay
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
                    OverlayAcao.AbrirHistorico -> alternarHistorico()
                    OverlayAcao.AbrirConfig -> alternarConfiguracoes()
                    OverlayAcao.SalvarConfig -> fecharConfiguracoes()
                    OverlayAcao.CancelarConfig -> fecharConfiguracoes()
                    OverlayAcao.Ocultar -> ocultarInterface()
                    OverlayAcao.Retratil -> retrairParaCompactaTemporaria()
                    OverlayAcao.ToqueForaDaCompacta -> recolherCompactaPorToqueFora()
                    OverlayAcao.Fechar -> solicitarFecharApp()
                    OverlayAcao.CancelarFechar -> cancelarFecharApp()
                    OverlayAcao.ConfirmarFechar -> confirmarFecharApp()
                    OverlayAcao.SolicitarLimparHistorico -> solicitarLimparHistorico()
                    OverlayAcao.CancelarLimparHistorico -> cancelarLimparHistorico()
                    OverlayAcao.ConfirmarLimparHistorico -> confirmarLimparHistorico()
                    is OverlayAcao.SelecionarHistorico -> selecionarHistoricoPorChave(acao.chave)
                    is OverlayAcao.AbaHistorico -> selecionarAbaHistorico(acao.aba)
                    is OverlayAcao.AbaConfiguracao -> {
                        state = state.copy(
                            abaConfiguracao = acao.indice.coerceIn(0, 3),
                            historicoVisivel = false,
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
        if (state.monitorando) {
            publicarOverlay()
            if (state.interfaceOculta) {
                _irParaSegundoPlano.tryEmit(Unit)
            }
            return
        }
        irParaSelo()
    }

    fun avaliarInicio(permissoesOk: Boolean, temConta: Boolean) {
        if (onboardingStore.concluido()) {
            if (!permissoesOk) {
                state = state.copy(
                    onboardingEtapa = OnboardingEtapa.PERMISSOES,
                    tutorialPasso = 0,
                    interfaceOculta = false,
                    destacarPermissoes = true,
                    monitorando = false,
                )
                return
            }
            if (state.onboardingEtapa != OnboardingEtapa.NENHUMA) {
                concluirOnboarding()
                return
            }
            iniciarMonitoramento()
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
        state = state.copy(
            onboardingEtapa = OnboardingEtapa.NENHUMA,
            tutorialPasso = 0,
            destacarPermissoes = false,
            interfaceOculta = true,
        )
        irParaSelo()
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
        state = state.copy(
            historicoVisivel = true,
            configuracoesVisivel = false,
            interfaceOculta = true,
            seloFlutuante = false,
            corrida = state.corrida.copy(
                modo = ModoApresentacao.DETALHES,
                acaoDetalhes = "Menos detalhes",
            ),
        )
        publicarOverlay()
        _irParaSegundoPlano.tryEmit(Unit)
    }

    private fun fecharHistoricoELimpar() {
        val manterOferta = state.ofertaAtiva
        val analise = if (manterOferta) state.analiseAtual else null
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
            notificacaoDisponivel = manterOferta,
            seloFlutuante = false,
            compactaTemporaria = false,
            monitorando = true,
            seloOffsetX = state.seloOffsetX,
            seloOffsetY = state.seloOffsetY,
            estadoSalvo = state.estadoSalvo,
            corridaAceita = false,
            ultimaCorridaAceita = state.ultimaCorridaAceita,
            ofertaAtiva = manterOferta,
            corridaAntesDaOferta = null,
            ofertasPendentes = state.ofertasPendentes,
            onboardingEtapa = state.onboardingEtapa,
            tutorialPasso = state.tutorialPasso,
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

    fun abrirConfiguracoes(destaquePermissao: Boolean = false, usarOverlay: Boolean = true) {
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
            interfaceOculta = usarOverlay,
            overlayAtivo = true,
            notificacaoDisponivel = manterOferta,
            seloFlutuante = false,
            compactaTemporaria = false,
            monitorando = true,
            seloOffsetX = state.seloOffsetX,
            seloOffsetY = state.seloOffsetY,
            estadoSalvo = state.estadoSalvo,
            corridaAceita = false,
            ultimaCorridaAceita = state.ultimaCorridaAceita,
            ofertaAtiva = manterOferta,
            corridaAntesDaOferta = null,
            ofertasPendentes = state.ofertasPendentes,
            onboardingEtapa = state.onboardingEtapa,
            tutorialPasso = state.tutorialPasso,
        )
        publicarOverlay()
        if (usarOverlay) {
            _irParaSegundoPlano.tryEmit(Unit)
        }
    }

    fun abrirHistoricoPeloOverlay() {
        if (!state.historicoVisivel) {
            alternarHistorico()
        } else {
            publicarOverlay()
        }
    }

    fun fecharConfiguracoes() {
        state = state.copy(
            configuracoesVisivel = false,
            destacarPermissoes = false,
            interfaceOculta = true,
            seloFlutuante = false,
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
        selecionarHistorico(item)
    }

    fun selecionarAbaHistorico(aba: String) {
        state = state.copy(abaHistorico = aba)
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
        if (!state.monitorando || state.interfaceOculta) {
            return
        }
        ocultarInterface()
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
            delay(COMPACTA_TEMPORARIA_MS)
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
        cancelarCompactaTemporaria()
        state = state.copy(
            interfaceOculta = true,
            seloFlutuante = false,
            compactaTemporaria = false,
            overlayAtivo = true,
            monitorando = true,
            historicoVisivel = false,
            configuracoesVisivel = false,
            corrida = state.corrida.copy(
                modo = ModoApresentacao.DETALHES,
                acaoDetalhes = "Menos detalhes",
            ),
            estadoSalvo = null,
        )
        publicarOverlay()
        _irParaSegundoPlano.tryEmit(Unit)
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
        estadoAntesLimparHistorico = state
        state = state.copy(
            confirmacaoLimparHistoricoVisivel = true,
            confirmacaoFecharVisivel = false,
            historicoVisivel = false,
            configuracoesVisivel = false,
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

    fun cancelarLimparHistorico() {
        val anterior = estadoAntesLimparHistorico
        estadoAntesLimparHistorico = null
        if (anterior != null) {
            state = anterior.copy(confirmacaoLimparHistoricoVisivel = false)
            publicarOverlay()
            if (state.interfaceOculta) {
                _irParaSegundoPlano.tryEmit(Unit)
            }
            return
        }
        state = state.copy(
            confirmacaoLimparHistoricoVisivel = false,
            historicoVisivel = true,
        )
        publicarOverlay()
    }

    fun confirmarLimparHistorico() {
        historicoRepository.limpar()
        estadoAntesLimparHistorico = null
        state = state.copy(
            historico = emptyList(),
            historicoSelecionado = null,
            confirmacaoLimparHistoricoVisivel = false,
            historicoVisivel = true,
            configuracoesVisivel = false,
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
        val emAppExpandido = !state.interfaceOculta && state.monitorando
        val expandidaOverlay = state.interfaceOculta &&
            state.corrida.modo == ModoApresentacao.DETALHES &&
            !state.seloFlutuante
        val manterDetalhes = emAppExpandido || expandidaOverlay || state.historicoVisivel
        state =
            PresentationBuilder.criarEstado(
                analise = analise,
                plano = state.plano,
                historico = state.historico,
                historicoSelecionado = state.historicoSelecionado,
                abaHistorico = state.abaHistorico,
                abaConfiguracao = state.abaConfiguracao,
                destacarPermissoes = false,
                modo = if (manterDetalhes) ModoApresentacao.DETALHES else ModoApresentacao.COMPACTA,
                historicoVisivel = state.historicoVisivel && !state.configuracoesVisivel,
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
            )
        publicarOverlay()
        _irParaSegundoPlano.tryEmit(Unit)
    }

    internal fun expirarOfertaAtual() {
        if (state.corridaAceita || !state.ofertaAtiva) {
            return
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
            compactaTemporaria = false,
            historicoVisivel = false,
            configuracoesVisivel = false,
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
        val expandidaVisivel = emOverlay &&
            state.corrida.modo == ModoApresentacao.DETALHES &&
            !state.seloFlutuante
        val compactaVisivel = emOverlay &&
            !expandidaVisivel &&
            !state.seloFlutuante &&
            (state.ofertaAtiva || state.compactaTemporaria)
        val seloVisivel = emOverlay && !compactaVisivel && !expandidaVisivel
        val analise = analiseExibida()
        val campos = state.corrida.camposCompactos.associate { it.id to it.valor }
        val detalhes = state.corrida.camposDetalhes.associate { it.id to it.valor }
        val itensHistorico = state.historico
            .sortedByDescending { it.dataHoraRegistro ?: java.time.LocalDateTime.MIN }
            .filter { it.pertenceAba(state.abaHistorico) }
            .map { item ->
                OverlayHistoricoItem(
                    chave = item.chaveHistorico(),
                    data = item.dataLista,
                    hora = item.horaLista,
                    valorPorKm = PresentationBuilder.formatarDinheiroHistorico(item.valorPorKm),
                    valor = PresentationBuilder.formatarDinheiroHistorico(item.valorTotal),
                    km = PresentationBuilder.formatarDistanciaHistorico(item.kmTotal),
                    tempo = PresentationBuilder.formatarTempoHistorico(item.tempoEstimado),
                    nota = item.notaPassageiro?.let { PresentationBuilder.formatarDecimalPublico(it) } ?: "—",
                    marcador = item.classificacao.marcador,
                    corMarcador = item.corClassificacao,
                    plataforma = item.plataforma,
                )
            }
        OverlayBridge.publicar(
            OverlaySnapshot(
                monitorando = state.monitorando,
                seloVisivel = seloVisivel,
                compactaVisivel = compactaVisivel,
                expandidaVisivel = expandidaVisivel,
                historicoVisivel = expandidaVisivel && state.historicoVisivel,
                configuracoesVisivel = expandidaVisivel && state.configuracoesVisivel,
                confirmacaoFecharVisivel = expandidaVisivel && state.confirmacaoFecharVisivel,
                confirmacaoLimparHistoricoVisivel = expandidaVisivel && state.confirmacaoLimparHistoricoVisivel,
                historicoAba = state.abaHistorico,
                historicoItens = itensHistorico,
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
                aguardandoOferta = analise == null,
                liquidoPorKm = PresentationBuilder.formatarLiquidoPorKm(analise),
                litrosEstimados = detalhes["combustivel_estimado"] ?: "—",
                gastoEstimado = detalhes["custo_combustivel"] ?: "—",
                lucroEstimado = detalhes["lucro_estimado"] ?: "—",
                kmAtePassageiro = detalhes["km_ate_passageiro"] ?: "—",
                kmViagem = detalhes["km_viagem"] ?: "—",
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

    private fun analiseExibida(): AnaliseCorrida? {
        if (state.ofertaAtiva) {
            return state.analiseAtual
        }
        if (state.historicoVisivel) {
            return state.historicoSelecionado?.paraAnalise()
        }
        return null
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

    companion object {
        const val COMPACTA_TEMPORARIA_MS = 5_000L
    }
}