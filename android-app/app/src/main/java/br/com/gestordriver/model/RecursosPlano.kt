package br.com.gestordriver.model

data class RecursosPlano(
    val exibeValorPorKm: Boolean,
    val exibeCombustivelEstimado: Boolean,
    val exibeCustoCombustivel: Boolean,
    val recursosAvancados: Boolean,
) {
    companion object {
        val FREE = RecursosPlano(
            exibeValorPorKm = false,
            exibeCombustivelEstimado = false,
            exibeCustoCombustivel = false,
            recursosAvancados = false,
        )
        val PRO = RecursosPlano(
            exibeValorPorKm = true,
            exibeCombustivelEstimado = true,
            exibeCustoCombustivel = true,
            recursosAvancados = true,
        )

        fun de(plano: PlanoAcesso): RecursosPlano =
            if (plano.ehPro) PRO else FREE
    }
}
