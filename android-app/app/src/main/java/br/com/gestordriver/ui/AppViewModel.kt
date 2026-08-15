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

    var state by mutableStateOf(PresentationBuilder.criarEstadoInicial())
        private set

    private val _fecharApp = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val fecharApp: SharedFlow<Unit> = _fecharApp.asSharedFlow()

    init {
        viewModelScope.launch {
            RideNotificationBus.events.collect { evento ->
                when (evento) {
                    is RideNotificationEvent.CorridaRecebida -> aplicarNovaCorrida(evento.analise)
                    RideNotificationEvent.NotificacaoNaoReconhecida -> Unit
                }
            }
        }
    }

    fun selecionarPlano(plano: PlanoAcesso) {
        val analise = state.analiseAtual
            ?: PresentationBuilder.criarEstadoInicial(plano).analiseAtual
            ?: return

        state = PresentationBuilder.criarEstado(
            analise = analise,
            plano = plano,
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

    fun alternarDetalhes() {
        val modo = if (state.corrida.modo == ModoApresentacao.COMPACTA) {
            ModoApresentacao.DETALHES
        } else {
            ModoApresentacao.COMPACTA
        }

        state = state.copy(
            corrida = state.corrida.copy(
                modo = modo,
                acaoDetalhes = if (modo == ModoApresentacao.DETALHES) {
                    "Menos detalhes"
                } else {
                    "ⓘ"
                },
            ),
        )
    }

    fun alternarHistorico() {
        val historicoVisivel = !state.historicoVisivel

        state = state.copy(
            historicoVisivel = historicoVisivel,
            configuracoesVisivel = if (historicoVisivel) false else state.configuracoesVisivel,
        )
    }

    fun abrirConfiguracoes() {
        state = state.copy(
            configuracoesVisivel = true,
            historicoVisivel = false,
        )
    }

    fun fecharConfiguracoes() {
        state = state.copy(configuracoesVisivel = false)
    }

    fun selecionarHistorico(item: HistoricoItemPresentation) {
        val analise = item.paraAnalise()

        state = PresentationBuilder.criarEstado(
            analise = analise,
            plano = state.plano,
            historico = state.historico,
            historicoSelecionado = item,
            modo = ModoApresentacao.DETALHES,
            historicoVisivel = false,
            configuracoesVisivel = false,
            interfaceOculta = state.interfaceOculta,
            overlayAtivo = !state.interfaceOculta,
            notificacaoDisponivel = state.notificacaoDisponivel,
            seloFlutuante = state.seloFlutuante,
            monitorando = state.monitorando,
            seloOffsetX = state.seloOffsetX,
            seloOffsetY = state.seloOffsetY,
            estadoSalvo = state.estadoSalvo,
        )
    }

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

    fun ocultarInterface() {
        state = state.copy(
            estadoSalvo = EstadoInterfaceSalvo(
                modo = state.corrida.modo,
                historicoVisivel = state.historicoVisivel,
                configuracoesVisivel = state.configuracoesVisivel,
            ),
            interfaceOculta = true,
            seloFlutuante = true,
            historicoVisivel = false,
            configuracoesVisivel = false,
            corrida = state.corrida.copy(
                modo = ModoApresentacao.COMPACTA,
                acaoDetalhes = "ⓘ",
            ),
            monitorando = true,
        )
    }

    fun reabrirInterface() {
        val salvo = state.estadoSalvo

        state = state.copy(
            interfaceOculta = false,
            seloFlutuante = false,
            overlayAtivo = true,
            monitorando = true,
            corrida = state.corrida.copy(
                modo = salvo?.modo ?: ModoApresentacao.COMPACTA,
                acaoDetalhes = if (salvo?.modo == ModoApresentacao.DETALHES) {
                    "Menos detalhes"
                } else {
                    "ⓘ"
                },
            ),
            historicoVisivel = salvo?.historicoVisivel ?: false,
            configuracoesVisivel = salvo?.configuracoesVisivel ?: false,
            estadoSalvo = null,
        )
    }

    fun solicitarFecharApp() {
        state = state.copy(confirmacaoFecharVisivel = true)
    }

    fun cancelarFecharApp() {
        state = state.copy(confirmacaoFecharVisivel = false)
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

    fun atualizarPosicaoSelo(offsetX: Float, offsetY: Float) {
        state = state.copy(
            seloOffsetX = offsetX,
            seloOffsetY = offsetY,
        )
    }

    private fun aplicarNovaCorrida(analise: AnaliseCorrida) {
        val novoHistorico = buildList {
            add(PresentationBuilder.historicoDe(analise))
            addAll(state.historico.take(9))
        }

        state = PresentationBuilder.criarEstado(
            analise = analise,
            plano = state.plano,
            historico = novoHistorico,
            historicoSelecionado = null,
            modo = ModoApresentacao.COMPACTA,
            historicoVisivel = false,
            configuracoesVisivel = false,
            interfaceOculta = state.interfaceOculta,
            overlayAtivo = !state.interfaceOculta,
            notificacaoDisponivel = true,
            seloFlutuante = state.interfaceOculta,
            monitorando = state.monitorando,
            seloOffsetX = state.seloOffsetX,
            seloOffsetY = state.seloOffsetY,
            estadoSalvo = state.estadoSalvo,
        )
    }
}
