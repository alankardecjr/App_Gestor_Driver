package br.com.gestordriver.model

import br.com.gestordriver.core.AnaliseCorrida

data class PresentationModel(
    val analise: AnaliseCorrida,
    val plano: PlanoAcesso,
    val corrida: CorridaPresentation,
    val historico: List<HistoricoItemPresentation>,
    val modo: ModoApresentacao,
    val historicoVisivel: Boolean,
    val overlayAtivo: Boolean,
    val notificacaoDisponivel: Boolean,
    val seloFlutuante: Boolean,
    val monitorando: Boolean,
)