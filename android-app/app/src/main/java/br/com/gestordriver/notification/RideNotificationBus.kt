package br.com.gestordriver.notification

import br.com.gestordriver.core.AnaliseCorrida
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class RideNotificationEvent {

    data class CorridaRecebida(
        val analise: AnaliseCorrida,
    ) : RideNotificationEvent()

    data object NotificacaoNaoReconhecida :
        RideNotificationEvent()
}

object RideNotificationBus {

    private val _events =
        MutableSharedFlow<RideNotificationEvent>(
            extraBufferCapacity = 64,
        )

    val events: SharedFlow<RideNotificationEvent> =
        _events.asSharedFlow()

    fun publish(
        event: RideNotificationEvent,
    ) {
        _events.tryEmit(event)
    }
}