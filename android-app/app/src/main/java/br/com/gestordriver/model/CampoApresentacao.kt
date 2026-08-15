package br.com.gestordriver.model

data class CampoApresentacao(
    val id: String,
    val titulo: String,
    val valor: String,
    val disponivel: Boolean = true,
    val destaque: Boolean = false,
)