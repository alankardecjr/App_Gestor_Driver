package br.com.gestordriver.ui

import br.com.gestordriver.core.AnaliseCorrida
import br.com.gestordriver.model.CorridaPresentation
import br.com.gestordriver.model.HistoricoItemPresentation
import br.com.gestordriver.model.ModoApresentacao
import br.com.gestordriver.model.PlanoAcesso

data class EstadoInterfaceSalvo(
    val modo: ModoApresentacao,
    val historicoVisivel: Boolean,
    val configuracoesVisivel: Boolean = false,
)

data class AppState(

    // ================================================================
    // CORRIDA ATUAL
    // ================================================================

    val corrida: CorridaPresentation,

    val analiseAtual: AnaliseCorrida? = null,

    val ultimaCorridaAceita: AnaliseCorrida? = null,

    val ofertaAtiva: Boolean = false,

    // ================================================================
    // PLANO
    // ================================================================

    val plano: PlanoAcesso = PlanoAcesso.BETA,

    // ================================================================
    // HISTÓRICO
    //
    // REGRA DE NEGÓCIO:
    //
    // Somente corridas cujo aceite foi detectado pelo Gestor Driver
    // podem existir nesta lista.
    // ================================================================

    val ofertasPendentes: Map<String, AnaliseCorrida> = emptyMap(),

    val historico: List<HistoricoItemPresentation>,

    val historicoSelecionado:
        HistoricoItemPresentation? = null,

    val abaHistorico: String = "Uber",

    val abaConfiguracao: Int = 0,

    val destacarPermissoes: Boolean = false,

    // ================================================================
    // ACEITE DA CORRIDA ATUAL
    //
    // false = oferta ainda não aceita
    // true  = aceite já detectado
    //
    // Uma nova notificação sempre reinicia este valor para false.
    // ================================================================

    val corridaAceita: Boolean = false,

    // ================================================================
    // INTERFACE
    // ================================================================

    val historicoVisivel: Boolean = false,

    val configuracoesVisivel: Boolean = false,

    val interfaceOculta: Boolean = false,

    val overlayAtivo: Boolean = true,

    val notificacaoDisponivel: Boolean = true,

    // ================================================================
    // SELO
    // ================================================================

    val seloFlutuante: Boolean = false,

    val compactaTemporaria: Boolean = false,

    val corridaAntesDaOferta: AnaliseCorrida? = null,

    // ================================================================
    // MONITORAMENTO
    // ================================================================

    val monitorando: Boolean = true,

    // ================================================================
    // FECHAMENTO
    // ================================================================

    val confirmacaoFecharVisivel: Boolean = false,

    // ================================================================
    // POSIÇÃO DO SELO
    // ================================================================

    val seloOffsetX: Float = 0f,

    val seloOffsetY: Float = 0f,

    // ================================================================
    // ESTADO DA INTERFACE ANTES DE OCULTAR
    // ================================================================

    val estadoSalvo: EstadoInterfaceSalvo? = null,
)