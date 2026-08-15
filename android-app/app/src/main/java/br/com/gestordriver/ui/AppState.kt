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
    val corrida: CorridaPresentation,
    val analiseAtual: AnaliseCorrida? = null,
    val plano: PlanoAcesso = PlanoAcesso.BETA,
    val historico: List<HistoricoItemPresentation>,
    val historicoSelecionado: HistoricoItemPresentation? = null,
    val historicoVisivel: Boolean = false,
    val configuracoesVisivel: Boolean = false,
    val interfaceOculta: Boolean = false,
    val overlayAtivo: Boolean = true,
    val notificacaoDisponivel: Boolean = true,
    val seloFlutuante: Boolean = false,
    val monitorando: Boolean = true,
    val confirmacaoFecharVisivel: Boolean = false,
    val seloOffsetX: Float = 0f,
    val seloOffsetY: Float = 0f,
    val estadoSalvo: EstadoInterfaceSalvo? = null,
)
