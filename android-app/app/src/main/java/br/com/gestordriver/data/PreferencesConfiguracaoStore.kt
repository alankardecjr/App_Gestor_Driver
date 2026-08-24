package br.com.gestordriver.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.gestordriver.model.ConfiguracaoUsuario
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.configuracaoDataStore by preferencesDataStore(name = "gestor_driver_config")

class PreferencesConfiguracaoStore(
    private val context: Context,
) : ConfiguracaoStore {
    override fun carregar(): ConfiguracaoUsuario = runBlocking {
        val prefs = context.configuracaoDataStore.data.first()
        val mapa = prefs.asMap().mapKeys { it.key.name }.mapValues { it.value.toString() }
        if (mapa.isEmpty()) ConfiguracaoUsuario.padrao() else mapa.paraConfiguracaoUsuario()
    }

    override fun salvar(configuracao: ConfiguracaoUsuario) {
        runBlocking {
            context.configuracaoDataStore.edit { prefs ->
                configuracao.paraPreferencias().forEach { (chave, valor) ->
                    prefs[stringPreferencesKey(chave)] = valor
                }
            }
        }
    }
}
