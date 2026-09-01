package br.com.gestordriver.data

import android.accounts.AccountManager
import android.content.Intent
import br.com.gestordriver.model.ConfiguracaoUsuario
import br.com.gestordriver.model.TipoContaVinculada

object ContaVinculo {
    fun emailValido(email: String): Boolean {
        val texto = email.trim()
        val dominio = texto.substringAfter('@', "")
        return texto.contains('@') &&
            dominio.contains('.') &&
            texto.length >= 6 &&
            !texto.startsWith('@') &&
            !texto.endsWith('@')
    }

    fun aplicar(
        configuracao: ConfiguracaoUsuario,
        tipo: TipoContaVinculada,
        email: String,
    ): ConfiguracaoUsuario {
        return configuracao.copy(
            contaTipo = tipo,
            contaEmail = email.trim(),
        )
    }

    @Suppress("DEPRECATION")
    fun intentEscolherContaGoogle(): Intent =
        AccountManager.newChooseAccountIntent(
            null,
            null,
            arrayOf("com.google"),
            false,
            null,
            null,
            null,
            null,
        )

    fun emailDaResposta(intent: Intent?): String? =
        intent?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)?.trim()?.takeIf { it.isNotEmpty() }
}
