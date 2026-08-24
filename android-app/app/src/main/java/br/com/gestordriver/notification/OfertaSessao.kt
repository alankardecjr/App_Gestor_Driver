package br.com.gestordriver.notification

/**
 * Controla a oferta atualmente monitorada.
 *
 * A remoção da notificação correspondente, sem aceite prévio,
 * é a evidência Android usada para expirar a oferta.
 * Nenhuma notificação desconhecida é tratada como aceite.
 */
object OfertaSessao {
    @Volatile
    private var chaveNotificacao: String? = null

    @Volatile
    var aceiteDetectado: Boolean = false
        private set

    fun registrarOferta(chave: String?) {
        chaveNotificacao = chave
        aceiteDetectado = false
    }

    fun registrarAceite() {
        aceiteDetectado = true
    }

    fun deveExpirar(chave: String): Boolean {
        return chave == chaveNotificacao &&
            chaveNotificacao != null &&
            !aceiteDetectado
    }

    fun limpar() {
        chaveNotificacao = null
        aceiteDetectado = false
    }
}
