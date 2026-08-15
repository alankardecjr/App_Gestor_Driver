package br.com.gestordriver.model

data class CorridaPresentation(
    val plano: PlanoAcesso,
    val modo: ModoApresentacao,
    val classificacao: ClassificacaoVisual,
    val corClassificacao: String,
    val acaoDetalhes: String,
    val camposCompactos: List<CampoApresentacao>,
    val camposDetalhes: List<CampoApresentacao>,
)