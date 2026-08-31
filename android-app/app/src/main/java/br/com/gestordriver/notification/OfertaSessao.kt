package br.com.gestordriver.notification

import java.util.concurrent.ConcurrentHashMap

/**
 * Uma sessão por plataforma. Uber e 99 podem ter oferta ao mesmo
 * tempo; o aceite de uma não apaga a outra.
 */
object OfertaSessao {
    private data class Estado(
        var chave: String? = null,
        var aceite: Boolean = false,
        var assinatura: String? = null,
    )

    private val sessoes = ConcurrentHashMap<String, Estado>()

    fun chaveAtiva(pacote: String = ""): Boolean {
        if (pacote.isNotBlank()) {
            val estado = sessoes[pacote] ?: return false
            return estado.chave != null && !estado.aceite
        }
        return sessoes.values.any { it.chave != null && !it.aceite }
    }

    fun aceiteDetectado(pacote: String = ""): Boolean {
        if (pacote.isNotBlank()) {
            return sessoes[pacote]?.aceite == true
        }
        return sessoes.values.any { it.aceite }
    }

    fun registrarOferta(chave: String?, pacote: String = "") {
        val estado = sessoes.getOrPut(pacote.ifBlank { "_" }) { Estado() }
        estado.chave = chave
        estado.aceite = false
    }

    fun mesmaOferta(assinatura: String, pacote: String = ""): Boolean {
        val chave = pacote.ifBlank { return false }
        return sessoes[chave]?.assinatura == assinatura
    }

    fun registrarAssinatura(assinatura: String, pacote: String = "") {
        sessoes.getOrPut(pacote.ifBlank { "_" }) { Estado() }.assinatura = assinatura
    }

    fun registrarAceite(pacote: String = "") {
        val estado = sessoes[pacote.ifBlank { return }] ?: return
        estado.aceite = true
    }

    fun deveExpirar(chave: String): Boolean {
        return sessoes.values.any { estado ->
            estado.chave == chave && estado.chave != null && !estado.aceite
        }
    }

    fun limpar(pacote: String = "") {
        if (pacote.isNotBlank()) {
            sessoes.remove(pacote)
            return
        }
        sessoes.clear()
    }

    fun limparPorChave(chave: String) {
        sessoes.entries.removeIf { it.value.chave == chave }
    }
}
