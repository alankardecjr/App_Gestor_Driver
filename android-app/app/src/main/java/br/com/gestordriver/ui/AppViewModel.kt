package br.com.gestordriver.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestordriver.core.AnaliseCorrida
import br.com.gestordriver.core.chaveOferta
import br.com.gestordriver.data.HistoricoRepository
import br.com.gestordriver.data.MemoriaHistoricoRepository
import br.com.gestordriver.data.chaveHistorico
import br.com.gestordriver.model.HistoricoItemPresentation
import br.com.gestordriver.model.ModoApresentacao
import br.com.gestordriver.model.PlanoAcesso
import br.com.gestordriver.notification.RideNotificationBus
import br.com.gestordriver.notification.RideNotificationEvent
import br.com.gestordriver.overlay.OverlayAcao
import br.com.gestordriver.overlay.OverlayBridge
import br.com.gestordriver.overlay.OverlaySnapshot
import br.com.gestordriver.presentation.PresentationBuilder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AppViewModel(
    private val historicoRepository: HistoricoRepository = MemoriaHistoricoRepository(),
) : ViewModel() {

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

    // ================================================================
    // NOTIFICAÇÕES
    // ================================================================

    init {
        viewModelScope.launch {
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

        viewModelScope.launch {
            OverlayBridge.acoes.collect { acao ->
                when (acao) {
                    OverlayAcao.Reabrir -> reabrirInterface()
                    is OverlayAcao.MoverSelo -> atualizarPosicaoSelo(acao.offsetX, acao.offsetY)
                }
            }
        }

        val historicoPersistido = historicoRepository.listar()
        if (historicoPersistido.isNotEmpty()) {
            state = state.copy(
                historico = historicoPersistido,
                ultimaCorridaAceita = historicoPersistido.last().paraAnalise(),
            )
            publicarOverlay()
        }
    }

    fun iniciarMonitoramento() {
        state = state.copy(
            monitorando = true,
            seloFlutuante = true,
            interfaceOculta = true,
            overlayAtivo = true,
            historicoVisivel = false,
            configuracoesVisivel = false,
        )
        publicarOverlay()
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
        )
        publicarOverlay()
    }

    // ================================================================
    // EXPANDIR / RETRAIR
    // ================================================================

    fun alternarDetalhes() {

        val modo =
            if (
                state.corrida.modo ==
                ModoApresentacao.COMPACTA
            ) {
                ModoApresentacao.DETALHES
            } else {
                ModoApresentacao.COMPACTA
            }

        state =
            state.copy(
                corrida =
                    state.corrida.copy(
                        modo = modo,
                        acaoDetalhes =
                            if (
                                modo ==
                                ModoApresentacao.DETALHES
                            ) {
                                "Menos detalhes"
                            } else {
                                "ⓘ"
                            }
                    )
            )
    }

    // ================================================================
    // HISTÓRICO
    // ================================================================

    fun alternarHistorico() {

        val novoEstado =
            !state.historicoVisivel

        state =
            state.copy(
                historicoVisivel = novoEstado,
                configuracoesVisivel =
                    if (novoEstado) {
                        false
                    } else {
                        state.configuracoesVisivel
                    }
            )
    }

    // ================================================================
    // CONFIGURAÇÕES
    // ================================================================

    fun abrirConfiguracoes() {

        state =
            state.copy(
                configuracoesVisivel = true,
                historicoVisivel = false
            )
    }

    fun fecharConfiguracoes() {

        state =
            state.copy(
                configuracoesVisivel = false
            )
    }

    // ================================================================
    // HISTÓRICO — SELEÇÃO
    // ================================================================

    fun selecionarHistorico(
        item: HistoricoItemPresentation
    ) {
        state = state.copy(
            historicoSelecionado = item,
        )
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
        val analise = state.analiseAtual ?: return
        val itemHistorico = PresentationBuilder.historicoDe(analise)
        if (state.historico.any { it.chaveHistorico() == itemHistorico.chaveHistorico() }) {
            return
        }
        historicoRepository.salvar(itemHistorico)
        state = state.copy(
            historico = state.historico + itemHistorico,
            historicoSelecionado = itemHistorico,
            corridaAceita = true,
            ofertaAtiva = false,
            ultimaCorridaAceita = analise,
        )
        publicarOverlay()
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

        state =
            state.copy(
                historicoVisivel = false,
                configuracoesVisivel = false,
                overlayAtivo = false,
                notificacaoDisponivel = false,
                seloFlutuante = true,
                interfaceOculta = true,
                monitorando = true
            )
    }

    // ================================================================
    // OCULTAR
    // ================================================================

    fun ocultarInterface() {

        state =
            state.copy(
                estadoSalvo =
                    EstadoInterfaceSalvo(
                        modo =
                            state.corrida.modo,

                        historicoVisivel =
                            state.historicoVisivel,

                        configuracoesVisivel =
                            state.configuracoesVisivel
                    ),

                interfaceOculta = true,

                seloFlutuante = true,

                historicoVisivel = false,

                configuracoesVisivel = false,

                corrida =
                    state.corrida.copy(
                        modo =
                            ModoApresentacao.COMPACTA,

                        acaoDetalhes = "ⓘ"
                    ),

                monitorando = true
            )
    }

    // ================================================================
    // REABRIR PELO SELO
    // ================================================================

    fun reabrirInterface() {

        val salvo =
            state.estadoSalvo

        state =
            state.copy(
                interfaceOculta = false,

                seloFlutuante = false,

                overlayAtivo = true,

                monitorando = true,

                corrida =
                    state.corrida.copy(
                        modo =
                            salvo?.modo
                                ?: ModoApresentacao.COMPACTA,

                        acaoDetalhes =
                            if (
                                salvo?.modo ==
                                ModoApresentacao.DETALHES
                            ) {
                                "Menos detalhes"
                            } else {
                                "ⓘ"
                            }
                    ),

                historicoVisivel =
                    salvo?.historicoVisivel
                        ?: false,

                configuracoesVisivel =
                    salvo?.configuracoesVisivel
                        ?: false,

                estadoSalvo = null
            )
    }

    // ================================================================
    // FECHAMENTO
    // ================================================================

    fun solicitarFecharApp() {

        state =
            state.copy(
                confirmacaoFecharVisivel = true
            )
    }

    fun cancelarFecharApp() {

        state =
            state.copy(
                confirmacaoFecharVisivel = false
            )
    }

    fun confirmarFecharApp() {

        state =
            state.copy(
                confirmacaoFecharVisivel = false,
                configuracoesVisivel = false,
                historicoVisivel = false,
                overlayAtivo = false,
                monitorando = false,
                seloFlutuante = false,
                interfaceOculta = false
            )

        _fecharApp.tryEmit(Unit)
        publicarOverlay()
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

        state =
            PresentationBuilder.criarEstado(
                analise = analise,
                plano = state.plano,

                // Histórico existente permanece intacto.
                historico = state.historico,

                historicoSelecionado = null,

                modo =
                    ModoApresentacao.COMPACTA,

                historicoVisivel = false,

                configuracoesVisivel = false,

                interfaceOculta = false,

                overlayAtivo = true,

                notificacaoDisponivel = true,

                seloFlutuante = false,

                monitorando = true,

                seloOffsetX =
                    state.seloOffsetX,

                seloOffsetY =
                    state.seloOffsetY,

                estadoSalvo =
                    state.estadoSalvo,

                corridaAceita = false,
                ultimaCorridaAceita = state.ultimaCorridaAceita,
                ofertaAtiva = true,
            )
        publicarOverlay()
    }

    internal fun expirarOfertaAtual() {
        if (state.corridaAceita || !state.ofertaAtiva) {
            return
        }
        val ultima = state.ultimaCorridaAceita
        state = PresentationBuilder.criarEstado(
            analise = ultima,
            plano = state.plano,
            historico = state.historico,
            historicoSelecionado = state.historicoSelecionado,
            modo = ModoApresentacao.COMPACTA,
            historicoVisivel = false,
            configuracoesVisivel = false,
            interfaceOculta = true,
            overlayAtivo = true,
            notificacaoDisponivel = ultima != null,
            seloFlutuante = true,
            monitorando = true,
            seloOffsetX = state.seloOffsetX,
            seloOffsetY = state.seloOffsetY,
            estadoSalvo = null,
            corridaAceita = ultima != null,
            ultimaCorridaAceita = ultima,
            ofertaAtiva = false,
        )
        publicarOverlay()
    }

    private fun publicarOverlay() {
        val campos = state.corrida.camposCompactos.associate { it.id to it.valor }
        OverlayBridge.publicar(
            OverlaySnapshot(
                monitorando = state.monitorando,
                seloVisivel = state.monitorando && state.seloFlutuante && state.interfaceOculta,
                compactaVisivel = state.monitorando && state.overlayAtivo && !state.seloFlutuante,
                offsetX = state.seloOffsetX,
                offsetY = state.seloOffsetY,
                valorPorKm = campos["valor_por_km"] ?: "—",
                valorTotal = campos["valor_total"] ?: "—",
                kmTotal = campos["km_total"] ?: "—",
                tempo = campos["tempo_estimado"] ?: "—",
                nota = campos["nota_passageiro"] ?: "—",
                aguardandoOferta = state.analiseAtual == null,
            ),
        )
    }
}