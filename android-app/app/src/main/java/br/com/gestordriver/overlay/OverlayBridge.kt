package br.com.gestordriver.overlay

import br.com.gestordriver.core.ClassificacaoConstantes
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

data class OverlayHistoricoItem(
    val chave: String,
    val data: String,
    val hora: String,
    val valorPorKm: String,
    val valor: String,
    val km: String,
    val tempo: String,
    val nota: String,
    val marcador: String,
    val corMarcador: String,
    val plataforma: String,
    val lucro: String = "—",
    val gasto: String = "—",
    val consumo: String = "—",
    val tempoHm: String = "—",
    val cabecalhoData: String = "",
    val embarque: String? = null,
    val destino: String? = null,
    val classificacao: String = "",
    val valorTotalNum: Double = 0.0,
    val kmNum: Double = 0.0,
    val minutosNum: Int = 0,
    val gastoNum: Double? = null,
)

data class OverlaySnapshot(
    val monitorando: Boolean = false,
    val seloVisivel: Boolean = false,
    val compactaVisivel: Boolean = false,
    val expandidaVisivel: Boolean = false,
    val historicoVisivel: Boolean = false,
    val configuracoesVisivel: Boolean = false,
    val dashboardVisivel: Boolean = false,
    val confirmacaoFecharVisivel: Boolean = false,
    val confirmacaoLimparHistoricoVisivel: Boolean = false,
    val historicoAba: String = "Todos",
    val historicoFaturamento: String = "—",
    val historicoDistancia: String = "—",
    val historicoGasto: String = "—",
    val historicoLucro: String = "—",
    val historicoChaveSelecionada: String = "",
    val historicoChavesSelecionadas: List<String> = emptyList(),
    val historicoLimparQuantidade: Int = 0,
    val planoPro: Boolean = true,
    val dashboardGanhosDia: String = "—",
    val dashboardGanhosSemana: String = "—",
    val dashboardGanhosMes: String = "—",
    val historicoEpochDay: Long = br.com.gestordriver.core.CalendarioApp.hoje().toEpochDay(),
    val historicoDiasComCorrida: List<Long> = emptyList(),
    val historicoPeriodo: String = br.com.gestordriver.core.CalendarioPeriodo.DIA.name,
    val historicoItens: List<OverlayHistoricoItem> = emptyList(),
    val destacarPermissoes: Boolean = false,
    val abaConfiguracao: Int = 0,
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
    val quantidadeParadas: Int = 0,
    val plataformaSigla: String = "",
    val tempoHm: String = "—",
    val enderecoEmbarque: String? = null,
    val enderecoDestino: String? = null,
    val corridaAceita: Boolean = false,
    val corClassificacao: String = ClassificacaoConstantes.COR_BORDA_NEUTRA,
) {
    val confirmacaoVisivel: Boolean
        get() = confirmacaoFecharVisivel || confirmacaoLimparHistoricoVisivel
}

sealed class OverlayAcao {
    data class Reabrir(val origemCompacta: Boolean = false) : OverlayAcao()
    data class MoverSelo(val offsetX: Float, val offsetY: Float) : OverlayAcao()
    data object AbrirHistorico : OverlayAcao()
    data object AbrirConfig : OverlayAcao()
    data object SalvarConfig : OverlayAcao()
    data object CancelarConfig : OverlayAcao()
    data object Ocultar : OverlayAcao()
    data object Retratil : OverlayAcao()
    data object ToqueForaDaCompacta : OverlayAcao()
    data object RecolherParaSelo : OverlayAcao()
    data object VoltarAtalho : OverlayAcao()
    data object VoltarBarra : OverlayAcao()
    data object RecentesBarra : OverlayAcao()
    data object EsconderSelo : OverlayAcao()
    data object SairParaMapaHistorico : OverlayAcao()
    data object DashboardPro : OverlayAcao()
    data object FecharDashboard : OverlayAcao()
    data class AbrirAtalhoConfig(val indice: Int) : OverlayAcao()
    data class HistoricoDia(val epochDay: Long) : OverlayAcao()
    data class HistoricoSemana(val deltaSemanas: Int) : OverlayAcao()
    data class HistoricoMes(val deltaMeses: Int) : OverlayAcao()
    data class HistoricoModo(val periodo: String) : OverlayAcao()
    data class HistoricoAvancar(val delta: Int) : OverlayAcao()
    data object Fechar : OverlayAcao()
    data object CancelarFechar : OverlayAcao()
    data object ConfirmarFechar : OverlayAcao()
    data object SolicitarLimparHistorico : OverlayAcao()
    data object CancelarLimparHistorico : OverlayAcao()
    data object ConfirmarLimparHistorico : OverlayAcao()
    data class SelecionarHistorico(val chave: String) : OverlayAcao()
    data class AbaHistorico(val aba: String) : OverlayAcao()
    data class AbaConfiguracao(val indice: Int) : OverlayAcao()
}

object OverlayBridge {
    private val _snapshot = MutableStateFlow(OverlaySnapshot())
    val snapshot: StateFlow<OverlaySnapshot> = _snapshot.asStateFlow()

    private val _acoes = MutableSharedFlow<OverlayAcao>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val acoes: SharedFlow<OverlayAcao> = _acoes.asSharedFlow()

    @Volatile
    private var pausarLeituraAteMs: Long = 0L

    fun leituraPausada(): Boolean = System.currentTimeMillis() < pausarLeituraAteMs

    fun pausarLeitura(duracaoMs: Long = 800L) {
        pausarLeituraAteMs = System.currentTimeMillis() + duracaoMs
    }

    fun publicar(snapshot: OverlaySnapshot) {
        if (_snapshot.value == snapshot) {
            return
        }
        _snapshot.value = snapshot
    }

    fun emitir(acao: OverlayAcao) {
        if (acao !is OverlayAcao.MoverSelo) {
            pausarLeitura()
        }
        _acoes.tryEmit(acao)
    }

    private val _reafirmarCamada = MutableSharedFlow<Unit>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val reafirmarCamada: SharedFlow<Unit> = _reafirmarCamada.asSharedFlow()

    fun reafirmarCamada() {
        _reafirmarCamada.tryEmit(Unit)
    }
}
