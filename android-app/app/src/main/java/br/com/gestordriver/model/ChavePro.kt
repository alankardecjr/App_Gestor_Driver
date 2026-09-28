package br.com.gestordriver.model

/**
 * Liberação provisória da versão Pro, antes da assinatura da Play.
 * A compra na loja entra depois por [assinaturaPlayAtiva], no mesmo ponto.
 */
object ChavePro {
    const val TEXTO = "GestorDrivePro"

    fun aceita(digitada: String): Boolean = digitada.trim() == TEXTO

    fun plano(liberada: Boolean, assinaturaPlayAtiva: Boolean = false): PlanoAcesso =
        if (liberada || assinaturaPlayAtiva) PlanoAcesso.PRO else PlanoAcesso.FREE
}
