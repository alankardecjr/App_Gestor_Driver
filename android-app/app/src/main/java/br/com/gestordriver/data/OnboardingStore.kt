package br.com.gestordriver.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

interface OnboardingStore {
    fun concluido(): Boolean

    fun marcarConcluido()
}

class MemoriaOnboardingStore(
    inicial: Boolean = true,
) : OnboardingStore {
    private var valor = inicial

    override fun concluido(): Boolean = valor

    override fun marcarConcluido() {
        valor = true
    }
}

private val Context.onboardingDataStore by preferencesDataStore(name = "gestor_driver_onboarding")

class PreferencesOnboardingStore(
    private val context: Context,
) : OnboardingStore {
    override fun concluido(): Boolean = runCatching {
        runBlocking(Dispatchers.IO) {
            context.onboardingDataStore.data.first()[CHAVE] ?: false
        }
    }.getOrDefault(false)

    override fun marcarConcluido() {
        runCatching {
            runBlocking(Dispatchers.IO) {
                context.onboardingDataStore.edit { prefs ->
                    prefs[CHAVE] = true
                }
            }
        }
    }

    private companion object {
        val CHAVE = booleanPreferencesKey("onboardingConcluido")
    }
}
