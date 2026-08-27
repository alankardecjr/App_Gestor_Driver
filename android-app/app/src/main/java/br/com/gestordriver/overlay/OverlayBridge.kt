package br.com.gestordriver.overlay

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

data class OverlaySnapshot(
    val monitorando: Boolean = false,
    val seloVisivel: Boolean = false,
    val compactaVisivel: Boolean = false,
    val expandidaVisivel: Boolean = false,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val valorPorKm: String = "—",
    val valorTotal: String = "—",
    val kmTotal: String = "—",
    val tempo: String = "—",
    val nota: String = "—",
    val detalhes: List<String> = emptyList(),
    val aguardandoOferta: Boolean = true,
    val liquidoPorKm: String = "—",
    val litrosEstimados: String = "—",
    val gastoEstimado: String = "—",
    val lucroEstimado: String = "—",
    val kmAtePassageiro: String = "—",
    val kmViagem: String = "—",
    val enderecoEmbarque: String? = null,
    val enderecoDestino: String? = null,
    val corridaAceita: Boolean = false,
)

sealed class OverlayAcao {
    data class Reabrir(val origemCompacta: Boolean = false) : OverlayAcao()
    data class MoverSelo(val offsetX: Float, val offsetY: Float) : OverlayAcao()
    data object AbrirHistorico : OverlayAcao()
    data object AbrirConfig : OverlayAcao()
    data object Ocultar : OverlayAcao()
    data object Retratil : OverlayAcao()
    data object Fechar : OverlayAcao()
}

object OverlayBridge {
    private val _snapshot = MutableStateFlow(OverlaySnapshot())
    val snapshot: StateFlow<OverlaySnapshot> = _snapshot.asStateFlow()

    private val _acoes = MutableSharedFlow<OverlayAcao>(extraBufferCapacity = 16)
    val acoes: SharedFlow<OverlayAcao> = _acoes.asSharedFlow()

    fun publicar(snapshot: OverlaySnapshot) {
        _snapshot.value = snapshot
    }

    fun emitir(acao: OverlayAcao) {
        _acoes.tryEmit(acao)
    }
}
