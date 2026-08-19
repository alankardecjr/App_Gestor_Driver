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

    // =====================================================================
    // EVENTO DE FECHAMENTO
    // =====================================================================

    private val _fecharApp = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
    )

    val fecharApp: SharedFlow<Unit> =
        _fecharApp.asSharedFlow()

    // =====================================================================
    // OBSERVAÇÃO DAS NOTIFICAÇÕES
    // =====================================================================

    init {
        viewModelScope.launch {
            RideNotificationBus.events.collect { evento ->

                when (evento) {

                    // -----------------------------------------------------
                    // NOVA OFERTA DE CORRIDA
                    //
                    // IMPORTANTE:
                    //
                    // A nova corrida NÃO entra no histórico.
                    //
                    // O usuário ainda precisa interagir com Uber,
                    // 99 ou inDrive.
                    //
                    // A detecção do aceite será implementada
                    // posteriormente na ETAPA 2.
                    // -----------------------------------------------------

                    is RideNotificationEvent.CorridaRecebida -> {
                        aplicarNovaCorrida(
                            evento.analise
                        )
                    }

                    // -----------------------------------------------------
                    // NOTIFICAÇÃO NÃO RECONHECIDA
                    // -----------------------------------------------------

                    RideNotificationEvent.NotificacaoNaoReconhecida -> {
                        // Nenhuma alteração no estado.
                    }
                }
            }
        }
    }

    // =====================================================================
    // PLANO
    // =====================================================================

    fun selecionarPlano(
        plano: PlanoAcesso,
    ) {
        val analise = state.analiseAtual
            ?: PresentationBuilder
                .criarEstadoInicial(plano)
                .analiseAtual
            ?: return

        state = PresentationBuilder.criarEstado(
            analise = analise,
            plano = plano,

            // Histórico atual permanece intacto.
            historico = state.historico,

            modo = state.corrida.modo,
            historicoVisivel = state.historicoVisivel,
            configuracoesVisivel = state.configuracoesVisivel,
            interfaceOculta = state.interfaceOculta,
            overlayAtivo = state.overlayAtivo,
            notificacaoDisponivel = true,
            seloFlutuante = state.seloFlutuante,
            monitorando = state.monitorando,
            seloOffsetX = state.seloOffsetX,
            seloOffsetY = state.seloOffsetY,
            estadoSalvo = state.estadoSalvo,
        )
    }

    // =====================================================================
    // EXPANDIR / RETRAIR
    // =====================================================================

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

        state = state.copy(
            corrida = state.corrida.copy(
                modo = modo,
                acaoDetalhes =
                    if (
                        modo ==
                        ModoApresentacao.DETALHES
                    ) {
                        "Menos detalhes"
                    } else {
                        "ⓘ"
                    },
            ),
        )
    }

    // =====================================================================
    // HISTÓRICO
    // =====================================================================

    fun alternarHistorico() {

        val historicoVisivel =
            !state.historicoVisivel

        state = state.copy(
            historicoVisivel = historicoVisivel,

            configuracoesVisivel =
                if (historicoVisivel) {
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
    // SELEÇÃO DE ITEM DO HISTÓRICO
    //
    // IMPORTANTE:
    //
    // Aqui estamos apenas consultando uma corrida que já está
    // no histórico.
    //
    // Nenhuma nova corrida é criada no histórico.
    // =====================================================================

    fun selecionarHistorico(
        item: HistoricoItemPresentation,
    ) {

        val analise =
            item.paraAnalise()

        state = PresentationBuilder.criarEstado(
            analise = analise,
            plano = state.plano,

            // Histórico permanece intacto.
            historico = state.historico,

            historicoSelecionado = item,

            modo = ModoApresentacao.DETALHES,

            historicoVisivel = false,
            configuracoesVisivel = false,

            interfaceOculta = state.interfaceOculta,
            overlayAtivo = !state.interfaceOculta,

            notificacaoDisponivel =
                state.notificacaoDisponivel,

            seloFlutuante =
                state.seloFlutuante,

            monitorando =
                state.monitorando,

            seloOffsetX =
                state.seloOffsetX,

            seloOffsetY =
                state.seloOffsetY,

            estadoSalvo =
                state.estadoSalvo,
        )
    }

    // =====================================================================
    // CONTROLE DE NOTIFICAÇÃO / INTERFACE
    // =====================================================================

    fun registrarNotificacao() {

        state = state.copy(
            overlayAtivo = true,
            notificacaoDisponivel = true,
            seloFlutuante = false,
            interfaceOculta = false,
            monitorando = true,
        )
    }

    fun semNotificacao() {

        state = state.copy(
            historicoVisivel = false,
            configuracoesVisivel = false,
            overlayAtivo = false,
            notificacaoDisponivel = false,
            seloFlutuante = true,
            interfaceOculta = true,
            monitorando = true,
        )
    }

    // =====================================================================
    // OCULTAR
    // =====================================================================

    fun ocultarInterface() {

        state = state.copy(

            estadoSalvo = EstadoInterfaceSalvo(
                modo = state.corrida.modo,
                historicoVisivel =
                    state.historicoVisivel,
                configuracoesVisivel =
                    state.configuracoesVisivel,
            ),

            interfaceOculta = true,
            seloFlutuante = true,

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
    // REABRIR INTERFACE
    // =====================================================================

    fun reabrirInterface() {

        val salvo =
            state.estadoSalvo

        state = state.copy(

            interfaceOculta = false,
            seloFlutuante = false,
            overlayAtivo = true,
            monitorando = true,

            corrida = state.corrida.copy(
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
                    },
            ),

            historicoVisivel =
                salvo?.historicoVisivel
                    ?: false,

            configuracoesVisivel =
                salvo?.configuracoesVisivel
                    ?: false,

            estadoSalvo = null,
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

            configuracoesVisivel = false,
            historicoVisivel = false,

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
    // NOVA CORRIDA / NOVA OFERTA
    // =====================================================================
    //
    // REGRA DE NEGÓCIO:
    //
    // Uma nova notificação representa uma nova oferta.
    //
    // Ela:
    //
    // 1. Atualiza a corrida atual;
    // 2. Atualiza analiseAtual;
    // 3. Exibe a interface compacta;
    // 4. NÃO altera o histórico.
    //
    // O aceite será tratado posteriormente.
    // =====================================================================

    private fun aplicarNovaCorrida(
        analise: AnaliseCorrida,
    ) {

        state = PresentationBuilder.criarEstado(
            analise = analise,
            plano = state.plano,

            // =============================================================
            // REGRA PRINCIPAL DA ETAPA 1
            //
            // O histórico existente é preservado exatamente como está.
            // A nova oferta NÃO é adicionada.
            // =============================================================

            historico = state.historico,

            historicoSelecionado = null,

            modo = ModoApresentacao.COMPACTA,

            historicoVisivel = false,
            configuracoesVisivel = false,

            interfaceOculta = false,

            overlayAtivo = true,

            notificacaoDisponivel = true,

            seloFlutuante = false,

            monitorando = true,

            seloOffsetX = state.seloOffsetX,
            seloOffsetY = state.seloOffsetY,

            estadoSalvo = state.estadoSalvo,
        )
    }
}