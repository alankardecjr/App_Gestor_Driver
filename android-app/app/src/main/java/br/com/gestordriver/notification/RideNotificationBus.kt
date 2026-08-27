package br.com.gestordriver.notification

import br.com.gestordriver.core.AnaliseCorrida
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class RideNotificationEvent {

    // ================================================================
    // NOVA OFERTA
    //
    // Uma notificação reconhecida representa uma corrida atual.
    // NÃO significa aceite.
    // ================================================================

    data class CorridaRecebida(
        val analise: AnaliseCorrida,
        val aceiteImediato: Boolean = false,
    ) : RideNotificationEvent()

    // ================================================================
    // ACEITE DETECTADO
    //
    // Este evento será publicado posteriormente pelo mecanismo
    // responsável por identificar que o usuário aceitou a corrida
    // diretamente no Uber, 99 ou inDrive.
    // ================================================================

    data object CorridaAceita : RideNotificationEvent()

    // ================================================================
    // OFERTA EXPIRADA / REMOVIDA SEM ACEITE
    // ================================================================

    data object CorridaExpirada : RideNotificationEvent()

    // ================================================================
    // NOTIFICAÇÃO NÃO RECONHECIDA
    // ================================================================

    data object NotificacaoNaoReconhecida : RideNotificationEvent()
}

object RideNotificationBus {

    private val scope =
        CoroutineScope(
            SupervisorJob() +
                Dispatchers.Default,
        )

    private val _events =
        MutableSharedFlow<RideNotificationEvent>(
            extraBufferCapacity = 64,
        )

    val events: SharedFlow<RideNotificationEvent> =
        _events.asSharedFlow()

    fun publish(
        event: RideNotificationEvent,
    ) {
        if (!_events.tryEmit(event)) {
            scope.launch {
                _events.emit(event)
            }
        }
    }
}