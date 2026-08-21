package br.com.gestordriver.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestordriver.core.AnaliseCorrida
import br.com.gestordriver.model.HistoricoItemPresentation
import br.com.gestordriver.model.ModoApresentacao
import br.com.gestordriver.model.PlanoAcesso
import br.com.gestordriver.notification.RideNotificationBus
import br.com.gestordriver.notification.RideNotificationEvent
import br.com.gestordriver.presentation.PresentationBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {

    // =====================================================================
    // ESTADO
    // =====================================================================

    var state by mutableStateOf(
        PresentationBuilder.criarEstadoInicial()
    )
        private set

    private var notificacaoJob: Job? = null

    // =====================================================================
    // EVENTO DE FECHAMENTO
    // =====================================================================

    private val _fecharApp = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
    )

    val fecharApp: SharedFlow<Unit> =
        _fecharApp.asSharedFlow()

    // =====================================================================
    // MONITORAMENTO DE NOTIFICAÇÕES
    // =====================================================================

    /**
     * Inicia a ponte:
     *
     * NotificationListenerService
     *          ↓
     * RideNotificationBus
     *          ↓
     * AppViewModel
     *
     * Não é executado no init.
     *
     * Isso evita que os testes unitários dependam do Main Dispatcher
     * do Android durante a simples criação do ViewModel.
     */
    fun iniciarMonitoramentoNotificacoes() {

        if (notificacaoJob?.isActive == true) {
            return
        }

        notificacaoJob = viewModelScope.launch {

            RideNotificationBus.events.collect { evento ->

                processarEvento(evento)
            }
        }
    }

    // =====================================================================
    // PLANO
    // =====================================================================

    fun selecionarPlano(
        plano: PlanoAcesso,
    ) {

        val analise =
            state.analiseAtual
                ?: PresentationBuilder
                    .criarEstadoInicial(plano)
                    .analiseAtual
                ?: return

        state = PresentationBuilder.criarEstado(

            analise = analise,

            plano = plano,

            historico = state.historico,

            historicoSelecionado =
                state.historicoSelecionado,

            modo =
                state.corrida.modo,

            historicoVisivel =
                state.historicoVisivel,

            configuracoesVisivel =
                state.configuracoesVisivel,

            interfaceOculta =
                state.interfaceOculta,

            overlayAtivo =
                state.overlayAtivo,

            notificacaoDisponivel =
                state.notificacaoDisponivel,

            seloFlutuante =
                state.seloFlutuante,

            monitorando =
                state.monitorando,

            confirmacaoFecharVisivel =
                state.confirmacaoFecharVisivel,

            seloOffsetX =
                state.seloOffsetX,

            seloOffsetY =
                state.seloOffsetY,
        )
    }

    // =====================================================================
    // EXPANDIR / RETRAIR
    // =====================================================================

    fun alternarDetalhes() {

        if (state.interfaceOculta) {
            return
        }

        val novoModo =
            if (
                state.corrida.modo ==
                ModoApresentacao.COMPACTA
            ) {
                ModoApresentacao.DETALHES
            } else {
                ModoApresentacao.COMPACTA
            }

        state = state.copy(

            corrida = state.corrida.copy(

                modo = novoModo,

                acaoDetalhes =
                    if (
                        novoModo ==
                        ModoApresentacao.DETALHES
                    ) {
                        "Menos detalhes"
                    } else {
                        "ⓘ"
                    },
            ),

            // Ao retrair, o histórico deixa de ser exibido.
            historicoVisivel =
                if (
                    novoModo ==
                    ModoApresentacao.COMPACTA
                ) {
                    false
                } else {
                    state.historicoVisivel
                },
        )
    }

    // =====================================================================
    // HISTÓRICO
    // =====================================================================

    fun alternarHistorico() {

        if (state.interfaceOculta) {
            return
        }

        // Histórico somente na tela expandida.
        if (
            state.corrida.modo !=
            ModoApresentacao.DETALHES
        ) {
            return
        }

        val novoHistoricoVisivel =
            !state.historicoVisivel

        state = state.copy(

            historicoVisivel =
                novoHistoricoVisivel,

            // Histórico e configuração são exclusivos.
            configuracoesVisivel =
                if (novoHistoricoVisivel) {
                    false
                } else {
                    state.configuracoesVisivel
                },
        )
    }

    // =====================================================================
    // CONFIGURAÇÕES
    // =====================================================================

    fun abrirConfiguracoes() {

        if (state.interfaceOculta) {
            return
        }

        state = state.copy(

            configuracoesVisivel = true,

            historicoVisivel = false,
        )
    }

    fun fecharConfiguracoes() {

        state = state.copy(
            configuracoesVisivel = false,
        )
    }

    // =====================================================================
    // HISTÓRICO
    // =====================================================================

    /**
     * Seleciona uma corrida que JÁ ESTÁ no histórico.
     *
     * Esta função NÃO cria uma corrida no histórico.
     */
    fun selecionarHistorico(
        item: HistoricoItemPresentation,
    ) {

        if (state.interfaceOculta) {
            return
        }

        val analise =
            item.paraAnalise()

        state =
            PresentationBuilder.criarEstado(

                analise = analise,

                plano = state.plano,

                // Histórico permanece intacto.
                historico = state.historico,

                historicoSelecionado = item,

                modo =
                    ModoApresentacao.DETALHES,

                historicoVisivel = false,

                configuracoesVisivel = false,

                interfaceOculta = false,

                overlayAtivo = true,

                notificacaoDisponivel =
                    state.notificacaoDisponivel,

                seloFlutuante = false,

                monitorando =
                    state.monitorando,

                confirmacaoFecharVisivel = false,

                seloOffsetX =
                    state.seloOffsetX,

                seloOffsetY =
                    state.seloOffsetY,
            )
    }

    // =====================================================================
    // NOTIFICAÇÃO
    // =====================================================================

    fun registrarNotificacao() {

        state = state.copy(

            overlayAtivo = true,

            notificacaoDisponivel = true,

            seloFlutuante = false,

            interfaceOculta = false,

            historicoVisivel = false,

            configuracoesVisivel = false,

            corrida = state.corrida.copy(

                modo =
                    ModoApresentacao.COMPACTA,

                acaoDetalhes = "ⓘ",
            ),

            monitorando = true,
        )
    }

    // =====================================================================
    // SEM NOTIFICAÇÃO
    // =====================================================================

    fun semNotificacao() {

        state = state.copy(

            historicoVisivel = false,

            configuracoesVisivel = false,

            overlayAtivo = false,

            notificacaoDisponivel = false,

            seloFlutuante = true,

            interfaceOculta = true,

            monitorando = true,

            corrida = state.corrida.copy(

                modo =
                    ModoApresentacao.COMPACTA,

                acaoDetalhes = "ⓘ",
            ),
        )
    }

    // =====================================================================
    // OCULTAR
    // =====================================================================

    /**
     * Ocultar = minimizar para o selo.
     *
     * Ordem obrigatória:
     *
     * 1. Fecha histórico.
     * 2. Fecha configuração.
     * 3. Retrai a corrida.
     * 4. Esconde a interface.
     * 5. Exibe o selo.
     * 6. Mantém o monitoramento.
     */
    fun ocultarInterface() {

        state = state.copy(

            historicoVisivel = false,

            configuracoesVisivel = false,

            corrida = state.corrida.copy(

                modo =
                    ModoApresentacao.COMPACTA,

                acaoDetalhes = "ⓘ",
            ),

            interfaceOculta = true,

            overlayAtivo = false,

            seloFlutuante = true,

            monitorando = true,
        )
    }

    // =====================================================================
    // REABRIR PELO SELO
    // =====================================================================

    /**
     * O selo sempre abre a tela COMPACTA.
     *
     * Não restaura:
     * - histórico;
     * - configuração;
     * - tela expandida.
     */
    fun reabrirInterface() {

        state = state.copy(

            interfaceOculta = false,

            seloFlutuante = false,

            overlayAtivo = true,

            historicoVisivel = false,

            configuracoesVisivel = false,

            corrida = state.corrida.copy(

                modo =
                    ModoApresentacao.COMPACTA,

                acaoDetalhes = "ⓘ",
            ),

            monitorando = true,
        )
    }

    // =====================================================================
    // FECHAMENTO
    // =====================================================================

    fun solicitarFecharApp() {

        state = state.copy(

            confirmacaoFecharVisivel = true,
        )
    }

    fun cancelarFecharApp() {

        state = state.copy(

            confirmacaoFecharVisivel = false,
        )
    }

    fun confirmarFecharApp() {

        state = state.copy(

            confirmacaoFecharVisivel = false,

            historicoVisivel = false,

            configuracoesVisivel = false,

            overlayAtivo = false,

            monitorando = false,

            seloFlutuante = false,

            interfaceOculta = false,
        )

        _fecharApp.tryEmit(Unit)
    }

    // =====================================================================
    // POSIÇÃO DO SELO
    // =====================================================================

    fun atualizarPosicaoSelo(
        offsetX: Float,
        offsetY: Float,
    ) {

        state = state.copy(

            seloOffsetX = offsetX,

            seloOffsetY = offsetY,
        )
    }

    // =====================================================================
    // EVENTOS
    // =====================================================================

    /**
     * Processamento determinístico dos eventos.
     *
     * Também é utilizado pelos testes unitários.
     */
    fun processarEvento(
        evento: RideNotificationEvent,
    ) {

        when (evento) {

            is RideNotificationEvent.CorridaRecebida -> {

                aplicarNovaCorrida(
                    evento.analise
                )
            }

            RideNotificationEvent.NotificacaoNaoReconhecida -> {
                // Não altera o estado.
            }
        }
    }

    // =====================================================================
    // NOVA OFERTA
    // =====================================================================

    private fun aplicarNovaCorrida(
        analise: AnaliseCorrida,
    ) {

        /*
         * REGRA FUNDAMENTAL DA ETAPA 1
         *
         * Notification = OFERTA
         *
         * Oferta:
         * - atualiza corrida atual;
         * - abre interface compacta;
         * - NÃO entra no histórico.
         *
         * O aceite será tratado na próxima etapa.
         */

        state =
            PresentationBuilder.criarEstado(

                analise = analise,

                plano = state.plano,

                // IMPORTANTE:
                // preserva exatamente o histórico existente.
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

                confirmacaoFecharVisivel = false,

                seloOffsetX =
                    state.seloOffsetX,

                seloOffsetY =
                    state.seloOffsetY,
            )
    }

    override fun onCleared() {

        notificacaoJob?.cancel()

        notificacaoJob = null

        super.onCleared()
    }
}