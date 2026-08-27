package br.com.gestordriver.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestordriver.core.AnaliseCorrida
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AppViewModel(
    private val historicoRepository: HistoricoRepository = MemoriaHistoricoRepository(),
    coroutineScope: CoroutineScope? = null,
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: viewModelScope
    private var compactaTemporariaJob: Job? = null

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
                    OverlayAcao.AbrirHistorico -> abrirHistoricoPeloOverlay()
                    OverlayAcao.AbrirConfig -> abrirConfiguracoes()
                    OverlayAcao.Ocultar -> ocultarInterface()
                    OverlayAcao.Retratil -> retrairParaCompactaTemporaria()
                    OverlayAcao.Fechar -> solicitarFecharApp()
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
        if (state.monitorando) {
            publicarOverlay()
            if (state.interfaceOculta) {
                _irParaSegundoPlano.tryEmit(Unit)
            }
            return
        }
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

        val novoEstado =
            !state.historicoVisivel

        state =
            state.copy(
                historicoVisivel = novoEstado,
                interfaceOculta = if (novoEstado) false else state.interfaceOculta,
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
        state = state.copy(
            configuracoesVisivel = true,
            historicoVisivel = false,
            interfaceOculta = false,
            seloFlutuante = false,
        )
        publicarOverlay()
    }

    fun abrirHistoricoPeloOverlay() {
        state = state.copy(
            historicoVisivel = true,
            configuracoesVisivel = false,
            interfaceOculta = false,
            seloFlutuante = false,
            corrida = state.corrida.copy(
                modo = ModoApresentacao.DETALHES,
                acaoDetalhes = "Menos detalhes",
            ),
        )
        publicarOverlay()
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
            compactaTemporaria = false,
            ultimaCorridaAceita = analise,
            seloFlutuante = state.interfaceOculta,
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

    fun retrairParaCompactaTemporaria() {
        cancelarCompactaTemporaria()
        val semOfertaAtual = !state.ofertaAtiva
        state = state.copy(
            interfaceOculta = true,
            seloFlutuante = false,
            compactaTemporaria = semOfertaAtual,
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
        if (!semOfertaAtual) {
            return
        }
        compactaTemporariaJob = scope.launch {
            delay(COMPACTA_TEMPORARIA_MS)
            if (state.ofertaAtiva || !state.compactaTemporaria) {
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
        state = state.copy(
            confirmacaoFecharVisivel = true,
            interfaceOculta = false,
            seloFlutuante = false,
        )
        publicarOverlay()
    }

    fun cancelarFecharApp() {
        if (state.monitorando) {
            irParaSelo()
            return
        }
        state = state.copy(
            confirmacaoFecharVisivel = false,
        )
    }

    fun confirmarFecharApp() {
        cancelarCompactaTemporaria()
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
        val emAppExpandido = !state.interfaceOculta && state.monitorando
        val expandidaOverlay = state.interfaceOculta &&
            state.corrida.modo == ModoApresentacao.DETALHES &&
            !state.seloFlutuante
        val manterDetalhes = emAppExpandido || expandidaOverlay
        val ultimaExibida = if (state.ofertaAtiva) {
            state.corridaAntesDaOferta
        } else {
            state.analiseAtual
        }
        state =
            PresentationBuilder.criarEstado(
                analise = analise,
                plano = state.plano,
                historico = state.historico,
                historicoSelecionado = if (emAppExpandido) state.historicoSelecionado else null,
                modo = if (manterDetalhes) ModoApresentacao.DETALHES else ModoApresentacao.COMPACTA,
                historicoVisivel = false,
                configuracoesVisivel = false,
                interfaceOculta = !emAppExpandido,
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
                corridaAntesDaOferta = ultimaExibida,
            )
        publicarOverlay()
        if (!emAppExpandido) {
            _irParaSegundoPlano.tryEmit(Unit)
        }
    }

    internal fun expirarOfertaAtual() {
        if (state.corridaAceita || !state.ofertaAtiva) {
            return
        }
        cancelarCompactaTemporaria()
        val manterExpandida = state.corrida.modo == ModoApresentacao.DETALHES && !state.seloFlutuante
        val restaurar = state.corridaAntesDaOferta ?: state.ultimaCorridaAceita
        val ultimaAceita = state.ultimaCorridaAceita
        val offsetsX = state.seloOffsetX
        val offsetsY = state.seloOffsetY
        val estadoSalvo = state.estadoSalvo
        if (manterExpandida) {
            state = PresentationBuilder.criarEstado(
                analise = restaurar,
                plano = state.plano,
                historico = state.historico,
                historicoSelecionado = state.historicoSelecionado,
                modo = ModoApresentacao.DETALHES,
                historicoVisivel = state.historicoVisivel,
                configuracoesVisivel = state.configuracoesVisivel,
                interfaceOculta = state.interfaceOculta,
                overlayAtivo = true,
                notificacaoDisponivel = restaurar != null,
                seloFlutuante = false,
                compactaTemporaria = false,
                monitorando = true,
                seloOffsetX = offsetsX,
                seloOffsetY = offsetsY,
                estadoSalvo = estadoSalvo,
                corridaAceita = restaurar != null && restaurar == ultimaAceita,
                ultimaCorridaAceita = ultimaAceita,
                ofertaAtiva = false,
                corridaAntesDaOferta = null,
            )
            publicarOverlay()
            return
        }
        state = PresentationBuilder.criarEstado(
            analise = ultimaAceita,
            plano = state.plano,
            historico = state.historico,
            historicoSelecionado = state.historicoSelecionado,
            modo = ModoApresentacao.COMPACTA,
            historicoVisivel = false,
            configuracoesVisivel = false,
            interfaceOculta = true,
            overlayAtivo = true,
            notificacaoDisponivel = ultimaAceita != null,
            seloFlutuante = true,
            compactaTemporaria = false,
            monitorando = true,
            seloOffsetX = offsetsX,
            seloOffsetY = offsetsY,
            estadoSalvo = estadoSalvo,
            corridaAceita = ultimaAceita != null,
            ultimaCorridaAceita = ultimaAceita,
            ofertaAtiva = false,
            corridaAntesDaOferta = null,
        )
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
        val compactaVisivel = emOverlay && !expandidaVisivel &&
            (state.ofertaAtiva || state.compactaTemporaria)
        val seloVisivel = emOverlay && !compactaVisivel && !expandidaVisivel
        val campos = state.corrida.camposCompactos.associate { it.id to it.valor }
        val detalhes = state.corrida.camposDetalhes.associate { it.id to it.valor }
        val analise = state.analiseAtual ?: state.ultimaCorridaAceita
        OverlayBridge.publicar(
            OverlaySnapshot(
                monitorando = state.monitorando,
                seloVisivel = seloVisivel,
                compactaVisivel = compactaVisivel,
                expandidaVisivel = expandidaVisivel,
                offsetX = state.seloOffsetX,
                offsetY = state.seloOffsetY,
                valorPorKm = campos["valor_por_km"] ?: "—",
                valorTotal = campos["valor_total"] ?: "—",
                kmTotal = campos["km_total"] ?: "—",
                tempo = campos["tempo_estimado"] ?: "—",
                nota = campos["nota_passageiro"] ?: "—",
                detalhes = state.corrida.camposDetalhes.map { "${it.titulo}: ${it.valor}" },
                aguardandoOferta = !state.ofertaAtiva,
                liquidoPorKm = PresentationBuilder.formatarLiquidoPorKm(analise),
                litrosEstimados = detalhes["combustivel_estimado"] ?: "—",
                gastoEstimado = detalhes["custo_combustivel"] ?: "—",
                lucroEstimado = detalhes["lucro_estimado"] ?: "—",
                kmAtePassageiro = detalhes["km_ate_passageiro"] ?: "—",
                kmViagem = detalhes["km_viagem"] ?: "—",
                enderecoEmbarque = state.analiseAtual?.corrida?.enderecoEmbarque
                    ?: state.ultimaCorridaAceita?.corrida?.enderecoEmbarque,
                enderecoDestino = state.analiseAtual?.corrida?.enderecoDestino
                    ?: state.ultimaCorridaAceita?.corrida?.enderecoDestino,
                corridaAceita = state.corridaAceita,
            ),
        )
    }

    companion object {
        const val COMPACTA_TEMPORARIA_MS = 3_000L
    }
}