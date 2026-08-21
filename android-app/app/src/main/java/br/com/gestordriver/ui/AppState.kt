package br.com.gestordriver.ui

import br.com.gestordriver.core.AnaliseCorrida
import br.com.gestordriver.model.CorridaPresentation
import br.com.gestordriver.model.HistoricoItemPresentation
import br.com.gestordriver.model.PlanoAcesso

/**
 * Estado único da interface do Gestor Driver.
 *
 * Regras da Etapa 1:
 *
 * - Corrida recebida = oferta atual.
 * - Oferta não entra automaticamente no histórico.
 * - Histórico será alimentado somente após identificação do aceite.
 * - Ocultar fecha histórico/configuração e leva ao selo.
 * - Toque no selo abre somente a tela compacta.
 * - Fechar exige confirmação.
 * - Confirmar fechamento encerra o monitoramento.
 */
data class AppState(
    val corrida: CorridaPresentation,

    val analiseAtual: AnaliseCorrida? = null,

    val plano: PlanoAcesso = PlanoAcesso.BETA,

    // ================================================================
    // HISTÓRICO
    // ================================================================

    val historico: List<HistoricoItemPresentation>,

    val historicoSelecionado: HistoricoItemPresentation? = null,

    val historicoVisivel: Boolean = false,

    // ================================================================
    // CONFIGURAÇÕES
    // ================================================================

    val configuracoesVisivel: Boolean = false,

    // ================================================================
    // INTERFACE
    // ================================================================

    val interfaceOculta: Boolean = false,

    val overlayAtivo: Boolean = false,

    val notificacaoDisponivel: Boolean = false,

    // ================================================================
    // SELO
    // ================================================================

    val seloFlutuante: Boolean = true,

    val monitorando: Boolean = true,

    val seloOffsetX: Float = 0f,

    val seloOffsetY: Float = 0f,

    // ================================================================
    // FECHAMENTO
    // ================================================================

    val confirmacaoFecharVisivel: Boolean = false,
)